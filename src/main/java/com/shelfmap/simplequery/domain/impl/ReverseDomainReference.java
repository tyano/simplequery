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

import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;
import com.shelfmap.simplequery.Client;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainAttribute;
import com.shelfmap.simplequery.domain.DomainAttributes;
import com.shelfmap.simplequery.domain.DomainReference;
import com.shelfmap.simplequery.domain.ReverseReference;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class ReverseDomainReference<T> implements DomainReference<T>, ReverseReference {
    private final Client client;
    private final String masterItemName;
    private final Domain<T> targetDomain;
    private final ConditionAttribute targetAttribute;

    public ReverseDomainReference(Client client, String masterItemName, Domain<T> targetDomain, ConditionAttribute targetAttribute) {
        this.client = client;
        this.masterItemName = masterItemName;
        this.targetDomain = targetDomain;
        this.targetAttribute = targetAttribute;
    }


    @Override
    public Domain<T> getTargetDomain() {
        return this.targetDomain;
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
        return getClient().select().from(getTargetDomain().getDomainClass()).where(getTargetAttribute(), is(getMasterItemName()));
    }

    public ConditionAttribute getTargetAttribute() {
        return targetAttribute;
    }

    public Client getClient() {
        return client;
    }

    public String getMasterItemName() {
        return masterItemName;
    }

    protected <T> DomainAttribute<String,String> getTargetDomainAttribute(Domain<T> targetDomain, ConditionAttribute attribute) {
        Configuration configuration = getClient().getConfiguration();
        DomainAttributes attributes = configuration.getDomainAttributes(targetDomain);
        return attributes.getAttribute(attribute.getAttributeName(), String.class, String.class);
    }
}
