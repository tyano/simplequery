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

package com.shelfmap.simplequery.expression.matcher;

import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.Describable;
import java.util.Collection;

/**
 *
 * @author Tsutomu YANO
 */
public interface Matcher<T> extends Describable {
    /* properties */
    AttributeConverter<T> getAttributeInfo();
    void setAttributeInfo(AttributeConverter<T> attributeInfo);
    Collection<T> getValues();
    boolean isAttributeInfoApplied();
    
    /* methods for building new instance with new property value. */
    Matcher<T> withAttributeInfo(AttributeConverter<T> attributeInfo);
}
