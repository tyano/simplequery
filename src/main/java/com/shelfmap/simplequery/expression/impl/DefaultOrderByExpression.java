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

import com.shelfmap.simplequery.expression.Expression;
import static com.shelfmap.simplequery.util.Assertion.*;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.WhereExpression;
import com.shelfmap.simplequery.util.Assertion;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultOrderByExpression<T> extends BaseExpression<T> implements OrderByExpression<T> {
    private final DomainExpression<T> domainExpression;
    private final WhereExpression<T> whereExpression;
    private final String attributeName;
    private final SortOrder sortOrder;

    public DefaultOrderByExpression(AmazonSimpleDB simpleDB, Configuration configuration, DomainExpression<T> domainExpression, String attributeName, SortOrder sortOrder) {
        this(simpleDB, 
             configuration,
             isNotNullAndReturn("domainExpression", domainExpression), 
             null, 
             attributeName, 
             sortOrder);
    }

    public DefaultOrderByExpression(AmazonSimpleDB simpleDB, Configuration configuration, final WhereExpression<T> whereExpression, String attributeName, SortOrder sortOrder) {
        this(simpleDB,
             configuration,
            isNotNullAndGet("whereExpression", whereExpression, 
                new Assertion.Accessor<DomainExpression<T>>() {
                    @Override
                    public DomainExpression<T> get() {
                        return whereExpression.getDomainExpression();
                    }
                }), 
            whereExpression, 
            attributeName, 
            sortOrder);
    }

    protected DefaultOrderByExpression(AmazonSimpleDB simpleDB, Configuration configuration, DomainExpression<T> domainExpression, WhereExpression<T> whereExpression, String attributeName, SortOrder sortOrder) {
        super(simpleDB, configuration);
        isNotNull("attributeName", attributeName);
        isNotNull("sortOrder", sortOrder);
        this.domainExpression = domainExpression;
        this.whereExpression = whereExpression;
        this.attributeName = attributeName;
        this.sortOrder = sortOrder;
    }

    @Override
    public WhereExpression<T> getWhereExpression() {
        return this.whereExpression;
    }

    @Override
    public DomainExpression<T> getDomainExpression() {
        return this.domainExpression;
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public String describe() {
        return (getWhereExpression() != null 
                ? getWhereExpression().describe() 
                : getDomainExpression().describe())
         + orderByExpression();
    }

    private String orderByExpression() {
        return " order by " + SimpleDBUtils.quoteName(attributeName) + " " + sortOrder.describe();
    }

    @Override
    public LimitExpression<T> limit(int limitCount) {
        return new DefaultLimitExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, limitCount);
    }

    @Override
    public Expression<T> rebuildWith(String... attributes) {
        SelectQuery select = new Select(getAmazonSimpleDB(), getConfiguration(), attributes);
        return domainExpression.rebuildWith(select);
    }
}
