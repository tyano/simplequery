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

import com.shelfmap.simplequery.expression.AttributeConverter;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.Operator;

/**
 *
 * @author Tsutomu YANO
 */
public final class NullCondition implements Condition<Object> {
    public static final NullCondition INSTANCE = new NullCondition();
    
    private NullCondition() {
        super();
    }

    @Override
    public Condition<?> and(Condition<?> other) {
        return other;
    }

    private <T> Condition<T> newCondition(String attributeName, Matcher<T> matcher) {
        return new DefaultCondition<T>(attributeName, matcher);
    }
    
    @Override
    public Condition<?> and(String attributeName, Matcher<?> matcher) {
        return newCondition(attributeName, matcher);
    }

    @Override
    public Condition<?> or(Condition<?> other) {
        return other;
    }

    @Override
    public Condition<?> or(String attributeName, Matcher<?> matcher) {
        return newCondition(attributeName, matcher);
    }

    @Override
    public Condition<?> group() {
        return this;
    }

    @Override
    public String describe() {
        return "";
    }

    @Override
    public Condition<Object> withParent(Condition<?> parent, Operator operator) {
        return this;
    }

    @Override
    public Condition<?> getParent() {
        return null;
    }

    @Override
    public <E> Condition<?> and(String attributeName, Matcher<E> matcher, AttributeConverter<E> attributeInfo) {
        return newCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
    }

    @Override
    public <E> Condition<?> or(String attributeName, Matcher<E> matcher, AttributeConverter<E> attributeInfo) {
        return newCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
    }

    @Override
    public Condition<?> intersection(Condition<?> other) {
        return other;
    }

    @Override
    public Condition<?> intersection(String attributeName, Matcher<?> matcher) {
        return newCondition(attributeName, matcher);
    }

    @Override
    public <E> Condition<?> intersection(String attributeName, Matcher<E> matcher, AttributeConverter<E> attributeInfo) {
        return newCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
    }

    @Override
    public Operator getOperator() {
        return null;
    }

    @Override
    public String getAttributeName() {
        return "";
    }

    @Override
    public Matcher<Object> getMatcher() {
        return null;
    }

    @Override
    public Condition<Object> withAttributeName(String attributeName) {
        return this;
    }

    @Override
    public Condition<Object> withMatcher(Matcher<Object> matcher) {
        return this;
    }
}
