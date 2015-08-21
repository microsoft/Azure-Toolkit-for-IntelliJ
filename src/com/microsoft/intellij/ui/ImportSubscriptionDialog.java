/**
 * Copyright 2014 Microsoft Open Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.intellij.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.ui.util.UIUtils;
import com.microsoft.intellij.util.PluginUtil;
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