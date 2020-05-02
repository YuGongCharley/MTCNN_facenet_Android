package com.charleyszc.faceDemo.mobilefacenet;

import android.graphics.Rect;

public class DrawInfo {
    private Rect rect;
    private int liveness;
    private String name = null;

    public DrawInfo(Rect rect, int liveness, String name) {
        this.rect = rect;
        this.liveness = liveness;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
