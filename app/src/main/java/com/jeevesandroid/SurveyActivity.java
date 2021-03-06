package com.jeevesandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.firebase.FirebaseSurvey;
import com.jeevesandroid.firebase.FirebaseUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jwetherell.heart_rate_monitor.HeartRateMonitor;
import com.ubhave.triggermanager.config.TriggerManagerConstants;
import com.ubhave.triggermanager.triggers.TriggerUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.jeevesandroid.ApplicationContext.COMPLETE;
import static com.jeevesandroid.ApplicationContext.COMPLETED_SURVEYS;
import static com.jeevesandroid.ApplicationContext.FINISHED_INTRODUCTION;
import static com.jeevesandroid.ApplicationContext.INIT_TIME;
import static com.jeevesandroid.ApplicationContext.LAST_SURVEY_SCORE;
import static com.jeevesandroid.ApplicationContext.STATUS;
import static com.jeevesandroid.ApplicationContext.SURVEY_ID;
import static com.jeevesandroid.ApplicationContext.SURVEY_NAME;
import static com.jeevesandroid.ApplicationContext.SURVEY_SCORE_DIFF;
import static com.jeevesandroid.ApplicationContext.TIME_SENT;
import static com.jeevesandroid.ApplicationContext.TRIG_TYPE;
import static com.jeevesandroid.ApplicationContext.UID;
import static com.jeevesandroid.ApplicationContext.getContext;

public class SurveyActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback,MediaPlayer.OnPreparedListener {
    public static final int OPEN_ENDED = 1;
    public static final int MULT_SINGLE = 2;
    public static final int MULT_MANY = 3;
    public static final int SCALE = 4;
    public static final int DATE = 5;
    public static final int GEO = 6;
    public static final int BOOLEAN = 7;
    public static final int NUMERIC = 8;
    public static final int TIME = 9;
    public static final int WIFI = 10;
    public static final int BLUETOOTH = 11;
    public static final int IMAGE = 12;
    public static final int TEXTPRESENT = 13;
    public static final int HEART = 14;
    public static final int AUDIO = 15;
    final Handler handler = new Handler();
    List<FirebaseQuestion> questions;
    int currentIndex = 0;
    List<String> answers; //For storing user's question data as we flip through
    AlertDialog.Builder finishalert;
    AlertDialog.Builder warningalert;
    Button btnNext;
    Button btnBack;
    ViewFlipper viewFlipper;
    EditText txtOpenEnded;
    EditText txtNumeric;
    RadioGroup grpBool;
    RadioGroup grpMultSingle;
    SeekBar seekBar;
 //   RadioGroup grpScale;
    LinearLayout grpMultMany;
    ListView lstWifi;
    ListView lstBluetooth;
    PhotoView photoView;
    TextView txtPresent;
    long finalscore = 0;
    TextView txtQNo;
    int currentQuestionCount = 0;
    String latlong;
    Animation slide_in_left, slide_out_right;
    GoogleMap map;
    int PLACE_PICKER_REQUEST = 1;
    DatabaseReference surveyRef;
    DatabaseReference completedSurveysRef;
    FirebaseAuth mFirebaseAuth;
    SharedPreferences prefs;
    boolean finished = false;
    long timeSent = 0;
    long initTime = 0;
    int triggerType = 0;
    FirebaseSurvey currentsurvey = null;
    ArrayList<CheckBox> allBoxes = new ArrayList<CheckBox>();

    private String surveyid;
    private Map<String, Object> myparams; //The parameters of the current question
    private GoogleApiClient mGoogleApiClient;

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    public void onBackPressed() {

        return;
    }
    protected void onStop() {
        super.onStop();
        if(currentsurvey != null) //Sometimes it's null if the activity is accessed from the lock screen
            currentsurvey.setanswers(answers); //Save the partially completed stuff
        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                surveyRef.removeEventListener(this);
                if (finished == false)
                    surveyRef.setValue(currentsurvey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Error", "we have an error here");
            }
        });
    }
//    WifiManager mWifiManager;
//    private ArrayList<String> mNetworkList = new ArrayList<String>();
//    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context c, Intent intent) {
//            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                Log.d("HELLO","IT@S MEEEEEEE");
//                List<ScanResult> mScanResults = mWifiManager.getScanResults();
//                mNetworkList.clear();
//                for (ScanResult mScanResult : mScanResults) {
//                    Log.d("RESULT","ound " + mScanResult.SSID);
//                    if(!mNetworkList.contains(mScanResult.SSID))
//                        mNetworkList.add(mScanResult.SSID);
//                }
//                lstWifi.setAdapter(new ArrayAdapter<String>(c,android.R.layout.simple_list_item_1,mNetworkList));
//                // add your logic here
//            }
//        }
//    };
//
//
//    private BluetoothAdapter mBluetoothAdapter;
//    private ArrayList<String> mDeviceList = new ArrayList<String>();
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent
//                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if(device.getName() == null)
//                    mDeviceList.add("Unknown device" + "\n" + device.getAddress());
//                else
//                    mDeviceList.add(device.getName() + "\n" + device.getAddress());
//
//                Log.i("BT", device.getName() + "\n" + device.getAddress());
//                lstBluetooth.setAdapter(new ArrayAdapter<String>(context,
//                        android.R.layout.simple_list_item_1, mDeviceList));
//            }
//        }
//    };

    @Override
    public void onDestroy(){
        super.onDestroy();
     //   unregisterReceiver(mReceiver);
     //   unregisterReceiver(mWifiScanReceiver);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("STARTED","Started the thing");
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_missed_survey);
        ActionBar actionBar = getSupportActionBar();

        setContentView(R.layout.activity_survey);

        surveyid = getIntent().getStringExtra(SURVEY_ID);
        initTime = getIntent().getLongExtra(INIT_TIME,0);
        timeSent = getIntent().getLongExtra(TIME_SENT,0);
        triggerType = getIntent().getIntExtra(TRIG_TYPE,0);
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Here's a fiddly wee bit of code that makes our starting survey MANDATORY (i.e. the user can't do anything before
        //they complete it)
        if(triggerType == TriggerUtils.TYPE_CLOCK_TRIGGER_BEGIN){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }
        else{
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        surveyRef = FirebaseUtils.PATIENT_REF.child("incomplete").child(surveyid);
        completedSurveysRef = FirebaseUtils.PATIENT_REF.child("complete");

        txtOpenEnded = ((EditText) findViewById(R.id.txtOpenEnded));
        txtNumeric = ((EditText) findViewById(R.id.txtNumeric));
        grpBool = ((RadioGroup) findViewById(R.id.grpBool));
        grpMultMany = ((LinearLayout) findViewById(R.id.grpMultMany));
        grpMultSingle = ((RadioGroup) findViewById(R.id.grpMultSingle));
       // grpScale = ((RadioGroup) findViewById(R.id.grpScale));
        seekBar = ((SeekBar) findViewById(R.id.seekBar));
        lstBluetooth = ((ListView)findViewById(R.id.lstBluetooth));

        lstWifi = ((ListView)findViewById(R.id.lstWifi));
        photoView = (PhotoView) findViewById(R.id.photo_view2);
        txtPresent = (TextView)findViewById(R.id.txtPresent);
//
//        mWifiManager = (WifiManager) ApplicationContext.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        registerReceiver(mWifiScanReceiver,
//                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        mWifiManager.startScan();

//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filter);
        txtQNo = ((TextView) findViewById(R.id.txtQno));
        txtQNo.setText("Question 1");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        finishalert = new AlertDialog.Builder(this);
        finishalert.setTitle("Thank you!");

        btnNext = ((Button) findViewById(R.id.btnNext));
        btnBack = ((Button) findViewById(R.id.btnBack));
        btnBack.setEnabled(false);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);
        viewFlipper.setInAnimation(slide_in_left);
        viewFlipper.setOutAnimation(slide_out_right);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextQ();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backQ();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Patient finished the survey!
        finishalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                currentsurvey.setanswers(null); //remove the unencoded answers
                currentsurvey.settimeFinished(System.currentTimeMillis());
                ArrayList<String> changedVariables = new ArrayList<String>();
                String concatAnswers = "";
                for (int i = 0; i < answers.size(); i++) {
                    String answer = answers.get(i);
                    FirebaseQuestion correspondingQuestion = questions.get(i);
                    concatAnswers += answer + ";";
                    if (correspondingQuestion.getassignedVar() != null) { //If we need to assign this answer to a variable
                        String varname = currentsurvey.getquestions().get(i).getassignedVar();
                        changedVariables.add(varname);
                        SharedPreferences.Editor editor = prefs.edit();
                        if (!answer.isEmpty()) {
                            editor.putString(varname, answer); //Put the variable into the var
                            Log.d("PUTTING","Putting variable " + varname + " AS " + answer);
                        }
                        editor.commit();
                    }
                    if (correspondingQuestion.getparams() != null && correspondingQuestion.getparams().containsKey("assignToScore"))
                        if (!answer.isEmpty())
                            finalscore += Long.parseLong(answer);
                }
                //Encode answers with a symmetric key
                currentsurvey.setencodedAnswers(FirebaseUtils.symmetricEncryption(concatAnswers));
                //Encode the symmetric key with public/private, send this along with the survey too
                currentsurvey.setencodedKey(FirebaseUtils.getSymmetricKey());
                currentsurvey.setscore(finalscore);

                //Add the relevant information to the survey ref of this project
                //We need to know that it was completed, the time it took for the patient to begin, and the time it took them to finish.
                //WE ALSO NEED TO KNOW WHO THE PATIENT WAS
                //We also need to know what it was that triggered the survey (i.e. button press, sensor trigger, etc).
                //Actually we need the full shebang, including the patient's encoded answers
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

                Map<String,Object> surveymap = new HashMap<String,Object>();
                surveymap.put(STATUS,1);
                if(triggerType != TriggerUtils.TYPE_SENSOR_TRIGGER_BUTTON && initTime > timeSent) //Then this was a button trigger and the init time doesn't count
                    surveymap.put(INIT_TIME,initTime-timeSent);
             //   Toast.makeText(getInstance(),"Init time" + initTime + ". timesent " + timeSent,Toast.LENGTH_SHORT).show();
                surveymap.put(COMPLETE,System.currentTimeMillis());
                surveymap.put(TRIG_TYPE,triggerType);
                surveymap.put(UID,prefs.getString(UID,""));
                surveymap.put("encodedAnswers",currentsurvey.getencodedAnswers());
                surveymap.put("encodedKey",currentsurvey.getencodedKey());
                FirebaseUtils.SURVEY_REF.child(currentsurvey.getsurveyId()).push().setValue(surveymap);
                //Update the various Survey-relevant variables

                SharedPreferences.Editor editor = prefs.edit();
                long oldscore = prefs.getLong(LAST_SURVEY_SCORE, 0);
                long difference = finalscore - oldscore;
                editor.putLong(LAST_SURVEY_SCORE, finalscore);
                editor.putLong(SURVEY_SCORE_DIFF, difference);
                long totalCompletedSurveyCount = prefs.getLong(COMPLETED_SURVEYS, 0);
                totalCompletedSurveyCount++;
                editor.putLong(COMPLETED_SURVEYS, totalCompletedSurveyCount);
                long thisCompletedSurveyCount = prefs.getLong(currentsurvey.gettitle() + "-Completed", 0);
                thisCompletedSurveyCount++;
                editor.putLong(currentsurvey.gettitle() + "-Completed", thisCompletedSurveyCount);
                editor.commit();

                //SEND A BROADCAST TO LISTENING SURVEY TRIGGERS
                Intent intended = new Intent();
                intended.setAction(TriggerManagerConstants.ACTION_NAME_SURVEY_TRIGGER);
                intended.putExtra(SURVEY_NAME, currentsurvey.gettitle());
                intended.putExtra("completed", thisCompletedSurveyCount);
                intended.putExtra("result", true);
                intended.putExtra(TIME_SENT, currentsurvey.gettimeSent()); //need this for removing SurveyAction notifications
                intended.putExtra("changedVariables", changedVariables);
                sendBroadcast(intended);
                surveyRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        DatabaseReference newPostRef = completedSurveysRef.push();
                        newPostRef.setValue(currentsurvey); //Maybe this needs tobe made explicit?
                        surveyRef.removeEventListener(this);
                        surveyRef.removeValue();
                        handler.removeCallbacksAndMessages(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Error", "we have an error THERE");

                    }
                });
                finished = true;

                //We should write this to Shared Preferences so that, if the app closes, we know we've already
                //finished the introductory survey
                editor.putBoolean(FINISHED_INTRODUCTION,finished);
                editor.commit();
                finish();
            }
        });

        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentsurvey = snapshot.getValue(FirebaseSurvey.class);
                if (currentsurvey != null) {
                    surveyRef.removeEventListener(this);
                    questions = currentsurvey.getquestions();

                    if (currentsurvey.getbegun()) {
                        timeSent = getIntent().getLongExtra("timeSent", 0);
                        answers = currentsurvey.getanswers(); //Pre-populated answers
                        int size = answers.size();
                        for (int i = 0; i < (questions.size() - size); i++)
                            answers.add("");
                    } else {
                        timeSent = getIntent().getLongExtra("timeSent", 0);
                        answers = new ArrayList<>();
                        for (int i = 0; i < questions.size(); i++)
                            answers.add("");
                    }

                    currentsurvey.setbegun(); //Confirm that this survey has been started

                    long expiryTime = currentsurvey.getexpiryTime();
                    long expiryMillis = expiryTime * 60 * 1000;
                    long deadline = timeSent + expiryMillis;
                    long timeToGo = deadline - System.currentTimeMillis();

                    //User has missed the survey, having previously triggered the notification
                    if (timeToGo > 0) {

                        warningalert = new AlertDialog.Builder(getInstance());
                        warningalert.setCancelable(false);
                        warningalert.setTitle("Sorry, your time to complete this survey has expired");
                        warningalert.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        });
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!getInstance().isDestroyed()) //If the activity isn't running we don't want the timeout to happen
                                    warningalert.show();
                            }
                        }, timeToGo);
                    }
                    launchQuestion(questions.get(0), "forward");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public SurveyActivity getInstance() {
        return this;
    }

    private void handleOpenEnded() {
        String answer = answers.get(currentIndex);
        if (!answer.isEmpty())
            txtOpenEnded.setText(answer);
        else {
            answers.set(currentIndex, "");
            txtOpenEnded.setText("");
        }
        txtOpenEnded.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                answers.set(currentIndex, txtOpenEnded.getText().toString());
            }
        });
        txtOpenEnded.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

    }

    private void handleImageView(){
        photoView.setVisibility(View.VISIBLE);
        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        String imageName = (String)options.get("image");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference gsReference = storage.getReferenceFromUrl("gs://jeeves-27914.appspot.com/" + imageName);
        Log.d("REFERENC","ref is " + gsReference.getPath());
        // File localFile = null;
        final File localFile;
        // String root = Environment.getExternalStorageDirectory().toString();
        File externalDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//        File myDir = new File(Environment.getExternalStorageDirectory(), "jeeves");
//        Log.d("IT IS ",myDir.getAbsolutePath());
        if(!externalDir.exists())
            externalDir.mkdirs();

        localFile = new File(externalDir,imageName);
        if(localFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            photoView.setImageBitmap(myBitmap);
            return;
        }
        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("SUCCSS","Success load");
                Log.d("Path","path is " + localFile);
                Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                photoView.setImageBitmap(myBitmap);                }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("FAIL","Failed to load");
                exception.printStackTrace();
            }
        });
    }
    private void handleTextView(){
        txtPresent.setVisibility(View.VISIBLE);
        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        String textToShow = (String)options.get("text");
        txtPresent.setText(textToShow);
    }
    private void handleMultSingle() {
        grpMultSingle.removeAllViews();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        Iterator<Object> opts = options.values().iterator();
        ArrayList<RadioButton> allButtons = new ArrayList<RadioButton>();
        while (opts.hasNext()) {
            String option = opts.next().toString();
            final RadioButton button = new RadioButton(this);
            button.setText(option);
            button.setTextSize(20);
            grpMultSingle.addView(button);
            allButtons.add(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answers.set(currentIndex, button.getText().toString());
                }
            });
        }
        String answer = answers.get(currentIndex);
        if (!answer.isEmpty())
            for (RadioButton but : allButtons) {
                if (but.getText().equals(answer.toString()))
                    but.setChecked(true);
            }
        else
            answers.set(currentIndex, "");
    }

    private void handleMultMany() {
        grpMultMany.removeAllViews();
        allBoxes.clear();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        Iterator<Object> opts = options.values().iterator();
        while (opts.hasNext()) {
            String option = opts.next().toString();
            CheckBox box = new CheckBox(this);
            box.setText(option);
            box.setTextSize(20);
            grpMultMany.addView(box);
            allBoxes.add(box);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String newanswers = "";
                    for (CheckBox allBox : allBoxes) {
                        if (allBox.isChecked())
                            newanswers += allBox.getText().toString() + ",";
                    }
                    answers.set(currentIndex, newanswers);
                }
            });
        }

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String[] allanswers = answer.split(",");
            for (String ans : allanswers) {
                for (CheckBox box : allBoxes)
                    if (box.getText().equals(ans))
                        box.setChecked(true);

            }
        } else
            answers.set(currentIndex, "");

    }

    private void handleScale() {
//        grpScale.removeAllViews();
//        grpScale.clearCheck();
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        int entries = Integer.parseInt(options.get("number").toString());
        ArrayList<String> labels = (ArrayList<String>) options.get("labels");
        TextView txtBegin = ((TextView)findViewById(R.id.txtBegin));
        TextView txtMiddle = ((TextView)findViewById(R.id.txtMiddle));
        TextView txtEnd = ((TextView)findViewById(R.id.txtEnd));
        TextView[] views = new TextView[]{txtBegin,txtMiddle,txtEnd};
        for(int i = 0; i < views.length; i++){
            if(labels.get(i) != null)
            views[i].setText(labels.get(i));
        }
        String answer = answers.get(currentIndex);
        seekBar.setMax(entries);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                Log.d("PREOG","set rogress to " + progress);
                answers.set(currentIndex, Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        //Do this here so that it gets filled with a default value if the user doesn't touch it
        if (!answer.isEmpty())
            seekBar.setProgress(Integer.parseInt(answer));
        else {
            seekBar.setProgress(entries / 2);
            answers.set(currentIndex, Integer.toString(entries/2));
        }
            //        for (int i = 0; i < entries; i++) {
//            final RadioButton button = new RadioButton(this);
//            button.setId(i + 1);
//            button.setText((i + 1) + "   " + labels.get(i));
//            button.setTextSize(20);
//        //    grpScale.addView(button);
//            if (!answer.isEmpty() && Integer.parseInt(answer) == (i + 1)) {
//                button.setChecked(true);
//            }
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    answers.set(currentIndex, Integer.toString(button.getId()));
//                }
//            });
//        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleDate() {
        final Calendar calendar = Calendar.getInstance();
        final DatePicker picker = (DatePicker) findViewById(R.id.datePicker2);
        String answer = answers.get(currentIndex);
        picker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
            }
        });

        if (!answer.isEmpty()) {
            String time = answer.toString();
            calendar.setTimeInMillis(Long.parseLong(time));
            picker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleTime() {
        final Calendar calendar = Calendar.getInstance();
        final Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        TimePicker tpicker = (TimePicker) findViewById(R.id.timePicker2);
        String answer = answers.get(currentIndex);
        tpicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR_OF_DAY, i);
                calendar.set(Calendar.MINUTE, i1);
                long msFromMidnight = calendar.getTimeInMillis() - midnight.getTimeInMillis();
                answers.set(currentIndex, Long.toString(msFromMidnight));

            }
        });

        if (!answer.isEmpty()) {
            String time = answer.toString();
            calendar.setTimeInMillis(midnight.getTimeInMillis() + Long.parseLong(time));
            tpicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            tpicker.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            answers.set(currentIndex, Long.toString(calendar.getTimeInMillis()- midnight.getTimeInMillis()));
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = place.getName().toString();

                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                TextView txtPlaceName = (TextView) findViewById(R.id.txtPlaceName);
                txtPlaceName.setText(toastMsg);
                LatLng coords = place.getLatLng();
                map.moveCamera(CameraUpdateFactory.newLatLng(coords)); //This oughta put our camera at the current location
                map.addMarker(new MarkerOptions()
                        .position(coords)
                        .title(place.getName().toString()));
                String answer = coords.latitude + ":" + coords.longitude + ";";
                answers.set(currentIndex, answer);
            }
        }
//        if(requestCode == REQUEST_ENABLE_BT)
//            mBluetoothAdapter.startDiscovery();
        if(requestCode == 1234){ //code i've defined for heart but cba making a constant
            Button btnStart = (Button)findViewById(R.id.btnStart);
            btnStart.setText("Heart rate acquired");
            btnStart.setEnabled(false);
            PhotoView heartview = (PhotoView)findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.fingerdone);
            int result = data.getIntExtra("result",0);
            answers.set(currentIndex,Integer.toString(result));

        }

    }

    private void handleGeo() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        final TextView txtPlaceName = (TextView) findViewById(R.id.txtPlaceName);

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String location = answer.toString();
            String[] locationbits = location.split(":");
            double latitude = Double.parseDouble(locationbits[0]);
            double longitude = Double.parseDouble(locationbits[1]);
            LatLng coords = new LatLng(latitude, longitude);
            String placename = locationbits[2];
            txtPlaceName.setText(placename);
            map.moveCamera(CameraUpdateFactory.newLatLng(coords)); //This oughta put our camera at the current location
            map.addMarker(new MarkerOptions()
                    .position(coords)
                    .title(placename));
        } else {
            answers.set(currentIndex, "");

        }

        Button btnPlacePicker = (Button) findViewById(R.id.btnPlacePicker);

        btnPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getInstance()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void handleNumeric() {
        if (answers.get(currentIndex) != null)
            txtNumeric.setText(answers.get(currentIndex));
        else {
            answers.set(currentIndex, "");
            txtNumeric.setText("");
        }
        txtNumeric.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) {
                answers.set(currentIndex, txtNumeric.getText().toString());
            }
        });
        txtNumeric.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    private void handleBoolean() {
        grpBool.removeAllViews();
        RadioButton trueButton = new RadioButton(this);
        trueButton.setText("Yes");
        trueButton.setTextSize(24);
        RadioButton falseButton = new RadioButton(this);
        falseButton.setText("No");
        falseButton.setTextSize(24);
        grpBool.addView(trueButton);
        grpBool.addView(falseButton);
        trueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "true");
            }
        });
        falseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answers.set(currentIndex, "false");
            }
        });

        String answer = answers.get(currentIndex);
        if (answer != null && !answer.equals(""))
            if (answer.toString().equals("true"))
                trueButton.setChecked(true);
            else
                falseButton.setChecked(true);
        else
            answers.set(currentIndex, "");
    }

//    private void handleWifi(){
//        lstWifi.setAdapter(new ArrayAdapter<String>(ApplicationContext.getContext(),android.R.layout.simple_list_item_1,mNetworkList));
//        lstWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String wifiText = mNetworkList.get(position);
//                answers.set(currentIndex,wifiText);
//            }
//        });
//
//    }
//    int REQUEST_ENABLE_BT = 99;
//
//    private void handleBluetooth(){
//       // lstBluetooth.setAdapter(new ArrayAdapter<String>(ApplicationContext.getContext(),
//        //        android.R.layout.simple_list_item_1, mDeviceList));
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(mBluetoothAdapter == null)return;
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//
//        }
//        else
//            mBluetoothAdapter.startDiscovery();
//        lstBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String bluetoothText = mDeviceList.get(position);
//                answers.set(currentIndex,bluetoothText);
//            }
//        });
//
//    }

    private void handleAudio(){
        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        final String audioName = (String)options.get("audio");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference gsReference = storage.getReferenceFromUrl("gs://jeeves-27914.appspot.com/" + audioName);
        Log.d("REFERENC","ref is " + gsReference.getPath());
        // File localFile = null;
        final File localFile;
        // String root = Environment.getExternalStorageDirectory().toString();
        final File externalDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//        File myDir = new File(Environment.getExternalStorageDirectory(), "jeeves");
//        Log.d("IT IS ",myDir.getAbsolutePath());
        if(!externalDir.exists())
            externalDir.mkdirs();
        final View.OnClickListener pauseListenr = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getInstance(),"Please listen to the whole clip!",Toast.LENGTH_SHORT).show();
            }
        };
        final Button btnStart = (Button)findViewById(R.id.audioBtnStart);
        final Button btnPause = (Button)findViewById(R.id.audioBtnPause);

        btnNext.setOnClickListener(pauseListenr);
        btnBack.setOnClickListener(pauseListenr);
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextQ();
                    }
                });
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        backQ();
                    }
                });
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnStart.setEnabled(false);
                        btnPause.setEnabled(true);
                        btnNext.setOnClickListener(pauseListenr);
                        btnBack.setOnClickListener(pauseListenr);

                        //      mediaPlayer.start();
                    }
                });
                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnStart.setEnabled(true);
                        btnPause.setEnabled(false);
                        //     mediaPlayer.pause();
                    }
                });
                //     mediaPlayer.reset();
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    File f = new File(externalDir,audioName);
                    final Uri myUri = Uri.fromFile(new File(f.getAbsolutePath()));
                    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                    mediaPlayer.setOnPreparedListener(getInstance());
                    mediaPlayer.prepareAsync(); // prepare async to not block main thread

                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        });
        localFile = new File(externalDir,audioName);
        final Uri myUri = Uri.fromFile(new File(localFile.getAbsolutePath()));
        if(localFile.exists()){
            Log.d("SUCCESS","Successful load!");
            // initialize Uri here
            try {
                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                mediaPlayer.setOnPreparedListener(getInstance());
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("SUCCSS","Success load");
                Log.d("Path","path is " + localFile);
                // initialize Uri here
                try {
                    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                    mediaPlayer.setOnPreparedListener(getInstance());
                    mediaPlayer.prepareAsync(); // prepare async to not block main thread
                } catch (IOException e) {
                    e.printStackTrace();
                }                }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("FAIL","Failed to load");
                exception.printStackTrace();
            }
        });

        PhotoView audio = (PhotoView)findViewById(R.id.audioPhotoview);
        audio.setImageResource(R.drawable.finger);


    }
    /** Called when MediaPlayer is ready */
    public void onPrepared(final MediaPlayer player) {
        final Button btnStart = (Button)findViewById(R.id.audioBtnStart);
        final Button btnPause = (Button)findViewById(R.id.audioBtnPause);
        PhotoView audioview = (PhotoView)findViewById(R.id.audioPhotoview);
        audioview.setImageResource(R.drawable.audio);
        btnStart.setEnabled(true);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                player.start();
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                player.pause();
            }
        });
    }
    private void handleHeart(){
        if(answers.get(currentIndex).isEmpty()){
            Button btnStart = (Button)findViewById(R.id.btnStart);
            btnStart.setText("Start sensing");
            btnStart.setEnabled(true);
            PhotoView heartview = (PhotoView)findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.finger);
        }
        else{
            Button btnStart = (Button)findViewById(R.id.btnStart);
            btnStart.setText("Heart rate acquired");
            btnStart.setEnabled(false);
            PhotoView heartview = (PhotoView)findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.fingerdone);
        }
        Button btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getInstance(),HeartRateMonitor.class);
                startActivityForResult(intent, 1234);
            }
        });
    }

    private void launchQuestion(FirebaseQuestion question, String direction) {

        String questionText = question.getquestionText();
        int questionType = (int) question.getquestionType();
        myparams = question.getparams();

        //QUESTION SKIPPING
        FirebaseQuestion conditionQuestion = null;
//            if(myparams != null)
        if (question.getconditionQuestion() != null)
            conditionQuestion = question.getconditionQuestion();

        if (conditionQuestion != null) {
            String questionid = conditionQuestion.getquestionId();
            int conditionQuestionIndex = 0;
            long conditionQuestionType = 0;
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).getquestionId().equals(questionid)) {
                    conditionQuestionIndex = i;
                    conditionQuestionType = questions.get(i).getquestionType();
                    break;
                }
            }
            String expectedanswer = question.getconditionConstraints();
            String actualanswer = answers.get(conditionQuestionIndex);
            boolean satisfied = false;
            //TODO: This whole section is absolutely shite and it's just to get it working.
            String[] constraints = expectedanswer.split(";");
            if (actualanswer.length() > 0)
                if (constraints.length > 1) {
                    switch (constraints[0]) {
                        case "less than":
                            if (Integer.parseInt(actualanswer) < Integer.parseInt(constraints[1]))
                                satisfied = true;
                            break;
                        case "more than":
                            if (Integer.parseInt(actualanswer) > Integer.parseInt(constraints[1]))
                                satisfied = true;
                            break;
                        case "equal to":
                            if (Integer.parseInt(actualanswer) == Integer.parseInt(constraints[1]))
                                satisfied = true;
                            break;
                        case "before":
                        case "after":
                            long constraintDateTime = Long.parseLong(constraints[1]);
                            long actualDateTime = Long.parseLong(actualanswer);
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.HOUR_OF_DAY, 0);
                            c.set(Calendar.MINUTE, 0);
                            c.set(Calendar.SECOND, 0);
                            long constraintMillis = 0;
                            if (conditionQuestionType == DATE)
                                constraintMillis = constraintDateTime;
                            else if (conditionQuestionType == TIME)
                                constraintMillis = c.getTimeInMillis() + constraintDateTime;

                            if (constraints[0].equals("before") && actualDateTime < constraintMillis)
                                satisfied = true;
                            if (constraints[0].equals("after") && actualDateTime > constraintMillis)
                                satisfied = true;
                            break;
                    }
                } else {
                    String[] potentialActualAnswers = actualanswer.split(";");
                    for (String answer : potentialActualAnswers) {
                        if (constraints[0].equals(answer)) //If we just have one part, then the part is our expected answer
                            satisfied = true;
                    }
                }
            if (satisfied) {

            } else { //This should hopefully skip to the next question
                if (direction.equals("forward"))
                    nextQ();
                else if (direction.equals("back"))
                    backQ();
                return;
            }

        }
        if (direction.equals("forward"))
            currentQuestionCount++;
        else if (direction.equals("back"))
            currentQuestionCount--;

        TextView questionView = (TextView) findViewById(R.id.txtQuestion);
        txtQNo.setText("Part " + (currentQuestionCount));

        if(questionType == IMAGE){
            questionView.setVisibility(View.INVISIBLE);
            viewFlipper.setVisibility(View.INVISIBLE);
            txtPresent.setVisibility(View.INVISIBLE);
            handleImageView();
            return;
        }
        else if(questionType == TEXTPRESENT){

            ScrollView scroller = (ScrollView)findViewById(R.id.scroll);
            scroller.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.INVISIBLE);
            questionView.setVisibility(View.INVISIBLE);
            viewFlipper.setVisibility(View.INVISIBLE);

            handleTextView();
            return;

        }

        questionView.setVisibility(View.VISIBLE);
        viewFlipper.setVisibility(View.VISIBLE);
        txtPresent.setVisibility(View.INVISIBLE);
        photoView.setVisibility(View.INVISIBLE);
        ScrollView scroller = (ScrollView)findViewById(R.id.scroll);
        scroller.setVisibility(View.INVISIBLE);
    //Cheap hack for now
        if(questionType == HEART)
            viewFlipper.setDisplayedChild(11);
        else if(questionType == AUDIO)
            viewFlipper.setDisplayedChild(12);
        else
            viewFlipper.setDisplayedChild(questionType -1);
        switch (questionType) {
            case OPEN_ENDED:
                handleOpenEnded();
                break;
            case MULT_SINGLE:
                handleMultSingle();
                break;
            case MULT_MANY:
                handleMultMany();
                break;
            case SCALE:
                handleScale();
                break;
            case DATE:
                handleDate();
                break;
            case TIME:
                handleTime();
                break;
            case GEO:
                try {
                    handleGeo();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
            case BOOLEAN:
                handleBoolean();
                break;
            case NUMERIC:
                handleNumeric();
                break;
//            case WIFI:
//                handleWifi();
//                break;
//            case BLUETOOTH:
//                handleBluetooth();
//                break;
            case HEART:
                handleHeart();
                break;
            case AUDIO:
                handleAudio();
                break;
        }
        questionView.setText(questionText);
    }

    public void backQ() {
        currentIndex--;
        if (currentIndex < 1)
            btnBack.setEnabled(false);
        launchQuestion(questions.get(currentIndex), "back");
    }


    public void nextQ() {
        if(questions.get(currentIndex).getisMandatory() && answers.get(currentIndex).isEmpty()) {
            Toast.makeText(this,"This question is mandatory!",Toast.LENGTH_SHORT).show();
            return;
        }
        currentIndex++;
        if (currentIndex == questions.size()) {
            finishalert.setCancelable(false); //Once they're done they're done
            finishalert.show();
            return;
        }
        if (currentIndex > questions.size()) return; //safety net
        btnBack.setEnabled(true);
        launchQuestion(questions.get(currentIndex), "forward");
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        LatLng sydney = new LatLng(-33.867, 151.206);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
        map.getUiSettings().setZoomControlsEnabled(true);
        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        double lat, lng;
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        } else {
            lat = 56.341703;
            lng = -2.792;
        }
        LatLng coordinate = new LatLng(lat, lng);
        map.moveCamera(CameraUpdateFactory.zoomTo(10));

        map.moveCamera(CameraUpdateFactory.newLatLng(coordinate)); //This oughta put our camera at the current location
        latlong = "Lat: " + coordinate.latitude + ",  Long: " + coordinate.longitude;
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng GoogleMap) {
                MarkerOptions options = new MarkerOptions();
                MarkerOptions opts = options.position(GoogleMap);
                map.clear();
                map.addMarker(opts);
                answers.set(currentIndex, opts.getPosition().latitude + ":" + opts.getPosition().longitude);
                Log.d("MAP", "just set it to " + answers.get(currentIndex));
                map.moveCamera(CameraUpdateFactory.newLatLng(GoogleMap)); //This oughta put our camera at the current location
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        });
    }

}