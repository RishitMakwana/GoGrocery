package com.example.gogrocery.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;

import com.example.gogrocery.R;

public class MainActivity extends AppCompatActivity {

    private Button Go,Btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Go = (Button) findViewById(R.id.sellerBtn);
        Btn = (Button) findViewById(R.id.button);
        Go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, MainSellerActivity.class));
            }
        });

        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this,MainUserActivity.class);
                //startActivity(intent);
            }
        });
    }
}