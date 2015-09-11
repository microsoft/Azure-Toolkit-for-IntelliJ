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

package com.microsoft.intellij.ui.azureroles;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TitlePanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoft.intellij.ui.NewCertificateDialog;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoft.wacommon.commoncontrols.NewCertificateDialogData;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.roleoperations.CertificateDialogUtilMethods;
import com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.security.cert.X509Certificate;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class CertificateDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField txtName;
    private JTextField txtThumb;

    private Module module;
    private Map<String, WindowsAzureCertificate> mapCert;
    private WindowsAzureRole waRole;
    public String certNameAdded = "";

    public CertificateDialog(Module module, Map<String, WindowsAzureCertificate> mapCert, WindowsAzureRole waRole) {
        super(true);
        this.module = module;
        this.mapCert = mapCert;
        this.waRole = waRole;
        init();
    }

    @Override
    protected void init() {
        createDocumentListener(txtName);
        createDocumentListener(txtThumb);
        myOKAction.setEnabled(false);
        super.init();
    }

    private void createDocumentListener(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                enableDisableOkBtn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                enableDisableOkBtn();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableDisableOkBtn();
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected JComponent createTitlePane() {
        return new TitlePanel(message("certAddTtl"), message("certMsg"));
    }

    @NotNull
    @Override
    protected Action[] createLeftSideActions() {
        final AbstractAction importAction = new AbstractAction(message("importBtn")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                importBtnListner();
            }
        };
        final AbstractAction newAction = new AbstractAction(message("newBtn")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                newBtnListener();
            }
        };
        return new Action[]{importAction, newAction};
    }

    /**
     * Method to remember which certificate got added recently.
     */
    public String getNewlyAddedCert() {
        return certNameAdded;
    }

    @Override
    protected void doOKAction() {
        boolean retVal;
        try {
            String name = txtName.getText().trim();
            String thumb = txtThumb.getText().trim();
            retVal = validateNameAndThumbprint(name, thumb);
            if (retVal) {
                waRole.addCertificate(name, thumb.toUpperCase());
                certNameAdded = name;
            }
        } catch (Exception ex) {
            PluginUtil.displayErrorDialogAndLog(message("rolsErr"), message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), ex);
            retVal = false;
        }
        if (retVal) {
            super.doOKAction();
        }
    }

    private void enableDisableOkBtn() {
        if (txtThumb.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
            myOKAction.setEnabled(false);
        } else {
            myOKAction.setEnabled(true);
        }
    }

    private boolean validateNameAndThumbprint(String name, String thumb) {
        boolean retVal = true;
        try {
            retVal = CertificateDialogUtilMethods.validateNameAndThumbprint(name, thumb, mapCert);
        } catch (AzureCommonsException e) {
            PluginUtil.displayErrorDialog(message("genErrTitle"), e.getMessage());
        }
        return retVal;
    }

    private void importBtnListner() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return file.isDirectory() || (file.getExtension() != null && (file.getExtension().equals("pfx") || file.getExtension().equals("pfx") ||
                        file.getExtension().equals("cer") || file.getExtension().equals(".CER")));
            }

            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return (file.getExtension() != null && (file.getExtension().equals("pfx") || file.getExtension().equals("pfx") ||
                        file.getExtension().equals("cer") || file.getExtension().equals(".CER")));
            }
        };
        fileChooserDescriptor.setTitle("Select Certificate");

        FileChooser.chooseFile(fileChooserDescriptor, null, null, new Consumer<VirtualFile>() {
            @Override
            public void consume(VirtualFile virtualFile) {
                if (virtualFile != null) {
                    String path = virtualFile.getPath();
                    String password = null;
                    boolean proceed = true;
                    if (path.endsWith(".pfx") || path.endsWith(".PFX")) {
                        SimplePfxPwdDlg dlg = new SimplePfxPwdDlg(path);
                        dlg.show();
                        if (dlg.isOK()) {
                            password = dlg.getPwd();
                        } else {
                            proceed = false;
                        }
                    }
                    if (proceed) {
                        X509Certificate cert = CerPfxUtil.getCert(path, password);
                        if (cert != null) {
                            if (txtName.getText().isEmpty()) {
                                populateCertName(CertificateDialogUtilMethods.removeSpaceFromCN(cert.getSubjectDN().getName()));
                            }
                            String thumbprint = "";
                            try {
                                thumbprint = CerPfxUtil.getThumbPrint(cert);
                            } catch (Exception e) {
                                PluginUtil.displayErrorDialog(message("certErrTtl"), message("certImpEr"));
                            }
                            txtThumb.setText(thumbprint);
                        }
                    }
                }
            }
        });
    }

    /**
     * Method checks if certificate name is already
     * used then make it unique by concatenating current date.
     *
     * @param certNameParam
     */
    private void populateCertName(String certNameParam) {
        txtName.setText(CertificateDialogUtilMethods.populateCertName(certNameParam, mapCert));
    }

    private void newBtnListener() {
        NewCertificateDialogData data = new NewCertificateDialogData();
        String jdkPath;
        try {
            jdkPath = waRole.getJDKSourcePath();
        } catch (Exception e) {
            jdkPath = "";
        }
        NewCertificateDialog dialog = new NewCertificateDialog(data, jdkPath, module.getProject());
        dialog.show();
        if (dialog.isOK()) {
            if (txtName.getText().isEmpty()) {
                populateCertName(CertificateDialogUtilMethods.removeSpaceFromCN(data.getCnName()));
            }
            try {
                txtThumb.setText(CerPfxUtil.getThumbPrint(data.getCerFilePath()));
            } catch (Exception e) {
                PluginUtil.displayErrorDialog(message("certErrTtl"), message("certImpEr"));
            }
        }
    }
}
