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
import com.shelfmap.simplequery.expression.CanNotConvertItemException;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.ItemConverter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tsutomu YANO
 */
public class SelectResultIterator<T> implements Iterator<T> {
    private AmazonSimpleDB simpleDB;
    private Expression<?> expression;
    private SelectResult currentResult;
    private ItemConverter<T> itemConverter;
    private List<Item> currentItemList;
    private int currentListSize;
    private int currentIndex;
    

    public SelectResultIterator(AmazonSimpleDB simpleDB, Expression<?> expression, SelectResult result, ItemConverter<T> itemConveter) {
        this.simpleDB = simpleDB;
        this.expression = expression;
        this.currentResult = result;
        this.currentItemList = result.getItems();
        this.currentIndex = 0;
        this.currentListSize = this.currentItemList.size();
        this.itemConverter = itemConveter;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < currentItemList.size() || currentResult.getNextToken() != null;
    }

    @Override
    public T next() {
        Item item = null;
        
        if(currentIndex >= currentListSize && currentResult.getNextToken() != null) {
            retrieveNextItems();
        }
        if(currentIndex < currentListSize) {
            item = currentItemList.get(currentIndex);
            currentIndex++;
        }
        
        T instance = null;
        try {
            instance = itemConverter.convert(item);
        } catch (CanNotConvertItemException ex) {
            throw new IllegalStateException("Could not convert an item to a domain object.", ex);
        }
        
        return instance;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    protected void retrieveNextItems() {
        final String nextToken = currentResult.getNextToken();
        if(nextToken != null) {
            SelectRequest request = new SelectRequest(expression.describe()).withNextToken(nextToken);
            SelectResult result = simpleDB.select(request);
            currentResult = result;
            currentItemList = currentResult.getItems();
            currentListSize = currentItemList.size();
            currentIndex = 0;
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<Item> getCurrentItemList() {
        return currentItemList;
    }

    public int getCurrentListSize() {
        return currentListSize;
    }

    public SelectResult getCurrentResult() {
        return currentResult;
    }

    public Expression<?> getExpression() {
        return expression;
    }

    public AmazonSimpleDB getSimpleDB() {
        return simpleDB;
    }
}
