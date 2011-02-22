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

import com.shelfmap.simplequery.expression.Attribute;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.LimitExpression;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.DomainAttribute;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.WhereExpression;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultWhereExpression<T> extends BaseExpression<T> implements WhereExpression<T> {
    private DomainExpression<T> domainExpression;
    private Condition condition;

    public DefaultWhereExpression(DomainExpression<T> domainExpression, Condition condition) {
        isNotNull("domainExpression", domainExpression);
        isNotNull("condition", condition);
        this.domainExpression = domainExpression;
        this.condition = condition;
    }
    
    @Override
    public OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        Class<T> domainClass = getDomainExpression().getDomainClass();
        DomainAttribute domainAttribute = new BeanDomainAttribute(domainClass);
        
        Condition current = condition;
        while(current.getParent() != null) {
            String attributeName = current.getAttributeName();
            Matcher<?> matcher = current.getMatcher();
            if(matcher != null && !matcher.isAttributeInfoApplied()) {
                if(domainAttribute.isAttributeDefined(attributeName)) {
                    Attribute attribute = domainAttribute.getAttribute(attributeName);
                    if(attribute.getMaxDigitLeft() > 0 || attribute.getMaxDigitRight() > 0 || attribute.getOffset() > 0L) {
                        Class<?> type = attribute.getType();
                        if(type == Float.class) {
                            matcher.setAttributeInfo(attribute.getMaxDigitLeft(), attribute.getMaxDigitRight(), (int)attribute.getOffset());
                        } else if(type == Integer.class) {
                            matcher.setAttributeInfo(attribute.getMaxDigitLeft(), (int)attribute.getOffset());
                        } else if(type == Long.class) {
                            matcher.setAttributeInfo(attribute.getMaxDigitLeft(), attribute.getOffset());
                        }
                    }
                }
            }
            current = current.getParent();
        }
        
        sb.append(domainExpression.describe());
        sb.append(" where ");
        sb.append(condition.describe());
        return sb.toString();
    }

    @Override
    public DomainExpression<T> getDomainExpression() {
        return this.domainExpression;
    }

    @Override
    public Condition getCondition() {
        return this.condition;
    }

    @Override
    public LimitExpression<T> limit(int limitCount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public WhereExpression<T> and(Condition other) {
        return new DefaultWhereExpression<T>(this.domainExpression, condition.and(other));
    }

    @Override
    public WhereExpression<T> and(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.and(other);
    }

    @Override
    public WhereExpression<T> and(String attributeName, Matcher<Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxDigitLeft, maxDigitRight, offsetValue));
        return this.and(other);
    }

    @Override
    public WhereExpression<T> and(String attributeName, Matcher<Integer> matcher, int maxNumDigits, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.and(other);
    }

    @Override
    public WhereExpression<T> and(String attributeName, Matcher<Long> matcher, int maxNumDigits, long offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.and(other);
    }

    @Override
    public WhereExpression<T> or(Condition other) {
        return new DefaultWhereExpression<T>(this.domainExpression, condition.or(other));
    }

    @Override
    public WhereExpression<T> or(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.or(other);
    }

    @Override
    public WhereExpression<T> or(String attributeName, Matcher<Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxDigitLeft, maxDigitRight, offsetValue));
        return this.or(other);
    }

    @Override
    public WhereExpression<T> or(String attributeName, Matcher<Integer> matcher, int maxNumDigits, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.or(other);
    }

    @Override
    public WhereExpression<T> or(String attributeName, Matcher<Long> matcher, int maxNumDigits, long offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.or(other);
    }

    @Override
    public WhereExpression<T> intersection(Condition other) {
        return new DefaultWhereExpression<T>(this.domainExpression, condition.intersection(other));
    }

    @Override
    public WhereExpression<T> intersection(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.intersection(other);
    }

    @Override
    public WhereExpression<T> intersection(String attributeName, Matcher<Float> matcher, int maxDigitLeft, int maxDigitRight, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxDigitLeft, maxDigitRight, offsetValue));
        return this.intersection(other);
    }

    @Override
    public WhereExpression<T> intersection(String attributeName, Matcher<Integer> matcher, int maxNumDigits, int offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.intersection(other);
    }

    @Override
    public WhereExpression<T> intersection(String attributeName, Matcher<Long> matcher, int maxNumDigits, long offsetValue) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(maxNumDigits, offsetValue));
        return this.intersection(other);
    }

    @Override
    public WhereExpression<T> group() {
        return new DefaultWhereExpression<T>(this.domainExpression, new ConditionGroup(this.condition));
    }

}
