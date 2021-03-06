package com.jeevesandroid.firebase;

import java.util.List;

/**
 * Created by Daniel on 29/04/2016.
 */
public class FirebaseSurvey {

    int triggerType;
    long timeAlive;
    long expiryTime;
    long timeSent;
    long timeFinished;
    long score;
    long id;
    List<FirebaseQuestion> questions;
    String description;
    String type;
    String title;
    String surveyId;
    long xPos;
    long yPos;
    String encodedAnswers;
    String encodedKey;
    boolean begun; //Has the user begun completing the survey?
    List<String> answers;
    String key;
    public FirebaseSurvey(){

    }

    public int gettriggerType() { return triggerType; }

    public void settriggerType(int triggerType) { this.triggerType = triggerType; }

    public long getexpiryTime() {
        return expiryTime;
    }

    public void setexpiryTime(long expiryTime){this.expiryTime = expiryTime;}

    public long getid() {
        return id;
    }

    public String getdescription() { return description; }

    public List<FirebaseQuestion> getquestions() {
        return questions;
    }

    public List<String> getanswers(){ return answers; }

    public String gettype() {
        return type;
    }

    public long getxPos() {
        return xPos;
    }

    public long getyPos() {
        return yPos;
    }

    public long gettimeSent(){ return timeSent; }

    public long gettimeAlive(){ return timeAlive; }

    public long gettimeFinished(){ return timeFinished; }

    public long getscore(){ return score; }

    public void setscore(long score){this.score = score;}

    public String getencodedAnswers(){
        return encodedAnswers;
    }

    public void setencodedAnswers(String encodedAnswers){
        this.encodedAnswers = encodedAnswers;
    }

    public String getencodedKey() { return encodedKey; }

    public void setencodedKey(String encodedKey) { this.encodedKey = encodedKey; }
    public String getsurveyId(){
        return surveyId;
    }
    public void setsurveyId(String id){
        this.surveyId = id;
    }
    public String gettitle(){
        return title;
    }
    public void settitle(String title){
        this.title = title;
    }

    public void setkey(String key){
        this.key = key;
    }
    public String getkey(){
        return key;
    }
    public void settimeSent(long timeSent){ this.timeSent = timeSent;}
    public void setanswers( List<String> answers){
        this.answers = answers;
    }
    public void settimeFinished(long timeFinished){
        this.timeFinished = timeFinished;
    }
    public boolean getbegun(){return begun;}
    public void setbegun(){this.begun = true;}
}
