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
package com.microsoft.intellij.runnable;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.AccountCachingExceptionEvent;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingAccoutListener;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public abstract class AccountActionRunnable implements Runnable, LoadingAccoutListener {

    protected PublishData data;

    ProgressIndicator progressIndicator;

    protected final AtomicBoolean wait = new AtomicBoolean(true);
    protected final AtomicBoolean error = new AtomicBoolean(false);

    private int numberOfAccounts = 1;
    protected Exception exception;
    protected String errorMessage;

    public abstract void doTask();

    public AccountActionRunnable(PublishData data) {
        this.data = data;
    }

    public void setNumberOfAccounts(int num) {
        this.numberOfAccounts = num;
    }

    @Override
    public void run() {
        this.progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        setIndicatorText();

        doTask();
        if (error.get()) {
            progressIndicator.cancel();
            PluginUtil.displayErrorDialogInAWTAndLog(message("error"), errorMessage, exception);
        }
    }

    void setIndicatorText() {
        progressIndicator.setText("Loading Account Settings...");
        progressIndicator.setText2("Subscriptions");
    }

    @Override
    public synchronized void onLoadedSubscriptions() {
        setWorked(1.0 / (4 * numberOfAccounts));
        progressIndicator.setText2("Storage Services, Cloud Services and Locations");
    }

    @Override
    public void onLoadedStorageServices() {
        setWorked(1.0 / (4 * numberOfAccounts));
    }

    @Override
    public void onLoadedHostedServices() {
        setWorked(1.0 / (4 * numberOfAccounts));
    }

    @Override
    public void onLoadedLocations() {
        setWorked(1.0 / (4 * numberOfAccounts));
    }

    @Override
    public void onRestAPIError(AccountCachingExceptionEvent e) {
        wait.set(false);
        error.set(true);
        exception = e.getException();
        errorMessage = e.getMessage();
    }

    private synchronized void setWorked(double work) {
        progressIndicator.setFraction(progressIndicator.getFraction() + work);
    }
}
