package io.okhi.okhiverification;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.okhi.android_okverify.OkVerify;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OkVerify.ping(getApplicationContext());
    }
}
