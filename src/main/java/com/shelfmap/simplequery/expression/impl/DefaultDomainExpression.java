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
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.attribute.impl.DefaultAttribute;
import com.shelfmap.simplequery.attribute.impl.ItemNameAttribute;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.matcher.Matcher;
import com.shelfmap.simplequery.expression.WhereExpression;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainExpression<T> extends BaseExpression<T> implements DomainExpression<T>, Cloneable {
    private SelectQuery selectObject;
    private String domainName;
    private Class<T> typeToken;

    public DefaultDomainExpression(AmazonSimpleDB simpleDB, Configuration configuration, SelectQuery selectObject, String domainName, Class<T> typeToken) {
        super(simpleDB, configuration, typeToken, domainName);
        isNotNull("selectObject", selectObject);
        isNotNull("domainName", domainName);
        isNotNull("typeToken", typeToken);
        this.selectObject = selectObject;
        this.domainName = domainName;
        this.typeToken = typeToken;
    }
    
    @Override
    public WhereExpression<T> where(Condition<?> expression) {
        return new DefaultWhereExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, expression);
    }

    private <T> Condition<T> newCondition(ConditionAttribute attribute, Matcher<T> matcher) {
        return new DefaultCondition<T>(attribute, matcher);
    }
    
    @Override
    public WhereExpression<T> where(String attributeName, Matcher<?> matcher) {
        Condition<?> condition = newCondition(new DefaultAttribute(attributeName), matcher);
        return this.where(condition);
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
        return selectObject.describe() + " from " + SimpleDBUtils.quoteName(domainName);
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public Class<T> getDomainClass() {
        return typeToken;
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
    public OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder) {
        return new DefaultOrderByExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, attributeName, sortOrder);
    }

    @Override
    public DomainExpression<T> rebuildWith(String... attributes) {
        SelectQuery select = new Select(getAmazonSimpleDB(), getConfiguration(), attributes);
        return rebuildWith(select);
    }

    @Override
    public DomainExpression<T> rebuildWith(SelectQuery select) {
        return new DefaultDomainExpression<T>(getAmazonSimpleDB(), getConfiguration(), select, domainName, typeToken);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DefaultDomainExpression<T> clone() {
        return (DefaultDomainExpression<T>)super.clone();
    }
}
