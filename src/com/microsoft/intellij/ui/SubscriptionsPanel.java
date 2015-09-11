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
import com.microsoft.intellij.AzureSettings;
import com.microsoft.intellij.util.MethodUtils;
import com.microsoft.intellij.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azurecommons.deploy.propertypages.SubscriptionPropertyPageTableElement;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class SubscriptionsPanel implements AzureAbstractPanel {
    private JPanel contentPane;
    private JTable subscriptionsTable;
    private JButton importButton;
    private JButton addButton;
    private JButton removeButton;

    private Project myProject;

    public SubscriptionsPanel(Project project) {
        this.myProject = project;
        init();
    }

    protected void init() {
        subscriptionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subscriptionsTable.setModel(new SubscriptionsTableModel(getTableContent()));
        for (int i = 0; i < subscriptionsTable.getColumnModel().getColumnCount(); i++) {
            TableColumn each = subscriptionsTable.getColumnModel().getColumn(i);
            each.setPreferredWidth(SubscriptionsTableModel.getColumnWidth(i, 450));
        }
        importButton.addActionListener(createImportSubscriptionAction());
        removeButton.addActionListener(createRemoveButtonListener());
        removeButton.setEnabled(false);
        subscriptionsTable.getSelectionModel().addListSelectionListener(createSubscriptionsTableListener());
    }

    public JComponent getPanel() {
        return contentPane;
    }

    public String getDisplayName() {
        return message("cmhLblSubscrpt");
    }

    public boolean doOKAction() {
        return true;
    }

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

    private ActionListener createRemoveButtonListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int curSelIndex = subscriptionsTable.getSelectedRow();
                if (curSelIndex > -1) {
                    String id = (String) subscriptionsTable.getModel().getValueAt(curSelIndex, 1);
                    WizardCacheManager.removeSubscription(id);
                    AzureSettings.getSafeInstance(myProject).savePublishDatas();
                    ((SubscriptionsTableModel) subscriptionsTable.getModel()).setSubscriptions(getTableContent());
                    ((SubscriptionsTableModel) subscriptionsTable.getModel()).fireTableDataChanged();
                }
            }
        };
    }

    private ListSelectionListener createSubscriptionsTableListener() {
        return new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                removeButton.setEnabled(subscriptionsTable.getSelectedRow() > -1);
            }
        };
    }

    private ActionListener createImportSubscriptionAction() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final ImportSubscriptionDialog importSubscriptionDialog = new ImportSubscriptionDialog();
                importSubscriptionDialog.show();
                if (importSubscriptionDialog.isOK()) {
                    String fileName = importSubscriptionDialog.getPublishSettingsPath();
                    MethodUtils.handleFile(fileName, myProject);
                    ((SubscriptionsTableModel) subscriptionsTable.getModel()).setSubscriptions(getTableContent());
                    ((SubscriptionsTableModel) subscriptionsTable.getModel()).fireTableDataChanged();
                }
            }
        };
    }

    /**
     * Method prepares storage account list to show in table.
     */
    private List<SubscriptionPropertyPageTableElement> getTableContent() {
        Collection<PublishData> publishDatas = WizardCacheManager.getPublishDatas();
        List<SubscriptionPropertyPageTableElement> tableRowElements = new ArrayList<SubscriptionPropertyPageTableElement>();
        for (PublishData pd : publishDatas) {
            for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
                SubscriptionPropertyPageTableElement el = new SubscriptionPropertyPageTableElement();
                el.setSubscriptionId(sub.getId());
                el.setSubscriptionName(sub.getName());
                if (!tableRowElements.contains(el)) {
                    tableRowElements.add(el);
                }
            }
        }
        return tableRowElements;
    }

//    public String getSelectedValue() {
//        int selectedIndex = subscriptionsTable.getSelectedRow();
//        if (selectedIndex >= 0) {
//            return ((SubscriptionsTableModel) subscriptionsTable.getModel()).getAccountNameAtIndex(subscriptionsTable.getSelectedRow());
//        }
//        return null;
//    }

    private static class SubscriptionsTableModel extends AbstractTableModel {
        public static final String[] COLUMNS = new String[]{message("subscriptionColName"), message("subscriptionIdColName")};
        private java.util.List<SubscriptionPropertyPageTableElement> subscriptions;

        public SubscriptionsTableModel(List<SubscriptionPropertyPageTableElement> subscriptions) {
            this.subscriptions = subscriptions;
        }

        public void setSubscriptions(List<SubscriptionPropertyPageTableElement> subscriptions) {
            this.subscriptions = subscriptions;
        }

//        public String getAccountNameAtIndex(int index) {
//            return subscriptions.get(index).getStorageName();
//        }

        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        public static int getColumnWidth(int column, int totalWidth) {
            switch (column) {
                case 0:
                    return (int) (totalWidth * 0.4);
                default:
                    return (int) (totalWidth * 0.6);
            }
        }

        public int getRowCount() {
            return subscriptions.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            SubscriptionPropertyPageTableElement subscription = subscriptions.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return subscription.getSubscriptionName();
                case 1:
                    return subscription.getSubscriptionId();
            }
            return null;
        }
    }
}

