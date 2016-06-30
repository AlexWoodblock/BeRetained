package com.woodblockwithoutco.beretained;


import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.woodblockwithouco.beretained.Retain;

public class MainActivity extends AppCompatActivity {

    @Retain
    int[] mRetainMe;

    @Retain
    Task mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!MainActivityFieldsRetainer.restore(this)) {
            android.util.Log.d("woodblock", "Nothing was retained");
            mRetainMe = new int[] {
              0, 1, 2, 3, 4, 5, 6
            };
        }

        if(mTask == null) {
            mTask = new Task();
            mTask.execute();
        }

        android.util.Log.d("woodblock", "Hash code " + System.identityHashCode(mRetainMe));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MainActivityFieldsRetainer.save(this);
    }

    static class Task extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while(!isCancelled()) {
                android.util.Log.d("woodblock", "Reporting instance: " + System.identityHashCode(this));
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }
    }
}
