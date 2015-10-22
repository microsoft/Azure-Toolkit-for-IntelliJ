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
package com.microsoft.intellij.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.interopbridges.tools.windowsazure.OSFamilyType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoft.intellij.ui.AzureAbstractPanel;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.propertypage.Azure;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.intellij.AzurePlugin.log;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class AzureModulePanel implements AzureAbstractPanel {
    private JPanel contentPane;
    private JTextField txtServiceName;
    private JComboBox comboType;
    private JComboBox targetOSComboType;

    private Module myModule;
    private WindowsAzureProjectManager waProjManager;
    private static String[] arrType = new String[]{message("proPageBFEmul"), message("proPageBFCloud")};

    public AzureModulePanel(Module module) {
        this.myModule = module;
        init();
    }

    private void init() {
        loadProject();
        try {
            txtServiceName.setText(waProjManager.getServiceName());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("proPageErrTitle"), message("proPageErrMsgBox1") + message("proPageErrMsgBox2"), e);
        }
        comboType.setModel(new DefaultComboBoxModel(arrType));
        List<String> osNames = new ArrayList<String>();
        for (OSFamilyType osType : OSFamilyType.values()) {
            osNames.add(osType.getName());
        }
        targetOSComboType.setModel(new DefaultComboBoxModel(osNames.toArray(new String[osNames.size()])));
        WindowsAzurePackageType type;
        try {
            type = waProjManager.getPackageType();
            comboType.setSelectedItem(type.equals(WindowsAzurePackageType.LOCAL) ? arrType[0] : arrType[1]);
            //Set current value for target OS
            targetOSComboType.setSelectedItem(waProjManager.getOSFamily().getName());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("proPageErrTitle"), message("proPageErrMsgBox1") + message("proPageErrMsgBox2"), e);
        }
    }

    /**
     * This method loads the projects available in workspace.
     * selProject variable will contain value of current selected project.
     */
    private void loadProject() {
        try {
            waProjManager = WindowsAzureProjectManager.load(new File(PluginUtil.getModulePath(myModule)));
        } catch (Exception e) {
            PluginUtil.displayErrorDialog(message("remAccSyntaxErr"), message("proPageErrMsgBox1") + message("proPageErrMsgBox2"));
            log(message("remAccErProjLoad"), e);
        }
    }

    @Override
    public JComponent getPanel() {
        return contentPane;
    }

    @Override
    public String getDisplayName() {
        return message("cmhLblWinAz");
    }

    @Override
    public boolean doOKAction() {
        try {
            loadProject();
            waProjManager = Azure.performOK(waProjManager, txtServiceName.getText(), (String) comboType.getSelectedItem(),
                    (String) targetOSComboType.getSelectedItem());
            waProjManager.save();
            LocalFileSystem.getInstance().findFileByPath(PluginUtil.getModulePath(myModule)).refresh(true, true);
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("proPageErrTitle"), message("proPageErrMsgBox1") + message("proPageErrMsgBox2"), e);
            return false;
        }
        return true;
    }

    @Override
    public String getSelectedValue() {
        return null;
    }

    @Override
    public ValidationInfo doValidate() {
        return null;
    }

    @Override
    public String getHelpTopic() {
        return "windows_azure_project_project_property";
    }
}
