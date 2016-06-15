package de.dk_s.babymonitor;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import de.dk_s.babymonitor.monitoring.MonitoringService;

public class ChildActivity extends AppCompatActivity implements SoundAnimationFragment.OnFragmentInteractionListener {

    private static final String TAG = "ChildActivity";

    private Intent lastServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToggleButton onOffToggleButton = (ToggleButton)findViewById(R.id.onOffToggleButton);
        onOffToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    lastServiceIntent = new Intent(ChildActivity.this, MonitoringService.class);
                    startService(lastServiceIntent);
                } else {
                    stopService(lastServiceIntent);
                }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
