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
package com.woodblockwithoutco.beretained.android;

import com.squareup.javapoet.ClassName;

/**
 * Stores ready-to-use references to classes and their names for JavaPoet and for other tasks,
 * like checking if object inherits from specific class.
 */
public final class AndroidClasses {

    private AndroidClasses() {}

    public static final String ANDROID_SUPPORT_V4_APP_FRAGMENT_ACTIVITY_CLASS_NAME = "android.support.v4.app.FragmentActivity";

    public static final String ANDROID_SUPPORT_ANNOTATION_NON_NULL = "@android.support.annotation.NonNull";

    public static final ClassName ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS = ClassName.get("android.support.v4.app", "FragmentManager");
    public static final ClassName ANDROID_SUPPORT_V4_APP_FRAGMENT_CLASS = ClassName.get("android.support.v4.app", "Fragment");
    public static final ClassName ANDROID_OS_BUNDLE_CLASS = ClassName.get("android.os", "Bundle");
    public static final ClassName COM_WOODBLOCKWITHOUTCO_BERETAINED_FIELDS_RETAINER = ClassName.get("com.woodblockwithoutco.beretained", "FieldsRetainer");
}
