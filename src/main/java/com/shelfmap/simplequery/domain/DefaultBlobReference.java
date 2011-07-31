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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.shelfmap.simplequery.Client;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
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

    public DefaultBlobReference(S3ResourceInfo resourceInfo, Class<T> targetClass, BlobContentConverter<T> converter) {
        this.resourceInfo = resourceInfo;
        this.targetClass = targetClass;
        this.converter = converter;
    }

    @Override
    public T getContent(Client client) throws BlobRestoreException {
        String bucket = resourceInfo.getBucketName();
        String key = resourceInfo.getKey();
        AmazonS3 s3 = client.getS3();

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
    public void setContent(Client client, T object, ObjectMetadata metadata) throws BlobOutputException {
        String bucket = resourceInfo.getBucketName();
        String key = resourceInfo.getKey();
        AmazonS3 s3 = client.getS3();

        InputStream source = null;
        try {
            source = getContentConverter().objectToStream(object);
            PutObjectRequest request = new PutObjectRequest(bucket, key, source, metadata);
            s3.putObject(request);
        } finally {
            if(source != null) {
                try {
                    source.close();
                } catch (IOException ex) {
                    LOGGER.error("Could not close an stream.", ex);
                }
            }
        }

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
}
