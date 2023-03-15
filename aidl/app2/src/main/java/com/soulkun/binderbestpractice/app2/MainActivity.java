package com.soulkun.binderbestpractice.app2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.soulkun.binderbestpractice.app2.databinding.ActivityMainBinding;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mMainBinding.getRoot());
        startService(new Intent(this, InterapplicationService.class));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, InterapplicationService.class));
        super.onDestroy();
    }

}