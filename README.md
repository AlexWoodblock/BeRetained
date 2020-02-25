[ ![Download](https://api.bintray.com/packages/drbreen/maven/com.woodblockwithoutco.beretained-processor/images/download.svg) ](https://bintray.com/drbreen/maven/com.woodblockwithoutco.beretained-processor/_latestVersion)

# This is, of course, is now deprecated as there are much better ways to retain your state on config changes (ViewModels, for example). However, it is a good starting point for writing your annotation processor.

# BeRetained
BeRetained is a simple library that will handle non-parcelable instances saving for configuration changes.

This library basically lets you keep objects alive for as long as one "screen"(i.e. for as long as user doesn't leave the screen by pressing back or by leaving it in the background for too long) is alive. This may be useful, for example, if you want to keep your Presenters alive. 

**Important:** Please note that while objects will survive configuration changes, they will not survive Activity destruction due to low memory conditions, so you always need to check if objects were restored and recreate them from scratch if necessary.

# Examples:
While examples may also be found in this very repository, the core usage generally should look like this:
```
public class SampleActivity extends FragmentActivity {

    @Retain
    Object mObjectToRetain;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    
        BeRetained.onCreate(this);
        BeRetained.restore(this);
        //check if you have all necessary objects after restoration, if not - recreate them from scratch
        …
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        BeRetained.save(this);
    }
}
```

If you have subclass of Activity that have @Retain fields in it, you don't need to anything extra - just subclass it:
```
public class SampleActivity extends FragmentActivity {

    @Retain
    Object mObjectToRetain;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    
        BeRetained.onCreate(this);
        BeRetained.restore(this);
        //check if you have all necessary objects after restoration, if not - recreate them from scratch
        …
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        BeRetained.save(this);
    }
}

public class SubclassSampleActivity extends SampleActivity {

    @Retain
    Object mSecondObject;

}
```

And that's it! Both objects from superclass and inherited class will be retained.

#Installation
Add apt plugin dependency to your project-level ```build.gradle```:
```
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```

Apply apt plugin - add the following line to the top of your module-level ```build.gradle```:
```
apply plugin: 'com.neenbedankt.android-apt'
```

Then add compile dependency for Android bridge to generated classes
and apt dependency for annotations processor, and you're all set.
```
dependencies {
    compile 'com.woodblockwithoutco:beretained:x.y.z'
    apt 'com.woodblockwithoutco:beretained-processor:x.y.z'
}
```

x.y.z is the latest version available - check it on the top of README.
