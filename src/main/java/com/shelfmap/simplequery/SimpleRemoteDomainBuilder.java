/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.shelfmap.simplequery.domain.Domain;
import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Tsutomu YANO
 */
public class SimpleRemoteDomainBuilder implements RemoteDomainBuilder, Serializable {
    private static final long serialVersionUID = 1L;
    
    private Context context;
    private final List<Domain<?>> domains = new ArrayList<Domain<?>>();
    private final Set<String> builtSet = new HashSet<String>();

    public SimpleRemoteDomainBuilder(Context context) {
        this.context = context;
    }

    @Override
    public void build() {
        AmazonSimpleDB simpleDB = getContext().getSimpleDB();
        Iterator<Domain<?>> iterator = this.domains.iterator();
        while(iterator.hasNext()) {
            Domain<?> domain = iterator.next();
            String domainName = domain.getDomainName();
            simpleDB.createDomain(new CreateDomainRequest(domainName));
            this.builtSet.add(domainName);
            iterator.remove();
        }
    }

    @Override
    public void add(Domain<?>... domains) {
        this.domains.addAll(Arrays.asList(domains));
    }

    public Context getContext() {
        return context;
    }

    protected void clear() {
        this.domains.clear();
    }

    protected List<Domain<?>> getDomains() {
        return new ArrayList<Domain<?>>(this.domains);
    }

    @Override
    public boolean isBuilt(Domain<?> domain) {
        return this.builtSet.contains(domain.getDomainName());
    }
}
