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
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.SelectQuery;

/**
 *
 * @author Tsutomu YANO
 */
public class Select implements SelectQuery {
    private AmazonSimpleDB sdb;

    public Select(AmazonSimpleDB client) {
        this.sdb = client;
    }
    
    @Override
    public <T> DomainExpression<T> from(Class<T> target) {
        Domain annotation = target.getAnnotation(Domain.class);
        if(annotation == null) throw new IllegalArgumentException("the class object must have @Domain annotation.");
        String domainName = annotation.value();
        return new DomainExpressionImpl<T>(sdb, this, domainName, target);
    }

    public AmazonSimpleDB getSimpleDB() {
        return sdb;
    }
    
    public String describe() {
        return "select * ";
    }
}
