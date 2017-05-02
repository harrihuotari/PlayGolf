package com.example.android.playgolf;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.Locale;
import java.util.jar.Manifest;

import static android.R.id.message;
import static android.media.CamcorderProfile.get;
import static android.os.Build.VERSION_CODES.M;
import static com.example.android.playgolf.HoleListAdapter.REQUEST_CODE_1;
import static com.example.android.playgolf.MainActivity.disableDatabaseUpdates;
import static com.example.android.playgolf.MainActivity.playersRound;
import static com.example.android.playgolf.MainActivity.roundEvent;
import static com.example.android.playgolf.MainActivity.selectedCourse;
import static com.example.android.playgolf.MainActivity.selectedPlayer;
import static com.example.android.playgolf.R.layout.holes;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.security.AccessController.getContext;

/**
 * Created by Harri on 21.10.2016.
 */

public class HoleList extends AppCompatActivity {

    // public static ArrayList<Hole> holesStatic = new ArrayList<Hole>();
    public static ArrayList<Score> allHoles = new ArrayList<Score>();
    public static boolean makeFooter;
    private static int holeStoreCounter;
    private static final int PERMISSION_SEND_SMS = 123;
    public static String smsMessage = "";
    public static Resources.Theme theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theme = this.getTheme();
        setContentView(holes);

        // It this is coming back from the 1st view, for the 2nd time, then only adapter need to be
        // restarted, not setting it again from the start and losing earlier edited holes
        makeFooter = false;
        if (allHoles.size() < 2) {

            // Get holes of the selected course
            String urlString;
            urlString = "http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/Hole/" + String.valueOf(selectedCourse.getCourseId());
            new getAsyncHoleList().execute(urlString);

            // make footer only once
            makeFooter = true;
        }
    }

    private class getAsyncHoleList extends AsyncTask<String, Void, String> {

        // Fetch the list of courses from the data base

        // Initialize URL connection
        HttpURLConnection urlConnection = null;

        // Initialize a buffer for reading the results
        BufferedReader reader = null;

        // Player data to be parsed
        String outputDataForParsing = null;

        @Override
        protected String doInBackground(String... params) {

            // get the string for accessing the course over php
            String urlString = params[0];
            try {
                // Construct the URL for reading the list of players
                // String urlString = "http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/Player";
                URL url = new URL(urlString);

                // Create the request to database through php interface, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    urlConnection.disconnect();
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    urlConnection.disconnect();
                    return null;
                }
                outputDataForParsing = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            urlConnection.disconnect();
            return outputDataForParsing;
        }

        @Override
        protected void onPostExecute(String jsonString) {

            // use a separate method for parsing the holes
            parseJsonAndUpdateHoleList(jsonString);

            prepareListOfHolesInUI();
        }
    }

    private void parseJsonAndUpdateHoleList ( String jsonResponseString) {

        // Show the JSON got from the server

        JSONObject hole = null;
        JSONArray jsonDataArray;
        JSONObject inDataAsObject;
        int holeId;
        int holeNr;
        int holePar;
        int holeHCP;

        try {
            inDataAsObject = new JSONObject(jsonResponseString);
            jsonDataArray = inDataAsObject.getJSONArray("data");

            int i;
            for (i = 0; i < jsonDataArray.length(); i++) {

                // Read required fields from the object
                hole = jsonDataArray.getJSONObject(i);
                holeId = parseInt(hole.getString("Hid"));
                holeNr = parseInt(hole.getString("HoleNr"));
                holePar = parseInt(hole.getString("HolePar"));
                holeHCP = parseInt(hole.getString("HoleHCP"));
                allHoles.add(new Score(holeId, holeNr, holePar, holeHCP, holePar, holePar, 2, false, false, false));
            }
            Toast.makeText(this, "all holes added", Toast.LENGTH_SHORT);
        } catch (JSONException e) {
            Toast.makeText(this, "Could not parse json data", Toast.LENGTH_SHORT);
        }

        // set scores to match 2 bogey points for each hole according to handicap
        preFillScoresAccordingToOwnSlope();
    }

    private void prepareListOfHolesInUI() {
        // Create an {@link HoleListAdapter}, whose data source is a list of {@link Hole}s. The
        // adapter knows how to create list items for each item in the list.
        final HoleListAdapter adapter = new HoleListAdapter(this, android.R.layout.simple_list_item_1, allHoles);

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        // holes.xml layout file.
        ListView listView = (ListView) findViewById(R.id.list);

        if (makeFooter) {
            ViewGroup footerView = (ViewGroup) getLayoutInflater().inflate(R.layout.final_store_button_view, listView, false);
            listView.addFooterView(footerView);

            // Find the View that triggers exiting from the view
            Button storeButton = (Button) findViewById(R.id.final_store_button);

            // Set a click listener on that View
            storeButton.setOnClickListener(new View.OnClickListener() {
                // The code in this method will be executed when the store button is clicked.
                @Override
                public void onClick(View view) {

                    // store all the hole scores the user has inserted
                    if (!disableDatabaseUpdates) {
                        // send result also as a SMS
                        constructSMSMessageContents();
                        sendResultAsSMS();
                        // start storing the results to database one score by one. Indicate first hole with true
                        storeOneScore(true);
                    } else {
                        sendResultsAsEmail();
                        // Toast.makeText(getApplicationContext(), "email to be sent", Toast.LENGTH_SHORT).show();
                        sendResultAsSMS();
                        finish();
                    }
                }
            });
        }

        // Make the {@link ListView} use the {@link WordAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link Word} in the list.
        listView.setAdapter(adapter);
    }

    public void constructSMSMessageContents() {
        int i;
        int bogeyPointsPerHole;
        int totalBogeyPoints = 0;
        int totalScore = 0;
        int totalPutts = 0;
        int nrOfFairwayHits = 0;
        int nrOfGreenHits = 0;
        for (i=0; i < 18; i++) {

            // count here bogey points
            // 2 + HolePar - HoleScore + (Slope DIV 18) + (18 - HoleHCP + (Slope % 18)) DIV 18
            bogeyPointsPerHole = 2 + allHoles.get(i).getHolePar() - allHoles.get(i).getHoleScore() + (playersRound.getSlope() / 18) +
                    (18 - allHoles.get(i).getHoleHCP() + (playersRound.getSlope() % 18))/18;
            // bogey point is always 0 or positive
            if (bogeyPointsPerHole < 0) {
                bogeyPointsPerHole = 0;
            }
            // count fairway hits and green hits
            if (allHoles.get(i).getHoleDetailsFilledIn()) {
                if (allHoles.get(i).getFairwayHit()) {
                    nrOfFairwayHits++;
                }
                if (allHoles.get(i).getGreenHit()) {
                    nrOfGreenHits++;
                }
            }
            totalBogeyPoints += bogeyPointsPerHole;
            totalScore += allHoles.get(i).getHoleScore();
            totalPutts += allHoles.get(i).getPutts();
        }

        // get the date for the subject field of the email, and needed also in the SMS
        SimpleDateFormat eDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar c = Calendar.getInstance();
        String formattedDate = eDate.format(c.getTime());

        // make summary that is added after result table and returned back
        smsMessage = "PlayGolf: " + selectedPlayer.getFirstName() + " in " + selectedCourse.getAbbreviation() + ", " + formattedDate + "\n" +
                "Total score " + String.valueOf(totalScore) + ". " + "Bogey points " + String.valueOf(totalBogeyPoints) + ".\n" + "Putts " +
                String.valueOf(totalPutts) + ". " + "Fairway hits " + String.valueOf(nrOfFairwayHits) + ". " + "Green hits " + String.valueOf(nrOfGreenHits) + ".";

        // if better round than player's slope, then let's add a praise for that
        if (totalBogeyPoints > 36) {
            smsMessage += "\n" + "Great job " + selectedPlayer.getFirstName() + "!!";
        }
    }

    public void sendResultAsSMS() {

        String phoneNumber;
        phoneNumber = selectedPlayer.getMobileNr();

        if (!(phoneNumber == null)) {
            trySendingSMS();
        }
    }

    public void trySendingSMS() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS);
            } else {
                sendSMS(smsMessage);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSMS(smsMessage);
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    private void sendSMS(String msg) {
        PendingIntent sentPI;
        String SENT = "SMS_SENT";
        SmsManager smsManager = SmsManager.getDefault();
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        smsManager.sendTextMessage(selectedPlayer.getMobileNr(), null, msg, sentPI, null);
        Toast.makeText(getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT).show();
    }

    public void sendResultsAsEmail() {
        String resultMessage = "";
        String emailSubject = "";
        int i;
        int totalScore = 0;
        int totalPutts = 0;
        int nrOfFairwayHits = 0;
        int nrOfGreenHits = 0;
        int bogeyPointsPerHole = 0;
        int totalBogeyPoints = 0;
        resultMessage = "Hole  " + "Par    " + "Score  " + "HCP    " + "BP     " + "Putts   "+   "Fw    " + "Gr"  + "\n";
        for (i=0; i < 18; i++) {
            resultMessage += String.valueOf(allHoles.get(i).getHoleNumber()) + "       ";
            if (allHoles.get(i).getHoleNumber() < 10) {
                resultMessage += "  ";
            }
            resultMessage += String.valueOf(allHoles.get(i).getHolePar()) + "       ";
            resultMessage += String.valueOf(allHoles.get(i).getHoleScore()) + "         ";
            resultMessage += String.valueOf(allHoles.get(i).getHoleHCP()) + "        ";
            if (allHoles.get(i).getHoleHCP() < 10) {
                resultMessage += "  ";
            }

            // count here bogey points
            // 2 + HolePar - HoleScore + (Slope DIV 18) + (18 - HoleHCP + (Slope % 18)) DIV 18
            bogeyPointsPerHole = 2 + allHoles.get(i).getHolePar() - allHoles.get(i).getHoleScore() + (playersRound.getSlope() / 18) +
                    (18 - allHoles.get(i).getHoleHCP() + (playersRound.getSlope() % 18))/18;
            // bogey point is always 0 or positive
            if (bogeyPointsPerHole < 0) {
                bogeyPointsPerHole = 0;
            }
            totalBogeyPoints += bogeyPointsPerHole;
            resultMessage += String.valueOf(bogeyPointsPerHole) + "        ";
            if (allHoles.get(i).getHoleDetailsFilledIn()) {
                resultMessage += String.valueOf(allHoles.get(i).getPutts()) + "        ";
                if (allHoles.get(i).getFairwayHit()) {
                    resultMessage += "Fw    ";
                    nrOfFairwayHits++;
                }
                if (allHoles.get(i).getGreenHit()) {
                    nrOfGreenHits++;
                    if (!allHoles.get(i).getFairwayHit()) {
                        resultMessage += "         ";
                    }
                    resultMessage += "Gr";
                }
            }
            resultMessage += "\n";
            totalScore += allHoles.get(i).getHoleScore();
            totalPutts += allHoles.get(i).getPutts();
        }

        resultMessage += "\n";

        // get the date for the subject field of the email, and needed also in the SMS
        SimpleDateFormat eDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar c = Calendar.getInstance();
        String formattedDate = eDate.format(c.getTime());

        // make summary that is added after result table and returned back
        smsMessage = "PlayGolf: " + selectedPlayer.getFirstName() + " in " + selectedCourse.getAbbreviation() + ", " + formattedDate + "\n" +
                "Total score " + String.valueOf(totalScore) + ". " + "Bogey points " + String.valueOf(totalBogeyPoints) + ".\n" + "Putts " +
                String.valueOf(totalPutts) + ". " + "Fairway hits " + String.valueOf(nrOfFairwayHits) + ". " + "Green hits " + String.valueOf(nrOfGreenHits) + ".";
        if (totalBogeyPoints > 36) {
            smsMessage += "\n" + "Great job " + selectedPlayer.getFirstName() + "!!";
        }

        // add total score, total putts, nr or fairway hits and nr of green hits to message
        resultMessage += smsMessage;

        // add weather information to the email
        resultMessage += "\n" + "The weather: temperature " + roundEvent.getTemperature() + ", wind " +
                roundEvent.getWind() + roundEvent.getWindDirection() + " and " + roundEvent.getClouds() + ".\n";

        emailSubject = "PlayGolf: " + selectedPlayer.getFirstName() + " in " + selectedCourse.getAbbreviation() + ", " + formattedDate;
        composeEmail(resultMessage, emailSubject, selectedPlayer.getEmail());
    }

    public void composeEmail(String messageText, String subject, String eMailAddress) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        if (!(eMailAddress == null)) {
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{eMailAddress});
        }
        intent.putExtra(Intent.EXTRA_TEXT, messageText);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void preFillScoresAccordingToOwnSlope() {

        // The purpose here is to use information of selected course (slope and CR), handicap of the
        // selected player, and par & HCP information of each hole so that list of scores is pre-filled
        // with the score that corresponds 2 bogey points for the player in each particular hole

        double slope;
        int finalSlope;
        int ownParScore;
        int i;
        Score s = new Score(1, 1, 4, 9, 4, 4, 2, false, false, false);
        for (i=0; i < allHoles.size(); i++) {
            s = allHoles.get(i);

            // Slope is an integer, counted individually for each player according to his/her handicap and difficulty of the course
            // Slope = handicap * (course slope / 113) + CR - par of the course
            slope = selectedPlayer.getHandicap() * selectedCourse.getCourseSlope() / 113 + selectedCourse.getCourseRating() - selectedCourse.getCoursePar();
            finalSlope = (int) Math.round(slope);
            playersRound.setSlope(finalSlope);

            // each hole has a HCP describing the order of difficulty out of 18 holes. E.g. if player's slope is 17,
            // he/she has to make one par result to the hole with HCP of 18, and rest could be bogeys (one more than par)
            ownParScore = s.getHolePar() + (finalSlope / 18) + ((18 - s.getHoleHCP() + (finalSlope % 18))/18);
            s.setHoleScore(ownParScore);
            s.setSlopeScore(ownParScore);
            allHoles.set(i, s);
            }
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_1) {
            Log.d("HoleList", "REQUEST_CODE_1 detected");
            if (resultCode == RESULT_OK) {
                // Log.d("HoleList", "RESULT_OK");
                Score holeBack = data.getParcelableExtra("modified_hole");
                int index;
                index = holeBack.getHoleNumber();
                // Toast.makeText(getApplicationContext(), "Putts " + holeBack.getPutts() + "FW " + holeBack.getFairwayHit(), Toast.LENGTH_SHORT).show();

                // when set to true, the symbols turns from arrow to check ok
                holeBack.setHoleDetailsFilledIn(true);

                // update the hole details
                allHoles.set(index-1, holeBack);

                // prepare for starting the list view again
                final HoleListAdapter adapter = new HoleListAdapter(this, android.R.layout.simple_list_item_1, allHoles);

                // start adapter again
                ListView listView = (ListView) findViewById(R.id.list);
                listView.getAdapter();

                // make sure the arrow symbol turns from arrow to check box ok, when returnig back to list view
                // it was strange to notice that notifyDataSetChanged was not enought to accomplish this
                listView.invalidateViews();
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void storeOneScore(boolean firstHole) {

        if (firstHole) {
            holeStoreCounter = 0;
        }

        if ((holeStoreCounter < allHoles.size() ) && (holeStoreCounter < 18)) {
            Log.d("ownDebug", "write new score " + holeStoreCounter);
            if((holeStoreCounter % 5) == 0) {
                Toast.makeText(getApplicationContext(), "Storing... " + holeStoreCounter, Toast.LENGTH_SHORT).show();
            }
            new PostOneHoleScore().execute("http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/Score");
        } else {
            // all scores stored, exit to main page
            // later implement sending an email
            Toast.makeText(getApplicationContext(), "All scores stored ! " + holeStoreCounter, Toast.LENGTH_LONG).show();
            finish();
        }

    }

    class PostOneHoleScore extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Initialize URL connection
            HttpURLConnection urlConnection = null;
            String urlString = params[0];

            // Initialize a buffer for reading back the results of writing
            BufferedReader reader = null;

            // Response data to be parsed
            String responseDataForParsing = null;

            JSONObject oneScore = new JSONObject();

            try {
                oneScore.put("HoleNr", allHoles.get(holeStoreCounter).getHoleNumber());
                oneScore.put("HoleScore", allHoles.get(holeStoreCounter).getHoleScore());
                oneScore.put("Putts", allHoles.get(holeStoreCounter).getPutts());
                oneScore.put("FairwayHit", allHoles.get(holeStoreCounter).getFairwayHit());
                oneScore.put("GreenHit", allHoles.get(holeStoreCounter).getGreenHit());
                oneScore.put("PlayersRoundid", playersRound.getPlayersRoundId());
                oneScore.put("Holeid", allHoles.get(holeStoreCounter).getHoleId());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                // Construct the URL for creating a round event

                URL url = new URL(urlString);
                String stringToWrite = oneScore.toString();

                // Create the request to database through php interface, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true); // Equals to POST according to android developer documentation
                urlConnection.setRequestMethod("POST"); // PUT and DELETE are possible
                urlConnection.connect();

                // Prepare to write
                OutputStream outStream = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream, "UTF-8"));
                // writer.write(String.valueOf(roundEventData));
                writer.write(stringToWrite);
                writer.flush();
                writer.close();
                outStream.close();

                // readStream(inStream); inStream is to get the response from the network
                InputStream inStream = new BufferedInputStream(urlConnection.getInputStream());
                StringBuffer buffer = new StringBuffer();
                if (inStream == null) {
                    // Nothing to do.
                    urlConnection.disconnect();
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    urlConnection.disconnect();
                    return null;
                }
                responseDataForParsing = buffer.toString();

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            urlConnection.disconnect();
            return responseDataForParsing;
        }

        @Override
        protected void onPostExecute(String jsonToBeParsed) {
            handleResponseOfStoringOneHole(jsonToBeParsed);
        }
    }
    private void handleResponseOfStoringOneHole(String jsonResponse) {

        // add here storing result to log
        Log.d("ownDebug", "call storeOneScore(false) " + holeStoreCounter + " " + jsonResponse);
        holeStoreCounter++;
        storeOneScore(false);
    }
}
