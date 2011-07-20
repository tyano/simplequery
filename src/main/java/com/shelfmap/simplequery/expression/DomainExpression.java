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

import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.expression.matcher.Matcher;

/**
 *
 * @author Tsutomu YANO
 */
public interface DomainExpression<T> extends Expression<T>{
    SelectQuery getSelectQuery();
    String getDomainName();
    Class<T> getDomainClass();
    WhereExpression<T> whereItemName(Matcher<?> matcher);
    WhereExpression<T> where(Condition<?> expression);
    WhereExpression<T> where(String attributeName, Matcher<?> matcher);
    WhereExpression<T> where(ConditionAttribute attribute, Matcher<?> matcher);
    OrderByExpression<T> orderBy(String attributeName, SortOrder sortOrder);
    LimitExpression<T> limit(int limitCount);
    DomainExpression<T> rebuildWith(SelectQuery select);

    @Override
    DomainExpression<T> rebuildWith(String... attributes);
}
