package test.down;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.util.List;

import test.down.entry.FilePathUrl;
import test.down.service.DownLoadService;

/**
 * Created by Administrator on 2016/12/14.
 */

public class MyAdapter extends BaseAdapter {
    private Context mContext;
    private List<FilePathUrl> list;

    public MyAdapter(Context context, List<FilePathUrl> list) {
        mContext = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder =null;
        if(view==null){
            view = View.inflate(mContext,R.layout.item,null);
            holder=new ViewHolder();
            holder.name=(TextView) view.findViewById(R.id.item_name);
            holder.progressBar=(NumberProgressBar)view.findViewById(R.id.progressBar);
            holder.start=(Button) view.findViewById(R.id.item_start);
            holder.stop=(Button)view.findViewById(R.id.item_stop);
            holder.progressBar.setMax(100);
            holder.progressBar.setProgressTextSize(30);

            view.setTag(holder);
        }else {
            holder=(ViewHolder) view.getTag();
        }
        final FilePathUrl info=list.get(i);
        holder.name.setText(info.name);
        holder.progressBar.setProgress(info.progress);
        holder.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownLoadService.start(mContext,info.pathUrl);
            }
        });
        holder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownLoadService.stop(mContext,info.pathUrl);
            }
        });

        return view;
    }

    class ViewHolder {
        TextView name;
        NumberProgressBar progressBar;
        Button start;
        Button stop;
    }
}