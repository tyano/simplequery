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
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.QueryResults;
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

    public DefaultQueryResult(AmazonSimpleDB simpleDB, Expression<T> expression, SelectResult result) {
        this.simpleDB = simpleDB;
        this.expression = expression;
        this.result = result;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public int size() {
        Expression<T> rebuilt = expression.rebuildWith("count(*)");
        SelectRequest req = new SelectRequest(rebuilt.describe());
        SelectResult selectResult = simpleDB.select(req);
        List<Item> items = selectResult.getItems();
        if(items.isEmpty()) throw new IllegalStateException("can not count records. expression was: " + rebuilt.describe());
        
        String value  = items.get(0).getAttributes().get(0).getValue();
        return Integer.parseInt(value);
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection<? extends T> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T get(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T set(int i, T e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(int i, T e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T remove(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<T> subList(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}