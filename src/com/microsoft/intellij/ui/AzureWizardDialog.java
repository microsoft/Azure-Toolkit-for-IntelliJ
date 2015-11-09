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

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.wizard.WizardDialog;
import com.interopbridges.tools.windowsazure.*;
import com.microsoft.intellij.module.AzureModuleBuilder;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.util.AppCmpntParam;
import com.microsoft.intellij.util.AppInsightsCustomEvent;
import com.microsoft.intellij.util.ParseXML;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.roleoperations.WizardUtilMethods;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class AzureWizardDialog extends WizardDialog<AzureWizardModel> {
    private static final int CACH_DFLTVAL = 30;
    private static final String DEBUG_PORT = "8090";
    private static final String HTTP_PRV_PORT = "8080";
    private static final String HTTP_PORT = "80";
    private static final String auto = "auto";
    private static final String dashAuto = "-auto";
    private static final String LAUNCH_FILE_PATH = File.separator
            + AzureBundle.message("pWizToolBuilder")
            + File.separator
            + AzureBundle.message("pWizLaunchFile");

    private AzureWizardModel model;
    //todo:
    private String errorTitle;
    private String errorMessage;

    public AzureWizardDialog(AzureWizardModel model) {
        super(model.getMyProject(), true, model);
        this.model = model;
    }

    @Override
    public void onWizardGoalAchieved() {
        super.onWizardGoalAchieved();
    }

    @Override
    protected Dimension getWindowPreferredSize() {
        return new Dimension(600, 400);
    }

    public void setCancelText(String text) {
        myModel.getCurrentNavigationState().CANCEL.setName(text);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        return myModel.doValidate();
    }

    protected boolean postponeValidation() {
        return false;
    }

    @Override
    protected void doOKAction() {
//        validateInput();
        if (isOKActionEnabled() && performFinish()) {
            AppInsightsCustomEvent.create(message("projCrtEvtName"), "");
            super.doOKAction();
        }
    }

    /**
     * This method gets called when wizard's finish button is clicked.
     *
     * @return True, if project gets created successfully; else false.
     */
    private boolean performFinish() {
        final String projName = model.getProjectName();
        final String projLocation = model.getProjectLocation();
        final boolean isDefault = model.isUseDefaultLocation();
        final Map<String, String> depParams = model.getDeployPageValues();
        final Map<String, Boolean> keyFtr = model.getKeyFeaturesValues();
        boolean retVal = true;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                doFinish(projName, projLocation, isDefault, depParams, keyFtr);
            }
        };
        /*
         * Check if third party JDK is selected
    	 * then license is accepted or not.
    	 */
        boolean tempAccepted = true;
        if (Boolean.valueOf(depParams.get("jdkThrdPartyChecked")) /*&& !WATabPage.isAccepted()*/) {
            tempAccepted = model.isLicenseAccepted();
        }
        if (tempAccepted) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, "Analyzing project structure...", true, model.getMyProject());
        } else {
            return false;
        }
        return retVal;
    }

    /**
     * Move the project structure to the location provided by user.
     * Also configure JDK, server, server application
     * and key features like session affinity, caching, debugging
     * if user wants to do so.
     *
     * @param projName     : Name of the project
     * @param projLocation : Location of the project
     * @param isDefault    : whether location of project is default
     * @param depMap       : stores configurations done on WATagPage
     * @param ftrMap       : stores configurations done on WAKeyFeaturesPage
     */
    private void doFinish(final String projName, final String projLocation, boolean isDefault, Map<String, String> depMap, Map<String, Boolean> ftrMap) {
        try {
            WindowsAzureRole role = model.getWaRole();
            //logic for handling deploy page components and their values.
            if (!depMap.isEmpty()) {
                File templateFile = new File(depMap.get("tempFile"));
                WizardUtilMethods.configureJDKServer(role, depMap);
                /*
                 * Handling adding server application
                 * without configuring server/JDK.
                 * Always add cloud attributes as
                 * approot directory is not created yet
                 * hence all applications are
                 * imported from outside of the Azure project
                 */
                if (!myModel.getAppsAsNames().isEmpty()) {
                    for (int i = 0; i < myModel.getAppsList().size(); i++) {
                        AppCmpntParam app = myModel.getAppsList().get(i);
                        if (!app.getImpAs().equalsIgnoreCase(AzureBundle.message("helloWorld"))) {
                            role.addServerApplication(app.getImpSrc(), app.getImpAs(), app.getImpMethod(), templateFile, true);
                        }
                    }
                }
            }

            /**
             * Handling for HelloWorld application in plug-in
             */
            if (!myModel.getAppsAsNames().contains(AzureBundle.message("helloWorld"))) {
                java.util.List<WindowsAzureRoleComponent> waCompList = model.getWaRole().getServerApplications();
                for (WindowsAzureRoleComponent waComp : waCompList) {
                    if (waComp.getDeployName().equalsIgnoreCase(AzureBundle.message("helloWorld")) && waComp.getImportPath().isEmpty()) {
                        waComp.delete();
                    }
                }
            }

            // Enable Key features
            // Session Affinity
            if (ftrMap.get("ssnAffChecked")) {
                WindowsAzureEndpoint httpEndPt = role.getEndpoint(AzureBundle.message("httpEp"));
                if (httpEndPt == null) {
                    /*
                     * server is not enabled.
            		 * hence create new endpoint
            		 * for session affinity.
            		 */
                    if (role.isValidEndpoint(AzureBundle.message("httpEp"),
                            WindowsAzureEndpointType.Input,
                            HTTP_PRV_PORT, HTTP_PORT)) {
                        httpEndPt = role.addEndpoint(AzureBundle.message("httpEp"),
                                WindowsAzureEndpointType.Input,
                                HTTP_PRV_PORT, HTTP_PORT);
                    }
                }
                if (httpEndPt != null) {
                    role.setSessionAffinityInputEndpoint(httpEndPt);
                }
            }

            // Caching
            if (ftrMap.get("cacheChecked")) {
                role.setCacheMemoryPercent(CACH_DFLTVAL);
                role.setCacheStorageAccountName(dashAuto);
            }

            // Remote Debugging
            if (ftrMap.get("debugChecked")) {
                if (role.isValidEndpoint(AzureBundle.message("dbgEp"), WindowsAzureEndpointType.Input, DEBUG_PORT, DEBUG_PORT)) {
                    WindowsAzureEndpoint dbgEndPt = role.addEndpoint(AzureBundle.message("dbgEp"), WindowsAzureEndpointType.Input, DEBUG_PORT, DEBUG_PORT);
                    if (dbgEndPt != null) {
                        role.setDebuggingEndpoint(dbgEndPt);
                        role.setStartSuspended(false);
                    }
                }
            }

            model.getWaProjMgr().save();
            WindowsAzureProjectManager.moveProjFromTemp(projName, projLocation);
            String launchFilePath = projLocation + File.separator + projName + LAUNCH_FILE_PATH;
            ParseXML.setProjectNameinLaunch(launchFilePath,
                    AzureBundle.message("pWizWinAzureProj"), projName);
            final String imlName = String.format("%s%s%s%s%s", projLocation, File.separator, projName, File.separator,
                    projName + ModuleFileType.DOT_DEFAULT_EXTENSION);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication()
                            .runWriteAction(new Runnable() {
                                @Override
                                public void run() {
                                    ModuleBuilder moduleBuilder = new AzureModuleBuilder();
                                    moduleBuilder.setModuleFilePath(imlName);
                                    moduleBuilder.setContentEntryPath(String.format("%s%s%s", projLocation, File.separator, projName));
                                    Module module = moduleBuilder.commitModule(myModel.getMyProject(), null);

//                                    Module module = ModuleManager.getInstance(myModel.getMyProject()).newModule(imlName, ModuleTypeId.JAVA_MODULE);

//                                    ((ModuleRootManagerImpl)ModuleRootManagerImpl.getInstance(module)).getRootModel().addContentEntry("file://$MODULE_DIR$");
//                                    ((ModuleRootManagerImpl)ModuleRootManagerImpl.getInstance(module)).getRootModel().addContentEntry(String.format("%s%s%s", projLocation, File.separator, projName));
                                }
                            });
                }
            });
        } catch (Exception e) {
            PluginUtil.displayErrorDialogAndLog(message("pWizErrTitle"), message("pWizErrMsg"), e);
        }
    }
}
