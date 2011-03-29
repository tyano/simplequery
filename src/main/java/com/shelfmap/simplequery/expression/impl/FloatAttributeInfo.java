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

import static com.amazonaws.services.simpledb.util.SimpleDBUtils.*;
import com.shelfmap.simplequery.expression.AttributeConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class FloatAttributeInfo implements AttributeConverter<Float> {

    private final int maxDigitLeft;
    private final int maxDigitRight;
    private final int offset;

    public FloatAttributeInfo(int maxDigitLeft, int maxDigitRight, int offset) {
        this.maxDigitLeft = maxDigitLeft;
        this.maxDigitRight = maxDigitRight;
        this.offset = offset;
    }

    @Override
    public String convertValue(Float targetValue) {
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
    public Float restoreValue(String targetValue) {
        Float restored = null;
        return (offset > 0)
            ? decodeRealNumberRangeFloat(targetValue, maxDigitRight, offset)
            : decodeZeroPaddingFloat(targetValue);
    }
}
