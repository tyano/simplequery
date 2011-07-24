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

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.StoryPath;
import static com.shelfmap.simplequery.attribute.Attributes.attr;
import static com.shelfmap.simplequery.expression.Conditions.$;

import static org.junit.Assert.assertThat;

import static com.shelfmap.simplequery.expression.Conditions.group;
import static com.shelfmap.simplequery.expression.Conditions.not;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.in;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/NotCondition.story")
public class NotConditionTest extends BaseStoryRunner {

    Condition<?> condition;

    @When("using .not() medhod against an condition object")
    public void createNotCondition() {
        condition = $(attr("name"), in("yano", "yano2")).not();
    }

    @When("using not() static method of the Conditions class")
    public void createNotConditionWithCondisions() {
        condition = not($(attr("name"), in("yano", "yano2")));
    }

    @Then("the result of describe() must be like 'not (a expression)'")
    public void assertNotCondition() {
        assertThat(condition.describe(), Matchers.is("not (`name` in ('yano', 'yano2'))"));
    }

    @When("using not() method against a GroupCondition object")
    public void createNotConditionOnGroupCondition() {
        condition = not(group($(attr("name"), is("yano")).and($(attr("address"), is("Tokyo")))));
    }

    @Then("the result of describe() must be like 'not ((a expression and a expression))")
    public void assertNotGroupCondition() {
        assertThat(condition.describe(), Matchers.is("not ((`name` = 'yano' and `address` = 'Tokyo'))"));
    }
}
