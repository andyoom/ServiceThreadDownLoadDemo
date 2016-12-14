package test.down.entry;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * 所需下载文件的封装类
 * Created by Administrator on 2016/12/14.
 */

public class FileInfo extends DataSupport{
    public String pathUrl;
    public String name;
    public String saveFilePath;
    public int length;
    public int progress;
    public boolean isFinish = false;

    public FileInfo() {

    }

    public static FileInfo isExist(String pathUrl){
        List<FileInfo> list = DataSupport.where("pathUrl=?",pathUrl)
                .find(FileInfo.class);
        if(list==null||list.size()==0)
            return null;
        return list.get(0);
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "pathUrl='" + pathUrl + '\'' +
                ", name='" + name + '\'' +
                ", saveFilePath='" + saveFilePath + '\'' +
                ", length=" + length +
                ", progress=" + progress +
                ", isFinish=" + isFinish +
                '}';
    }
}
