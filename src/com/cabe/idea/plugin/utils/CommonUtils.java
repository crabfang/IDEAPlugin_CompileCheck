package com.cabe.idea.plugin.utils;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.TextRange;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 *
 * Created by cabe on 17/1/3.
 */
public class CommonUtils {
    private static long latestClickTime;

    public static String getCurrentWords(Editor editor) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int caretOffset = caretModel.getOffset();
        int lineNum = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(lineNum);
        int lineEndOffset = document.getLineEndOffset(lineNum);
        String lineContent = document.getText(new TextRange(lineStartOffset, lineEndOffset));
        char[] chars = lineContent.toCharArray();
        int start = 0, end = 0, cursor = caretOffset - lineStartOffset;

        if (!Character.isLetter(chars[cursor])) {
            return null;
        }

        for (int ptr = cursor; ptr >= 0; ptr--) {
            if (!Character.isLetter(chars[ptr])) {
                start = ptr + 1;
                break;
            }
        }

        int lastLetter = 0;
        for (int ptr = cursor; ptr < lineEndOffset - lineStartOffset; ptr++) {
            lastLetter = ptr;
            if (!Character.isLetter(chars[ptr])) {
                end = ptr;
                break;
            }
        }
        if (end == 0) {
            end = lastLetter + 1;
        }

        String ret = new String(chars, start, end-start);
        return ret;
    }

    public static String addBlanks(String str) {
        String temp = str.replaceAll("_", " ");
        if (temp.equals(temp.toUpperCase())) {
            return temp;
        }
        String result = temp.replaceAll("([A-Z]+)", " $0");
        return result;
    }

    public static String strip(String str) {
        return str.replaceAll("/\\*+", "")
                .replaceAll("\\*+/", "")
                .replaceAll("\\*", "")
                .replaceAll("//+", "")
                .replaceAll("\r\n", " ")
                .replaceAll("\\s+", " ");
    }

    public static boolean isFastClick(long timeMillis) {
        long time = System.currentTimeMillis();
        long timeD = time - latestClickTime;
        if (0 < timeD && timeD < timeMillis) {
            return true;
        }
        latestClickTime = time;
        return false;
    }

    public static Sdk findAndroidSDK() {
        Sdk[] allJDKs = ProjectJdkTable.getInstance().getAllJdks();
        for (Sdk sdk : allJDKs) {
            if (sdk.getSdkType().getName().toLowerCase().contains("android")) {
                return sdk;
            }
        }

        return null; // no Android SDK found
    }

    public static void saveFile(String filePath, String content) throws Exception {
        File file = new File(filePath);
        if(file.exists() || file.createNewFile()) {
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.newLine();
            bw.write(content);
            bw.close();
            fw.close();
        }
    }

    public static String createLevelPrefix(int level) {
        String prefix = "";
        for(int i=0;i<level;i++) {
            prefix += "         ";
        }
        prefix += "---->";
        return prefix;
    }
}
