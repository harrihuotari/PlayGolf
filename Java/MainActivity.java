package com.example.android.playgolf;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;

import static android.R.attr.data;
import static android.R.attr.format;
import static android.R.attr.id;
import static android.R.attr.name;
import static android.R.attr.value;
import static android.media.CamcorderProfile.get;
import static android.os.FileObserver.DELETE;
import static android.support.v4.app.ActivityCompat.startActivity;
import static com.example.android.playgolf.HoleList.smsMessage;
import static com.example.android.playgolf.R.id.nameSpinner;
import static com.example.android.playgolf.R.id.teeTime;
import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private Button btnSubmit;
    public static final String backgroundColorString = "#E8EAF6";
    public static Course selectedCourse = new Course(1, "Nokia River Golf", "NRG", "Nokia", 73, 131.0, 72.5);
    public static Player selectedPlayer = new Player(1, "Harri", "Huotari", "Nokia River Golf", 13.7, "+358503131142", "harri.huotari51@outlook.com");
    public static RoundEvent roundEvent = new RoundEvent("Text of event", "20.08.2016", 20, 0, 50, 3, "SW", "", 1);
    public static PlayersRound playersRound = new PlayersRound(60000, "Yellow", "12:00", 15.1, 36, 60000, 60000);
    private static ArrayList<Course> courses = new ArrayList<Course>();
    private static ArrayList<Player> players = new ArrayList<Player>();
    public static boolean disableDatabaseUpdates = true;
    public static double latitude = 15.00;
    public static double longitude = 47.00;
    private LocationManager locationManager;
    private static final int PERMISSION_ACCESS_LOCATION = 321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View textContainer = findViewById(R.id.main_activity_container);
        textContainer.setBackgroundColor(Color.parseColor(backgroundColorString));

        // get weather based on location. Result stored to roundEvent,
        // where it can be seen from database through web application
        // and also added to the SMS as a part of the summary of results
        getWeather();

        // Get list of courses with async task from the data base, and put them onto a spinner list
        new AsyncCourseList().execute("http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/Course");

        // Get list of players with async task from the data base, and put them onto a spinner list
        new AsyncPlayerList().execute("http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/Player");

        // Set current date as an assumption
        final EditText dateField = (EditText) findViewById(R.id.editDate);
        SimpleDateFormat eDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar c = Calendar.getInstance();
        String formattedDate = eDate.format(c.getTime());
        dateField.setText(formattedDate);

        // set current time as an assumption
        final EditText timeField = (EditText) findViewById(teeTime);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        String formattedTime = timeFormat.format(c.getTime());
        timeField.setText(formattedTime);

        // set listener on the name list to detect which name is selected
        final Spinner pNameSpinner = (Spinner) findViewById(nameSpinner);
        pNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String playerName = (String) parent.getItemAtPosition(position);
                String[] nameWords = playerName.split(" ");
                int i;
                for (i = 0; i < players.size(); i++) {
                    selectedPlayer = players.get(i);
                    if (nameWords[0].equals(selectedPlayer.getFirstName()) && nameWords[1].equals(selectedPlayer.getLastName())) {
                        final EditText exactHCPField = (EditText) findViewById(R.id.exactHCP);
                        exactHCPField.setText("" + selectedPlayer.getHandicap());
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Find the View that triggers exiting from the view
        TextView fillHoles = (TextView) findViewById(R.id.textStartGolf);

        // Set a click listener on that View
        fillHoles.setOnClickListener(new OnClickListener() {
            // The code in this method will be executed when the store button is clicked.
            @Override
            public void onClick(View view) {

                // read all fields the user has selected or inputted
                readAllFields();

                // Create a new intent to open the {@link HoleList Activity}
                Intent holesIntent = new Intent(MainActivity.this, HoleList.class);

                // Start the new activity
                startActivity(holesIntent);
            }
        });
    }

    // a procedure to add a name or a course to a spinner, which ever in question
    public void addNameOnSpinner(int layoutId, ArrayList<String> names) {
        Spinner spinner = (Spinner) findViewById(layoutId);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

        /* Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                arrayId, android.R.layout.simple_spinner_item); */

    private class AsyncCourseList extends AsyncTask<String, Void, String> {

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

            // Parse JSON and update UI

            // Show the JSON got from the server
            // Toast.makeText(this, )
            parseJsonAndUpdateCourseList(jsonString);
        }
    }

    private void parseJsonAndUpdateCourseList(String jsonToParse) {
        // Toast.makeText(this, jsonToParse, Toast.LENGTH_LONG).show();
        JSONObject course = null;
        JSONArray jsonDataArray;
        JSONObject inDataAsObject;
        int courseId;
        String courseName;
        String abbreviation;
        String location;
        int coursePar;
        double courseSlope;
        double courseRating;

        try {
            inDataAsObject = new JSONObject(jsonToParse);
            jsonDataArray = inDataAsObject.getJSONArray("data");

            int i;
            for (i = 0; i < jsonDataArray.length(); i++) {

                // Read required fields from the object
                course = jsonDataArray.getJSONObject(i);
                courseId = parseInt(course.getString("Cid"));
                courseName = course.getString("CourseName");
                abbreviation = course.getString("Abbreviation");
                location = course.getString("Location");
                coursePar = parseInt(course.getString("CoursePar"));
                courseSlope = parseDouble(course.getString("CourseSlope"));
                courseRating = parseDouble(course.getString("CR"));
                courses.add(new Course(courseId, courseName, abbreviation, location, coursePar, courseSlope, courseRating));
            }
            // sort the courses into the alphabetical order
            Collections.sort(courses, new Comparator<Course>() {
                        @Override
                        public int compare(Course lhs, Course rhs) {
                            return lhs.getCourseName().compareTo(rhs.getCourseName());
                        }
                    });
        } catch (JSONException e) {
            Toast.makeText(this, "Could not parse json data", Toast.LENGTH_SHORT).show();
        }

        ArrayList<String> spinnerCourses = new ArrayList<>();

        int i;
        for (i = 0; i < courses.size(); i++) {
            Course courseData;
            courseData = courses.get(i);
            courseName = courseData.getCourseName();
            spinnerCourses.add(courseName);
        }

        addNameOnSpinner(R.id.courseSpinner, spinnerCourses);
    }

    private class AsyncPlayerList extends AsyncTask<String, Void, String> {

        // Fetch the list of players from the data base

        // Initialize URL connection
        HttpURLConnection urlConnection = null;

        // Initialize a buffer for reading the results
        BufferedReader reader = null;

        // Player data to be parsed
        String outputDataForParsing = null;

        @Override
        protected String doInBackground(String... params) {

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

            // Parse JSON and update UI

            // Show the JSON got from the server
            // Toast.makeText(this, )
            parseJsonAndUpdatePlayerList(jsonString);
        }
    }

    private void parseJsonAndUpdatePlayerList(String jsonToParse) {
        // Toast.makeText(this, jsonToParse, Toast.LENGTH_LONG).show();
        JSONObject player = null;
        JSONArray jsonDataArray;
        JSONObject inDataAsObject;
        int playerId;
        String fName;
        String lName;
        String courseAbbr;
        double handicap;
        String mobileNr;
        String email;

        try {
            inDataAsObject = new JSONObject(jsonToParse);
            jsonDataArray = inDataAsObject.getJSONArray("data");

            // newPlayers = jsonDataString.getJSONArray("data");
            // Toast.makeText(this, jsonDataArray, Toast.LENGTH_SHORT);

            int i;
            for (i = 0; i < jsonDataArray.length(); i++) {

                // Read required fields from the object
                player = jsonDataArray.getJSONObject(i);
                playerId = parseInt(player.getString("Pid"));
                fName = player.getString("Fname");
                lName = player.getString("Lname");
                courseAbbr = player.getString("Courseabbr");
                handicap = parseDouble(player.getString("Handicap"));
                mobileNr = player.getString("MobileNr");
                email = player.getString("email");
                players.add(new Player(playerId, fName, lName, courseAbbr, handicap, mobileNr, email));
            }
            // sort names to alphabetical order according to the first name
            Collections.sort(players, new Comparator<Player>() {
                @Override
                public int compare(Player lhs, Player rhs) {
                    return lhs.getFirstName().compareTo(rhs.getFirstName());
                }
            });

        } catch (JSONException e) {
            Toast.makeText(this, "Could not parse json data", Toast.LENGTH_SHORT).show();
        }

        ArrayList<String> names = new ArrayList<>();
        int i;
        for (i = 0; i < players.size(); i++) {
            Player playerData;
            playerData = players.get(i);
            fName = playerData.getFirstName();
            lName = playerData.getLastName();
            names.add(fName + " " + lName);
        }
        addNameOnSpinner(nameSpinner, names);
    }

    public void readAllFields() {

        final EditText dateField = (EditText) findViewById(R.id.editDate);
        String dateString = dateField.getText().toString();
        // SimpleDateFormat dFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        // Date pvm = dFormat.parse(dateString);
        roundEvent.setTheDate(dateString);

        final EditText teeTimeField = (EditText) findViewById(teeTime);
        String teeTime = teeTimeField.getText().toString();

        final EditText exactHCPField = (EditText) findViewById(R.id.exactHCP);
        String hcpString = exactHCPField.getText().toString();
        Double handicap = Double.valueOf(hcpString);

        String golfCourse;
        final Spinner gCourse = (Spinner) findViewById(R.id.courseSpinner);
        golfCourse = (String) gCourse.getSelectedItem();

        int i;
        for (i = 0; i < courses.size(); i++) {
            selectedCourse = courses.get(i);
            if (golfCourse.equals(selectedCourse.getCourseName())) {
                break;
            }
        }
        String playerName;
        final Spinner pNameSpinner = (Spinner) findViewById(nameSpinner);
        playerName = (String) pNameSpinner.getSelectedItem();
        String[] nameWords = playerName.split(" ");

        for (i = 0; i < players.size(); i++) {
            selectedPlayer = players.get(i);
            if (nameWords[0].equals(selectedPlayer.getFirstName()) && nameWords[1].equals(selectedPlayer.getLastName())) {
                selectedPlayer.setHandicap(handicap);
                break;
            }
        }

        // Fill the event description field (RoundName) for the data base to identify the course, and also shown in the first page
        final EditText eventField = (EditText) findViewById(R.id.eventDescription);
        String event = eventField.getText().toString();
        event += " " + nameWords[0] + " in " + selectedCourse.getAbbreviation() + ", " + dateString + " " + teeTime;

        // The maximum string length in the data base is 45 characters for the event description. So, let's cut it from the end, if needed
        if (event.length() > 45) {
            String shortenedEventString = event.substring(0, 44);
            eventField.setText(shortenedEventString);
            roundEvent.setRoundName(shortenedEventString);
        } else {
            eventField.setText(event);
            roundEvent.setRoundName(event);
        }

        // Set the course id for the RoundEvent in the data base, and create new event to the data base
        roundEvent.setCourseId(selectedCourse.getCourseId());

        // if user wish to update database, it is enabled
        RadioGroup fwButton = (RadioGroup) findViewById(R.id.select_database_group);
        if (fwButton.getCheckedRadioButtonId() == R.id.database_yes) {
            Toast.makeText(getBaseContext(), "Database to be updated",  Toast.LENGTH_SHORT).show();
            new CreateNewRoundEvent().execute("http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/RoundEvent");
            disableDatabaseUpdates = false;
        }
        // Note ! Here the roundEvent is ready to be stored to the data base

        // Let's set the data for the PlayersRound table in the data base, which is mandatory before storing the scores
        playersRound.setTeeTime(teeTime);
        playersRound.setExactHCP(selectedPlayer.getHandicap());
        playersRound.setPlayerId(selectedPlayer.getPlayerId());
    }

    class CreateNewRoundEvent extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Initialize URL connection
            HttpURLConnection urlConnection = null;
            String urlString = params[0];

            // Initialize a buffer for reading back the results of writing
            BufferedReader reader = null;

            // Response data to be parsed
            String responseDataForParsing = null;

            JSONObject roundEventData = new JSONObject();

            try {
                roundEventData.put("RoundName", roundEvent.getRoundName());
                roundEventData.put("Date", roundEvent.getTheDate());
                roundEventData.put("Temperature", roundEvent.getTemperature());
                roundEventData.put("Rain", roundEvent.getRain());
                roundEventData.put("Sun", roundEvent.getSun());
                roundEventData.put("Wind", roundEvent.getWind());
                roundEventData.put("WindDirection", roundEvent.getWindDirection());
                roundEventData.put("Courseid", roundEvent.getCourseId());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                // Construct the URL for creating a round event

                URL url = new URL(urlString);
                String stringToWrite = roundEventData.toString();

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
            handleResponseOfCreatingRoundEvent(jsonToBeParsed);
        }
    }

    private void handleResponseOfCreatingRoundEvent(String jsonToBeParsed) {

        try {
            JSONObject jsonResponse;
            jsonResponse = new JSONObject(jsonToBeParsed);
            String status = jsonResponse.getString("ok");
            // Toast.makeText(getBaseContext(), "Round event response status: " + status,  Toast.LENGTH_LONG).show();
            if (status.equals("updated")) {
                // Toast.makeText(getBaseContext(), "Round event: " + jsonToBeParsed,  Toast.LENGTH_LONG).show();
                // Round event id is needed for creating players round, which in turn is needed for scores
                startAsyncEventList();
            } else
                Toast.makeText(getBaseContext(), "Cannot create round event: " + jsonToBeParsed,  Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            // Cannot proceed with storing
            Toast.makeText(getBaseContext(), "Json exception: ",  Toast.LENGTH_LONG).show();
        }

        // Toast.makeText(getBaseContext(), "Response of creating round event: " + jsonToBeParsed,  Toast.LENGTH_LONG).show();
    }

    private void startAsyncEventList() {

        // Round event id is needed for creating players round, which in turn is needed for scores
        new AsyncEventList().execute("http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/RoundEvent");
    }

    class AsyncEventList extends AsyncTask<String, Void, String> {

        // Fetch the list of events from the data base

        // Initialize URL connection
        HttpURLConnection urlConnection = null;

        // Initialize a buffer for reading the events
        BufferedReader reader = null;

        // RoundEvent data to be parsed
        String outputDataForParsing = null;

        @Override
        protected String doInBackground(String... params) {

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
                urlConnection.disconnect();
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

            // Now new RoundEvent is created, and ready to proceed to next phase to find out what is the id of the new event
            parseJsonAndGetRoundEventId(jsonString);
        }
    }

    private void parseJsonAndGetRoundEventId(String eventListInJson) {

        // The purpose is find the right event, which was just stored,
        // because its idenfier in data base is needed for storing PlayersRound

        JSONObject event;
        JSONArray jsonDataArray;
        JSONObject inDataAsObject;
        int eventId;
        int courseId;
        String roundEventName;

        // Toast.makeText(this, jsonToParse, Toast.LENGTH_LONG).show();

        try {
            inDataAsObject = new JSONObject(eventListInJson);
            jsonDataArray = inDataAsObject.getJSONArray("data");

            int i;
            for (i = 0; i < jsonDataArray.length(); i++) {

                // Read required fields from the object
                event = jsonDataArray.getJSONObject(i);
                eventId = parseInt(event.getString("REid"));
                courseId = parseInt(event.getString("Courseid"));
                roundEventName = event.getString("RoundName");

                // Let's test if course id and round name are the same
                if ((courseId == roundEvent.getCourseId()) && (roundEventName.equals(roundEvent.getRoundName()))) {
                    playersRound.setRoundEventId(eventId);
                    startCreatingPlayersRound();
                }
            }
            // Toast.makeText(this, "all players added", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(this, "Could not parse json data", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCreatingPlayersRound() {
        // when id of event is known, we can create a new PlayersRound element to the data base
        new CreatePlayersRound().execute("http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/PlayersRound");
    }

    class CreatePlayersRound extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Initialize URL connection
            HttpURLConnection urlConnection = null;
            String urlString = params[0];

            // Initialize a buffer for reading back the results of writing
            BufferedReader reader = null;

            // Response data to be parsed
            String responseDataForParsing = null;

            JSONObject playersRoundData = new JSONObject();

            try {
                playersRoundData.put("TypeofTee", playersRound.getTypeofTee());
                playersRoundData.put("TeeTime", playersRound.getTeeTime());
                playersRoundData.put("ExactHCP", playersRound.getExactHCP());
                playersRoundData.put("RoundEventid", playersRound.getRoundEventId());
                playersRoundData.put("Playerid", playersRound.getPlayerId());

            } catch (JSONException e) {
                Log.e("PlaceholderFragment", "no json could be created");
                return null;
            }

            try {
                // Construct the URL for creating a players round

                URL url = new URL(urlString);
                String stringToWrite = playersRoundData.toString();

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

            handleResponseOfNewPlayersRound(jsonToBeParsed);
        }
    }

    private void handleResponseOfNewPlayersRound(String jsonToBeParsed) {

        // Toast.makeText(getBaseContext(), "Players Round created " + jsonToBeParsed,  Toast.LENGTH_LONG).show();

        // Now new Players Round is created, and ready to proceed to next phase to find out what is the id of that Round.
        // Players Round id is needed for storing scores.
        // For that purpose, first the RlayersRound table need to be downloaded from the data base.

        try {
            JSONObject jsonResponse;
            jsonResponse = new JSONObject(jsonToBeParsed);
            String status = jsonResponse.getString("ok");
            if (status.equals("updated")) {
                startAsyncListOfPlayersRoundByEvent();
            } else {
                Toast.makeText(getBaseContext(), "Cannot create players round " + jsonToBeParsed,  Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            // Cannot proceed with storing
            Log.e("PlaceholderFragment", "Error, cannot create round event", e);
        }
    }

    private void startAsyncListOfPlayersRoundByEvent() {

        String urlString;

        // Note ! There might be several players participating one event, but at least the list is shorter than the whole list of rounds
        urlString = "http://home.tamk.fi/~c6hhuota/CMD/cmd_api/cmd_api/index.php/PlayersRoundByEvent/" + String.valueOf(playersRound.getRoundEventId());
        // Toast.makeText(getBaseContext(), "players round string" + urlString,  Toast.LENGTH_SHORT).show();
        new AsyncListOfPlayersRoundByEvent().execute(urlString);
    }

    class AsyncListOfPlayersRoundByEvent extends AsyncTask<String, Void, String> {

        // Fetch the list of rounds from the data base

        // Initialize URL connection
        HttpURLConnection urlConnection = null;

        // Initialize a buffer for reading the events
        BufferedReader reader = null;

        // RoundEvent data to be parsed
        String outputDataForParsing = null;

        @Override
        protected String doInBackground(String... params) {

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

            // Now new RoundEvent is created, and ready to proceed to next phase to find out what is the id of the new event
            parseJsonAndGetPlayersRoundId(jsonString);
        }
    }

    private void parseJsonAndGetPlayersRoundId(String listOfPlayersRoundsInJson) {

        // The purpose is find the right event, which was just stored,
        // because its idenfier in data base is needed for storing PlayersRound

        JSONObject round = null;
        JSONArray jsonDataArray;
        JSONObject inDataAsObject;
        int roundId;
        int playerId;

        // Toast.makeText(this, jsonToParse, Toast.LENGTH_LONG).show();

        try {
            inDataAsObject = new JSONObject(listOfPlayersRoundsInJson);
            jsonDataArray = inDataAsObject.getJSONArray("data");

            // In most cases this is only list of one item, but let's use for loop still as a general solution
            // Let's use player id for identifying the right players round
            int i;
            for (i = 0; i < jsonDataArray.length(); i++) {

                // Read required fields from the object
                round = jsonDataArray.getJSONObject(i);
                roundId = parseInt(round.getString("PRid"));
                playerId = parseInt(round.getString("Playerid"));

                // Let's test if player id is the same
                if (playerId == playersRound.getPlayerId() ) {

                    // Finally all information is ready to store scores
                    playersRound.setPlayersRoundId(roundId);
                    Toast.makeText(getBaseContext(), "Ready to store scores!",  Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Could not parse json data", Toast.LENGTH_SHORT).show();
        }
    }

    public void getWeather() {

        // get GPS coordinates first. If got, call GetWeatherTask after user permission
        startPositioning();
    }

    public void startPositioning() {
        // Connect system service LocationManager and register to listen to
        // location related events
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if( permissionCheck == PackageManager.PERMISSION_GRANTED ) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
                // get weather based on the GPS cocordinates
                new GetWeatherTask().execute();
            }
        }
        else { // ask the permissions...)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_ACCESS_LOCATION);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        switch (requestCode) {
            case PERMISSION_ACCESS_LOCATION: {
                int permissionCheck = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                if( permissionCheck == PackageManager.PERMISSION_GRANTED ) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            latitude = lastKnownLocation.getLatitude();
                            longitude = lastKnownLocation.getLongitude();

                            // get weather based on the GPS cocordinates
                            new GetWeatherTask().execute();
                        }
                    }
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    class GetWeatherTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            // Make HTTP Request
            return doWebRequest();
        }

        protected void onPostExecute( String jsonString ) {
            // Parse JSON and update UI
            parseJsonAndGetWeather(jsonString);
        }
    }

    private String doWebRequest() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            String urlString = "http://api.geonames.org/findNearByWeatherJSON?formatted=true&lat="
                    + latitude + "&lng=" + longitude + "&username=tonytorp&style=full";
            URL url = new URL(urlString);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
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
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            forecastJsonStr = null;
        } finally{
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
        return forecastJsonStr;
    }

    private JSONObject parseJsonAndGetWeather( String jsonToParse ){
        // Show the json response on the screen with a Toast notification
        // Toast.makeText(this, jsonToParse, Toast.LENGTH_LONG).show();
        // Let's parse the json and update ui
        JSONObject weatherObservation = null;
        String stringValue;
        double windDirect;
        String windDirectionString;

        try {
            weatherObservation = new JSONObject(jsonToParse).getJSONObject("weatherObservation");

            // get temperature and store
            stringValue = weatherObservation.getString("temperature");
            roundEvent.setTemperature(Double.parseDouble(stringValue));

            // get wind speed and store
            stringValue = weatherObservation.getString("windSpeed");
            roundEvent.setWind(Integer.parseInt(stringValue));

            // get clouds and visibility
            stringValue = weatherObservation.getString("clouds");
            roundEvent.setClouds(stringValue);

            // get wind direction in degrees
            stringValue = weatherObservation.getString("windDirection");
            windDirect = Double.parseDouble(stringValue);

            // wind directions in degrees can be found from here. Note, this is divided into 16 pies, below modified into 8 pies
            // http://climate.umn.edu/snow_fence/components/winddirectionanddegreeswithouttable3.htm
            if ((windDirect >= 337.50) && (windDirect < 22.50)) {
                windDirectionString = "N";
            } else if ((windDirect >= 22.50) && (windDirect < 67.50)) {
                windDirectionString = "NE";
            } else if ((windDirect >= 67.50) && (windDirect < 112.50)) {
                windDirectionString = "E";
            } else if ((windDirect >= 112.50) && (windDirect < 157.50)) {
                windDirectionString = "SE";
            } else if ((windDirect >= 157.50) && (windDirect < 202.50)) {
                windDirectionString = "S";
            } else if ((windDirect >= 202.50) && (windDirect < 247.50)) {
                windDirectionString = "SW";
            } else if ((windDirect >= 247.50) && (windDirect < 292.50)) {
                windDirectionString = "W";
            } else if ((windDirect >= 292.50) && (windDirect < 337.50)) {
                windDirectionString = "NW";
            } else {
                windDirectionString = "S";
            }
            roundEvent.setWindDirection(windDirectionString);
        }
        catch (JSONException e){
            Toast.makeText(this, "Could not update weather data", Toast.LENGTH_SHORT).show();
        }
        return weatherObservation;
    }
}

