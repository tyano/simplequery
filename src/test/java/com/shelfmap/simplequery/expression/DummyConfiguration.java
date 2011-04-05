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

package com.shelfmap.simplequery.expression;

import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.InstanceFactory;
import com.shelfmap.simplequery.expression.impl.BeanDomainAttributes;

/**
 *
 * @author Tsutomu YANO
 */
public class DummyConfiguration implements Configuration {
    @Override
    public <T> ItemConverter<T> getItemConverter(Class<T> domainClass, String domainName) {
        return null;
    }

    @Override
    public <T> InstanceFactory<T> getInstanceFactory(Class<T> domainClass, String domainName) {
        return null;
    }

    @Override
    public DomainAttributes getDomainAttributes(Class<?> domainClass, String domainName) {
        return new BeanDomainAttributes(domainClass, domainName);
    }
}
