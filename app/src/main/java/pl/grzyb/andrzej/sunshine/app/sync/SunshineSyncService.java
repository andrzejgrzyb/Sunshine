package pl.grzyb.andrzej.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SunshineSyncService extends Service {

    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    // Storage for an instance of the sync adapter
    private static SunshineSyncAdapter sSunshineSyncAdapter = null;
    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}