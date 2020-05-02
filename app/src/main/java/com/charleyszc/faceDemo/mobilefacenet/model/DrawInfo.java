package com.charleyszc.faceDemo.mobilefacenet.model;

import android.graphics.Rect;

public class DrawInfo {
    private Rect rect;
    private int liveness;
    private String id = null;

    public DrawInfo(Rect rect, int liveness, String name) {
        this.rect = rect;
        this.liveness = liveness;
        this.id = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getLiveness() {
        return liveness;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
    }
}
