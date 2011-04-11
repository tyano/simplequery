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
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;

/**
 *
 * @author Tsutomu YANO
 */
public class NullAttributeConverter implements AttributeConverter<Object> {
    private static final NullAttributeConverter INSTANCE = new NullAttributeConverter();

    private NullAttributeConverter() {
    }
    
    @Override
    public String convertValue(Object targetValue) {
        return targetValue.toString();
    }

    @Override
    public Object restoreValue(String targetValue) throws CanNotRestoreAttributeException {
        return targetValue;
    }
}
