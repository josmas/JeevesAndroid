package com.jeevesandroid.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jeevesandroid.ApplicationContext;
import com.jeevesandroid.R;
import com.jeevesandroid.WelcomeActivity;
import com.jeevesandroid.firebase.FirebaseProject;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.jeevesandroid.ApplicationContext.STUDY_NAME;
import static com.jeevesandroid.ApplicationContext.USERNAME;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity{
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;


    public Activity getInstance() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //If we're not signed in, launch the sign-in activity
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
            //Maybe we can get the project HERE, so that when the app is destroyed and recreated, we're only allowed to carry
            //on once our project has been loaded.
            //This would even work if we restart while offline, because I think persistence is now enabled...?

            if (preferences.contains(STUDY_NAME)) {
                final FirebaseDatabase database = FirebaseUtils.getDatabase();
                SharedPreferences varPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
                String studyname = varPrefs.getString(STUDY_NAME, "");
                //If we've restarted the app we also want to reset the triggers NO WE BLOODY WELL DO NOT, this happens in SenseService!
//                SharedPreferences.Editor prefseditor = varPrefs.edit();
//                prefseditor.putStringSet("triggerids",new HashSet<String>());
//                prefseditor.commit();

                DatabaseReference projectRef = database.getReference(FirebaseUtils.PUBLIC_KEY).child(FirebaseUtils.PROJECTS_KEY).child(studyname);

                projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        FirebaseProject post = snapshot.getValue(FirebaseProject.class);
                        ApplicationContext.setCurrentproject(post);
                        if(post == null){
                            Toast.makeText(getInstance(),"OH NO IT WAS NULL",Toast.LENGTH_SHORT).show();
                            Log.d("OH NO","IT WAS NULL");
                            return;
                        }
                        //Okay, NOW we're safe to start the welcome activity, maybe...
                        Intent intent = new Intent(getInstance(), WelcomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

            }
            //Here, this is where the user has logged in previously, cleared their data and tried to sign in again.
            //Storing their credentials independent of the study they signed up to would require restructuring the database,
            //so for now this forces them to delete their account and start again.
            else if(!preferences.contains(USERNAME)) {
                Intent intent = new Intent(getInstance(), DeletedActivity.class);
                startActivity(intent);
                finish();
            }
            else
                startStudySignUp();
        }
    }
    private void startStudySignUp() {
        Intent intent = new Intent(this, StudySignupActivity.class);
        startActivity(intent);
        finish();
    }

}
