/*
 * Copyright 2012 Tsutomu YANO.
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

/**
 *
 * @author Tsutomu YANO
 */
public class SimpleClassReference implements ClassReference {
    private static final long serialVersionUID = 1L;

    private final String className;

    public SimpleClassReference(Class<?> clazz) {
        this.className = (clazz == null ? null : clazz.getName());
    }

    @Override
    public Class<?> get() {
        if(className == null) return null;
        
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("No such class in current context classloader.", ex);
        }
        return clazz;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleClassReference other = (SimpleClassReference) obj;
        if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.className != null ? this.className.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "SimpleClassReference{" + "className=" + className + '}';
    }
}
