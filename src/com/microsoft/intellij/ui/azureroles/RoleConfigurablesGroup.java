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
import com.intellij.openapi.options.ConfigurableGroup;
import com.intellij.openapi.options.SearchableConfigurable;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoleConfigurablesGroup extends SearchableConfigurable.Parent.Abstract
        implements SearchableConfigurable, ConfigurableGroup, Configurable.NoScroll {
    private static final String MY_GROUP_ID = "configurable.group.azure";
    private final Configurable[] rolesConfigurable;

    public RoleConfigurablesGroup(Module module, WindowsAzureProjectManager waProjMgr, WindowsAzureRole role, boolean isNew) {
        rolesConfigurable = new Configurable[]{new RolesConfigurable(module, waProjMgr, role, isNew)};
    }

    @Override
    protected Configurable[] buildConfigurables() {
        return rolesConfigurable;
    }

    @NotNull
    @Override
    public String getId() {
        return MY_GROUP_ID;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "azure";
    }

    @Override
    public String getShortName() {
        return getDisplayName();
    }

    @Override
    public boolean isModified() {
        return rolesConfigurable[0].isModified();
    }
}

