package org.modelix.model.mpsplugin.plugin;

/*Generated by MPS */

import jetbrains.mps.workbench.action.BaseAction;
import javax.swing.Icon;
import org.modelix.model.mpsplugin.CloudRepository;
import jetbrains.mps.workbench.action.ActionAccess;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.util.Map;
import jetbrains.mps.ide.actions.MPSCommonDataKeys;
import org.modelix.model.mpsplugin.ModelCloudImportUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import org.jetbrains.mps.openapi.module.SModule;

public class ModuleAlreadyOnCloud_Action extends BaseAction {
  private static final Icon ICON = null;

  private CloudRepository treeInRepository;
  public ModuleAlreadyOnCloud_Action(CloudRepository treeInRepository_par) {
    super("Copy on Cloud & Sync", "", ICON);
    this.treeInRepository = treeInRepository_par;
    this.setIsAlwaysVisible(false);
    this.setActionAccess(ActionAccess.UNDO_PROJECT);
  }
  @Override
  public boolean isDumbAware() {
    return true;
  }
  @Override
  public boolean isApplicable(AnActionEvent event, final Map<String, Object> _params) {
    boolean connected = ModuleAlreadyOnCloud_Action.this.treeInRepository.isConnected();
    event.getPresentation().setText(event.getData(MPSCommonDataKeys.MODULE).getModuleName() + " already in Cloud");
    try {
      return connected && ModelCloudImportUtils.containsModule(ModuleAlreadyOnCloud_Action.this.treeInRepository, event.getData(MPSCommonDataKeys.MODULE));
    } catch (RuntimeException e) {
      // This could happen because of repositories in invalid state. In this case let's ignore those repositories without preventing usage of other repositories
      return false;
    }
  }
  @Override
  public void doUpdate(@NotNull AnActionEvent event, final Map<String, Object> _params) {
    this.setEnabledState(event.getPresentation(), this.isApplicable(event, _params));
  }
  @Override
  protected boolean collectActionData(AnActionEvent event, final Map<String, Object> _params) {
    if (!(super.collectActionData(event, _params))) {
      return false;
    }
    {
      Project p = event.getData(CommonDataKeys.PROJECT);
      if (p == null) {
        return false;
      }
    }
    {
      SModule p = event.getData(MPSCommonDataKeys.MODULE);
      if (p == null) {
        return false;
      }
    }
    return true;
  }
  @Override
  public void doExecute(@NotNull final AnActionEvent event, final Map<String, Object> _params) {
    // noop
  }
  @NotNull
  public String getActionId() {
    StringBuilder res = new StringBuilder();
    res.append(super.getActionId());
    res.append("#");
    res.append(treeInRepository_State((CloudRepository) this.treeInRepository));
    res.append("!");
    return res.toString();
  }
  public static String treeInRepository_State(CloudRepository object) {
    return object.presentation();
  }
}