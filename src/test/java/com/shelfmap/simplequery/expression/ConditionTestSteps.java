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

import com.shelfmap.simplequery.domain.impl.IntAttributeConverter;
import static com.shelfmap.simplequery.expression.Conditions.group;
import static com.shelfmap.simplequery.expression.Conditions.condition;
import static org.junit.Assert.assertThat;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.*;
import com.shelfmap.specsfinder.Steps;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@Steps
public class ConditionTestSteps {

    Condition<?> condition;

    @When("a condition is initialized with a attribute name '$attributeName' and a matcher 'is($value)'")
    public void createCondition1(String attributeName, int value) {
        condition = condition(attributeName, is(value));
    }

    @Then("the describe() method of the condition must return a string like \"$expected\"")
    public void assert1(String expected) {
        assertThat(condition.describe(), Matchers.is(expected));
    }

    @When("a condition is initialized with a attribute name '$attributeName' and a matcher 'greaterEqual($value)', and the matcher has a padding of $padding")
    public void createPaddingCondition(String attributeName, int value, int padding) {
        condition = condition(attributeName, greaterEqual(value).withAttributeInfo(new IntAttributeConverter(padding, 0)));
    }

    @When("a condition is initialized with a attribute name '$attributeName' and a matcher 'greaterThan($value)', and the matcher has a padding of $padding and a offset of $offsetValue,")
    public void createOffsetCondition(String attributeName, int value, int padding, int offsetValue) {
        condition = condition(attributeName, greaterThan(value).withAttributeInfo(new IntAttributeConverter(padding, offsetValue)));
    }

    @When("a condition with a expression like $attributeName = '$value', and group() method has been called to the condition")
    public void createGroupCondigion(String attributeName, String value) {
        condition = condition(attributeName, is(value)).group();
    }

    @When("a groupd condition and a normal condition is joined with operator 'and'")
    public void createGroupedCondition1() {
        condition = condition("first", like("yano-%")).group().and("last", notLike("%.java"));
    }

    @Then("the result must be like (first condition) and normal-condition")
    public void assertGroupedCondition1() {
        assertThat(condition.describe(), Matchers.is("(`first` like 'yano-%') and `last` not like '%.java'"));
    }

    @When("multiple grouped conditions are used and collected conditions have been grouped at last")
    public void createGroupedCondition2() {
        condition = condition("first", is(1)).and("second", isNot(2)).group().or(
                condition("third", lessThan(100)).and("fourth", lessEqual(200)).group()).group();
    }

    @Then("the result must be grouped multiple times like ((first expression) or (second expression)).")
    public void assertGroupedCondition2() {
        assertThat(condition.describe(), Matchers.is("((`first` = '1' and `second` != '2') or (`third` < '100' and `fourth` <= '200'))"));
    }

    @When("a intersection method is called")
    public void createIntersectionCondition() {
        condition = condition("first", is(1));
    }

    @Then("two conditions will be joined with a intersection operator.")
    public void assertIntersection() {
        Condition intersection = condition.intersection("second", greaterEqual(30));
        assertThat(intersection.describe(), Matchers.is("`first` = '1' intersection `second` >= '30'"));
    }

    @When("another condition has been joined after using intersection")
    public void join3Conditions() {
        condition = condition("first", is(1)).intersection("second", lessThan(0).withAttributeInfo(new IntAttributeConverter(5, 90000))).or("third", is("name"));
    }

    @Then("three conditions will be joined as a series of conditions. Don't grouped automatically.")
    public void assert3conditions() {
        assertThat(condition.describe(), Matchers.is("`first` = '1' intersection `second` < '90000' or `third` = 'name'"));
    }

    @When("two grouped condition has been joined with a intersection operator")
    public void join2groupedConditions() {
        Condition<?> c1 = group(condition("first", is(1)).and("second", is("name")));
        Condition<?> c2 = group(condition("third", is(2)).or("fourth", is("age")));
        condition = c1.intersection(c2);
    }

    @Then("the grouping will be keeped.")
    public void assert2GroupedConditions() {
        assertThat(condition.describe(), Matchers.is("(`first` = '1' and `second` = 'name') intersection (`third` = '2' or `fourth` = 'age')"));
    }
}
