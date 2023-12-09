package org.modelix.model.mpsplugin.plugin;

/*Generated by MPS */

import jetbrains.mps.plugins.actions.GeneratedActionGroup;
import org.jetbrains.annotations.NotNull;
import jetbrains.mps.workbench.action.ApplicationPlugin;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.extensions.PluginId;

public class CloudActionsForPhysicalModules_ActionGroup extends GeneratedActionGroup {
  public static final String ID = "org.modelix.model.mpsplugin.plugin.CloudActionsForPhysicalModules_ActionGroup";

  public CloudActionsForPhysicalModules_ActionGroup(@NotNull ApplicationPlugin plugin) {
    super("CloudActionsForPhysicalModules", ID, plugin);
    setIsInternal(false);
    setPopup(false);
    {
      GeneratedActionGroup newAction = new CloudActionsForPhysicalModulesCloudActions_ActionGroup(getApplicationPlugin());
      ActionManagerEx manager = ActionManagerEx.getInstanceEx();
      AnAction oldAction = manager.getAction(newAction.getId());
      if (oldAction == null) {
        manager.registerAction(newAction.getId(), newAction, PluginId.getId("org.modelix.model.mpsplugin"));
        oldAction = newAction;
      }
      CloudActionsForPhysicalModules_ActionGroup.this.addAction(oldAction);
    }
  }
}