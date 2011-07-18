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

import static java.lang.String.format;

/**
 * an implementation of QueryAttribute.
 * this class describes 'every(attrbuteName)' expression of Amazon SimpleDB.
 *
 * @author Tsutomu YANO
 */
public class EveryAttribute extends DefaultAttribute {

    /**
     * @param name name of attribute
     */
    public EveryAttribute(String name) {
        super(name);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String describe() {
        return format("every(%s)", super.describe());
    }
}
