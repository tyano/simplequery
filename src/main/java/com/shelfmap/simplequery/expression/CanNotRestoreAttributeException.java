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

/**
 *
 * @author Tsutomu YANO
 */
public class CanNotRestoreAttributeException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String attributeValue;
    private final Class<?> targetClass;

    public CanNotRestoreAttributeException(Throwable thrwbl, String attributeValue, Class<?> targetClass) {
        super(thrwbl);
        this.attributeValue = attributeValue;
        this.targetClass = targetClass;
    }

    public CanNotRestoreAttributeException(String string, Throwable thrwbl, String attributeValue, Class<?> targetClass) {
        super(string, thrwbl);
        this.attributeValue = attributeValue;
        this.targetClass = targetClass;
    }

    public CanNotRestoreAttributeException(String string, String attributeValue, Class<?> targetClass) {
        super(string);
        this.attributeValue = attributeValue;
        this.targetClass = targetClass;
    }

    public CanNotRestoreAttributeException(String attributeValue, Class<?> targetClass) {
        this.attributeValue = attributeValue;
        this.targetClass = targetClass;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
