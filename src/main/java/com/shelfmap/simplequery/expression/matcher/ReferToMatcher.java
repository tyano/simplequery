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
package com.shelfmap.simplequery.expression.matcher;

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.domain.AttributeConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class ReferToMatcher extends BaseMatcher<String> {
    public ReferToMatcher(String... values) {
        super(values);
    }

    protected ReferToMatcher(AttributeConverter<String> attributeConverter, String... values) {
        super(attributeConverter, values);
    }

    @Override
    protected BaseMatcher<String> newMatcher(AttributeConverter<String> attributeConverter, String... values) {
        return new ReferToMatcher(attributeConverter, values);
    }

    @Override
    protected String expression() {
        return getValues().size() > 1 ? "in" : "=";
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression());
        if(getValues().size() > 1) {
            sb.append(" (");
            sb.append(SimpleDBUtils.quoteValues(getValues()));
            sb.append(")");            
        } else {
            sb.append(" ");
            sb.append(SimpleDBUtils.quoteValue(values()[0]));
        }
        return sb.toString();
    }
}
