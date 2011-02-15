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

import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.Operator;

/**
 *
 * @author Tsutomu YANO
 */
public class ConditionGroup implements Condition {

    private final Condition condition;
    private Condition parent;
    private Operator operator;
    private final Object parentLock = new Object();

    public ConditionGroup(Condition condition) {
        this.condition = condition;
    }

    @Override
    public Condition withParent(Condition parent, Operator operator) {
        setParent(parent, operator);
        return this;
    }

    @Override
    public void setParent(Condition parent, Operator operator) {
        synchronized (parentLock) {
            this.parent = parent;
            this.operator = operator;
        }
    }

    @Override
    public Condition getParent() {
        return this.condition;
    }

    @Override
    public Condition and(Condition other) {
        return other.withParent(this, BasicOperator.AND);
    }

    @Override
    public Condition and(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.and(other);
    }

    @Override
    public Condition and(String attributeName, Matcher<? extends Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxDigitLeft, maxDigitRight, offsetValue));
        return this.and(other);
    }

    @Override
    public Condition and(String attributeName, Matcher<? extends Integer> matcher, int maxNumDigits, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.and(other);
    }

    @Override
    public Condition and(String attributeName, Matcher<? extends Long> matcher, int maxNumDigits, long offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.and(other);
    }

    @Override
    public Condition or(Condition other) {
        return other.withParent(this, BasicOperator.OR);
    }

    @Override
    public Condition or(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.or(other);
    }

    @Override
    public Condition or(String attributeName, Matcher<? extends Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxDigitLeft, maxDigitRight, offsetValue));
        return this.or(other);
    }

    @Override
    public Condition or(String attributeName, Matcher<? extends Integer> matcher, int maxNumDigits, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.or(other);
    }

    @Override
    public Condition or(String attributeName, Matcher<? extends Long> matcher, int maxNumDigits, long offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.or(other);
    }

    @Override
    public Condition group() {
        return new ConditionGroup(this);
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.condition.describe());
        sb.append(")");
        return sb.toString();
    }

    public Condition getCondition() {
        return condition;
    }

    public Operator getOperator() {
        return operator;
    }
}
