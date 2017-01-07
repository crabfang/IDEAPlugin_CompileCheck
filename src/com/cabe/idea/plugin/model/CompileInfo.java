package com.cabe.idea.plugin.model;

/**
 * 依赖库信息
 * Created by cabe on 17/1/7.
 */
public class CompileInfo {
    public String group;
    public String artifact;
    public String version;
    public String scope;

    public CompileInfo() {
    }

    public CompileInfo(String group, String artifact, String version) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    @Override
    public int hashCode() {
        return createCompile(group, artifact, version).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CompileInfo) {
            CompileInfo dest = (CompileInfo) obj;

            String dGroup = dest.group;
            String dArtifact = dest.artifact;
            String dVersion = dest.version;
            return createCompile(group, artifact, version).equals(createCompile(dGroup, dArtifact, dVersion));
        }
        return super.equals(obj);
    }

    public String toString() {
        return createCompile(group, artifact, version);
    }

    private static String createCompile(String group, String artifact, String version) {
        return group + ":" + artifact + ":" + version;
    }

    public static CompileInfo parseCompile(String compile) {
        CompileInfo info = null;

        String[] group = compile.split(":");
        if(group.length == 3) {
            info = new CompileInfo();
            info.group = group[0];
            info.artifact = group[1];
            info.version = group[2];
        }

        return info;
    }
}
