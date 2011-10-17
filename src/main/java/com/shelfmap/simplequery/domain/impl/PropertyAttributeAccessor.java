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
package com.shelfmap.simplequery.domain.impl;

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import com.shelfmap.simplequery.DomainInstanceFactory;
import static com.shelfmap.simplequery.util.Assertion.*;
import com.shelfmap.simplequery.domain.AttributeAccessor;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.util.Objects;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a implementation-class for AttributeAccessor interface.
 * this implementation uses reflection for accessing an attribute of a domain object.
 * @author Tsutomu YANO
 */
public class PropertyAttributeAccessor<T> implements AttributeAccessor<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyAttributeAccessor.class);
    private final String propertyPath;
    private final Context context;

    public PropertyAttributeAccessor(Context context, String propertyPath) {
        isNotEmpty("propertyPath", propertyPath);
        isNotNull("context", context);
        this.propertyPath = propertyPath;
        this.context = context;
    }

    @Override
    public void write(Object instance, T value) {
        isNotNull("instance", instance);
        LOGGER.trace("propertyPath = " + propertyPath);

        String[] paths = propertyPath.split("\\.");
        int pathSize = paths.length;
        Object target = instance;
        String path = "";
        try {
            int i = 0;
            for (; i < (pathSize - 1); i++) {
                path = paths[i];
                PropertyDescriptor descriptor = findPropertyDescriptor(target, path);
                Method readMethod = descriptor.getReadMethod();
                Object current = target;
                target = readMethod.invoke(current, new Object[]{});

                //if a property's value is null, we must create a new instance for the property automatically
                //and put it into the property.
                if(target == null) {
                    target = generateNewPropertyValueForNullProperty(descriptor, current);
                }
            }
            path = paths[pathSize - 1];
            PropertyDescriptor descriptor = findPropertyDescriptor(target, path);
            Method writeMethod = descriptor.getWriteMethod();
            writeMethod.invoke(target, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not access to a property of a class for a security reason. propertyName: " + path + ", class: " + target.getClass().getCanonicalName() + ", full property-path: " + propertyPath, ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException("An exception has been thrown by accessing a property on the class. propertyName: " + path + ", class: " + target.getClass().getCanonicalName() + ", full property-path: " + propertyPath, ex);
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Could not introspect a bean. the class of the target bean is: " + target.getClass().getCanonicalName(), ex);
        } catch (PropertyNotFoundException ex) {
            throw new IllegalArgumentException("the property '" + path + "' is not found in class = " + target.getClass().getCanonicalName() + ". The full property-path: " + propertyPath, ex);
        }
    }

    private Object generateNewPropertyValueForNullProperty(PropertyDescriptor descriptor, Object current) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
        Object target;
        Class<?> propertyType = descriptor.getPropertyType();
        SimpleDbDomain annotation = Objects.findAnnotation(propertyType, SimpleDbDomain.class);

        Object newInstance = annotation != null
                ? newDomainInstance(propertyType)
                : newObjectInstance(propertyType);
        Method writeMethod = descriptor.getWriteMethod();
        writeMethod.invoke(current, newInstance);
        target = newInstance;
        return target;
    }

    private <T> T newDomainInstance(Class<T> propertyType) {
        DomainFactory factory = getContext().getDomainFactory();
        Domain<T> domain = factory.createDomain(propertyType);
        DomainInstanceFactory<T> instanceFactory = getContext().getDomainInstanceFactory(domain);
        return instanceFactory.create(domain);
    }

    private <T> T newObjectInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(new Class<?>[0]);
            return constructor.newInstance(new Object[0]);
        } catch (InstantiationException ex) {
            throw new IllegalStateException("Could not instanciate an instance for the class: " + clazz.getCanonicalName(), ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not access the default constructor of the class: " + clazz.getCanonicalName(), ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Could not invoke the default constructor of the class: " + clazz.getCanonicalName(), ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException("the default constructor of the class (" + clazz.getCanonicalName() + ") threw an exception.", ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("a property through the passed property-path was null, but the class of the property do not have a default constructor. So we could not create a new instance for the null property.", ex);
        } catch (SecurityException ex) {
            throw new IllegalStateException("Could not access to the default constructor of the class (" + clazz.getCanonicalName() + ") for any security reason.", ex);
        }
    }

    private PropertyDescriptor findPropertyDescriptor(Object target, String path) throws IntrospectionException, PropertyNotFoundException {
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(target.getClass()).getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(path)) {
                return descriptor;
            }
        }
        throw new PropertyNotFoundException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T read(Object instance) {
        isNotNull("instance", instance);
        String[] paths = propertyPath.split("\\.");
        int pathSize = paths.length;
        Object target = instance;
        String path = "";
        try {
            for (int i = 0; i < pathSize; i++) {
                path = paths[i];
                PropertyDescriptor descriptor = findPropertyDescriptor(target, path);
                Method readMethod = descriptor.getReadMethod();
                target = readMethod.invoke(target, new Object[]{});
            }
            return (T) target;

        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not access to a property of a class for a security reason. propertyName: " + path + ", class: " + target.getClass().getCanonicalName() + ", full property-path: " + propertyPath, ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException("An exception has been thrown by accessing a property on the class. propertyName: " + path + ", class: " + target.getClass().getCanonicalName() + ", full property-path: " + propertyPath, ex);
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Could not introspect a bean. the class of the target bean is: " + target.getClass().getCanonicalName(), ex);
        } catch (PropertyNotFoundException ex) {
            return null;
        }
    }

    public Context getContext() {
        return context;
    }
}
