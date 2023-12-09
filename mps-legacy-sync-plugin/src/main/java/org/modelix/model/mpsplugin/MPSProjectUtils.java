package org.modelix.model.mpsplugin;

/*Generated by MPS */

import jetbrains.mps.internal.collections.runtime.ListSequence;
import jetbrains.mps.persistence.DefaultModelRoot;
import jetbrains.mps.persistence.MementoImpl;
import jetbrains.mps.project.*;
import jetbrains.mps.project.structure.modules.ModuleFacetDescriptor;
import jetbrains.mps.smodel.ModuleDependencyVersions;
import jetbrains.mps.smodel.language.LanguageRegistry;
import org.jetbrains.mps.openapi.module.SModule;

import java.io.File;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import java.io.IOException;
import java.util.List;

import jetbrains.mps.vfs.IFile;
import de.slisson.mps.reflection.runtime.ReflectionUtil;
import jetbrains.mps.ide.newSolutionDialog.NewModuleUtil;
import jetbrains.mps.project.structure.modules.SolutionDescriptor;
import jetbrains.mps.smodel.GeneralModuleFactory;

public class MPSProjectUtils {
  public MPSProjectUtils() {
  }
  public static SModule createModule(final MPSProject mpsProject, String nameSpace, ModuleId moduleId, Object requestor) {
    if (nameSpace == null) {
      throw new IllegalArgumentException("nameSpace should not be null");
    }

    // A module may already exist in the global repository, but is just not part of the project yet.
    SModule existingModule = mpsProject.getRepository().getModule(moduleId);
    if (existingModule != null) {
      mpsProject.addModule(existingModule);
      return existingModule;
    }

    File moduleFolder = new File(mpsProject.getProjectFile(), nameSpace);
    VirtualFile moduleFolder_ = LocalFileSystem.getInstance().findFileByIoFile(moduleFolder);
    if (moduleFolder_ != null && moduleFolder_.exists()) {
      try {
        moduleFolder_.delete(requestor);
      } catch (IOException e) {
        throw new RuntimeException("Failed to delete " + moduleFolder_, e);
      }
    }

    IFile descriptorFile = mpsProject.getFileSystem().getFile(mpsProject.getProject().getBasePath())
            .findChild(nameSpace).findChild(nameSpace + MPSExtentions.DOT_SOLUTION);
    if (descriptorFile == null) {
      throw new IllegalStateException("descriptor file should not be null");
    }
    SolutionDescriptor descriptor = createNewSolutionDescriptor(nameSpace, descriptorFile);
    descriptor.setId(moduleId);

    descriptor.setId(moduleId);
    Solution module = (Solution) new GeneralModuleFactory().instantiate(descriptor, descriptorFile);
    mpsProject.addModule(module);
    new ModuleDependencyVersions(LanguageRegistry.getInstance(mpsProject.getRepository()), mpsProject.getRepository()).update(module);
    module.save();
    return module;
  }

  private static SolutionDescriptor createNewSolutionDescriptor(String namespace, IFile descriptorFile) {
    SolutionDescriptor descriptor = new SolutionDescriptor();
    descriptor.setNamespace(namespace);
    descriptor.setId(ModuleId.regular());
    IFile moduleLocation = descriptorFile.getParent();
    IFile modelsDir = moduleLocation.findChild("models");
    if (modelsDir.exists() && modelsDir.getChildren().size() != 0) {
      throw new IllegalStateException("Trying to create a solution in an existing solution's directory: " + moduleLocation);
    } else {
      modelsDir.mkdirs();
      descriptor.getModelRootDescriptors().add(DefaultModelRoot.createDescriptor(modelsDir.getParent(), new IFile[]{modelsDir}));
      descriptor.getModuleFacetDescriptors().add(new ModuleFacetDescriptor("java", new MementoImpl()));
      ProjectPathUtil.setGeneratorOutputPath(descriptor, moduleLocation.findChild("source_gen").getPath());
      return descriptor;
    }
  }
}
