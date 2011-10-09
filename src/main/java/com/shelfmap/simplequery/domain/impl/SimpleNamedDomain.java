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

import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import com.shelfmap.simplequery.domain.Domain;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.util.Objects;
import java.util.Collection;

/**
 *
 * @param <T> the type of domain-class.
 * @author Tsutomu YANO
 */
public class SimpleNamedDomain<T> implements Domain<T> {

    private final Class<T> domainClass;
    private final String domainName;

    public static <X> SimpleNamedDomain<X> of(final Class<X> domainClass) {
        return new SimpleNamedDomain<X>(domainClass);
    }
    
    public static SimpleNamedDomain<?> find(final Class<?> domainClass) {
        Collection<Class<?>> linearized = Objects.linearlize(domainClass);
        for (Class<?> clazz : linearized) {
            if(clazz.isAnnotationPresent(SimpleDbDomain.class)) {
                return newDomain(clazz);
            }
        }
        throw new IllegalArgumentException("domainClass must have a @SimpleDbDomain annotation.");
    }
    
    private static <C> SimpleNamedDomain<C> newDomain(Class<C> clazz) {
        return new SimpleNamedDomain<C>(clazz);
    }

    public SimpleNamedDomain(final Class<T> domainClass) {
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
        this.domainClass = domainClass;
    }

    @Override
    public String getDomainName() {
        return this.domainName;
    }

    @Override
    public Class<T> getDomainClass() {
        return this.domainClass;
    }
}
