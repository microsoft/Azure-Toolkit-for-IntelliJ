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
package com.microsoft.intellij.util;

import com.intellij.openapi.module.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.AzureSettings;
import com.microsoft.intellij.runnable.AccountActionRunnable;
import com.microsoft.intellij.runnable.CacheAccountWithProgressBar;
import com.microsoft.intellij.runnable.LoadAccountWithProgressBar;
import com.microsoft.intellij.ui.libraries.AILibraryHandler;
import com.microsoft.intellij.ui.util.UIUtils;
import com.microsoft.intellij.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageServices;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

/**
 * Class has common methods which
 * handles publish settings file and extract data.
 * Methods get called whenever user clicks
 * "Import from publish settings file..." button
 * on publish wizard or preference page dialog.
 */
public class MethodUtils {
    /**
     * Method checks file selected by user is valid
     * and call method which extracts data from it.
     */
    public static void handleFile(String fileName, Project project) {
        if (fileName != null && !fileName.isEmpty()) {
            File file = new File(fileName);
            PublishData publishDataToCache = handlePublishSettings(file, project);
            if (publishDataToCache == null) {
                return;
            }
            WizardCacheManager.setCurrentPublishData(publishDataToCache);
            // Make centralized storage registry.
            prepareListFromPublishData(project);
        }
    }

    /**
     * Method extracts data from publish settings file.
     */
    public static PublishData handlePublishSettings(File file, Project project) {
        PublishData data = UIUtils.createPublishDataObj(file);
        /*
         * If data is equal to null,
		 * then publish settings file already exists.
		 * So don't load information again.
		 */
        if (data != null) {
            AccountActionRunnable settings = new CacheAccountWithProgressBar(file, data, message("loadingCred"));
            ProgressManager.getInstance().runProcessWithProgressSynchronously(settings, "Loading Account Settings...", true, project);
            AzureSettings.getSafeInstance(project).savePublishDatas();
        }
        return data;
    }

    /**
     * Method prepares storage account list.
     * Adds data from publish settings file.
     */
    public static void prepareListFromPublishData(Project project) {
        List<StorageAccount> strgList = StorageAccountRegistry.getStrgList();
        Collection<PublishData> publishDatas = WizardCacheManager.getPublishDatas();
        for (PublishData pd : publishDatas) {
            for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
                /*
				 * Get collection of storage services in each subscription.
				 */
                StorageServices services = pd.getStoragesPerSubscription().get(sub.getId());
                // iterate over collection of services.
                for (StorageService strgService : services) {
                    StorageAccount strEle = new StorageAccount(
                            strgService.getServiceName(),
                            strgService.getPrimaryKey(),
                            strgService.getStorageAccountProperties().
                                    getEndpoints().get(0).toString());
					/*
					 * Check if storage account is already present
					 * in centralized repository,
					 * if present then do not add.
					 * if not present then check
					 * access key is valid or not.
					 * If not then update with correct one in registry. 
					 */
                    if (strgList.contains(strEle)) {
                        int index = strgList.indexOf(strEle);
                        StorageAccount account = strgList.get(index);
                        String newKey = strEle.getStrgKey();
                        if (!account.getStrgKey().equals(newKey)) {
                            account.setStrgKey(newKey);
                        }
                    } else {
                        strgList.add(strEle);
                    }
                }
            }
        }
        AzureSettings.getSafeInstance(project).saveStorage();
    }

    /**
     * When we start new session,
     * reload the subscription and storage account
     * registry information just for once.
     */
    public static void loadSubInfoFirstTime(final Project project) {
        Task.Backgroundable task = new Task.Backgroundable(project, "Loading Account Settings...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                new LoadAccountWithProgressBar(project).run();
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        prepareListFromPublishData(project);
    }

    /**
     * Method scans all open Maven or Dynamic web projects form workspace
     * and prepare a list of instrumentation keys which are in use.
     * @return
     */
    public static List<String> getInUseInstrumentationKeys(Project project) {
        List<String> keyList = new ArrayList<String>();
        try {
            Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module : modules) {
                if (module!= null && module.isLoaded() && ModuleTypeId.JAVA_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE))) {
                    AILibraryHandler handler = new AILibraryHandler();
                    String aiXMLFilePath = String.format("%s%s%s", PluginUtil.getModulePath(module), File.separator, message("aiXMLPath"));
                    if (new File(aiXMLFilePath).exists()) {
                        handler.parseAIConfXmlPath(aiXMLFilePath);
                        String key = handler.getAIInstrumentationKey();
                        if (key != null && !key.isEmpty()) {
                            keyList.add(key);
                        }
                    }
                }
            }
        } catch(Exception ex) {
            AzurePlugin.log(ex.getMessage(), ex);
        }
        return keyList;
    }

    /**
     * Method scans all open Maven or Dynamic web projects form workspace
     * and returns name of project who is using specific key.
     * @return
     */
    public static String getModuleNameAsPerKey(Project project, String keyToRemove) {
        String name = "";
        try {
            Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module : modules) {
                if (module!= null && module.isLoaded() && ModuleTypeId.JAVA_MODULE.equals(module.getOptionValue(Module.ELEMENT_TYPE))) {
                    String aiXMLPath = String.format("%s%s%s", PluginUtil.getModulePath(module), File.separator, message("aiXMLPath"));
                    String webXMLPath = String.format("%s%s%s", PluginUtil.getModulePath(module), File.separator, message("xmlPath"));
                    AILibraryHandler handler = new AILibraryHandler();
                    if (new File(aiXMLPath).exists() && new File(webXMLPath).exists()) {
                        handler.parseWebXmlPath(webXMLPath);
                        handler.parseAIConfXmlPath(aiXMLPath);
                        // if application insights configuration is enabled.
                        if (handler.isAIWebFilterConfigured()) {
                            String key = handler.getAIInstrumentationKey();
                            if (key != null && !key.isEmpty() && key.equals(keyToRemove)) {
                                return module.getName();
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            AzurePlugin.log(ex.getMessage(), ex);
        }
        return name;
    }
}
