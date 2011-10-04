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
package com.shelfmap.simplequery.domain;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import static com.shelfmap.simplequery.SimpleDbUtil.attr;
import com.shelfmap.simplequery.*;
import com.shelfmap.simplequery.annotation.ForwardDomainReference;
import com.shelfmap.simplequery.annotation.IntAttribute;
import com.shelfmap.simplequery.annotation.ItemName;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import static com.shelfmap.simplequery.attribute.Attributes.attr;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.domain.impl.DefaultReverseToManyDomainReference;
import com.shelfmap.simplequery.domain.impl.DefaultToOneDomainReference;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.notNullValue;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/ToOneRelation.story")
public class ToOneRelationTest extends BaseStoryRunner {

    private static final String PARENT_DOMAIN = "ToOneRelationTest-parent";
    private static final String CHILD_DOMAIN = "ToOneRelationTest-child";

    private Context context;
    private Date targetDate = new Date(2011, Calendar.AUGUST, 2);

    @Given("a test-specific context")
    public void createContext() throws IOException {
        context = new DefaultContext(new PropertiesCredentials(new File(ClientFactory.CREDENTIAL_PATH))) {
            private static final long serialVersionUID = 1L;

            @Override
            @SuppressWarnings("unchecked")
            public <T> InstanceFactory<T> getInstanceFactory(Domain<T> domain) {
                Class<T> domainClass = domain.getDomainClass();
                
                if(Detail.class.isAssignableFrom(domainClass)) {
                    return (InstanceFactory<T>) new DetailInstanceFactory(this);
                }
                
                if(PurchaseRecord.class.isAssignableFrom(domainClass)) {
                    return (InstanceFactory<T>) new ParentInstanceFactory(this);
                }
                return super.getInstanceFactory(domain);
            }
            
        };
    }
    
    @Given("domains where one-side have a relationship to another one.")
    public void setupDomains() {
        AmazonSimpleDB simpleDb = context.createNewClient().getSimpleDB();

        simpleDb.deleteDomain(new DeleteDomainRequest(PARENT_DOMAIN));
        simpleDb.deleteDomain(new DeleteDomainRequest(CHILD_DOMAIN));

        simpleDb.createDomain(new CreateDomainRequest(PARENT_DOMAIN));
        simpleDb.createDomain(new CreateDomainRequest(CHILD_DOMAIN));

        simpleDb.putAttributes(new PutAttributesRequest(
                CHILD_DOMAIN,
                "child1",
                Arrays.asList(attr("name", "本", true),
                              attr("amount", SimpleDBUtils.encodeZeroPadding(100, 4), true),
                              attr("parentItemName", "parent", true))));

        simpleDb.putAttributes(new PutAttributesRequest(
                PARENT_DOMAIN,
                "parent",
                Arrays.asList(attr("requestDate", SimpleDBUtils.encodeDate(targetDate), true),
                              attr("detail", "child1", true))));

    }

    private Detail detailObject;
    
    @Given("an instance of a domain-object which have a relationship.")
    public void createDomainObject() throws SimpleQueryException, MultipleResultsExistException {
        Client client = context.createNewClient();
        detailObject = client.select().from(Detail.class).whereItemName(is("child1")).getSingleResult(true);
    }

    @Then("we can get another domain-object from the relationship.")
    public void assertRelationship() throws SimpleQueryException, MultipleResultsExistException {
        assertThat(detailObject, Matchers.is(notNullValue()));
        PurchaseRecord parent = detailObject.getParentRecordReference().get(true);
        assertThat(parent, Matchers.is(notNullValue()));
        assertThat(parent.getItemName(), Matchers.is("parent"));
        assertThat(parent.getRequestDate(), Matchers.is(targetDate));
    }


    @SimpleDbDomain(PARENT_DOMAIN)
    public interface PurchaseRecord {
        @ItemName
        String getItemName();
        void setItemName(String itemName);

        Date getRequestDate();
        void setRequestDate(Date requestDate);

        ReverseToManyDomainReference<Detail> getDetailReference();
    }

    @SimpleDbDomain(CHILD_DOMAIN)
    public interface Detail {
        @ItemName
        String getItemName();
        void setItemName(String itemName);

        String getName();
        void setName(String name);

        @IntAttribute(padding=5)
        int getAmount();
        void setAmount(int amount);

        @ForwardDomainReference(attributeName = "parentItemName", targetDomainClass=PurchaseRecord.class)
        ToOneDomainReference<PurchaseRecord> getParentRecordReference();
        void setParentRecordReference(ToOneDomainReference<PurchaseRecord> reference);
        
    }


    public static class DefaultPurchaseRecord implements PurchaseRecord {

        private String itemName;
        private Date requestDate;
        private final ReverseToManyDomainReference<Detail> detailReference;

        public DefaultPurchaseRecord(Context context) {
            this(context, null, null);
        }
        
        public DefaultPurchaseRecord(Context context, String itemName, Date requestDate) {
            super();
            this.itemName = itemName;
            this.requestDate = requestDate == null ? null : new Date(requestDate.getTime());
            DomainFactory factory = context.getDomainFactory();
            Domain<Detail> detailDomain = factory.createDomain(Detail.class);
            ConditionAttribute targetAttribute = attr("parentItemName");
            this.detailReference = new DefaultReverseToManyDomainReference<Detail>(context, itemName, detailDomain, targetAttribute);
        }

        @Override
        public String getItemName() {
            return this.itemName;
        }

        @Override
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public Date getRequestDate() {
            return new Date(this.requestDate.getTime());
        }

        @Override
        public void setRequestDate(Date requestDate) {
            this.requestDate = new Date(requestDate.getTime());
        }

        @Override
        public ReverseToManyDomainReference<Detail> getDetailReference() {
            return this.detailReference;
        }
    }

    public static class DefaultDetail implements Detail {
        private String itemName;
        private String name;
        private int amount;
        private ToOneDomainReference<PurchaseRecord> parentReference;

        public DefaultDetail(Context context) {
            this(context, null, null, 0);
        }
        
        public DefaultDetail(Context context, String itemName, String name, int amount) {
            super();
            this.itemName = itemName;
            this.name = name;
            this.amount = amount;
            Domain<PurchaseRecord> domain = context.getDomainFactory().createDomain(PurchaseRecord.class);
            this.parentReference = new DefaultToOneDomainReference<PurchaseRecord>(context, domain);
        }

        @Override
        public String getItemName() {
            return this.itemName;
        }

        @Override
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int getAmount() {
            return this.amount;
        }

        @Override
        public void setAmount(int amount) {
            this.amount = amount;
        }

        @Override
        public ToOneDomainReference<PurchaseRecord> getParentRecordReference() {
            return this.parentReference;
        }

        @Override
        public void setParentRecordReference(ToOneDomainReference<PurchaseRecord> reference) {
            this.parentReference = reference;
        }
    }
    
    public static class DetailInstanceFactory implements InstanceFactory<Detail> {
        private Context context;

        public DetailInstanceFactory(Context context) {
            this.context = context;
        }
        
        @Override
        public Detail createInstance(Class<Detail> clazz) {
            return new DefaultDetail(context);
        }
    }
    
    public static class ParentInstanceFactory implements InstanceFactory<PurchaseRecord> {
        private Context context;

        public ParentInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public PurchaseRecord createInstance(Class<PurchaseRecord> clazz) {
            return new DefaultPurchaseRecord(context);
        }
    }
}
