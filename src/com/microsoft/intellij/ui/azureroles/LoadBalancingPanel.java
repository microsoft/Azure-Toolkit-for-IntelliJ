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

import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.interopbridges.tools.windowsazure.*;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.model.RoleAndEndpoint;
import com.microsoftopentechnologies.azurecommons.roleoperations.WARLoadBalanceUtilMethods;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class LoadBalancingPanel extends BaseConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private JPanel contentPane;
    private JCheckBox btnSsnAffinity;
    private JComboBox comboEndpt;
    private JLabel lblEndptToUse;
    private JTextPane lbNote;
    private boolean isManualUpdate = true;

    private List<WindowsAzureEndpoint> endpointsList;
    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole waRole;

    public LoadBalancingPanel(WindowsAzureProjectManager waProjManager, WindowsAzureRole waRole) {
        this.waProjManager = waProjManager;
        this.waRole = waRole;
        init();
    }

    private void init() {
        try {
            if (waRole != null) {
                WindowsAzureEndpoint endPt = waRole.getSessionAffinityInputEndpoint();
                if (endPt == null) {
                    btnSsnAffinity.setSelected(false);
                    lblEndptToUse.setEnabled(false);
                    comboEndpt.setEnabled(false);
                    comboEndpt.removeAllItems();
                } else {
                    populateEndPointList();
                    comboEndpt.setSelectedItem(String.format(message("dbgEndPtStr"), endPt.getName(), endPt.getPort(), endPt.getPrivatePort()));
                    isEditableEndpointCombo(endPt);
                }
            }
        } catch (Exception e) {
            PluginUtil.displayErrorDialogAndLog(message("adRolErrTitle"), message("dlgDbgErr"), e);
        }
        btnSsnAffinity.addItemListener(createBtnSsnAffinityListener());
        comboEndpt.addItemListener(createComboEndptListener());
        Messages.configureMessagePaneUi(lbNote, message("lbNote"));
    }

    private ItemListener createComboEndptListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && isManualUpdate) {
                    try {
                        if (btnSsnAffinity.isSelected()) {
                            WindowsAzureEndpoint stcSelEndpoint = WARLoadBalanceUtilMethods.getStickySelectedEndpoint(waRole, (String) comboEndpt.getSelectedItem());
                            if (!stcSelEndpoint.equals(waRole.getSessionAffinityInputEndpoint())) {
                                waRole.setSessionAffinityInputEndpoint(null);
                                setModified(true);
                                waRole.setSessionAffinityInputEndpoint(stcSelEndpoint);
                            }
                        }
                    } catch (WindowsAzureInvalidProjectOperationException ex) {
                        PluginUtil.displayErrorDialogAndLog(message("adRolErrTitle"), message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), ex);
                    }
                }
            }
        };
    }

    private ItemListener createBtnSsnAffinityListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                lblEndptToUse.setEnabled(btnSsnAffinity.isSelected());
                comboEndpt.setEnabled(btnSsnAffinity.isSelected());
                if (btnSsnAffinity.isSelected()) {
                    if (isManualUpdate) {
                        enableSessionAff();
                    }
                } else {
                    try {
                        comboEndpt.removeAllItems();
                        waRole.setSessionAffinityInputEndpoint(null);
                        setModified(true);
                    } catch (WindowsAzureInvalidProjectOperationException ex) {
                        PluginUtil.displayErrorDialogAndLog(message("adRolErrTitle"), message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), ex);
                    }
                }
            }
        };
    }

    /**
     * Enable session affinity.
     */
    protected void enableSessionAff() {
        try {
            WindowsAzureEndpoint endpt = null;
            isManualUpdate = false;
            populateEndPointList();
            endpt = WARLoadBalanceUtilMethods.findInputEndpt(waRole, endpointsList);
            if (endpt == null) {
                int choice = Messages.showYesNoDialog(message("lbCreateEndptTtl"), message("lbCreateEndptMsg"), Messages.getQuestionIcon());
                if (choice == Messages.YES) {
                    WindowsAzureEndpoint newEndpt = createEndpt();
                    populateEndPointList();
                    comboEndpt.setSelectedItem(String.format(message("dbgEndPtStr"), newEndpt.getName(), newEndpt.getPort(), newEndpt.getPrivatePort()));
                    waRole.setSessionAffinityInputEndpoint(newEndpt);
                    btnSsnAffinity.setSelected(true);
                    setModified(true);
                    isEditableEndpointCombo(newEndpt);
                } else {
                    btnSsnAffinity.setSelected(false);
                    lblEndptToUse.setEnabled(false);
                    comboEndpt.setEnabled(false);
                }
            } else {
                comboEndpt.setSelectedItem(String.format(message("dbgEndPtStr"), endpt.getName(), endpt.getPort(), endpt.getPrivatePort()));
                waRole.setSessionAffinityInputEndpoint(endpt);
                setModified(true);
                isEditableEndpointCombo(endpt);
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("adRolErrTitle"), message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), e);
        } finally {
            isManualUpdate = true;
        }
    }

    private void isEditableEndpointCombo(WindowsAzureEndpoint endPt) throws WindowsAzureInvalidProjectOperationException {
        comboEndpt.setEnabled(!endPt.equals(waRole.getSslOffloadingInputEndpoint()));
    }

    /**
     * Method creates endpoint associated with session affinity.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private WindowsAzureEndpoint createEndpt() throws WindowsAzureInvalidProjectOperationException {
        RoleAndEndpoint obj = WARLoadBalanceUtilMethods.createEndpt(waRole, waProjManager);
        setModified(true);
        WindowsAzureEndpoint endpt = obj.getEndPt();
//        waRole = obj.getRole();
        return endpt;
    }

    /**
     * Populates endpoints having type input in combo box.
     *
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private void populateEndPointList() throws WindowsAzureInvalidProjectOperationException {
        endpointsList = waRole.getEndpoints();
        comboEndpt.removeAllItems();
        for (WindowsAzureEndpoint endpoint : endpointsList) {
            if (endpoint.getEndPointType().equals(WindowsAzureEndpointType.Input) &&
                    endpoint.getPrivatePort() != null && !endpoint.equals(waRole.getDebuggingEndpoint())) {
                comboEndpt.addItem(String.format(message("dbgEndPtStr"), endpoint.getName(), endpoint.getPort(), endpoint.getPrivatePort()));
            }
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

    @Nls
    @Override
    public String getDisplayName() {
        return message("cmhLblLdBlnc");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "windows_azure_loadbalance_page";
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
            PluginUtil.displayErrorDialogAndLog(message("adRolErrTitle"), message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), e);
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
}
