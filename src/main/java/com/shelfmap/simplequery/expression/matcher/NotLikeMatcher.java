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

/**
 *
 * @author Tsutomu YANO
 */
public class NotLikeMatcher<T> extends BaseMatcher<T> {

    @SuppressWarnings("unchecked")
    public NotLikeMatcher(T value) {
        super(value);
    }

    public NotLikeMatcher(int maxDigitLeft, int maxDigitRight, int offsetInt, long offsetLong, NumberType numberType, T[] values) {
        super(maxDigitLeft, maxDigitRight, offsetInt, offsetLong, numberType, values);
    }

    @Override
    protected String expression() {
        return "not like";
    }

    @Override
    protected NotLikeMatcher<T> newMatcher(int maxDigitLeft, int maxDigitRight, int offsetInt, long offsetLong, NumberType numberType, T... values) {
        return new NotLikeMatcher<T>(maxDigitLeft, maxDigitRight, offsetInt, offsetLong, numberType, values);
    }
}
