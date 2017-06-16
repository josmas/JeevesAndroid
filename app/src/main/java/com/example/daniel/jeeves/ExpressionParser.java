package com.example.daniel.jeeves;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.daniel.jeeves.firebase.FirebaseExpression;
import com.example.daniel.jeeves.firebase.UserVariable;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.classifier.SensorDataClassifier;
import com.ubhave.sensormanager.config.SensorConfig;
import com.ubhave.sensormanager.config.pull.LocationConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.example.daniel.jeeves.ApplicationContext.BOOLEAN;
import static com.example.daniel.jeeves.ApplicationContext.DATE;
import static com.example.daniel.jeeves.ApplicationContext.LOCATION;
import static com.example.daniel.jeeves.ApplicationContext.NUMERIC;
import static com.example.daniel.jeeves.ApplicationContext.TIME;

/**
 * Created by Daniel on 09/06/15.
 */
public class ExpressionParser {

    public static final String AND = "Both True";
    public static final String OR = "Either True";
    public static final String LESS_THAN = "Less Than";
    public static final String GREATER_THAN = "Greater Than";
    public static final String EQUALS = "Equality";
    public static final String NOT_EQUALS = "Not True";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    //Sensor expression constants
    private static final String SENSOR = "selectedSensor";
    private static final String RESULT = "result";

    //Time expression constants
    private static final String TIME_DIFF = "timeDiff";
    private static final String BEFORE_AFTER = "beforeAfter";
    private static final String BEFORE = "before";
    private static final String AFTER = "after";
    private static final String TIME_VAR = "timeVar";
    private static final String MONTH = "1 month";
    private static final String WEEK = "1 week";
    private static final String DAY = "1 day";

    protected Context appContext;

    public ExpressionParser(Context appContext) {
        this.appContext = appContext;
    }

    public String evaluate(FirebaseExpression expr) {

        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
        if (expr.getisValue()) {
            return (expr).getvalue().toString();
        }
        else if (expr.getvariables() == null) {
            Map<String, Object> params = expr.getparams();
            if (params.containsKey(SENSOR)) { //a sensor expression
                String sensor = params.get(SENSOR).toString();
                String returns = params.get(RESULT).toString();
                //Now we want to poll this sensor here to see what it returns
                try {
                    int sensortype = SensorUtils.getSensorType(sensor);
                    SampleOnceTask sampler = new SampleOnceTask(sensortype);

                    //We need to irritatingly make an exception for location sensor

                    //This gets the last known location from our user prefs
                    //It then gets the location required in the test expression
                    //If they are roughly equal, it returns true!
                    if (sensortype == SensorUtils.SENSOR_TYPE_LOCATION) {
                        returns = userPrefs.getString(returns, "");
                        if (returns.isEmpty()) return FALSE;
                        String[] testLatLong = returns.split(";");

                        String lastLoc = userPrefs.getString("LastLocation", "");
                        if (lastLoc.isEmpty()) return FALSE;
                        String[] lastLatLong = lastLoc.split(";");

                        Location testLocation = new Location("");
                        testLocation.setLatitude(Double.parseDouble(testLatLong[0]));
                        testLocation.setLongitude(Double.parseDouble(testLatLong[1]));

                        Location lastLocation = new Location("");
                        lastLocation.setLatitude(Double.parseDouble(lastLatLong[0]));
                        lastLocation.setLongitude(Double.parseDouble(lastLatLong[1]));

                        if (testLocation.distanceTo(lastLocation) <= LocationConfig.LOCATION_CHANGE_DISTANCE_THRESHOLD)
                            return TRUE;
                        else
                            return FALSE;
                    }

                    //Otherwise we're looking at other sensor data (just accelerometer for now)
                    SensorData data = sampler.execute().get();
                    SensorDataClassifier classifier = SensorUtils.getSensorDataClassifier(sensortype);
                    if (classifier.isInteresting(data, SensorConfig.getDefaultConfig(sensortype), returns))
                        return TRUE; //Return true if it returns the result we want!
                    return FALSE;
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if (params.containsKey(TIME_DIFF)) { //a timediff expression
                String beforeAfter = params.get(BEFORE_AFTER).toString();
                String timeDiff = params.get(TIME_DIFF).toString();
                String dateStr = params.get(TIME_VAR).toString();
                long timevar = Long.parseLong(dateStr); //Milliseconds since epoch
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(timevar);
                //     SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                Log.d("DAY/MONTH/YEAR", c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR));
                long differenceInMillis = 0;
                long marginOfError = 0;
                switch (timeDiff) {
                    case MONTH:
                        differenceInMillis = 30L * 24L * 3600L * 1000L;
                        marginOfError = 24 * 3600 * 1000;
                        break; //Margin of error of a day
                    case WEEK:
                        differenceInMillis = 7L * 24L * 3600L * 1000;
                        marginOfError = 24 * 3600 * 1000;
                        break; //Margin of error of a day
                    case DAY:
                        differenceInMillis = 24L * 3600L * 1000L;
                        marginOfError = 24 * 3600 * 1000;
                        break; //Margin of error of a day
                    //       case "1 hour": differenceInMillis = 3600 * 1000; marginOfError = 3600*1000; break; //Margin of error of an hour
                }
                long currentTime = System.currentTimeMillis();
                c.setTimeInMillis(currentTime);
                long diff = timevar - currentTime;

                if (beforeAfter.equals(BEFORE)) {
                    if (diff < 0) return FALSE;
                    long error = diff - differenceInMillis;
                    if (Math.abs(error) < marginOfError) return TRUE;
                    return FALSE;

                } else if (beforeAfter.equals(AFTER)) {
                    if (diff > 0) return FALSE;
                    long error = (-diff) - differenceInMillis;
                    if (Math.abs(error) < marginOfError) return TRUE;
                    return FALSE;

                }

            } else {
                String name = (expr).getname();
                switch (expr.getvartype()) {
                    case LOCATION:
                        return userPrefs.getString(name, "");
                    case NUMERIC:
                        return Long.toString(userPrefs.getLong(name, 0));
                    case TIME:
                        return Long.toString(userPrefs.getLong(name, 0));
                    case DATE:
                        return Long.toString(userPrefs.getLong(name, 0));
                    case BOOLEAN:
                        return Boolean.toString(userPrefs.getBoolean(name, false));
                }
            }
        }

        List<FirebaseExpression> vars = expr.getvariables();
        FirebaseExpression lhs = vars.get(0);
        FirebaseExpression rhs = null;
        if (vars.size() > 1) //Sometimes expressions will only have one variable, i.e. NOT(var)
            rhs = vars.get(1);
        String operation = expr.getname();
        Log.d("OPERATION", "Operation is " + operation);
        if (operation.equals(AND))
            return Boolean.toString(Boolean.parseBoolean(evaluate(lhs)) && Boolean.parseBoolean(evaluate(rhs)));
        else if (operation.equals(OR))
            return Boolean.toString(Boolean.parseBoolean(evaluate(lhs)) || Boolean.parseBoolean(evaluate(rhs)));
        else if (operation.equals(EQUALS)) {
            return Boolean.toString((evaluate(lhs)).equals(evaluate(rhs)));
        } else if (operation.equals(NOT_EQUALS))
            return Boolean.toString(!(Boolean.parseBoolean(evaluate(lhs))));
        else if (operation.equals(LESS_THAN))
            return Boolean.toString(Long.parseLong(evaluate(lhs)) < Long.parseLong(evaluate(rhs)));
        else if (operation.equals(GREATER_THAN))
            return Boolean.toString(Long.parseLong(evaluate(lhs)) > Long.parseLong(evaluate(rhs)));
        return FALSE;
    }


    //Need this to sample once in our sensor expression
    public static class SampleOnceTask extends AsyncTask<Void, Void, SensorData> {
        private final ESSensorManager sensorManager;
        private final int sensorType;
        protected String errorMessage;

        public SampleOnceTask(int sensorType) throws ESException {
            this.sensorType = sensorType;
            sensorManager = ESSensorManager.getSensorManager(ApplicationContext.getContext());
        }

        @Override
        protected SensorData doInBackground(Void... params) {
            try {
                return sensorManager.getDataFromSensor(sensorType);
            } catch (ESException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return null;
            }
        }

    }
}
