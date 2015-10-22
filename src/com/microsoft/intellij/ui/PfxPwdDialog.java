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
import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.microsoft.intellij.ui.util.UIUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class PfxPwdDialog extends DialogWrapper {
    private JPanel contentPane;
    private JLabel pfxInputMsg;
    private TextFieldWithBrowseButton txtPfxPath;
    private JPasswordField passwordField;


    private WindowsAzureCertificate cert;

    public PfxPwdDialog(WindowsAzureCertificate cert) {
        super(true);
        setTitle(message("pfxInputTtl"));
        this.cert = cert;
        init();
    }

    @Override
    protected void init() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return file.isDirectory() || (file.getExtension() != null && (file.getExtension().equals("pfx") || file.getExtension().equals("PFX")));
            }

            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return (file.getExtension() != null && (file.getExtension().equals("pfx") || file.getExtension().equals("PFX")));
            }
        };
        txtPfxPath.addActionListener(UIUtils.createFileChooserListener(txtPfxPath, null, fileChooserDescriptor));
        pfxInputMsg.setText(String.format(message("pfxInputMsg"), cert.getName()));
        super.init();
    }

    public String getPfxPath() {
        return txtPfxPath.getText();
    }

    public String getPwd() {
        return new String(passwordField.getPassword());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
