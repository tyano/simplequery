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
import com.amazonaws.services.s3.model.S3Object;
import com.shelfmap.simplequery.Client;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultBlobReference<T> implements BlobReference<T> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBlobReference.class);

    private final S3ResourceInfo resourceInfo;

    public DefaultBlobReference(S3ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    @Override
    public T getContent(Client client) throws BlobRestoreException {
        String bucket = resourceInfo.getBucketName();
        String key = resourceInfo.getKey();
        AmazonS3 s3 = client.getS3();

        GetObjectRequest request = new GetObjectRequest(bucket, key);
        S3Object resource = s3.getObject(request);
        InputStream resourceStream = resource.getObjectContent();
        try {
            return restoreObject(resourceStream);
        } catch (IOException ex) {
            throw new BlobRestoreException("could not read a object stream of Blob(bucket=" + resourceInfo.getBucketName() + ", key=" + resourceInfo.getKey() + ").", ex, resourceInfo);
        } catch (ClassNotFoundException ex) {
            throw new BlobRestoreException("could not find a class for restoring an object.", ex, resourceInfo);
        }
    }

    @SuppressWarnings("unchecked")
    private T restoreObject(InputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(stream);
        try {
            return (T) ois.readObject();
        } finally {
            try {
                if(ois != null) ois.close();
            } catch (IOException ex) {
                LOGGER.error("could not close a stream.", ex);
            }
        }
    }

    @Override
    public S3ResourceInfo getResourceInfo() {
        return this.resourceInfo;
    }
}
