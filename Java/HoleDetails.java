package com.example.android.playgolf;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static android.os.Build.VERSION_CODES.M;
import static com.example.android.playgolf.HoleListAdapter.REQUEST_CODE_1;
import static com.example.android.playgolf.MainActivity.backgroundColorString;
import static com.example.android.playgolf.R.id.fw_hit_no;
import static com.example.android.playgolf.R.id.fw_hit_yes;
import static com.example.android.playgolf.R.id.green_hit_no;
import static com.example.android.playgolf.R.id.green_hit_yes;
import static com.example.android.playgolf.R.id.nr_of_putts;
import static java.security.AccessController.getContext;

/**
 * Created by Harri on 25.10.2016.
 */


public class HoleDetails extends AppCompatActivity {

    int nrOfPutts;
    private static final String colorBlack = "#000000";
    private static final String colorWhite = "#FFFFFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_hole);
        // Toast.makeText(getApplicationContext(), "Enter hole details \n", Toast.LENGTH_SHORT).show();

        final Score oneHole;

        oneHole = getIntent().getParcelableExtra("hole_scoring");

        View textContainer = findViewById(R.id.single_hole_container);
        textContainer.setBackgroundColor(Color.parseColor(backgroundColorString));

        TextView holeNumber = (TextView) findViewById(R.id.hole_number);
        holeNumber.setText("" + oneHole.getHoleNumber());

        TextView holeScore = (TextView) findViewById(R.id.hole_score);
        holeScore.setText("" + oneHole.getHoleScore());

        nrOfPutts = oneHole.getPutts();
        Button putts = (Button) findViewById(R.id.nr_of_putts);
        putts.setText("" + nrOfPutts);
        putts.setBackgroundColor(Color.parseColor(colorWhite)); // white background for putts
        putts.setTextColor(Color.parseColor(colorBlack));

        TextView holePar = (TextView) findViewById(R.id.hole_par);
        holePar.setText("" + oneHole.getHolePar());

        RadioGroup fwButton = (RadioGroup) findViewById(R.id.fw_group);

        // if par 3, then it is obsolete if fairway was hit, and thus it is made invisible
        if (oneHole.getHolePar() == 3) {
            fwButton.setVisibility(View.INVISIBLE);
            TextView fairwayText = (TextView) findViewById(R.id.fairway_text_id);
            fairwayText.setVisibility(View.INVISIBLE);
        }
        if (oneHole.getFairwayHit()) {
            fwButton.check(fw_hit_yes);
        } else
            fwButton.check(fw_hit_no);

        fwButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == fw_hit_yes) {
                    oneHole.setFairwayHit(true);
                } else {
                    oneHole.setFairwayHit(false);
                }
            }
        });

        RadioGroup ghButton = (RadioGroup) findViewById(R.id.gh_group);

        if (oneHole.getGreenHit()) {
            ghButton.check(green_hit_yes);
        } else
            ghButton.check(green_hit_no);

        ghButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == green_hit_yes) {
                    oneHole.setGreenHit(true);
                } else {
                    oneHole.setGreenHit(false);
                }
            }
        });

        Button storeButton = (Button) findViewById(R.id.store_button);

        // Set a click listener on that View
        storeButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the store hole details is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link HoleList Activity}
                Intent holeListIntent = new Intent(getApplicationContext(), HoleList.class);

                holeListIntent.putExtra("modified_hole", oneHole);
             //   Log.v("HoleDetails", "Store button clicked");


                setResult(RESULT_OK, holeListIntent);
                // Start the new activity
                finish();
            }
        });

        Button incPuttsButton = (Button) findViewById(R.id.plus_putt_button);
        incPuttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick ( View view) {
                nrOfPutts++;
                TextView puttView = (TextView) findViewById(R.id.nr_of_putts);
                puttView.setText("" + nrOfPutts);
                oneHole.setPutts(nrOfPutts);
            }
        });

        Button decPuttsButton = (Button) findViewById(R.id.minus_putt_button);
        decPuttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick ( View view) {
                if (nrOfPutts > 0) {
                    nrOfPutts--;
                    TextView puttView = (TextView) findViewById(R.id.nr_of_putts);
                    puttView.setText("" + nrOfPutts);
                    oneHole.setPutts(nrOfPutts);
                }
            }
        });
    }
}
