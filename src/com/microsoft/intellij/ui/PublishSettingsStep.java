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
package com.microsoft.intellij.ui;

import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.microsoft.intellij.ui.components.AzureWizardStep;

import javax.swing.*;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class PublishSettingsStep extends AzureWizardStep {

    private JdkServerPanel jdkServerPanel;
    private final AzureWizardModel myModel;

    public PublishSettingsStep(final String title, final AzureWizardModel model) {
        super(title, message("dplPageJdkMsg"));
        myModel = model;
        jdkServerPanel = new JdkServerPanel(model.getMyProject(), model.getWaRole(), null);
    }

    @Override
    public WizardStep onNext(final AzureWizardModel model) {
        int currentTab = jdkServerPanel.getSelectedIndex();
        if (currentTab == 2) {
            return super.onNext(model);
        } else {
            jdkServerPanel.setSelectedIndex(++currentTab);
            return this;
        }
    }

    @Override
    public WizardStep onPrevious(final AzureWizardModel model) {
        int currentTab = jdkServerPanel.getSelectedIndex();
        if (currentTab == 0) {
            return super.onPrevious(model);
        } else {
            jdkServerPanel.setSelectedIndex(--currentTab);
            return this;
        }
    }

    @Override
    public JComponent prepare(final WizardNavigationState state) {
//        rootPanel.revalidate();
        state.FINISH.setEnabled(true);
        return jdkServerPanel.getPanel();
    }

    @Override
    public ValidationInfo doValidate() {
        return jdkServerPanel.doValidate();
    }

    public JdkServerPanel getJdkServerPanel() {
        return jdkServerPanel;
    }
}
