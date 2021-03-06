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

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import java.io.Serializable;

/**
 * Simple implementation of QueryAttribute, which holds a name of the attribute
 * and describes the attribute as a quoted string of the name.
 *
 * @author Tsutomu YANO
 */
public class DefaultAttribute implements SelectAttribute, ConditionAttribute, Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * @param name name of attribute
     */
    public DefaultAttribute(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String describe() {
        return SimpleDBUtils.quoteName(getAttributeName());
    }

    /**
     * @return name of this attribute, not quoted.
     */
    @Override
    public String getAttributeName() {
        return name;
    }
}
