package test.down.download;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import test.down.entry.FileInfo;
import test.down.entry.ThreadInfo;
import test.down.entry.UpdateInfo;

/**
 * 任务管理器
 * Created by Administrator on 2016/12/14.
 */

public class DownLoadTask {

    private static DownLoadTask sTask;

    private ThreadPoolExecutor mPoolExecutor;
    private final int fileThreadSize = 3;
    private LinkedBlockingDeque<Runnable> mBlockingDeque;
    private int queueSize;
    private Map<String,List<ThreadInfo>> mMap;

    public static DownLoadTask getInstance(){
        return  getInstance(2);
    }

    public static DownLoadTask getInstance(int maxFileSize){
        if(sTask==null)
            sTask=new DownLoadTask(maxFileSize);
        return sTask;
    }

    public DownLoadTask(int maxFileSize) {
        if(maxFileSize <= 0)
            maxFileSize = 1;
        int size = maxFileSize * fileThreadSize;
        queueSize = fileThreadSize*5;
        mBlockingDeque=new LinkedBlockingDeque<>(queueSize);
        mPoolExecutor=new ThreadPoolExecutor(size,size,0L, TimeUnit.MILLISECONDS
            ,mBlockingDeque);
        mMap=new HashMap<>();
        EventBus.getDefault().register(this);
    }

    /**
     * 添加任务
     * @param context
     * @param info
     */
    public void addTask(Context context,FileInfo info){
        if(mBlockingDeque.size()==queueSize){
            Toast.makeText(context, "下载队列已满,"+info.name+"无法下载",
                    Toast.LENGTH_SHORT).show();
            return;
        }else {
            List<ThreadInfo> list = mMap.get(info.pathUrl);
            //不等于表示正在下载
            if(list!=null){
                Toast.makeText(context, info.name+" 正在下载中", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        orderThread(info);
    }

    /**
     * 停止文件下载
     * @param pathUrl
     */
    public void stop(String pathUrl){
        for(Runnable runnable:mBlockingDeque){
            DownloadThread downloadThread =(DownloadThread)runnable;
            if(pathUrl.equals(downloadThread.getInfo().pathUrl))
                mBlockingDeque.remove(runnable);
        }

        List<ThreadInfo> list = mMap.remove(pathUrl);
        if(list==null)
            return;
        for(ThreadInfo info:list) {
            info.isLoading = false;
        }
    }

    /**
     * 停止所有文件文件下载
     */
    public void stopAll(){
        mBlockingDeque.clear();

        List<String> list=new ArrayList<>();
        for(String pathUrl:mMap.keySet())
            list.add(pathUrl);

        for(String pathUrl : list)
            stop(pathUrl);
    }

    @Subscribe(threadMode = ThreadMode.Async)
    public void updateThreadInfo(String pathUrl){
        int finish =0;
        ThreadInfo info = null;
        List<ThreadInfo> list = ThreadInfo.selectByUrl(pathUrl);
        if(list!=null&&list.size()>0){
            //可以通过数据库计算
            for (ThreadInfo threadInfo:list){
                if(threadInfo!=null){
                    finish+=threadInfo.currentPos;
                    if(info==null)
                        info = threadInfo;
                }
            }

            int progress=finish*100/info.length;

            if(progress==100)
                mMap.remove(info.pathUrl);

            UpdateInfo updateInfo =new UpdateInfo();
            updateInfo.pathUrl=pathUrl;
            updateInfo.name = info.name;
            updateInfo.progress=progress;
            EventBus.getDefault().post(updateInfo);
        }
    }

    /**
     * 为任务分配线程
     * @param info
     */
    private void orderThread(FileInfo info) {

        List<ThreadInfo> list = ThreadInfo.selectByUrl(info.pathUrl);

        //计算分配方案
        if(list==null || list.size()==0){
            int len = info.length;
            int child = len/fileThreadSize;
            /**
             *
             *  100 分 3份
             *   0 -32   33-65 66-99
             */
            for (int i=0;i<fileThreadSize;i++){
                //构造线程对象
                ThreadInfo threadInfo =new ThreadInfo(i,info);
                threadInfo.startPos = i*child;
                threadInfo.endPos=(i+1)*child-1;
                if(i==fileThreadSize-1)
                    threadInfo.endPos=info.length-1;
                //保存到数据库
                threadInfo.save();
            }
        }
        list = ThreadInfo.selectByUrl(info.pathUrl);
        mMap.put(info.pathUrl,list);
        //开启线程
        for (ThreadInfo f : list){
            //开线程下载
            DownloadThread downloadThread = new DownloadThread(f);
            //放入线程池
            mPoolExecutor.execute(downloadThread);
        }
    }

    public void onDestroy(){
        EventBus.getDefault().unregister(this);
    }
}
