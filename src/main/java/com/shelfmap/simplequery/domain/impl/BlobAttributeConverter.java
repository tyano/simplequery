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
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @param <T> 
 * @author Tsutomu YANO
 */
public class BlobAttributeConverter<T> implements AttributeConverter<T> {

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
    
    @Override
    public String convertValue(T targetValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T restoreValue(String targetValue) throws CanNotRestoreAttributeException {
        try {
            //'targetValue' is a url for S3 resource.
            //we must retrieve the binary data from the url and restore object T from it.
            URL url = new URL(targetValue);
            
            return null;
        } catch (MalformedURLException ex) {
            throw new CanNotRestoreAttributeException(targetValue, ex, targetValue, getTargetClass());
        }
    }
    
}
