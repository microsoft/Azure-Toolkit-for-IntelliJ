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
package com.microsoft.intellij.ui.util;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;

import java.util.Arrays;

import static com.microsoft.intellij.AzurePlugin.log;

public class JdkSrvConfigListener extends JdkSrvConfig {
    /**
     * Method decides whether to
     * show third party JDK names or not.
     *
     * @param status
     */
    public static String[] getThirdPartyJdkNames(Boolean status, String depJdkName) {
        if (status) {
            try {
                String[] thrdPrtJdkArr = WindowsAzureProjectManager.getThirdPartyJdkNames(cmpntFile, depJdkName);
                // check at least one element is present
                return thrdPrtJdkArr;
            } catch (WindowsAzureInvalidProjectOperationException e) {
                log(e.getMessage());
            }
        }
        return new String[]{};
    }

    public static String[] getServerList() {
        try {
            String[] servList = WindowsAzureProjectManager.getServerTemplateNames(cmpntFile);
            Arrays.sort(servList);
            return servList;
        } catch (WindowsAzureInvalidProjectOperationException e) {
            log(e.getMessage());
        }
        return new String[]{};
    }
}
