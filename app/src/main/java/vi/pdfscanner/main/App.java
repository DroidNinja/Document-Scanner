
package vi.pdfscanner.main;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ManagerInitializer.i.init(getApplicationContext());
        FlowManager.init(this);
    }

}
