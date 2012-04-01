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
import com.shelfmap.simplequery.domain.ReverseToManyDomainReference;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.SimpleQueryException;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultReverseToManyDomainReference<M,T> extends AbstractReverseDomainReference<M,T> implements ReverseToManyDomainReference<T> {
    private static final long serialVersionUID = 1L;
    public DefaultReverseToManyDomainReference(Context context, M masterObject, Domain<T> targetDomain, ConditionAttribute targetAttribute) {
        super(context, masterObject, targetDomain, targetAttribute);
    }

    @Override
    public void add(T... objects) {
        if(objects == null || objects.length == 0) return;

        DomainAttribute<String,String> targetAttribute = getTargetDomainAttribute(getTargetDomain(), getTargetAttribute());
        for (T target : objects) {
            targetAttribute.getAttributeAccessor().write(target, getMasterItemName());

            //add the target object into context. it will be saved when context#save() is called.
            getContext().putObjects(target);
        }
    }

    @Override
    public T get(boolean consistent) throws SimpleQueryException, MultipleResultsExistException {
        return createExpression().getSingleResult(consistent);
    }
}
