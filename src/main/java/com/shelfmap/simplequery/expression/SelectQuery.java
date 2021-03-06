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

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import java.util.Collection;

/**
 *
 * @author Tsutomu YANO
 */
public interface SelectQuery extends Describable {
    
    void addAttributes(SelectAttribute... attribute);
    SelectQuery withAttributes(SelectAttribute... attribute);
    Collection<SelectAttribute> getAttributes();
    
    /**
     * You can create a Expression object which returns all items of a domain.
     * the domain will be detected from the target
     * object that is passed as a argument. the target object must have @Target(domainName)
     * annotation on the class definition. If the target don't have @Target 
     * annotation, InvalidTargetException will be throwed.
     * 
     * @param <T> a type of results.
     * @param target a class for returned instances.
     * @return An expression object which will retrieve all records of the domain specified by the {@code target} parameter.
     */
    <T> DomainExpression<T> from(Class<T> target);
    Context getContext();
}
