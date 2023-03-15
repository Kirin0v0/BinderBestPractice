package com.soulkun.binderbestpractice.app1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ViewDataBinding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.soulkun.binderbestpractice.app1.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mActivityMainBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mActivityMainBinding.getRoot());
        mActivityMainBinding.btn1.setOnClickListener(v -> {
            startActivity(new Intent(this, InterprocessActivity.class));
        });
        mActivityMainBinding.btn2.setOnClickListener(v -> {
            startActivity(new Intent(this, InterapplicationActivity.class));
        });
    }

}