package com.cabe.idea.plugin.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
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

    private JPanel rootPanel;
    private JTextField customPath;
    private JTextField localCache;
    private JTextField compileLevel;

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

        String level = compileLevel.getText();
        setCompileLevel(level);
    }

    @Override
    public void reset() {
        String path = getCustomPath();
        customPath.setText(path);

        String cache = getLocalCache();
        localCache.setText(cache);

        String level = getCompileLevel() + "";
        compileLevel.setText(level);
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

    @Override
    public void disposeUIResources() {

    }
}
