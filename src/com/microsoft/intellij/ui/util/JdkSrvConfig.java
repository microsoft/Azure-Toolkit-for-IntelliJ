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


import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.util.WAHelper;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

public class JdkSrvConfig {
    public static final String NONE_TXT = "(none)";
    public static final String AUTO_TXT = "(auto)";
    protected static File cmpntFile = new File(WAHelper.getTemplateFile(AzureBundle.message("cmpntFileName")));
    public static final String JDK_TXT = "JDK";
    public static final String SRV_TXT = "SERVER";
    public static String[] accNames = getStrgAccoNamesAsPerTab(null, false);

    /**
     * Method initializes storage account list
     * and populates in combo box.
     *
     * @param valToSet
     * @param combo
     * @param tabControl
     * @param needAuto   If its caching page, we need auto even though
     *                   tabControl is null
     * @return
     */
    public static JComboBox populateStrgAccComboBox(String valToSet, JComboBox combo, String tabControl, boolean needAuto) {
        accNames = getStrgAccoNamesAsPerTab(tabControl, needAuto);
        combo.setModel(new DefaultComboBoxModel(accNames));
        /*
         * If value to set is not present
		 * then set it to none.
		 */
        if (valToSet == null || valToSet.isEmpty() || !Arrays.asList(accNames).contains(valToSet)) {
            combo.setSelectedItem(accNames[0]);
        } else {
            combo.setSelectedItem(valToSet);
        }
        return combo;
    }

    /**
     * Method returns blob endpoint URL from storage registry
     * according to account name selected in combo box.
     *
     * @param combo
     * @return
     */
    public static String getBlobEndpointUrl(JComboBox combo) {
        String url = "";
        int strgAccIndex = combo.getSelectedIndex();
        if (strgAccIndex > 0 && !combo.getSelectedItem().toString().isEmpty()) {
            url = StorageAccountRegistry.getStrgList().get(strgAccIndex - 1).getStrgUrl();
        }
        return url;
    }

    /**
     * Method prepares storage account name list
     * as per current tab.
     * If no tab = null, then page is other than serverconfiguration
     * then add (none)
     * If tab is JDK or server, then add (none) or (auto)
     * as per selection of radio buttons.
     * If auto upload or third party JDK radio button selected --> Add (auto)
     * if not --> Add (none)
     *
     * @param tabControl
     * @param needAuto   If its caching page, we need auto even though
     *                   tabControl is null
     * @return
     */
    public static String[] getStrgAccoNamesAsPerTab(String tabControl, boolean needAuto) {
//		needAuto = (tabControl == null) ? needAuto : ((tabControl.equals(JDK_TXT)) ? autoDlRdCldBtn.getSelection()
//				|| thrdPrtJdkBtn.getSelection() : autoDlRdCldBtnSrv.getSelection());
        if (tabControl == null) {
            if (needAuto) {
                accNames = StorageRegistryUtilMethods.getStorageAccountNames(true);
            } else {
                accNames = StorageRegistryUtilMethods.getStorageAccountNames(false);
            }
        } else {
            // For JDK
            if (JDK_TXT.equals(tabControl)) {
                /*
				 * (auto) storage account is needed for
				 * auto upload as well as third party JDK
				 */
//                accNames = StorageRegistryUtilMethods.
//                        getStorageAccountNames(autoDlRdCldBtn.getSelection()
//                                || thrdPrtJdkBtn.getSelection());
                accNames = StorageRegistryUtilMethods.getStorageAccountNames(true);
            } else if (SRV_TXT.equals(tabControl)) {
//                accNames = StorageRegistryUtilMethods.getStorageAccountNames(autoDlRdCldBtnSrv.getSelection());
                accNames = StorageRegistryUtilMethods.getStorageAccountNames(true);
            }
        }
        return accNames;
    }
}
