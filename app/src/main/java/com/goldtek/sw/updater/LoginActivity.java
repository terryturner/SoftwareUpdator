package com.goldtek.sw.updater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.goldtek.sw.updater.model.ReportHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Terry on 2016/12/30.
 */

public class LoginActivity extends Activity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        findViewById(R.id.loginSubmit).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        EditText text = (EditText) findViewById(R.id.loginText);

        if (auth(text.getText().toString())) {
            ReportHandler.getInstance().writeMessage(R.string.msg_user_login);

            Intent intent=new Intent();
            intent.setComponent(new ComponentName("com.goldtek.sw.updater", "com.goldtek.sw.updater.SettingsActivity"));
            startActivity(intent);

            finish();
        } else
            Toast.makeText(this, getString(R.string.toast_login_fail), Toast.LENGTH_SHORT).show();
    }

    private boolean auth(String password) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
        String currentDateTime = sdf.format(new Date());
        return password.equalsIgnoreCase("goldtek"+ currentDateTime);
    }
}
