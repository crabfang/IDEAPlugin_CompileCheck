package com.cabe.idea.plugin.model;

/**
 * maven metadata
 * Created by cabe on 17/1/12.
 */
public class MetaData {
    public String lastTime;
    public String release;

    @Override
    public String toString() {
        return release + "_" + lastTime;
    }
}
