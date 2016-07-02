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
    
        SampleActivityFieldsRetainer.onCreate(this);
    
        boolean restored = SampleActivityFieldsRetainer.restore(this);
        if(!restored) {
            //create all the objects from scratch, as they either weren't saved or Activity was completely destroyed
            ...
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SampleActivityFieldsRetainer.save(this);
    }
}
```

**Please note:** during first run you will not have class SampleActivityFieldsRetainer. To get this file to be generated, you need to build your project once first. You also need to rebuild project every time you'll add new Activity with @Retain fields.
