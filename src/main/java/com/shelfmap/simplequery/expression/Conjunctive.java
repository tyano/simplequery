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


import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.matcher.Matcher;

/**
 * @param <T> the type of conbined object. ex) Expression.and(Condition) will return a Expression, Condition.and(Condition) will return a Condition.
 * @author Tsutomu YANO
 */
public interface Conjunctive<T> {
    T and(Condition<?> other);
    T and(ConditionAttribute attribute, Matcher<?> matcher);
    <E> T and(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter);
    T or(Condition<?> other);
    T or(ConditionAttribute attribute, Matcher<?> matcher);
    <E> T or(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter);
    T intersection(Condition<?> other);
    T intersection(ConditionAttribute attribute, Matcher<?> matcher);
    <E> T intersection(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter);
    T group();
    T not();
}
