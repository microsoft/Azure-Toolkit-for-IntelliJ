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
package com.microsoft.intellij.actions;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.PlatformUtils;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.ui.AzureWizardDialog;
import com.microsoft.intellij.ui.AzureWizardModel;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.util.PluginUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static com.microsoft.intellij.AzurePlugin.log;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class PackageAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT);
        if (checkSdk()) {
            AzureWizardModel model = new AzureWizardModel(project, createWaProjMgr());
            AzureWizardDialog wizard = new AzureWizardDialog(model);

            final String title = AzureBundle.message("pWizWindowTitle");
            wizard.setTitle(title);
            wizard.show();
            if (wizard.isOK()) {
//            FileContentUtil.reparseFiles();
            }
        }
    }

    private boolean checkSdk() {
        String sdkPath = null;
        if (AzurePlugin.IS_WINDOWS) {
            try {
                sdkPath = WindowsAzureProjectManager.getLatestAzureSdkDir();
            } catch (IOException e) {
                log(message("error"), e);
            }
            try {
                if (sdkPath == null) {
                    int choice = Messages.showOkCancelDialog(message("sdkInsErrMsg"), message("sdkInsErrTtl"), Messages.getQuestionIcon());
                    if (choice == Messages.OK) {
                        Desktop.getDesktop().browse(URI.create(message("sdkInsUrl")));
                    }
                    return false;
                }
            } catch (Exception ex) {
                // only logging the error in log file not showing anything to
                // end user
                log(message("error"), ex);
                return false;
            }
        } else {
            log("Not Windows OS, skipping getSDK");
        }
        return true;
    }

    private WindowsAzureProjectManager createWaProjMgr() {
        WindowsAzureProjectManager waProjMgr = null;
        try {
            String zipFile = String.format("%s%s%s%s%s", PathManager.getPluginsPath(), File.separator, AzurePlugin.PLUGIN_ID, File.separator,
                    message("starterKitFileName"));

            //Extract the WAStarterKitForJava.zip to temp dir
            waProjMgr = WindowsAzureProjectManager.create(zipFile);
            //	By deafult - disabling remote access
            //  when creating new project
            waProjMgr.setRemoteAccessAllRoles(false);
            waProjMgr.setClassPathInPackage("azure.lib.dir", PluginUtil.getAzureLibLocation());
            WindowsAzureRole waRole = waProjMgr.getRoles().get(0);
            // remove http endpoint
            waRole.getEndpoint(AzureBundle.message("httpEp")).delete();
        } catch (IOException e) {
            PluginUtil.displayErrorDialogAndLog(AzureBundle.message("pWizErrTitle"), AzureBundle.message("pWizErrMsg"), e);
        } catch (Exception e) {
            String errorTitle = AzureBundle.message("adRolErrTitle");
            String errorMessage = AzureBundle.message("pWizErrMsgBox1") + AzureBundle.message("pWizErrMsgBox2");
            PluginUtil.displayErrorDialogAndLog(errorTitle, errorMessage, e);
        }
        return waProjMgr;
    }

    public void update(AnActionEvent event) {
        final Module module = event.getData(LangDataKeys.MODULE);
        event.getPresentation().setVisible(PlatformUtils.isIdeaUltimate() &&
                module != null && ModuleTypeId.JAVA_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE))
                || ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace()));
    }
}
