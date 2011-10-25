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
import java.util.*;
import static java.util.Arrays.asList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * THIS CLASS IS THREAD SAFE
 *
 * @author Tsutomu YANO
 */
public class DefaultContext implements Context {
    private static final long serialVersionUID = 1L;

    //TODO hey! AWSCredentials must be serializable! or must be transient!
    private final AWSCredentials credentials;
    private final Set<Object> putObjects = new LinkedHashSet<Object>();;
    private final Set<Object> deleteObjects = new LinkedHashSet<Object>();

    private final ReentrantReadWriteLock putObjectRwl = new ReentrantReadWriteLock();
    private final Lock putObjectReadLock = putObjectRwl.readLock();
    private final Lock putObjectWriteLock = putObjectRwl.writeLock();

    private final ReentrantReadWriteLock deleteObjectRwl = new ReentrantReadWriteLock();
    private final Lock deleteObjectReadLock = deleteObjectRwl.readLock();
    private final Lock deleteObjectWriteLock = deleteObjectRwl.writeLock();

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
    public void putObjects(Object... domainObjects) {
        putObjectWriteLock.lock();
        try {
            putObjects.addAll(asList(domainObjects));
        } finally {
            putObjectWriteLock.unlock();
        }
    }

    @Override
    public Set<Object> getPutObjects() {
        putObjectReadLock.lock();
        try {
            return new LinkedHashSet<Object>(this.putObjects);
        } finally {
            putObjectReadLock.unlock();
        }
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
    public void deleteObjects(Object... domainObjects) {
        deleteObjectWriteLock.lock();
        try {
            deleteObjects.addAll(asList(domainObjects));
        } finally {
            deleteObjectWriteLock.unlock();
        }
    }

    @Override
    public Set<Object> getDeleteObjects() {
        deleteObjectReadLock.lock();
        try {
            return new LinkedHashSet<Object>(this.deleteObjects);
        } finally {
            deleteObjectReadLock.unlock();
        }
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
        //we must reading and writing putObjects and deleteObjects as atomic processing
        //from reading putObjects and deleteObjects until clear the two collections,
        //because if other thread writing data into the to collections and then we
        //got writeLock and clear the collections, the wrote data written by other thread lost.
        //So we must get WRITE locks at first.

        //TODO I believe we can change this implementation more efficient. ex) putting and deleting data wtih some threads, and wait until all threads end.
        putObjectWriteLock.lock();
        deleteObjectWriteLock.lock();
        try {
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

            //all objects are processed successfully, then clear all objects from caches.
            deleteObjects.clear();
            putObjects.clear();
        } finally {
            putObjectWriteLock.unlock();
            deleteObjectWriteLock.unlock();
        }
    }
}
