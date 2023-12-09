package org.modelix.model.mpsplugin.plugin;

/*Generated by MPS */

import jetbrains.mps.plugins.actions.GeneratedActionGroup;
import org.jetbrains.annotations.NotNull;
import jetbrains.mps.workbench.action.ApplicationPlugin;

public class CloudBranchGroup_ActionGroup extends GeneratedActionGroup {
  public static final String ID = "org.modelix.model.mpsplugin.plugin.CloudBranchGroup_ActionGroup";

  public CloudBranchGroup_ActionGroup(@NotNull ApplicationPlugin plugin) {
    super("CloudBranchGroup", ID, plugin);
    setIsInternal(false);
    setPopup(false);
    CloudBranchGroup_ActionGroup.this.addAction("org.modelix.model.mpsplugin.plugin.AddBranch_Action");
    CloudBranchGroup_ActionGroup.this.addAction("org.modelix.model.mpsplugin.plugin.SwitchBranch_Action");
    CloudBranchGroup_ActionGroup.this.addAction("org.modelix.model.mpsplugin.plugin.LoadHistoryForBranch_Action");
  }
}
