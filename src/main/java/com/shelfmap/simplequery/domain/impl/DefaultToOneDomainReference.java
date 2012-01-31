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
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;

/**
 *
 * @param <T>
 * @author Tsutomu YANO
 */
public class DefaultToOneDomainReference<T> implements ToOneDomainReference<T>, ForwardReference {

    private final Context context;
    private final Domain<T> targetDomain;
    private String targetItemName = null;

    public DefaultToOneDomainReference(Context context, Domain<T> targetDomain) {
        this.context = context;
        this.targetDomain = targetDomain;
    }

    @Override
    public Domain<T> getTargetDomain() {
        return targetDomain;
    }

    @Override
    public T get(boolean consistent) throws SimpleQueryException, MultipleResultsExistException {
        return getTargetItemName() == null ? null : createExpression().getSingleResult(consistent);
    }

    @Override
    public QueryResults<T> getResults(boolean consistent) throws SimpleQueryException {
        return getTargetItemName() == null ? null : createExpression().getResults(consistent);
    }

    private Expression<T> createExpression() {
        return context.select().from(getTargetDomain().getDomainClass()).whereItemName(is(getTargetItemName()));
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public String getTargetItemName() {
        return targetItemName;
    }

    @Override
    public void setTargetItemName(String targetItemName) {
        this.targetItemName = targetItemName;
    }

    @Override
    public void set(T object) {
        this.targetItemName = getContext().getDomainDescriptorFactory().create(getTargetDomain()).getItemNameFrom(object);
    }

    private DomainAttribute<?,?> findItemNameAttribute() {
        DomainDescriptor descriptor = getContext().getDomainDescriptorFactory().create(getTargetDomain());
        return descriptor.getItemNameAttribute();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultToOneDomainReference<T> other = (DefaultToOneDomainReference<T>) obj;
        if (this.targetDomain != other.targetDomain && (this.targetDomain == null || !this.targetDomain.equals(other.targetDomain))) {
            return false;
        }
        if ((this.targetItemName == null) ? (other.targetItemName != null) : !this.targetItemName.equals(other.targetItemName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.targetDomain != null ? this.targetDomain.hashCode() : 0);
        hash = 73 * hash + (this.targetItemName != null ? this.targetItemName.hashCode() : 0);
        return hash;
    }
}
