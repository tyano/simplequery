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

import com.shelfmap.simplequery.domain.testdomain.PurchaseRecord;
import com.shelfmap.simplequery.domain.testdomain.Detail;
import com.shelfmap.simplequery.domain.testdomain.ToManyPurchaseRecord;
import com.shelfmap.simplequery.domain.testdomain.PurchaseRecord2;
import com.shelfmap.simplequery.domain.testdomain.ToOnePurchaseRecord;
import com.shelfmap.simplequery.domain.testdomain.DefaultDetail;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import static com.shelfmap.simplequery.SimpleDbUtil.attr;
import com.shelfmap.simplequery.*;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;
import static com.shelfmap.simplequery.util.Dates.date;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
@StoryPath("stories/ReferenceToOtherDomain.story")
public class DomainReferenceTest extends BaseStoryRunner {

    public static final String PARENT_DOMAIN = "ToOneRelationTest-parent";
    public static final String CHILD_DOMAIN = "ToOneRelationTest-child";

    private Context context;
    private Date targetDate = date(2011, 10, 2);

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
                if(PurchaseRecord2.class.isAssignableFrom(domainClass)) {
                    return (InstanceFactory<T>) new ToOneParentInstanceFactory(this);
                }
                return super.getInstanceFactory(domain);
            }

        };
    }

    @Given("domains which refer each other")
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
                              attr("amount", SimpleDBUtils.encodeZeroPadding(100, 5), true),
                              attr("parentItemName", "parent", true))));

        simpleDb.putAttributes(new PutAttributesRequest(
                CHILD_DOMAIN,
                "child2",
                Arrays.asList(attr("name", "本2", true),
                              attr("amount", SimpleDBUtils.encodeZeroPadding(200, 5), true),
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

    @Then("we can get a domain-object of parent from the reference of a child object.")
    public void assertRelationship() throws SimpleQueryException, MultipleResultsExistException {
        assertThat(detailObject, Matchers.is(notNullValue()));
        PurchaseRecord parent = detailObject.getParentRecordReference().get(true);
        assertThat(parent, Matchers.is(notNullValue()));
        assertThat(parent.getItemName(), Matchers.is("parent"));
        assertThat(parent.getRequestDate(), Matchers.is(targetDate));
    }

    @Then("we can get all children from parent's reverse-reference.")
    public void assertReverseReference() throws Exception {
        Client client = context.createNewClient();
        PurchaseRecord parent = client.select().from(PurchaseRecord.class).whereItemName(is("parent")).getSingleResult(true);
        assertThat(parent, Matchers.is(notNullValue()));
        Iterable<Detail> children = parent.getDetailReference().getResults(true);
        assertThat(children, Matchers.is(notNullValue()));
        int index = 0;
        for (Detail detail : children) {
            index++;
            switch(index) {
                case 1:
                    assertThat(detail.getItemName(), Matchers.is("child1"));
                    assertThat(detail.getAmount(), Matchers.is(100));
                    break;
                case 2:
                    assertThat(detail.getItemName(), Matchers.is("child2"));
                    assertThat(detail.getAmount(), Matchers.is(200));
                    break;
            }
        }
        assertThat(index, Matchers.is(2));
    }


    PurchaseRecord2 master;

    @Given("an instance of master-object which have multiple children but handle them with ReverseToOneDomainReference")
    public void createInstanceWithReverseToOneReference() throws SimpleQueryException, MultipleResultsExistException {
        Client client = context.createNewClient();
        master = client.select().from(PurchaseRecord2.class).whereItemName(is("parent")).getSingleResult(true);
    }

    @Then("the master object can get only 1 child from the reference.")
    public void assertReverseToOne() throws SimpleQueryException {
        assertThat(master, Matchers.is(notNullValue()));
        Iterable<Detail> details = master.getDetailReference().getResults(true);
        int index = 0;
        for (Detail detail : details) {
            index++;
        }
        assertThat(index, Matchers.is(1));
    }

    private static class DetailInstanceFactory implements InstanceFactory<Detail> {
        private Context context;

        public DetailInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public Detail createInstance(Class<Detail> clazz) {
            return new DefaultDetail(context);
        }
    }

    private static class ParentInstanceFactory implements InstanceFactory<PurchaseRecord> {
        private Context context;

        public ParentInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public PurchaseRecord createInstance(Class<PurchaseRecord> clazz) {
            return new ToManyPurchaseRecord(context);
        }
    }

    private static class ToOneParentInstanceFactory implements InstanceFactory<PurchaseRecord2> {
        private Context context;

        public ToOneParentInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public PurchaseRecord2 createInstance(Class<PurchaseRecord2> clazz) {
            return new ToOnePurchaseRecord(context);
        }
    }
}
