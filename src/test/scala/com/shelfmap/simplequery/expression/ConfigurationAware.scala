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

import _root_.com.shelfmap.simplequery.{Configuration, InstanceFactory, Domain}
import _root_.com.shelfmap.simplequery.domain.DomainAttributes
import _root_.com.shelfmap.simplequery.domain.impl.BeanDomainAttributes
import impl.DefaultItemConverter

trait ConfigurationAware {
  def getConfiguration: Configuration = new DefaultConfiguration
}

class DefaultConfiguration extends Configuration {
  override def getItemConverter[T](domainClass: Class[T], domainName: String): ItemConverter[T] = {
    val annotation = domainClass.getAnnotation(classOf[Domain])
    new DefaultItemConverter[T](domainClass, annotation.value(), this)
  }
  
  override def getInstanceFactory[T](domainClass: Class[T], domainName: String): InstanceFactory[T] = {
    new InstanceFactory[T] {
      def createInstance(domainClass: Class[T]): T = domainClass.newInstance
    }
  }  
  
  override def getDomainAttributes(domainClass: Class[_], domainName: String): DomainAttributes = {
    new BeanDomainAttributes(domainClass, domainName, this)
  }
}
