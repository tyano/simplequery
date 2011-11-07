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
import com.shelfmap.simplequery.domain.testdomain.*;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import com.shelfmap.simplequery.expression.matcher.MatcherFactory;
import static com.shelfmap.simplequery.util.Dates.date;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import static org.hamcrest.Matchers.*;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
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
        context = new TokyoContext(new PropertiesCredentials(new File(TestClientFactory.CREDENTIAL_PATH))) {
            private static final long serialVersionUID = 1L;

            @Override
            @SuppressWarnings("unchecked")
            public <T> DomainInstanceFactory<T> getDomainInstanceFactory(Domain<T> domain) {
                Class<T> domainClass = domain.getDomainClass();

                if(ToManyDetail.class.isAssignableFrom(domainClass)) {
                    return (DomainInstanceFactory<T>) new DetailInstanceFactory(this);
                }

                if(ToOneDetail.class.isAssignableFrom(domainClass)) {
                    return (DomainInstanceFactory<T>) new ToOneDetailInstanceFactory(this);
                }

                if(ToManyPurchaseRecord.class.isAssignableFrom(domainClass)) {
                    return (DomainInstanceFactory<T>) new ParentInstanceFactory(this);
                }
                if(ToOnePurchaseRecord.class.isAssignableFrom(domainClass)) {
                    return (DomainInstanceFactory<T>) new ToOneParentInstanceFactory(this);
                }
                return super.getDomainInstanceFactory(domain);
            }

        };
    }

    @Given("domains which refer each other")
    public void setupDomains() {
        AmazonSimpleDB simpleDb = context.getSimpleDB();

        simpleDb.deleteDomain(new DeleteDomainRequest(PARENT_DOMAIN));
        simpleDb.deleteDomain(new DeleteDomainRequest(CHILD_DOMAIN));

        simpleDb.createDomain(new CreateDomainRequest(PARENT_DOMAIN));
        simpleDb.createDomain(new CreateDomainRequest(CHILD_DOMAIN));

        simpleDb.putAttributes(new PutAttributesRequest(
                CHILD_DOMAIN,
                "child1",
                Arrays.asList(attr("name", "Book", true),
                              attr("amount", SimpleDBUtils.encodeZeroPadding(100, 5), true),
                              attr("parentItemName", "parent", true))));

        simpleDb.putAttributes(new PutAttributesRequest(
                CHILD_DOMAIN,
                "child2",
                Arrays.asList(attr("name", "Book 2", true),
                              attr("amount", SimpleDBUtils.encodeZeroPadding(200, 5), true),
                              attr("parentItemName", "parent", true))));

        simpleDb.putAttributes(new PutAttributesRequest(
                PARENT_DOMAIN,
                "parent",
                Arrays.asList(attr("requestDate", SimpleDBUtils.encodeDate(targetDate), true),
                              attr("detail", "child1", true))));

    }

    private ToManyDetail detailObject;

    @Given("an instance of a domain-object which have a relationship.")
    public void createDomainObject() throws SimpleQueryException, MultipleResultsExistException {
        detailObject = context.select().from(ToManyDetail.class).whereItemName(MatcherFactory.is("child1")).getSingleResult(true);
    }

    @Then("we can get a domain-object of parent from the reference of a child object.")
    public void assertRelationship() throws SimpleQueryException, MultipleResultsExistException {
        assertThat(detailObject, is(notNullValue()));
        ToManyPurchaseRecord parent = detailObject.getParentRecordReference().get(true);
        assertThat(parent, is(notNullValue()));
        assertThat(parent.getItemName(), is("parent"));
        assertThat(parent.getRequestDate(), is(targetDate));
    }

    @Then("we can get all children from parent's reverse-reference.")
    public void assertReverseReference() throws Exception {
        ToManyPurchaseRecord parent = context.select().from(ToManyPurchaseRecord.class).whereItemName(MatcherFactory.is("parent")).getSingleResult(true);
        assertThat(parent, is(notNullValue()));
        Iterable<ToManyDetail> children = parent.getDetailReference().getResults(true);
        assertThat(children, is(notNullValue()));
        int index = 0;
        for (ToManyDetail detail : children) {
            index++;
            switch(index) {
                case 1:
                    assertThat(detail.getItemName(), is("child1"));
                    assertThat(detail.getAmount(), is(100));
                    break;
                case 2:
                    assertThat(detail.getItemName(), is("child2"));
                    assertThat(detail.getAmount(), is(200));
                    break;
            }
        }
        assertThat(index, is(2));
    }


    ToOnePurchaseRecord master;

    @Given("an instance of master-object which have multiple children but handle them with ReverseToOneDomainReference")
    @Alias("an instance which have a ReverseToOneDomainReference")
    public void createInstanceWithReverseToOneReference() throws SimpleQueryException, MultipleResultsExistException {
        master = context.select().from(ToOnePurchaseRecord.class).whereItemName(MatcherFactory.is("parent")).getSingleResult(true);
    }

    @Then("the master object can get only 1 child from the reference.")
    public void assertReverseToOne() throws SimpleQueryException {
        assertThat(master, is(notNullValue()));
        Iterable<ToOneDetail> details = master.getDetailReference().getResults(true);
        int index = 0;
        for (ToOneDetail detail : details) {
            index++;
        }
        assertThat(index, is(1));
    }


    ToOneDetail newDetail = null;
    ToOneDetail oldDetail = null;

    @When("the content of the reference is changed,")
    public void changeContentOfAReverseToOneReference() throws SimpleQueryException, MultipleResultsExistException {
        oldDetail = master.getDetailReference().get(true);
        newDetail = new ToOneDetailImpl(context, "item2", "changedDetail", 1);
        master.getDetailReference().set(newDetail);
        context.putObjects(master);
    }

    @Then("objects previously referenced by ReverseToOneDomainReference and all new targets exist in the current context.")
    public void assertTheContentPushedIntoContext() {
        Collection<Object> deleted = context.getDeleteObjects();
        Collection<Object> put = context.getPutObjects();

        assertThat(deleted.isEmpty(), is(true));
        assertThat(put.isEmpty(), is(not(nullValue())));
        assertThat(put.size(), is(3));
        assertThat(put, hasItems(master, newDetail, oldDetail));
    }

    /*
     * Instance Factories
     */

    private static class DetailInstanceFactory implements DomainInstanceFactory<ToManyDetail> {
        private Context context;

        public DetailInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public ToManyDetail create() {
            return new ToManyDetailImpl(context);
        }
    }

    private static class ToOneDetailInstanceFactory implements DomainInstanceFactory<ToOneDetail> {
        private Context context;

        public ToOneDetailInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public ToOneDetail create() {
            return new ToOneDetailImpl(context);
        }
    }

    private static class ParentInstanceFactory implements DomainInstanceFactory<ToManyPurchaseRecord> {
        private Context context;

        public ParentInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public ToManyPurchaseRecord create() {
            return new ToManyPurchaseRecordImpl(context);
        }
    }

    private static class ToOneParentInstanceFactory implements DomainInstanceFactory<ToOnePurchaseRecord> {
        private Context context;

        public ToOneParentInstanceFactory(Context context) {
            this.context = context;
        }

        @Override
        public ToOnePurchaseRecord create() {
            return new ToOnePurchaseRecordImpl(context);
        }
    }
}
