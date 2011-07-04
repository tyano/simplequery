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

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;

import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.ClientFactory;
import com.shelfmap.simplequery.IClientHolder;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.TestContext;
import com.shelfmap.simplequery.annotation.Attribute;
import com.shelfmap.simplequery.annotation.Domain;
import com.shelfmap.simplequery.annotation.ItemName;
import com.shelfmap.simplequery.expression.impl.Select;
import com.shelfmap.simplequery.expression.matcher.MatcherFactory;

import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import static com.shelfmap.simplequery.SimpleDbUtil.*;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;

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

    ItemTestDomain result;
    
    @When("querying with the item name from a test domain")
    public void selectByItemName() throws SimpleQueryException, MultipleResultsExistException {
        result = ctx.getClient().select().from(ItemTestDomain.class).whereItemName(is("firstItem")).getSingleResult(true);
    }

    @Then("we can get the only one record from the test domain")
    public void assertResultItem() {
        assertThat(result, Matchers.is(notNullValue()));
        assertThat(result.getItemName(), Matchers.is("firstItem"));
        assertThat(result.getSampleValue(), Matchers.is("this is sample."));
    }

    @When("there is no record matching the specified item's name")
    public void selectNonExistingItem() throws SimpleQueryException, MultipleResultsExistException {
        result = ctx.getClient().select().from(ItemTestDomain.class).whereItemName(is("secondItem")).getSingleResult(true);
    }

    @Then("the return value must be a null")
    public void assertResultIsNull() {
        assertThat(result, Matchers.is(nullValue()));
    }
    
    private Expression<ItemTestDomain> exp;
    
    @When("the expression is created with 'is' matcher")
    public void createItemQueryWithIs() {
        exp = new Select(ctx.getSimpleDb(), ctx.getConfiguration(), "*").from(ItemTestDomain.class).whereItemName(is("firstItem"));
    }

    @Then("the result string must be -> $resultExp")
    public void assertExpression(String resultExp) {
        assertThat(exp.describe(), Matchers.is(resultExp));
    }

    @When("the expression is created with 'in' matcher")
    public void createItemQueryWithIn() {
        exp = new Select(ctx.getSimpleDb(), ctx.getConfiguration(), "*").from(ItemTestDomain.class).whereItemName(MatcherFactory.in("firstItem", "secondItem"));
    }

    @Domain(DOMAIN_NAME)
    public static class ItemTestDomain {

        private String itemName;
        private String sampleValue;

        @ItemName
        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Attribute(attributeName = "sample-value")
        public String getSampleValue() {
            return sampleValue;
        }

        public void setSampleValue(String sampleValue) {
            this.sampleValue = sampleValue;
        }
    }
}
