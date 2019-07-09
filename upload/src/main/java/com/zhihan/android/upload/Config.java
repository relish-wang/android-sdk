package com.zhihan.android.upload;

import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.Recorder;
import com.qiniu.android.storage.persistent.FileRecorder;
import com.qiniu.android.utils.UrlSafeBase64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 七牛云上传配置(配置断点续传工呢过)
 *
 * @author wangxin
 * @since 20190708
 */
class Config {

    static Configuration getInstance() {
        //断点上传
        String dirPath = "/storage/emulated/0/Download";
        Recorder recorder = null;
        try {
            File f = File.createTempFile("qiniu_xxxx", ".tmp");
            Utils.Log.d("qiniu", f.getAbsolutePath());
            dirPath = f.getParent();
            //设置记录断点的文件的路径
            recorder = new FileRecorder(dirPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String dirPath1 = dirPath;
        //默认使用 key 的url_safe_base64编码字符串作为断点记录文件的文件名。
        //避免记录文件冲突（特别是key指定为null时），也可自定义文件名(下方为默认实现)：

        KeyGenerator keyGen = (key, file) -> {
            // 不必使用url_safe_base64转换，uploadManager内部会处理
            // 该返回值可替换为基于key、文件内容、上下文的其它信息生成的文件名
            String path = key + "_._" + new StringBuffer(file.getAbsolutePath()).reverse();
            Utils.Log.d("qiniu", path);
            File f = new File(dirPath1, UrlSafeBase64.encodeToString(path));
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(f));
                String tempString;
                int line = 1;
                try {
                    while ((tempString = reader.readLine()) != null) {
                        Utils.Log.d("qiniu", "line " + line + ": " + tempString);
                        line++;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return path;
        };
        // 实例化一个上传的实例
        return new Configuration.Builder()
                // recorder 分片上传时，已上传片记录器
                // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
                .recorder(recorder, keyGen)
                .build();
    }
}
