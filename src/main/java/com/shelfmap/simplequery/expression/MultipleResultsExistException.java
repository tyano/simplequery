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

/**
 * A exception class that means a query return multiple records, although
 * user request an only single result.
 * 
 * @author Tsutomu YANO
 */
public class MultipleResultsExistException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public MultipleResultsExistException(Throwable cause) {
        super(cause);
    }

    public MultipleResultsExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleResultsExistException(String message) {
        super(message);
    }

    public MultipleResultsExistException() {
    }

}
