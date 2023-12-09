package org.modelix.model.mpsplugin.history;

/*Generated by MPS */

import jetbrains.mps.ide.ui.tree.TextTreeNode;
import org.modelix.model.api.IBranchListener;
import org.modelix.model.api.ITree;
import javax.swing.SwingUtilities;
import org.modelix.model.mpsplugin.ModelServerConnection;
import org.modelix.model.api.IBranch;
import org.modelix.model.mpsplugin.CloudIcons;
import java.util.Map;
import org.jetbrains.mps.openapi.model.SNode;
import jetbrains.mps.internal.collections.runtime.MapSequence;
import java.util.LinkedHashMap;
import jetbrains.mps.ide.ThreadUtils;
import jetbrains.mps.internal.collections.runtime.Sequence;
import javax.swing.tree.TreeNode;
import org.modelix.model.mpsplugin.SharedExecutors;
import java.util.List;
import org.modelix.model.area.PArea;
import kotlin.jvm.functions.Function0;
import jetbrains.mps.internal.collections.runtime.IListSequence;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import java.util.ArrayList;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SLinkOperations;
import jetbrains.mps.internal.collections.runtime.ISelector;
import org.modelix.model.mpsplugin.ModelixNotifications;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SPropertyOperations;
import jetbrains.mps.internal.collections.runtime.NotNullWhereFilter;
import jetbrains.mps.internal.collections.runtime.IVisitor;
import org.jetbrains.mps.openapi.language.SContainmentLink;
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory;
import org.jetbrains.mps.openapi.language.SProperty;

public class ModelServerTreeNode extends TextTreeNode {
  private IBranchListener branchListener = new IBranchListener() {
    @Override
    public void treeChanged(ITree oldTree, ITree newTree) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          ((CloudView.CloudViewTree) getTree()).runRebuildAction(new Runnable() {
            public void run() {
              updateChildren();
            }
          }, true);
        }
      });
    }
  };
  private ModelServerConnection modelServer;
  private IBranch infoBranch;
  private ModelServerConnection.IListener repoListener = new ModelServerConnection.IListener() {
    @Override
    public void connectionStatusChanged(final boolean connected) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (connected) {
            infoBranch = modelServer.getInfoBranch();
            if (getTree() != null) {
              infoBranch.addListener(branchListener);
            }
          }
          updateText();
          updateChildren();
        }
      });
    }
  };

  public ModelServerTreeNode(ModelServerConnection modelServer) {
    super(CloudIcons.MODEL_SERVER_ICON, modelServer.getBaseUrl());
    this.modelServer = modelServer;
    setAllowsChildren(true);
    setNodeIdentifier("" + System.identityHashCode(modelServer));
    modelServer.addListener(repoListener);
    updateText();
    updateChildren();
  }

  public ModelServerConnection getModelServer() {
    return this.modelServer;
  }

  public void updateText() {
    String text = modelServer.getBaseUrl();
    if (modelServer.isConnected()) {
      text += " (" + modelServer.getId() + ")";
    } else {
      text += " (not connected)";
    }
    String email = modelServer.getEmail();
    if ((email != null && email.length() > 0)) {
      text += " " + email;
    }
    setTextAndRepaint(text);
  }

  public void setTextAndRepaint(String text) {
    TreeModelUtil.setTextAndRepaint(this, text);
  }

  public void updateChildren() {
    if (modelServer.isConnected()) {
      final Map<SNode, RepositoryTreeNode> existing = MapSequence.fromMap(new LinkedHashMap<SNode, RepositoryTreeNode>(16, (float) 0.75, false));
      ThreadUtils.runInUIThreadAndWait(new Runnable() {
        public void run() {
          if (Sequence.fromIterable(TreeModelUtil.getChildren(ModelServerTreeNode.this)).isEmpty()) {
            TreeModelUtil.setChildren(ModelServerTreeNode.this, Sequence.<TreeNode>singleton(LoadingIcon.apply(new TextTreeNode("loading ..."))));
          }
          for (RepositoryTreeNode node : Sequence.fromIterable(TreeModelUtil.getChildren(ModelServerTreeNode.this)).ofType(RepositoryTreeNode.class)) {
            MapSequence.fromMap(existing).put(node.getRepositoryInfo(), node);
          }
        }
      });

      SharedExecutors.FIXED.execute(new Runnable() {
        public void run() {
          final List<TreeNode> newChildren = new PArea(modelServer.getInfoBranch()).executeRead(new Function0<IListSequence<TreeNode>>() {
            public IListSequence<TreeNode> invoke() {
              SNode info = modelServer.getInfo();
              if (info == null) {
                return ListSequence.fromList(new ArrayList<TreeNode>());
              }
              return ListSequence.fromList(SLinkOperations.getChildren(info, LINKS.repositories$b56J)).select(new ISelector<SNode, TreeNode>() {
                public TreeNode select(SNode it) {
                  TreeNode tn = null;
                  try {
                    tn = (MapSequence.fromMap(existing).containsKey(it) ? MapSequence.fromMap(existing).get(it) : new RepositoryTreeNode(modelServer, it));
                  } catch (Throwable t) {
                    t.printStackTrace();
                    ModelixNotifications.notifyError("Repository in invalid state", "Repository " + SPropertyOperations.getString(it, PROPS.id$baYB) + " cannot be loaded: " + t.getMessage());
                  }
                  return tn;
                }
              }).where(new NotNullWhereFilter()).toListSequence();
            }
          });

          ThreadUtils.runInUIThreadNoWait(new Runnable() {
            public void run() {
              TreeModelUtil.setChildren(ModelServerTreeNode.this, newChildren);
              Sequence.fromIterable(TreeModelUtil.getChildren(ModelServerTreeNode.this)).ofType(RepositoryTreeNode.class).visitAll(new IVisitor<RepositoryTreeNode>() {
                public void visit(RepositoryTreeNode it) {
                  it.updateChildren();
                }
              });
            }
          });
        }
      });
    } else {
      ThreadUtils.runInUIThreadNoWait(new Runnable() {
        public void run() {
          TreeModelUtil.clearChildren(ModelServerTreeNode.this);
        }
      });
    }
  }

  @Override
  protected void onAdd() {
    super.onAdd();
    if (infoBranch != null) {
      infoBranch.addListener(branchListener);
    }
  }

  @Override
  protected void onRemove() {
    super.onRemove();
    if (infoBranch != null) {
      modelServer.getInfoBranch().removeListener(branchListener);
    }
  }

  private static final class LINKS {
    /*package*/ static final SContainmentLink repositories$b56J = MetaAdapterFactory.getContainmentLink(0xb6980ebdf01d459dL, 0xa95238740f6313b4L, 0x62b7d9b07cecbcbfL, 0x62b7d9b07cecbcc2L, "repositories");
  }

  private static final class PROPS {
    /*package*/ static final SProperty id$baYB = MetaAdapterFactory.getProperty(0xb6980ebdf01d459dL, 0xa95238740f6313b4L, 0x62b7d9b07cecbcc0L, 0x62b7d9b07cecbcc6L, "id");
  }
}