package com.esakki.smsreceiver;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Telephony;
import android.util.Log;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class SMSReceiverModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private BroadcastReceiver smsReceiver;
    private boolean isReceiverRegistered = false;

    public SMSReceiverModule(ReactApplicationContext reactContext) {
        super(reactContext);
        smsReceiver = new SMSBroadcastReceiver(reactContext);
        getReactApplicationContext().addLifecycleEventListener(this);
        registerReceiverIfNecessary(smsReceiver);
    }
    private void registerReceiverIfNecessary(BroadcastReceiver receiver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && getCurrentActivity() != null) {
            getCurrentActivity().registerReceiver(
                    receiver,
                    new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            );
            isReceiverRegistered = true;
            return;
        }

        if (getCurrentActivity() != null) {
            getCurrentActivity().registerReceiver(
                    receiver,
                    new IntentFilter("android.provider.Telephony.SMS_RECEIVED")
            );
            isReceiverRegistered = true;
        }
    }
    private void unregisterReceiver(BroadcastReceiver receiver) {
        if (isReceiverRegistered && getCurrentActivity() != null) {
            getCurrentActivity().unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }
    @Override
    public void onHostResume() {
        Log.d(SMSReceiverPackage.TAG, "HOST RESUMED");
        registerReceiverIfNecessary(smsReceiver);
    }

    @Override
    public void onHostPause() {
        Log.d(SMSReceiverPackage.TAG, "HOST PAUSED");
        unregisterReceiver(smsReceiver);
    }

    @Override
    public void onHostDestroy() {
        Log.d(SMSReceiverPackage.TAG, "HOST DESTROYED");
        unregisterReceiver(smsReceiver);
    }

    @Override
    public String getName() {
        return "SMSReceiverPackage";
    }
}
