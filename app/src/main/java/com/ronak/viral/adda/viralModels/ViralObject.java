package com.ronak.viral.adda.viralModels;

import java.util.List;

public class ViralObject {
    public String title;
    public String drawable;
    public String submenu;
    public boolean iap;
    public List<Tab> tabs;

    public ViralObject() {
    }

    public ViralObject(String title, String drawable, String submenu, boolean iap, List<Tab> tabs) {
        this.title = title;
        this.drawable = drawable;
        this.submenu = submenu;
        this.iap = iap;
        this.tabs = tabs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDrawable() {
        return drawable;
    }

    public void setDrawable(String drawable) {
        this.drawable = drawable;
    }

    public String getSubmenu() {
        return submenu;
    }

    public void setSubmenu(String submenu) {
        this.submenu = submenu;
    }

    public boolean isIap() {
        return iap;
    }

    public void setIap(boolean iap) {
        this.iap = iap;
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public void setTabs(List<Tab> tabs) {
        this.tabs = tabs;
    }
}