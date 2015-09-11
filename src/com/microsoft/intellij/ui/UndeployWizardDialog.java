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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TitlePanel;
import com.microsoft.intellij.AzureSettings;
import com.microsoft.intellij.runnable.FetchDeploymentsForHostedServiceWithProgressWindow;
import com.microsoft.intellij.runnable.LoadAccountWithProgressBar;
import com.microsoft.intellij.ui.util.UIUtils;
import com.microsoft.intellij.wizards.WizardCacheManager;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse.Deployment;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;
import static com.microsoft.intellij.ui.util.UIUtils.ElementWrapper;

public class UndeployWizardDialog extends DialogWrapper {
    private JPanel contentPane;
    private JComboBox subscriptionCombo;
    private JComboBox hostedServiceCombo;
    private JComboBox deploymentCombo;

    private Module myModule;
    private PublishData currentPublishData;

    public UndeployWizardDialog(Module module) {
        super(module.getProject());
        this.myModule = module;
        init();
    }

    @Override
    protected void init() {
        setTitle(message("undeployWizTitle"));
        setOKButtonText(message("undeployWizTitle"));
        subscriptionCombo.addItemListener(createSubscriptionComboListener());
        hostedServiceCombo.addItemListener(createHostedServiceComboListener());
        deploymentCombo.addItemListener(createDeploymentComboListener());
        AzureSettings azureSettings = AzureSettings.getSafeInstance(myModule.getProject());
        if (!azureSettings.isSubscriptionLoaded()) {
            LoadAccountWithProgressBar task = new LoadAccountWithProgressBar(myModule.getProject());
            ProgressManager.getInstance().runProcessWithProgressSynchronously(task, "Loading Account Settings...", true, myModule.getProject());
            azureSettings.setSubscriptionLoaded(true);
        }
        populateSubscriptionCombo();
        validatePageComplete();

        super.init();
    }

    private ItemListener createSubscriptionComboListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                currentPublishData = UIUtils.changeCurrentSubAsPerCombo((JComboBox) e.getSource());

                hostedServiceCombo.setEnabled(false);
                deploymentCombo.setEnabled(false);
                deploymentCombo.removeAllItems();
                populateHostedServices();
//                setPageComplete(validatePageComplete());
            }
        };
    }

    private ItemListener createHostedServiceComboListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                deploymentCombo.removeAllItems();
                deploymentCombo.setEnabled(false);

                populateDeployment();

                setComponentState();

//                setPageComplete(validatePageComplete());
            }
        };
    }

    private ItemListener createDeploymentComboListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
//                setPageComplete(validatePageComplete());
            }
        };
    }

    private void populateSubscriptionCombo() {
        UIUtils.populateSubscriptionCombo(subscriptionCombo);
        setComponentState();
    }

    public void populateHostedServices() {
        if (currentPublishData != null) {
            Subscription currentSubscription = currentPublishData.getCurrentSubscription();
            List<HostedService> hostedServices = currentPublishData.getServicesPerSubscription().get(currentSubscription.getId());
            if (hostedServices != null) {
                hostedServiceCombo.removeAllItems();
                for (HostedService hsd : hostedServices) {
                    hostedServiceCombo.addItem(new ElementWrapper<HostedService>(hsd.getServiceName(), hsd));
                }
                if (hostedServiceCombo.getItemCount() > 0) {
                    String defaultSelection = null;
                    HostedService currentHostedService = WizardCacheManager.getCurentHostedService();
                    if (currentHostedService != null) {
                        defaultSelection = currentHostedService.getServiceName();
                    }
                    UIUtils.selectByText(hostedServiceCombo, defaultSelection);
                }
            }
        }
        setComponentState();
    }

    private void populateDeployment() {
        int sel = hostedServiceCombo.getSelectedIndex();
        if (sel > -1) {
            HostedServiceGetDetailedResponse hostedServiceDetailed;
            FetchDeploymentsForHostedServiceWithProgressWindow progress =
                    new FetchDeploymentsForHostedServiceWithProgressWindow(null, hostedServiceCombo.getSelectedItem().toString());
            ProgressManager.getInstance().runProcessWithProgressSynchronously(progress, "Progress Information", true, myModule.getProject());

            hostedServiceDetailed = progress.getHostedServiceDetailed();

            deploymentCombo.removeAllItems();

            for (HostedServiceGetDetailedResponse.Deployment deployment : hostedServiceDetailed.getDeployments()) {
                if (deployment.getName() == null) {
                    continue;
                }
                if (deployment.getStatus().equals(DeploymentStatus.Running)) {
                    String label = deployment.getLabel();
                    String id = label + " - " + deployment.getDeploymentSlot();
                    deploymentCombo.addItem(new ElementWrapper<Deployment>(id, deployment));
                }
            }
//            setComponentState();
        }
    }

    private void setComponentState() {
        if (hostedServiceCombo == null) {
            return;
        }
        hostedServiceCombo.setEnabled(subscriptionCombo.getSelectedIndex() > -1 && hostedServiceCombo.getItemCount() > 0);
        if (deploymentCombo == null) {
            return;
        }
        deploymentCombo.setEnabled(hostedServiceCombo.getSelectedIndex() > -1 && deploymentCombo.getItemCount() > 0);
    }

    protected boolean validatePageComplete() {
        Object subscription = subscriptionCombo.getSelectedItem();
        if (subscription == null) {
//            setErrorMessage("subscription can not be null or empty");
            return false;
        }
        Object service = hostedServiceCombo.getSelectedItem();
        if (service == null) {
//            setErrorMessage(message("hostedServiceIsNull"));
            return false;
        }
        ElementWrapper<Deployment> deploymentItem = (ElementWrapper<Deployment>) deploymentCombo.getSelectedItem();
        if (deploymentItem == null) {
//            setErrorMessage(message("deploymentIsNull"));
            return false;
        }
//        setErrorMessage(null);
        return true;
    }

    public String getServiceName() {
        return hostedServiceCombo.getSelectedItem().toString();
    }

    public Deployment getDeployment() {
        return ((ElementWrapper<Deployment>) deploymentCombo.getSelectedItem()).getValue();
    }

    protected JComponent createTitlePane() {
        return new TitlePanel(message("unpubplishAzureProjPage"), "");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected String getHelpId() {
        return "unpublish_project_command";
    }
}