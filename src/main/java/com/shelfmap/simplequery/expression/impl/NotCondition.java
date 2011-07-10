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

import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Operator;
import com.shelfmap.simplequery.expression.matcher.Matcher;

/**
 *
 * @author Tsutomu YANO
 */
public class NotCondition implements Condition<Void>{

    private Condition<?> condition;
    private Condition<?> parent;
    private Operator operator;

    public NotCondition(Condition<?> condition) {
        this(condition, NullCondition.INSTANCE, NullOperator.INSTANCE);
    }

    protected NotCondition(Condition<?> condition, Condition<?> parent, Operator operator) {
        this.condition = condition;
        this.parent = parent;
        this.operator = operator;
    }

    private <T> Condition<T> newCondition(String attributeName, Matcher<T> matcher) {
        return new DefaultCondition<T>(attributeName, matcher);
    }

    @Override
    public Condition<?> getParent() {
        return this.parent;
    }

    @Override
    public Operator getOperator() {
        return this.operator;
    }

    public Condition<?> getCondition() {
        return condition;
    }

    @Override
    public String getAttributeName() {
        return "";
    }

    @Override
    public Matcher<Void> getMatcher() {
        return null;
    }

    @Override
    public Condition<Void> withParent(Condition<?> parent, Operator operator) {
        return new NotCondition(this.condition, parent, operator);
    }

    @Override
    public Condition<Void> withAttributeName(String attributeName) {
        return this;
    }

    @Override
    public Condition<Void> withMatcher(Matcher<Void> matcher) {
        return this;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(getParent().describe());
        sb.append(getOperator().describe());
        sb.append("not (");
        sb.append(getCondition().describe());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Condition<?> and(Condition<?> other) {
        return other.withParent(this, BasicOperator.AND);
    }

    @Override
    public Condition<?> and(String attributeName, Matcher<?> matcher) {
        Condition<?> other = newCondition(attributeName, matcher);
        return this.and(other);
    }

    @Override
    public <E> Condition<?> and(String attributeName, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attributeName, matcher.withAttributeConverter(attributeConverter));
        return this.and(other);
    }

    @Override
    public Condition<?> or(Condition<?> other) {
        return other.withParent(this, BasicOperator.OR);
    }

    @Override
    public Condition<?> or(String attributeName, Matcher<?> matcher) {
        Condition<?> other = newCondition(attributeName, matcher);
        return this.or(other);
    }

    @Override
    public <E> Condition<?> or(String attributeName, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attributeName, matcher.withAttributeConverter(attributeConverter));
        return this.or(other);
    }

    @Override
    public Condition<?> intersection(Condition<?> other) {
        return other.withParent(this, BasicOperator.INTERSECTION);
    }

    @Override
    public Condition<?> intersection(String attributeName, Matcher<?> matcher) {
        Condition<?> other = newCondition(attributeName, matcher);
        return this.intersection(other);
    }

    @Override
    public <E> Condition<?> intersection(String attributeName, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attributeName, matcher.withAttributeConverter(attributeConverter));
        return this.intersection(other);
    }

    @Override
    public Condition<?> group() {
        return new ConditionGroup(this);
    }

    /**
     * {@inheritDoc }
     *
     * <p>
     * this implementations returns the child condition object as the return value,
     * because not(not(condition)) == condition.
     */
    @Override
    public Condition<?> not() {
        return getCondition();
    }
}
