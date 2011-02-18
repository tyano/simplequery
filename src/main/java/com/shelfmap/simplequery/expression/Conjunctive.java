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

package com.shelfmap.simplequery.expression;

/**
 *
 * @author Tsutomu YANO
 */
public interface Conjunctive<T> {
    T and(Condition other);
    T and(String attributeName, Matcher<?> matcher);
    T and(String attributeName, Matcher<Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue);
    T and(String attributeName, Matcher<Integer> matcher, int maxNumDigits, int offsetValue);
    T and(String attributeName, Matcher<Long> matcher, int maxNumDigits, long offsetValue);
    T or(Condition other);
    T or(String attributeName, Matcher<?> matcher);
    T or(String attributeName, Matcher<Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue);
    T or(String attributeName, Matcher<Integer> matcher, int maxNumDigits, int offsetValue);
    T or(String attributeName, Matcher<Long> matcher, int maxNumDigits, long offsetValue);
    T intersection(Condition other);
    T intersection(String attributeName, Matcher<?> matcher);
    T intersection(String attributeName, Matcher<Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue);
    T intersection(String attributeName, Matcher<Integer> matcher, int maxNumDigits, int offsetValue);
    T intersection(String attributeName, Matcher<Long> matcher, int maxNumDigits, long offsetValue);
    T group();
}
