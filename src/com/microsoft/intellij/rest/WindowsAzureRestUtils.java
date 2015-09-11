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
package com.microsoft.intellij.rest;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static com.microsoft.intellij.AzurePlugin.log;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

/**
 * this is a temporary class;
 * to be removed when classloader issue fixed (in Azure Java SDK or AzureManagementUtil)
 */
public class WindowsAzureRestUtils {
    public static Configuration getConfiguration(File file, String subscriptionId) throws IOException {
        try {
            // Get current context class loader
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            // Change context classloader to class context loader
            Thread.currentThread().setContextClassLoader(WindowsAzureRestUtils.class.getClassLoader());
            Configuration configuration = PublishSettingsLoader.createManagementConfiguration(file.getPath(), subscriptionId);
            // Call Azure API and reset back the context loader
            Thread.currentThread().setContextClassLoader(contextLoader);
            log("Created configuration for subscriptionId: " + subscriptionId);
            return configuration;
        } catch (IOException ex) {
            log(message("error"), ex);
            throw ex;
        }
    }

    public static Configuration loadConfiguration(String subscriptionId, String url) throws IOException {
        String keystore = System.getProperty("user.home") + File.separator + ".azure" + File.separator + subscriptionId + ".out";
        URI mngUri = URI.create(url);
        // Get current context class loader
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        // Change context classloader to class context loader
        Thread.currentThread().setContextClassLoader(WindowsAzureRestUtils.class.getClassLoader());
        Configuration configuration = ManagementConfiguration.configure(null, Configuration.load(), mngUri, subscriptionId, keystore, "", KeyStoreType.pkcs12);
        // Call Azure API and reset back the context loader
        Thread.currentThread().setContextClassLoader(contextLoader);
        return configuration;
    }
}
