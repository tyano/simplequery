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

import com.shelfmap.simplequery.attribute.impl.DefaultAttribute;
import com.shelfmap.simplequery.attribute.impl.EveryAttribute;
import com.shelfmap.simplequery.attribute.impl.ItemNameAttribute;

/**
 * Utility class for creating instance of QueryAttribute.
 *
 * @author Tsutomu YANO
 */
public class Attributes {

    private Attributes() {
    }

    /**
     * Utility method for creating a DefaultAttribute.
     * @param name name of an attribute
     * @return an instance of DefaultAttribute
     */
    public static DefaultAttribute attr(String name) {
        return new DefaultAttribute(name);
    }

    /**
     * Utility method for creating a EveryAttribute.
     * @param name name of an attribute
     * @return an instance of EveryAttribute
     */
    public static EveryAttribute every(String name) {
        return new EveryAttribute(name);
    }

    public static ItemNameAttribute itemName() {
        return ItemNameAttribute.INSTANCE;
    }
}
