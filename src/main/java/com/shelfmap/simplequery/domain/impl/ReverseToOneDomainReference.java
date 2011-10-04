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
import com.shelfmap.simplequery.domain.ToOneDomainReference;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import com.shelfmap.simplequery.expression.impl.InstanceQueryResult;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tsutomu YANO
 */
public class ReverseToOneDomainReference<T> extends ReverseDomainReference<T> implements ToOneDomainReference<T> {

    public ReverseToOneDomainReference(Context context, String masterItemName, Domain<T> targetDomain, ConditionAttribute targetAttribute) {
        super(context, masterItemName, targetDomain, targetAttribute);
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

        DomainAttribute<String,String> targetAttribute = getTargetDomainAttribute(getTargetDomain(), getTargetAttribute());
        targetAttribute.getAttributeAccessor().write(object, getMasterItemName());
    }
}
