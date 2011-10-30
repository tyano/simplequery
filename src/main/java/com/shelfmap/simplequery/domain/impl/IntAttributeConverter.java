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

import static com.amazonaws.services.simpledb.util.SimpleDBUtils.*;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;

/**
 *
 * @author Tsutomu YANO
 */
public class IntAttributeConverter implements AttributeConverter<Integer> {

    private final int maxNumberOfDigits;
    private final int offset;

    public IntAttributeConverter(int maxNumberOfDigits, int offset) {
        this.maxNumberOfDigits = maxNumberOfDigits;
        this.offset = offset;
    }

    @Override
    public String convertValue(Integer targetValue) {
        isNotNull("targetValue", targetValue);
        String result = "";
        if (offset > 0) {
            result = encodeRealNumberRange(targetValue, maxNumberOfDigits, offset);
        } else if (maxNumberOfDigits > 0) {
            result = encodeZeroPadding(targetValue, maxNumberOfDigits);
        } else {
            result = targetValue.toString();
        }
        return result;
    }

    @Override
    public Integer restoreValue(String targetValue)  throws CanNotRestoreAttributeException {
        isNotNull("targetValue", targetValue);
        return (offset > 0)
            ? decodeRealNumberRangeInt(targetValue, offset)
            : decodeZeroPaddingInt(targetValue);
    }
}
