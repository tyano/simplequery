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
package com.shelfmap.simplequery.attribute.impl;

import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.attribute.SelectAttribute;

/**
 *
 * @author Tsutomu YANO
 */
public class ItemNameAttribute implements SelectAttribute, ConditionAttribute {
    public static final ItemNameAttribute INSTANCE = new ItemNameAttribute();
    
    private ItemNameAttribute() {
        super();
    }

    @Override
    public String describe() {
        return "itemName()";
    }

    @Override
    public String getAttributeName() {
        return "itemName()";
    }
}