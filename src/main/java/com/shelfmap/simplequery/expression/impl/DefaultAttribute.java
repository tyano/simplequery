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

package com.shelfmap.simplequery.expression.impl;

import com.shelfmap.simplequery.expression.AttributeInfo;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Attribute;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultAttribute implements Attribute {
    private String attributeName;
    private Class<?> type;
    private int maxDigitLeft;
    private int maxDigitRight;
    private long offset;

    protected DefaultAttribute(String attributeName, Class<?> type, int maxDigitLeft, int maxDigitRight, long offset) {
        isNotNull("attributeName", attributeName);
        isNotNull("type", type);
        this.attributeName = attributeName;
        this.type = type;
        this.maxDigitLeft = maxDigitLeft;
        this.maxDigitRight = maxDigitRight;
        this.offset = offset;
    }
    
    public DefaultAttribute(String attributeName, Class<?> type) {
        this(attributeName, type, 0, 0, 0L);
    }
    
    public DefaultAttribute(String attributeName, Class<Integer> type, int maxNumDigits, int offset) {
        this(attributeName, type, maxNumDigits, 0, (long)offset);
    }

    public DefaultAttribute(String attributeName, Class<Long> type, int maxNumDigits, long offset) {
        this(attributeName, type, maxNumDigits, 0, offset);
    }
    
    public DefaultAttribute(String attributeName, Class<Float> type, int maxDigitLeft, int maxDigitRight, int offset) {
        this(attributeName, type, maxDigitLeft, maxDigitRight, (long)offset);
    }
    

    @Override
    public String getName() {
        return attributeName;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public int getMaxDigitLeft() {
        return maxDigitLeft;
    }

    @Override
    public int getMaxDigitRight() {
        return maxDigitRight;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public AttributeInfo<?> getAttributeInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
