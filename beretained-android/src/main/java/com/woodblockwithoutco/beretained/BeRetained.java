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

/**
 * Class that contains static helper methods to handle retaining of
 * non-Parcelable objects via retained Fragments.
 *
 * As this class is using retained fragments, please understand that
 * retained objects will only survive configuration changes - in low memory conditions
 * or some other conditions where Activity state will be parcelled retained Fragments will be destroyed, and
 * thus retained objects will also be destroyed.
 *
 * Typical usecase should be the following:
 *
 * <pre>
 *     {@code
 *
 *     &#064;Override
 *     public void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         BeRetained.onCreate(this);
 *         BeRetained.restore(this);
 *
 *         //check if you have all necessary objects after restoration, if not - recreate them from scratch
 *             ...
 *         }
 *
 *     &#064;Override
 *     public void onSaveInstanceState(Bundle outState) {
 *         super.onSaveInstanceState(outState);
 *         BeRetained.save(this);
 *     }
 *
 *     }
 * </pre>
 */
public final class BeRetained {

    private final static String JAVA_PACKAGE = "java";
    private final static String ANDROID_PACKAGE = "android";

    /**
     * Suffix for classes which will contain static methods to trigger saving/restoring.
     */
    private static final String FIELDS_RETAINER_SUFFIX = "FieldsRetainer";

    private static final String FIELDS_RETAINER_PACKAGE = "com.woodblockwithoutco.beretained";

    private final static Map<Class<?>, FieldsRetainer<?>> FIELDS_RETAINER_MAP = new HashMap<>();

    private BeRetained() {}

    /**
     * Determines if this activity already has retained fragment that stores instances attached, and if no, attaches it.
     * Call this method as early as possible.
     *
     * Take note that this method also calls executePendingTransactions(), and that's why it's better to perform
     * all fragment manipulations after this method have been called.
     * @param activity Activity that will have it's assigned retained fragment attached to it.
     */
    public static void onCreate(FragmentActivity activity) {
        FieldsRetainer<FragmentActivity> retainer = findFieldsRetainer(activity.getClass());
        if(retainer != null) {
            retainer.onCreate(activity);
        }
    }

    /**
     * Saves instances of objects for fields marked with @Retain.
     * @param source Activity that wants to save it's fields.
     * @throws NullPointerException Will throw NullPointerException if field marked with @NonNull and @Retain was null during this call.
     */
    public static void save(FragmentActivity source) {
        FieldsRetainer<FragmentActivity> retainer = findFieldsRetainer(source.getClass());
        if(retainer != null) {
            retainer.save(source);
        }
    }

    /**
     * Restores instances to fields marked with @Retain. Will not do anything if BeRetained.save() was not called before.
     * @param target Activity to restore instances to.
     * @return true if there are instances to restore, false otherwise.
     * @throws NullPointerException Will throw NullPointerException if saved instance for field marked with @NonNull and @Retain was null during this call.
     */
    public static boolean restore(FragmentActivity target) {
        FieldsRetainer<FragmentActivity> retainer = findFieldsRetainer(target.getClass());
        if(retainer != null) {
            return retainer.restore(target);
        } else {
            return false;
        }
    }

    private static FieldsRetainer<FragmentActivity> findFieldsRetainer(Class<? extends FragmentActivity> clazz) {
        FieldsRetainer<FragmentActivity> retainer = (FieldsRetainer<FragmentActivity>) FIELDS_RETAINER_MAP.get(clazz);

        if(retainer == null) {
            try {
                Class<?> retainEnabledClass = clazz;
                Class<?> retainerClass = getRetainerClass(retainEnabledClass);
                //while we can't find the retainer class and while we haven't reached root
                while (retainerClass == null && retainEnabledClass != null) {
                    retainEnabledClass = getParentClass(retainEnabledClass);
                    if(retainEnabledClass != null) {
                        retainerClass = getRetainerClass(retainEnabledClass);
                    }
                }

                if(retainerClass != null) {
                    retainer = (FieldsRetainer<FragmentActivity>) retainerClass.getDeclaredConstructor().newInstance();
                } else {
                    return null;
                }
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

        if(retainer != null) {
            FIELDS_RETAINER_MAP.put(clazz, retainer);
        }

        return retainer;
    }

    private static Class<?> getParentClass(Class<?> clazz) {
        if(clazz.getName().startsWith(ANDROID_PACKAGE) || clazz.getName().startsWith(JAVA_PACKAGE)) {
            //we climbed up to root objects, let's stop it here
            return null;
        }

        return clazz.getSuperclass();
    }

    private static Class<?> getRetainerClass(Class<?> clazz) {
        try {
            return Class.forName(FIELDS_RETAINER_PACKAGE + "." + clazz.getSimpleName() + FIELDS_RETAINER_SUFFIX);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
