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

package com.shelfmap.simplequery.expression;

import com.shelfmap.simplequery.expression.matcher.BetweenMatcher;
import com.shelfmap.simplequery.expression.matcher.GreaterEqualMatcher;
import com.shelfmap.simplequery.expression.matcher.GreaterThanMatcher;
import com.shelfmap.simplequery.expression.matcher.InMatcher;
import com.shelfmap.simplequery.expression.matcher.IsMatcher;
import com.shelfmap.simplequery.expression.matcher.IsNotMatcher;
import com.shelfmap.simplequery.expression.matcher.LessEqualMatcher;
import com.shelfmap.simplequery.expression.matcher.LessThanMatcher;
import com.shelfmap.simplequery.expression.matcher.LikeMatcher;
import com.shelfmap.simplequery.expression.matcher.NotLikeMatcher;

/**
 *
 * @author Tsutomu YANO
 */
public final class MatcherFactory {
    private MatcherFactory() {
        super();
    }
    
    public static <T> IsMatcher<T> is(T value) {
        return new IsMatcher<T>(value);
    }

    public static <T> IsNotMatcher<T> isNot(T value) {
        return new IsNotMatcher<T>(value);
    }
    
    public static <T> GreaterEqualMatcher<T> greaterEqual(T value) {
        return new GreaterEqualMatcher<T>(value);
    }
    
    public static <T> GreaterThanMatcher<T> greaterThan(T value) {
        return new GreaterThanMatcher<T>(value);
    }
    
    public static <T> LessEqualMatcher<T> lessEqual(T value) {
        return new LessEqualMatcher<T>(value);
    }
    
    public static <T> LessThanMatcher<T> lessThan(T value) {
        return new LessThanMatcher<T>(value);
    }
    
    public static <T> LikeMatcher<T> like(T value) {
        return new LikeMatcher<T>(value);
    }
    
    public static <T> NotLikeMatcher<T> notLike(T value) {
        return new NotLikeMatcher<T>(value);
    }
    
    public static <T> BetweenMatcher<T> between(T value) {
        return new BetweenMatcher<T>(value);
    }
    
    public static <T> InMatcher<T> in(T... values) {
        return new InMatcher<T>(values);
    }
}
