/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.shelfmap.simplequery.factory.impl;

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.expression.ItemConverter;
import com.shelfmap.simplequery.expression.impl.DefaultItemConverter;
import com.shelfmap.simplequery.factory.ItemConverterFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultItemConverterFactory implements ItemConverterFactory {
    private Context context;

    public DefaultItemConverterFactory(Context context) {
        this.context = context;
    }

    @Override
    public <T> ItemConverter<T> create(Domain<T> domain) {
        return new DefaultItemConverter<T>(getContext(), domain);
    }

    @Override
    public Context getContext() {
        return this.context;
    }
}
