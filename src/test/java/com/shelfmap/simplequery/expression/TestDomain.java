package com.shelfmap.simplequery.expression;

import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.SimpleDBAttribute;

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

/**
 *
 * @author Tsutomu YANO
 */
@Domain("test-domain")
public interface TestDomain {
    String getName();
    
    @SimpleDBAttribute(maxDigitLeft=3)
    int getAge();
}