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
    private DomainExpression<T> domainExpression;
    private Condition condition;

    public DefaultWhereExpression(DomainExpression<T> domainExpression, Condition condition) {
        isNotNull("domainExpression", domainExpression);
        isNotNull("condition", condition);
        this.domainExpression = domainExpression;
        this.condition = condition;
    }
    
    @Override
    public WhereExpression<T> and(Condition other) {
        return new DefaultWhereExpression<T>(this.domainExpression, condition.and(other));
    }

    @Override
    public WhereExpression<T> and(String attributeName, Matcher<T> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.and(other);
    }

    @Override
    public WhereExpression<T> or(Condition other) {
        return new DefaultWhereExpression<T>(this.domainExpression, condition.or(other));
    }

    @Override
    public WhereExpression<T> or(String attributeName, Matcher<T> matcher) {
        Condition other = new DefaultCondition(attributeName, matcher);
        return this.or(other);
    }

    @Override
    public OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDomainExpression().describe());
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

}
