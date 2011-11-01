/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.shelfmap.simplequery;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.*;
import com.shelfmap.simplequery.annotation.ForwardDomainReference;
import com.shelfmap.simplequery.annotation.IntAttribute;
import com.shelfmap.simplequery.annotation.ItemName;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.ToOneDomainReference;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import com.shelfmap.simplequery.expression.matcher.MatcherFactory;
import java.io.File;
import org.jbehave.core.annotations.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;

import org.jbehave.core.annotations.When;

import static com.shelfmap.simplequery.SimpleDbUtil.*;

import com.shelfmap.simplequery.domain.impl.DefaultToOneDomainReference;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import java.io.IOException;
import org.jbehave.core.annotations.Then;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/SavingDomains.story")
public class SavingDomainsTest extends BaseStoryRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SavingDomainsTest.class);
    private static final String MAIN_DOMAIN = "SavingDomainsTest-main";
    private static final String SUB_DOMAIN = "SavingDomainsTest-sub";
    Context context;

    @Given("a test-specific context")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    public void createContext() throws IOException {
        context = new TokyoContext(new PropertiesCredentials(new File(TestClientFactory.CREDENTIAL_PATH))) {

            private static final long serialVersionUID = 1L;

            @Override
            @SuppressWarnings("unchecked")
            public <T> DomainInstanceFactory<T> getDomainInstanceFactory(Domain<T> domain) {
                Class<T> domainClass = domain.getDomainClass();

                if (Province.class.isAssignableFrom(domainClass)) {
                    return (DomainInstanceFactory<T>) new SavingDomainsTest.ProvinceInstanceFactory(this);
                }
                if (User.class.isAssignableFrom(domainClass)) {
                    return (DomainInstanceFactory<T>) new SavingDomainsTest.UserInstanceFactory(this);
                }
                return super.getDomainInstanceFactory(domain);
            }
        };
    }

    @Given("2 test domains")
    public void createTestDomains() {
        AmazonSimpleDB simpleDb = context.getSimpleDB();

        simpleDb.deleteDomain(new DeleteDomainRequest(MAIN_DOMAIN));
        simpleDb.deleteDomain(new DeleteDomainRequest(SUB_DOMAIN));

        simpleDb.createDomain(new CreateDomainRequest(MAIN_DOMAIN));
        simpleDb.createDomain(new CreateDomainRequest(SUB_DOMAIN));

        BatchPutAttributesRequest subRequest = new BatchPutAttributesRequest(
                SUB_DOMAIN,
                asList(item("tokyo", attr("name", "Tokyo", true)),
                       item("osaka", attr("name", "Osaka", true)),
                       item("chiba", attr("name", "Chiba", true))));

        simpleDb.batchPutAttributes(subRequest);


        BatchPutAttributesRequest mainRequest =
                new BatchPutAttributesRequest(
                MAIN_DOMAIN,
                asList(item("0001",
                            attr("name", "User 1", true),
                            attr("age", "035", true),
                            attr("province", "tokyo", true)),
                       item("0002",
                            attr("name", "User 2", true),
                            attr("age", "022", true),
                            attr("province", "chiba", true))));

        simpleDb.batchPutAttributes(mainRequest);
    }
    Province tokyo;
    Province chiba;
    User user1;
    User user2;

    @Given("A instance of the test domain which have a reference to another domain")
    public void createTestInstances() {
        try {
            tokyo = context.select().from(Province.class).whereItemName(MatcherFactory.is("tokyo")).getSingleResult(true);
            user1 = context.select().from(User.class).whereItemName(MatcherFactory.is("0001")).getSingleResult(true);
        } catch (Exception ex) {
            LOGGER.error("exception occur", ex);
            fail();
        }
    }

    @When("we change some properties of the instance of the test domain")
    public void changeTestInstances() {
        tokyo.setName("TOKYO");
        user1.setAge(24);
    }

    @Then("we can apply the changes against the one instance immediately with one method.")
    public void saveTheMainInstanceAndAssert() {
        try {
            context.putObjectImmediately(user1);

            User assertUser = context.select().from(User.class).whereItemName(MatcherFactory.is("0001")).getSingleResult(true);

            assertThat(assertUser.getName(), is("User 1"));
            assertThat(assertUser.getAge(), is(24));
        } catch (Exception ex) {
            LOGGER.error("exception occur", ex);
            fail();
        }

    }

    @Then("the instance of another domain do not saved.")
    public void assertReferencedDomainsDoNotSaved() {
        try {
            User assertUser = context.select().from(User.class).whereItemName(MatcherFactory.is("0001")).getSingleResult(true);

            Province assertTokyo = assertUser.getProvinceReference().get(true);
            assertThat(assertTokyo.getItemName(), is("tokyo"));
            assertThat(assertTokyo.getName(), is(not("TOKYO")));
            assertThat(assertTokyo.getName(), is("Tokyo"));
        } catch (Exception ex) {
            LOGGER.error("exception occur", ex);
            fail();
        }
    }

    @Given("some instances of the test domain which have a reference to another domain")
    public void createMultiInstances() {
        try {
            QueryResults<Province> results = context.select().from(Province.class).whereItemName(MatcherFactory.in("tokyo", "chiba")).getResults(true);
            for (Province p : results) {
                if (p.getItemName().equals("tokyo")) {
                    tokyo = p;
                } else if (p.getItemName().equals("chiba")) {
                    chiba = p;
                }
            }

            QueryResults<User> users = context.select().from(User.class).whereItemName(MatcherFactory.in("0001", "0002")).getResults(true);
            for (User u : users) {
                if (u.getItemName().equals("0001")) {
                    user1 = u;
                } else if (u.getItemName().equals("0002")) {
                    user2 = u;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("exception occur", ex);
            fail();
        }
    }

    @When("we change some properties of instances of test domains and the referenced instances")
    public void changePropertiesOfAllInstances() {
        try {
            user1.setName("changed user 1");
            user2.setName("changed user 2");

            Province refTokyo = user1.getProvinceReference().get(true);
            Province refChiba = user2.getProvinceReference().get(true);

            refTokyo.setName("TOKYO");
            refChiba.setName("CHIBA");

            //save all changes
            context.putObjects(user1, user2, refTokyo, refChiba);
            context.save();
        } catch (Exception ex) {
            LOGGER.error("exception occur", ex);
            fail();
        }
    }

    @Then("we can save all instances, which put into Context, with one method call")
    public void assertChangesApplied() {
        try {
            QueryResults<Province> results = context.select().from(Province.class).whereItemName(MatcherFactory.in("tokyo", "chiba")).getResults(true);
            for (Province p : results) {
                if (p.getItemName().equals("tokyo")) {
                    assertThat(p.getName(), is("TOKYO"));
                } else if (p.getItemName().equals("chiba")) {
                    assertThat(p.getName(), is("CHIBA"));
                }
            }

            QueryResults<User> users = context.select().from(User.class).whereItemName(MatcherFactory.in("0001", "0002")).getResults(true);
            for (User u : users) {
                if (u.getItemName().equals("0001")) {
                    assertThat(u.getName(), is("changed user 1"));
                } else if (u.getItemName().equals("0002")) {
                    assertThat(u.getName(), is("changed user 2"));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("exception occur", ex);
            fail();
        }
    }

    @When("we set null-value to an attribute of an instance")
    public void changeAPropertyNull() {
        user1.setName(null);
        context.putObjectImmediately(user1);
    }

    @Then("the attribute will be deleted in SimpleDB")
    public void assertTheAttributeDeletedInAWS() {
        AmazonSimpleDB simpleDB = context.getSimpleDB();

        GetAttributesRequest req = new GetAttributesRequest().withDomainName(MAIN_DOMAIN).withItemName("0001").withConsistentRead(true).withAttributeNames("name");

        GetAttributesResult attributes = simpleDB.getAttributes(req);
        for (Attribute attr : attributes.getAttributes()) {
            if (attr.getName().equals("name")) {
                fail("the 'name' attribute exists. It must be deleted.");
            }
        }
    }

    @Then("the property of the instance become null if the instance is refreshed")
    public void assertThePropertyBecomeNull() throws SimpleQueryException, MultipleResultsExistException {
        User refreshedUser1 = context.select().from(User.class).whereItemName(MatcherFactory.is("0001")).getSingleResult(true);
        assertThat(refreshedUser1.getName(), is(nullValue()));
    }

    @When("we put a changed object, delete it and put it again")
    public void updateContext() {
        tokyo.setName("TOKYO");
        chiba.setName("CHIBA");
        context.putObjects(tokyo, chiba);
        context.deleteObjects(tokyo, chiba);
        context.putObjects(tokyo);
        context.save();
    }

    @Then("the last operation must be alive")
    public void assertTheLastState() throws SimpleQueryException, MultipleResultsExistException {
        Province changedTokyo = context.select().from(Province.class).whereItemName(MatcherFactory.is("tokyo")).getSingleResult(true);
        Province changedChiba = context.select().from(Province.class).whereItemName(MatcherFactory.is("chiba")).getSingleResult(true);

        assertThat(changedTokyo, is(not(nullValue())));
        assertThat(changedTokyo.getName(), is("TOKYO"));
        assertThat(changedChiba, is(nullValue()));
    }

    /**
     * テスト用インタフェース
     */
    @SuppressWarnings("PublicInnerClass")
    @SimpleDbDomain("SavingDomainsTest-main")
    public static interface User {

        @ItemName
        String getItemName();

        void setItemName(String itemName);

        String getName();

        void setName(String name);

        @IntAttribute(padding = 3)
        int getAge();

        void setAge(int age);

        @ForwardDomainReference(attributeName = "province", targetDomainClass = Province.class)
        ToOneDomainReference<Province> getProvinceReference();

        void setProvinceReference(ToOneDomainReference<Province> reference);
    }

    @SuppressWarnings("PublicInnerClass")
    @SimpleDbDomain("SavingDomainsTest-sub")
    public static interface Province {

        @ItemName
        String getItemName();

        void setItemName(String name);

        String getName();

        void setName(String name);
    }

    /**
     * テスト用インタフェースの実装クラス
     */
    @SuppressWarnings("PublicInnerClass")
    public static class UserImpl implements User {

        Context context;
        String itemName;
        String userName;
        int age;
        ToOneDomainReference<Province> provinceReference;

        public UserImpl(Context context, String itemName, String userName, int age) {
            this.context = context;
            this.itemName = itemName;
            this.userName = userName;
            this.age = age;
            this.provinceReference = new DefaultToOneDomainReference<Province>(context, context.getDomainFactory().createDomain(Province.class));
        }

        public UserImpl(Context context) {
            this(context, null, null, 0);
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
            return this.userName;
        }

        @Override
        public void setName(String name) {
            this.userName = name;
        }

        @Override
        public int getAge() {
            return this.age;
        }

        @Override
        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public ToOneDomainReference<Province> getProvinceReference() {
            return this.provinceReference;
        }

        @Override
        public void setProvinceReference(ToOneDomainReference<Province> reference) {
            this.provinceReference = reference;
        }
    }

    @SuppressWarnings("PublicInnerClass")
    public static class ProvinceImpl implements Province {

        Context context;
        String name;
        String itemName;

        public ProvinceImpl(Context context, String itemName, String name) {
            this.context = context;
            this.itemName = itemName;
            this.name = name;
        }

        public ProvinceImpl(Context context) {
            this(context, null, null);
        }

        @Override
        public String getItemName() {
            return itemName;
        }

        @Override
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * インスタンスファクトリ
     */
    @SuppressWarnings("PublicInnerClass")
    public class UserInstanceFactory implements DomainInstanceFactory<User> {

        Context context;

        public UserInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public User create() {
            return new UserImpl(context);
        }
    }

    @SuppressWarnings("PublicInnerClass")
    public class ProvinceInstanceFactory implements DomainInstanceFactory<Province> {

        Context context;

        public ProvinceInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public Province create() {
            return new ProvinceImpl(context);
        }
    }
}
