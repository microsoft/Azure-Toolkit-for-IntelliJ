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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class RolesConfigurable extends SearchableConfigurable.Parent.Abstract {
    private Module module;
    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;

    private AzureRolePanel panel;

    public RolesConfigurable(Module module, WindowsAzureProjectManager waProjManager, WindowsAzureRole windowsAzureRole, boolean isNew) {
        this.module = module;
        this.waProjManager = waProjManager;
        this.windowsAzureRole = windowsAzureRole;
        panel = new AzureRolePanel(module, waProjManager, windowsAzureRole, isNew);
    }

    @Override
    public boolean hasOwnContent() {
        return true;
    }

    @Override
    protected Configurable[] buildConfigurables() {
        Project project = module.getProject();
        return new Configurable[]{
                new CachingPanel(module, waProjManager, windowsAzureRole),
                new CertificatesPanel(module, waProjManager, windowsAzureRole),
                new ComponentsPanel(project, waProjManager, windowsAzureRole), new DebuggingPanel(project, waProjManager, windowsAzureRole),
                new RoleEndpointsPanel(module, waProjManager, windowsAzureRole),
                new EnvVarsPanel(module, waProjManager, windowsAzureRole),
                new LoadBalancingPanel(waProjManager, windowsAzureRole),
                new LocalStoragePanel(module, waProjManager, windowsAzureRole),
                new ServerConfigurationConfigurable(module, waProjManager, windowsAzureRole),
                new SSLOffloadingPanel(module, waProjManager, windowsAzureRole)};
    }

    @NotNull
    @Override
    public String getId() {
        return getDisplayName();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return message("cmhLblGeneral");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return panel.getHelpTopic();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return panel.createComponent();
    }

    @Override
    public boolean isModified() {
        return panel.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        panel.apply();
    }

    @Override
    public void reset() {
        panel.reset();
    }
}
