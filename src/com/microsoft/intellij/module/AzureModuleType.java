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
package com.microsoft.intellij.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AzureModuleType extends ModuleType<AzureModuleBuilder> {
    @NonNls
    public static final String AZURE_MODULE = "AZURE_MODULE";
    public static final String MODULE_NAME = "Azure Module";
    public static final Icon AZURE_MODULE_ICON = IconLoader.getIcon("/icons/ProjectFolder.png");
    private static AzureModuleType MODULE_TYPE = new AzureModuleType();

    public static ModuleType getModuleType() {
        return ModuleTypeManager.getInstance().findByID(AZURE_MODULE);
    }

    public AzureModuleType() {
        this(AZURE_MODULE);
    }

    protected AzureModuleType(@NonNls String id) {
        super(id);
    }

    public static ModuleType getInstance() {
        return MODULE_TYPE;
    }

    @NotNull
    @Override
    public AzureModuleBuilder createModuleBuilder() {
        return new AzureModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return ProjectBundle.message("module.type.java.description");
    }

    @Override
    public Icon getBigIcon() {
        return getAzureModuleIcon();
    }

    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return getAzureModuleNodeIconClosed();
    }

    private static Icon getAzureModuleIcon() {
        return AZURE_MODULE_ICON;
    }

    private static Icon getAzureModuleNodeIconClosed() {
        return AZURE_MODULE_ICON;
    }

    public static boolean isAzureModule(@NotNull Module module) {
        return AZURE_MODULE.equals(ModuleType.get(module).getId());
    }
}
