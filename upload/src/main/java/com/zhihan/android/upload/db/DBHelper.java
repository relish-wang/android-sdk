package com.zhihan.android.upload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190708
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "uploadsdk.db";
    private static final int VERSION = 1;


    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO 建数据库表
        db.execSQL("CREATE table task(" +
                "id INTEGER primary key," +
                "md5key varchar(45) not null" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
