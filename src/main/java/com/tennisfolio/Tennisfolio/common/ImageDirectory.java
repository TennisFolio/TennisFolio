package com.tennisfolio.Tennisfolio.common;

public enum ImageDirectory {
    PLAYER("player", "player/thumbnail/");

    private final String name;
    private final String directory;

    ImageDirectory(String name, String directory){
        this.name = name;
        this.directory = directory;
    }

    public String getName(){
        return this.name;
    }

    public String getDirectory(){
        return this.directory;
    }
}
