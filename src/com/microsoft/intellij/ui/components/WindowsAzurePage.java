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
package com.microsoft.intellij.ui.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.ConfigurationEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.ConfigurationEventListener;

import javax.swing.event.EventListenerList;

public abstract class WindowsAzurePage extends DialogWrapper {

    private static final EventListenerList LISTENERS = new EventListenerList();

    //	protected WindowsAzurePage(String pageName) {
//		super(pageName);
//	}
    protected WindowsAzurePage(Project project) {
        super(project);
    }

    public static void addConfigurationEventListener(ConfigurationEventListener listener) {
        LISTENERS.add(ConfigurationEventListener.class, listener);
    }

    public void removeConfigurationEventListener(ConfigurationEventListener listener) {
        LISTENERS.remove(ConfigurationEventListener.class, listener);
    }

    public void fireConfigurationEvent(ConfigurationEventArgs config) {
        Object[] list = LISTENERS.getListenerList();

        for (int i = 0; i < list.length; i += 2) {
            if (list[i] == ConfigurationEventListener.class) {
                ((ConfigurationEventListener) list[i + 1]).onConfigurationChanged(config);
            }
        }
    }
}
