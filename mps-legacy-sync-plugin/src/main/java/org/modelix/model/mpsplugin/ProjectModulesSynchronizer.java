package org.modelix.model.mpsplugin;

/*Generated by MPS */

import org.jetbrains.mps.openapi.module.SModule;
import jetbrains.mps.project.MPSProject;
import java.util.List;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import jetbrains.mps.internal.collections.runtime.IWhereFilter;
import jetbrains.mps.project.Solution;
import org.modelix.model.api.ITree;
import jetbrains.mps.internal.collections.runtime.Sequence;
import org.modelix.model.api.IConcept;
import org.modelix.model.mpsadapters.mps.SConceptAdapter;
import org.jetbrains.mps.openapi.module.SModuleId;
import jetbrains.mps.project.ModuleId;
import org.jetbrains.mps.openapi.language.SAbstractConcept;
import jetbrains.mps.module.ModuleDeleteHelper;
import java.util.Collections;
import java.util.Map;
import jetbrains.mps.internal.collections.runtime.MapSequence;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import org.jetbrains.mps.openapi.persistence.PersistenceFacade;
import org.modelix.model.api.IWriteTransaction;
import org.jetbrains.mps.openapi.language.SContainmentLink;
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory;
import org.jetbrains.mps.openapi.language.SConcept;
import org.jetbrains.mps.openapi.language.SProperty;

public class ProjectModulesSynchronizer extends Synchronizer<SModule> {

  private MPSProject project;

  public ProjectModulesSynchronizer(long cloudParentId, MPSProject project) {
    super(cloudParentId, LINKS.modules$Bi3g.getName());
    this.project = project;
  }

  public MPSProject getProject() {
    return project;
  }

  @Override
  protected Iterable<SModule> getMPSChildren() {
    List<SModule> projectModules = project.getProjectModules();
    return ListSequence.fromList(projectModules).where(new IWhereFilter<SModule>() {
      public boolean accept(SModule it) {
        return it instanceof Solution;
      }
    });
  }

  @Override
  protected Iterable<Long> getCloudChildren(final ITree tree) {
    return Sequence.fromIterable(super.getCloudChildren(tree)).where(new IWhereFilter<Long>() {
      public boolean accept(Long it) {
        IConcept concept = tree.getConcept(it);
        return concept.isExactly(SConceptAdapter.wrap(CONCEPTS.Module$4i)) || concept.isSubConceptOf(SConceptAdapter.wrap(CONCEPTS.Solution$q3));
      }
    });
  }

  @Override
  protected SModule createMPSChild(ITree tree, long cloudChildId) {
    SModuleId id = getModuleId(tree, cloudChildId);
    if (id == null) {
      id = ModuleId.foreign("cloud-" + cloudChildId);
    }
    String name = tree.getProperty(cloudChildId, PROPS.name$MnvL.getName());
    IConcept concept = tree.getConcept(cloudChildId);
    return createModule(name, id, cloudChildId, SConceptAdapter.unwrap(concept));
  }

  protected SModule createModule(String name, SModuleId id, long modelNodeId, SAbstractConcept type) {
    if (type.isSubConceptOf(CONCEPTS.Language$qy)) {
      return null;
    } else if (type.isSubConceptOf(CONCEPTS.DevKit$r1)) {
      return null;
    } else {
      return MPSProjectUtils.createModule(project, name, (ModuleId) id, this);
    }
  }

  @Override
  public void removeMPSChild(SModule mpsChild) {
    new ModuleDeleteHelper(project).deleteModules(Collections.singletonList(mpsChild), false, true);
    project.removeModule(mpsChild);
  }

  @Override
  public Map<Long, SModule> associate(ITree tree, List<Long> cloudChildren, List<SModule> mpsChildren, SyncDirection direction) {
    Map<Long, SModule> result = MapSequence.fromMap(new HashMap<Long, SModule>());
    List<SModule> availableModules = ListSequence.fromListWithValues(new ArrayList<SModule>(), mpsChildren);

    for (long cloudModuleId : cloudChildren) {
      SModuleId id = getModuleId(tree, cloudModuleId);
      String name = tree.getProperty(cloudModuleId, PROPS.name$MnvL.getName());

      // There can be modules with duplicate names. That's why we can't just search in a map.
      Iterator<SModule> itr = ListSequence.fromList(availableModules).iterator();
      while (itr.hasNext()) {
        SModule it = itr.next();
        if (id != null && Objects.equals(it.getModuleId(), id) || Objects.equals(it.getModuleName(), name)) {
          MapSequence.fromMap(result).put(cloudModuleId, it);
          itr.remove();
          break;
        }
      }
    }

    return result;
  }

  protected SModuleId getModuleId(ITree tree, long cloudModuleId) {
    String serializedId = tree.getProperty(cloudModuleId, PROPS.id$7MjP.getName());
    if ((serializedId == null || serializedId.length() == 0)) {
      return null;
    }
    return PersistenceFacade.getInstance().createModuleId(serializedId);
  }

  @Override
  public long createCloudChild(IWriteTransaction t, SModule mpsChild) {
    long modelNodeId = t.addNewChild(getCloudParentId(), LINKS.modules$Bi3g.getName(), -1, SConceptAdapter.wrap(CONCEPTS.Module$4i));
    t.setProperty(modelNodeId, PROPS.id$7MjP.getName(), mpsChild.getModuleId().toString());
    t.setProperty(modelNodeId, PROPS.name$MnvL.getName(), mpsChild.getModuleName());
    return modelNodeId;
  }

  private static final class LINKS {
    /*package*/ static final SContainmentLink modules$Bi3g = MetaAdapterFactory.getContainmentLink(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x37a0917d689de959L, 0x37a0917d689de9e2L, "modules");
  }

  private static final class CONCEPTS {
    /*package*/ static final SConcept Module$4i = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, "org.modelix.model.repositoryconcepts.structure.Module");
    /*package*/ static final SConcept Solution$q3 = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x65e0d25ff052e203L, "org.modelix.model.repositoryconcepts.structure.Solution");
    /*package*/ static final SConcept DevKit$r1 = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x65e0d25ff052e205L, "org.modelix.model.repositoryconcepts.structure.DevKit");
    /*package*/ static final SConcept Language$qy = MetaAdapterFactory.getConcept(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x65e0d25ff052e204L, "org.modelix.model.repositoryconcepts.structure.Language");
  }

  private static final class PROPS {
    /*package*/ static final SProperty name$MnvL = MetaAdapterFactory.getProperty(0xceab519525ea4f22L, 0x9b92103b95ca8c0cL, 0x110396eaaa4L, 0x110396ec041L, "name");
    /*package*/ static final SProperty id$7MjP = MetaAdapterFactory.getProperty(0xa7577d1d4e5431dL, 0x98b1fae38f9aee80L, 0x69652614fd1c50fL, 0x3aa34013f2a802e0L, "id");
  }
}
