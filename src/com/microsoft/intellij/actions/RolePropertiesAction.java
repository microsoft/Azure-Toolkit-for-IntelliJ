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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurableGroup;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoft.intellij.ui.azureroles.RoleConfigurablesGroup;
import com.microsoft.intellij.util.PluginUtil;

import java.io.File;

import static com.microsoft.intellij.AzurePlugin.log;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class RolePropertiesAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        // Get selected WA module
        final Module module = event.getData(LangDataKeys.MODULE);
        WindowsAzureProjectManager projMngr;
        try {
            VirtualFile vFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
            projMngr = WindowsAzureProjectManager.load(new File(PluginUtil.getModulePath(module)));
            WindowsAzureRole role = projMngr.roleFromPath(new File(vFile.getPath()));
            if (role != null) {
                ShowSettingsUtil.getInstance().showSettingsDialog(module.getProject(),
                        new ConfigurableGroup[]{new RoleConfigurablesGroup(module, projMngr, role, false)});
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            log(message("error"), e);
        }
    }

    public void update(AnActionEvent event) {
        final Module module = event.getData(LangDataKeys.MODULE);
        VirtualFile selectedFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        event.getPresentation().setVisible(module != null && PluginUtil.isRoleFolder(selectedFile, module));
    }
}
