package com.AbyssDigest.personalalert.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alerts")
public class Alert {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String keywords;
    public boolean flashlight;
    public String sound;
}
