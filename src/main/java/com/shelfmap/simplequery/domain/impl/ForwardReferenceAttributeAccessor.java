/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery.domain.impl;

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.domain.AttributeAccessor;
import com.shelfmap.simplequery.domain.ForwardReference;

/**
 *
 * @author Tsutomu YANO
 */
public class ForwardReferenceAttributeAccessor implements AttributeAccessor<String> {
    private PropertyAttributeAccessor<ForwardReference> propertyAccessor;
    private String propertyPath;

    public ForwardReferenceAttributeAccessor(Context context, String propertyPath) {
        super();
        this.propertyPath = propertyPath;
        this.propertyAccessor = new PropertyAttributeAccessor<ForwardReference>(context, propertyPath);
    }

    @Override
    public String read(Object instance) {
        ForwardReference reference = propertyAccessor.read(instance);
        return reference.getTargetItemName();
    }

    @Override
    public void write(Object instance, String value) {
        ForwardReference reference = propertyAccessor.read(instance);
        reference.setTargetItemName(value);
    }

}
