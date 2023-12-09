package org.modelix.model.mpsplugin.plugin;

/*Generated by MPS */

import jetbrains.mps.plugins.actions.GeneratedActionGroup;
import java.util.Set;
import com.intellij.openapi.util.Pair;
import jetbrains.mps.workbench.ActionPlace;
import org.jetbrains.mps.util.Condition;
import jetbrains.mps.workbench.action.BaseAction;
import jetbrains.mps.internal.collections.runtime.SetSequence;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import jetbrains.mps.workbench.action.ApplicationPlugin;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.mps.project.MPSProject;
import jetbrains.mps.ide.actions.MPSCommonDataKeys;
import org.modelix.model.mpsplugin.history.CloudNodeTreeNode;
import org.modelix.model.api.INode;
import org.modelix.model.api.PNodeAdapter;
import org.modelix.model.api.IConcept;
import org.jetbrains.mps.openapi.language.SAbstractConcept;
import org.modelix.model.mpsadapters.mps.SConceptAdapter;
import org.jetbrains.mps.openapi.language.SProperty;
import jetbrains.mps.internal.collections.runtime.Sequence;
import jetbrains.mps.internal.collections.runtime.ISelector;
import org.jetbrains.annotations.Nullable;

public class CloudNodeGroupSetProperty_ActionGroup extends GeneratedActionGroup {
  public static final String ID = "org.modelix.model.mpsplugin.plugin.CloudNodeGroupSetProperty_ActionGroup";
  private final Set<Pair<ActionPlace, Condition<BaseAction>>> myPlaces = SetSequence.fromSet(new HashSet<Pair<ActionPlace, Condition<BaseAction>>>());

  public CloudNodeGroupSetProperty_ActionGroup(@NotNull ApplicationPlugin plugin) {
    super("Set Property", ID, plugin);
    setIsInternal(false);
    setPopup(true);
  }
  public void doUpdate(AnActionEvent event) {
    removeAll();
    MPSProject project = event.getData(MPSCommonDataKeys.MPS_PROJECT);
    CloudNodeTreeNode treeNode = as_shasbg_a0a2a4(event.getData(MPSCommonDataKeys.TREE_NODE), CloudNodeTreeNode.class);
    if (treeNode == null) {
      return;
    }
    INode node = treeNode.getNode();
    if (!(node instanceof PNodeAdapter)) {
      return;
    }
    PNodeAdapter pnode = ((PNodeAdapter) node);
    IConcept concept = pnode.getConcept();
    if (concept == null) {
      return;
    }
    SAbstractConcept sconcept = SConceptAdapter.unwrap(concept);

    Iterable<SProperty> properties = sconcept.getProperties();
    for (SProperty role : Sequence.fromIterable(properties).sort(new ISelector<SProperty, String>() {
      public String select(SProperty it) {
        return it.getName();
      }
    }, true)) {
      CloudNodeGroupSetProperty_ActionGroup.this.addParameterizedAction(new SetProperty_Action(node, role), node, role);
    }
    for (Pair<ActionPlace, Condition<BaseAction>> p : this.myPlaces) {
      this.addPlace(p.first, p.second);
    }
  }
  public void addPlace(ActionPlace place, @Nullable Condition<BaseAction> cond) {
    SetSequence.fromSet(this.myPlaces).addElement(new Pair<ActionPlace, Condition<BaseAction>>(place, cond));
  }
  public boolean isStrict() {
    return false;
  }
  private static <T> T as_shasbg_a0a2a4(Object o, Class<T> type) {
    return (type.isInstance(o) ? (T) o : null);
  }
}