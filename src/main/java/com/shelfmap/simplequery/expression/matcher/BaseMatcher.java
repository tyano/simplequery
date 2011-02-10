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
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.ValueConverter;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class BaseMatcher<T> implements Matcher<T> {
    private T value;
    private int maxDigitLeft;
    private int maxDigitRight;
    private int offsetInt;
    private long offsetLong;
    private NumberType numberType;

    public BaseMatcher(T value) {
        isNotNull("value", value);
        this.value = value;
        this.maxDigitLeft = 0;
        this.maxDigitRight = 0;
        this.offsetInt = 0;
        this.offsetLong = 0L;
        this.numberType = NumberType.NOT_NUMBER;
    }
    
    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression()).append(" ");
        
        switch(numberType) {
            case NOT_NUMBER:
                sb.append(quoteValue(value.toString()));
                break;
            case FLOAT:
                if(offsetInt > 0) {
                   sb.append(quoteValue(encodeRealNumberRange(((Float)value).floatValue(), maxDigitLeft, maxDigitRight, offsetInt)));
                } else if(maxDigitLeft > 0) {
                   sb.append(quoteValue(encodeZeroPadding(((Float)value).floatValue(), maxDigitLeft)));
                } else {
                   sb.append(quoteValue(value.toString()));
                }
                break;
            case INTEGER:
                if(offsetInt > 0) {
                   sb.append(quoteValue(encodeRealNumberRange(((Integer)value).intValue(), maxDigitLeft, offsetInt)));
                } else if(maxDigitLeft > 0) {
                   sb.append(quoteValue(encodeZeroPadding(((Integer)value).intValue(), maxDigitLeft)));
                } else {
                   sb.append(quoteValue(value.toString()));
                }
                break;
            case LONG:
                if(offsetInt > 0) {
                   sb.append(quoteValue(encodeRealNumberRange(((Long)value).longValue(), maxDigitLeft, offsetLong)));
                } else if(maxDigitLeft > 0) {
                   sb.append(quoteValue(encodeZeroPadding(((Long)value).longValue(), maxDigitLeft)));
                } else {
                   sb.append(quoteValue(value.toString()));
                }
                break;
            default:
                throw new IllegalStateException("No such NumberType: " + numberType);
        }
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
        this.numberType = NumberType.INTEGER;
        return this;
    }

    @Override
    public Matcher<T> withAttributeInfo(int maxNumDigits, long offsetValue) {
        this.maxDigitLeft = maxNumDigits;
        this.maxDigitRight = 0;
        this.offsetLong = offsetValue;
        return this;
    }
    
    protected abstract String expression();

    private ValueConverter getValueConverter() {
        return null;
    }

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

    public T getValue() {
        return value;
    }
}
