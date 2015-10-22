/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.intellij.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.ui.util.UIUtils;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class ImportSubscriptionDialog extends DialogWrapper {
    private JPanel contentPane;
    private JButton downloadButton;
    private TextFieldWithBrowseButton publishSettingsPath;

    public ImportSubscriptionDialog() {
        super(true);
        init();
    }

    @Override
    protected void init() {
        setTitle(AzureBundle.message("impSubDlgTtl"));
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return !file.isDirectory();
            }
        };
        publishSettingsPath.addActionListener(UIUtils.createFileChooserListener(publishSettingsPath, null, fileChooserDescriptor));
        downloadButton.addActionListener(createDownloadButtonListener());
        super.init();
    }

    private ActionListener createDownloadButtonListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create(PreferenceSetUtil.
                            getSelectedPublishSettingsURL(PreferenceSetUtil.getSelectedPreferenceSetName(AzurePlugin.prefFilePath), AzurePlugin.prefFilePath)));
                } catch (Exception e1) {
                    PluginUtil.displayErrorDialogAndLog(message("error"), message("error"), e1);
                }
            }
        };
    }

    public String getPublishSettingsPath() {
        return publishSettingsPath.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}