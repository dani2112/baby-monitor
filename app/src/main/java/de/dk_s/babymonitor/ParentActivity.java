package de.dk_s.babymonitor;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.dk_s.babymonitor.client.ConnectionService;
import de.dk_s.babymonitor.gui.SoundAnimationFragment;
import de.dk_s.babymonitor.gui.eventlist.MonitorEventFragment;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class ParentActivity extends AppCompatActivity implements SoundAnimationFragment.OnFragmentInteractionListener, MonitorEventFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(BabyVoiceMonitor.AudioEvent item) {

    }

    public ConnectionService getConnectionService() {
        return null;
    }
}
