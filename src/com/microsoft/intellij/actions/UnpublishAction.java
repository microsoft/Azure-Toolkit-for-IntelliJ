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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.intellij.module.AzureModuleType;
import com.microsoft.intellij.ui.UndeployWizardDialog;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoft.tasks.WindowsAzureUndeploymentTask;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse.Deployment;


public class UnpublishAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(LangDataKeys.PROJECT);
        UndeployWizardDialog deployDialog = new UndeployWizardDialog(project);
        deployDialog.show();
        if (deployDialog.isOK()) {
            Deployment deployment = deployDialog.getDeployment();
            WindowsAzureUndeploymentTask undeploymentTask =
                    new WindowsAzureUndeploymentTask(project, deployDialog.getServiceName(), deployment.getName(), deployment.getDeploymentSlot().toString());
            undeploymentTask.queue();
        }
    }

    public void update(AnActionEvent event) {
        final Module module = event.getData(LangDataKeys.MODULE);
        VirtualFile selectedFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        event.getPresentation().setEnabledAndVisible(module != null && AzureModuleType.AZURE_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE))
                && PluginUtil.isModuleRoot(selectedFile, module) || ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace()));
    }
}
