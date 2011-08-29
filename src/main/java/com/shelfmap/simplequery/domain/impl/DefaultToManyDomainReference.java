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
package com.shelfmap.simplequery.domain.impl;

import com.shelfmap.simplequery.Client;
import com.shelfmap.simplequery.attribute.Attributes;
import com.shelfmap.simplequery.domain.DomainReference;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import com.shelfmap.simplequery.expression.matcher.MatcherFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultToManyDomainReference<T> implements DomainReference<T> {

    private final Client client;
    private final String masterItemName;
    private final Class<T> targetDomainClass;
    private final String targetAttributeName;

    public DefaultToManyDomainReference(Client client, String masterItemName, Class<T> targetDomainClass, String targetAttributeName) {
        this.client = client;
        this.masterItemName = masterItemName;
        this.targetDomainClass = targetDomainClass;
        this.targetAttributeName = targetAttributeName;
    }

    @Override
    public Class<T> getDomainClass() {
        return this.targetDomainClass;
    }

    @Override
    public T get(boolean consistent) throws SimpleQueryException, MultipleResultsExistException {
        return createExpression().getSingleResult(consistent);
    }

    @Override
    public QueryResults<T> getResults(boolean consistent) throws SimpleQueryException {
        return createExpression().getResults(consistent);
    }

    private Expression<T> createExpression() {
        return getClient().select().from(getDomainClass()).where(Attributes.attr(getTargetAttributeName()), MatcherFactory.is(getMasterItemName()));
    }

    public String getTargetAttributeName() {
        return targetAttributeName;
    }

    public Client getClient() {
        return client;
    }

    public String getMasterItemName() {
        return masterItemName;
    }
}
