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

import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;

/**
 *
 * @author Tsutomu YANO
 */
public class BaseExpression<T> implements Expression<T> {

    @Override
    public T singleResult() throws SimpleQueryException, MultipleResultsExistException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryResults<T> results() throws SimpleQueryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
