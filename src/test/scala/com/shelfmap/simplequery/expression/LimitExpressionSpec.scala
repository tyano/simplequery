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

package com.shelfmap.simplequery.expression

import com.shelfmap.simplequery.expression.impl.Select
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.shelfmap.simplequery.expression.Conditions._
import com.shelfmap.simplequery.expression.MatcherFactory._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class LimitExpressionSpec extends FlatSpec with ShouldMatchers with AWSSecurityCredential with ConfigurationAware {
  val simpleDB = getAmazonSimpleDB
  val configuration = getConfiguration
  val limitExpression = new Select(simpleDB, configuration, "*").from(classOf[TestDomain]).where("name", like("yano%")).orderBy("name", SortOrder.Asc).limit(10)

  "limit() for OrderByExpression" should "generate 'limit 10'" in {
    val expression = new Select(simpleDB, configuration, "*").from(classOf[TestDomain]).where("name", like("yano%")).orderBy("name", SortOrder.Asc).limit(10)
    expression.describe should be === "select * from `test-domain` where `name` like 'yano%' order by `name` asc limit 10"
  }
  
  "limit() for WhereExpression" should "generate 'limit 10' after where expression" in {
    val expression = new Select(simpleDB, configuration, "*").from(classOf[TestDomain]).where("name", like("yano%")).limit(10)
    expression.describe should be === "select * from `test-domain` where `name` like 'yano%' limit 10"
  }
  
  "limit() for DomainExpression" should "generate 'limit 10' after domain expression" in {
    val expression = new Select(simpleDB, configuration, "*").from(classOf[TestDomain]).limit(10)
    expression.describe should be === "select * from `test-domain` limit 10"
  }
}
