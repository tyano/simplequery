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

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

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
import com.shelfmap.simplequery.expression.Expression;

import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import static com.shelfmap.simplequery.SimpleDbUtil.*;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.*;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/FlatAttributeSpec.story")
public class FlatAttributeTest extends BaseStoryRunner {

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

    private static final String DOMAIN_NAME = "flatattribute-test";

    @Inject
    TestContext ctx;

    User user;
    Address address;

    @Given("initialized domains for FlatAttribute test")
    public void initializeDomains() {
        ctx.getSimpleDb().deleteDomain(new DeleteDomainRequest(DOMAIN_NAME));
        ctx.getSimpleDb().createDomain(new CreateDomainRequest(DOMAIN_NAME));

        List<ReplaceableItem> itemList = asList(
                item("1", asList(attr("name", "test-1", true), attr("age", "018", true), attr("postalCode", "1640011", true), attr("address1", "test1-Address1", true), attr("address2", "test1-Address2", true), attr("telNo", "0300010001", true), attr("faxNo", "0300020001", true))),
                item("2", asList(attr("name", "test-2", true), attr("age", "019", true), attr("postalCode", "1640012", true), attr("address1", "test2-Address1", true), attr("address2", "test2-Address2", true), attr("telNo", "0300010002", true), attr("faxNo", "0300020002", true))),
                item("3", asList(attr("name", "test-3", true), attr("age", "020", true), attr("postalCode", "1640013", true), attr("address1", "test3-Address1", true), attr("address2", "test3-Address2", true), attr("telNo", "0300010003", true), attr("faxNo", "0300020003", true))),
                item("4", asList(attr("name", "test-4", true), attr("age", "021", true), attr("postalCode", "1640014", true), attr("address1", "test4-Address1", true), attr("address2", "test4-Address2", true), attr("telNo", "0300010004", true), attr("faxNo", "0300020004", true))));

        BatchPutAttributesRequest request = new BatchPutAttributesRequest(DOMAIN_NAME, itemList);
        ctx.getSimpleDb().batchPutAttributes(request);
    }

    @When("select an item whose name is 'test-1'")
    public void selectingTest1() throws Exception {
        Expression<User> exp = ctx.getClient().select("*").from(User.class).where("name", is("test-1"));
        user = exp.getSingleResult(true);
    }

    @Then("the user should have an Address object whose postalCode is 1640011")
    public void assertTest1() {
        assertThat(user, Matchers.is(notNullValue()));
        assertThat(user.getName(), Matchers.is("test-1"));
        assertThat(user.getAddress().getPostalCode(), Matchers.is(1640011));
        assertThat(user.getAddress().getAddress1(), Matchers.is("test1-Address1"));
        assertThat(user.getAddress().getAddress2(), Matchers.is("test1-Address2"));
    }

    @When("selecting an item whose address1 of the address property is 'test2-Address1'")
    public void selectingTest2() throws Exception {
        Expression<User> exp = ctx.getClient().select().from(User.class).where("address1", is("test2-Address1"));
        user = exp.getSingleResult(true);
    }

    @Then("it should return the item whose name is 'test-2'")
    public void assertTest2() throws Exception {
        assertThat(user, Matchers.is(not(nullValue())));
        assertThat(user.getName(), Matchers.is("test-2"));
    }

    @When("selecting an item which have a FlatAttribute")
    public void selectingTest3() throws Exception {
        Expression<User> exp = ctx.getClient().select().from(User.class).where("name", is("test-3"));
        user = exp.getSingleResult(true);
    }

    @Then("we should be able to access telNo and faxNo through the address.telInfo property.")
    public void assertTest3() throws Exception {
        assertThat(user, Matchers.is(not(nullValue())));
        assertThat(user.getAddress().getTelInfo(), Matchers.is(not(nullValue())));
        assertThat(user.getAddress().getTelInfo().getTelNo(), Matchers.is("0300010003"));
        assertThat(user.getAddress().getTelInfo().getFaxNo(), Matchers.is("0300020003"));
    }

    @When("selecting an item with Address class as the domain class")
    public void selectingTest4() throws Exception {
        Expression<Address> exp = ctx.getClient().select().from(Address.class).where("address1", is("test4-Address1"));
        address = exp.getSingleResult(true);
    }

    @Then("we should be able to get a separated Address object")
    public void assertTest4() throws Exception {
        assertThat(address, Matchers.is(not(nullValue())));
        assertThat(address.getAddress2(), Matchers.is("test4-Address2"));
        assertThat(address.getTelInfo(), Matchers.is(notNullValue()));
        assertThat(address.getTelInfo().getTelNo(), Matchers.is("0300010004"));
        assertThat(address.getTelInfo().getFaxNo(), Matchers.is("0300020004"));
    }
}
