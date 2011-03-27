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
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class BaseExpression<T> implements Expression<T> {

    private final Configuration configuration;
    private final AmazonSimpleDB simpleDB;
    private final Class<T> domainClass;

    public BaseExpression(AmazonSimpleDB simpleDB, Configuration configuration, Class<T> domainClass) {
        isNotNull("simpleDB", simpleDB);
        isNotNull("configuration", configuration);
        isNotNull("domainClass", domainClass);
        this.simpleDB = simpleDB;
        this.configuration = configuration;
        this.domainClass = domainClass;
    }
    
    @Override
    public T getSingleResult() throws SimpleQueryException, MultipleResultsExistException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResults<T> getResults() throws SimpleQueryException {
        SelectRequest selectReq = new SelectRequest(describe());
        SelectResult result = simpleDB.select(selectReq);
        return new DefaultQueryResult<T>(simpleDB, this, result, getConfiguration().getItemConveter(domainClass));
    }
    
    public AmazonSimpleDB getAmazonSimpleDB() {
        return this.simpleDB;
    }

    public Configuration getConfiguration() {
        return configuration;
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
