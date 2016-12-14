package test.down.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import test.down.download.DownLoadTask;
import test.down.entry.FileInfo;

public class DownLoadService extends Service{
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String STARTALL = "start_all";
    public static final String STOPALL = "stop_all";

    public static final String FILE_PATH_URL = "file_path_url";
    private String basePath;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        basePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    public static void start(Context context,String pathUrl) {
        Intent intent =new Intent(context, DownLoadService.class);
        intent.setAction(DownLoadService.START);
        intent.putExtra(DownLoadService.FILE_PATH_URL,pathUrl);
        context.startService(intent);
    }
    public static  void stop(Context context,String pathUrl) {
        Intent intent =new Intent(context, DownLoadService.class);
        intent.setAction(DownLoadService.STOP);
        intent.putExtra(DownLoadService.FILE_PATH_URL,pathUrl);
        context.startService(intent);
    }

    public static  void stopAll(Context context) {
        Intent intent =new Intent(context, DownLoadService.class);
        intent.setAction(DownLoadService.STOPALL);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null)
            return super.onStartCommand(intent, flags, startId);
        checkAction(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkAction(Intent intent) {
        String action = intent.getAction();
        if(START.equals(action)){
            String pathUrl = intent.getStringExtra(FILE_PATH_URL);
            if(pathUrl!=null)
                startAction(pathUrl);
            else
                Toast.makeText(this," 下载失败", Toast.LENGTH_SHORT).show();
        }else if(STOP.equals(action)){
            String pathUrl = intent.getStringExtra(FILE_PATH_URL);
            DownLoadTask.getInstance().stop(pathUrl);
        }else if(STOPALL.equals(action)){
            DownLoadTask.getInstance().stopAll();
        }
    }

    private void startAction(String pathUrl) {
        FileInfo info = FileInfo.isExist(pathUrl);
        if(info==null){
            info=new FileInfo();
            info.pathUrl=pathUrl;
            info.name = pathUrl.substring(pathUrl.lastIndexOf("/")+1);
            info.saveFilePath= basePath+"/"+info.name;
            //添加到数据库
            info.save();
        }
        //获得文件的长度
        if(info.length==0) {
            FileLength.getFileLength(info);
        }else{
            //判断是否下载完成
            if(info.isFinish){
                if(new File(info.saveFilePath).exists()){
                    Toast.makeText(this, info.name+" 下载完成", Toast.LENGTH_SHORT).show();
                } else {
                    info.delete();
                    startAction(pathUrl);
                }
            }else {
                DownLoadTask.getInstance().addTask(this,info);
            }
        }
    }

    /**
     * 获取文件长度后通过eventBus切换到此方法
     * @param info
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void getFileLenCallBack(FileInfo info){
        if(info.length!=0){
            //长度已获取更新至数据库
            info.save();
            DownLoadTask.getInstance().addTask(this,info);
        }else{
            Toast.makeText(this, info.name+"--->下载失败！！！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DownLoadTask.getInstance().onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
