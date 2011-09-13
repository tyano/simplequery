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

import static com.shelfmap.simplequery.SimpleDbUtil.item;
import static com.shelfmap.simplequery.SimpleDbUtil.attr;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.ClientFactory;
import com.shelfmap.simplequery.IClientHolder;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.TestContext;
import com.shelfmap.simplequery.annotation.Attribute;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import com.shelfmap.simplequery.annotation.ItemName;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/ToOneRelation.story")
public class ToOneRelationTest extends BaseStoryRunner {

    @Override
    protected void configureTestContext(Binder binder) {
        binder.bind(IClientHolder.class).to(TestContext.class).in(Scopes.SINGLETON);
        binder.bind(TestContext.class).in(Scopes.SINGLETON);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<? extends Class<?>> getStepsClasses() {
        return Arrays.asList(ClientFactory.class);
    }

    @Inject
    TestContext ctx;

    private static final String PARENT_DOMAIN = "ToOneRelationTest-parent";
    private static final String CHILD_DOMAIN = "ToOneRelationTest-child";


    @Given("domains where one-side have a relationship to another one.")
    public void setupDomains() {
        AmazonSimpleDB simpleDb = ctx.getClient().getSimpleDB();

        simpleDb.deleteDomain(new DeleteDomainRequest(PARENT_DOMAIN));
        simpleDb.deleteDomain(new DeleteDomainRequest(CHILD_DOMAIN));

        simpleDb.createDomain(new CreateDomainRequest(PARENT_DOMAIN));
        simpleDb.createDomain(new CreateDomainRequest(CHILD_DOMAIN));

        ReplaceableItem child = item("child1",
                                     attr("name", "本", true)
                                    ,attr("amount", SimpleDBUtils.encodeZeroPadding(100, 4), true));

        simpleDb.putAttributes(new PutAttributesRequest(
                CHILD_DOMAIN,
                "child1",
                Arrays.asList(attr("name", "本", true),
                              attr("amount", SimpleDBUtils.encodeZeroPadding(100, 4), true))));

        simpleDb.putAttributes(new PutAttributesRequest(
                PARENT_DOMAIN,
                "parent",
                Arrays.asList(attr("requestDate", SimpleDBUtils.encodeDate(new Date(2011, Calendar.AUGUST, 2)), true),
                              attr("detail", "child1", true))));

    }



    @Given("an instance of a domain-object which have a relationship.")
    public void createDomainObject() {
    }

    @Then("we can get another domain-object from the relationship.")
    public void assertRelationship() {
    }


    @SimpleDbDomain(PARENT_DOMAIN)
    public interface PurchaseRecord {
        @ItemName
        String getItemName();
        void setItemName(String itemName);

        Date getRequestDate();
        void setRequestDate(Date requestDate);

        ToOneDomainReference<Detail> getDetail();
        void setDetail(ToOneDomainReference<Detail> detail);
    }

    @SimpleDbDomain(CHILD_DOMAIN)
    public interface Detail {
        @ItemName
        String getItemName();
        void setItemName(String itemName);

        String getName();
        void setName(String name);

        int getAmount();
        void setAmount(int amount);
    }


    public static class DefaultPurchaseRecord implements PurchaseRecord {

        private String itemName;
        private Date requestDate;
        private ToOneDomainReference<Detail> detailReference;

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
            return this.requestDate;
        }

        @Override
        public void setRequestDate(Date requestDate) {
            this.requestDate = requestDate;
        }

        @Override
        public ToOneDomainReference<Detail> getDetail() {
            return this.detailReference;
        }

        @Override
        public void setDetail(ToOneDomainReference<Detail> detail) {
            this.detailReference = detail;
        }
    }

    public static class DefaultDetail implements Detail {
        private String itemName;
        private String name;
        private int amount;

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
    }
}
