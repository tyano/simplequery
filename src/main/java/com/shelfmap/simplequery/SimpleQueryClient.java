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

package com.shelfmap.simplequery;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.impl.Select;

/**
 *
 * @author Tsutomu YANO
 */
public class SimpleQueryClient implements Client {
    private final AmazonSimpleDB simpleDB;

    public SimpleQueryClient(AmazonSimpleDB simpleDb) {
        this.simpleDB = simpleDb;
    }
    
    @Override
    public AmazonSimpleDB getSimpleDB() {
        return this.simpleDB;
    }

    @Override
    public SelectQuery select(String... attribute) {
        return newSelectQuery(attribute);
    }
    
    protected SelectQuery newSelectQuery(String... attribute) {
        return new Select(simpleDB, attribute);
    }
}