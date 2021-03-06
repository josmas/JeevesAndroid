package com.jeevesandroid.firebase;

import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.jeevesandroid.ApplicationContext;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jeevesandroid.R;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static com.ubhave.datastore.db.DatabaseStorage.TAG;

/**
 * Created by Daniel on 24/05/2017.
 */

public class FirebaseUtils {

    //Database keys
    public static String PUBLIC_KEY = "public";
    public static String PRIVATE_KEY = "private";
    public static String PROJECTS_KEY = "projects";
    public static String PATIENTS_KEY = "patients";
    public static String SURVEYS_KEY = "surveys";
    public static String SURVEYDATA_KEY = "surveydata";
    public static String SENSORDATA_KEY = "sensordata";

    //Variable types
    public static final String BOOLEAN = "Boolean";
    public static final String NUMERIC = "Numeric";
    public static final String LOCATION = "Location";
    public static final String TIME = "Time";
    public static final String DATE = "Date";
    public static final String TEXT = "Text";

    public static DatabaseReference PATIENT_REF;
    public static DatabaseReference SURVEY_REF;

    public static String SYMMETRICKEY;

    public static String getSymmetricKey(){
        return encodeKey(SYMMETRICKEY);
    }
    public static String symmetricEncryption(String answers){
        Log.d("BEFORE","answers are " + answers);
        SecretKeySpec sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("any data used as random seed".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
            SYMMETRICKEY = Base64.encodeToString(sks.getEncoded(),Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "AES secret key spec error");
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(answers.getBytes());
            String base64 = Base64.encodeToString(encodedBytes,Base64.NO_WRAP);

            byte[] decodedBytes = null;
            try {
                Cipher c2 = Cipher.getInstance("AES");
                c2.init(Cipher.DECRYPT_MODE, sks);
                decodedBytes = c2.doFinal(encodedBytes);
                Log.d("DECODE","DECODED IS " + new String(decodedBytes));
            } catch (Exception e) {
                Log.e(TAG, "AES decryption error");
            }
            return base64;
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
        return null;
        // Decode the encoded data with AES


    }

    //Encryption for sensitive data
    public static String encodeKey(String symmetricKey){
        String pubKey = ApplicationContext.getProject().getpubKey();
        byte[] keyBytes = Base64.decode(pubKey,Base64.DEFAULT);
        //  byte[] keyBytes = Base64.decodeBase64(pubKey);
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return encryptText(symmetricKey,kf.generatePublic(X509publicKey));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String encryptText(String msg, PublicKey key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init(Cipher.ENCRYPT_MODE, key);
        String base64 = Base64.encodeToString(cipher.doFinal(msg.getBytes("UTF-8")),Base64.NO_WRAP);
        return base64;
    }
    public static Cipher cipher;
        private static FirebaseDatabase mDatabase;

        public static FirebaseDatabase getDatabase() {
            if (mDatabase == null) {
                mDatabase = FirebaseDatabase.getInstance();
                mDatabase.setPersistenceEnabled(true);
            }
            return mDatabase;
        }
}
