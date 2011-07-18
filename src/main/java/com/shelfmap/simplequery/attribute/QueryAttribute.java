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
package com.shelfmap.simplequery.attribute;

import com.shelfmap.simplequery.expression.Describable;

/**
 * A simple base interface which express a meaning of query-attribute of SimpleDB.
 * the implementations will express a expression for attribute of SimpleDB like every('attribute-name').
 * <p>
 * All attributes of a query must be an implementation of Attribute,
 * if it's a simple single attribute's name without any expression. the most simple attribute will be expressed by
 * DefaultAttribute class.
 *
 * @author Tsutomu YANO
 */
public interface QueryAttribute extends Describable {
}
