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

package com.shelfmap.simplequery.expression.impl;

import com.shelfmap.simplequery.expression.DomainAttribute;

/**
 *
 * @author Tsutomu YANO
 */
public enum BasicAttribute implements DomainAttribute {
    ALL("*"), COUNT("count(*)");
    
    private String attributeName;

    private BasicAttribute(String attributeName) {
        this.attributeName = attributeName;
    }
    
    @Override
    public String getName() {
        return this.attributeName;
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    @Override
    public int getMaxDigitLeft() {
        return 0;
    }

    @Override
    public int getMaxDigitRight() {
        return 0;
    }

    @Override
    public long getOffset() {
        return 0;
    }
}
