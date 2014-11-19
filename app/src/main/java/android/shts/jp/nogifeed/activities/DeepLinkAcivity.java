package android.shts.jp.nogifeed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.shts.jp.nogifeed.MyActivity;
import android.support.v7.app.ActionBarActivity;

/**
 * Activity for deep link from browser application.
 */
public class DeepLinkAcivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, MyActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        finish();
    }
}