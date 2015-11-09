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
import com.microsoft.applicationinsights.management.rest.model.Resource;
import com.microsoft.applicationinsights.management.rest.model.ResourceGroup;
import com.microsoft.applicationinsights.management.rest.model.Subscription;
import com.microsoft.applicationinsights.preference.ApplicationInsightsResource;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.util.PluginUtil;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class ApplicationInsightsNewDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField txtName;
    private JComboBox comboSub;
    private JComboBox comboGrp;
    private JComboBox comboReg;
    private JButton btnNew;
    ApplicationInsightsManagementClient client;
    Map<String, String> subMap = new HashMap<String, String>();
    String currentSub;
    static ApplicationInsightsResource resourceToAdd;

    public ApplicationInsightsNewDialog(ApplicationInsightsManagementClient client) {
        super(true);
        this.client = client;
        setTitle(message("aiErrTtl"));
        init();
    }

    protected void init() {
        super.init();
        comboSub.addItemListener(subscriptionListener());
        btnNew.addActionListener(newBtnListener());
        populateValues();
    }

    private ItemListener subscriptionListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String newSub = (String) comboSub.getSelectedItem();
                String prevResGrpVal = (String) comboGrp.getSelectedItem();
                String key = findKeyAsPerValue(newSub);
                if (key != null && !key.isEmpty()) {
                    if (currentSub.equalsIgnoreCase(newSub)) {
                        populateResourceGroupValues(key, prevResGrpVal);
                    } else {
                        populateResourceGroupValues(key, "");
                    }
                    currentSub = newSub;
                } else {
                    AzurePlugin.log(message("getSubIdErrMsg"));
                }
            }
        };
    }

    private ActionListener newBtnListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String subTxt = (String) comboSub.getSelectedItem();
                NewResourceGroupDialog newResourceGroupDialog = new NewResourceGroupDialog(client, subTxt);
                newResourceGroupDialog.show();
                // populate data in storage registry dialog
                if (newResourceGroupDialog.isOK()) {
                    ResourceGroup group = NewResourceGroupDialog.getResourceGroup();
                    if (group != null) {
                        populateResourceGroupValues(findKeyAsPerValue(subTxt), group.getName());
                    }
                }
            }
        };
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
                    Set<String> keySet = subMap.keySet();
                    String[] subKeyArray = keySet.toArray(new String[keySet.size()]);

                    comboSub.setModel(new DefaultComboBoxModel(subNameArray));
                    comboSub.setSelectedItem(subNameArray[0]);
                    currentSub = subNameArray[0];

                    populateResourceGroupValues(subKeyArray[0], "");
                    btnNew.setEnabled(true);
                } else {
                    btnNew.setEnabled(false);
                }

                List<String> regionList = client.getAvailableGeoLocations();
                String[] regionArray = regionList.toArray(new String[regionList.size()]);
                comboReg.setModel(new DefaultComboBoxModel(regionArray));
                comboReg.setSelectedItem(regionArray[0]);
            }
        } catch (Exception ex) {
            AzurePlugin.log(message("getValuesErrMsg"), ex);
        }
    }

    private void populateResourceGroupValues(String subId, String valtoSet) {
        try {
            List<ResourceGroup> groupList = client.getResourceGroups(subId);
            if (groupList.size() > 0) {
                List<String> groupStringList = new ArrayList<String>();
                for (ResourceGroup group : groupList) {
                    groupStringList.add(group.getName());
                }
                String[] groupArray = groupStringList.toArray(new String[groupStringList.size()]);
                comboGrp.removeAll();
                comboGrp.setModel(new DefaultComboBoxModel(groupArray));
                if (valtoSet.isEmpty() || !groupStringList.contains(valtoSet)) {
                    comboGrp.setSelectedItem(groupArray[0]);
                } else {
                    comboGrp.setSelectedItem(valtoSet);
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
        return new TitlePanel(message("newKeyTtl"), message("newKeyMsg"));
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
                || ((String) comboGrp.getSelectedItem()).isEmpty()
                || ((String) comboReg.getSelectedItem()).isEmpty()) {
            if (((String) comboSub.getSelectedItem()).isEmpty() || comboSub.getItemCount() <= 0) {
                PluginUtil.displayErrorDialog(message("aiErrTtl"), message("noSubErrMsg"));
            } else if (((String) comboGrp.getSelectedItem()).isEmpty() || comboGrp.getItemCount() <= 0) {
                PluginUtil.displayErrorDialog(message("aiErrTtl"), message("noResGrpErrMsg"));
            } else {
                PluginUtil.displayErrorDialog(message("aiErrTtl"), message("nameEmptyMsg"));
            }
        } else {
            try {
                String subId = findKeyAsPerValue((String) comboSub.getSelectedItem());
                Resource resource = client.createResource(subId, (String) comboGrp.getSelectedItem(),
                        txtName.getText(), (String) comboReg.getSelectedItem());
                resourceToAdd = new ApplicationInsightsResource(resource.getName(), resource.getInstrumentationKey(),
                        (String) comboSub.getSelectedItem(), subId, resource.getLocation(),
                        resource.getResourceGroup(), true);
                isValid = true;
            } catch (java.net.SocketTimeoutException e) {
                PluginUtil.displayErrorDialogAndLog(message("aiErrTtl"), message("timeOutErr"), e);
            } catch (Exception ex) {
                PluginUtil.displayErrorDialogAndLog(message("aiErrTtl"), message("resCreateErrMsg"), ex);
            }
        }
        if (isValid) {
            super.doOKAction();
        }
    }

    public static ApplicationInsightsResource getResource() {
        return resourceToAdd;
    }
}
