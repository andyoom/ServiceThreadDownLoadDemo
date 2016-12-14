package test.down;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by Administrator on 2016/12/14.
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
