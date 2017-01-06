package com.cabe.idea.plugin.setting;

import com.cabe.idea.plugin.utils.Logger;
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
    private final static String CUSTOM_PATH_KEY = "keyCustomHost";

    private JPanel rootPanel;
    private JTextField customPath;

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
        Logger.init(SettingForm.class.getSimpleName(), Logger.DEBUG);
        return rootPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        String str = customPath.getText();
        Logger.info("apply custom path : " + str);
        setCustomPath(str);
    }

    @Override
    public void reset() {
        String str = getCustomPath();
        customPath.setText(str);
    }

    private static void setCustomPath(String customHost) {
        PropertiesComponent.getInstance().setValue(CUSTOM_PATH_KEY, customHost);
    }

    public static String getCustomPath() {
        return PropertiesComponent.getInstance().getValue(CUSTOM_PATH_KEY, "");
    }

    @Override
    public void disposeUIResources() {

    }
}
