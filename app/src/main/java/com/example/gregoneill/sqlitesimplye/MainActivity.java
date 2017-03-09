package com.example.gregoneill.sqlitesimplye;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import java.util.logging.Logger;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testNetworkQueue();
    }

    private void testNetworkQueue() {

        NYPLRequestQueue queue = new NYPLRequestQueue(this);

        //Create fake data for requests
        int libraryID = 0;
        String bookIdentifier = "urn:librarysimplified.org/terms/id/Overdrive%20ID/d9602b23-fe3e-461e-a310-dc60e6176483";
        String annotationsURL = "https://circulation.librarysimplified.org/annotations/";
        String analyticsURL = "https://circulation.librarysimplified.org/analytics/Overdrive/Overdrive%20ID/d9602b23-fe3e-461e-a310-dc60e6176483/open_book";
        String updateID = "urn:librarysimplified.org/terms/id/Overdrive%20ID/d9602b23-fe3e-461e-a310-dc60e6176483";

        String body = "{\n" +
                "    \"@context\": \"http://www.w3.org/ns/anno.jsonld\",\n" +
                "    \"motivation\": \"http://librarysimplified.org/terms/annotation/idling\",\n" +
                "    \"target\":     {\n" +
                "        \"selector\":         {\n" +
                "            \"type\": \"oa:FragmentSelector\",\n" +
                "            \"value\": \"{\\\"idref\\\":\\\"cover\\\",\\\"contentCFI\\\":\\\"/4/2[cov]/2/2\\\"}\"\n" +
                "        },\n" +
                "        \"source\": \"urn:librarysimplified.org/terms/id/Overdrive%20ID/8cddd0b7-c6cd-4f04-9ee2-3655b051a64c\"\n" +
                "    },\n" +
                "    \"type\": \"Annotation\"\n" +
                "}";


        //Add them all to the SQLiteQueue

//        queue.addRequest(0, null, annotationsURL, 0, body, null);
//        queue.addRequest(1, null, annotationsURL, 0, body, null);
//        queue.addRequest(2, null, annotationsURL, 0, body, null);
//        queue.addRequest(3, null, annotationsURL, 0, body, null);

        //Make sure new rows are added
        queue.addRequest(0, null, analyticsURL, 0, null, null);
//        queue.addRequest(0, null, analyticsURL, 0, null, null);
//        queue.addRequest(0, null, analyticsURL, 0, null, null);
//        queue.addRequest(0, null, analyticsURL, 0, null, null);
//        queue.addRequest(0, null, analyticsURL, 0, null, null);
//
//        //Add requests that should update a row instead of inserting a new one
//        queue.addRequest(0, updateID, analyticsURL, 0, body, null);
//        queue.addRequest(0, updateID, analyticsURL, 0, body, null);
//        queue.addRequest(0, updateID, analyticsURL, 0, body, null); //Should Update Row twice
//        queue.addRequest(1, updateID, analyticsURL, 0, body, null); //Should Add new row

        //Add Requests with a POST instead of GET

        //Attempt to retry queue
        queue.retryQueue();

        //If a retry is successful, make sure it is removed from the queue

        //If a retry is not successful, make sure it remains in the queue, with the retry count incremented

        //Once a retry has reached its count limit, make sure it's removed from the queue
    }
}
