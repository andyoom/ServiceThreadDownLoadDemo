package test.down.download;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import de.greenrobot.event.EventBus;
import test.down.entry.ThreadInfo;

/**
 * 下载线程
 * Created by Administrator on 2016/12/14.
 */

public class DownloadThread implements Runnable{

    private ThreadInfo info;

    public DownloadThread(ThreadInfo info) {
        this.info = info;
    }

    public ThreadInfo getInfo() {
        return info;
    }

    @Override
    public void run() {
        download();
    }

    private void download() {
        info.isLoading = true;

        String saveFile = info.saveFilePath;
        String urlStr = info.pathUrl;
        int start = info.startPos;
        int end = info.endPos;
        int currentPos = info.currentPos;

        HttpURLConnection conn=null;
        InputStream is = null;
        RandomAccessFile raf = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30*1000);
            conn.setReadTimeout(30*1000);
            //设置取的范围
            conn.setRequestProperty("Range", "bytes=" + (start+currentPos) + "-"+ end);

            if(conn.getResponseCode()== HttpURLConnection.HTTP_PARTIAL) {
                is=conn.getInputStream();
                raf =new RandomAccessFile(saveFile, "rwd");
                raf.seek(start+currentPos);
                byte [] buffer = new byte[1024];
                int len = -1;
                long time = System.currentTimeMillis();

                while((len=is.read(buffer))!=-1&& info.isLoading){
                    raf.write(buffer,0,len);
                    currentPos+=len;

                    Log.e("down",info.name+"---"+info.id+"---"+currentPos);

                    if(System.currentTimeMillis()-time>2000){
                        //想办法更新进度
                        info.currentPos = currentPos;
                        info.save();
                        EventBus.getDefault().post(info.pathUrl);
                        time= System.currentTimeMillis();
                    }
                }

                info.currentPos = currentPos;
                if(currentPos!=info.endPos+1){
                    info.isLoading=false;
                }
                EventBus.getDefault().post(info.pathUrl);
                info.save();
            }else {
                EventBus.getDefault().post(info.pathUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(is!=null)
                    is.close();
                if(raf!=null)
                    raf.close();
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
