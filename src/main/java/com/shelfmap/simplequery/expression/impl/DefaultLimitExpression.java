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
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.WhereExpression;
import com.shelfmap.simplequery.util.Assertion;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultLimitExpression<T> extends BaseExpression<T> implements LimitExpression<T> {

    private final int limitCount;
    private DomainExpression<T> domainExpression;
    private WhereExpression<T> whereExpression;
    private OrderByExpression<T> orderByExpression;

    protected DefaultLimitExpression(AmazonSimpleDB simpleDB, DomainExpression<T> domainExpression, WhereExpression<T> whereExpression, OrderByExpression<T> orderByExpression, int limitCount) {
        super(simpleDB);
        this.limitCount = limitCount;
        this.domainExpression = domainExpression;
        this.whereExpression = whereExpression;
        this.orderByExpression = orderByExpression;
    }

    public DefaultLimitExpression(AmazonSimpleDB simpleDB, DomainExpression<T> domainExpression, int limitCount) {
        this(simpleDB,
                isNotNullAndReturn("domainExpression", domainExpression),
                null,
                null,
                limitCount);
    }

    public DefaultLimitExpression(AmazonSimpleDB simpleDB, final WhereExpression<T> whereExpression, int limitCount) {
        this(simpleDB,
                isNotNullAndGet("whereExpression", whereExpression,
                    new Assertion.Accessor<DomainExpression<T>>() {
                        @Override
                        public DomainExpression<T> get() {
                            return whereExpression.getDomainExpression();
                        }
                    }),
                whereExpression,
                null,
                limitCount);
    }

    public DefaultLimitExpression(AmazonSimpleDB simpleDB, final OrderByExpression<T> orderByExpression, int limitCount) {
        this(simpleDB,
                isNotNullAndGet("orderByExpression", orderByExpression, 
                    new Assertion.Accessor<DomainExpression<T>>() {
                        @Override
                        public DomainExpression<T> get() {
                            return orderByExpression.getDomainExpression();
                        }
                    }),
                orderByExpression.getWhereExpression(),
                orderByExpression,
                limitCount);
    }

    @Override
    public String describe() {
        return (orderByExpression != null ? orderByExpression.describe()
                : whereExpression != null ? whereExpression.describe()
                : domainExpression.describe())
                + " limit " + limitCount;
    }

    @Override
    public DomainExpression<T> getDomainExpression() {
        return this.domainExpression;
    }

    @Override
    public WhereExpression<T> getWhereExpression() {
        return this.whereExpression;
    }

    @Override
    public OrderByExpression<T> getOrderByExpression() {
        return this.orderByExpression;
    }

    @Override
    public int getLimitCount() {
        return this.limitCount;
    }

    @Override
    public Expression<T> rebuildWith(String... attributes) {
        SelectQuery select = new Select(getAmazonSimpleDB(), attributes);
        return domainExpression.rebuildWith(select);
    }
}