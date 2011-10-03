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
package com.shelfmap.simplequery;

import com.amazonaws.auth.AWSCredentials;
import com.shelfmap.simplequery.domain.AttributeConverterFactory;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainAttributes;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.expression.ItemConverter;
import java.io.Serializable;

/**
 *
 * @author Tsutomu YANO
 */
public interface Context extends Serializable {
    Client createNewClient();
     <T> ItemConverter<T> getItemConverter(Domain<T> domain);
     <T> InstanceFactory<T> getInstanceFactory(Domain<T> domain);
    DomainFactory getDomainFactory();
    DomainAttributes getDomainAttributes(Domain<?> domain);
    AttributeConverterFactory getAttributeConverterFactory();
    AWSCredentials getCredentials();
}