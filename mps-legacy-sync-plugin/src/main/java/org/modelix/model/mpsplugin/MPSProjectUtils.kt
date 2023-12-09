package org.modelix.model.mpsplugin

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import jetbrains.mps.persistence.DefaultModelRoot
import jetbrains.mps.persistence.MementoImpl
import jetbrains.mps.project.MPSExtentions
import jetbrains.mps.project.MPSProject
import jetbrains.mps.project.ModuleId
import jetbrains.mps.project.ProjectPathUtil
import jetbrains.mps.project.Solution
import jetbrains.mps.project.structure.modules.ModuleFacetDescriptor
import jetbrains.mps.project.structure.modules.SolutionDescriptor
import jetbrains.mps.smodel.GeneralModuleFactory
import jetbrains.mps.smodel.ModuleDependencyVersions
import jetbrains.mps.smodel.language.LanguageRegistry
import jetbrains.mps.vfs.IFile
import org.jetbrains.mps.openapi.module.SModule
import java.io.File
import java.io.IOException

/*Generated by MPS */
object MPSProjectUtils {
    fun createModule(mpsProject: MPSProject?, nameSpace: String?, moduleId: ModuleId?, requestor: Any?): SModule {
        if (nameSpace == null) {
            throw IllegalArgumentException("nameSpace should not be null")
        }

        // A module may already exist in the global repository, but is just not part of the project yet.
        val existingModule: SModule? = mpsProject!!.getRepository().getModule((moduleId)!!)
        if (existingModule != null) {
            mpsProject.addModule(existingModule)
            return existingModule
        }
        val moduleFolder: File = File(mpsProject.getProjectFile(), nameSpace)
        val moduleFolder_: VirtualFile? = LocalFileSystem.getInstance().findFileByIoFile(moduleFolder)
        if (moduleFolder_ != null && moduleFolder_.exists()) {
            try {
                moduleFolder_.delete(requestor)
            } catch (e: IOException) {
                throw RuntimeException("Failed to delete " + moduleFolder_, e)
            }
        }
        val descriptorFile: IFile? = mpsProject.getFileSystem().getFile((mpsProject.getProject().getBasePath())!!)
            .findChild(nameSpace).findChild(nameSpace + MPSExtentions.DOT_SOLUTION)
        if (descriptorFile == null) {
            throw IllegalStateException("descriptor file should not be null")
        }
        val descriptor: SolutionDescriptor = createNewSolutionDescriptor(nameSpace, descriptorFile)
        descriptor.setId(moduleId)
        descriptor.setId(moduleId)
        val module: Solution = GeneralModuleFactory().instantiate(descriptor, descriptorFile) as Solution
        mpsProject.addModule(module)
        ModuleDependencyVersions(
            LanguageRegistry.getInstance(mpsProject.getRepository()),
            mpsProject.getRepository()
        ).update(module)
        module.save()
        return module
    }

    private fun createNewSolutionDescriptor(namespace: String, descriptorFile: IFile): SolutionDescriptor {
        val descriptor: SolutionDescriptor = SolutionDescriptor()
        descriptor.setNamespace(namespace)
        descriptor.setId(ModuleId.regular())
        val moduleLocation: IFile? = descriptorFile.getParent()
        val modelsDir: IFile = moduleLocation!!.findChild("models")
        if (modelsDir.exists() && modelsDir.getChildren()!!.size != 0) {
            throw IllegalStateException("Trying to create a solution in an existing solution's directory: " + moduleLocation)
        } else {
            modelsDir.mkdirs()
            descriptor.getModelRootDescriptors()
                .add(DefaultModelRoot.createDescriptor((modelsDir.getParent())!!, *arrayOf(modelsDir)))
            descriptor.getModuleFacetDescriptors().add(ModuleFacetDescriptor("java", MementoImpl()))
            ProjectPathUtil.setGeneratorOutputPath(descriptor, moduleLocation.findChild("source_gen").getPath())
            return descriptor
        }
    }
}
