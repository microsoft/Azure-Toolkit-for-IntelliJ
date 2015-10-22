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

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.util.AppCmpntParam;
import com.microsoft.intellij.util.PluginUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.intellij.AzurePlugin.log;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class ApplicationsTab extends JPanel {
    private JPanel contentPane;
    private JTable appTable;
    private JButton addButton;
    private JButton removeButton;

    private Project project;
    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole waRole;
    private boolean newRole;
    private boolean modified;

    private final ArrayList<AppCmpntParam> appList = new ArrayList<AppCmpntParam>();
    private List<String> fileToDel;

    public JPanel getPanel() {
        return contentPane;
    }

    void init(Project project, WindowsAzureProjectManager waProjManager, WindowsAzureRole waRole, List<String> fileToDel) {
        this.project = project;
        this.waProjManager = waProjManager;
        this.waRole = waRole;
        this.fileToDel = fileToDel;
        this.newRole = waProjManager == null;
        appTable.setModel(new ApplicationsTableModel());
        appTable.getSelectionModel().addListSelectionListener(createAppTableListener());
        addButton.addActionListener(createAddApplicationListener());
        removeButton.addActionListener(createRemoveButtonListener());
        removeButton.setEnabled(false);
    }

    private ListSelectionListener createAppTableListener() {
        return new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean buttonsEnabled = appTable.getSelectedRow() > -1;
                removeButton.setEnabled(buttonsEnabled);
            }
        };
    }

    private ActionListener createAddApplicationListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final AddApplicationDialog addApplicationDialog = new AddApplicationDialog(project, waRole, getAppsAsNames());
                addApplicationDialog.show();
                if (addApplicationDialog.isOK()) {
                    appList.add(addApplicationDialog.getAppCmpntParam());
                    ((ApplicationsTableModel) appTable.getModel()).fireTableDataChanged();
                    if (!newRole) {
                        List<WindowsAzureRoleComponent> srvApp = null;
                        try {
                            if (!getAppsAsNames().isEmpty()) {
                                AppCmpntParam app = getAppsList().get(getAppsList().size() - 1);
                                String impSrc = app.getImpSrc();
                                String impAs = app.getImpAs();

                                srvApp = waRole.getServerApplications();
                                String approotPathSubStr = String.format("%s%s%s%s", waProjManager.getProjectName(),
                                        File.separator, waRole.getName(), message("approot"));
                                boolean needCldAttr = true;
                                if (impSrc.contains(approotPathSubStr)) {
                                    needCldAttr = false;
                                }
                                if (srvApp.size() == 0) {
                                    waRole.addServerApplication(impSrc, impAs, app.getImpMethod(), AzurePlugin.cmpntFile, needCldAttr);
                                    modified = true;
                                } else {
                                    boolean isExist = false;
                                    for (int i = 0; i < srvApp.size(); i++) {
                                        WindowsAzureRoleComponent c = srvApp.get(i);
                                        if (impAs.equalsIgnoreCase(c.getDeployName()) && impSrc.equalsIgnoreCase(c.getImportPath())) {
                                            isExist = true;
                                            break;
                                        }
                                    }
                                    if (!isExist) {
                                        waRole.addServerApplication(impSrc, impAs, app.getImpMethod(), AzurePlugin.cmpntFile, needCldAttr);
                                        modified = true;
                                    }
                                }
                            }
                        } catch (WindowsAzureInvalidProjectOperationException ex) {
                            PluginUtil.displayErrorDialogAndLog(message("srvErrTtl"), message("addSrvAppErrMsg"), ex);
                            return;
                        }
                    }
                }
            }
        };
    }

    private ActionListener createRemoveButtonListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int curSelIndex = appTable.getSelectedRow();
                if (curSelIndex > -1) {
                    if (!newRole) {
                        try {
                            int choice = Messages.showYesNoDialog(message("appRmvMsg"), message("appRmvTtl"), Messages.getQuestionIcon());
                            if (choice == Messages.YES) {
                                String cmpntName = appList.get(curSelIndex).getImpAs();
                                String cmpntPath = String.format("%s%s%s%s%s",
                                        PluginUtil.getModulePath(ModuleManager.getInstance(project).findModuleByName(waProjManager.getProjectName())),
                                        File.separator, waRole.getName(), message("approot"), cmpntName);
                                waRole.removeServerApplication(cmpntName);
                                modified = true;
                                if (!fileToDel.contains(cmpntPath)) {
                                    fileToDel.add(cmpntPath);
                                }
//                                JdkSrvConfig.getTableViewer().refresh();
//                                JdkSrvConfigListener.disableRemoveButton();
                            } else {
                                return;
                            }
                        } catch (Exception ex) {
                            PluginUtil.displayErrorDialogAndLog(message("srvErrTtl"), message("rmvSrvAppErrMsg"), ex);
                            return;
                        }
                    }
                    appList.remove(curSelIndex);
                    ((ApplicationsTableModel) appTable.getModel()).fireTableDataChanged();

                }
            }
        };
    }

    void initAppTab() {
        if (newRole) {
            try {
                AppCmpntParam acp = new AppCmpntParam();
                acp.setImpAs(waRole.getComponents().get(0).getDeployName());
                appList.add(acp);
            } catch (WindowsAzureInvalidProjectOperationException e) {
                log(message("error"), e);
            }
        } else {
            List<WindowsAzureRoleComponent> srvApp1 = null;
            // Get previously added sever applications
            try {
                srvApp1 = waRole.getServerApplications();
            } catch (WindowsAzureInvalidProjectOperationException e) {
                PluginUtil.displayErrorDialogAndLog(message("srvErrTtl"), message("getSrvAppErrMsg"), e);
            }
        /* Display existing server
         * applications in Applications table
		 */
            for (int i = 0; i < srvApp1.size(); i++) {
                WindowsAzureRoleComponent cmpnt = srvApp1.get(i);
                AppCmpntParam acp = new AppCmpntParam();
                acp.setImpAs(cmpnt.getDeployName());
                appList.add(acp);
            }
        }
    }

    void setEnable(boolean enable) {
        appTable.setEnabled(enable);
        addButton.setEnabled(enable);
    }

    /**
     * @return added application Asnames which is to be set in table.
     */
    public ArrayList<String> getAppsAsNames() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < appList.size(); i++) {
            list.add(appList.get(i).getImpAs());
        }
        return list;
    }

    /**
     * @return applist
     */
    public ArrayList<AppCmpntParam> getAppsList() {
        return appList;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    private class ApplicationsTableModel extends AbstractTableModel {
        public final String[] COLUMNS = new String[]{"Name"};

        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        public int getRowCount() {
            return appList.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            AppCmpntParam appCmpntParam = appList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return appCmpntParam.getImpAs();
            }
            return null;
        }
    }
}
