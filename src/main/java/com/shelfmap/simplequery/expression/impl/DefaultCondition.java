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

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.expression.AttributeInfo;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.Operator;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultCondition implements Condition {

    private Condition parent;
    private Operator operator;
    private String attributeName;
    private Matcher<?> matcher;

    public DefaultCondition(String attributeName, Matcher<?> matcher) {
        isNotNull("attributeName", attributeName);
        isNotNull("matcher", matcher);
        this.parent = NullCondition.INSTANCE;
        this.operator = NullOperator.INSTANCE;
        this.attributeName = attributeName;
        this.matcher = matcher;
    }

    protected DefaultCondition(Condition parent, Operator operator, String attributeName, Matcher<?> matcher) {
        this.parent = parent;
        this.operator = operator;
        this.attributeName = attributeName;
        this.matcher = matcher;
    }
    
    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public Matcher<?> getMatcher() {
        return matcher;
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
    public Condition or(Condition other) {
        return other.withParent(this, BasicOperator.OR);
    }

    @Override
    public Condition or(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.or(other);
    }
    
    @Override
    public Condition group() {
        return new ConditionGroup(this);
    }
    
    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(getParent().describe());
        sb.append(getOperator().describe());
        sb.append(SimpleDBUtils.quoteName(getAttributeName())).append(" ").append(getMatcher().describe());
        
        return sb.toString();
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public Condition getParent() {
        return parent;
    }
    
    @Override
    public Condition withParent(Condition parent, Operator operator) {
        return new DefaultCondition(parent, operator, getAttributeName(), getMatcher());
    }

    @Override
    public <E> Condition and(String attributeName, Matcher<E> matcher, AttributeInfo<E> attributeInfo) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
        return this.and(other);
    }

    @Override
    public <E> Condition or(String attributeName, Matcher<E> matcher, AttributeInfo<E> attributeInfo) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
        return this.or(other);
    }

    @Override
    public Condition intersection(Condition other) {
        return other.withParent(this, BasicOperator.INTERSECTION);
    }

    @Override
    public Condition intersection(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.intersection(other);
    }

    @Override
    public <E> Condition intersection(String attributeName, Matcher<E> matcher, AttributeInfo<E> attributeInfo) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
        return this.intersection(other);
    }

    @Override
    public Condition withAttributeName(String attributeName) {
        return new DefaultCondition(getParent(), getOperator(), attributeName, getMatcher());
    }

    @Override
    public Condition withMatcher(Matcher<?> matcher) {
        return new DefaultCondition(getParent(), getOperator(), getAttributeName(), matcher);
    }
}
