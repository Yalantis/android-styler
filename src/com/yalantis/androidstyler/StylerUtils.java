package com.yalantis.androidstyler;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class StylerUtils {
    public static void showBalloonPopup(Project project, String htmlText, NotificationType notificationType) {
        Notification notification = new Notification(Consts.NOTIFICATION_GROUP_ID, null, notificationType);
        notification.setTitle(Consts.DIALOG_NAME_TITLE);
        notification.setContent(htmlText);
        notification.notify(project);
    }
}
