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

import java.util.Arrays;
import java.util.List;

import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Operator;
import com.shelfmap.simplequery.expression.matcher.Matcher;

import static com.shelfmap.simplequery.util.Assertion.isNotNull;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultCondition<T> implements Condition<T> {

    private Condition<?> parent;
    private Operator operator;
    private ConditionAttribute attribute;
    private Matcher<T> matcher;

    public static final List<String> SIMPLEDB_FUNCTION_LIST = Arrays.asList("itemName()");


    public DefaultCondition(ConditionAttribute attribute, Matcher<T> matcher) {
        isNotNull("attribute", attribute);
        isNotNull("matcher", matcher);
        this.parent = NullCondition.INSTANCE;
        this.operator = NullOperator.INSTANCE;
        this.attribute = attribute;
        this.matcher = matcher;
    }

    protected DefaultCondition(Condition<?> parent, Operator operator, ConditionAttribute attribute, Matcher<T> matcher) {
        this.parent = parent;
        this.operator = operator;
        this.attribute = attribute;
        this.matcher = matcher;
    }

    @Override
    public ConditionAttribute getAttribute() {
        return attribute;
    }

    @Override
    public Matcher<T> getMatcher() {
        return matcher;
    }


    @Override
    public Condition<?> and(Condition<?> other) {
        return other.withParent(this, BasicOperator.AND);
    }

    private <T> Condition<T> newCondition(ConditionAttribute attribute, Matcher<T> matcher) {
        return new DefaultCondition<T>(attribute, matcher);
    }

    @Override
    public Condition<?> and(ConditionAttribute attribute, Matcher<?> matcher) {
        Condition<?> other = newCondition(attribute, matcher);
        return this.and(other);
    }

    @Override
    public Condition<?> or(Condition<?> other) {
        return other.withParent(this, BasicOperator.OR);
    }

    @Override
    public Condition<?> or(ConditionAttribute attribute, Matcher<?> matcher) {
        Condition<?> other = newCondition(attribute, matcher);
        return this.or(other);
    }

    @Override
    public Condition<?> group() {
        return new ConditionGroup(this);
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();

        sb.append(getParent().describe());
        sb.append(getOperator().describe());
        sb.append(getAttribute().describe()).append(" ").append(getMatcher().describe());

        return sb.toString();
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public Condition<?> getParent() {
        return parent;
    }

    @Override
    public Condition<T> withParent(Condition<?> parent, Operator operator) {
        return new DefaultCondition<T>(parent, operator, getAttribute(), getMatcher());
    }

    @Override
    public <E> Condition<?> and(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
        return this.and(other);
    }

    @Override
    public <E> Condition<?> or(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
        return this.or(other);
    }

    @Override
    public Condition<?> intersection(Condition<?> other) {
        return other.withParent(this, BasicOperator.INTERSECTION);
    }

    @Override
    public Condition<?> intersection(ConditionAttribute attribute, Matcher<?> matcher) {
        Condition<?> other = newCondition(attribute, matcher);
        return this.intersection(other);
    }

    @Override
    public <E> Condition<?> intersection(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
        return this.intersection(other);
    }

    @Override
    public Condition<T> withAttribute(ConditionAttribute attribute) {
        return new DefaultCondition<T>(getParent(), getOperator(), attribute, getMatcher());
    }

    @Override
    public Condition<T> withMatcher(Matcher<T> matcher) {
        return new DefaultCondition<T>(getParent(), getOperator(), getAttribute(), matcher);
    }

    @Override
    public Condition<?> not() {
        return new NotCondition(this);
    }
}
