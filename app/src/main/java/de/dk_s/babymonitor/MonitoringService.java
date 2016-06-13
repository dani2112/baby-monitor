package de.dk_s.babymonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MonitoringService extends Service {
    public MonitoringService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
