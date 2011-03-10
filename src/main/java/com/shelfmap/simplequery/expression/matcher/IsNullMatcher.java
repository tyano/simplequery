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

import com.shelfmap.simplequery.expression.AttributeInfo;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.impl.NullAttributeInfo;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Tsutomu YANO
 */
public class IsNullMatcher implements Matcher<Void> {

    private final AttributeInfo<Void> attributeInfo = new NullAttributeInfo<Void>();
    
    public IsNullMatcher() {
    }

    @Override
    public Matcher<Void> withAttributeInfo(AttributeInfo<Void> attributeInfo) {
        return this;
    }

    @Override
    public String describe() {
        return "is null";
    }

    @Override
    public Collection<Void> getValues() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAttributeInfoApplied() {
        return true;
    }

    @Override
    public void setAttributeInfo(AttributeInfo<Void> attributeInfo) {
    }

    @Override
    public AttributeInfo<Void> getAttributeInfo() {
        return this.attributeInfo;
    }
}
