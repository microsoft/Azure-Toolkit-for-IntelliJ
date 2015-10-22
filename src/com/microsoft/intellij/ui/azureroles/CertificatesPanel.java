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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.AnActionButtonUpdater;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoft.intellij.ui.AzureAbstractPanel;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class CertificatesPanel extends BaseConfigurable implements AzureAbstractPanel, SearchableConfigurable, Configurable.NoScroll {
    private JPanel contentPane;
    private JPanel tablePanel;
    private TableView<WindowsAzureCertificate> tblCertificates;

    private Module module;
    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole waRole;
    private Map<String, WindowsAzureCertificate> mapCert;
    public String certSelected = "";

    public CertificatesPanel(Module module, WindowsAzureProjectManager waProjManager, WindowsAzureRole waRole) {
        this.module = module;
        this.waProjManager = waProjManager;
        this.waRole = waRole;
        init();
    }

    private void init() {
        try {
            mapCert = waRole.getCertificates();
            CertificatesTableModel myModel = new CertificatesTableModel(new ArrayList<WindowsAzureCertificate>(mapCert.values()));
            tblCertificates.setModelAndUpdateColumns(myModel);
            tblCertificates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } catch (Exception e) {
            PluginUtil.displayErrorDialogAndLog(message("certErrTtl"), message("certErrMsg"), e);
        }
    }

    @NotNull
    @Override
    public String getId() {
        return getDisplayName();
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Override
    public JComponent getPanel() {
        return contentPane;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return message("cmhLblCert");
    }

    @Override
    public boolean doOKAction() {
        try {
            apply();
            return true;
        } catch (ConfigurationException e) {
            PluginUtil.displayErrorDialogAndLog(e.getTitle(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getSelectedValue() {
        return certSelected;
    }

    @Override
    public ValidationInfo doValidate() {
        return null;
    }

    @Override
    public String getHelpTopic() {
        return "windows_azure_certificates_page";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPane;
    }

    @Override
    public void apply() throws ConfigurationException {
        try {
            waProjManager.save();
        } catch (WindowsAzureInvalidProjectOperationException e) {
            throw new ConfigurationException(message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), message("adRolErrTitle"));
        }
        setModified(false);
    }

    @Override
    public void reset() {
        setModified(false);
    }

    @Override
    public void disposeUIResources() {
    }

    private final ColumnInfo<WindowsAzureCertificate, String> NAME = new ColumnInfo<WindowsAzureCertificate, String>(message("evColName")) {
        public String valueOf(WindowsAzureCertificate object) {
            return object.getName();
        }

        @Override
        public void setValue(WindowsAzureCertificate cert, String modifiedVal) {
            try {
                String certName = modifiedVal.trim();
                if (certName.isEmpty()) {
                    PluginUtil.displayErrorDialog(message("certErrTtl"), message("certInvMsg"));
                } else {
                    if (WAEclipseHelperMethods.isAlphaNumericUnderscore(certName)) {
                        boolean isValidName = true;
                        for (Iterator<String> iterator = mapCert.keySet().iterator(); iterator.hasNext(); ) {
                            String key = iterator.next();
                            if (key.equalsIgnoreCase(certName)) {
                                isValidName = false;
                                break;
                            }
                        }
                        if (isValidName || certName.equalsIgnoreCase(cert.getName())) {
                            cert.setName(certName);
                        } else {
                            PluginUtil.displayErrorDialog(message("certErrTtl"), message("certAddErrMsg"));
                        }
                    } else {
                        PluginUtil.displayErrorDialog(message("certErrTtl"), message("certRegMsg"));
                    }
                }
            } catch (Exception e) {
                PluginUtil.displayErrorDialogAndLog(message("certErrTtl"), message("certErrMsg"), e);
            }
        }
    };

    private final ColumnInfo<WindowsAzureCertificate, String> THUMBPRINT = new ColumnInfo<WindowsAzureCertificate, String>(message("evColName")) {
        public String valueOf(WindowsAzureCertificate object) {
            return object.getFingerPrint();
        }

        @Override
        public void setValue(WindowsAzureCertificate cert, String modifiedVal) {
            try {
                String modifiedTxt = modifiedVal.trim();
                if (modifiedTxt.isEmpty()) {
                    PluginUtil.displayErrorDialog(message("certErrTtl"), message("certInvMsg"));
                } else {
                    boolean isValidName = true;
                    for (Iterator<Map.Entry<String, WindowsAzureCertificate>> iterator = mapCert.entrySet().iterator(); iterator.hasNext(); ) {
                        WindowsAzureCertificate certObj = iterator.next().getValue();
                        if (certObj.getFingerPrint().equalsIgnoreCase(modifiedTxt)) {
                            isValidName = false;
                            break;
                        }
                    }
                    if (isValidName || modifiedTxt.equalsIgnoreCase(cert.getFingerPrint())) {
                        cert.setFingerPrint(modifiedTxt.toUpperCase());
                    } else {
                        PluginUtil.displayErrorDialog(message("certErrTtl"), message("certAddErrMsg"));
                    }
                }
            } catch (Exception e) {
                PluginUtil.displayErrorDialogAndLog(message("certErrTtl"), message("certErrMsg"), e);
            }
        }
    };

    private void createUIComponents() {
        tblCertificates = new TableView<WindowsAzureCertificate>();
        tablePanel = ToolbarDecorator.createDecorator(tblCertificates, null)
                .setAddAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        addCertificate();
                    }
                }).setEditAction(null).setRemoveAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        removeCertificate();
                    }
                }).setEditActionUpdater(new AnActionButtonUpdater() {
                    @Override
                    public boolean isEnabled(AnActionEvent e) {
                        return tblCertificates.getSelectedObject() != null;
                    }
                }).setRemoveActionUpdater(new AnActionButtonUpdater() {
                    @Override
                    public boolean isEnabled(AnActionEvent e) {
                        return tblCertificates.getSelectedObject() != null;
                    }
                }).disableUpDownActions().createPanel();
        tablePanel.setPreferredSize(new Dimension(-1, 200));
    }

    private void addCertificate() {
        CertificateDialog dialog = new CertificateDialog(module, mapCert, waRole);
        dialog.show();
        if (dialog.isOK()) {
            setModified(true);
            String name = dialog.getNewlyAddedCert();
            tblCertificates.getListTableModel().addRow(mapCert.get(name));
            List<WindowsAzureCertificate> items = tblCertificates.getItems();
            for (int i = 0; i < items.size(); i++) {
                WindowsAzureCertificate cert = items.get(i);
                if (cert.getName().equalsIgnoreCase(name)) {
                    tblCertificates.addSelection(cert);
                    break;
                }
            }
            decideCurSelectedCert();
        }
    }

    private void removeCertificate() {
        try {
            WindowsAzureCertificate delCert = tblCertificates.getSelectedObject();
            if (delCert.isRemoteAccess() && delCert.isSSLCert()) {
                String temp = String.format("%s%s%s", message("sslTtl"), " and ", message("cmhLblRmtAces"));
                PluginUtil.displayErrorDialog(message("certRmTtl"), String.format(message("certComMsg"), temp, temp));
            } else if (delCert.isRemoteAccess()) {
                PluginUtil.displayErrorDialog(message("certRmTtl"), String.format(message("certComMsg"), message("cmhLblRmtAces"), message("cmhLblRmtAces")));
            } else if (delCert.isSSLCert()) {
                PluginUtil.displayErrorDialog(message("certRmTtl"), String.format(message("certComMsg"), message("sslTtl"), message("sslTtl")));
            } else {
                int choice = Messages.showOkCancelDialog(String.format(message("certRmMsg"), delCert.getName()), message("certRmTtl"), Messages.getQuestionIcon());
                if (choice == Messages.OK) {
                    delCert.delete();
                    certSelected = "";
                    setModified(true);
                    tblCertificates.getListTableModel().removeRow(tblCertificates.getSelectedRow());
                }
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("certErrTtl"), message("certErrMsg"), e);
        }
    }

    private void decideCurSelectedCert() {
        if (tblCertificates.getSelectedObject() != null) {
            WindowsAzureCertificate cert = tblCertificates.getSelectedObject();
            certSelected = cert.getName();
        } else {
            certSelected = "";
        }
    }

    private class CertificatesTableModel extends ListTableModel<WindowsAzureCertificate> {
        private CertificatesTableModel(java.util.List<WindowsAzureCertificate> listCertificates) {
            super(new ColumnInfo[]{NAME, THUMBPRINT}, listCertificates, 0);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            boolean retVal = true;
            @SuppressWarnings("unchecked")
            WindowsAzureCertificate certificate = getRowValue(rowIndex);
            try {
                if (certificate.isRemoteAccess() || certificate.isSSLCert()) {
                    retVal = false;
                }
            } catch (WindowsAzureInvalidProjectOperationException e) {
                PluginUtil.displayErrorDialogAndLog(message("certErrTtl"), message("certErrMsg"), e);
            }
            return retVal;
        }
    }
}
