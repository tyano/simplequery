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

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.attribute.impl.CountAttribute;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.expression.*;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import java.util.List;

/**
 *
 * @param <T> the type of the domain-class on which this expression has been created.
 * @author Tsutomu YANO
 */
public abstract class BaseExpression<T> implements Expression<T> {
    private final Context context;
    private final Domain<T> domain;
    private final AmazonSimpleDB simpleDB;

    public BaseExpression(Context context, Domain<T> domain) {
        isNotNull("context", context);
        isNotNull("domain", domain);
        this.context = context;
        this.domain = domain;

        this.simpleDB = context.getSimpleDB();
    }

    @Override
    public T getSingleResult(boolean consistent) throws SimpleQueryException, MultipleResultsExistException {
        String expression = describe();
        SelectRequest selectReq = new SelectRequest(expression, consistent);
        SelectResult result = simpleDB.select(selectReq);
        List<Item> items = result.getItems();
        if(items.size() > 1) throw new MultipleResultsExistException("more than 1 results returned by the expression: " + expression);
        if(items.isEmpty()) return null;

        Item first = items.get(0);
        try {
            return getContext().getItemConverterFactory().create(getDomain()).convertToInstance(first);
        } catch (CanNotConvertItemException ex) {
            throw new SimpleQueryException("Can not convert an item", ex);
        }
    }

    @Override
    public QueryResults<T> getResults(boolean consistent) throws SimpleQueryException {
        SelectRequest selectReq = new SelectRequest(describe(), consistent);
        SelectResult result = simpleDB.select(selectReq);
        return new DefaultQueryResult<T>(getContext(), getDomain(), this, result);
    }

    @Override
    public int count() throws SimpleQueryException {
        Expression<T> rebuilt = rebuildWith(CountAttribute.INSTANCE);
        SelectRequest req = new SelectRequest(rebuilt.describe());
        SelectResult selectResult = simpleDB.select(req);
        List<Item> items = selectResult.getItems();
        if(items.isEmpty()) throw new SimpleQueryException("can not count records. expression was: " + rebuilt.describe());

        String value  = items.get(0).getAttributes().get(0).getValue();
        return Integer.parseInt(value);
    }

    @Override
    public Context getContext() {
        return context;
    }

    public Domain<T> getDomain() {
        return domain;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            //never occured.
            throw new IllegalStateException(ex);
        }
    }
}
