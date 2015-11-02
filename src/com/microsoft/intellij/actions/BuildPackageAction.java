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
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.module.AzureModuleType;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.util.AntHelper;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoft.wacommon.utils.WACommonException;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;

import java.io.File;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class BuildPackageAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        // Get selected WA module
        final Module module = event.getData(LangDataKeys.MODULE);
        final String modulePath = PluginUtil.getModulePath(module);
        try {
            WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.load(new File(modulePath));

            if (waProjManager.getPackageType().equals(WindowsAzurePackageType.LOCAL)) {
                waProjManager.setPackageType(WindowsAzurePackageType.CLOUD);
            }
            try {
                String pluginInstLoc = String.format("%s%s%s", PathManager.getPluginsPath(), File.separator, AzurePlugin.PLUGIN_ID);
                String prefFile = String.format("%s%s%s", pluginInstLoc, File.separator, AzureBundle.message("prefFileName"));
                String prefSetUrl = PreferenceSetUtil.getSelectedPortalURL(PreferenceSetUtil.getSelectedPreferenceSetName(prefFile), prefFile);
                /*
                 * Don't check if URL is empty or null.
				 * As if it is then we remove "portalurl" attribute
				 * from package.xml.
				 */
                waProjManager.setPortalURL(prefSetUrl);
            } catch (WACommonException e1) {
                PluginUtil.displayErrorDialog(message("errTtl"), message("getPrefUrlErMsg"));
            }
            waProjManager.save();
            WindowsAzureProjectManager.load(new File(modulePath)); // to verify correctness

            AntHelper.runAntBuild(event.getDataContext(), module, AntHelper.createBuildPackageListener(module));
        } catch (final Exception e) {
            PluginUtil.displayErrorDialogInAWTAndLog(message("bldCldErrTtl"), String.format("%s %s", message("bldCldErrMsg"), module.getName()), e);
        }
    }

    public void update(AnActionEvent event) {
        final Module module = event.getData(LangDataKeys.MODULE);
        boolean value = false;
        if (module != null) {
            String modulePath = PluginUtil.getModulePath(module);
            try {
                WindowsAzureProjectManager projMngr = WindowsAzureProjectManager.load(new File(modulePath));
                value = WAEclipseHelperMethods.isFirstPackageWithAuto(projMngr);
            } catch(Exception ex) {
                value = false;
            }
        }
        event.getPresentation().setEnabled(module != null && AzureModuleType.AZURE_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE)) && value);
    }
}
