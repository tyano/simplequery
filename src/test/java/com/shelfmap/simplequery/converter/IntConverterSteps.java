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

package com.shelfmap.simplequery.converter;

import static junit.framework.Assert.fail;
import com.shelfmap.simplequery.util.ValueGreaterThanIntMaxException;
import com.shelfmap.simplequery.util.ValueIsNotNumberException;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import com.shelfmap.simplequery.util.IntConverter;
import com.shelfmap.simplequery.util.converter.DefaultIntConverter;
import com.shelfmap.specsfinder.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@Steps
public class IntConverterSteps {
    private IntConverter converter;
    private String convertResult;
    private int restoreResult;
    
    @Given("a DefaultIntConverter")
    public void ceateConverter() {
        converter = new DefaultIntConverter();
    }
    
    @When("the parameter value is <intParam>")
    public void pushParameter(@Named("intParam") int intParam) {
        convertResult = converter.convert(intParam);
    }
    
    @Then("the converted value must be <convertAnswer>")
    public void assertConvertResult(@Named("convertAnswer") String convertAnswer) {
        assertThat(convertResult, is(convertAnswer));
    }
    
    @When("<stringParam> is restored")
    public void restoreValue(@Named("stringParam") String stringParam) throws ValueGreaterThanIntMaxException, ValueIsNotNumberException {
        restoreResult = converter.restore(stringParam);
    }
    
    @Then("the restored value must be <restoreAnswer>")
    public void assertRestoreResult(@Named("restoreAnswer") int restoreAnswer) {
        assertThat(restoreResult, is(restoreAnswer));
    }
    
    private String targetString;
    
    @When("a number greater than Integer.MAX ($number) is restored")
    public void restoreNumberGreaterThanMax(long number) {
        targetString = "" + (DefaultIntConverter.OFFSET + number);
    }
    
    @Then("ValueGreaterThanIntMaxException will be thrown")
    public void assertIntMaxExeption() throws ValueIsNotNumberException {
        try {
            converter.restore(targetString);
        } catch (ValueGreaterThanIntMaxException ex) {
            return;
        }
        fail("ValueGreaterThanIntMaxException did not occur.");
    }
}
