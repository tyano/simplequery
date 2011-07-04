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

import com.shelfmap.simplequery.annotation.Domain;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import static java.util.Arrays.asList;
import static com.shelfmap.simplequery.SimpleDbUtil.*;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;


/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/ListProperty.story")
public class ListPropertyTest extends BaseStoryRunner {
    private static final String DOMAIN_NAME = "list-property";
    
    @Inject
    TestContext ctx;
    
    @Override
    protected void configureTestContext(Binder binder) {
        binder.bind(IClientHolder.class).to(TestContext.class).in(Scopes.SINGLETON);
        binder.bind(TestContext.class).in(Scopes.SINGLETON);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<? extends Class<?>> getStepsClasses() {
        return asList(ClientFactory.class);
    }

    @Given("a initialized domain which have a multi-value column")
    public void createDomains() {
        AmazonSimpleDB simpleDb = ctx.getSimpleDb();
        List<ReplaceableItem> items = asList(
                     item("first", attr("tag", "red", true)),
                     item("first", attr("tag", "blue", false))
                );
        
        simpleDb.deleteDomain(new DeleteDomainRequest(DOMAIN_NAME));
        simpleDb.createDomain(new CreateDomainRequest(DOMAIN_NAME));
        
        simpleDb.batchPutAttributes(new BatchPutAttributesRequest(DOMAIN_NAME, items));
    }

    @Pending
    @When("selecting an item from the domain which have a multi-value column")
    public void selectItemsWithMultiValues() {
    }

    @Pending
    @Then("we can get the values through a property whose type is a kind of Collection")
    public void assertListProperty() {
    }

    @Pending
    @When("selecting an item from the same domain, but the properties type is not a kind of Collection")
    public void selectItemBySingleObjectProperty() {
    }

    @Pending
    @Then("we should get a first value of the multi-value column")
    public void assertItMustBeAFirstValue() {
    }

    @Pending
    @When("the value of a multi-value column is null and the type of the property associated with the column is a kind of Collection")
    public void selectEmptyMultiValueColumnByList() {
    }

    @Pending
    @Then("the return value must be a empty collection")
    public void assertTheResultIsEmptyCollection() {
    }

    @Pending
    @When("the value of a multi-value column is null and the type of the property associated with the column is not a Collection")
    public void selectEmptyMultiValueColumnBySingleObjectProperty() {
    }

    @Pending
    @Then("the return value must be a null")
    public void assertItsResultMustBeANullValue() {
    }
    
    @Domain(value=DOMAIN_NAME)
    private static class ListPropertyDomain {
        private String itemName;
        private List<String> tags;
        
        private final Object tagMonitor = new Object();

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }
        
        public List<String> getTags() {
            synchronized(tagMonitor) {
                return new ArrayList<String>(tags);
            }
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
        
        public void addTag(String tag) {
            createInnerList();
            tags.add(tag);
        }
        
        public void addTags(Collection<String> tags) {
            createInnerList();
            synchronized(tagMonitor) {
                tags.addAll(tags);
            }
        }

        private void createInnerList() {
            synchronized(tagMonitor) {
                if(tags == null) {
                    tags = new ArrayList<String>();
                }
            }
        }
    }
}
