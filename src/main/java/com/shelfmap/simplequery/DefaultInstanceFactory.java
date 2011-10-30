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
package com.shelfmap.simplequery;

import com.shelfmap.simplequery.domain.Domain;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultInstanceFactory<T> implements DomainInstanceFactory<T> {
    private Context context;
    private Domain<T> domain;

    public DefaultInstanceFactory(Context context, Domain<T> domain) {
        this.context = context;
        this.domain = domain;
    }

    public Context getContext() {
        return context;
    }

    public Domain<T> getDomain() {
        return domain;
    }

    @Override
    public T create() {
        try {
            return getDomain().getDomainClass().newInstance();
        } catch (InstantiationException ex) {
            throw new IllegalStateException("Could not instanciate the class: " + domain, ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not access to the default constructor of: " + domain, ex);
        }
    }

}
