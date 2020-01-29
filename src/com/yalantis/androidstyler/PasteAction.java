package com.yalantis.androidstyler;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class PasteAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        executeWriteAction(event.getProject(), event.getData(CommonDataKeys.EDITOR));
    }

    public void executeWriteAction(Project project, Editor editor) {
        Document document = editor.getDocument();

        if (editor == null || document == null || !document.isWritable()) {
            return;
        }

        String styleName = getStyleName();

        // get text from clipboard
        String source = getCopiedText();
        if (source == null) {
            StylerUtils.showBalloonPopup(project, Consts.ERROR_CLIPBOARD, NotificationType.ERROR);
            return;
        }

        // get result
        if (styleName == null || styleName.isEmpty()) {
            StylerUtils.showBalloonPopup(project, Consts.ERROR_NAME, NotificationType.ERROR);
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                try {
                    String output = StylerEngine.style(styleName, source);
                    // delete text that is selected now
                    deleteSelectedText(editor, document);
                    CaretModel caretModel = editor.getCaretModel();
                    // insert new duplicated string into the document
                    document.insertString(caretModel.getOffset(), output);
                    // move caret to the end of inserted text
                    caretModel.moveToOffset(caretModel.getOffset() + output.length());
                    // scroll to the end of inserted text
                    editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
                } catch (ParserConfigurationException | TransformerException e) {
                    e.printStackTrace();
                    StylerUtils.showBalloonPopup(project, Consts.XML_ERROR, NotificationType.ERROR);
                } catch (Exception e) {
                    e.printStackTrace();
                    StylerUtils.showBalloonPopup(project, Consts.WRONG_INPUT, NotificationType.ERROR);
                }
            }
        });
    }

    private String getCopiedText() {
        try {
            return (String) CopyPasteManager.getInstance().getContents().getTransferData(DataFlavor.stringFlavor);
        } catch (NullPointerException | IOException | UnsupportedFlavorException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteSelectedText(Editor editor, Document document) {
        SelectionModel selectionModel = editor.getSelectionModel();
        document.deleteString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd());
    }

    private static String getStyleName() {
        return (String) JOptionPane.showInputDialog(
                new JFrame(), Consts.DIALOG_NAME_CONTENT,
                Consts.DIALOG_NAME_TITLE,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null, "");
    }
}
