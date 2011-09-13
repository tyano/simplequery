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
package com.shelfmap.simplequery.domain.impl;

import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;

/**
 *
 * @author Tsutomu YANO
 */
public class SimpleNamedDomain implements Domain {

    private final String domainName;

    public SimpleNamedDomain(String domainName) {
        isNotNull("domainName", domainName);
        this.domainName = domainName;
    }

    public SimpleNamedDomain(Class<?> domainClass) {
        isNotNull("domainClass", domainClass);

        if(!domainClass.isAnnotationPresent(SimpleDbDomain.class)) {
            throw new IllegalArgumentException("domainClass must have a @SimpleDbDomain annotation.");
        }

        SimpleDbDomain domainAnnotation = domainClass.getAnnotation(SimpleDbDomain.class);
        String value = domainAnnotation.value();
        if(value.isEmpty()) {
            value = domainClass.getSimpleName();
        }

        this.domainName = value;
    }

    @Override
    public String getDomainName() {
        return this.domainName;
    }
}
