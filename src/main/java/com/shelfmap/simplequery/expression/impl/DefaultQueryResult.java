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
import com.amazonaws.services.simpledb.model.SelectResult;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.ItemConverter;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultQueryResult<T> implements QueryResults<T> {
    private final AmazonSimpleDB simpleDB;
    private final Expression<T> expression;
    private final SelectResult result;
    private final ItemConverter<T> itemConveter;

    public DefaultQueryResult(AmazonSimpleDB simpleDB, Expression<T> expression, SelectResult result, ItemConverter<T> itemConveter) {
        this.simpleDB = simpleDB;
        this.expression = expression;
        this.result = result;
        this.itemConveter = itemConveter;
    }

    @Override
    public Iterator<T> iterator() {
        return new SelectResultIterator<T>(simpleDB, expression, result, itemConveter);
    }

    @Override
    public int size() {
        try {
            return expression.count();
        } catch (SimpleQueryException ex) {
            throw new IllegalStateException("could not count the expression.", ex);
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}
