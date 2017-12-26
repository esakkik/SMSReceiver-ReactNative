package com.esakki.smsreceiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    public static final String SMS_BUNDLE = "pdus";
    private static final String EVENT = "com.esakki.smsreceiver:smsReceived";
    private ReactApplicationContext appContext;
    public SMSBroadcastReceiver() {
        super();
    }

    public SMSBroadcastReceiver(ReactApplicationContext applicationContext) {
        appContext = applicationContext;
    }

    private void processMessage(SmsMessage msg) {
        if (appContext == null) {
            return;
        }
        if (!appContext.hasActiveCatalystInstance()) {
            return;
        }
        Log.d(SMSReceiverPackage.TAG,  String.format("%s: %s", msg.getOriginatingAddress(), msg.getMessageBody()));
        WritableNativeMap receivedMsg = new WritableNativeMap();
        receivedMsg.putString("address", msg.getOriginatingAddress());
        receivedMsg.putString("body", msg.getMessageBody());
        appContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT, receivedMsg);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                processMessage(message);
            }

            return;
        }
        try {
            Bundle smsBundle = intent.getExtras();
            if (smsBundle == null || ! smsBundle.containsKey("pdus")) {
                return;
            }
            final Object[] smsMsg = (Object[]) smsBundle.get(SMSBroadcastReceiver.SMS_BUNDLE);
            for (Object msg: smsMsg) {
                processMessage(SmsMessage.createFromPdu((byte[]) msg));
            }
        } catch (Exception e) {
            Log.e(SMSReceiverPackage.TAG, e.getMessage());
        }
    }
}
