package com.example.gregoneill.sqlitesimplye;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testNetworkQueue();
    }

    //If row to add has same updateID, update row, else
    //add new row.
    //RetryQueue should iterate and remove all rows in the table
    private void testNetworkQueue() {

        NetworkQueue queue = new NetworkQueue(this);

        
    }
}
