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

import static org.junit.Assert.*;
import static com.shelfmap.simplequery.expression.MatcherFactory.*;
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.SimpleDBAttribute;
import com.shelfmap.simplequery.expression.impl.Select;
import com.shelfmap.specsfinder.Steps;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@Steps
public class WhereExpressionTestSteps {

    Expression<?> expression;

    private String getSecurityCredentialPath() {
        return "/Users/t_yano/aws.credential.properties";
    }

    @When("TestDomain don't have any @SimpleDBAttribute annotation on it's properties,")
    public void createExpression() {
        expression = new Select().from(DomainWithoutAttribute.class).where("saving", greaterThan(100000));
    }

    @Then("WhereExpression will generate a simple expression with no padding and no offset.")
    public void assertExpression() {
        assertThat(expression.describe(), Matchers.is("select * from testdomain where `saving` > '100000'"));
    }

    @When("TestDomain have a @SimpleDBAttribute on a property whose name is same with the attribute specified in a expression")
    public void createExpressionWithAnnotation() {
        expression = new Select().from(DomainWithAttribute.class).where("saving", greaterThan(100000));
    }

    @Then("WhereExpression will use a padding and a offset on the annotation")
    public void assertExpressionWithAnnotation() {
        assertThat(expression.describe(), Matchers.is("select * from with-attribute where `saving` > '01100000'"));
    }

    @When("a property have a @SimpleDBAttribute annotation, but the annotation don't have attributeName,")
    public void createExpressionWithoutAttributeName() {
        expression = new Select().from(DomainWithoutAttributeName.class).where("saving", greaterThan(500));
    }

    @Then("this library will find the annotation through the name of property instead of the attributeName, and uses values of the annotation.")
    public void assertExpressionWithoutAttributeName() {
        assertThat(expression.describe(), Matchers.is("select * from without-attributename where `saving` > '0200500'"));
    }

    
    @Domain("testdomain")
    public static interface DomainWithoutAttribute {

        int getSaving();
    }

    @Domain("with-attribute")
    public static interface DomainWithAttribute {

        @SimpleDBAttribute(attributeName = "saving", maxDigitLeft = 8, offset = 1000000L)
        int getAccountSaving();
    }
    
    @Domain("without-attributename")
    public static interface DomainWithoutAttributeName {
        @SimpleDBAttribute(maxDigitLeft=7, offset=200000L)
        int getSaving();
    }

}
