package com.cabe.idea.plugin.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * SettingForm
 * Created by cabe on 17/1/6.
 */
public class SettingForm implements Configurable {
    private final static String KEY_CUSTOM_PATH = "keyCustomHost";
    private final static String KEY_LOCAL_CACHE = "keyLocalCache";
    private final static String KEY_COMPILE_LEVEL = "keyCompileLevel";
    private final static String KEY_LOCAL_LEVEL = "keyLocalLevel";
    private final static String KEY_LOCAL_FILTER = "keyLocalFilter";

    private JPanel rootPanel;
    private JTextField customPath;
    private JTextField localCache;
    private JTextField compileLevel;
    private JTextField localLevel;
    private JTextField localFilter;

    @Nls
    @Override
    public String getDisplayName() {
        return "Compile Check";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Setting Compile Repository";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return rootPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        String path = customPath.getText();
        setCustomPath(path);

        String cache = localCache.getText();
        setLocalCache(cache);

        String compileLevelStr = compileLevel.getText();
        setCompileLevel(compileLevelStr);

        String localLevelStr = localLevel.getText();
        setLocalLevel(localLevelStr);

        String localFilterStr = localFilter.getText();
        setLocalFilter(localFilterStr);
    }

    @Override
    public void reset() {
        String path = getCustomPath();
        customPath.setText(path);

        String cache = getLocalCache();
        localCache.setText(cache);

        String compileLevelStr = getCompileLevel() + "";
        compileLevel.setText(compileLevelStr);

        String localLevelStr = getLocalLevel() + "";
        localLevel.setText(localLevelStr);

        String localFilterStr = getLocalFilter();
        localFilter.setText(localFilterStr);
    }

    private static void setCustomPath(String customHost) {
        PropertiesComponent.getInstance().setValue(KEY_CUSTOM_PATH, customHost);
    }

    public static String getCustomPath() {
        return PropertiesComponent.getInstance().getValue(KEY_CUSTOM_PATH, "");
    }

    private static void setLocalCache(String localCache) {
        PropertiesComponent.getInstance().setValue(KEY_LOCAL_CACHE, localCache);
    }

    public static String getLocalCache() {
        return PropertiesComponent.getInstance().getValue(KEY_LOCAL_CACHE, "");
    }

    private static void setCompileLevel(String compileLevel) {
        PropertiesComponent.getInstance().setValue(KEY_COMPILE_LEVEL, compileLevel);
    }

    public static int getCompileLevel() {
        int defaultLevel = 2;
        int level = defaultLevel;
        try {
            String strLevel = PropertiesComponent.getInstance().getValue(KEY_COMPILE_LEVEL, "" + defaultLevel);
            level = Integer.parseInt(strLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(level < defaultLevel) {
            level = defaultLevel;
        }
        return level;
    }

    private static void setLocalLevel(String localLevel) {
        PropertiesComponent.getInstance().setValue(KEY_LOCAL_LEVEL, localLevel);
    }

    public static int getLocalLevel() {
        int defaultLevel = 4;
        int level = defaultLevel;
        try {
            String strLevel = PropertiesComponent.getInstance().getValue(KEY_LOCAL_LEVEL, "" + defaultLevel);
            level = Integer.parseInt(strLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(level < defaultLevel) {
            level = defaultLevel;
        }
        return level;
    }

    private static void setLocalFilter(String localFilter) {
        if(!TextUtils.isEmpty(localFilter)) {
            localFilter = localFilter.replace(" ", "");
        }
        PropertiesComponent.getInstance().setValue(KEY_LOCAL_FILTER, localFilter);
    }

    public static String getLocalFilter() {
        return PropertiesComponent.getInstance().getValue(KEY_LOCAL_FILTER, "");
    }

    @Override
    public void disposeUIResources() {

    }
}
