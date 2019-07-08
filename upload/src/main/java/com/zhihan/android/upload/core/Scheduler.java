package com.zhihan.android.upload.core;


import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.zhihan.android.upload.FileModel;

/**
 * 用于线程资源调度:
 * 1 当一个任务<strong>暂停</strong>|<strong>完成</strong>|<strong>失败</strong>时, 将启动一个新的任务
 *
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190707
 */
public interface Scheduler {
    class A{

        static void f(Context context){
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.schedule(new JobInfo.Builder(0x123,new ComponentName("pkg","cla")).build());
        }

    }

    void enqueue(FileModel fileModel);
}
