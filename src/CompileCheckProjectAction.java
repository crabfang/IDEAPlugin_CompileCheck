import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.http.util.TextUtils;
import runnable.CheckRunnable;
import runnable.FileRunnable;
import utils.CommonUtils;
import utils.Logger;

import java.io.File;

/**
 *
 * Created by cabe on 17/1/3.
 */
public class CompileCheckProjectAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic heree
        if (!CommonUtils.isFastClick(1000)) {
            Logger.init(getClass().getSimpleName(), Logger.DEBUG);
            handleEvent(e);
        }
    }

    private void handleEvent(AnActionEvent event) {
        String projectPath = "";
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if(virtualFile != null) {
            projectPath = virtualFile.getPath();
        }

        String[] group = projectPath.split("/");
        String projectName = group[group.length - 1];
        Logger.info(projectName + "-->" + projectPath);
        new Thread(new FileRunnable(projectPath)).start();
    }
}
