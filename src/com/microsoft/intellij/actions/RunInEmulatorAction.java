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
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.module.AzureModuleType;
import com.microsoft.intellij.util.AntHelper;
import com.microsoft.intellij.util.PluginUtil;

import java.io.File;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class RunInEmulatorAction extends AnAction {

    public void actionPerformed(final AnActionEvent event) {
        final Module module = event.getData(LangDataKeys.MODULE);
        if (module == null || !AzureModuleType.AZURE_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE))) {
            return;
        }
        try {
            final String modulePath = PluginUtil.getModulePath(module);
            WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.load(new File(modulePath));

            if (waProjManager.getPackageType().equals(WindowsAzurePackageType.CLOUD)) {
                waProjManager.setPackageType(WindowsAzurePackageType.LOCAL);
            }
            waProjManager.save();
            try {
                final WindowsAzureProjectManager waProjMgr = WindowsAzureProjectManager.load(new File(modulePath));

                AntHelper.runAntBuild(event.getDataContext(), module, AntHelper.createRunInEmulatorListener(module, waProjMgr));
            } catch (WindowsAzureInvalidProjectOperationException e) {
                String errorTitle = String.format("%s%s%s", message("waEmulator"), " ", message("runEmltrErrTtl"));
                String errorMessage = String.format("%s %s%s%s", message("runEmltrErrMsg"), module.getName(), " in ", message("waEmulator"));
                PluginUtil.displayErrorDialogInAWTAndLog(errorTitle, errorMessage, e);
            } catch (Exception ex) {
                PluginUtil.displayErrorDialogInAWTAndLog(message("bldErrTtl"), message("bldErrMsg"), ex);
            }
        } catch (Exception e) {
            String errorTitle = String.format("%s%s%s", message("waEmulator"), " ", message("runEmltrErrTtl"));
            String errorMessage = String.format("%s %s%s%s", message("runEmltrErrMsg"), module.getName(), " in ", message("waEmulator"));
            PluginUtil.displayErrorDialogAndLog(errorTitle, errorMessage, e);
        }
    }

    public void update(AnActionEvent event) {
        final Module module = event.getData(LangDataKeys.MODULE);
        event.getPresentation().setVisible(AzurePlugin.IS_WINDOWS);
        event.getPresentation().setEnabled(module != null && AzureModuleType.AZURE_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE)));
    }
}