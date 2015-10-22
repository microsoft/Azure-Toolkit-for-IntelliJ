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
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoft.intellij.util.WAHelper;
import com.microsoftopentechnologies.azurecommons.wacommonutil.FileUtil;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

/**
 * This class resets the Azure Emulator.
 */
public class ResetEmulatorAction extends AnAction {
    private static final int BUFF_SIZE = 1024;

    public void actionPerformed(AnActionEvent event) {
        try {
            String strKitLoc = WAHelper.getTemplateFile(message("pWizStarterKit"));
            StringBuilder output = new StringBuilder();
            ZipFile zipFile = new ZipFile(strKitLoc);

            // copy elevate.vbs to temp location
            String tmpPath = System.getProperty("java.io.tmpdir");
            FileUtil.copyFileFromZip(new File(strKitLoc), "%proj%/.templates/emulatorTools/.elevate.vbs",
                    new File(String.format("%s%s%s", tmpPath, File.separator, ".elevate.vbs")));

            @SuppressWarnings("rawtypes")
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.getName().toLowerCase().indexOf(message("rstEmCmd")) != -1) {
                    InputStream in = zipFile.getInputStream(entry);
                    Reader reader = new InputStreamReader(in);
                    char[] buf = new char[BUFF_SIZE];
                    int length = reader.read(buf, 0, buf.length);
                    while (length > 0) {
                        output.append(buf, 0, length);
                        length = reader.read(buf, 0, buf.length);
                    }
                    break;
                }
            }
            zipFile.close();
            WindowsAzureProjectManager.resetEmulator(output.toString());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(message("rstEmltrErrTtl"), message("rstEmuErrMsg"), e);
        } catch (IOException e1) {
            AzurePlugin.log(message("ioErrMsg"), e1);
        }
    }

    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(AzurePlugin.IS_WINDOWS);
    }
}