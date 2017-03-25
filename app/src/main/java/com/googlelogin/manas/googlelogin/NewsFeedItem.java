package com.googlelogin.manas.googlelogin;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by HP on 30-01-2017.
 */

public class NewsFeedItem {

    String id;
    String name;
    String content;
    String imageurl;
    Date date;
    Bitmap bitmap;
    int imageId;

    public NewsFeedItem(String id, String name, String content, String imageurl, Bitmap bitmap) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.imageurl = imageurl;
        this.bitmap = bitmap;
    }

    public NewsFeedItem(String id, String name, String content, Date date,int imageId) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.date = date;
        this.imageId=imageId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getImageurl() {
        return imageurl;
    }

    public int getImageId() {
        return imageId;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Date getDate() { return date;    }
}
