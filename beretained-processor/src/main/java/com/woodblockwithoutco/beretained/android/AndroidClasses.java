package com.woodblockwithoutco.beretained.android;

import com.squareup.javapoet.ClassName;

/**
 * Created by aleksandr on 6/11/16.
 */
public final class AndroidClasses {

    private AndroidClasses() {}

    public static final String ANDROID_SUPPORT_V4_APP_FRAGMENT_ACTIVITY_CLASS_NAME = "android.support.v4.app.FragmentActivity";

    public static final ClassName ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS = ClassName.get("android.support.v4.app", "FragmentManager");
    public static final ClassName ANDROID_SUPPORT_V4_APP_FRAGMENT_CLASS = ClassName.get("android.support.v4.app", "Fragment");
    public static final ClassName ANDROID_OS_BUNDLE_CLASS = ClassName.get("android.os", "Bundle");
}
