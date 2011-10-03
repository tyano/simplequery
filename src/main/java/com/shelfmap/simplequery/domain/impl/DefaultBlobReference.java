/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery.domain.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.shelfmap.simplequery.Client;
import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.domain.BlobContentConverter;
import com.shelfmap.simplequery.domain.BlobOutputException;
import com.shelfmap.simplequery.domain.BlobReference;
import com.shelfmap.simplequery.domain.BlobRestoreException;
import com.shelfmap.simplequery.domain.S3ResourceInfo;
import com.shelfmap.simplequery.util.IO;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <T>
 * @author Tsutomu YANO
 */
public class DefaultBlobReference<T> implements BlobReference<T> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBlobReference.class);
    private final S3ResourceInfo resourceInfo;
    private final Class<T> targetClass;
    private final BlobContentConverter<T> converter;
    private final Context context;
    private Upload lastUpload;

    private ObjectMetadata metadata;

    public DefaultBlobReference(Context context, S3ResourceInfo resourceInfo, Class<T> targetClass, BlobContentConverter<T> converter) {
        this.resourceInfo = resourceInfo;
        this.targetClass = targetClass;
        this.converter = converter;
        this.context = context;
    }

    @Override
    public T getContent() throws BlobRestoreException {
        InputStream resourceStream = null;
        try {
            resourceStream = getInputStream();
            T content = getContentConverter().restoreObject(getObjectMetadata(), resourceStream);
            return content;
        } finally {
            IO.close(resourceStream, this);
        }
    }

    @Override
    public InputStream getInputStream() {
        S3Object resource = getS3ObjectRemote();
        this.metadata = resource.getObjectMetadata();
        return resource.getObjectContent();
    }

    @Override
    public ObjectMetadata getObjectMetadata() {
        if(this.metadata == null) {
            S3Object resource = getS3ObjectRemote();
            this.metadata = resource.getObjectMetadata();
        }
        return this.metadata;
    }

    private S3Object getS3ObjectRemote() {
        String bucket = resourceInfo.getBucketName();
        String key = resourceInfo.getKey();
        AmazonS3 s3 = getContext().createNewClient().getS3();

        GetObjectRequest request = new GetObjectRequest(bucket, key);
        return s3.getObject(request);
    }

    @Override
    public void setContent(T object, ObjectMetadata metadata) throws BlobOutputException {
        Upload upload = setContentAsync(object, metadata);
        try {
            upload.waitForCompletion();
        } catch (AmazonServiceException ex) {
            throw new BlobOutputException("AWS returned an error response, or client can not understand the response.", ex);
        } catch (AmazonClientException ex) {
            throw new BlobOutputException("Client could not send request, or could not receive the response.", ex);
        } catch (InterruptedException ex) {
            LOGGER.warn("Thread was interrupted. Program will continue but something wrong might be occured.", ex);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Upload setContentAsync(T object, ObjectMetadata metadata) throws BlobOutputException {
        InputStream source = getContentConverter().objectToStream(object);
        return uploadFrom(source, metadata);
    }

    @Override
    public S3ResourceInfo getResourceInfo() {
        return this.resourceInfo;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    @Override
    public BlobContentConverter<T> getContentConverter() {
        return converter;
    }

    @Override
    public Upload uploadFrom(InputStream uploadSource, ObjectMetadata metadata) throws BlobOutputException {
        String bucket = resourceInfo.getBucketName();
        String key = resourceInfo.getKey();

        try {
            PutObjectRequest request = new PutObjectRequest(bucket, key, uploadSource, metadata);
            TransferManager transfer = new TransferManager(getContext().getCredentials());
            this.lastUpload = transfer.upload(request);
            return this.lastUpload;
        } catch (AmazonServiceException ex) {
            throw new BlobOutputException("a problem occured in Amazon S3.", ex);
        } catch (AmazonClientException ex) {
            throw new BlobOutputException("Client had an problem when uploading data.", ex);
        }
    }

    private static final int BUFFER_SIZE = 1024 * 500;

    @Override
    public OutputStream getUploadStream(ObjectMetadata metadata) throws BlobOutputException {
        PipedOutputStream output = null;
        PipedInputStream source = null;
        try {
            output = new PipedOutputStream();
            source = new PipedInputStream(BUFFER_SIZE);
            output.connect(source);
        } catch (IOException ex) {
            IO.close(output, this);
            IO.close(source, this);
            throw new BlobOutputException("Could not create a PipedStream.", ex);
        }

        uploadFrom(source, metadata);

        return output;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public Upload getLastUpload() {
        return this.lastUpload;
    }
}
