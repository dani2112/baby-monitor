package de.dk_s.babymonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import de.dk_s.babymonitor.client.ConnectionService;
import de.dk_s.babymonitor.gui.SoundAnimationFragment;
import de.dk_s.babymonitor.gui.eventlist.MonitorEventFragment;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;
import de.dk_s.babymonitor.monitoring.MonitoringService;

public class ParentActivity extends AppCompatActivity implements SoundAnimationFragment.OnFragmentInteractionListener, MonitorEventFragment.OnListFragmentInteractionListener {

    private static final String TAG = "ParentActivity";

    private Intent lastServiceIntent;

    private ConnectionService connectionService = null;

    private boolean isBound = true;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            connectionService = ((ConnectionService.ConnectionServiceBinder) service).getService();
            ToggleButton connectionToggleButton = (ToggleButton) findViewById(R.id.connectionToggleButton);
            if (connectionService.isStarted()) {
                connectionToggleButton.setOnCheckedChangeListener(null);
                connectionToggleButton.setChecked(true);
                connectionToggleButton.setOnCheckedChangeListener(checkedChangeListener);
            } else {
                connectionToggleButton.setChecked(false);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            connectionService = null;
        }
    };

    private void doBindService() {
        lastServiceIntent = new Intent(this, ConnectionService.class);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        doBindService();

        ToggleButton connectionToggleButton = (ToggleButton) findViewById(R.id.connectionToggleButton);
        connectionToggleButton.setOnCheckedChangeListener(checkedChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(BabyVoiceMonitor.AudioEvent item) {

    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }
}
