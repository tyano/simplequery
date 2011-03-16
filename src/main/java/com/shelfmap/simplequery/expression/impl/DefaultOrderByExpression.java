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

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.expression.LimitExpression;
import com.shelfmap.simplequery.expression.SortOrder;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.OrderByExpression;
import com.shelfmap.simplequery.expression.WhereExpression;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultOrderByExpression<T> extends BaseExpression<T> implements OrderByExpression<T> {

    protected enum ParentType { DOMAIN, WHERE }
    
    private final ParentType parentType;
    private final DomainExpression<T> domainExpression;
    private final WhereExpression<T> whereExpression;
    private final String attributeName;
    private final SortOrder sortOrder;

    public DefaultOrderByExpression(DomainExpression<T> domainExpression, String attributeName, SortOrder sortOrder) {
        this(domainExpression, null, ParentType.DOMAIN, attributeName, sortOrder);
    }

    public DefaultOrderByExpression(WhereExpression<T> whereExpression, String attributeName, SortOrder sortOrder) {
        this(whereExpression.getDomainExpression(), whereExpression, ParentType.WHERE, attributeName, sortOrder);
    }
    
    protected DefaultOrderByExpression(DomainExpression<T> domainExpression, WhereExpression<T> whereExpression, ParentType parentType, String attributeName, SortOrder sortOrder) {
        isNotNull("parentType", parentType);
        
        switch(parentType) {
            case DOMAIN:
                isNotNull("domainExpression", domainExpression);
                break;
            case WHERE:
                isNotNull("whereExpression", whereExpression);
                break;
            default:
                throw new IllegalStateException("No such ParentType: " + parentType);
        }
        
        isNotNull("attributeName", attributeName);
        isNotNull("sortOrder", sortOrder);
        this.domainExpression = domainExpression;
        this.whereExpression = whereExpression;
        this.parentType = parentType;
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
        switch(parentType) {
            case DOMAIN:
                return getDomainExpression().describe() + orderByExpression();
            case WHERE:
                return getWhereExpression().describe() + orderByExpression();
            default:
                throw new IllegalStateException("No such parentType: " + parentType);
        }
    }
    
    private String orderByExpression() {
        return " order by " + SimpleDBUtils.quoteName(attributeName) + " " + sortOrder.describe();
    }
    
    @Override
    public LimitExpression<T> limit(int limitCount) {
        return new DefaultLimitExpression<T>(this, limitCount);
    }
}
