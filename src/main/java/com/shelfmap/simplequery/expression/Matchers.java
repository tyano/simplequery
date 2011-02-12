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
import com.shelfmap.simplequery.expression.matcher.LesserEqualMatcher;
import com.shelfmap.simplequery.expression.matcher.LesserThanMatcher;
import com.shelfmap.simplequery.expression.matcher.LikeMatcher;
import com.shelfmap.simplequery.expression.matcher.NotLikeMatcher;

/**
 *
 * @author Tsutomu YANO
 */
public final class Matchers {
    private Matchers() {
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
    
    public static <T> LesserEqualMatcher<T> lesserEqual(T value) {
        return new LesserEqualMatcher<T>(value);
    }
    
    public static <T> LesserThanMatcher<T> lesserThan(T value) {
        return new LesserThanMatcher<T>(value);
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
