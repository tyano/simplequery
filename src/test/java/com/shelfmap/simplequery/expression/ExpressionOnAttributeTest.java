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

import static com.shelfmap.simplequery.attribute.Attributes.attr;
import static com.shelfmap.simplequery.attribute.Attributes.$;
import static com.shelfmap.simplequery.attribute.Attributes.every;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.attribute.impl.AllAttribute;
import com.shelfmap.simplequery.attribute.impl.CountAttribute;
import com.shelfmap.simplequery.attribute.impl.DefaultAttribute;
import com.shelfmap.simplequery.attribute.impl.EveryAttribute;
import com.shelfmap.simplequery.attribute.impl.ItemNameAttribute;
import com.shelfmap.simplequery.attribute.QueryAttribute;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/ExpressionOnAttribute.story")
public class ExpressionOnAttributeTest extends BaseStoryRunner {

    QueryAttribute attribute;

    @When("an attribute of name '<name>' is an instance of DefaultAttribute")
    public void createDefaultAttribute(@Named("name") String name) {
        attribute = new DefaultAttribute(name);
    }

    @When("an instance of DefaultAttribute is created by Attributes.$() method")
    public void createDefaultAttributeWith$(@Named("name") String name) {
        attribute = $(name);
    }

    @When("an instance of DefaultAttribute is created by Attributes.attr() method")
    public void createDefaultAttributeWith_attr(@Named("name") String name) {
        attribute = attr(name);
    }

    @When("an attribute of name '<name>' is an instance of EveryAttribute")
    public void createEveryAttribute(@Named("name") String name) {
        attribute = new EveryAttribute(name);
    }

    @When("an attribute of name '<name>' is created with Attributes.every() method")
    public void createEveryWithAttributes(@Named("name") String name) {
        attribute = every(name);
    }

    @Then("the describe() method of the instance must return '<quoted>'")
    @Alias("the describe() method of the instance must return a string '<quoted>'")
    public void assertDefaultAttribute(@Named("quoted") String quoted) {
        assertThat(attribute.describe(), is(quoted));
    }

    @When("using ItemNameAttribute class as a attribute")
    public void createItemNameAttribute() {
        attribute = new ItemNameAttribute();
    }

    @When("using CountAttribute class as a attribute")
    public void createCountAttribute() {
        attribute = new CountAttribute();
    }

    @When("using AllAttribute class as a attribute")
    public void createAllAttribute() {
        attribute = new AllAttribute();
    }

    @Then("the described name of the attribute must be $result")
    public void assertDescribed(String result) {
        assertThat(attribute.describe(), is(result));
    }
}
