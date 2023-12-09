package org.modelix.model.mpsplugin.projectview;

/*Generated by MPS */

import jetbrains.mps.ide.ui.tree.module.ProjectModuleTreeNode;
import org.jetbrains.mps.openapi.module.SModuleListenerBase;
import org.jetbrains.mps.openapi.module.SModule;
import org.jetbrains.mps.openapi.model.SModel;
import org.jetbrains.mps.openapi.model.SModelReference;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import jetbrains.mps.internal.collections.runtime.Sequence;
import jetbrains.mps.internal.collections.runtime.ISelector;
import jetbrains.mps.ide.ui.tree.smodel.SModelTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class CloudModuleTreeNode extends ProjectModuleTreeNode {
  private boolean myInitialized = false;
  private SModuleListenerBase moduleListener = new SModuleListenerBase() {
    @Override
    public void modelAdded(SModule module, SModel model) {
      update();
    }
    @Override
    public void modelRemoved(SModule module, SModelReference ref) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          update();
        }
      });
    }
  };
  public CloudModuleTreeNode(@NotNull SModule module) {
    super(module);
    setNodeIdentifier(module.getModuleId().toString());
    setIcon(CloudProjectViewExtension.MODULE_ICON);
    module.addModuleListener(moduleListener);
  }
  @Override
  public String getModuleText() {
    return getModule().getModuleName();
  }
  @Override
  public boolean isInitialized() {
    return myInitialized;
  }

  @Override
  protected void doInit() {
    populate();
    myInitialized = true;
  }

  protected void populate() {
    Iterable<SModel> models = getModule().getModels();
    for (SModel model : Sequence.fromIterable(models).sort(new ISelector<SModel, String>() {
      public String select(SModel it) {
        return it.getName().getLongName();
      }
    }, true)) {
      SModelTreeNode tn = new SModelTreeNode(model);
      tn.setIcon(CloudProjectViewExtension.MODEL_ICON);
      tn.setBaseIcon(CloudProjectViewExtension.MODEL_ICON);
      add(tn);
    }
    check_7wx4yo_a2a8(check_7wx4yo_a0c0i(getTree(), this), this);
  }

  @Override
  protected void doUpdate() {
    super.doUpdate();
    myInitialized = false;
    removeAllChildren();
  }

  public void dispose() {
    getModule().removeModuleListener(moduleListener);
  }
  private static void check_7wx4yo_a2a8(DefaultTreeModel checkedDotOperand, CloudModuleTreeNode checkedDotThisExpression) {
    if (null != checkedDotOperand) {
      checkedDotOperand.nodeStructureChanged(checkedDotThisExpression);
    }

  }
  private static DefaultTreeModel check_7wx4yo_a0c0i(JTree checkedDotOperand, CloudModuleTreeNode checkedDotThisExpression) {
    if (null != checkedDotOperand) {
      return (DefaultTreeModel) checkedDotOperand.getModel();
    }
    return null;
  }
}
