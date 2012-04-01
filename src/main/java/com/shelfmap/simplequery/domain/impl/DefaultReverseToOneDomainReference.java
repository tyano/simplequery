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
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainAttribute;
import com.shelfmap.simplequery.domain.ReverseToOneDomainReference;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import com.shelfmap.simplequery.expression.impl.InstanceQueryResult;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultReverseToOneDomainReference<M,T> extends AbstractReverseDomainReference<M,T> implements ReverseToOneDomainReference<T> {
    private static final long serialVersionUID = 1L;

    public DefaultReverseToOneDomainReference(Context context, M masterObject, Domain<? extends T> targetDomain, ConditionAttribute targetAttribute) {
        super(context, masterObject, targetDomain, targetAttribute);
    }

    @Override
    public QueryResults<T> getResults(boolean consistent) throws SimpleQueryException {
        QueryResults<T> results = super.getResults(consistent);
        List<T> element = new ArrayList<T>(1);
        for (T t : results) {
            element.add(t);
            break;
        }
        return new InstanceQueryResult<T>(getContext(), element);
    }

    @Override
    public void set(T object) {
        if(object == null) return;
        try {
            DomainAttribute<String,String> targetAttribute = getTargetDomainAttribute(getTargetDomain(), getTargetAttribute());
            T oldTarget = get(true);
            Context context = getContext();

            if(oldTarget != null) {
                //change the referencial attribute of old referenced object to null value for detaching the old object from this reference.
                targetAttribute.getAttributeAccessor().write(oldTarget, null);
                context.putObjects(oldTarget);
            }

            targetAttribute.getAttributeAccessor().write(object, getMasterItemName());

            //add the target object into context. it will be saved when context#save() is called.
            getContext().putObjects(object);
        } catch (SimpleQueryException ex) {
            throw new IllegalStateException("Could not get the referenced object from a ReverseToOneDomainReference.", ex);
        }
    }

    @Override
    public T get(boolean consist) throws SimpleQueryException {
        for (T result : getResults(consist)) {
            return result;
        }
        return null;
    }
}
