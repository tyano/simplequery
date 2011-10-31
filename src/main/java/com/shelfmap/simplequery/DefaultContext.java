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
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.*;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.domain.impl.DefaultDomainFactory;
import com.shelfmap.simplequery.expression.ItemConverter;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.impl.Select;
import com.shelfmap.simplequery.factory.DomainDescriptorFactory;
import com.shelfmap.simplequery.factory.ItemConverterFactory;
import com.shelfmap.simplequery.factory.impl.DefaultDomainDescriptorFactory;
import com.shelfmap.simplequery.factory.impl.DefaultItemConverterFactory;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * Order of lock-aquisition:<br>
 * all method which need have more than 1 lock must follow the following locking order.
 * <ol>
 * <li>cachedObjects' Lock
 * <li>putObjects' Lock
 * <li>deleteObjects' Lock
 * </ol>
 *
 * THIS CLASS IS THREAD SAFE
 *
 * @author Tsutomu YANO
 */
public class DefaultContext implements Context {
    private static final long serialVersionUID = 1L;

    //TODO hey! AWSCredentials must be serializable! or must be transient!
    private final AWSCredentials credentials;

    private AmazonSimpleDB simpleDB;
    private final Lock simpleDBLock = new ReentrantLock();

    private AmazonS3 s3;
    private final Lock s3Lock = new ReentrantLock();

    private final Deque<CachedObject> cachedObjects = new ArrayDeque<CachedObject>();

    private final ReentrantReadWriteLock cachedObjectRwl = new ReentrantReadWriteLock();
    private final Lock cachedObjectReadLock = cachedObjectRwl.readLock();
    private final Lock cachedObjectWriteLock = cachedObjectRwl.writeLock();


    public DefaultContext(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public ItemConverterFactory getItemConverterFactory() {
        return new DefaultItemConverterFactory(this);
    }

    @Override
    public <T> DomainInstanceFactory<T> getDomainInstanceFactory(Domain<T> domain) {
        return new DefaultInstanceFactory<T>(this, domain);
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
    public AmazonSimpleDB getSimpleDB() {
        simpleDBLock.lock();
        try {
            if(this.simpleDB == null) {
                this.simpleDB = createSimpleDb(getCredentials());
            }
            return this.simpleDB;
        } finally {
            simpleDBLock.unlock();
        }
    }

    @Override
    public SelectQuery select(SelectAttribute... attributes) {
        return newSelectQuery(attributes);
    }

    protected SelectQuery newSelectQuery(SelectAttribute... attributes) {
        return new Select(this, attributes);
    }

    @Override
    public AmazonS3 getS3() {
        s3Lock.lock();
        try {
            if(this.s3 == null) {
                this.s3 = createS3(getCredentials());
            };
            return this.s3;
        } finally {
            s3Lock.unlock();
        }
    }

    protected AmazonSimpleDB createSimpleDb(AWSCredentials securityCredential) {
        ClientConfiguration clientConfig = configureSimpleDb();
        return clientConfig == null
                ? new AmazonSimpleDBClient(securityCredential)
                : new AmazonSimpleDBClient(securityCredential, clientConfig);
    }

    protected ClientConfiguration configureSimpleDb() {
        return null;
    }

    protected AmazonS3 createS3(AWSCredentials securityCredential) {
        ClientConfiguration clientConfig = configureS3();
        return clientConfig == null
                ? new AmazonS3Client(securityCredential)
                : new AmazonS3Client(securityCredential, clientConfig);
    }

    protected ClientConfiguration configureS3() {
        return null;
    }


    private Collection<CachedObject> asCachedUpdateObjects(Object... domainObjects) {
        List<CachedObject> cachedList = new ArrayList<CachedObject>();
        for (Object object : domainObjects) {
            cachedList.add(new UpdateObject(object));
        }
        return cachedList;
    }

    private Collection<CachedObject> asCachedDeleteObjects(Object... domainObjects) {
        List<CachedObject> cachedList = new ArrayList<CachedObject>();
        for (Object object : domainObjects) {
            cachedList.add(new DeleteObject(object));
        }
        return cachedList;
    }

    @Override
    public void putObjects(Object... domainObjects) {
        cachedObjectWriteLock.lock();
        try {
            cachedObjects.addAll(asCachedUpdateObjects(domainObjects));
        } finally {
            cachedObjectWriteLock.unlock();
        }
    }

    @Override
    public LinkedHashSet<Object> getPutObjects() {
        cachedObjectReadLock.lock();
        try {
            LinkedHashSet<Object> set = new LinkedHashSet<Object>();
            for (CachedObject cached : cachedObjects) {
                if(cached.getObjectType() == ObjectType.PUT) {
                    set.add(cached.getObject());
                }
            }
            return set;
        } finally {
            cachedObjectReadLock.unlock();
        }
    }

    @Override
    public void putObjectImmediately(final Object domainObject) throws AmazonServiceException, AmazonClientException {
        Domain<?> domain = getDomainFactory().findDomain(domainObject.getClass());
        DomainDescriptor descriptor = getDomainDescriptorFactory().create(domain);
        String itemName = descriptor.getItemNameAttribute().getAttributeAccessor().read(domainObject);

        ItemConverter<?> itemConverter = getItemConverterFactory().create(domain);
        ItemState itemState = itemConverter.makeCurrentStateOf(domainObject);

        Collection<ReplaceableAttribute> changed = itemState.getChangedItems();
        Collection<Attribute> deleted = itemState.getDeletedItems();

        if(!changed.isEmpty()) {
            PutAttributesRequest request = new PutAttributesRequest()
                                                .withDomainName(domain.getDomainName())
                                                .withItemName(itemName)
                                                .withAttributes(changed);
            getSimpleDB().putAttributes(request);
        }

        if(!deleted.isEmpty()) {
            DeleteAttributesRequest request = new DeleteAttributesRequest()
                                                .withDomainName(domain.getDomainName())
                                                .withItemName(itemName)
                                                .withAttributes(deleted);
            getSimpleDB().deleteAttributes(request);
        }
    }

    @Override
    public void deleteObjects(Object... domainObjects) {
        cachedObjectWriteLock.lock();
        try {
            cachedObjects.addAll(asCachedDeleteObjects(domainObjects));
        } finally {
            cachedObjectWriteLock.unlock();
        }
    }

    @Override
    public LinkedHashSet<Object> getDeleteObjects() {
        cachedObjectReadLock.lock();
        try {
            LinkedHashSet<Object> set = new LinkedHashSet<Object>();
            for (CachedObject cached : cachedObjects) {
                if(cached.getObjectType() == ObjectType.DELETE) {
                    set.add(cached.getObject());
                }
            }
            return set;
        } finally {
            cachedObjectReadLock.unlock();
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
        DeleteAttributesRequest request = new DeleteAttributesRequest(domain.getDomainName(), itemName);
        getSimpleDB().deleteAttributes(request);
    }

    @Override
    public void save() throws AmazonServiceException, AmazonClientException {
        //we must reading and writing putObjects and deleteObjects as atomic processing
        //from reading putObjects and deleteObjects until clear the two collections,
        //because if other thread writing data into the to collections and then we
        //got writeLock and clear the collections, the wrote data written by other thread lost.
        //So we must get WRITE locks at first.

        //TODO I believe we can change this implementation more efficient. ex) putting and deleting data wtih some threads, and wait until all threads end.
        cachedObjectWriteLock.lock();
        try {
            if(cachedObjects.isEmpty()) return;

            Map<Domain<?>, List<DeletableItem>> deleteItems = new HashMap<Domain<?>, List<DeletableItem>>();
            Map<Domain<?>, List<ReplaceableItem>> putItems = new HashMap<Domain<?>, List<ReplaceableItem>>();

            int handlingCount = 0;
            ObjectType prevType = null;
            for (CachedObject cached : new ArrayList<CachedObject>(cachedObjects)) {
                ObjectType currentType = cached.getObjectType();
                Object object = cached.getObject();

                if(prevType != null && prevType != currentType) {
                    switch(prevType) {
                        case PUT:
                            doPutObjects(putItems);
                            break;
                        case DELETE:
                            doDeleteObjects(deleteItems);
                            break;
                        default:
                            throw new IllegalStateException("No such objecType: " + currentType);
                    }

                    for(int i = 0; i < handlingCount; i++) {
                        cachedObjects.removeFirst();
                    }
                    handlingCount = 0;
                }

                switch(currentType) {
                    case PUT:
                        handlePutObject(object, putItems, deleteItems);
                        break;
                    case DELETE:
                        handleDeleteObject(object, deleteItems);
                        break;
                    default:
                        throw new IllegalStateException("No such objecType: " + currentType);
                }
                handlingCount++;
                prevType = currentType;
            }

            //handle all remaining objects
            doPutObjects(putItems);
            doDeleteObjects(deleteItems);

            //all objects are processed successfully, then clear all objects from caches.
            cachedObjects.clear();
        } finally {
            cachedObjectWriteLock.unlock();
        }
    }

    private void doPutObjects(Map<Domain<?>, List<ReplaceableItem>> putItems) throws AmazonClientException {
        AmazonSimpleDB sdb = getSimpleDB();
        for (Map.Entry<Domain<?>, List<ReplaceableItem>> entry : putItems.entrySet()) {
            Domain<?> domain = entry.getKey();
            List<ReplaceableItem> items = entry.getValue();
            BatchPutAttributesRequest request = new BatchPutAttributesRequest(domain.getDomainName(), items);
            sdb.batchPutAttributes(request);
        }
        putItems.clear();
    }

    private void doDeleteObjects(Map<Domain<?>, List<DeletableItem>> deleteItems) throws AmazonClientException {
        AmazonSimpleDB sdb = getSimpleDB();
        for (Map.Entry<Domain<?>, List<DeletableItem>> entry : deleteItems.entrySet()) {
            Domain<?> domain = entry.getKey();
            List<DeletableItem> items = entry.getValue();
            BatchDeleteAttributesRequest request = new BatchDeleteAttributesRequest(domain.getDomainName(), items);
            sdb.batchDeleteAttributes(request);
        }
        deleteItems.clear();
    }

    private void handleDeleteObject(Object object, Map<Domain<?>, List<DeletableItem>> deleteItems) {
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

    private void handlePutObject(Object object, Map<Domain<?>, List<ReplaceableItem>> putItems, Map<Domain<?>, List<DeletableItem>> deleteItems) {
        Domain<?> domain = getDomainFactory().findDomain(object.getClass());
        DomainDescriptor descriptor = getDomainDescriptorFactory().create(domain);
        String itemName = descriptor.getItemNameAttribute().getAttributeAccessor().read(object);

        ItemConverter<?> itemConverter = getItemConverterFactory().create(domain);
        ItemState itemState = itemConverter.makeCurrentStateOf(object);
        Collection<ReplaceableAttribute> changed = itemState.getChangedItems();
        Collection<Attribute> deleted = itemState.getDeletedItems();

        if(!changed.isEmpty()) {
            List<ReplaceableItem> list = putItems.get(domain);
            if(list == null) {
                list = new ArrayList<ReplaceableItem>();
                putItems.put(domain, list);
            }
            list.add(new ReplaceableItem().withName(itemName).withAttributes(changed));
        }

        if(!deleted.isEmpty()){
            List<DeletableItem> list = deleteItems.get(domain);
            if(list == null) {
                list = new ArrayList<DeletableItem>();
                deleteItems.put(domain, list);
            }
            list.add(new DeletableItem().withName(itemName).withAttributes(deleted));
        }
    }

    private enum ObjectType {
        DELETE, PUT;
    }

    private static interface CachedObject {
        Object getObject();
        ObjectType getObjectType();
    }

    private static abstract class BaseCachedObject implements CachedObject {
        private Object object;

        public BaseCachedObject(Object object) {
            this.object = object;
        }

        @Override
        public Object getObject() {
            return this.object;
        }
    }

    private static class DeleteObject extends BaseCachedObject {
        public DeleteObject(Object object) {
            super(object);
        }

        @Override
        public ObjectType getObjectType() {
            return ObjectType.DELETE;
        }
    }

    private static class UpdateObject extends BaseCachedObject {
        public UpdateObject(Object object) {
            super(object);
        }

        @Override
        public ObjectType getObjectType() {
            return ObjectType.PUT;
        }
    }
}
