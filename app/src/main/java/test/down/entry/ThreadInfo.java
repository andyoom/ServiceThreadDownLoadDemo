package test.down.entry;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by Administrator on 2016/12/14.
 */

public class ThreadInfo extends DataSupport{
    public int id;
    public String pathUrl;
    public String name;
    public String saveFilePath;
    public int length;
    public int progress;
    public int startPos;
    public int endPos;
    public int currentPos;
    public boolean isLoading;


    public ThreadInfo(int id,FileInfo info) {
        this.id = id;
        this.pathUrl = info.pathUrl;
        this.name = info.name;
        this.saveFilePath = info.saveFilePath;
        this.length = info.length;
        this.progress = info.progress;
    }

    public static List<ThreadInfo> selectByUrl(String pathUrl){
        return DataSupport.where("pathUrl=?",pathUrl)
                .find(ThreadInfo.class);
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "id=" + id +
                ", isLoading=" + isLoading +
                ", pathUrl='" + pathUrl + '\'' +
                ", name='" + name + '\'' +
                ", saveFilePath='" + saveFilePath + '\'' +
                ", length=" + length +
                ", progress=" + progress +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", currentPos=" + currentPos +
                '}';
    }
}
