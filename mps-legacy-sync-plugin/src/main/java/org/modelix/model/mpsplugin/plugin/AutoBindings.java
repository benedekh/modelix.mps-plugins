package org.modelix.model.mpsplugin.plugin;

/*Generated by MPS */

import org.modelix.model.mpsplugin.ModelServerConnections;
import org.modelix.model.mpsplugin.ModelServerConnection;
import org.modelix.model.api.IBranchListener;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import java.util.Set;
import jetbrains.mps.internal.collections.runtime.SetSequence;
import java.util.HashSet;
import org.modelix.model.client.ActiveBranch;
import java.util.List;
import org.modelix.model.lazy.RepositoryId;
import java.util.concurrent.ScheduledFuture;
import jetbrains.mps.internal.collections.runtime.Sequence;
import org.modelix.model.mpsplugin.SharedExecutors;
import jetbrains.mps.internal.collections.runtime.IWhereFilter;
import org.modelix.model.api.IBranch;
import org.modelix.model.api.ITree;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import java.util.ArrayList;
import org.jetbrains.mps.openapi.model.SNode;
import org.modelix.model.area.PArea;
import kotlin.jvm.functions.Function0;
import jetbrains.mps.internal.collections.runtime.IListSequence;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SLinkOperations;
import jetbrains.mps.internal.collections.runtime.ISelector;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SPropertyOperations;
import jetbrains.mps.baseLanguage.closures.runtime._FunctionTypes;
import kotlin.Unit;
import org.modelix.model.api.ITransaction;
import org.modelix.model.mpsadapters.mps.NodeToSNodeAdapter;
import org.modelix.model.api.PNodeAdapter;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SNodeOperations;
import org.modelix.model.mpsadapters.mps.SNodeToNodeAdapter;
import jetbrains.mps.project.Project;
import jetbrains.mps.project.ProjectManager;
import org.modelix.model.mpsplugin.ProjectBinding;
import jetbrains.mps.project.MPSProject;
import org.modelix.model.mpsplugin.TransientModuleBinding;
import jetbrains.mps.internal.collections.runtime.IVisitor;
import org.jetbrains.mps.openapi.language.SContainmentLink;
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory;
import org.jetbrains.mps.openapi.language.SProperty;
import org.jetbrains.mps.openapi.language.SConcept;

public class AutoBindings implements ModelServerConnections.IListener, ModelServerConnection.IListener, IBranchListener {
  private static final Logger LOG = LogManager.getLogger(AutoBindings.class);

  private ModelServerConnections repositories;
  private Set<ModelServerConnection> subscribedModelServers = SetSequence.fromSet(new HashSet<ModelServerConnection>());
  private Set<ModelServerConnection> subscribedInfoBranches = SetSequence.fromSet(new HashSet<ModelServerConnection>());
  private Set<ActiveBranch> subscribedActiveBranches = SetSequence.fromSet(new HashSet<ActiveBranch>());
  private volatile boolean requiresUpdate = false;
  private List<RepositoryId> fixedRepositories;
  private boolean bindProjects;
  private ScheduledFuture<?> updateJob;

  public AutoBindings(ModelServerConnections repositories) {
    this(repositories, null, false);
  }

  public AutoBindings(ModelServerConnections repositories, Iterable<RepositoryId> fixedRepositoryIds, boolean bindProjects) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("AutoBindings.init");
    }
    this.repositories = repositories;
    this.fixedRepositories = (fixedRepositoryIds == null ? null : Sequence.fromIterable(fixedRepositoryIds).toListSequence());
    this.bindProjects = bindProjects;
    repositories.addListener(this);
    subscribeToServers();
    if (fixedRepositories == null) {
      subscribeToInfoBranches();
    }
    updateBindingsLater();
    updateJob = SharedExecutors.fixDelay(500, new Runnable() {
      public void run() {
        if (requiresUpdate) {
          requiresUpdate = false;
          updateBindings();
        }
      }
    });
  }

  @Override
  public void repositoriesChanged() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("AutoBindings.repositoriesChanged");
    }
    subscribeToServers();
    subscribeToInfoBranches();
    updateBindingsLater();
  }

  protected void subscribeToServers() {
    for (ModelServerConnection server : Sequence.fromIterable(repositories.getModelServers()).where(new IWhereFilter<ModelServerConnection>() {
      public boolean accept(ModelServerConnection it) {
        return !(SetSequence.fromSet(subscribedModelServers).contains(it));
      }
    })) {
      server.addListener(this);
      SetSequence.fromSet(subscribedModelServers).addElement(server);
    }
  }

  @Override
  public void connectionStatusChanged(boolean connected) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("AutoBindings.connectionStatusChanged, " + connected);
    }
    subscribeToInfoBranches();
    updateBindingsLater();
  }

  protected void subscribeToInfoBranches() {
    for (ModelServerConnection repo : SetSequence.fromSet(subscribedModelServers).subtract(SetSequence.fromSet(subscribedInfoBranches)).where(new IWhereFilter<ModelServerConnection>() {
      public boolean accept(ModelServerConnection it) {
        return it.isConnected();
      }
    })) {
      IBranch infoBranch = repo.getInfoBranch();
      if (infoBranch != null) {
        infoBranch.addListener(this);
        SetSequence.fromSet(subscribedInfoBranches).addElement(repo);
      }
    }
  }

  @Override
  public void treeChanged(ITree oldTree, ITree newTree) {
    updateBindingsLater();
  }

  public void updateBindingsLater() {
    requiresUpdate = true;
  }

  protected synchronized void updateBindings() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("AutoBindings.updateBindings");
    }

    List<ActiveBranch> allActiveBranches = ListSequence.fromList(new ArrayList<ActiveBranch>());

    for (final ModelServerConnection connection : Sequence.fromIterable(repositories.getModelServers()).where(new IWhereFilter<ModelServerConnection>() {
      public boolean accept(ModelServerConnection it) {
        return it.isConnected();
      }
    })) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("update bindings for " + connection.getBaseUrl());
      }
      List<RepositoryId> repositories;
      if (fixedRepositories == null) {
        final SNode info = connection.getInfo();
        repositories = new PArea(connection.getInfoBranch()).executeRead(new Function0<IListSequence<RepositoryId>>() {
          public IListSequence<RepositoryId> invoke() {
            return ListSequence.fromList(SLinkOperations.getChildren(info, LINKS.repositories$b56J)).select(new ISelector<SNode, RepositoryId>() {
              public RepositoryId select(SNode it) {
                return new RepositoryId(SPropertyOperations.getString(it, PROPS.id$baYB));
              }
            }).toListSequence();
          }
        });
      } else {
        repositories = fixedRepositories;
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("using repositories: " + ListSequence.fromList(repositories).select(new ISelector<RepositoryId, String>() {
          public String select(RepositoryId it) {
            return it.getId();
          }
        }));
      }
      for (final RepositoryId repositoryId : ListSequence.fromList(repositories)) {
        final ActiveBranch activeBranch = connection.getActiveBranch(repositoryId);
        if (LOG.isDebugEnabled()) {
          LOG.debug("using branch: " + activeBranch.getBranchName());
        }
        ListSequence.fromList(allActiveBranches).addElement(activeBranch);
        final List<_FunctionTypes._void_P0_E0> outsideRead = ListSequence.fromList(new ArrayList<_FunctionTypes._void_P0_E0>());
        new PArea(activeBranch.getBranch()).executeRead(new Function0<Unit>() {
          public Unit invoke() {
            ITransaction t = activeBranch.getBranch().getTransaction();
            Iterable<Long> allChildren_ = t.getAllChildren(ITree.ROOT_ID);
            Iterable<SNode> allChildren = Sequence.fromIterable(allChildren_).select(new ISelector<Long, SNode>() {
              public SNode select(Long it) {
                SNode n = NodeToSNodeAdapter.wrap(PNodeAdapter.Companion.wrap(it, activeBranch.getBranch()));
                return n;
              }
            });

            Iterable<SNode> allProjects = SNodeOperations.ofConcept(allChildren, CONCEPTS.Project$An);
            Iterable<SNode> remainingProjects = allProjects;
            if (bindProjects) {
              SNode firstProject = Sequence.fromIterable(allProjects).first();
              if (LOG.isDebugEnabled()) {
                LOG.debug("trying to bind project: " + firstProject);
              }
              remainingProjects = Sequence.fromIterable(allProjects).skip(1);
              final long projectNodeId = (firstProject == null ? 0L : ((PNodeAdapter) SNodeToNodeAdapter.wrap(firstProject)).getNodeId());
              final List<Project> mpsProjects = ProjectManager.getInstance().getOpenedProjects();
              if (ListSequence.fromList(mpsProjects).isNotEmpty()) {
                if (LOG.isDebugEnabled()) {
                  LOG.debug("mps projects found: " + ListSequence.fromList(mpsProjects).select(new ISelector<Project, String>() {
                    public String select(Project it) {
                      return it.getName();
                    }
                  }));
                }
                if (!(connection.hasProjectBinding(repositoryId, projectNodeId))) {
                  if (LOG.isDebugEnabled()) {
                    LOG.debug("adding project binding");
                  }
                  if (projectNodeId != 0L && ListSequence.fromList(SLinkOperations.getChildren(firstProject, LINKS.modules$Bi3g)).isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                      LOG.debug("Server side project doesn't contain any modules");
                    }
                  }
                  ListSequence.fromList(outsideRead).addElement(new _FunctionTypes._void_P0_E0() {
                    public void invoke() {
                      connection.addBinding(repositoryId, new ProjectBinding((MPSProject) ListSequence.fromList(mpsProjects).first(), projectNodeId, null));
                    }
                  });
                }
              } else {
                if (LOG.isDebugEnabled()) {
                  LOG.debug("no mps project found yet");
                }
                updateBindingsLater();
              }
            }

            Iterable<SNode> modules = Sequence.fromIterable(SNodeOperations.ofConcept(allChildren, CONCEPTS.Module$4i)).concat(Sequence.fromIterable(SLinkOperations.collectMany(remainingProjects, LINKS.modules$Bi3g)));
            for (SNode module : Sequence.fromIterable(modules)) {
              final long moduleNodeId = ((PNodeAdapter) SNodeToNodeAdapter.wrap(module)).getNodeId();
              if (connection.hasModuleBinding(repositoryId, moduleNodeId)) {
                continue;
              }
              ListSequence.fromList(outsideRead).addElement(new _FunctionTypes._void_P0_E0() {
                public void invoke() {
                  connection.addBinding(repositoryId, new TransientModuleBinding(moduleNodeId));
                }
              });
            }
            return Unit.INSTANCE;
          }
        });
        ListSequence.fromList(outsideRead).visitAll(new IVisitor<_FunctionTypes._void_P0_E0>() {
          public void visit(_FunctionTypes._void_P0_E0 it) {
            it.invoke();
          }
        });
      }
    }

    for (ActiveBranch newBranch : ListSequence.fromList(allActiveBranches).subtract(SetSequence.fromSet(subscribedActiveBranches)).toListSequence()) {
      newBranch.addListener(this);
    }

    for (ActiveBranch removedBranch : SetSequence.fromSet(subscribedActiveBranches).subtract(ListSequence.fromList(allActiveBranches)).toListSequence()) {
      removedBranch.removeListener(this);
      SetSequence.fromSet(subscribedActiveBranches).removeElement(removedBranch);
    }
  }

  public void dispose() {
    check_7p0tz2_a0a82(updateJob);
    for (ModelServerConnection repo : SetSequence.fromSet(subscribedInfoBranches)) {
      repo.getInfoBranch().removeListener(this);
    }
    for (ModelServerConnection repo : SetSequence.fromSet(subscribedModelServers)) {
      repo.removeListener(this);
    }
    repositories.removeListener(this);
  }
  private static boolean check_7p0tz2_a0a82(ScheduledFuture<?> checkedDotOperand) {
    if (null != checkedDotOperand) {
      return checkedDotOperand.cancel(false);
    }
    return false;
  }

  private static final class LINKS {
    /*package*/ static final SContainmentLink repositories$b56J = MetaAdapterFactory.getContainmentLink(0xb6980ebdf01d459dL, 0xa95238740f6313b4L, 0x62b7d9b07cecbcbfL, 0x62b7d9b07cecbcc2L, "repositories");
    /*package*/ static final SContainmentLink modules$Bi3g = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x37a0917d689de959L, 0x37a0917d689de9e2L, "modules");
  }

  private static final class PROPS {
    /*package*/ static final SProperty id$baYB = MetaAdapterFactory.getProperty(0xb6980ebdf01d459dL, 0xa95238740f6313b4L, 0x62b7d9b07cecbcc0L, 0x62b7d9b07cecbcc6L, "id");
  }

  private static final class CONCEPTS {
    /*package*/ static final SConcept Project$An = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x37a0917d689de959L, "org.modelix.model.repositoryconcepts.structure.Project");
    /*package*/ static final SConcept Module$4i = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, "org.modelix.model.repositoryconcepts.structure.Module");
  }
}
