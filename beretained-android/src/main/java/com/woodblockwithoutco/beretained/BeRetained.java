/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package com.woodblockwithoutco.beretained;

import android.support.v4.app.FragmentActivity;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class BeRetained {

    /**
     * Suffix for classes which will contain static methods to trigger saving/restoring.
     */
    private static final String FIELDS_RETAINER_SUFFIX = "FieldsRetainer";

    private static final String FIELDS_RETAINER_PACKAGE = "com.woodblockwithoutco.beretained";

    private final static Map<Class<?>, FieldsRetainer<?>> FIELDS_RETAINER_MAP = new HashMap<>();

    private BeRetained() {}

    public static void onCreate(FragmentActivity activity) {
        FieldsRetainer<FragmentActivity> retainer = findFieldsRetainer(activity.getClass());
        retainer.onCreate(activity);
    }

    public static void save(FragmentActivity source) {
        FieldsRetainer<FragmentActivity> retainer = findFieldsRetainer(source.getClass());
        retainer.save(source);
    }

    public static boolean restore(FragmentActivity target) {
        FieldsRetainer<FragmentActivity> retainer = findFieldsRetainer(target.getClass());
        return retainer.restore(target);
    }

    private static FieldsRetainer<FragmentActivity> findFieldsRetainer(Class<? extends FragmentActivity> clazz) {
        FieldsRetainer<FragmentActivity> retainer = (FieldsRetainer<FragmentActivity>) FIELDS_RETAINER_MAP.get(clazz);

        if(retainer == null) {
            try {
                Class<?> retainerClass = Class.forName(FIELDS_RETAINER_PACKAGE + "." + clazz.getSimpleName() + FIELDS_RETAINER_SUFFIX);
                retainer = (FieldsRetainer<FragmentActivity>) retainerClass.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException e) {
                return null;
            } catch (NoSuchMethodException e) {
                return null;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            } catch (InvocationTargetException e) {
                return null;
            }
        }

        return retainer;
    }
}
