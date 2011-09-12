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
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.domain.DomainAttribute;
import com.shelfmap.simplequery.domain.DomainAttributes;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.WhereExpression;
import com.shelfmap.simplequery.expression.matcher.Matcher;
import com.shelfmap.simplequery.util.Assertion;

import static com.shelfmap.simplequery.util.Assertion.isNotNull;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultWhereExpression<T> extends BaseExpression<T> implements WhereExpression<T> {

    private DomainExpression<T> domainExpression;
    private Condition<?> condition;

    public DefaultWhereExpression(AmazonSimpleDB simpleDB, Configuration configuration, final DomainExpression<T> domainExpression, Condition<?> condition) {
        super(simpleDB,
                configuration,
                Assertion.isNotNullAndGet("domainExpression", domainExpression, new Assertion.Accessor<Class<T>>() {
                    @Override
                    public Class<T> get() {
                        return domainExpression.getDomainClass();
                    }
                }),
                domainExpression.getDomainName());

        isNotNull("condition", condition);
        this.domainExpression = domainExpression;
        this.condition = condition;
    }

    @Override
    public OrderByExpression<T> orderBy(ConditionAttribute attribute, SortOrder sortOrder) {
        return new DefaultOrderByExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, attribute, sortOrder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String describe() {
        StringBuilder sb = new StringBuilder();
        Class<T> domainClass = getDomainExpression().getDomainClass();
        String domainName = getDomainExpression().getDomainName();
        DomainAttributes domainAttributes = getConfiguration().getDomainAttributes(domainClass, domainName);

        Condition<?> current = condition;
        while (current.getParent() != null) {
            configureMatcher(domainAttributes, current);
            current = current.getParent();
        }

        sb.append(domainExpression.describe());
        sb.append(" where ");
        sb.append(condition.describe());
        return sb.toString();
    }

    private <AT> void configureMatcher(DomainAttributes domainAttributes, Condition<AT> current) {
        String attributeName = current.getAttribute().getAttributeName();
        Matcher<AT> matcher = current.getMatcher();
        if (matcher != null) {

            //TODO DomainAttributes#getAttribute might return an DomainAttribute whose type parameter don't match with the Condition 'current'.
            @SuppressWarnings("unchecked")
            DomainAttribute<AT,?> attribute = (DomainAttribute<AT,?>) domainAttributes.getAttribute(attributeName);
            if(attribute != null) {
                matcher.setAttributeConverter(attribute.getAttributeConverter());
            }
        }
    }

    @Override
    public DomainExpression<T> getDomainExpression() {
        return this.domainExpression;
    }

    @Override
    public Condition<?> getCondition() {
        return this.condition;
    }

    @Override
    public LimitExpression<T> limit(int limitCount) {
        return new DefaultLimitExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, limitCount);
    }

    @Override
    public WhereExpression<T> and(Condition<?> other) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, condition.and(other));
    }

    @Override
    public WhereExpression<T> and(ConditionAttribute attribute, Matcher<?> matcher) {
        Condition<?> other = newCondition(attribute, matcher);
        return this.and(other);
    }

    private <T> Condition<T> newCondition(ConditionAttribute attribute, Matcher<T> matcher) {
        return new DefaultCondition<T>(attribute, matcher);
    }

    @Override
    public <E> WhereExpression<T> and(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
        return this.and(other);
    }

    @Override
    public WhereExpression<T> or(Condition<?> other) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, condition.or(other));
    }

    @Override
    public WhereExpression<T> or(ConditionAttribute attribute, Matcher<?> matcher) {
        Condition<?> other = newCondition(attribute, matcher);
        return this.or(other);
    }

    @Override
    public <E> WhereExpression<T> or(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
        return this.or(other);
    }

    @Override
    public WhereExpression<T> intersection(Condition<?> other) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, condition.intersection(other));
    }

    @Override
    public WhereExpression<T> intersection(ConditionAttribute attribute, Matcher<?> matcher) {
        Condition<?> other = newCondition(attribute, matcher);
        return this.intersection(other);
    }

    @Override
    public <E> WhereExpression<T> intersection(ConditionAttribute attribute, Matcher<E> matcher, AttributeConverter<E> attributeConverter) {
        Condition<?> other = newCondition(attribute, matcher.withAttributeConverter(attributeConverter));
        return this.intersection(other);
    }

    @Override
    public WhereExpression<T> group() {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, new ConditionGroup(this.condition));
    }

    @Override
    public WhereExpression<T> rebuildWith(SelectAttribute... attributes) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(),
                getConfiguration(),
                domainExpression.rebuildWith(attributes),
                condition);
    }

    @Override
    public WhereExpression<T> not() {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this.domainExpression, new NotCondition(this.condition));
    }
}
