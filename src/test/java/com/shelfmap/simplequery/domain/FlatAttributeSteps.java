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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;
import static java.util.Arrays.asList;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.shelfmap.simplequery.Client;
import com.shelfmap.simplequery.DefaultConfiguration;
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.FlatAttribute;
import com.shelfmap.simplequery.IntAttribute;
import com.shelfmap.simplequery.SimpleQueryClient;
import com.shelfmap.simplequery.expression.AWSTestBase;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.stepsfinder.Steps;
import java.util.List;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@Steps
public class FlatAttributeSteps extends AWSTestBase {

    private static final String DOMAIN_NAME = "flatattribute-test";
    AmazonSimpleDB simpleDb;
    Client client;
    User user;

    @Given("a SimpleQuery client")
    public void createClient() {
        this.simpleDb = getAmazonSimpleDB();
        this.client = new SimpleQueryClient(this.simpleDb, new DefaultConfiguration());
    }

    @Given("initialized domains")
    public void initializeDomains() {
        simpleDb.deleteDomain(new DeleteDomainRequest(DOMAIN_NAME));
        simpleDb.createDomain(new CreateDomainRequest(DOMAIN_NAME));

        List<ReplaceableItem> itemList = asList(
                item("1", asList(attr("name", "test-1", true), attr("age", "018", true), attr("postalCode", "1640011", true), attr("address1", "test1-Address1", true), attr("address2", "test1-Address2", true), attr("telNo", "0300010001", true), attr("faxNo", "0300020001", true))),
                item("2", asList(attr("name", "test-2", true), attr("age", "019", true), attr("postalCode", "1640012", true), attr("address1", "test2-Address1", true), attr("address2", "test2-Address2", true), attr("telNo", "0300010002", true), attr("faxNo", "0300020002", true))),
                item("3", asList(attr("name", "test-3", true), attr("age", "020", true), attr("postalCode", "1640013", true), attr("address1", "test3-Address1", true), attr("address2", "test3-Address2", true), attr("telNo", "0300010003", true), attr("faxNo", "0300020003", true))),
                item("4", asList(attr("name", "test-4", true), attr("age", "021", true), attr("postalCode", "1640014", true), attr("address1", "test4-Address1", true), attr("address2", "test4-Address2", true), attr("telNo", "0300010004", true), attr("faxNo", "0300020004", true))));

        BatchPutAttributesRequest request = new BatchPutAttributesRequest(DOMAIN_NAME, itemList);
        simpleDb.batchPutAttributes(request);
    }

    @When("select an item whose name is 'test-1'")
    public void selectingTest1() throws Exception {
        Expression<User> exp = client.select("*").from(User.class).where("name", is("test-1"));
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
        Expression<User> exp = client.select().from(User.class).where("address1", is("test2-Address1"));
        user = exp.getSingleResult(true);
    }

    @Then("it should return the item whose name is 'test-2'")
    public void assertTest2() throws Exception {
        assertThat(user, Matchers.is(not(nullValue())));
        assertThat(user.getName(), Matchers.is("test-2"));
    }

    @When("selecting an item which have a FlatAttribute")
    public void selectingTest3() throws Exception {
        Expression<User> exp = client.select().from(User.class).where("name", is("test-3"));
        user = exp.getSingleResult(true);
    }

    @Then("we should be able to access telNo and faxNo through the address.telInfo property.")
    public void assertTest3() throws Exception {
        assertThat(user, Matchers.is(not(nullValue())));
        assertThat(user.getAddress().getTelInfo(), Matchers.is(not(nullValue())));
        assertThat(user.getAddress().getTelInfo().getTelNo(), Matchers.is("0300010003"));
        assertThat(user.getAddress().getTelInfo().getFaxNo(), Matchers.is("0300020003"));
    }
    
    private Address address;

    @When("selecting an item with Address class as the domain class")
    public void selectingTest4() throws Exception {
        Expression<Address> exp = client.select().from(Address.class).where("address1", is("test4-Address1"));
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

    private static ReplaceableAttribute attr(String name, String value, boolean replace) {
        return new ReplaceableAttribute(name, value, replace);
    }

    private static ReplaceableItem item(String itemName, List<ReplaceableAttribute> attrs) {
        return new ReplaceableItem(itemName, attrs);
    }

    @Domain("flatattribute-test")
    public static class User {

        private int age;
        private String name;
        private Address address;

        public User() {
        }

        public User(String name, int age, Address address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        @FlatAttribute
        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        @IntAttribute(padding = 3)
        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Domain("flatattribute-test")
    public static class Address {

        private int postalCode;
        private TelInfo telInfo;
        private String address1;
        private String address2;

        public Address() {
        }

        public Address(int postalCode, TelInfo telInfo) {
            this.postalCode = postalCode;
            this.telInfo = telInfo;
        }

        @IntAttribute(padding = 7)
        public int getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(int postalCode) {
            this.postalCode = postalCode;
        }

        @FlatAttribute
        public TelInfo getTelInfo() {
            return telInfo;
        }

        public void setTelInfo(TelInfo telInfo) {
            this.telInfo = telInfo;
        }

        public String getAddress1() {
            return address1;
        }

        public void setAddress1(String address1) {
            this.address1 = address1;
        }

        public String getAddress2() {
            return address2;
        }

        public void setAddress2(String address2) {
            this.address2 = address2;
        }
    }

    public static class TelInfo {

        private String telNo;
        private String faxNo;

        public TelInfo(String telNo, String faxNo) {
            super();
            this.telNo = telNo;
            this.faxNo = faxNo;
        }

        public TelInfo() {
            super();
        }

        public String getFaxNo() {
            return faxNo;
        }

        public void setFaxNo(String faxNo) {
            this.faxNo = faxNo;
        }

        public String getTelNo() {
            return telNo;
        }

        public void setTelNo(String telNo) {
            this.telNo = telNo;
        }
    }
}
