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
public class ConditionGroup implements Condition<Void>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Condition<?> condition;
    private Condition<?> parent;
    private Operator operator;

    public ConditionGroup(Condition<?> condition) {
        this.condition = condition;
        this.parent = NullCondition.INSTANCE;
        this.operator = NullOperator.INSTANCE;
    }

    protected ConditionGroup(Condition<?> condition, Condition<?> parent, Operator operator) {
        this.condition = condition;
        this.parent = parent;
        this.operator = operator;
    }

    @Override
    public Condition<Void> withParent(Condition<?> parent, Operator operator) {
        return new ConditionGroup(getCondition(), parent, operator);
    }

    @Override
    public Condition<?> getParent() {
        return this.parent;
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
    public <E> Condition<?> and(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
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
    public <E> Condition<?> or(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
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
        sb.append("(");
        sb.append(getCondition().describe());
        sb.append(")");
        return sb.toString();
    }

    public Condition<?> getCondition() {
        return condition;
    }

    @Override
    public Operator getOperator() {
        return operator;
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
    public ConditionAttribute getAttribute() {
        return new DefaultAttribute("");
    }

    @Override
    public Matcher<Void> getMatcher() {
        return null;
    }

    @Override
    public Condition<Void> withAttribute(ConditionAttribute attribute) {
        return this;
    }

    @Override
    public Condition<Void> withMatcher(Matcher<Void> matcher) {
        return this;
    }

    @Override
    public Condition<?> not() {
        return new NotCondition(this);
    }
}
