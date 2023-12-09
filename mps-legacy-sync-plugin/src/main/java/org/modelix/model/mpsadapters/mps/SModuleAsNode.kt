package org.modelix.model.mpsadapters.mps;

/*Generated by MPS */

import jetbrains.mps.baseLanguage.closures.runtime._FunctionTypes;
import org.jetbrains.mps.openapi.module.SModule;
import jetbrains.mps.smodel.tempmodel.TempModule;
import jetbrains.mps.smodel.tempmodel.TempModule2;
import java.util.List;
import jetbrains.mps.project.Project;
import jetbrains.mps.project.ProjectManager;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import jetbrains.mps.project.MPSProject;
import jetbrains.mps.internal.collections.runtime.ISelector;
import jetbrains.mps.internal.collections.runtime.NotNullWhereFilter;
import jetbrains.mps.project.structure.modules.ModuleDescriptor;
import jetbrains.mps.project.AbstractModule;
import jetbrains.mps.project.DevKit;
import org.modelix.model.api.INode;
import org.jetbrains.mps.openapi.model.SModel;
import jetbrains.mps.internal.collections.runtime.Sequence;
import org.jetbrains.mps.openapi.module.SModuleFacet;
import jetbrains.mps.internal.collections.runtime.IWhereFilter;
import jetbrains.mps.project.facets.JavaModuleFacet;
import java.util.Collections;
import java.util.LinkedList;
import jetbrains.mps.internal.collections.runtime.IMapping;
import org.jetbrains.mps.openapi.module.SModuleReference;
import jetbrains.mps.internal.collections.runtime.MapSequence;
import org.jetbrains.mps.openapi.language.SLanguage;
import jetbrains.mps.internal.collections.runtime.CollectionSequence;
import org.jetbrains.mps.openapi.module.SDependencyScope;
import org.jetbrains.mps.openapi.module.SModuleId;
import jetbrains.mps.project.Solution;
import jetbrains.mps.project.structure.modules.SolutionDescriptor;
import jetbrains.mps.project.structure.modules.Dependency;
import java.util.Objects;
import org.jetbrains.mps.openapi.module.SDependency;
import org.jetbrains.annotations.NotNull;
import org.modelix.model.area.IArea;
import org.modelix.model.api.IConcept;
import org.modelix.model.api.INodeReference;
import org.jetbrains.annotations.Nullable;
import jetbrains.mps.baseLanguage.closures.runtime.Wrappers;
import org.jetbrains.mps.openapi.module.SRepository;
import jetbrains.mps.smodel.MPSModuleRepository;
import jetbrains.mps.project.structure.project.ModulePath;
import org.jetbrains.mps.openapi.language.SConcept;
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory;
import org.jetbrains.mps.openapi.language.SContainmentLink;
import org.jetbrains.mps.openapi.language.SProperty;
import org.modelix.model.mpsplugin.SModuleUtils;

public class SModuleAsNode extends TreeElementAsNode<SModule> {
  public static boolean isTempModule(SModule module) {
    return module instanceof TempModule || module instanceof TempModule2;
  }

  private static TreeElementAsNode.IPropertyAccessor<SModule> nameAccessor = new ReadOnlyPropertyAccessor<SModule>() {
    public String get(SModule element) {
      return element.getModuleName();
    }
  };

  private static TreeElementAsNode.IPropertyAccessor<SModule> idAccessor = new ReadOnlyPropertyAccessor<SModule>() {
    public String get(SModule element) {
      return element.getModuleId().toString();
    }
  };
  private static TreeElementAsNode.IPropertyAccessor<SModule> virtualFolderAccessor = new ReadOnlyPropertyAccessor<SModule>() {
    public String get(final SModule element) {
      List<Project> projects = ProjectManager.getInstance().getOpenedProjects();
      String value = ListSequence.fromList(projects).ofType(MPSProject.class).select(new ISelector<MPSProject, String>() {
        public String select(MPSProject it) {
          return check_jbj149_a0a0a0a0b0a0a0f(it.getPath(element));
        }
      }).where((_FunctionTypes._return_P1_E0<Boolean, String>) (_FunctionTypes._return_P1_E0) new NotNullWhereFilter()).first();
      if ("".equals(value)) return null; // default value is returned as not being set to avoid unnecessary synchronization
      return value;
    }
  };

  private static TreeElementAsNode.IPropertyAccessor<SModule> moduleVersionAccessor = new ReadOnlyPropertyAccessor<SModule>() {
    public String get(SModule element) {
      ModuleDescriptor moduleDescriptor = ((AbstractModule) element).getModuleDescriptor();
      if (element instanceof AbstractModule) {
        int version = check_jbj149_a0a0b0a0a0h(moduleDescriptor);
        if (version == 0) return null; // default value is returned as not being set to avoid unnecessary synchronization
        return Integer.toString(version);
      } else {
        return Integer.toString(0);
      }
    }
  };

  private static TreeElementAsNode.IPropertyAccessor<SModule> compileInMPSAccessor = new ReadOnlyPropertyAccessor<SModule>() {
    public String get(SModule element) {
      if (element instanceof DevKit) {
        return Boolean.toString(false);
      }
      if (element instanceof AbstractModule) {
        try {
          boolean value = check_jbj149_a0a0a0b0a0a0j(((AbstractModule) element).getModuleDescriptor());
          if (value) return null; // default value is returned as not being set to avoid unnecessary synchronization
          return Boolean.toString(value);
        } catch (UnsupportedOperationException uoe) {
          return Boolean.toString(false);
        }
      } else {
        return Boolean.toString(false);
      }
    }
  };

  private static TreeElementAsNode.IChildAccessor<SModule> modelsAccessor = new TreeElementAsNode.IChildAccessor<SModule>() {
    public Iterable<INode> get(SModule module) {
      Iterable<SModel> models = SModuleUtils.getModelsWithoutDescriptor(module);
      return Sequence.fromIterable(models).select(new ISelector<SModel, SModelAsNode>() {
        public SModelAsNode select(SModel it) {
          return new SModelAsNode(it);
        }
      });
    }
  };

  private static TreeElementAsNode.IChildAccessor<SModule> facetsAccessor = new TreeElementAsNode.IChildAccessor<SModule>() {
    public Iterable<INode> get(SModule module) {
      Iterable<SModuleFacet> facets = module.getFacets();
      // TODO We ignore facets which are not JavaModuleFacet. In the future we may need to process those too
      return Sequence.fromIterable(facets).where(new IWhereFilter<SModuleFacet>() {
        public boolean accept(SModuleFacet it) {
          return it instanceof JavaModuleFacet;
        }
      }).select(new ISelector<SModuleFacet, JavaModuleFacetAsNode>() {
        public JavaModuleFacetAsNode select(SModuleFacet it) {
          return new JavaModuleFacetAsNode(((JavaModuleFacet) it));
        }
      });
    }
  };

  private static TreeElementAsNode.IChildAccessor<SModule> dependenciesAccessor = new TreeElementAsNode.IChildAccessor<SModule>() {
    public Iterable<INode> get(SModule module) {
      if (!((module instanceof AbstractModule))) {
        return Sequence.fromIterable(Collections.<INode>emptyList());
      }
      ModuleDescriptor moduleDescriptor = ((AbstractModule) module).getModuleDescriptor();
      List<INode> deps = ListSequence.fromList(new LinkedList<INode>());
      if (moduleDescriptor == null) {
        return null;
      }
      for (IMapping<SModuleReference, Integer> depVersion : MapSequence.fromMap(moduleDescriptor.getDependencyVersions())) {
        ListSequence.fromList(deps).addElement(new ModuleDependencyAsNode(depVersion.key(), depVersion.value(), isDirectDependency(module, depVersion.key().getModuleId()), isReexport(module, depVersion.key().getModuleId()), module, getDependencyScope(module, depVersion.key().getModuleId())));
      }
      return deps;
    }
  };

  private static TreeElementAsNode.IChildAccessor<SModule> languageDependenciesAccessor = new TreeElementAsNode.IChildAccessor<SModule>() {
    public Iterable<INode> get(SModule module) {
      if (!((module instanceof AbstractModule))) {
        return Sequence.fromIterable(Collections.<INode>emptyList());
      }
      ModuleDescriptor moduleDescriptor = ((AbstractModule) module).getModuleDescriptor();
      List<INode> deps = ListSequence.fromList(new LinkedList<INode>());
      if (moduleDescriptor != null) {
        for (IMapping<SLanguage, Integer> depVersion : MapSequence.fromMap(moduleDescriptor.getLanguageVersions())) {
          ListSequence.fromList(deps).addElement(new SingleLanguageDependencyAsNode(depVersion.key().getSourceModuleReference(), depVersion.value(), module));
        }
        for (SModuleReference devKit : CollectionSequence.fromCollection(moduleDescriptor.getUsedDevkits())) {
          ListSequence.fromList(deps).addElement(new DevKitDependencyAsNode(devKit, module));
        }
      }
      return deps;
    }
  };

  private static SDependencyScope getDependencyScope(SModule module, SModuleId moduleId) {
    if (module instanceof Solution) {
      // This gives slightly different results than the getDeclaredDepencies
      Solution solution = ((Solution) module);
      SolutionDescriptor solutionDescriptor = solution.getModuleDescriptor();
      for (Dependency dep : CollectionSequence.fromCollection(solutionDescriptor.getDependencies())) {
        if (Objects.equals(dep.getModuleRef().getModuleId(), moduleId)) {
          return dep.getScope();
        }
      }
      return null;
    }
    for (SDependency declaredDep : Sequence.fromIterable(module.getDeclaredDependencies())) {
      if (Objects.equals(declaredDep.getTargetModule().getModuleId(), moduleId)) {
        return declaredDep.getScope();
      }
    }
    return null;
  }
  private static boolean isDirectDependency(SModule module, SModuleId moduleId) {
    if (module instanceof Solution) {
      // This gives slightly different results than the getDeclaredDepencies
      Solution solution = ((Solution) module);
      SolutionDescriptor solutionDescriptor = solution.getModuleDescriptor();
      for (Dependency dep : CollectionSequence.fromCollection(solutionDescriptor.getDependencies())) {
        if (Objects.equals(dep.getModuleRef().getModuleId(), moduleId)) {
          return true;
        }
      }
      return false;
    }
    for (SDependency declaredDep : Sequence.fromIterable(module.getDeclaredDependencies())) {
      if (Objects.equals(declaredDep.getTargetModule().getModuleId(), moduleId)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isReexport(SModule module, SModuleId moduleId) {
    for (SDependency declaredDep : Sequence.fromIterable(module.getDeclaredDependencies())) {
      if (Objects.equals(declaredDep.getTargetModule().getModuleId(), moduleId)) {
        return declaredDep.isReexport();
      }
    }
    return false;
  }

  public static SModuleAsNode wrap(SModule module) {
    return (module == null ? null : new SModuleAsNode(module));
  }

  public SModuleAsNode(@NotNull SModule module) {
    super(module);
  }

  @NotNull
  @Override
  public IArea getArea() {
    return new MPSArea(getElement().getRepository());
  }

  @Override
  public IConcept getConcept() {
    return SConceptAdapter.wrap(CONCEPTS.Module$4i);
  }

  @Override
  protected TreeElementAsNode.IChildAccessor<SModule> getChildAccessor(String role) {
    if (role.equals(LINKS.models$h3QT.getName())) {
      return modelsAccessor;
    }
    if (role.equals(LINKS.facets$vw9T.getName())) {
      return facetsAccessor;
    }
    if (role.equals(LINKS.dependencies$vC8r.getName())) {
      return dependenciesAccessor;
    }
    if (role.equals(LINKS.languageDependencies$vKlY.getName())) {
      return languageDependenciesAccessor;
    }
    return super.getChildAccessor(role);
  }
  @Override
  protected TreeElementAsNode.IPropertyAccessor<SModule> getPropertyAccessor(String role) {
    if (role.equals(PROPS.name$MnvL.getName())) {
      return nameAccessor;
    }
    if (role.equals(PROPS.id$7MjP.getName())) {
      return idAccessor;
    }
    if (role.equals(PROPS.moduleVersion$sDQK.getName())) {
      return moduleVersionAccessor;
    }
    if (role.equals(PROPS.compileInMPS$sEzN.getName())) {
      return compileInMPSAccessor;
    }
    if (role.equals(PROPS.virtualPackage$EkXl.getName())) {
      return virtualFolderAccessor;
    }
    return super.getPropertyAccessor(role);
  }

  @Override
  protected TreeElementAsNode.IReferenceAccessor<SModule> getReferenceAccessor(String role) {
    return super.getReferenceAccessor(role);
  }

  @Override
  public INode getParent() {
    if (!(SRepositoryAsNode.isVisible(getElement()))) {
      return null;
    }
    return new SRepositoryAsNode(getElement().getRepository());
  }

  @Override
  public String getRoleInParent() {
    if (!(SRepositoryAsNode.isVisible(getElement()))) {
      return null;
    }
    return ((isTempModule(getElement()) ? LINKS.tempModules$Zqoa : LINKS.modules$jBPn)).getName();
  }

  @NotNull
  @Override
  public INodeReference getReference() {
    return new NodeReference(getElement().getModuleReference());
  }

  @Nullable
  public ModuleDependencyAsNode findDependency(SModuleId dependencyId) {
    if (!((getElement() instanceof AbstractModule))) {
      return null;
    }
    ModuleDescriptor moduleDescriptor = ((AbstractModule) getElement()).getModuleDescriptor();
    if (moduleDescriptor == null) {
      return null;
    }
    for (IMapping<SModuleReference, Integer> entry : MapSequence.fromMap(moduleDescriptor.getDependencyVersions())) {
      if (Objects.equals(entry.key().getModuleId(), dependencyId)) {
        return new ModuleDependencyAsNode(entry.key(), entry.value(), isDirectDependency(getElement(), entry.key().getModuleId()), isReexport(getElement(), entry.key().getModuleId()), getElement(), getDependencyScope(getElement(), entry.key().getModuleId()));
      }
    }
    return null;
  }

  @Nullable
  public SingleLanguageDependencyAsNode findSingleLanguageDependency(SModuleId dependencyId) {
    if (!((getElement() instanceof AbstractModule))) {
      return null;
    }
    ModuleDescriptor moduleDescriptor = ((AbstractModule) getElement()).getModuleDescriptor();
    if (moduleDescriptor != null) {
      for (IMapping<SModuleReference, Integer> entry : MapSequence.fromMap(moduleDescriptor.getDependencyVersions())) {
        if (Objects.equals(entry.key().getModuleId(), dependencyId)) {
          return new SingleLanguageDependencyAsNode(entry.key(), entry.value(), getElement());
        }
      }
    }
    return null;
  }
  @Nullable
  public DevKitDependencyAsNode findDevKitDependency(SModuleId dependencyId) {
    if (!((getElement() instanceof AbstractModule))) {
      return null;
    }
    ModuleDescriptor moduleDescriptor = ((AbstractModule) getElement()).getModuleDescriptor();
    if (moduleDescriptor != null) {
      for (SModuleReference devKit : CollectionSequence.fromCollection(moduleDescriptor.getUsedDevkits())) {
        if (Objects.equals(devKit.getModuleId(), dependencyId)) {
          return new DevKitDependencyAsNode(devKit, getElement());
        }
      }
    }
    return null;
  }

  public static class NodeReference implements INodeReference {
    private SModuleReference moduleRef;

    public NodeReference(SModuleReference moduleRef) {
      this.moduleRef = moduleRef;
    }

    public SModuleReference getModuleRef() {
      return this.moduleRef;
    }

    @NotNull
    @Override
    public String serialize() {
      return "mps-module:" + moduleRef;
    }

    @Nullable
    @Override
    public INode resolveNode(@Nullable IArea area) {
      final Wrappers._T<SRepository> repo = new Wrappers._T<SRepository>(null);
      if (area != null) {
        List<IArea> areas = area.collectAreas();
        repo.value = ListSequence.fromList(areas).ofType(MPSArea.class).select(new ISelector<MPSArea, SRepository>() {
          public SRepository select(MPSArea it) {
            return it.getRepository();
          }
        }).where((_FunctionTypes._return_P1_E0<Boolean, SRepository>) (_FunctionTypes._return_P1_E0) new NotNullWhereFilter()).first();
      }
      if (repo.value == null) {
        repo.value = MPSModuleRepository.getInstance();
      }

      final Wrappers._T<SModule> resolved = new Wrappers._T<SModule>(null);
      if (repo.value.getModelAccess().canRead()) {
        resolved.value = moduleRef.resolve(repo.value);
      } else {
        repo.value.getModelAccess().runReadAction(new Runnable() {
          public void run() {
            resolved.value = moduleRef.resolve(repo.value);
          }
        });
      }
      return (resolved.value == null ? null : new SModuleAsNode(resolved.value));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || this.getClass() != o.getClass()) {
        return false;
      }

      NodeReference that = (NodeReference) o;
      if ((moduleRef != null ? !(moduleRef.equals(that.moduleRef)) : that.moduleRef != null)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = 0;
      result = 31 * result + ((moduleRef != null ? ((Object) moduleRef).hashCode() : 0));
      return result;
    }
  }
  private static String check_jbj149_a0a0a0a0b0a0a0f(ModulePath checkedDotOperand) {
    if (null != checkedDotOperand) {
      return checkedDotOperand.getVirtualFolder();
    }
    return null;
  }
  private static int check_jbj149_a0a0b0a0a0h(ModuleDescriptor checkedDotOperand) {
    if (null != checkedDotOperand) {
      return checkedDotOperand.getModuleVersion();
    }
    return 0;
  }
  private static boolean check_jbj149_a0a0a0b0a0a0j(ModuleDescriptor checkedDotOperand) {
    if (null != checkedDotOperand) {
      return checkedDotOperand.getCompileInMPS();
    }
    return false;
  }

  private static final class CONCEPTS {
    /*package*/ static final SConcept Module$4i = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, "org.modelix.model.repositoryconcepts.structure.Module");
  }

  private static final class LINKS {
    /*package*/ static final SContainmentLink models$h3QT = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x69652614fd1c512L, "models");
    /*package*/ static final SContainmentLink facets$vw9T = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde953529916cL, "facets");
    /*package*/ static final SContainmentLink dependencies$vC8r = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299179L, "dependencies");
    /*package*/ static final SContainmentLink languageDependencies$vKlY = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299187L, "languageDependencies");
    /*package*/ static final SContainmentLink modules$jBPn = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c516L, 0x69652614fd1c517L, "modules");
    /*package*/ static final SContainmentLink tempModules$Zqoa = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c516L, 0x72291b7f31486ecaL, "tempModules");
  }

  private static final class PROPS {
    /*package*/ static final SProperty name$MnvL = MetaAdapterFactory.getProperty(0xceab519525ea4f22L, 0x9b92103b95ca8c0cL, 0x110396eaaa4L, 0x110396ec041L, "name");
    /*package*/ static final SProperty id$7MjP = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x3aa34013f2a802e0L, "id");
    /*package*/ static final SProperty moduleVersion$sDQK = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299142L, "moduleVersion");
    /*package*/ static final SProperty compileInMPS$sEzN = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x1e9fde9535299145L, "compileInMPS");
    /*package*/ static final SProperty virtualPackage$EkXl = MetaAdapterFactory.getProperty(0xceab519525ea4f22L, 0x9b92103b95ca8c0cL, 0x10802efe25aL, 0x115eca8579fL, "virtualPackage");
  }
}