package com.cabe.idea.plugin.utils;

import com.intellij.notification.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * logger
 * Created by moxun on 15/11/27.
 */
public class Logger {
    private static String NAME;
    private static int LEVEL = 0;

    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    public static final int DEBUG = 3;
    public static final int INFO = 2;
    public static final int WARN = 1;
    public static final int ERROR = 0;

    public static void init(String name,int level) {
        NAME = name;
        LEVEL = level;
        NotificationsConfiguration.getNotificationsConfiguration().register(NAME, NotificationDisplayType.NONE);
    }

    public static void debug(String text) {
        if (LEVEL >= DEBUG) {
            println(text);
            Notifications.Bus.notify(new Notification(NAME, NAME + " [DEBUG]", text, NotificationType.INFORMATION));
        }
    }

    public static void info(String text) {
        if (LEVEL > INFO) {
            println(text);
            Notifications.Bus.notify(new Notification(NAME, NAME + " [INFO]", text, NotificationType.INFORMATION));
        }
    }

    public static void warn(String text) {
        if (LEVEL > WARN) {
            println(text);
            Notifications.Bus.notify(new Notification(NAME, NAME + " [WARN]", text, NotificationType.WARNING));
        }
    }

    public static void error(String text) {
        if (LEVEL > ERROR) {
            println(text);
            Notifications.Bus.notify(new Notification(NAME, NAME + " [ERROR]", text, NotificationType.ERROR));
        }
    }

    private static void println(String text) {
        String prefix = format.format(new Date()) + ":  ";
        String newStr = prefix + text;

        Pattern p = Pattern.compile("\n");
        Matcher m = p.matcher(newStr);
        newStr = m.replaceAll("\n" + prefix);

        System.out.println(newStr);
    }
}
