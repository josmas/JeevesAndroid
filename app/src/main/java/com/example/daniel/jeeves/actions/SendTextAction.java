package com.example.daniel.jeeves.actions;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

import com.example.daniel.jeeves.ApplicationContext;

import java.util.Map;


/**
 * Created by Daniel on 27/05/15.
 */
public class SendTextAction extends FirebaseAction {

    public SendTextAction(Map<String,Object> params){
        setparams(params);

    }
    @Override
    public boolean execute() {
        String recipient = "";
        //TODO: Make this a little bit nicer at least
        String number;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());

        if(getparams().get("recipient") instanceof Map){
            recipient = ((Map<String,Object>)getparams().get("recipient")).get("name").toString();
                number = preferences.getString(recipient,"");
        }
        else
            number = (getparams().get("recipient").toString());

        String message = getparams().get("msgtext").toString();
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("0" +number, null, message, null, null);
        return true;
    }
}
