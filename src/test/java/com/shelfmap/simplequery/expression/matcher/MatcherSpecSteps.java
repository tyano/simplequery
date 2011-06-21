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
package com.shelfmap.simplequery.expression.matcher;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import com.shelfmap.simplequery.domain.impl.IntAttributeConverter;
import com.shelfmap.stepsfinder.Steps;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@Steps
public class MatcherSpecSteps {

    private Matcher<?> matcher;

    @When("the value is \"<targetValue>\",")
    public void convert(@Named("targetValue") String value) {
        matcher = new IsMatcher<String>(value);
    }

    @Then("the converted value must be \"<resultValue>\".")
    public void assertConvertedValue(@Named("resultValue") String expected) {
        assertThat(matcher.describe(), is(expected));
    }

    @When("the int value is <targetValue>,")
    public void convertIntWithoutPaddingOffset(@Named("targetValue") int value) {
        matcher = new IsMatcher<Integer>(value);
    }

    @When("the padding size is <paddingSize> and the int value is <targetValue>,")
    public void convertIntWithPadding(@Named("paddingSize") int padding, @Named("targetValue") int value) {
        matcher = new IsMatcher<Integer>(value).withAttributeConverter(new IntAttributeConverter(padding, 0));
    }

    @When("the padding size is <paddingSize>, the offset is <offset> and the int value is <targetValue>,")
    public void convertIntWithOffsetAndPadding(@Named("paddingSize") int padding, @Named("offset") int offset, @Named("targetValue") int value) {
        matcher = new IsMatcher<Integer>(value).withAttributeConverter(new IntAttributeConverter(padding, offset));
    }

    @When("a IsNullMatcher is described")
    public void createIsNullMatcher() {
        matcher = new IsNullMatcher();
    }

    @Then("the result string must be 'is null'")
    public void assertIsNull() {
        assertThat(matcher.describe(), is("is null"));
    }

    @When("a IsNotNullMatcher is described")
    public void createIsNotNullMatcher() {
        matcher = new IsNotNullMatcher();
    }

    @Then("the result string must be 'is not null'")
    public void assertIsNotNull() {
        assertThat(matcher.describe(), is("is not null"));
    }

    @When("a InMatcher with parameters '$arg1' and '$arg2' is described")
    public void createInMatcher(String arg1, String arg2) {
        matcher = new InMatcher<String>(arg1, arg2);
    }

    @Then("the result string must be like '$result'")
    public void assertInMatcherResult(String result) {
        assertThat(matcher.describe(), is(result));
    }

    @When("a InMatcher's parameters are $arg1 and $arg2, and padding = $padding, offset = $offset")
    public void createInMatcherWithPaddingOffset(int arg1, int arg2, int padding, int offset) {
        matcher = new InMatcher<Integer>(arg1, arg2).withAttributeConverter(new IntAttributeConverter(padding, offset));
    }
}
