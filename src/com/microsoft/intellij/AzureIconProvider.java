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
package com.microsoft.intellij;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.microsoft.intellij.module.AzureModuleType;
import com.microsoft.intellij.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AzureIconProvider extends IconProvider implements DumbAware {
    public static final Icon WorkerRole = IconLoader.getIcon("/icons/RoleFolder.gif");

    @Override
    public Icon getIcon(@NotNull final PsiElement element, final int flags) {
        if (element instanceof PsiDirectory) {
            final PsiDirectory psiDirectory = (PsiDirectory) element;
            final VirtualFile vFile = psiDirectory.getVirtualFile();
            final Project project = psiDirectory.getProject();
            Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vFile);
            if (module != null && AzureModuleType.AZURE_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE)) && PluginUtil.isRoleFolder(vFile, module)) {
                return WorkerRole;
            }
        }
        return null;
    }
}