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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class ServiceEndpointsPanel implements AzureAbstractConfigurablePanel {
    private static final String DISPLAY_NAME = "Service Endpoints";
    private JPanel contentPane;

    private JComboBox prefNameCmb;
    private JButton editBtn;
    private JTextField txtPortal;
    private JTextField txtMangmnt;
    private JTextField txtBlobUrl;
    private JTextField txtPubSet;

    private String valOkToLeave = "";

    public ServiceEndpointsPanel() {
        if (!AzurePlugin.IS_ANDROID_STUDIO && AzurePlugin.IS_WINDOWS) {
            init();
        }
    }

    protected void init() {
        prefNameCmb.addItemListener(createPrefNameCmbListener());
        setToDefaultName();
        populateValues();
        editBtn.addActionListener(createEditBtnListener());
    }

    public JComponent getPanel() {
        return contentPane;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean doOKAction() {
        try {
            String cmbValue = (String) prefNameCmb.getSelectedItem();
			PreferenceSetUtil.setPrefDefault(cmbValue,  AzurePlugin.prefFilePath);
        } catch (Exception e) {
            PluginUtil.displayErrorDialog(message("errTtl"), message("setPrefErMsg"));
            return false;
        }
        return true;
    }

    @Override
    public String getSelectedValue() {
        return null;
    }

    public ValidationInfo doValidate() {
        return null;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    private ItemListener createPrefNameCmbListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                populateValues();
            }
        };
    }

    private ActionListener createEditBtnListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final File file = new File(AzurePlugin.prefFilePath);
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        final VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                        if (vf != null) {
                            try {
                                vf.setWritable(true);
                                DataContext dataContext = DataManager.getInstance().getDataContext();
                                Project project = DataKeys.PROJECT.getData(dataContext);
                                FileEditorManager.getInstance(project).openFile(vf, true);
                            } catch (IOException ex) {
                                AzurePlugin.log(message("error"), ex);
                            }
                        }
                    }
                });
            }
        };
    }

    /**
     * Method sets extracted <preferenceset> names to
     * active set combo box. By default, it is set to
     * the default setting of the parent <preferencesets> element.
     * But if user is visiting service endpoint page
     * after okToLeave then value modified by user is populated.
     */
    private void setToDefaultName() {
        try {
            prefNameCmb.setModel(new DefaultComboBoxModel(PreferenceSetUtil.getPrefSetNameArr(AzurePlugin.prefFilePath)));
            if (!valOkToLeave.isEmpty()) {
                prefNameCmb.setSelectedItem(valOkToLeave);
            } else {
                prefNameCmb.setSelectedItem(PreferenceSetUtil.getSelectedPreferenceSetName(AzurePlugin.prefFilePath));
            }
        } catch (Exception e) {
            PluginUtil.displayErrorDialog(message("errTtl"), message("getPrefErMsg"));
        }
    }

    /**
     * Blob, Management, Portal
     * and Publish Settings
     * values from preferencesets.xml
     * will be populated according to active set value.
     */
    private void populateValues() {
        String nameInCombo = (String) prefNameCmb.getSelectedItem();
        try {
            txtPortal.setText(PreferenceSetUtil.getSelectedPortalURL(nameInCombo, AzurePlugin.prefFilePath));
            txtMangmnt.setText(PreferenceSetUtil.getManagementURL(nameInCombo, AzurePlugin.prefFilePath));
            txtBlobUrl.setText(PreferenceSetUtil.getBlobServiceURL(nameInCombo, AzurePlugin.prefFilePath));
            txtPubSet.setText(PreferenceSetUtil.getSelectedPublishSettingsURL(nameInCombo, AzurePlugin.prefFilePath));
        } catch (Exception e) {
            PluginUtil.displayErrorDialog(message("errTtl"), message("getPrefErMsg"));
        }
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void reset() {
        setToDefaultName();
        populateValues();
    }
}
