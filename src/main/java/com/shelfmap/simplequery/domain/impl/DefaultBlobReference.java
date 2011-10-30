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
import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.util.IO;
import java.io.*;
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

        //TODO for avoiding a strange behavior of Amazon S3, I download all data from a bucket into a file and create a InputStream.
        //If I process something directly on the stream which have gotten by s3.getObject().getObjectContent(),
        //the remote socket of the s3 object suddenly be closed while the processing.
        //Same problems are foundable in google search, but no appropriate answer.
        File temp = null;
        InputStream resourceStream = null;
        try {
            String bucket = resourceInfo.getBucketName();
            String key = resourceInfo.getKey();
            String version = resourceInfo.getVersionId();
            AmazonS3 s3 = getContext().getS3();

            GetObjectRequest request = version.isEmpty() ? new GetObjectRequest(bucket, key) : new GetObjectRequest(bucket, key, version);
            temp = File.createTempFile("simplequery-", ".tmp");
            s3.getObject(request, temp);

            resourceStream = new FileInputStream(temp);
            T content = getContentConverter().restoreObject(getObjectMetadata(), resourceStream);
            return content;
        } catch(IOException ex) {
            throw new BlobRestoreException(ex);
        } finally {
            IO.close(resourceStream, this);
            if(temp != null) temp.delete();
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
        String version = resourceInfo.getVersionId();
        AmazonS3 s3 = getContext().getS3();

        GetObjectRequest request = version.isEmpty() ? new GetObjectRequest(bucket, key) : new GetObjectRequest(bucket, key, version);
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
            AmazonS3 s3 = getContext().getS3();
            TransferManager transfer = new TransferManager(s3);
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
