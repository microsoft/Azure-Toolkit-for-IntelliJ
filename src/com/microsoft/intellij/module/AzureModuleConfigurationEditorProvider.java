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
package com.microsoft.intellij.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.microsoft.intellij.ui.RolesPanel;
import com.microsoft.intellij.ui.SubscriptionsPanel;
import com.microsoft.intellij.ui.WARemoteAccessPanel;

public class AzureModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
    @Override
    public ModuleConfigurationEditor[] createEditors(final ModuleConfigurationState state) {
        Module module = state.getRootModel().getModule();
        if (!AzureModuleType.isAzureModule(module)) {
            return ModuleConfigurationEditor.EMPTY;
        }
        return new ModuleConfigurationEditor[]{new ModuleEditor(state, new AzureModulePanel(state.getRootModel().getModule())),
                new ModuleEditor(state, new WARemoteAccessPanel(state.getRootModel().getModule(), false, null, null, null)),
                new ModuleEditor(state, new RolesPanel(state.getRootModel().getModule())),
                new ModuleEditor(state, new SubscriptionsPanel(state.getRootModel().getModule().getProject()))};
    }
}

