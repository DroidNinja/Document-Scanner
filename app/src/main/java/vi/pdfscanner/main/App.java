
package vi.pdfscanner.main;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.raizlabs.android.dbflow.config.FlowManager;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        ManagerInitializer.i.init(getApplicationContext());
        FlowManager.init(this);
    }

}
