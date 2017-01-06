package com.cabe.idea.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.apache.http.util.TextUtils;
import com.cabe.idea.plugin.runnable.CheckRunnable;
import com.cabe.idea.plugin.utils.CommonUtils;
import com.cabe.idea.plugin.utils.Logger;

/**
 *
 * Created by cabe on 17/1/3.
 */
public class CompileCheckAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic heree
        if (!CommonUtils.isFastClick(1000)) {
            Logger.init(getClass().getSimpleName(), Logger.DEBUG);
            handleEvent(e);
        }
    }

    private void handleEvent(AnActionEvent event) {
        Editor mEditor = event.getData(PlatformDataKeys.EDITOR);
        if (mEditor == null) return;

        SelectionModel model = mEditor.getSelectionModel();
        String text = model.getSelectedText();
        if (TextUtils.isEmpty(text)) {
            text = CommonUtils.getCurrentWords(mEditor);
            if (TextUtils.isEmpty(text)) {
                return;
            }
        }
        Logger.info(text);
        new Thread(new CheckRunnable(text)).start();
    }
}
