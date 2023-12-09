package org.modelix.model.mpsplugin;

/*Generated by MPS */

import javax.swing.tree.TreeNode;
import jetbrains.mps.project.Project;
import org.modelix.model.mpsplugin.history.CloudNodeTreeNode;
import org.modelix.model.mpsplugin.history.CloudNodeTreeNodeBinding;
import org.modelix.model.api.PNodeAdapter;
import jetbrains.mps.project.Solution;
import jetbrains.mps.ide.project.ProjectHelper;
import org.modelix.model.mpsplugin.plugin.PersistedBindingConfiguration;
import jetbrains.mps.project.MPSProject;
import org.modelix.model.lazy.RepositoryId;
import org.jetbrains.mps.openapi.module.SModule;
import org.modelix.model.api.INode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.mps.openapi.util.ProgressMonitor;
import org.jetbrains.mps.openapi.model.SNode;
import org.modelix.model.mpsadapters.mps.NodeToSNodeAdapter;
import java.util.function.Consumer;
import jetbrains.mps.baseLanguage.closures.runtime.Wrappers;
import jetbrains.mps.progress.EmptyProgressMonitor;
import org.modelix.model.mpsadapters.mps.SModuleAsNode;
import java.util.List;
import org.jetbrains.mps.openapi.model.SModel;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import java.util.ArrayList;
import org.modelix.model.mpsadapters.mps.SModelAsNode;
import jetbrains.mps.internal.collections.runtime.Sequence;
import org.modelix.model.mpsadapters.mps.SConceptAdapter;
import org.jetbrains.mps.openapi.language.SProperty;
import org.jetbrains.mps.openapi.model.SReference;
import org.modelix.model.mpsadapters.mps.SNodeToNodeAdapter;
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory;
import org.jetbrains.mps.openapi.language.SContainmentLink;
import org.jetbrains.mps.openapi.language.SConcept;

/**
 * This class is responsible for importing local MPS modules into the Modelix server
 */
public class ModelCloudImportUtils {

  public static void checkoutAndSync(TreeNode treeNode, final Project mpsProject) {
    CloudNodeTreeNode nodeTreeNode = (CloudNodeTreeNode) treeNode;
    final CloudRepository treeInRepository = CloudNodeTreeNodeBinding.getTreeInRepository(nodeTreeNode);
    final PNodeAdapter cloudModuleNode = ((PNodeAdapter) nodeTreeNode.getNode());
    final Solution solution = new ModuleCheckout(mpsProject, treeInRepository).checkoutCloudModule(cloudModuleNode);
    mpsProject.getRepository().getModelAccess().runReadAction(new Runnable() {
      public void run() {
        syncInModelixAsIndependentModule(treeInRepository, solution, ProjectHelper.toIdeaProject(mpsProject), cloudModuleNode);
      }
    });
    PersistedBindingConfiguration.getInstance(ProjectHelper.toIdeaProject(mpsProject)).addMappedBoundModule(treeInRepository, cloudModuleNode);
  }

  public static void checkoutAndSync(final CloudRepository treeInRepository, final Project mpsProject, long cloudModuleNodeId) {
    final PNodeAdapter cloudModuleNode = new PNodeAdapter(cloudModuleNodeId, treeInRepository.getActiveBranch().getBranch());
    final Solution solution = new ModuleCheckout(mpsProject, treeInRepository).checkoutCloudModule(cloudModuleNode);
    mpsProject.getRepository().getModelAccess().runReadAction(new Runnable() {
      public void run() {
        syncInModelixAsIndependentModule(treeInRepository, solution, ProjectHelper.toIdeaProject(mpsProject), cloudModuleNode);
      }
    });
    PersistedBindingConfiguration.getInstance(ProjectHelper.toIdeaProject(mpsProject)).addMappedBoundModule(treeInRepository, cloudModuleNode);
  }

  public static void bindCloudProjectToMpsProject(CloudRepository repositoryInModelServer, long cloudProjectId, MPSProject mpsProject, SyncDirection initialSyncDirection) {
    repositoryInModelServer.addBinding(new ProjectBinding(mpsProject, cloudProjectId, initialSyncDirection));
  }

  public static TransientModuleBinding addTransientModuleBinding(com.intellij.openapi.project.Project mpsProject, CloudRepository repositoryInModelServer, long cloudNodeId) {
    ModelServerConnection modelServerConnection = repositoryInModelServer.getModelServer();
    RepositoryId repositoryId = repositoryInModelServer.getRepositoryId();
    TransientModuleBinding transientModuleBinding = new TransientModuleBinding(cloudNodeId);
    modelServerConnection.addBinding(repositoryId, transientModuleBinding);
    PersistedBindingConfiguration.getInstance(mpsProject).addTransientBoundModule(repositoryInModelServer, repositoryInModelServer.getActiveBranch().getBranch(), cloudNodeId);
    return transientModuleBinding;
  }
  public static boolean containsModule(CloudRepository treeInRepository, SModule module) {
    return treeInRepository.hasModuleInRepository(module.getModuleId().toString());
  }

  /**
   * We create an exact copy of a physical module into Modelix, as a root level module
   * (i.e., a module right below a Tree)
   */
  public static INode copyInModelixAsIndependentModule(final CloudRepository treeInRepository, final SModule module, com.intellij.openapi.project.Project project, @Nullable ProgressMonitor progress) {
    // First create the module
    final INode cloudModuleNode = treeInRepository.createModule(module.getModuleName());
    replicatePhysicalModule(treeInRepository, cloudModuleNode, module, null, progress);
    return cloudModuleNode;
  }

  public static void copyAndSyncInModelixAsIndependentModule(CloudRepository treeInRepository, SModule module, com.intellij.openapi.project.Project mpsProject, @Nullable ProgressMonitor progress) {

    // 1. Copy the module in the cloud repo
    INode cloudModuleNode = copyInModelixAsIndependentModule(treeInRepository, module, mpsProject, progress);

    syncInModelixAsIndependentModule(treeInRepository, module, mpsProject, cloudModuleNode);
  }

  public static ProjectBinding copyAndSyncInModelixAsEntireProject(CloudRepository treeInRepository, MPSProject mpsProject, SNode cloudProject) {
    ProjectBinding binding;
    if (cloudProject == null) {
      binding = treeInRepository.addProjectBinding(0L, mpsProject, SyncDirection.TO_CLOUD);
      PersistedBindingConfiguration.getInstance(ProjectHelper.toIdeaProject(mpsProject)).addTransientBoundProject(treeInRepository, cloudProject);
    } else {
      NodeToSNodeAdapter cloudProjectAsNodeToSNodeAdapter = (NodeToSNodeAdapter) ((Object) cloudProject);
      INode cloudProjectAsINode = cloudProjectAsNodeToSNodeAdapter.getWrapped();
      long nodeId = INodeUtils.nodeIdAsLong(cloudProjectAsINode);
      binding = treeInRepository.addProjectBinding(nodeId, mpsProject, SyncDirection.TO_MPS);
      PersistedBindingConfiguration.getInstance(ProjectHelper.toIdeaProject(mpsProject)).addTransientBoundProject(treeInRepository, cloudProject);
    }
    return binding;
  }


  public static void syncInModelixAsIndependentModule(CloudRepository treeInRepository, SModule module, com.intellij.openapi.project.Project mpsProject, INode cloudModuleNode) {

    ModelServerConnection msc = treeInRepository.getModelServer();
    msc.addBinding(treeInRepository.getRepositoryId(), new ProjectModuleBinding(((PNodeAdapter) cloudModuleNode).getNodeId(), module, SyncDirection.TO_MPS));

    PersistedBindingConfiguration.getInstance(mpsProject).addMappedBoundModule(treeInRepository, ((PNodeAdapter) cloudModuleNode));
  }

  /**
   * Take an INode already created and make sure it is exactly the same as the physical module given.
   * The modelMappingConsumer may be used to attach a model synchronizer, for example. It is optional.
   */
  public static void replicatePhysicalModule(final CloudRepository treeInRepository, final INode cloudModule, final SModule physicalModule, final Consumer<PhysicalToCloudModelMapping> modelMappingConsumer, @Nullable ProgressMonitor progress) {
    final Wrappers._T<ProgressMonitor> _progress = new Wrappers._T<ProgressMonitor>(progress);
    if (_progress.value == null) {
      _progress.value = new EmptyProgressMonitor();
    }

    final SModuleAsNode sModuleAsNode = SModuleAsNode.wrap(physicalModule);

    treeInRepository.runWrite(new Consumer<PNodeAdapter>() {
      public void accept(PNodeAdapter rootNode) {
        INodeUtils.copyProperty(cloudModule, sModuleAsNode, PROPS.name$MnvL.getName());
        INodeUtils.copyProperty(cloudModule, sModuleAsNode, PROPS.id$7MjP.getName());
        INodeUtils.copyProperty(cloudModule, sModuleAsNode, PROPS.moduleVersion$sDQK.getName());
        INodeUtils.copyProperty(cloudModule, sModuleAsNode, PROPS.compileInMPS$sEzN.getName());

        INodeUtils.cloneChildren(cloudModule, sModuleAsNode, LINKS.facets$vw9T.getName());
        INodeUtils.cloneChildren(cloudModule, sModuleAsNode, LINKS.dependencies$vC8r.getName());
        INodeUtils.cloneChildren(cloudModule, sModuleAsNode, LINKS.languageDependencies$vKlY.getName());
      }
    });


    final Wrappers._T<List<SModel>> models = new Wrappers._T<List<SModel>>(null);
    physicalModule.getRepository().getModelAccess().runReadAction(new Runnable() {
      public void run() {
        models.value = ListSequence.fromListWithValues(new ArrayList<SModel>(), SModuleUtils.getModelsWithoutDescriptor(physicalModule));
      }
    });
    _progress.value.start("Module " + physicalModule.getModuleName(), ListSequence.fromList(models.value).count());
    for (final SModel model : ListSequence.fromList(models.value)) {
      if (_progress.value.isCanceled()) {
        break;
      }
      physicalModule.getRepository().getModelAccess().runReadAction(new Runnable() {
        public void run() {
          ProgressMonitor modelProgress = _progress.value.subTask(1);
          modelProgress.start("Model " + model.getName(), 1);
          INode cloudModel = copyPhysicalModel(treeInRepository, cloudModule, model);
          if (modelMappingConsumer != null) {
            modelMappingConsumer.accept(new PhysicalToCloudModelMapping(model, cloudModel));
          }
          modelProgress.done();
        }
      });
    }
    _progress.value.done();
  }

  /**
   * This creates a copy of the given physicalModel under the given cloudModule. It then ensures that it is exactly the same as the given physicalModule.
   * 
   * @return the created cloud model
   */
  public static INode copyPhysicalModel(CloudRepository treeInRepository, INode cloudModule, final SModel physicalModel) {
    final INode originalModel = SModelAsNode.wrap(physicalModel);

    INode cloudModel = treeInRepository.createNode(cloudModule, LINKS.models$h3QT, CONCEPTS.Model$2P, new Consumer<INode>() {
      public void accept(INode cloudModel) {
        INodeUtils.copyProperty(cloudModel, originalModel, PROPS.name$MnvL.getName());
        INodeUtils.copyProperty(cloudModel, originalModel, PROPS.id$lDUo.getName());

        INodeUtils.cloneChildren(cloudModel, originalModel, LINKS.modelImports$8DOI.getName());
        INodeUtils.cloneChildren(cloudModel, originalModel, LINKS.usedLanguages$QK4E.getName());

        for (SNode physicalRoot : Sequence.fromIterable(physicalModel.getRootNodes())) {
          INode cloudRoot = cloudModel.addNewChild(LINKS.rootNodes$jxXY.getName(), -1, SConceptAdapter.wrap(physicalRoot.getConcept()));
          replicatePhysicalNode(cloudRoot, physicalRoot);
        }
      }
    });
    return cloudModel;
  }

  /**
   * This takes a cloud node already created and a physical node.
   *  It then ensures that the cloud node is exactly as the original physical node. 
   * 
   * It operates recursively on children.
   */
  private static void replicatePhysicalNode(INode cloudNode, SNode physicalNode) {
    MPSNodeMapping.mapToMpsNode(cloudNode, physicalNode);
    for (SProperty prop : Sequence.fromIterable(physicalNode.getProperties())) {
      cloudNode.setPropertyValue(prop.getName(), physicalNode.getProperty(prop));
    }
    for (SReference ref : Sequence.fromIterable(physicalNode.getReferences())) {
      cloudNode.setReferenceTarget(ref.getRole(), SNodeToNodeAdapter.wrap(ref.getTargetNode()));
    }
    for (SNode physicalChild : Sequence.fromIterable(physicalNode.getChildren())) {
      INode cloudChild = cloudNode.addNewChild(physicalChild.getContainmentLink().getName(), -1, SConceptAdapter.wrap(physicalChild.getConcept()));
      replicatePhysicalNode(cloudChild, physicalChild);
    }
  }

  private static final class PROPS {
    /*package*/ static final SProperty name$MnvL = MetaAdapterFactory.getProperty(0xceab519525ea4f22L, 0x9b92103b95ca8c0cL, 0x110396eaaa4L, 0x110396ec041L, "name");
    /*package*/ static final SProperty id$7MjP = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x3aa34013f2a802e0L, "id");
    /*package*/ static final SProperty moduleVersion$sDQK = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299142L, "moduleVersion");
    /*package*/ static final SProperty compileInMPS$sEzN = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299145L, "compileInMPS");
    /*package*/ static final SProperty id$lDUo = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50cL, 0x244b85440ee67212L, "id");
  }

  private static final class LINKS {
    /*package*/ static final SContainmentLink facets$vw9T = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde953529916cL, "facets");
    /*package*/ static final SContainmentLink dependencies$vC8r = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299179L, "dependencies");
    /*package*/ static final SContainmentLink languageDependencies$vKlY = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299187L, "languageDependencies");
    /*package*/ static final SContainmentLink models$h3QT = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x69652614fd1c512L, "models");
    /*package*/ static final SContainmentLink modelImports$8DOI = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50cL, 0x58dbe6e4d4f32eb8L, "modelImports");
    /*package*/ static final SContainmentLink usedLanguages$QK4E = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50cL, 0x4aaf28cf2092e98eL, "usedLanguages");
    /*package*/ static final SContainmentLink rootNodes$jxXY = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50cL, 0x69652614fd1c514L, "rootNodes");
  }

  private static final class CONCEPTS {
    /*package*/ static final SConcept Model$2P = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50cL, "org.modelix.model.repositoryconcepts.structure.Model");
  }
}
