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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.microsoft.intellij.forms.WebSiteDeployForm;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.IDEHelper;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;

public class AzureWebDeployAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        WebSiteDeployForm form = new WebSiteDeployForm(e.getProject());
        form.show();

        if (form.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            try {
                form.deploy();
            } catch (AzureCmdException ex) {
                DefaultLoader.getUIHelper().showException("An error occurred while attempting to deploy web app.", ex,
                        "Azure Toolkit for IntelliJ - Error Deploying Web App", false, true);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();

        if (project != null) {
            IDEHelper.ProjectDescriptor projectDescriptor = new IDEHelper.ProjectDescriptor(project.getName(),
                    project.getBasePath() == null ? "" : project.getBasePath());

            try {
                for (IDEHelper.ArtifactDescriptor descriptor : DefaultLoader.getIdeHelper().getArtifacts(projectDescriptor)) {
                    if ("war".equals(descriptor.getArtifactType())) {
                        e.getPresentation().setEnabled(true);
                        return;
                    }
                }
            } catch (AzureCmdException ignored) {
            }
        }

        e.getPresentation().setEnabled(false);
    }
}