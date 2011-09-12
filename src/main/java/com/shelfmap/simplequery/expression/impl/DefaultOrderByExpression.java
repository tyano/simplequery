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


import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.amazonaws.services.simpledb.AmazonSimpleDB;

import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.WhereExpression;
import com.shelfmap.simplequery.util.Assertion;

import static com.shelfmap.simplequery.util.Assertion.*;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultOrderByExpression<T> extends BaseExpression<T> implements OrderByExpression<T> {
    private final DomainExpression<T> domainExpression;
    private final WhereExpression<T> whereExpression;
    private final ConditionAttribute attribute;
    private final SortOrder sortOrder;

    public DefaultOrderByExpression(AmazonSimpleDB simpleDB, Configuration configuration, DomainExpression<T> domainExpression, ConditionAttribute attribute, SortOrder sortOrder) {
        this(simpleDB,
             configuration,
             isNotNullAndReturn("domainExpression", domainExpression),
             null,
             attribute,
             sortOrder);
    }

    public DefaultOrderByExpression(AmazonSimpleDB simpleDB, Configuration configuration, final WhereExpression<T> whereExpression, ConditionAttribute attribute, SortOrder sortOrder) {
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
            attribute,
            sortOrder);
    }

    protected DefaultOrderByExpression(AmazonSimpleDB simpleDB, Configuration configuration, final DomainExpression<T> domainExpression, WhereExpression<T> whereExpression, ConditionAttribute attribute, SortOrder sortOrder) {
        super(simpleDB,
                configuration,
                Assertion.isNotNullAndGet("domainExpression", domainExpression, new Assertion.Accessor<Class<T>>() {
                    @Override
                    public Class<T> get() {
                        return domainExpression.getDomainClass();
                    }
                }),
                domainExpression.getDomainName());

        isNotNull("attributeName", attribute);
        isNotNull("sortOrder", sortOrder);
        this.domainExpression = domainExpression;
        this.whereExpression = whereExpression;
        this.attribute = attribute;
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
    public ConditionAttribute getAttribute() {
        return attribute;
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
        return " order by " + attribute.describe() + " " + sortOrder.describe();
    }

    @Override
    public LimitExpression<T> limit(int limitCount) {
        return new DefaultLimitExpression<T>(getAmazonSimpleDB(), getConfiguration(), this, limitCount);
    }

    @Override
    public OrderByExpression<T> rebuildWith(SelectAttribute... attributes) {
        return (getWhereExpression() != null)
                ? new DefaultOrderByExpression<T>(getAmazonSimpleDB(),
                                                  getConfiguration(),
                                                  getWhereExpression().rebuildWith(attributes),
                                                  attribute,
                                                  sortOrder)
                : new DefaultOrderByExpression<T>(getAmazonSimpleDB(),
                                                  getConfiguration(),
                                                  getDomainExpression().rebuildWith(attributes),
                                                  attribute,
                                                  sortOrder);
    }
}
