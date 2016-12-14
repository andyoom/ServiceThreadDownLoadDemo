package test.down.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.greenrobot.event.EventBus;
import test.down.entry.FileInfo;

/**
 * Created by Administrator on 2016/12/14.
 */

public class FileLength {
    /**
     * 获取文件长度
     * @param info
     */
    public static void getFileLength(final FileInfo info) {
        new Thread(){
            @Override
            public void run() {
                StringBuffer result=new StringBuffer();
                HttpURLConnection conn=null;
                try {
                    URL url = new URL(info.pathUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3*1000);
                    conn.setReadTimeout(3*1000);
                    if(conn.getResponseCode()==200){
                        int len = conn.getContentLength();
                        info.length = len;
                    }else {
                        info.length = 0;
                    }
                    EventBus.getDefault().post(info);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    conn.disconnect();
                }
            }
        }.start();
    }
}
