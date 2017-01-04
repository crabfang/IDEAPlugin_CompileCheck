import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.apache.http.util.TextUtils;

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
        if (null == mEditor) {
            return;
        }

        SelectionModel model = mEditor.getSelectionModel();
        String selectedText = model.getSelectedText();
        if (TextUtils.isEmpty(selectedText)) {
            selectedText = CommonUtils.getCurrentWords(mEditor);
            if (TextUtils.isEmpty(selectedText)) {
                return;
            }
        }
        Logger.info(selectedText);

        new Thread(new CheckRunnable(selectedText)).start();
    }
}
