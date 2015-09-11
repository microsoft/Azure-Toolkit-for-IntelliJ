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
package com.microsoft.intellij.util;

import com.intellij.lang.ant.config.*;
import com.intellij.lang.ant.config.execution.ExecutionHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.actions.DeployAction;
import com.microsoftopentechnologies.azurecommons.deploy.model.AutoUpldCmpnts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class AntHelper {

    public static void runAntBuild(final DataContext dataContext, final Module module, final AntBuildListener antBuildListener) {
        final VirtualFile packageXmlFile = LocalFileSystem.getInstance().findFileByPath(PluginUtil.getModulePath(module) + File.separator + message("resCLPkgXML"));
        final AntConfiguration antConfiguration = AntConfiguration.getInstance(module.getProject());
        ApplicationManager.getApplication().invokeAndWait(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final AntBuildFile buildFile;
                            boolean notInAnt = true;
                            AntBuildFile buildFileTemp = null;
                            for (AntBuildFile antBuildFile : antConfiguration.getBuildFiles()) {
                                if (antBuildFile.getPresentableUrl().equals(packageXmlFile.getPresentableUrl())) {
                                    buildFileTemp = antBuildFile;
                                    notInAnt = false;
                                    break;
                                }
                            }
                            buildFile = notInAnt ? antConfiguration.addBuildFile(packageXmlFile) : buildFileTemp;
                            final String[] targets = ArrayUtil.EMPTY_STRING_ARRAY; // default target
                            ExecutionHandler.runBuild((AntBuildFileBase) buildFile, targets, null, dataContext, new ArrayList(), antBuildListener);
                            if (notInAnt) {
                                antConfiguration.removeBuildFile(buildFile);
                            }
                        } catch (AntNoFileException e) {
                            throw new RuntimeException(message("bldErrMsg"), e);
                        }
                    }
                }, ModalityState.defaultModalityState());
    }

    public static AntBuildListener createDeployListener(final Module myModule, final List<AutoUpldCmpnts> mdfdCmpntList, final List<String> roleMdfdCache) {
        return new AntBuildListener() {
            @Override
            public void buildFinished(int state, int errorCount) {
                if (state == AntBuildListener.FINISHED_SUCCESSFULLY) {
                    try {
                        WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.load(new File(PluginUtil.getModulePath(myModule)));
                    /*
                     * Build job is completed.
					 * If component's url settings done before build then,
					 * replace with auto.
					*/
                        if (mdfdCmpntList.size() > 0) {
                            waProjManager = DeployAction.addAutoCloudUrl(waProjManager, mdfdCmpntList);
                            waProjManager.save();
                        }
                    /*
                     * If cache's storage account name and key,
					 * is changed before build then replace with auto.
					*/
                        if (roleMdfdCache.size() > 0) {
                            waProjManager = DeployAction.addAutoSettingsForCache(waProjManager, roleMdfdCache);
                            waProjManager.save();
                        }
                    } catch (WindowsAzureInvalidProjectOperationException e) {
                        PluginUtil.displayErrorDialogInAWTAndLog(message("error"), message("autoUploadEr"), e);
                    }
                    DeployAction.WindowsAzureDeploymentTask task = new DeployAction.WindowsAzureDeploymentTask(myModule);
                    task.queue();
                }
            }

        };
    }

    public static AntBuildListener createBuildPackageListener(final Module module) {
        return new AntBuildListener() {
            @Override
            public void buildFinished(int state, int errorCount) {
                if (state == AntBuildListener.FINISHED_SUCCESSFULLY) {
                    String dplyFolderPath = "";
                    try {
                        WindowsAzureProjectManager waProjMngr = WindowsAzureProjectManager.load(new File(PluginUtil.getModulePath(module)));

                        dplyFolderPath = WAHelper.getDeployFolderPath(waProjMngr, module);
                        String bldFlFilePath = String.format("%s%s%s", dplyFolderPath, File.separator, message("bldErFileName"));
                        File buildFailFile = new File(bldFlFilePath);
                        File deployFile = new File(dplyFolderPath);

                        if (deployFile.exists() && deployFile.isDirectory() && deployFile.listFiles().length > 0 && !buildFailFile.exists()) {
                            String[] cmd = {"explorer.exe", "\"" + dplyFolderPath + "\""};
                            new ProcessBuilder(cmd).start();
                        } else {
                            PluginUtil.displayErrorDialog(message("bldErrTtl"), message("bldErrMsg"));
                        }
                        waProjMngr.save();
                    } catch (IOException e) {
                        AzurePlugin.log(String.format("%s %s", message("dplyFldErrMsg"), dplyFolderPath), e);
                    } catch (Exception e) {
                        PluginUtil.displayErrorDialogInAWTAndLog(message("bldErrTtl"), message("bldErrMsg"), e);
                    }
                } else if (state == AntBuildListener.FAILED_TO_RUN) {
                    PluginUtil.displayErrorDialog(message("bldErrTtl"), message("bldErrMsg"));
                }
            }
        };
    }

    public static AntBuildListener createRunInEmulatorListener(final Module module, final WindowsAzureProjectManager waProjMgr) {
        return new AntBuildListener() {
            @Override
            public void buildFinished(int state, int errorCount) {
                if (state == AntBuildListener.FINISHED_SUCCESSFULLY) {
                    try {
                        waProjMgr.deployToEmulator();
                    } catch (WindowsAzureInvalidProjectOperationException e) {
                        String errorTitle = String.format("%s%s%s", message("waEmulator"), " ", message("runEmltrErrTtl"));
                        String errorMessage = String.format("%s %s%s%s", message("runEmltrErrMsg"), module.getName(), " in ", message("waEmulator"));
                        PluginUtil.displayErrorDialogInAWTAndLog(errorTitle, errorMessage, e);
                    }
                } else if (state == AntBuildListener.FAILED_TO_RUN) {
                    PluginUtil.displayErrorDialog(message("bldErrTtl"), message("bldErrMsg"));
                }
            }
        };
    }
}
