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

import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.attribute.impl.CountAttribute;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.expression.CanNotConvertItemException;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import java.util.List;

/**
 *
 * @param <T> the type of the domain-class on which this expression has been created.
 * @author Tsutomu YANO
 */
public abstract class BaseExpression<T> implements Expression<T> {
    private final Configuration configuration;
    private final AmazonSimpleDB simpleDB;
    private final Domain<T> domain;

    public BaseExpression(AmazonSimpleDB simpleDB, Configuration configuration, Domain<T> domain) {
        isNotNull("simpleDB", simpleDB);
        isNotNull("configuration", configuration);
        isNotNull("domain", domain);
        this.simpleDB = simpleDB;
        this.configuration = configuration;
        this.domain = domain;
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
            return getConfiguration().getItemConverter(getDomain()).convert(first);
        } catch (CanNotConvertItemException ex) {
            throw new SimpleQueryException("Can not convert an item", ex);
        }
    }

    @Override
    public QueryResults<T> getResults(boolean consistent) throws SimpleQueryException {
        SelectRequest selectReq = new SelectRequest(describe(), consistent);
        SelectResult result = simpleDB.select(selectReq);
        return new DefaultQueryResult<T>(simpleDB, this, result, getConfiguration().getItemConverter(getDomain()));
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

    public AmazonSimpleDB getAmazonSimpleDB() {
        return this.simpleDB;
    }

    public Configuration getConfiguration() {
        return configuration;
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
