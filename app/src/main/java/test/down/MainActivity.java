package test.down;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import test.down.entry.FilePathUrl;
import test.down.entry.UpdateInfo;
import test.down.service.DownLoadService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView lv;
    private MyAdapter adapter;
    private List<FilePathUrl> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();

        EventBus.getDefault().register(this);
    }

    private void initData() {
        list=new ArrayList<>();
        String [] urls={
                "http://a5.pc6.com/pc6_soure/2016-1/com.magic.voice_5.apk",
            "http://a5.pc6.com/zxy3/xiaouwenjianguanli.apk",
            "http://a5.pc6.com/pc6_soure/2016-2/mobi.c527a197.o17d6e.apk",
            "http://a5.pc6.com/pc6_soure/2016-3/SUjCi6P6xP7X7Nu2.apk"
        };
        for(String url :urls){
            FilePathUrl info =new FilePathUrl();
            info.pathUrl = url;
            info.name = url.substring(url.lastIndexOf("/")+1);
            list.add(info);
        }
    }

    private void initView() {
        lv=(ListView)findViewById(R.id.lv);
        adapter=new MyAdapter(this,list);
        lv.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start:
                startAll();
                break;
            case R.id.stop:
                stopAll();
                break;
            default:
                startAll();
                break;
        }
    }

    private void startAll() {
        for (FilePathUrl info:list){
            DownLoadService.start(this,info.pathUrl);
        }
    }
    private void stopAll() {
        DownLoadService.stopAll(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void update(UpdateInfo info){
        String url = info.pathUrl;
        for(int i=0;i<list.size();i++){
            FilePathUrl filePathUrl = list.get(i);
            if(url.equals(filePathUrl.pathUrl)
                    &&info.progress>filePathUrl.progress){

                filePathUrl.progress=info.progress;
                adapter.notifyDataSetChanged();
                if(filePathUrl.progress==100)
                    Toast.makeText(this, info.name+" 下载完成", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
