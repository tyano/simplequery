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

import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.domain.BlobReference;
import com.shelfmap.simplequery.domain.DefaultBlobReference;
import com.shelfmap.simplequery.domain.S3Resource;
import com.shelfmap.simplequery.domain.S3ResourceInfo;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;

/**
 *
 * @param <T>
 * @author Tsutomu YANO
 */
public class BlobAttributeConverter<T> implements AttributeConverter<BlobReference<T>> {

    private Class<T> targetClass;

    public BlobAttributeConverter(Class<T> targetClass) {
        super();
        this.targetClass = targetClass;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * {@inheritDoc }
     * <p>
     * the string expression of BlobReference must be a string which contains
     * bucket's name and key name of Amazon S3, separated by '|'.
     */
    @Override
    public String convertValue(BlobReference<T> targetValue) {
        S3ResourceInfo info = targetValue.getResourceInfo();
        return info.getBucketName() + "|" + info.getKey();
    }

    @Override
    public BlobReference<T> restoreValue(String targetValue) throws CanNotRestoreAttributeException {
        String[] nameAndKey = targetValue.split("|", 2);
        if(nameAndKey.length != 2) throw new IllegalStateException("the string expression for a Blob must be a string which contains the bucket's name and key separated by '|'.");

        String bucketName = nameAndKey[0];
        String key = nameAndKey[1];

        return new DefaultBlobReference<T>(new S3Resource(bucketName, key), getTargetClass());
    }

}
