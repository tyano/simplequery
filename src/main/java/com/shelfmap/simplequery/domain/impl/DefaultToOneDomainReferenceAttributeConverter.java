/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.shelfmap.simplequery.domain.impl;

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultToOneDomainReferenceAttributeConverter implements AttributeConverter<DefaultToOneDomainReference<?>> {

    private Context context;
    private Class<?> targetClass;

    public DefaultToOneDomainReferenceAttributeConverter(Context context, Class<?> targetClass) {
        this.context = context;
        this.targetClass = targetClass;
    }

    @Override
    public String convertValue(DefaultToOneDomainReference<?> targetValue) {
        return targetValue.getTargetItemName();
    }

    @Override
    public DefaultToOneDomainReference<?> restoreValue(String targetValue) throws CanNotRestoreAttributeException {
        DomainFactory factory = context.getDomainFactory();
        Domain<?> targetDomain = factory.findDomain(targetClass);
        DefaultToOneDomainReference<?> reference = createDefaultToOneDomainReference(context, targetDomain);
        reference.setTargetItemName(targetValue);
        return reference;
    }
    
    private <D> DefaultToOneDomainReference<D> createDefaultToOneDomainReference(Context context, Domain<D> targetDomain) {
        return new DefaultToOneDomainReference<D>(context, targetDomain);
    }
}
