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

import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.nullValue;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import static java.util.Arrays.asList;
import static com.shelfmap.simplequery.SimpleDbUtil.*;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.annotation.Attribute;
import com.shelfmap.simplequery.annotation.Container;
import com.shelfmap.simplequery.annotation.ItemName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/ListProperty.story")
public class ListPropertyTest extends BaseStoryRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPropertyTest.class);
    private static final String DOMAIN_NAME = "list-property";
    @Inject
    TestContext ctx;

    @Override
    protected void configureTestContext(Binder binder) {
        binder.bind(ContextHolder.class).to(TestContext.class).in(Scopes.SINGLETON);
        binder.bind(TestContext.class).in(Scopes.SINGLETON);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<? extends Class<?>> getStepsClasses() {
        return asList(TestClientFactory.class);
    }

    @Given("a initialized domain which have a multi-value column")
    public void createDomains() {
        AmazonSimpleDB simpleDb = ctx.getSimpleDb();
        List<ReplaceableItem> items = asList(
                item("first", attr("tag", "red", true), attr("tag", "blue", false), attr("tag", "yellow", false)));

        simpleDb.deleteDomain(new DeleteDomainRequest(DOMAIN_NAME));
        simpleDb.createDomain(new CreateDomainRequest(DOMAIN_NAME));

        simpleDb.batchPutAttributes(new BatchPutAttributesRequest(DOMAIN_NAME, items));
    }

    @Given("a initialized domain which have a multi-value column without values")
    public void createDomainWithNullColumn() {
        AmazonSimpleDB simpleDb = ctx.getSimpleDb();
        List<ReplaceableItem> items = asList(
                item("empty", attr("name", "sample", true)));

        simpleDb.deleteDomain(new DeleteDomainRequest(DOMAIN_NAME));
        simpleDb.createDomain(new CreateDomainRequest(DOMAIN_NAME));

        simpleDb.batchPutAttributes(new BatchPutAttributesRequest(DOMAIN_NAME, items));
    }
    ListPropertyDomain result;
    DomainWithoutList noListResult;

    @When("selecting an item from the domain which have a multi-value column")
    public void selectItemsWithMultiValues() throws SimpleQueryException, MultipleResultsExistException {
        result = ctx.getContext().getClientFactory().create().select().from(ListPropertyDomain.class).whereItemName(is("first")).getSingleResult(true);
    }

    @Then("we can get the values through a property whose type is a kind of Collection")
    public void assertListProperty() {
        assertThat(result, Matchers.is(notNullValue()));
        assertThat(result.getTags(), Matchers.is(notNullValue()));
        assertThat(result.getTags().size(), Matchers.is(3));

        List<String> tags = result.getTags();
        LOGGER.debug("tags = {}", tags);

        Collections.sort(tags);
        LOGGER.debug("tags = {}", tags);

        assertThat(tags.get(0), Matchers.is("blue"));
        assertThat(tags.get(1), Matchers.is("red"));
        assertThat(tags.get(2), Matchers.is("yellow"));
    }

    @When("selecting an item from the same domain, but the properties type is not a kind of Collection")
    public void selectItemBySingleObjectProperty() throws SimpleQueryException, MultipleResultsExistException {
        noListResult = ctx.getContext().getClientFactory().create().select().from(DomainWithoutList.class).whereItemName(is("first")).getSingleResult(true);
    }

    @Then("we should get a random value from values of the multi-value column")
    public void assertItMustBeAFirstValue() {
        List<String> values = Arrays.asList("red", "blue", "yellow");
        assertThat(noListResult, Matchers.is(notNullValue()));
        assertThat(noListResult.getTag(), Matchers.is(notNullValue()));
        String tag = noListResult.getTag();
        assertThat(tag, isIn(values));
    }

    @When("the value of a multi-value column is null and the type of the property associated with the column is a kind of Collection")
    public void selectEmptyMultiValueColumnByList() throws SimpleQueryException, MultipleResultsExistException {
        result = ctx.getContext().getClientFactory().create().select().from(ListPropertyDomain.class).whereItemName(is("empty")).getSingleResult(true);
    }

    @Then("the return value must be an empty collection")
    public void assertTheResultIsEmptyCollection() {
        assertThat(result, Matchers.is(notNullValue()));
        assertThat(result.getTags(), Matchers.is(notNullValue()));
        assertThat(result.getTags().isEmpty(), Matchers.is(true));
    }

    @When("the value of a multi-value column is null and the type of the property associated with the column is not a Collection")
    public void selectEmptyMultiValueColumnBySingleObjectProperty() throws SimpleQueryException, MultipleResultsExistException {
        noListResult = ctx.getContext().getClientFactory().create().select().from(DomainWithoutList.class).whereItemName(is("empty")).getSingleResult(true);
    }

    @Then("the return value must be a null")
    public void assertItsResultMustBeANullValue() {
        assertThat(noListResult, Matchers.is(notNullValue()));
        assertThat(noListResult.getTag(), Matchers.is(nullValue()));
    }
    ArrayDomain arrayResult;

    @When("selecting an item from the domain which have a multi-value column and receive the result with an Array property")
    public void selectWithArray() throws SimpleQueryException, MultipleResultsExistException {
        arrayResult = ctx.getContext().getClientFactory().create().select().from(ArrayDomain.class).whereItemName(is("first")).getSingleResult(true);
    }

    @Then("we can get the values through a property whose type is an Array")
    public void assertArray() {
        assertThat(arrayResult, Matchers.is(notNullValue()));
        assertThat(arrayResult.getTags(), Matchers.is(notNullValue()));
        String[] tags = arrayResult.getTags();
        assertThat(tags.length, Matchers.is(3));
        Arrays.sort(tags);

        assertThat(tags[0], Matchers.is("blue"));
        assertThat(tags[1], Matchers.is("red"));
        assertThat(tags[2], Matchers.is("yellow"));
    }

    @When("the value of a multi-value column is null and the type of the property associated with the column is an array")
    public void selectEmptyValueByArray() throws SimpleQueryException, MultipleResultsExistException {
        arrayResult = ctx.getContext().getClientFactory().create().select().from(ArrayDomain.class).whereItemName(is("empty")).getSingleResult(true);
    }

    @Then("the return value must be an array which size is zero")
    public void assertEmptyArray() {
        assertThat(arrayResult, Matchers.is(notNullValue()));
        assertThat(arrayResult.getTags(), Matchers.is(notNullValue()));
        assertThat(arrayResult.getTags().length, Matchers.is(0));
    }

    @SimpleDbDomain(value = DOMAIN_NAME)
    public static class ListPropertyDomain {

        private String itemName;
        private List<String> tags;
        private final Object tagMonitor = new Object();

        @ItemName
        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Container(containerType = ArrayList.class, valueType = String.class)
        @Attribute(attributeName = "tag")
        public List<String> getTags() {
            synchronized (tagMonitor) {
                if (tags == null) {
                    createInnerList();
                }
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
            synchronized (tagMonitor) {
                tags.addAll(tags);
            }
        }

        private void createInnerList() {
            synchronized (tagMonitor) {
                if (tags == null) {
                    tags = new ArrayList<String>();
                }
            }
        }
    }

    @SimpleDbDomain(DOMAIN_NAME)
    public static class ArrayDomain {

        private String domainName;
        private String[] tags;

        @ItemName
        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        @Container(containerType = String[].class, valueType = String.class)
        @Attribute(attributeName = "tag")
        public String[] getTags() {
            if(tags == null) return new String[0];
            String[] array = new String[tags.length];
            System.arraycopy(tags, 0, array, 0, tags.length);
            return array;
        }

        public void setTags(String[] tags) {
            this.tags = new String[tags.length];
            System.arraycopy(tags, 0, this.tags, 0, tags.length);
        }
    }

    @SimpleDbDomain(DOMAIN_NAME)
    public static class DomainWithoutList {

        private String domainName;
        private String tag;

        @ItemName
        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        @Attribute(attributeName = "tag")
        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}
