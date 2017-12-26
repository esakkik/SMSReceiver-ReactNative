package com.esakki.smsreceiver;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

public class SMSReader {
    private ReactApplicationContext appContext;
    public SMSReader(ReactApplicationContext applicationContext) {
        appContext = applicationContext;
    }
    private void readSMS(Promise promise) {
        ContentResolver smsResolver = appContext.getContentResolver();
        Cursor smsCursor = smsResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int bodyIndex = smsCursor.getColumnIndex("body");
        int addressIndex = smsCursor.getColumnIndex("address");
        if (smsCursor == null) {
            return;
        }
        try {
            WritableNativeArray msgArray = new WritableNativeArray();
            if (smsCursor.moveToFirst()) {
                WritableNativeMap currentMsg = new WritableNativeMap();
                currentMsg.putString("address", smsCursor.getString(addressIndex));
                currentMsg.putString("body", smsCursor.getString(bodyIndex));
                Log.d(SMSReceiverPackage.TAG, String.format("%s: %s", smsCursor.getString(addressIndex), smsCursor.getString(bodyIndex)));
                msgArray.pushMap(currentMsg);
            }
            promise.resolve(msgArray);
        } catch (Exception e) {
            Log.e(SMSReceiverPackage.TAG, e.getMessage());
            promise.reject(e);
        }
        finally {
            smsCursor.close();
        }
    }
}
