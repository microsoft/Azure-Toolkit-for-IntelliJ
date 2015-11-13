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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.wizard.WizardModel;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoft.intellij.ui.components.AzureWizardStep;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.util.AppCmpntParam;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AzureWizardModel extends WizardModel {
    private SelectLocationStep selectLocationStep;
    private PublishSettingsStep publishSettingsStep;
    private KeyFeaturesStep keyFeaturesStep;
    private Project myProject;
    private WindowsAzureProjectManager waProjMgr;

    public AzureWizardModel(final Project project, WindowsAzureProjectManager waProjMgr) {
        super(AzureBundle.message("wizPageTitle"));
        myProject = project;
        this.waProjMgr = waProjMgr;
        selectLocationStep = new SelectLocationStep(this.getTitle(), this);
        publishSettingsStep = new PublishSettingsStep(this.getTitle(), this);
        keyFeaturesStep = new KeyFeaturesStep(this.getTitle());
        add(selectLocationStep);
        add(publishSettingsStep);
        add(keyFeaturesStep);
    }

    public Map<String, String> getDeployPageValues() {
        return publishSettingsStep.getJdkServerPanel().getDeployPageValues();
    }

    public Map<String, Boolean> getKeyFeaturesValues() {
        return keyFeaturesStep.getValues();
    }

    public String getProjectName() {
        return selectLocationStep.getProjectName();
    }

    public String getProjectLocation() {
        return selectLocationStep.getProjectLocation();
    }

    public boolean isUseDefaultLocation() {
        return selectLocationStep.isUseDefaultLocation();
    }

    public ArrayList<String> getAppsAsNames() {
        return publishSettingsStep.getJdkServerPanel().getApplicationsTab().getAppsAsNames();
    }

    public boolean isLicenseAccepted() {
        return publishSettingsStep.getJdkServerPanel().createAccLicenseAggDlg(true) && publishSettingsStep.getJdkServerPanel().createAccLicenseAggDlg(false);
    }

    /**
     * @return applist
     */
    public ArrayList<AppCmpntParam> getAppsList() {
        return publishSettingsStep.getJdkServerPanel().getApplicationsTab().getAppsList();
    }

    /**
     * Method returns access key from storage registry
     * according to account name selected in combo box.
     *
     * @param combo
     * @return
     */
    public static String getAccessKey(JComboBox combo) {
        String key = "";
        // get access key.
        int strgAccIndex = combo.getSelectedIndex();
        List<StorageAccount> list =  StorageAccountRegistry.getStrgList();
        if (strgAccIndex > 0 && !combo.getSelectedItem().toString().isEmpty() && list.size() > 0) {
            key = list.get(strgAccIndex - 1).getStrgKey();
        }
        return key;
    }

    public Project getMyProject() {
        return myProject;
    }

    public WindowsAzureProjectManager getWaProjMgr() {
        return waProjMgr;
    }

    public WindowsAzureRole getWaRole() {
        try {
            return waProjMgr.getRoles().get(0);
        } catch (WindowsAzureInvalidProjectOperationException e) {

            return null;
        }
    }

    public ValidationInfo doValidate() {
        return ((AzureWizardStep) getCurrentStep()).doValidate();
    }
}