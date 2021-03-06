package com.jeevesandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jeevesandroid.firebase.FirebaseSurvey;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ubhave.triggermanager.config.TriggerManagerConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.jeevesandroid.ApplicationContext.INCOMPLETE;
import static com.jeevesandroid.ApplicationContext.SURVEY_ID;
import static com.jeevesandroid.ApplicationContext.SURVEY_NAME;
import static com.jeevesandroid.ApplicationContext.TIME_SENT;
import static com.jeevesandroid.ApplicationContext.TRIG_TYPE;

/**
 * This shows any surveys that the user has either ignored or partially completed, and allows the user to select them
 * for completion.
 */
public class MissedSurveyActivity extends AppCompatActivity {
    ListView list;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Get the top 10 most recently missed survyes out of here
        Query myTopPostsQuery = FirebaseUtils.PATIENT_REF.child(INCOMPLETE).orderByChild(TIME_SENT).limitToLast(10);
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final ArrayList<FirebaseSurvey> surveys = new ArrayList<FirebaseSurvey>();
                final ArrayList<String> surveynames = new ArrayList<String>();
                ArrayList<DataSnapshot> children = new ArrayList<DataSnapshot>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    children.add(0,postSnapshot);
                }
                //Hopefully this should reverse them?
                    for (DataSnapshot postSnapshot : children) {
                    FirebaseSurvey survey = postSnapshot.getValue(FirebaseSurvey.class);
                    String id = postSnapshot.getKey();
                    survey.setkey(id);
                    long expiryTime = survey.getexpiryTime();
                    long expiryMillis = expiryTime * 60 * 1000;
                    long deadline = survey.gettimeSent() + expiryMillis;
                    String name = survey.gettitle();

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
                    boolean isAvailable = prefs.getBoolean(survey.getsurveyId(),false); //Check whether this survey should be avaialble
                    //This way we only add the most recent survey
                    if (!surveynames.contains(name) && isAvailable && deadline > System.currentTimeMillis() || survey.getexpiryTime() == 0) {
                        surveys.add(survey);
                        surveynames.add(name);

                    }

                }
                list = (ListView) findViewById(android.R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                        Intent resultIntent = new Intent(MissedSurveyActivity.this, SurveyActivity.class);
                        FirebaseSurvey clickedSurvey = surveys.get(position);


                        resultIntent.putExtra(SURVEY_ID, clickedSurvey.getkey());
                        resultIntent.putExtra(SURVEY_NAME, clickedSurvey.gettitle());
                        resultIntent.putExtra(TIME_SENT, clickedSurvey.gettimeSent());
                        resultIntent.putExtra(TRIG_TYPE, clickedSurvey.gettriggerType());
                        clickedSurvey.setbegun(); //Confirm that this survey has been started
                        startActivity(resultIntent);
                    }
                });
                MissedSurveyItem adapter = new MissedSurveyItem(MissedSurveyActivity.this, surveys);
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    /**
     * Custom list item
     */
    public static class MissedSurveyItem extends BaseAdapter {
        private static LayoutInflater inflater = null;
        ArrayList<FirebaseSurvey> result;
        Activity context;

        public MissedSurveyItem(Activity mainActivity, ArrayList<FirebaseSurvey> prgmNameList) {
            result = prgmNameList;
            context = mainActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return result.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.survey_list, parent, false);
            TextView labelView = (TextView) rowView.findViewById(R.id.labelView);
            TextView valueView = (TextView) rowView.findViewById(R.id.valueView);
            TextView startedView = (TextView) rowView.findViewById(R.id.startedView);

            // 4. Set the text for textView
            labelView.setText(result.get(position).gettitle());
            final String surveyName = result.get(position).gettitle();
            final String surveyKey = result.get(position).getkey();
            final long timeSent = result.get(position).gettimeSent();
            final int triggerType = result.get(position).gettriggerType();
            long expiryTime = result.get(position).getexpiryTime();
            long expiryMillis = expiryTime * 60 * 1000;
            long deadline = result.get(position).gettimeSent() + expiryMillis;
            long timeToGo = deadline - System.currentTimeMillis();

            boolean begun = result.get(position).getbegun();
            if (begun)
                startedView.setText("Partially completed");

            int minutes = (int) (timeToGo / 60000);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateString = formatter.format(new Date(timeSent));
            if (timeToGo > 0)
                valueView.setText("Sent at " + dateString + "\nExpiring in " + (minutes + 1) + " minutes");
            else
                valueView.setText("Sent at " + dateString);

//            rowView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent resultIntent = new Intent(context, SurveyActivity.class);
//                    resultIntent.putExtra(SURVEY_ID, surveyKey);
//                    resultIntent.putExtra(SURVEY_NAME, surveyName);
//                    resultIntent.putExtra(TIME_SENT, timeSent);
//                    resultIntent.putExtra(TRIG_TYPE, triggerType);
//
//
//                    Context context = ApplicationContext.getContext();
//                    //This intent sends a broadcast to cancel the notification, if there is one
//                    Intent intended = new Intent();
//                    intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
//                    intended.putExtra(SURVEY_ID, surveyKey);
//                    context.sendBroadcast(intended);
//                    result.get(position).setbegun(); //Confirm that this survey has been started
//                    context.startActivity(resultIntent); //So we know which one to delete
//                }
//            });
            return rowView;
        }

    }
}
