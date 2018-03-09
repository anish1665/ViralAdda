package com.ronak.viral.adda.viralModels;

import java.util.List;

public class Tab {
    public String title;
    public String provider;
    public List<String> arguments;

    public Tab(String title, String provider, List<String> arguments) {
        this.title = title;
        this.provider = provider;
        this.arguments = arguments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
}