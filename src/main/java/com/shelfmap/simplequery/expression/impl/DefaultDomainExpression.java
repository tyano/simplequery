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


import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;

import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.attribute.impl.ItemNameAttribute;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.WhereExpression;
import com.shelfmap.simplequery.expression.matcher.Matcher;

import static com.shelfmap.simplequery.util.Assertion.isNotNull;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainExpression<T> extends BaseExpression<T> implements DomainExpression<T>, Cloneable {
    private SelectQuery selectObject;

    public DefaultDomainExpression(AmazonSimpleDB simpleDB, Configuration configuration, SelectQuery selectObject, Domain<T> domain) {
        super(simpleDB, configuration, domain);
        isNotNull("selectObject", selectObject);
        this.selectObject = selectObject;
    }

    @Override
    public WhereExpression<T> where(Condition<?> expression) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, expression);
    }

    private <T> Condition<T> newCondition(ConditionAttribute attribute, Matcher<T> matcher) {
        return new DefaultCondition<T>(attribute, matcher);
    }

    @Override
    public WhereExpression<T> where(ConditionAttribute attribute, Matcher<?> matcher) {
        Condition<?> condition = newCondition(attribute, matcher);
        return this.where(condition);
    }

    @Override
    public WhereExpression<T> whereItemName(Matcher<?> matcher) {
        return this.where(ItemNameAttribute.INSTANCE, matcher);
    }

    @Override
    public String describe() {
        return selectObject.describe() + " from " + SimpleDBUtils.quoteName(getDomain().getDomainName());
    }

    @Override
    public SelectQuery getSelectQuery() {
        return this.selectObject;
    }

    @Override
    public LimitExpression<T> limit(int limitCount) {
        return new DefaultLimitExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, limitCount);
    }

    @Override
    public OrderByExpression<T> orderBy(ConditionAttribute attribute, SortOrder sortOrder) {
        return new DefaultOrderByExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, attribute, sortOrder);
    }

    @Override
    public DomainExpression<T> rebuildWith(SelectAttribute... attributes) {
        SelectQuery select = new Select(getAmazonSimpleDB(), getConfiguration(), attributes);
        return rebuildWith(select);
    }

    @Override
    public DomainExpression<T> rebuildWith(SelectQuery select) {
        return new DefaultDomainExpression<T>(getAmazonSimpleDB(), getConfiguration(), select, getDomain());
    }

    @Override
    @SuppressWarnings("unchecked")
    public DefaultDomainExpression<T> clone() {
        return (DefaultDomainExpression<T>)super.clone();
    }
}
