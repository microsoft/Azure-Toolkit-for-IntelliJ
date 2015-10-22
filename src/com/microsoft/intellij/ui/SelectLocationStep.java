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

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.microsoft.intellij.ui.components.AzureWizardStep;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.ui.util.UIUtils;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class SelectLocationStep extends AzureWizardStep {
    private JPanel rootPanel;
    private JTextField projectName;
    private JCheckBox useDefaultLocation;
    private TextFieldWithBrowseButton projectLocation;
    private JLabel locationLabel;
    private final AzureWizardModel myModel;

    public SelectLocationStep(final String title, final AzureWizardModel model) {
        super(title, AzureBundle.message("wizPageDesc"));
        myModel = model;
        init();
    }

    public void init() {
        projectName.requestFocusInWindow();
        projectLocation.addActionListener(UIUtils.createFileChooserListener(projectLocation, myModel.getMyProject(),
                FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        projectLocation.setText(myModel.getMyProject().getBasePath());
        useDefaultLocation.addItemListener(createDefaultLocationListener());
        useDefaultLocation.setSelected(true);
    }

    @Override
    public JComponent prepare(final WizardNavigationState state) {
        rootPanel.revalidate();
        return rootPanel;
    }

    private ItemListener createDefaultLocationListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (useDefaultLocation.isSelected()) {
                    locationLabel.setEnabled(false);
                    projectLocation.setText(myModel.getMyProject().getBasePath());
                    projectLocation.setEnabled(false);
//                    buttonBrowse.setEnabled(false);
//                    setErrorMessage(null);
//                    setPageComplete(true);
                } else {
                    locationLabel.setEnabled(true);
                    projectLocation.setEnabled(true);
                    projectLocation.setText(""); //$NON-NLS-1$
//                    buttonBrowse.setEnabled(true);
//                    setDescription(message("wizPageEnterLoc"));
//                    setPageComplete(false);
                }
            }
        };
    }

    @Override
    public WizardStep onNext(final AzureWizardModel model) {
        if (doValidate() == null) {
            return super.onNext(model);
        } else {
            return this;
        }
    }

    @Override
    public ValidationInfo doValidate() {
        String projName = projectName.getText();
        if (projName.isEmpty()) {
            myModel.getCurrentNavigationState().NEXT.setEnabled(false);
            return null;
        } /*else if (!projNameStatus.isOK()) {
            setErrorMessage(projNameStatus.getMessage());
            setPageComplete(false);
        } */ else if (ModuleManager.getInstance(myModel.getMyProject()).findModuleByName(projName) != null) {
            return createValidationInfo(message("wizPageErrMsg1"), projectName);
        } else if (!(new File(projectLocation.getText()).exists())) {
            return createValidationInfo(message("wizPageErrPath"), projectLocation);
        } else {
            myModel.getCurrentNavigationState().NEXT.setEnabled(true);
            return null;
        }
    }

    ValidationInfo createValidationInfo(String message, JComponent component) {
        myModel.getCurrentNavigationState().NEXT.setEnabled(false);
        return new ValidationInfo(message, component);
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return projectName;
    }

    public String getProjectName() {
        return projectName.getText();
    }

    public String getProjectLocation() {
        return projectLocation.getText();
    }

    public boolean isUseDefaultLocation() {
        return useDefaultLocation.isSelected();
    }
}
