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

/**
 * Test class for 'getResults()' and 'getSingleResult()' of the BaseExpression class.
 */
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BaseExpressionSpecs extends FlatSpec with ShouldMatchers with AWSSecurityCredential with ConfigurationAware {
  val simpleDB = getAmazonSimpleDB
  val configuration = getConfiguration
  
  //create a domain into Amazon SimpleDB for testing.
  
  //tests.
  
}
