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
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.*;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.domain.impl.DefaultDomainFactory;
import com.shelfmap.simplequery.expression.ItemConverter;
import com.shelfmap.simplequery.factory.ClientFactory;
import com.shelfmap.simplequery.factory.DomainDescriptorFactory;
import com.shelfmap.simplequery.factory.ItemConverterFactory;
import com.shelfmap.simplequery.factory.impl.DefaultClientFactory;
import com.shelfmap.simplequery.factory.impl.DefaultDomainDescriptorFactory;
import com.shelfmap.simplequery.factory.impl.DefaultItemConverterFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultContext implements Context {
    private static final long serialVersionUID = 1L;

    private AWSCredentials credentials;
    private final Set<Object> putObjects = new LinkedHashSet<Object>();;
    private final Set<Object> deleteObjects = new LinkedHashSet<Object>();

    public DefaultContext(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public ClientFactory getClientFactory() {
        return new DefaultClientFactory(this);
    }

    @Override
    public ItemConverterFactory getItemConverterFactory() {
        return new DefaultItemConverterFactory(this);
    }

    @Override
    public <T> DomainInstanceFactory<T> getDomainInstanceFactory(Domain<T> domain) {
        return new DefaultInstanceFactory<T>();
    }

    @Override
    public DomainDescriptorFactory getDomainDescriptorFactory() {
        return new DefaultDomainDescriptorFactory(this);
    }

    @Override
    public AttributeConverterFactory getAttributeConverterFactory() {
        return new DefaultAttributeConverterFactory(this);
    }

    @Override
    public DomainFactory getDomainFactory() {
        return new DefaultDomainFactory();
    }

    @Override
    public AWSCredentials getCredentials() {
        return this.credentials;
    }

    @Override
    public void putObject(Object domainObject) {
        putObjects.add(domainObject);
    }

    @Override
    public void putObjectImmediately(final Object domainObject) throws AmazonServiceException, AmazonClientException {
        Domain<?> domain = getDomainFactory().findDomain(domainObject.getClass());
        ItemConverter<?> itemConverter = getItemConverterFactory().create(domain);
        ReplaceableItem item = itemConverter.convertToItem(domainObject);

        Client client = getClientFactory().create();
        PutAttributesRequest request = new PutAttributesRequest(domain.getDomainName(), item.getName(), item.getAttributes());
        client.getSimpleDB().putAttributes(request);
    }

    @Override
    public void deleteObject(Object domainObject) {
        deleteObjects.add(domainObject);
    }

    @Override
    public void deleteObjectImmediately(Object domainObject) throws AmazonServiceException, AmazonClientException {
        Domain<?> domain = getDomainFactory().findDomain(domainObject.getClass());
        DomainDescriptor descriptor = getDomainDescriptorFactory().create(domain);
        String itemName = descriptor.getItemNameAttribute().getAttributeAccessor().read(domainObject);
        deleteItem(domain, itemName);
    }

    @Override
    public void deleteItem(Domain<?> domain, String itemName) throws AmazonServiceException, AmazonClientException {
        Client client = getClientFactory().create();
        DeleteAttributesRequest request = new DeleteAttributesRequest(domain.getDomainName(), itemName);
        client.getSimpleDB().deleteAttributes(request);
    }

    @Override
    public void save() throws AmazonServiceException, AmazonClientException {
        if(putObjects.isEmpty() && deleteObjects.isEmpty()) return;

        Map<Domain<?>, List<DeletableItem>> deleteItems = new HashMap<Domain<?>, List<DeletableItem>>();
        Map<Domain<?>, List<ReplaceableItem>> putItems = new HashMap<Domain<?>, List<ReplaceableItem>>();

        for (Object object : deleteObjects) {
            Domain<?> domain = getDomainFactory().findDomain(object.getClass());
            DomainDescriptor descriptor = getDomainDescriptorFactory().create(domain);
            String itemName = descriptor.getItemNameAttribute().getAttributeAccessor().read(object);
            DeletableItem item = new DeletableItem().withName(itemName);

            List<DeletableItem> list = deleteItems.get(domain);
            if(list == null) {
                list = new ArrayList<DeletableItem>();
                deleteItems.put(domain, list);
            }
            list.add(item);
        }
        deleteObjects.clear();

        for (Object object : putObjects) {
            Domain<?> domain = getDomainFactory().findDomain(object.getClass());
            ItemConverter<?> itemConverter = getItemConverterFactory().create(domain);
            ReplaceableItem item = itemConverter.convertToItem(object);
            List<ReplaceableItem> list = putItems.get(domain);
            if(list == null) {
                list = new ArrayList<ReplaceableItem>();
                putItems.put(domain, list);
            }
            list.add(item);
        }
        putObjects.clear();


        AmazonSimpleDB simpleDB = getClientFactory().create().getSimpleDB();
        for (Domain<?> domain : deleteItems.keySet()) {
            List<DeletableItem> items = deleteItems.get(domain);
            BatchDeleteAttributesRequest request = new BatchDeleteAttributesRequest(domain.getDomainName(), items);
            simpleDB.batchDeleteAttributes(request);
        }

        for (Domain<?> domain : putItems.keySet()) {
            List<ReplaceableItem> items = putItems.get(domain);
            BatchPutAttributesRequest request = new BatchPutAttributesRequest(domain.getDomainName(), items);
            simpleDB.batchPutAttributes(request);
        }
    }
}
