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
package com.shelfmap.simplequery.expression.impl;

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.expression.QueryResults;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author Tsutomu YANO
 */
public class InstanceQueryResult<T> implements QueryResults<T> {

    private final Collection<? extends T> values;
    private final Context context;

    public InstanceQueryResult(Context context, Collection<? extends T> values) {
        this.context = context;
        this.values = values;
    }

    protected Collection<? extends T> getValues() {
        return Collections.unmodifiableCollection(values);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return (Iterator<T>) values.iterator();
    }

    @Override
    public Context getContext() {
        return context;
    }

}
