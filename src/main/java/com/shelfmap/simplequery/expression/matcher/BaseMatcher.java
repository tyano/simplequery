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
package com.shelfmap.simplequery.expression.matcher;

import static com.amazonaws.services.simpledb.util.SimpleDBUtils.encodeZeroPadding;
import static com.amazonaws.services.simpledb.util.SimpleDBUtils.encodeRealNumberRange;
import static com.amazonaws.services.simpledb.util.SimpleDBUtils.quoteValue;
import static com.shelfmap.simplequery.util.Assertion.*;
import com.shelfmap.simplequery.expression.Matcher;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class BaseMatcher<T> implements Matcher<T> {

    private T[] values;
    private int maxDigitLeft;
    private int maxDigitRight;
    private int offsetInt;
    private long offsetLong;
    private NumberType numberType;

    public BaseMatcher(T... values) {
        isNotNull("values", values);
        isNotEmpty("values", values);

        this.values = values;
        this.maxDigitLeft = 0;
        this.maxDigitRight = 0;
        this.offsetInt = 0;
        this.offsetLong = 0L;
        this.numberType = NumberType.NOT_NUMBER;
    }

    protected String convertValue(T targetValue) {
        String result;

        switch (numberType) {
            case NOT_NUMBER:
                result = quoteValue(targetValue.toString());
                break;
            case FLOAT:
                if (offsetInt > 0) {
                    result = quoteValue(encodeRealNumberRange(((Float) targetValue).floatValue(), maxDigitLeft, maxDigitRight, offsetInt));
                } else if (maxDigitLeft > 0) {
                    result = quoteValue(encodeZeroPadding(((Float) targetValue).floatValue(), maxDigitLeft));
                } else {
                    result = quoteValue(targetValue.toString());
                }
                break;
            case INTEGER:
                if (offsetInt > 0) {
                    result = quoteValue(encodeRealNumberRange(((Integer) targetValue).intValue(), maxDigitLeft, offsetInt));
                } else if (maxDigitLeft > 0) {
                    result = quoteValue(encodeZeroPadding(((Integer) targetValue).intValue(), maxDigitLeft));
                } else {
                    result = quoteValue(targetValue.toString());
                }
                break;
            case LONG:
                if (offsetLong > 0) {
                    result = quoteValue(encodeRealNumberRange(((Long) targetValue).longValue(), maxDigitLeft, offsetLong));
                } else if (maxDigitLeft > 0) {
                    result = quoteValue(encodeZeroPadding(((Long) targetValue).longValue(), maxDigitLeft));
                } else {
                    result = quoteValue(targetValue.toString());
                }
                break;
            default:
                throw new IllegalStateException("No such NumberType: " + numberType);
        }
        return result;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression()).append(" ");
        sb.append(convertValue(values[0]));
        return sb.toString();
    }

    @Override
    public Matcher<T> withAttributeInfo(int maxDigitLeft, int maxDigitRight, int offsetValue) {
        this.maxDigitLeft = maxDigitLeft;
        this.maxDigitRight = maxDigitRight;
        this.offsetInt = offsetValue;
        this.numberType = NumberType.FLOAT;
        return this;
    }

    @Override
    public Matcher<T> withAttributeInfo(int maxNumDigits, int offsetValue) {
        this.maxDigitLeft = maxNumDigits;
        this.maxDigitRight = 0;
        this.offsetInt = offsetValue;
        this.offsetLong = 0L;
        this.numberType = NumberType.INTEGER;
        return this;
    }

    @Override
    public Matcher<T> withAttributeInfo(int maxNumDigits, long offsetValue) {
        this.maxDigitLeft = maxNumDigits;
        this.maxDigitRight = 0;
        this.offsetInt = 0;
        this.offsetLong = offsetValue;
        this.numberType = NumberType.LONG;
        return this;
    }

    protected abstract String expression();

    public int getMaxDigitLeft() {
        return maxDigitLeft;
    }

    public int getMaxDigitRight() {
        return maxDigitRight;
    }

    public NumberType getNumberType() {
        return numberType;
    }

    public int getOffsetInt() {
        return offsetInt;
    }

    public long getOffsetLong() {
        return offsetLong;
    }

    public Collection<T> getValues() {
        return Arrays.asList(values);
    }
}
