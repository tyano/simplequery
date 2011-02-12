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
 *    Matcher<T> withAttributeInfo(int maxDigitLeft, int maxDigitRight, int offsetValue);
    Matcher<T> withAttributeInfo(int maxNumDigits, int offsetValue);
    Matcher<T> withAttributeInfo(int maxNumDigits, long offsetValue);
 * @author Tsutomu YANO
 */
public interface Condition extends Describable {
    Condition withParent(Condition parent, Operator operator);
    void setParent(Condition parent, Operator operator);
    Condition getParent();
    Condition and(Condition other);
    Condition and(String attributeName, Matcher<?> matcher);
    Condition and(String attributeName, Matcher<? extends Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue);
    Condition and(String attributeName, Matcher<? extends Integer> matcher, int maxNumDigits, int offsetValue);
    Condition and(String attributeName, Matcher<? extends Long> matcher, int maxNumDigits, long offsetValue);
    Condition or(Condition other);
    Condition or(String attributeName, Matcher<?> matcher);
    Condition or(String attributeName, Matcher<? extends Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue);
    Condition or(String attributeName, Matcher<? extends Integer> matcher, int maxNumDigits, int offsetValue);
    Condition or(String attributeName, Matcher<? extends Long> matcher, int maxNumDigits, long offsetValue);
    Condition group();
}
