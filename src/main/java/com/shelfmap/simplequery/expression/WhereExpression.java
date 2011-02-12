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
 *
 * @author Tsutomu YANO
 */
public interface WhereExpression<T> extends Expression<T> {
    WhereExpression<T> and(Condition other);
    WhereExpression<T> and(String attributeName, Matcher<T> matcher);
    WhereExpression<T> or(Condition other);
    WhereExpression<T> or(String attributeName, Matcher<T> matcher);
    OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder);
}
