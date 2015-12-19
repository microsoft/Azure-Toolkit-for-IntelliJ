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
package com.microsoft.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.deploy.DeploymentManager;
import com.microsoft.wacommon.utils.WACommonException;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventListener;
import com.microsoftopentechnologies.azurecommons.exception.RestAPIException;
import org.jetbrains.annotations.NotNull;

import static com.microsoft.intellij.AzurePlugin.log;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class WindowsAzureUndeploymentTask extends Task.Backgroundable {
    private String serviceName;
    private String deploymentName;
    private String deploymentState;

    public WindowsAzureUndeploymentTask(Project project, String serviceName, String deploymentName, String deploymentState) {
        super(project, message("deployingToAzure"), true, Backgroundable.DEAF);
        this.serviceName = serviceName;
        this.deploymentName = deploymentName;
        this.deploymentState = deploymentState;
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
        AzurePlugin.removeUnNecessaryListener();
        DeploymentEventListener undeployListnr = new DeploymentEventListener() {
            @Override
            public void onDeploymentStep(DeploymentEventArgs args) {
                indicator.setFraction(indicator.getFraction() + args.getDeployCompleteness() / 100.0);
                indicator.setText(message("undeployWizTitle"));
                indicator.setText2(args.toString());
            }
        };
        AzurePlugin.addDeploymentEventListener(undeployListnr);
        AzurePlugin.depEveList.add(undeployListnr);

        try {
            DeploymentManager.getInstance().undeploy(serviceName, deploymentName, deploymentState);
        } catch (RestAPIException e) {
            log(message("error"), e);
        } catch (InterruptedException e) {
            log(message("error"), e);
        } catch (WACommonException e) {
            log(message("error"), e);
            e.printStackTrace();
        }
    }
}
