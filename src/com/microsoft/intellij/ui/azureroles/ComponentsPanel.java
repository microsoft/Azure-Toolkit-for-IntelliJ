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
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.AnActionButtonUpdater;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.interopbridges.tools.windowsazure.*;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class ComponentsPanel extends BaseConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private JPanel contentPane;
    private JPanel tablePanel;
    private TableView<WindowsAzureRoleComponent> tblComponents;

    private Project project;
    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;
    private List<WindowsAzureRoleComponent> listComponents;

    private ArrayList<File> fileToDel = new ArrayList<File>();

    public ComponentsPanel(Project project, WindowsAzureProjectManager waProjManager, WindowsAzureRole windowsAzureRole) {
        this.project = project;
        this.waProjManager = waProjManager;
        this.windowsAzureRole = windowsAzureRole;
        try {
            listComponents = windowsAzureRole.getComponents();
        } catch (Exception e) {
            PluginUtil.displayErrorDialogAndLog(message("cmpntSetErrTtl"), message("cmpntgetErrMsg"), e);
        }
        init();
    }

    private void init() {
        ListTableModel<WindowsAzureRoleComponent> model =
                new ListTableModel<WindowsAzureRoleComponent>(new ColumnInfo[]{IMPORT, FROM, AS, DEPLOY, TO}, listComponents);
        tblComponents.setModelAndUpdateColumns(model);
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
        return message("cmhLblCmpnts");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "windows_azure_components_page";
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
            /*
             * Delete files from approot,
    		 * whose entry from component table is removed
    		 * Should be outside of above isSaved() if condition
    		 * as performOk() of ServerConfiguration page is called first
    		 * and project manager object is saved on that page.
    		 * So this if is not executed.
    		 */
            if (!fileToDel.isEmpty()) {
                for (int i = 0; i < fileToDel.size(); i++) {
                    File file = fileToDel.get(i);
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            WAEclipseHelperMethods.deleteDirectory(file);
                        } else {
                            file.delete();
                        }
                    }
                }
            }
            fileToDel.clear();
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("adRolErrTitle"), message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), e);
            throw new ConfigurationException(message("adRolErrMsgBox1") + message("adRolErrMsgBox2"), message("adRolErrTitle"));
        }
        myModified = false;
    }

    @Override
    public void reset() {
        myModified = false;
    }

    @Override
    public void disposeUIResources() {

    }

    private final ColumnInfo<WindowsAzureRoleComponent, String> IMPORT = new ColumnInfo<WindowsAzureRoleComponent, String>(message("cmpntsImprt")) {
        public String valueOf(WindowsAzureRoleComponent component) {
            String result = "";
            if (component.getImportMethod() != null) {
                if (component.getImportMethod().equals(WindowsAzureRoleComponentImportMethod.auto)) {
                    String path = component.getImportPath();
//                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
//                    IWorkspaceRoot root = workspace.getRoot();
//                    if (path.contains("\\")) {
//                        path = path.substring(path.lastIndexOf('\\'), path.length());
//                        IProject proj = root.getProject(path);
//                        ProjExportType type = ProjectNatureHelper.getProjectNature(proj);
//                        result = type.toString();
//                    }
                } else {
                    result = component.getImportMethod().toString();
                }
            } else {
                result = WindowsAzureRoleComponentImportMethod.none.name();
            }
            return result;
        }
    };
    private final ColumnInfo<WindowsAzureRoleComponent, String> FROM = new ColumnInfo<WindowsAzureRoleComponent, String>(message("cmpntsFrm")) {
        public String valueOf(WindowsAzureRoleComponent component) {
            if (component.getImportPath() == null || component.getImportPath().isEmpty()) {
                return ".\\";
            } else {
                return component.getImportPath();
            }
        }
    };
    private final ColumnInfo<WindowsAzureRoleComponent, String> AS = new ColumnInfo<WindowsAzureRoleComponent, String>(message("cmpntsAs")) {
        public String valueOf(WindowsAzureRoleComponent component) {
            return component.getDeployName();
        }
    };
    private final ColumnInfo<WindowsAzureRoleComponent, WindowsAzureRoleComponentDeployMethod> DEPLOY =
            new ColumnInfo<WindowsAzureRoleComponent, WindowsAzureRoleComponentDeployMethod>(message("cmpntsDply")) {
                public WindowsAzureRoleComponentDeployMethod valueOf(WindowsAzureRoleComponent component) {
                    if (component.getDeployMethod() != null) {
                        return component.getDeployMethod();
                    } else {
                        return WindowsAzureRoleComponentDeployMethod.none;
                    }
                }
            };
    private final ColumnInfo<WindowsAzureRoleComponent, String> TO = new ColumnInfo<WindowsAzureRoleComponent, String>(message("cmpntsTo")) {
        public String valueOf(WindowsAzureRoleComponent component) {
            if (component.getDeployDir() == null || component.getDeployDir().isEmpty()) {
                return ".\\";
            } else {
                return component.getDeployDir();
            }
        }
    };

    private void createUIComponents() {
        tblComponents = new TableView<WindowsAzureRoleComponent>();
        tablePanel = ToolbarDecorator.createDecorator(tblComponents, null)
                .setAddAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        addComponent();
                    }
                }).setEditAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        editComponent();
                    }
                }).setRemoveAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        removeComponent();
                    }
                }).setEditActionUpdater(new AnActionButtonUpdater() {
                    @Override
                    public boolean isEnabled(AnActionEvent e) {
                        return tblComponents.getSelectedObject() != null;
                    }
                }).setRemoveActionUpdater(new AnActionButtonUpdater() {
                    @Override
                    public boolean isEnabled(AnActionEvent e) {
                        return tblComponents.getSelectedObject() != null;
                    }
                }).createPanel();
//        tablePanel.setPreferredSize(new Dimension(-1, 200));
    }

    /**
     * Listener method for add button which opens a dialog
     * to add a component.
     */
    private void addComponent() {
        ImportExportDialog dialog = new ImportExportDialog(project, waProjManager, windowsAzureRole, null);
        dialog.show();
        myModified = myModified || dialog.isModified();
    }

    private void editComponent() {
        /**
         * Listener method for edit button which opens a dialog
         * to edit a component.
         */
        try {
            WindowsAzureRoleComponent component = tblComponents.getSelectedObject();
                /*
                 * Checks component is part of a JDK,
    			 * server configuration then do not allow edit.
    			 */
            if (component.getIsPreconfigured()) {
                PluginUtil.displayErrorDialog(message("editNtAlwErTtl"), message("editNtAlwErMsg"));
            } else {
                ImportExportDialog dialog = new ImportExportDialog(project, waProjManager, windowsAzureRole, component);
                dialog.show();
                myModified = myModified || dialog.isModified();
//                        tblViewer.refresh(true);
//                        updateMoveButtons();
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("cmpntSetErrTtl"), message("cmpntEdtErrMsg"), e);
        }
    }

    /**
     * Listener method for remove button which
     * deletes the selected component.
     */
    private void removeComponent() {
        WindowsAzureRoleComponent component = tblComponents.getSelectedObject();
//        IWorkspace workspace = ResourcesPlugin.getWorkspace();
//        IWorkspaceRoot root = workspace.getRoot();
//        WindowsAzureRoleComponent component = listComponents.get(selIndex);
        try {
                /* First condition: Checks component is part of a JDK,
                 * server configuration
        		 * Second condition: For not showing error message
        		 * "Disable Server JDK Configuration"
        		 * while removing server application
        		 * when server or JDK  is already disabled.
        		 */
            if (component.getIsPreconfigured() && (!(component.getType().equals(message("typeSrvApp")) && windowsAzureRole.getServerName() == null))) {
                PluginUtil.displayErrorDialog(message("jdkDsblErrTtl"), message("jdkDsblErrMsg"));
            } else {
                int choice = Messages.showYesNoDialog(message("cmpntRmvMsg"), message("cmpntRmvTtl"), Messages.getQuestionIcon());
                if (choice == Messages.YES) {
                    String cmpntPath = String.format("%s%s%s%s%s",
                            PluginUtil.getModulePath(ModuleManager.getInstance(project).findModuleByName(waProjManager.getProjectName())),
                            File.separator, windowsAzureRole.getName(), message("approot"), component.getDeployName());
                    File file = new File(cmpntPath);
                    // Check import source is equal to approot
                    if (component.getImportPath().isEmpty() && file.exists()) {
                        int selected = Messages.showYesNoCancelDialog(message("cmpntSrcRmvMsg"), message("cmpntSrcRmvTtl"), Messages.getQuestionIcon());
                        switch (selected) {
                            case Messages.YES:
                                //yes
                                component.delete();
//                                tblViewer.refresh();
                                fileToDel.add(file);
                                break;
                            case Messages.NO:
                                //no
                                component.delete();
//                                tblViewer.refresh();
                                break;
                            case Messages.CANCEL:
                                //cancel
                                break;
                            default:
                                break;
                        }
                    } else {
                        component.delete();
//                        tblViewer.refresh();
                        fileToDel.add(file);
                    }
                    myModified = true;
                }
            }
//            if (tblComponents.getItemCount() == 0) {
//                // table is empty i.e. number of rows = 0
//                btnRemove.setEnabled(false);
//                btnEdit.setEnabled(false);
//            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("cmpntSetErrTtl"), message("cmpntRmvErrMsg"), e);
        }
    }
}
