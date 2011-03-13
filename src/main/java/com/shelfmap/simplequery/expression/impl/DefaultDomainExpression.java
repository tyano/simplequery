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

import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.SortOrder;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.WhereExpression;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainExpression<T> extends BaseExpression<T> implements DomainExpression<T> {
    private SelectQuery selectObject;
    private String domainName;
    private Class<T> typeToken;

    public DefaultDomainExpression(SelectQuery selectObject, String domainName, Class<T> typeToken) {
        isNotNull("selectObject", selectObject);
        isNotNull("domainName", domainName);
        isNotNull("typeToken", typeToken);
        this.selectObject = selectObject;
        this.domainName = domainName;
        this.typeToken = typeToken;
    }
    
    @Override
    public WhereExpression<T> where(Condition expression) {
        return new DefaultWhereExpression<T>(this, expression);
    }

    @Override
    public WhereExpression<T> where(String attributeName, Matcher<?> matcher) {
        Condition condition = new DefaultCondition(attributeName, matcher);
        return this.where(condition);
    }

    @Override
    public String describe() {
        return selectObject.describe() + " from " + domainName;
    }

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder) {
        return new DefaultOrderByExpression<T>(this, attributeName, sortOrder);
    }
}
