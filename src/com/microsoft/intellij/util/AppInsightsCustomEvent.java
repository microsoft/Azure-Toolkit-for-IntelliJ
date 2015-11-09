package com.microsoft.intellij.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.applicationinsights.TelemetryClient;

import com.microsoftopentechnologies.azurecommons.xmlhandling.DataOperations;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class AppInsightsCustomEvent {
    public static void create(String eventName, String version) {
        String dataFile = WAHelper.getTemplateFile(message("dataFileName"));
        if (new File(dataFile).exists()) {
            String prefValue = DataOperations.getProperty(dataFile, message("prefVal"));
            if (prefValue != null && !prefValue.isEmpty() && prefValue.equalsIgnoreCase("true")) {
                TelemetryClient telemetry = new TelemetryClient();
                telemetry.getContext().setInstrumentationKey("220c5e6d-14da-4ae1-b2e4-8497bf78a6b1");
                Map<String, String> properties = new HashMap<String, String>();
                if (version != null && !version.isEmpty()) {
                    properties.put("Library Version", version);
                }
                String pluginVersion = DataOperations.getProperty(dataFile, message("pluginVersion"));
                if (pluginVersion != null && !pluginVersion.isEmpty()) {
                    properties.put("Plugin Version", pluginVersion);
                }

                Map<String, Double> metrics = new HashMap<String, Double>();
                String instID = DataOperations.getProperty(dataFile, message("instID"));
                if (instID != null && !instID.isEmpty()) {
                    metrics.put("Installation ID", Double.parseDouble(instID));
                }

                telemetry.trackEvent(eventName, properties, metrics);
                telemetry.flush();
            }
        }
    }
}
