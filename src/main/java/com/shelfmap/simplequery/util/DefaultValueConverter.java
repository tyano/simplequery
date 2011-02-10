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

package com.shelfmap.simplequery.util;

import com.shelfmap.simplequery.expression.ValueConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultValueConverter implements ValueConverter {

    @Override
    public String convertValue(Class<?> type, Object value) {
        return null;
    }

    @Override
    public String convertAttribute(String attributeName) {
        return null;
    }
    
    protected IntConverter newIntConverter() {
        return null;
    }
    
    protected ByteConverter newByteConveter() {
        return null;
    }
    
    protected ShortConverter newShortConveter() {
        return null;
    }
    
    protected LongConverter newLongConverter() {
        return null;
    }
    
    protected StringConverter newStringConverter() {
        return null;
    }
    
    protected FloatConverter newFloatConverter() {
        return null;
    }
    
    protected DoubleConverter newDoubleConverter() {
        return null;
    }
    
    protected ObjectConverter newObjectConverter() {
        return null;
    }
}
