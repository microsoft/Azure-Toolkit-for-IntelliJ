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
import com.microsoft.intellij.ui.components.AzureWizardStep;
import org.jdesktop.swingx.JXHyperlink;

import javax.swing.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class KeyFeaturesStep extends AzureWizardStep {
    private JPanel rootPanel;
    private JCheckBox sessionAffinityCheckBox;
    private JCheckBox cachingCheckBox;
    private JCheckBox debuggingCheckBox;
    private JXHyperlink ssnAffLnk;
    private JXHyperlink cachLnk;
    private JXHyperlink debugLnk;

    public KeyFeaturesStep(final String title) {
        super(title, message("keyFtrPgMsg"));
        init();
    }

    public void init() {
        initLink(ssnAffLnk, message("ssnAffLnk"));
        initLink(cachLnk, message("cachLnk"));
        initLink(debugLnk, message("debugLnk"));
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
        rootPanel.revalidate();
        return rootPanel;
    }

    private void initLink(JXHyperlink link, String linkText) {
        link.setURI(URI.create(linkText));
        link.setText(message("lblLearnMore"));
    }

    public Map<String, Boolean> getValues() {
        Map<String, Boolean> values = new HashMap<String, Boolean>();
        values.put("ssnAffChecked", sessionAffinityCheckBox.isSelected());
        values.put("cacheChecked", cachingCheckBox.isSelected());
        values.put("debugChecked", debuggingCheckBox.isSelected());
        return values;
    }

    @Override
    public ValidationInfo doValidate() {
        return null;
    }
}
