package com.example.android.playgolf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import java.util.ArrayList;

import static android.R.attr.action;
import static android.R.attr.data;
import static android.R.attr.drawable;
import static android.R.attr.targetActivity;
import static android.R.attr.targetPackage;
import static android.R.attr.top;
import static android.R.attr.x;
import static android.R.color.black;
import static android.R.color.holo_green_light;
import static android.R.id.input;
import static android.app.Activity.RESULT_OK;
import static android.graphics.Color.GREEN;
import static android.media.CamcorderProfile.get;
import static android.os.Build.VERSION_CODES.M;
import static android.support.v4.app.ActivityCompat.startActivity;
import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static com.example.android.playgolf.HoleList.allHoles;
import static com.example.android.playgolf.HoleList.theme;
import static com.example.android.playgolf.MainActivity.playersRound;
import static com.example.android.playgolf.MainActivity.roundEvent;
import static com.example.android.playgolf.R.layout.holes;
import static java.lang.Integer.parseInt;

/**
 * Created by Harri on 24.10.2016.
 */


public class HoleListAdapter extends ArrayAdapter<Score> {
    private Context contextHoleList;
    public static final int REQUEST_CODE_1 = 1;
    private static final String colorString1 = "#E8EAF6";
    private static final String colorString2 = "#C5CAE9";
    private static final String colorStringBlue = "#1565C0";
    private static final String colorWhite = "#FFFFFF";
    private static final String colorBlack = "#000000";
    private static final String colorStringRed = "#F44336";
    private static final String colorStringGreen = "#8BC34A";


    private static final String colorStringYellow = "#FFEB3B";
    private static View listItemView;

    public HoleListAdapter(Activity context, int ViewId, ArrayList<Score> holes) {

        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter , the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, holes);
        contextHoleList = context;

    }

    static public void setHole(int index, Score holeToUpdate) {
        allHoles.set(index, holeToUpdate);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.holelist_item, parent, false);
        }

        // Get the {@link AndroidFlavor} object located at this position in the list
        final Score currentHole = getItem(position);

        // Find the TextView in the holelist_item.xml layout with the ID version_name
        TextView holeNrTextView = (TextView) listItemView.findViewById(R.id.hole_nr_view);

        // set the hole number as text on the TextView
        holeNrTextView.setText("" + currentHole.getHoleNumber());

        // Find the TextView in the holelist_item.xml layout with the ID version_number
        TextView parTextView = (TextView) listItemView.findViewById(R.id.hole_par_view);

        // set the hole par as text on TextView
        parTextView.setText("" + currentHole.getHolePar());

        // Find the TextView in the holelist_item.xml layout with the ID version_number
        TextView hcpTextView = (TextView) listItemView.findViewById(R.id.hole_hcp_view);

        // set the text on the hole HCP TextView
        hcpTextView.setText("" + currentHole.getHoleHCP());

      // Find the TextView in the holelist_item.xml layout with the ID version_number
        final Button scoreTextView = (Button) listItemView.findViewById(R.id.hole_score);
        scoreTextView.setText(String.valueOf(currentHole.getHoleScore()));

        // spending one hour in order to find the current way to access color resources with no success
        // the following is syntactically correct, but instead of null should be something else, not 'getContext()', nor 'this'
        // scoreTextView.setBackgroundColor(ResourcesCompat.getColor(null, R.color.colorYellow500, theme));
        // the old way not supported any more: get.getColor(R.color.colorYellow500));
        // this does not work either: (ContextCompat.getColor(this, R.color.white));

        int scoreDelta = currentHole.getHoleScore() - currentHole.getSlopeScore();

        // the background color and text color is set according to scoring
        switch (scoreDelta) {
            case -2: {
                scoreTextView.setBackgroundColor(Color.parseColor(colorStringYellow)); // "" yellow if two less
                scoreTextView.setTextColor(Color.parseColor(colorBlack));
            };
                break;
            case -1: {
                scoreTextView.setBackgroundColor(Color.parseColor(colorStringRed)); // "" red if one less
                scoreTextView.setTextColor(Color.parseColor(colorWhite));
            };
                break;
            case 0: {
                scoreTextView.setBackgroundColor(Color.parseColor(colorWhite)); // #green if the same
                scoreTextView.setTextColor(Color.parseColor(colorBlack));
            };
                break;
            case 1: {
                scoreTextView.setBackgroundColor(Color.parseColor(colorStringBlue)); // dark blue is one more
                scoreTextView.setTextColor(Color.parseColor(colorWhite));
            };
                break;
            case 2: {
                scoreTextView.setBackgroundColor(Color.parseColor(colorBlack)); // black if two more
                scoreTextView.setTextColor(Color.parseColor(colorWhite));
            };
                break;
            default: ;
            {
                scoreTextView.setBackgroundColor(Color.parseColor(colorWhite));
                scoreTextView.setTextColor(Color.parseColor(colorBlack));
            }
                break;
        }

        // when scoring 3 less or better, white background
        if (scoreDelta < -2) {
            scoreTextView.setBackgroundColor(Color.parseColor(colorWhite)); // white if own par
            scoreTextView.setTextColor(Color.parseColor(colorBlack));
            }

        // when scoring 3 more or worse, same as 2 more
        if (scoreDelta > 2) {
            scoreTextView.setBackgroundColor(Color.parseColor(colorBlack)); // white if two less
            scoreTextView.setTextColor(Color.parseColor(colorWhite));
        }

        // set colors to alter in successive rows
        View textContainer = listItemView.findViewById(R.id.hole_list_item_container);
        if ((position % 2) == 0) {
            textContainer.setBackgroundColor(Color.parseColor(colorString1));
        } else {
            textContainer.setBackgroundColor(Color.parseColor(colorString2));
        }

        Button scoreButton = (Button) listItemView.findViewById(R.id.hole_score);
        scoreButton.setOnClickListener(new View.OnClickListener() {
            public  void onClick (View view) {
                // count scoring result up to this hole, total strokes and bogeypoints
                int upToWhichHole = currentHole.getHoleNumber();
                int i;
                int sumOfStrokes = 0;
                int sumOfBogeyPoints =0;
                for (i=0; i < upToWhichHole; i++) {
                    sumOfStrokes += allHoles.get(i).getHoleScore();

                    // count here bogey points
                    // 2 + HolePar - HoleScore + (Slope DIV 18) + (18 - HoleHCP + (Slope % 18)) DIV 18
                    sumOfBogeyPoints += 2 + allHoles.get(i).getHolePar() - allHoles.get(i).getHoleScore() + (playersRound.getSlope() / 18) +
                            (18 - allHoles.get(i).getHoleHCP() + (playersRound.getSlope() % 18))/18;
                    // bogey point is always 0 or positive
                    if (sumOfBogeyPoints < 0) {
                        sumOfBogeyPoints = 0;
                    }
                }
                // show result as a Toast to UI
                Toast.makeText(getContext(), "Strokes " + sumOfStrokes + ". BP " + sumOfBogeyPoints + ".", Toast.LENGTH_LONG).show();
            }
        }
        );

        Button btMinus = (Button) listItemView.findViewById(R.id.minus_button);
        btMinus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGray300));
        btMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (scoreTextView != null) {
                    final String scoreString = scoreTextView.getText().toString();
                    int score = parseInt(scoreString);
                    score--;
                    currentHole.setHoleScore(score);
                    setHole(position, currentHole);
                    notifyDataSetChanged();
  /*                  Toast.makeText(getContext(), "Score: " + scoreString + "\n", Toast.LENGTH_SHORT).show(); */
                }
            }
        });

        Button btPlus = (Button) listItemView.findViewById(R.id.plus_button);
        btPlus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorGray300));
        btPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (scoreTextView != null) {
                    final String scoreString = scoreTextView.getText().toString();
                    int score = parseInt(scoreString);
                    score++;
                    currentHole.setHoleScore(score);

                    // Make sure the current hole contains the updated information performed in hole details view,
                    // before writing the new score to allHoles
                    currentHole.setPutts(allHoles.get(position).getPutts());
                    currentHole.setFairwayHit(allHoles.get(position).getFairwayHit());
                    currentHole.setGreenHit(allHoles.get(position).getGreenHit());
                    setHole(position, currentHole);

                    // Notify the list view adapter on the changed data
                    notifyDataSetChanged();
                }
            }
        });

        // Find the View that triggers exiting from the view
        ImageView icon = (ImageView) listItemView.findViewById(R.id.array_image);

        // Choose which image to show, whether it is array_image or hole details checked image
        // As an assumption, an array image is shown

        int drawableID;
        if (allHoles.get(position).getHoleDetailsFilledIn()) {
            drawableID = getContext().getResources().getIdentifier("btn_check_on_pressed_holo_light", "drawable", "com.example.android.playgolf");
            icon.setImageResource(drawableID);
            if (currentHole.getHoleDetailsFilledIn() == false) {
                currentHole.setHoleDetailsFilledIn(true);
                notifyDataSetChanged();
            }
        } else {
            drawableID = getContext().getResources().getIdentifier("arrow2", "drawable", "com.example.android.playgolf");
            icon.setImageResource(drawableID);
        }

        // Set a click listener on that View
        icon.setOnClickListener(new AdapterView.OnClickListener() {
            // The code in this method will be executed when the arrow is clicked on.
        //  @Override
            public void onClick(View view) {

        //       Toast.makeText(getContext(), "HoleListAdapter, before intent \n", Toast.LENGTH_SHORT).show();

         //       startSingleHole(currentHole);

                // Make sure that the hole details obtained in hole details view are updated to the current Hole,
                // in case the user is returning back to update the same hole
                currentHole.setPutts(allHoles.get(position).getPutts());
                currentHole.setFairwayHit(allHoles.get(position).getFairwayHit());
                currentHole.setGreenHit(allHoles.get(position).getGreenHit());

                Intent singleHoleIntent = new Intent(contextHoleList, HoleDetails.class);
                singleHoleIntent.putExtra("hole_scoring", currentHole);

                // Start the new activity

                // contextHoleList.startActivity(singleHoleIntent);
                startActivityForResult((Activity) contextHoleList, singleHoleIntent, REQUEST_CODE_1, Bundle.EMPTY);
            }
        });

        // Return the whole list item layout (containing 2 TextViews and an ImageView)
        // so that it can be shown in the ListView
        return listItemView;
    }

}
