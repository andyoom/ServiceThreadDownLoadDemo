package test.down.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import de.greenrobot.event.EventBus;
import test.down.entry.ThreadInfo;

/**
 * 下载工具类
 */

public class DownLoadHttp {

    public static void download(ThreadInfo info) {
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
            conn.setConnectTimeout(3*1000);
            conn.setReadTimeout(3*1000);
            //设置取的范围
            conn.setRequestProperty("Range", "bytes=" + (start+currentPos) + "-"+ end);

            if(conn.getResponseCode()== HttpURLConnection.HTTP_PARTIAL) {
                is=conn.getInputStream();
                raf =new RandomAccessFile(saveFile, "rwd");
                raf.seek(start+currentPos);
                byte [] buffer = new byte[1024];
                int len = -1;
                long time = System.currentTimeMillis();

                while((len=is.read(buffer))!=-1){
                    raf.write(buffer,0,len);
                    currentPos+=len;

                    if(System.currentTimeMillis()-time>1000){
                        //想办法更新进度
                        info.currentPos = currentPos;
                        info.save();
                        EventBus.getDefault().post(info.pathUrl);
                        time= System.currentTimeMillis();
                    }
                }

                info.currentPos = currentPos;
                EventBus.getDefault().post(info.pathUrl);
                //Log.e("download",info.id+" download success");
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
