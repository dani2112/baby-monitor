package de.dk_s.babymonitor;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import de.dk_s.babymonitor.gui.SoundAnimationFragment;
import de.dk_s.babymonitor.gui.eventlist.MonitorEventFragment;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;
import de.dk_s.babymonitor.monitoring.MonitoringService;
import de.dk_s.babymonitor.monitoring.db.DatabaseEventLogger;

public class ChildActivity extends AppCompatActivity implements SoundAnimationFragment.OnFragmentInteractionListener, MonitorEventFragment.OnListFragmentInteractionListener {

    private static final String TAG = "ChildActivity";

    private Intent lastServiceIntent;

    private MonitoringService monitoringService = null;

    private boolean isBound = true;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            monitoringService = ((MonitoringService.MonitoringServiceBinder) service).getService();
            ToggleButton onOffToggleButton = (ToggleButton) findViewById(R.id.onOffToggleButton);
            if (monitoringService.isStarted()) {
                onOffToggleButton.setOnCheckedChangeListener(null);
                onOffToggleButton.setChecked(true);
                onOffToggleButton.setOnCheckedChangeListener(checkedChangeListener);
            } else {
                onOffToggleButton.setChecked(false);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            monitoringService = null;
        }
    };

    private void doBindService() {
        lastServiceIntent = new Intent(this, MonitoringService.class);
        isBound = bindService(lastServiceIntent, serviceConnection, 0);
    }

    private void doUnbindService() {
        if (isBound) {
            // Detach our existing connection.
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                startService(lastServiceIntent);
                doBindService();
            } else {
                stopService(lastServiceIntent);
                doUnbindService();
            }
        }
    };

    public MonitoringService getMonitoringService() {
        return monitoringService;
    }

    public DatabaseEventLogger getDatabaseEventLogger() {
        return new DatabaseEventLogger(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        doBindService();

        ToggleButton onOffToggleButton = (ToggleButton) findViewById(R.id.onOffToggleButton);
        onOffToggleButton.setOnCheckedChangeListener(checkedChangeListener);
    }

    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(BabyVoiceMonitor.AudioEvent item) {

    }
}
