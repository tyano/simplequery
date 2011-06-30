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

import static org.junit.Assert.assertThat;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.*;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.ClientFactory;
import com.shelfmap.simplequery.IClientHolder;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.TestContext;
import com.shelfmap.simplequery.expression.impl.Select;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/LimitExpressionSpec.story")
public class LimitExpressionTest extends BaseStoryRunner {
    @Override
    protected void configureTestContext(Binder binder) {
        binder.bind(IClientHolder.class).to(TestContext.class).in(Scopes.SINGLETON);
        binder.bind(TestContext.class).in(Scopes.SINGLETON);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<? extends Class<?>> getStepsClasses() {
        return Arrays.asList(ClientFactory.class);
    }

    @Inject
    TestContext ctx;

    Expression<?> exp;

    @When("calling limit(10) for OrderByExpression")
    public void callLimitAgainstOrderByExp() {
        exp = new Select(ctx.getSimpleDb(), ctx.getConfiguration(), "*").from(TestDomain.class).where("name", like("yano%")).orderBy("name", SortOrder.Asc).limit(10);
    }

    @Then("it should generate 'limit 10'")
    public void assertLimit() {
        assertThat(exp.describe(), Matchers.is("select * from `test-domain` where `name` like 'yano%' order by `name` asc limit 10"));
    }

    @When("limit(10) for WhereExpression")
    public void callLimitAgainstWhereExp() {
        exp = new Select(ctx.getSimpleDb(), ctx.getConfiguration(), "*").from(TestDomain.class).where("name", like("yano%")).limit(10);
    }

    @Then("it should generate 'limit 10' after where expression")
    public void assertLimitOnWhereExp() {
        assertThat(exp.describe(), Matchers.is("select * from `test-domain` where `name` like 'yano%' limit 10"));
    }

    @When("limit(10) for DomainExpression")
    public void callLimitAgainstDomainExp() {
        exp = new Select(ctx.getSimpleDb(), ctx.getConfiguration(), "*").from(TestDomain.class).limit(10);
    }

    @Then("it should generate 'limit 10' after domain expression")
    public void assertLimitOnDomainExp() {
        assertThat(exp.describe(), Matchers.is("select * from `test-domain` limit 10"));
    }
}
