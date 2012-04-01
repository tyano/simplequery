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

import java.io.Serializable;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class BaseCachedObject implements CachedObject, Serializable {
    private static final long serialVersionUID = 1L;

    private Object object;

    public BaseCachedObject(Object object) {
        this.object = object;
    }

    @Override
    public Object getObject() {
        return this.object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseCachedObject other = (BaseCachedObject) obj;
        if (this.object != other.object && (this.object == null || !this.object.equals(other.object))) {
            return false;
        }
        if (this.getObjectType() != other.getObjectType()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.object != null ? this.object.hashCode() : 0);
        hash = 97 * hash + this.getObjectType().hashCode();
        return hash;
    }
}
