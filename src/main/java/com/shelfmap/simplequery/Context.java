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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.shelfmap.simplequery.domain.AttributeConverterFactory;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.factory.DomainDescriptorFactory;
import com.shelfmap.simplequery.factory.ItemConverterFactory;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Tsutomu YANO
 */
public interface Context extends Serializable {
    ItemConverterFactory getItemConverterFactory();
    <T> DomainInstanceFactory<T> getDomainInstanceFactory(Domain<T> domain);
    DomainFactory getDomainFactory();
    DomainDescriptorFactory getDomainDescriptorFactory();
    AttributeConverterFactory getAttributeConverterFactory();
    AWSCredentials getCredentials();

    AmazonSimpleDB getSimpleDB();
    AmazonS3 getS3();
    SelectQuery select(SelectAttribute... attribute);

    void putObjectImmediately(Object domainObject) throws AmazonServiceException, AmazonClientException;
    void putObjects(Object... domainObjects);
    Set<Object> getPutObjects();

    void deleteObjectImmediately(Object domainObject) throws AmazonServiceException, AmazonClientException;
    void deleteItem(Domain<?> domain, String itemName) throws AmazonServiceException, AmazonClientException;
    void deleteObjects(Object... domainObjects);
    Set<Object> getDeleteObjects();

    void save() throws AmazonServiceException, AmazonClientException;
}
