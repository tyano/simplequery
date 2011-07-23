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

package com.shelfmap.simplequery.expression;

import com.shelfmap.simplequery.attribute.SelectAttribute;

/**
 * The base interface which all expressions depend on.
 * All expressions must be able to return the results of their own evaluation.
 * 
 * @author Tsutomu YANO
 */
public interface Expression<T> extends Describable {
    /**
     * this method return a instance which is the result of 
     * evaluation of this expression object.
     * <p>
     * if the number of result instances is more than one, this method throw
     * @return A result of evaluation of this expression.
     * @throws SimpleQueryException something bad occurs. 
     *         The result of getCause() is the original exception object that explain about the reason of error occured.
     * @throws MultipleResultsExistException the number of result instances is more than one.
     */
    T getSingleResult(boolean consistent) throws SimpleQueryException, MultipleResultsExistException;
    
    /**
     * You can get the result of this expression.
     * The result object will be empty if no result exists on this expression.
     *  
     * @return An QueryResults object which contains all records returned by this expression.
     * the QueryResults object will be empty, if no record returned from SimpleDB with this expression. 
     * @throws SimpleQueryException SimpleQueryException something bad occurs. 
     *         The result of getCause() is the original exception object that explain about the reason of error occured.
     */
    QueryResults<T> getResults(boolean consistent) throws SimpleQueryException;
    
    int count() throws SimpleQueryException;
    Expression<T> rebuildWith(SelectAttribute... attributes);
}
