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

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import com.shelfmap.simplequery.expression.matcher.MatcherFactory;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.referTo;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class AbstractReverseDomainReference<M,T> implements DomainReference<T>, ReverseReference {
    private final Context context;
    private final M masterObject;
    private final Domain<T> targetDomain;
    private final ConditionAttribute targetAttribute;

    public AbstractReverseDomainReference(Context context, M masterObject, Domain<T> targetDomain, ConditionAttribute targetAttribute) {
        this.context = context;
        this.masterObject = masterObject;
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
        return getContext().createNewClient().select().from(getTargetDomain().getDomainClass()).where(getTargetAttribute(), referTo(getMasterItemName()));
    }

    public ConditionAttribute getTargetAttribute() {
        return targetAttribute;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public String getMasterItemName() {
        Class<?> clazz = masterObject.getClass();
        Domain<?> domain = context.getDomainFactory().findDomain(clazz);
        DomainDescriptor descriptor = context.createDomainDescriptor(domain);
        return descriptor.getItemNameAttribute().getAttributeAccessor().read(masterObject);
    }

    protected <T> DomainAttribute<String,String> getTargetDomainAttribute(Domain<T> targetDomain, ConditionAttribute attribute) {
        DomainDescriptor descriptor = getContext().createDomainDescriptor(targetDomain);
        return descriptor.getAttribute(attribute.getAttributeName(), String.class, String.class);
    }
}
