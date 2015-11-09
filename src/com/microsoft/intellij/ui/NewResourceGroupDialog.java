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

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TitlePanel;
import com.microsoft.applicationinsights.management.rest.ApplicationInsightsManagementClient;
import com.microsoft.applicationinsights.management.rest.model.ResourceGroup;
import com.microsoft.applicationinsights.management.rest.model.Subscription;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.util.PluginUtil;

import javax.swing.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class NewResourceGroupDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField txtName;
    private JComboBox comboSub;
    private JComboBox comboReg;
    ApplicationInsightsManagementClient client;
    String subscription;
    Map<String, String> subMap = new HashMap<String, String>();
    static ResourceGroup group;

    public NewResourceGroupDialog(ApplicationInsightsManagementClient client, String subscription) {
        super(true);
        this.client = client;
        this.subscription = subscription;
        setTitle(message("newResGrpTtl"));
        init();
    }

    protected void init() {
        super.init();
        populateValues();
    }

    private void populateValues() {
        try {
            if (client != null) {
                List<Subscription> subList = client.getSubscriptions();
                // check at least single subscription is associated with the account
                if (subList.size() > 0) {
                    for (Subscription sub : subList) {
                        subMap.put(sub.getId(), sub.getName());
                    }
                    Collection<String> values = subMap.values();
                    String[] subNameArray = values.toArray(new String[values.size()]);
                    comboSub.setModel(new DefaultComboBoxModel(subNameArray));

                    // To do - Get list of locations available for subscription.
                    // For now pulling value using application insights function
                    List<String> regionList = client.getAvailableGeoLocations();
                    String[] regionArray = regionList.toArray(new String[regionList.size()]);
                    comboReg.setModel(new DefaultComboBoxModel(regionArray));
                    comboReg.setSelectedItem(regionArray[0]);

					/*
					 * If subscription name is there,
					 * dialog invoked from application insights dialog,
					 * hence disable subscription combo.
					 */
                    if (subscription != null && !subscription.isEmpty()) {
                        comboSub.setEnabled(false);
                        comboSub.setSelectedItem(subscription);
                    } else {
                        comboSub.setSelectedItem(subNameArray[0]);
                    }
                }
            }
        } catch (Exception ex) {
            AzurePlugin.log(message("getValuesErrMsg"), ex);
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected JComponent createTitlePane() {
        return new TitlePanel(message("newResGrpTtl"), message("newResGrpMsg"));
    }

    private String findKeyAsPerValue(String subName) {
        String key = "";
        for (Map.Entry<String, String> entry : subMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(subName)) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

    @Override
    protected void doOKAction() {
        boolean isValid = false;
        if (txtName.getText().trim().isEmpty()
                || ((String) comboSub.getSelectedItem()).isEmpty()
                || ((String) comboReg.getSelectedItem()).isEmpty()) {
            if (((String) comboSub.getSelectedItem()).isEmpty() || comboSub.getItemCount() <= 0) {
                PluginUtil.displayErrorDialog(message("aiErrTtl"), message("noSubErrMsg"));
            } else {
                PluginUtil.displayErrorDialog(message("aiErrTtl"), message("nameEmptyMsg"));
            }
        } else {
            try {
                String subId = findKeyAsPerValue((String) comboSub.getSelectedItem());
                group = client.createResourceGroup(subId,
                        txtName.getText().trim(), (String) comboReg.getSelectedItem());
                isValid = true;
            } catch (java.net.SocketTimeoutException e) {
                PluginUtil.displayErrorDialogAndLog(message("newResGrpTtl"), message("timeOutErr"), e);
            } catch (Exception ex) {
                PluginUtil.displayErrorDialogAndLog(message("newResGrpTtl"), message("newResErrMsg"), ex);
            }
        }
        if (isValid) {
            super.doOKAction();
        }
    }

    public static ResourceGroup getResourceGroup() {
        return group;
    }
}
