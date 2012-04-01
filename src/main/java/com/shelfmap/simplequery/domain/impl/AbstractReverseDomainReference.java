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
import com.shelfmap.simplequery.expression.*;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.referTo;
import java.io.Serializable;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class AbstractReverseDomainReference<M,T> implements DomainReference<T>, ReverseReference, Serializable {
    private static final long serialVersionUID = 1L;

    private final Context context;
    private final M masterObject;
    private final Domain<? extends T> targetDomain;
    private final ConditionAttribute targetAttribute;

    public AbstractReverseDomainReference(Context context, M masterObject, Domain<? extends T> targetDomain, ConditionAttribute targetAttribute) {
        this.context = context;
        this.masterObject = masterObject;
        this.targetDomain = targetDomain;
        this.targetAttribute = targetAttribute;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Domain<T> getTargetDomain() {
        return (Domain<T>) this.targetDomain;
    }

    @Override
    public QueryResults<T> getResults(boolean consistent) throws SimpleQueryException {
        return createExpression().getResults(consistent);
    }

    protected Expression<T> createExpression() {
        return getContext().select().from(getTargetDomain().getDomainClass()).where(getTargetAttribute(), referTo(getMasterItemName())).orderBy(getTargetAttribute(), SortOrder.Asc);
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
        DomainDescriptor descriptor = context.getDomainDescriptorFactory().create(domain);
        return descriptor.getItemNameFrom(masterObject);
    }

    protected <T> DomainAttribute<String,String> getTargetDomainAttribute(Domain<T> targetDomain, ConditionAttribute attribute) {
        DomainDescriptor descriptor = getContext().getDomainDescriptorFactory().create(targetDomain);
        return descriptor.getAttribute(attribute.getAttributeName(), String.class, String.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractReverseDomainReference<M, T> other = (AbstractReverseDomainReference<M, T>) obj;
        if (this.targetDomain != other.targetDomain && (this.targetDomain == null || !this.targetDomain.equals(other.targetDomain))) {
            return false;
        }
        if (this.targetAttribute != other.targetAttribute && (this.targetAttribute == null || !this.targetAttribute.equals(other.targetAttribute))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.targetDomain != null ? this.targetDomain.hashCode() : 0);
        hash = 29 * hash + (this.targetAttribute != null ? this.targetAttribute.hashCode() : 0);
        return hash;
    }
}
