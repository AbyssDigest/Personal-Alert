package com.AbyssDigest.personalalert.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Alert.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlertDao alertDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "alert_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
