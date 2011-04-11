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

import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import static com.amazonaws.services.simpledb.util.SimpleDBUtils.*;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;

/**
 *
 * @author Tsutomu YANO
 */
public class FloatAttributeConverter implements AttributeConverter<Float> {

    private final int maxDigitLeft;
    private final int maxDigitRight;
    private final int offset;

    public FloatAttributeConverter(int maxDigitLeft, int maxDigitRight, int offset) {
        this.maxDigitLeft = maxDigitLeft;
        this.maxDigitRight = maxDigitRight;
        this.offset = offset;
    }

    @Override
    public String convertValue(Float targetValue) {
        isNotNull("targetValue", targetValue);
        String result = "";
        if (offset > 0) {
            result = quoteValue(encodeRealNumberRange(targetValue.floatValue(), maxDigitLeft, maxDigitRight, offset));
        } else if (maxDigitLeft > 0) {
            result = quoteValue(encodeZeroPadding(targetValue.floatValue(), maxDigitLeft));
        } else {
            result = quoteValue(targetValue.toString());
        }
        return result;
    }

    @Override
    public Float restoreValue(String targetValue)  throws CanNotRestoreAttributeException {
        isNotNull("targetValue", targetValue);
        return (offset > 0)
            ? decodeRealNumberRangeFloat(targetValue, maxDigitRight, offset)
            : decodeZeroPaddingFloat(targetValue);
    }
}
