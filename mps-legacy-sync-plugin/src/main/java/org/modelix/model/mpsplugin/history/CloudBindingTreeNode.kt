package org.modelix.model.mpsplugin.history;

/*Generated by MPS */

import jetbrains.mps.ide.ui.tree.TextTreeNode;
import org.modelix.model.mpsplugin.Binding;
import org.modelix.model.mpsplugin.CloudRepository;
import org.modelix.model.mpsplugin.ModelServerConnection;
import javax.swing.SwingUtilities;
import java.util.Map;
import jetbrains.mps.internal.collections.runtime.MapSequence;
import java.util.HashMap;
import jetbrains.mps.internal.collections.runtime.Sequence;
import jetbrains.mps.internal.collections.runtime.IVisitor;
import jetbrains.mps.internal.collections.runtime.ISelector;
import javax.swing.tree.TreeNode;

public class CloudBindingTreeNode extends TextTreeNode {
  private Binding binding;
  private CloudRepository cloudRepository;
  private Binding.IListener bindingListener = new Binding.IListener() {
    @Override
    public void bindingAdded(Binding binding) {
      updateBindingsLater();
    }

    @Override
    public void bindingRemoved(Binding binding) {
      updateBindingsLater();
    }

    @Override
    public void ownerChanged(Binding newOwner) {
      updateBindingsLater();
    }

    @Override
    public void bindingActivated() {
      updateText();
      updateBindingsLater();
    }

    @Override
    public void bindingDeactivated() {
      updateText();
      updateBindingsLater();
    }
  };

  public CloudBindingTreeNode(Binding binding, CloudRepository repositoryInModelServer) {
    super(binding.toString());
    this.binding = binding;
    this.cloudRepository = repositoryInModelServer;
    updateBindings();
  }

  public Binding getBinding() {
    return binding;
  }

  @Override
  protected void onAdd() {
    super.onAdd();
    binding.addListener(bindingListener);
  }

  @Override
  protected void onRemove() {
    super.onRemove();
    binding.removeListener(bindingListener);
  }

  public ModelServerConnection getModelServer() {
    return cloudRepository.getModelServer();
  }

  public CloudRepository getRepositoryInModelServer() {
    return cloudRepository;
  }

  public void updateBindingsLater() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        updateBindings();
      }
    });
  }

  public void updateText() {
    setText(binding.toString() + ((binding.isActive() ? "" : " [disabled]")));
  }

  public void updateBindings() {
    final Map<Binding, CloudBindingTreeNode> existing = MapSequence.fromMap(new HashMap<Binding, CloudBindingTreeNode>());
    Sequence.fromIterable(TreeModelUtil.getChildren(this)).ofType(CloudBindingTreeNode.class).visitAll(new IVisitor<CloudBindingTreeNode>() {
      public void visit(CloudBindingTreeNode it) {
        MapSequence.fromMap(existing).put(it.getBinding(), it);
      }
    });
    TreeModelUtil.setChildren(this, Sequence.fromIterable(binding.getOwnedBindings()).select(new ISelector<Binding, CloudBindingTreeNode>() {
      public CloudBindingTreeNode select(Binding it) {
        return (MapSequence.fromMap(existing).containsKey(it) ? MapSequence.fromMap(existing).get(it) : new CloudBindingTreeNode(it, cloudRepository));
      }
    }).ofType(TreeNode.class));
  }
}