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


import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.attribute.impl.DefaultAttribute;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Operator;
import com.shelfmap.simplequery.expression.matcher.Matcher;
import java.io.Serializable;

/**
 *
 * @author Tsutomu YANO
 */
public final class NullCondition implements Condition<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final NullCondition INSTANCE = new NullCondition();

    private NullCondition() {
        super();
    }

    @Override
    public Condition<?> and(Condition<?> other) {
        return other;
    }

    private <T> Condition<T> newCondition(ConditionAttribute attribute, Matcher<T> matcher) {
        return new DefaultCondition<T>(attribute, matcher);
    }

    @Override
    public Condition<?> and(ConditionAttribute attribute, Matcher<?> matcher) {
        return newCondition(attribute, matcher);
    }

    @Override
    public Condition<?> or(Condition<?> other) {
        return other;
    }

    @Override
    public Condition<?> or(ConditionAttribute attribute, Matcher<?> matcher) {
        return newCondition(attribute, matcher);
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
    public <E> Condition<?> and(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        return newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
    }

    @Override
    public <E> Condition<?> or(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        return newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
    }

    @Override
    public Condition<?> intersection(Condition<?> other) {
        return other;
    }

    @Override
    public Condition<?> intersection(ConditionAttribute attribute, Matcher<?> matcher) {
        return newCondition(attribute, matcher);
    }

    @Override
    public <E> Condition<?> intersection(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        return newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
    }

    @Override
    public Operator getOperator() {
        return null;
    }

    @Override
    public ConditionAttribute getAttribute() {
        return new DefaultAttribute("");
    }

    @Override
    public Matcher<Object> getMatcher() {
        return null;
    }

    @Override
    public Condition<Object> withAttribute(ConditionAttribute attribute) {
        return this;
    }

    @Override
    public Condition<Object> withMatcher(Matcher<Object> matcher) {
        return this;
    }

    @Override
    public Condition<?> not() {
        return this;
    }
}
