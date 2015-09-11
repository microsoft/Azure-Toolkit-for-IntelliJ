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

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TitlePanel;
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoft.intellij.wizards.WizardCacheManager;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class NewHostedServiceDialog extends DialogWrapper {
    private final static String HOSTED_SERVICE_NAME_PATTERN = "^[a-z0-9]+$";

    private JPanel contentPane;
    private JTextField hostedServiceTxt;
    private JComboBox locationComb;
    private JTextField descriptionTxt;

    private String defaultLocation;

    public NewHostedServiceDialog() {
        super(true);
        setTitle(message("cldSrv"));
        init();
    }

    protected void init() {
        populateLocations();
        super.init();
    }

    private void populateLocations() {
        List<LocationsListResponse.Location> items = WizardCacheManager.getLocation();
        locationComb.removeAllItems();

        for (LocationsListResponse.Location location : items) {
            locationComb.addItem(location.getName());
        }
        /*
         * default location will exist if the user has
		 * created a storage account before creating the hosted service
		 */
        if (defaultLocation != null) {
            locationComb.setSelectedItem(defaultLocation);
        }
    }

    @Override
    protected void doOKAction() {
        String hostedServiceNameToCreate = hostedServiceTxt.getText();
        String hostedServiceLocation = (String) locationComb.getSelectedItem();
        boolean isNameAvailable;
        try {
            isNameAvailable = WizardCacheManager.isHostedServiceNameAvailable(hostedServiceNameToCreate);
            if (isNameAvailable) {
                WizardCacheManager.createHostedServiceMock(hostedServiceNameToCreate, hostedServiceLocation, descriptionTxt.getText());
//                valid = true;
                super.doOKAction();
            } else {
                PluginUtil.displayErrorDialog(message("dnsCnf"), message("hostedServiceConflictError"));
                hostedServiceTxt.requestFocusInWindow();
                hostedServiceTxt.selectAll();
            }
        } catch (final Exception ex) {
            PluginUtil.displayErrorDialogAndLog(message("error"), ex.getMessage(), ex);
        }
    }

    @Override
    protected boolean postponeValidation() {
        return false;
    }

    @Nullable
    protected ValidationInfo doValidate() {

        String host = hostedServiceTxt.getText();
        String location = (String) locationComb.getSelectedItem();

        boolean legalName = validateHostedServiceName(host);
        if (host == null || host.isEmpty()) {
            return new ValidationInfo(message("hostedIsNullError"), hostedServiceTxt);
        }
        if (!legalName) {
            return new ValidationInfo("<html>Hosted Service name may consist only of<br>numbers and lower case letters, and be 3-24 characters long.</html>",
                    hostedServiceTxt);
        }
        if (location == null || location.isEmpty()) {
            return new ValidationInfo(message("hostedLocNotSelectedError"), locationComb);
        }
//        setMessage(message("hostedCreateNew"));
        return null;
    }

    private boolean validateHostedServiceName(String host) {
        if (host.length() < 3 || host.length() > 24) {
            return false;
        }
        return host.matches(HOSTED_SERVICE_NAME_PATTERN);
    }

    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public String getHostedServiceName() {
        return hostedServiceTxt.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected JComponent createTitlePane() {
        return new TitlePanel(message("hostedNew"), message("hostedCreateNew"));
    }

    @Override
    protected String getHelpId() {
        return "new_hosted_service";
    }
}
