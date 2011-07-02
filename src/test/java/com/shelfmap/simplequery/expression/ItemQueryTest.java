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

import static java.util.Arrays.asList;
import static com.shelfmap.simplequery.SimpleDbUtil.*;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.Attribute;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.ClientFactory;
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.IClientHolder;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.TestContext;
import java.util.Arrays;
import java.util.List;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/ItemQuery.story")
public class ItemQueryTest extends BaseStoryRunner {
    private static final String DOMAIN_NAME = "item-test-domain";
    
    @Inject
    private TestContext ctx;
    
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

    @Given("a initialized test domain")
    public void initializeTestDomain() {
        AmazonSimpleDB simpleDb = ctx.getSimpleDb();
        simpleDb.deleteDomain(new DeleteDomainRequest(DOMAIN_NAME));
        simpleDb.createDomain(new CreateDomainRequest(DOMAIN_NAME));
        
        List<ReplaceableItem> items = asList(
                    item("firstItem", attr("sample-value", "this is sample.", true)));
        
        simpleDb.batchPutAttributes(new BatchPutAttributesRequest(DOMAIN_NAME, items));
    }

    @When("querying with the item name from a test domain")
    public void selectByItemName() {
    }

    @Then("we can get the only one record from the test domain")
    public void assertResultItem() {
    }

    @When("there is no record matching the specified item's name")
    public void selectNonExistingItem() {
    }

    @Then("the return value must be a null")
    public void assertResultIsNull() {
    }
    
    @Domain(DOMAIN_NAME)
    private static class ItemTestDomain {
        private String itemName;
        private String sampleValue;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Attribute(attributeName="sample-value")
        public String getSampleValue() {
            return sampleValue;
        }

        public void setSampleValue(String sampleValue) {
            this.sampleValue = sampleValue;
        }
    }
}
