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

import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.LimitExpression;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.SortOrder;
import com.shelfmap.simplequery.expression.WhereExpression;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultWhereExpression<T> extends BaseExpression<T> implements WhereExpression<T> {
    private DefaultDomainExpression<T> domainExpression;

    public DefaultWhereExpression(DefaultDomainExpression<T> domainExpression) {
        isNotNull("domainExpression", domainExpression);
        this.domainExpression = domainExpression;
    }
    
    @Override
    public WhereExpression<T> and(Condition other) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WhereExpression<T> and(String attributeName, Matcher<T> matcher) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WhereExpression<T> or(Condition other) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WhereExpression<T> or(String attributeName, Matcher<T> matcher) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String describe() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DomainExpression<T> getDomainExpression() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Condition getCondition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LimitExpression<T> limit(int limitCount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
