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

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.expression.DomainAttribute;
import com.shelfmap.simplequery.expression.AttributeInfo;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.LimitExpression;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.DomainAttributes;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.WhereExpression;
import com.shelfmap.simplequery.util.Assertion;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultWhereExpression<T> extends BaseExpression<T> implements WhereExpression<T> {

    private DomainExpression<T> domainExpression;
    private Condition condition;

    public DefaultWhereExpression(AmazonSimpleDB simpleDB, Configuration configuration, final DomainExpression<T> domainExpression, Condition condition) {
        super(simpleDB, 
              configuration, 
              Assertion.isNotNullAndGet("domainExpression", domainExpression, new Assertion.Accessor<Class<T>>() {
                    @Override
                    public Class<T> get() {
                        return domainExpression.getDomainClass();
                    }
              })); 
        isNotNull("condition", condition);
        this.domainExpression = domainExpression;
        this.condition = condition;
    }

    @Override
    public OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder) {
        return new DefaultOrderByExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, attributeName, sortOrder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String describe() {
        StringBuilder sb = new StringBuilder();
        Class<T> domainClass = getDomainExpression().getDomainClass();
        DomainAttributes domainAttribute = new BeanDomainAttributes(domainClass);

        Condition current = condition;
        while (current.getParent() != null) {
            String attributeName = current.getAttributeName();
            Matcher<?> matcher = current.getMatcher();
            if (matcher != null) {
                if (domainAttribute.isAttributeDefined(attributeName)) {
                    DomainAttribute<?> attribute = domainAttribute.getAttribute(attributeName);
                    Class<?> type = attribute.getType();
                    if (type == Float.class) {
                        ((Matcher<Float>) matcher).setAttributeInfo((AttributeInfo<Float>)attribute.getAttributeInfo());
                    } else if (type == Integer.class) {
                        ((Matcher<Integer>) matcher).setAttributeInfo((AttributeInfo<Integer>)attribute.getAttributeInfo());
                    } else if (type == Long.class) {
                        ((Matcher<Long>) matcher).setAttributeInfo((AttributeInfo<Long>)attribute.getAttributeInfo());
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
        return new DefaultLimitExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, limitCount);
    }

    @Override
    public WhereExpression<T> and(Condition other) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, condition.and(other));
    }

    @Override
    public WhereExpression<T> and(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.and(other);
    }

    @Override
    public <E> WhereExpression<T> and(String attributeName, Matcher<E> matcher, AttributeInfo<E> attributeInfo) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
        return this.and(other);
    }

    @Override
    public WhereExpression<T> or(Condition other) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, condition.or(other));
    }

    @Override
    public WhereExpression<T> or(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.or(other);
    }

    @Override
    public <E> WhereExpression<T> or(String attributeName, Matcher<E> matcher, AttributeInfo<E> attributeInfo) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
        return this.or(other);
    }

    @Override
    public WhereExpression<T> intersection(Condition other) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, condition.intersection(other));
    }

    @Override
    public WhereExpression<T> intersection(String attributeName, Matcher<?> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.intersection(other);
    }

    @Override
    public <E> WhereExpression<T> intersection(String attributeName, Matcher<E> matcher, AttributeInfo<E> attributeInfo) {
        Condition other = new DefaultCondition(attributeName, matcher.withAttributeInfo(attributeInfo));
        return this.intersection(other);
    }

    @Override
    public WhereExpression<T> group() {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, new ConditionGroup(this.condition));
    }

    @Override
    public Expression<T> rebuildWith(String... attributes) {
        SelectQuery select = new Select(getAmazonSimpleDB(), getConfiguration(), attributes);
        return domainExpression.rebuildWith(select);
    }
}
