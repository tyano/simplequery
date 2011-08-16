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
package com.shelfmap.simplequery.domain;

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
    private final Client client;

    public DefaultBlobReference(Client client, S3ResourceInfo resourceInfo, Class<T> targetClass, BlobContentConverter<T> converter) {
        this.resourceInfo = resourceInfo;
        this.targetClass = targetClass;
        this.converter = converter;
        this.client = client;
    }

    @Override
    public T getContent() throws BlobRestoreException {
        String bucket = resourceInfo.getBucketName();
        String key = resourceInfo.getKey();
        AmazonS3 s3 = getClient().getS3();

        GetObjectRequest request = new GetObjectRequest(bucket, key);
        InputStream resourceStream = null;
        try {
            S3Object resource = s3.getObject(request);
            ObjectMetadata metadata = resource.getObjectMetadata();
            resourceStream = resource.getObjectContent();
            T content = getContentConverter().restoreObject(metadata, resourceStream);
            return content;
        } finally {
            try {
                if (resourceStream != null) {
                    resourceStream.close();
                }
            } catch (IOException ex) {
                LOGGER.error("could not close a stream.", ex);
            }
        }
    }

    @Override
    public void setContent(T object, ObjectMetadata metadata) throws BlobOutputException {
        InputStream source = getContentConverter().objectToStream(object);
        uploadFrom(source, metadata);
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
    public void uploadFrom(InputStream uploadSource, ObjectMetadata metadata) throws BlobOutputException {
        String bucket = resourceInfo.getBucketName();
        String key = resourceInfo.getKey();

        try {
            PutObjectRequest request = new PutObjectRequest(bucket, key, uploadSource, metadata);

            TransferManager transfer = new TransferManager(getClient().getCredentials());
            Upload upload = transfer.upload(request);
            upload.waitForCompletion();
        } catch (AmazonServiceException ex) {
            throw new BlobOutputException("a problem occured in Amazon S3.", ex);
        } catch (AmazonClientException ex) {
            throw new BlobOutputException("Client had an problem when uploading data.", ex);
        } catch (InterruptedException ex) {
            LOGGER.warn("Thead is interrupted.", ex);
            Thread.currentThread().interrupt();
        }
    }


    private static final int BUFFER_SIZE = 1024 * 500;

    @Override
    public OutputStream getUploadStream(ObjectMetadata metadata) throws BlobOutputException {
        PipedOutputStream output = null;
        PipedInputStream source = null;
        try {
            source = new PipedInputStream(BUFFER_SIZE);
            output = new PipedOutputStream(source);
        } catch (IOException ex) {
            if(output != null) {
                try {
                    output.close();
                } catch(IOException e) {
                    LOGGER.error("Could not close a stream.", e);
                }
            }

            if(source != null) {
                try {
                    source.close();
                } catch(IOException e) {
                    LOGGER.error("Could not close a stream.", e);
                }
            }
            throw new BlobOutputException("Could not create a PipedStream.", ex);
        }

        //Uploading is handled by another thread.
        uploadFrom(source, metadata);

        return output;
    }

    @Override
    public Client getClient() {
        return this.client;
    }
}
