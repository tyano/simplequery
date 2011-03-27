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

import org.junit.runner.RunWith
import impl.Select
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class OrderByExpressionSpec extends FlatSpec with ShouldMatchers with AWSSecurityCredential with ConfigurationAware {
  val simpleDB = getAmazonSimpleDB;
  val orderByExp = new Select(simpleDB, getConfiguration, "*") from (classOf[TestDomain]) orderBy ("name", SortOrder.Asc);
  
  "describe() method of OrderByExpression" should "generate encoded attribute's name" in {
    orderByExp.describe should be === "select * from test-domain order by `name` asc"
  }
}
