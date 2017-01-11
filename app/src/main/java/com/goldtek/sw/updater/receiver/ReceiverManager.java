package com.goldtek.sw.updater.receiver;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ReceiverManager {
    private List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
    private Context context;

    public ReceiverManager(Context context){
        this.context = context;
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter){
        receivers.add(receiver);
        Intent intent = context.registerReceiver(receiver, intentFilter);
        Log.i(getClass().getSimpleName(), "registered receiver: "+receiver+"  with filter: "+intentFilter);
        Log.i(getClass().getSimpleName(), "receiver Intent: "+intent);
        return intent;
    }

    public boolean isReceiverRegistered(BroadcastReceiver receiver){
        boolean registered = receivers.contains(receiver);
        Log.i(getClass().getSimpleName(), "is receiver "+receiver+" registered? "+registered);
        return registered;
    }

    public void unregisterReceiver(BroadcastReceiver receiver){
        if (isReceiverRegistered(receiver)){
            receivers.remove(receiver);
            context.unregisterReceiver(receiver);
            Log.i(getClass().getSimpleName(), "unregistered receiver: "+receiver);
        }
    }
}
