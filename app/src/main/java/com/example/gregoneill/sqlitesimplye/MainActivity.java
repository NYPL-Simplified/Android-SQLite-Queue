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

    private void testNetworkQueue() {

        NetworkQueue queue = new NetworkQueue(this);

        //Create several fake requests
        //Add them all to the SQLiteQueue
        //Make sure new rows are added

        //Add several requests that should update a row instead of inserting a new one

        //Attempt to retry queue and make sure every request is retried

        //If a retry is successful, make sure it is removed from the queue
        //If a retry is not successful, make sure it remains in the queue, with the retry count incremented

        //Once a retry has reached its count limit, make sure it's removed from the queue
    }
}
