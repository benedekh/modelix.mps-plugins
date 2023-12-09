package org.modelix.model.mpsplugin

import de.slisson.mps.reflection.runtime.ReflectionUtil
import jetbrains.mps.extapi.model.SModelDescriptorStub
import jetbrains.mps.internal.collections.runtime.IWhereFilter
import jetbrains.mps.internal.collections.runtime.ListSequence
import jetbrains.mps.internal.collections.runtime.Sequence
import jetbrains.mps.project.DevKit
import jetbrains.mps.project.ModuleId
import jetbrains.mps.project.Project
import jetbrains.mps.project.ProjectManager
import jetbrains.mps.project.structure.modules.ModuleReference
import jetbrains.mps.smodel.DefaultSModelDescriptor
import jetbrains.mps.smodel.Language
import jetbrains.mps.smodel.ModelImports
import jetbrains.mps.smodel.ModuleDependencyVersions
import jetbrains.mps.smodel.SModelReference
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory
import jetbrains.mps.smodel.language.LanguageRegistry
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.mps.openapi.language.SConcept
import org.jetbrains.mps.openapi.language.SContainmentLink
import org.jetbrains.mps.openapi.language.SLanguage
import org.jetbrains.mps.openapi.language.SProperty
import org.jetbrains.mps.openapi.language.SReferenceLink
import org.jetbrains.mps.openapi.model.SModel
import org.jetbrains.mps.openapi.model.SModelId
import org.jetbrains.mps.openapi.module.SModuleReference
import org.jetbrains.mps.openapi.module.SRepository
import org.modelix.model.api.IBranch
import org.modelix.model.api.INode
import org.modelix.model.api.ITree
import org.modelix.model.api.PNodeAdapter
import org.modelix.model.api.PNodeAdapter.Companion.wrap
import org.modelix.model.area.PArea
import org.modelix.model.mpsadapters.mps.DevKitDependencyAsNode
import org.modelix.model.mpsadapters.mps.ModelImportAsNode
import org.modelix.model.mpsadapters.mps.SModelAsNode
import org.modelix.model.mpsadapters.mps.SingleLanguageDependencyAsNode
import java.util.Objects
import java.util.UUID

/*Generated by MPS */ /*package*/
internal class ModelPropertiesSynchronizer(
    protected var modelNodeId: Long,
    protected var model: SModel?,
    private val cloudRepository: ICloudRepository?
) {
    private val branch: IBranch?
        private get() {
            return cloudRepository?.branch
        }

    fun syncModelPropertiesFromMPS() {
        PArea((branch)!!).executeWrite({
            syncUsedLanguagesAndDevKitsFromMPS()
            syncModelImportsFromMPS()
            Unit
        })
    }

    fun syncModelPropertiesToMPS(tree: ITree?, cloudRepository: ICloudRepository) {
        syncModelPropertiesToMPS(tree, model, modelNodeId, cloudRepository)
    }

    fun syncUsedLanguagesAndDevKitsFromMPS() {
        PArea((branch)!!).executeWrite<Unit>({

            // First get the dependencies in MPS
            val mpsModelNode: SModelAsNode? = SModelAsNode.Companion.wrap(model)
            val dependenciesInMPS: List<INode?>? = IterableOfINodeUtils.toList(
                mpsModelNode!!.getChildren(LINKS.`usedLanguages$QK4E`.getName())
            )

            //  Then get the dependencies in the cloud
            val branch: IBranch? = branch
            val cloudModelNode: INode = PNodeAdapter(modelNodeId, (branch)!!)
            val dependenciesInCloud: Iterable<INode> = cloudModelNode.getChildren(LINKS.`usedLanguages$QK4E`.getName())

            // For each import in MPS, add it if not present in the cloud, or otherwise ensure all properties are the same
            for (dependencyInMPS: INode? in ListSequence.fromList<INode?>(dependenciesInMPS)) {
                if (dependencyInMPS is DevKitDependencyAsNode) {
                    val matchingDependencyInCloud: INode? =
                        Sequence.fromIterable<INode>(dependenciesInCloud).findFirst(object : IWhereFilter<INode>() {
                            public override fun accept(dependencyInCloud: INode): Boolean {
                                return Objects.equals(
                                    dependencyInMPS.getPropertyValue(PROPS.`uuid$lpJp`.getName()),
                                    dependencyInCloud.getPropertyValue(
                                        PROPS.`uuid$lpJp`.getName()
                                    )
                                )
                            }
                        })
                    if (matchingDependencyInCloud == null) {
                        INodeUtils.replicateChild(cloudModelNode, LINKS.`usedLanguages$QK4E`.getName(), dependencyInMPS)
                    } else {
                        INodeUtils.copyProperty(cloudModelNode, dependencyInMPS, PROPS.`name$lpYq`)
                    }
                } else if (dependencyInMPS is SingleLanguageDependencyAsNode) {
                    val matchingDependencyInCloud: INode? =
                        Sequence.fromIterable<INode>(dependenciesInCloud).findFirst(object : IWhereFilter<INode>() {
                            public override fun accept(dependencyInCloud: INode): Boolean {
                                return Objects.equals(
                                    dependencyInMPS.getPropertyValue(PROPS.`uuid$lpJp`.getName()),
                                    dependencyInCloud.getPropertyValue(
                                        PROPS.`uuid$lpJp`.getName()
                                    )
                                )
                            }
                        })
                    if (matchingDependencyInCloud == null) {
                        INodeUtils.replicateChild(cloudModelNode, LINKS.`usedLanguages$QK4E`.getName(), dependencyInMPS)
                    } else {
                        INodeUtils.copyProperty(cloudModelNode, dependencyInMPS, PROPS.`name$lpYq`)
                        INodeUtils.copyProperty(cloudModelNode, dependencyInMPS, PROPS.`version$ApUL`)
                    }
                } else {
                    throw RuntimeException("Unknown dependency type: " + dependencyInMPS!!.javaClass.getName())
                }
            }

            // For each import not in MPS, remove it
            for (dependencyInCloud: INode in Sequence.fromIterable<INode>(dependenciesInCloud)) {
                val matchingDependencyInMPS: INode? =
                    Sequence.fromIterable<INode>(dependenciesInCloud).findFirst(object : IWhereFilter<INode>() {
                        public override fun accept(dependencyInMPS: INode): Boolean {
                            return Objects.equals(
                                dependencyInCloud.getPropertyValue(PROPS.`uuid$lpJp`.getName()),
                                dependencyInMPS.getPropertyValue(
                                    PROPS.`uuid$lpJp`.getName()
                                )
                            )
                        }
                    })
                if (matchingDependencyInMPS == null) {
                    cloudModelNode.removeChild(dependencyInCloud)
                }
            }
            Unit
        })
    }

    fun syncModelImportsFromMPS() {
        PArea((branch)!!).executeWrite<Unit>({

            // First get the dependencies in MPS. Model imports do not include implicit ones
            val mpsModelNode: SModelAsNode? = SModelAsNode.Companion.wrap(model)
            val dependenciesInMPS: List<ModelImportAsNode?>? = IterableOfINodeUtils.toCastedList(
                mpsModelNode!!.getChildren(LINKS.`modelImports$8DOI`.getName())
            )

            //  Then get the dependencies in the cloud
            val branch: IBranch? = branch
            val cloudModelNode: INode = PNodeAdapter(modelNodeId, (branch)!!)
            val dependenciesInCloud: Iterable<INode> = cloudModelNode.getChildren(LINKS.`modelImports$8DOI`.getName())

            // For each import in MPS, add it if not present in the cloud, or otherwise ensure all properties are the same
            for (dependencyInMPS: ModelImportAsNode? in ListSequence.fromList<ModelImportAsNode?>(dependenciesInMPS)) {
                val modelImportedInMps: INode? = dependencyInMPS!!.getReferenceTarget(LINKS.`model$GJHn`.getName())
                if (modelImportedInMps != null) {
                    val modelIDimportedInMPS: String? = modelImportedInMps.getPropertyValue(PROPS.`id$lDUo`.getName())
                    val matchingDependencyInCloud: INode? =
                        Sequence.fromIterable<INode>(dependenciesInCloud).findFirst(object : IWhereFilter<INode>() {
                            public override fun accept(dependencyInCloud: INode): Boolean {
                                val modelImportedInCloud: INode? =
                                    dependencyInCloud.getReferenceTarget(LINKS.`model$GJHn`.getName())
                                if (modelImportedInCloud == null) {
                                    return false
                                }
                                val modelIDimportedInCloud: String? =
                                    modelImportedInCloud!!.getPropertyValue(PROPS.`id$lDUo`.getName())
                                return Objects.equals(modelIDimportedInMPS, modelIDimportedInCloud)
                            }
                        })
                    if (matchingDependencyInCloud == null) {
                        INodeUtils.replicateChild(cloudModelNode, LINKS.`modelImports$8DOI`.getName(), dependencyInMPS)
                    } else {
                        // no properties to set here
                    }
                }
            }

            // For each import not in MPS, remove it
            for (dependencyInCloud: INode in Sequence.fromIterable<INode>(dependenciesInCloud)) {
                val modelImportedInCloud: INode? = dependencyInCloud.getReferenceTarget(LINKS.`model$GJHn`.getName())
                if (modelImportedInCloud != null) {
                    val modelIDimportedInCloud: String? =
                        modelImportedInCloud.getPropertyValue(PROPS.`id$lDUo`.getName())
                    val matchingDependencyInMPS: INode? =
                        Sequence.fromIterable<INode>(dependenciesInCloud).findFirst(object : IWhereFilter<INode>() {
                            public override fun accept(dependencyInMPS: INode): Boolean {
                                val modelImportedInMPS: INode? =
                                    dependencyInMPS.getReferenceTarget(LINKS.`model$GJHn`.getName())
                                if (modelImportedInMPS == null) {
                                    return false
                                }
                                val modelIDimportedInMPS: String? =
                                    modelImportedInMPS!!.getPropertyValue(PROPS.`id$lDUo`.getName())
                                return Objects.equals(modelIDimportedInCloud, modelIDimportedInMPS)
                            }
                        })
                    if (matchingDependencyInMPS == null) {
                        cloudModelNode.removeChild(dependencyInCloud)
                    }
                }
            }
            Unit
        })
    }

    private object LINKS {
        /*package*/
        val `usedLanguages$QK4E`: SContainmentLink = MetaAdapterFactory.getContainmentLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x4aaf28cf2092e98eL,
            "usedLanguages"
        )

        /*package*/
        val `modelImports$8DOI`: SContainmentLink = MetaAdapterFactory.getContainmentLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x58dbe6e4d4f32eb8L,
            "modelImports"
        )

        /*package*/
        val `model$GJHn`: SReferenceLink = MetaAdapterFactory.getReferenceLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x58dbe6e4d4f332a3L,
            0x58dbe6e4d4f332a4L,
            "model"
        )
    }

    private object PROPS {
        /*package*/
        val `uuid$lpJp`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x7c527144386aca0fL,
            0x7c527144386aca12L,
            "uuid"
        )

        /*package*/
        val `name$lpYq`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x7c527144386aca0fL,
            0x7c527144386aca13L,
            "name"
        )

        /*package*/
        val `version$ApUL`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x1e9fde953529917dL,
            0x1e9fde9535299183L,
            "version"
        )

        /*package*/
        val `id$lDUo`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x244b85440ee67212L,
            "id"
        )

        /*package*/
        val `name$MnvL`: SProperty = MetaAdapterFactory.getProperty(
            -0x3154ae6ada15b0deL,
            -0x646defc46a3573f4L,
            0x110396eaaa4L,
            0x110396ec041L,
            "name"
        )

        /*package*/
        val `id$7MjP`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50fL,
            0x3aa34013f2a802e0L,
            "id"
        )
    }

    private object CONCEPTS {
        /*package*/
        val `DevkitDependency$Ns`: SConcept = MetaAdapterFactory.getConcept(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x7c527144386aca16L,
            "org.modelix.model.repositoryconcepts.structure.DevkitDependency"
        )

        /*package*/
        val `SingleLanguageDependency$_9`: SConcept = MetaAdapterFactory.getConcept(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x1e9fde953529917dL,
            "org.modelix.model.repositoryconcepts.structure.SingleLanguageDependency"
        )
    }

    companion object {
        private val LOG: Logger = LogManager.getLogger(ModelPropertiesSynchronizer::class.java)

        /*package*/
        fun syncModelPropertiesToMPS(
            tree: ITree?,
            model: SModel?,
            modelNodeId: Long,
            cloudRepository: ICloudRepository
        ) {
            syncUsedLanguagesAndDevKitsToMPS(tree, model, modelNodeId, cloudRepository)
            syncModelImportsToMPS(tree, model, modelNodeId, cloudRepository)
            try {
                val projects: List<Project> = ProjectManager.getInstance().getOpenedProjects()
                if (ListSequence.fromList(projects).isNotEmpty()) {
                    val project: Project = ListSequence.fromList(projects).first()
                    ModuleDependencyVersions(
                        LanguageRegistry.getInstance(project.getRepository()),
                        project.getRepository()
                    ).update(
                        model!!.getModule()
                    )
                }
            } catch (ex: Exception) {
                if (LOG.isEnabledFor(Level.ERROR)) {
                    LOG.error(
                        "Failed to update language version after change in model " + model!!.getName().getValue(),
                        ex
                    )
                }
            }
        }

        fun syncUsedLanguagesAndDevKitsToMPS(
            tree: ITree?,
            model: SModel?,
            modelNodeId: Long,
            cloudRepository: ICloudRepository
        ) {
            PArea(cloudRepository.branch!!).executeRead<Unit>({
                ModelAccess.runInWriteActionIfNeeded(model, object : Runnable {
                    public override fun run() {
                        // First get the dependencies in MPS
                        val mpsModelNode: SModelAsNode? = SModelAsNode.Companion.wrap(model)
                        val dependenciesInMPS: List<INode?>? = IterableOfINodeUtils.toList(
                            mpsModelNode!!.getChildren(LINKS.`usedLanguages$QK4E`.getName())
                        )

                        //  Then get the dependencies in the cloud
                        val branch: IBranch? = cloudRepository.branch
                        val cloudModelNode: INode = PNodeAdapter(modelNodeId, (branch)!!)
                        val dependenciesInCloud: Iterable<INode> =
                            cloudModelNode.getChildren(LINKS.`usedLanguages$QK4E`.getName())

                        // For each import in the cloud add it if not present in MPS or otherwise ensure all properties are the same
                        for (dependencyInCloud: INode in Sequence.fromIterable<INode>(dependenciesInCloud)) {
                            val matchingDependencyInMPS: INode? = ListSequence.fromList<INode?>(dependenciesInMPS)
                                .findFirst(object : IWhereFilter<INode>() {
                                    public override fun accept(dependencyInMPS: INode): Boolean {
                                        return Objects.equals(
                                            dependencyInCloud.getPropertyValue(PROPS.`uuid$lpJp`.getName()),
                                            dependencyInMPS.getPropertyValue(
                                                PROPS.`uuid$lpJp`.getName()
                                            )
                                        )
                                    }
                                })
                            if (matchingDependencyInMPS == null) {
                                if (Objects.equals(
                                        dependencyInCloud.concept!!.getLongName(),
                                        (CONCEPTS.`DevkitDependency$Ns`.getLanguage()
                                            .getQualifiedName() + "." + CONCEPTS.`DevkitDependency$Ns`.getName())
                                    )
                                ) {
                                    val repo: SRepository = model!!.getRepository()
                                    val devKitUUID: String? =
                                        dependencyInCloud.getPropertyValue(PROPS.`uuid$lpJp`.getName())
                                    val devKit: DevKit? =
                                        (repo.getModule(ModuleId.regular(UUID.fromString(devKitUUID))) as DevKit?)
                                    val devKitModuleReference: SModuleReference = devKit!!.getModuleReference()
                                    SModelUtils.addDevKit(mpsModelNode.element, devKitModuleReference)
                                } else if (Objects.equals(
                                        dependencyInCloud.concept!!.getLongName(),
                                        (CONCEPTS.`SingleLanguageDependency$_9`.getLanguage()
                                            .getQualifiedName() + "." + CONCEPTS.`SingleLanguageDependency$_9`.getName())
                                    )
                                ) {
                                    val repo: SRepository = model!!.getRepository()
                                    val languageUUID: String? =
                                        dependencyInCloud.getPropertyValue(PROPS.`uuid$lpJp`.getName())
                                    val language: Language? =
                                        (repo.getModule(ModuleId.regular(UUID.fromString(languageUUID))) as Language?)
                                    val sLanguage: SLanguage =
                                        MetaAdapterFactory.getLanguage(language!!.getModuleReference())
                                    SModelUtils.addLanguageImport(
                                        mpsModelNode.element, sLanguage, dependencyInCloud.getPropertyValue(
                                            PROPS.`version$ApUL`.getName()
                                        )!!
                                            .toInt()
                                    )
                                } else {
                                    throw UnsupportedOperationException("Unknown dependency with concept " + dependencyInCloud.concept!!.getLongName())
                                }
                            } else {
                                // We use this method to avoid using set, if it is not strictly necessary, which may be not supported
                                INodeUtils.copyPropertyIfNecessary(
                                    matchingDependencyInMPS,
                                    dependencyInCloud,
                                    PROPS.`name$lpYq`
                                )
                                INodeUtils.copyPropertyIfNecessary(
                                    matchingDependencyInMPS,
                                    dependencyInCloud,
                                    PROPS.`version$ApUL`
                                )
                            }
                        }

                        // For each import not in Cloud remove it
                        for (dependencyInMPS: INode? in ListSequence.fromList<INode?>(dependenciesInMPS)) {
                            if (dependencyInMPS is DevKitDependencyAsNode) {
                                var matchingDependencyInCloud: INode? = null
                                for (dependencyInCloud: INode in Sequence.fromIterable<INode>(dependenciesInCloud)) {
                                    if (Objects.equals(
                                            dependencyInMPS.getPropertyValue(PROPS.`uuid$lpJp`.getName()),
                                            dependencyInCloud.getPropertyValue(
                                                PROPS.`uuid$lpJp`.getName()
                                            )
                                        )
                                    ) {
                                        matchingDependencyInCloud = dependencyInCloud
                                    }
                                }
                                if (matchingDependencyInCloud == null) {
                                    val dsmd: DefaultSModelDescriptor? =
                                        (mpsModelNode.element as DefaultSModelDescriptor?)
                                    val moduleReference: SModuleReference? = (ReflectionUtil.readField(
                                        SingleLanguageDependencyAsNode::class.java, dependencyInMPS, "moduleReference"
                                    ) as SModuleReference?)
                                    val languageToRemove: SLanguage =
                                        MetaAdapterFactory.getLanguage((moduleReference)!!)
                                    dsmd!!.deleteLanguageId(languageToRemove)
                                }
                            } else if (dependencyInMPS is SingleLanguageDependencyAsNode) {
                                var matchingDependencyInCloud: INode? = null
                                for (dependencyInCloud: INode in Sequence.fromIterable<INode>(dependenciesInCloud)) {
                                    if (Objects.equals(
                                            dependencyInMPS.getPropertyValue(PROPS.`uuid$lpJp`.getName()),
                                            dependencyInCloud.getPropertyValue(
                                                PROPS.`uuid$lpJp`.getName()
                                            )
                                        )
                                    ) {
                                        matchingDependencyInCloud = dependencyInCloud
                                    }
                                }
                                if (matchingDependencyInCloud == null) {
                                    val dsmd: DefaultSModelDescriptor? =
                                        (mpsModelNode.element as DefaultSModelDescriptor?)
                                    val moduleReference: SModuleReference? = dependencyInMPS.getModuleReference()
                                    val languageToRemove: SLanguage =
                                        MetaAdapterFactory.getLanguage((moduleReference)!!)
                                    dsmd!!.deleteLanguageId(languageToRemove)
                                }
                            } else {
                                throw RuntimeException("Unknown dependency type: " + dependencyInMPS!!.javaClass.getName())
                            }
                        }
                    }
                })
                Unit
            })
        }

        private fun isNullModel(model: INode?): Boolean {
            if (model == null) {
                return true
            }
            if (model is SModelAsNode) {
                if (model.element == null) {
                    return true
                }
            }
            return false
        }

        fun syncModelImportsToMPS(tree: ITree?, model: SModel?, modelNodeId: Long, cloudRepository: ICloudRepository) {
            PArea(cloudRepository.branch).executeRead<Unit>({
                ModelAccess.runInWriteActionIfNeeded(model, object : Runnable {
                    public override fun run() {
                        // First get the dependencies in MPS
                        val mpsModelNode: SModelAsNode? = SModelAsNode.Companion.wrap(model)
                        val dependenciesInMPS: List<ModelImportAsNode?>? = IterableOfINodeUtils.toCastedList(
                            mpsModelNode!!.getChildren(LINKS.`modelImports$8DOI`.getName())
                        )

                        //  Then get the dependencies in the cloud
                        val branch = cloudRepository.branch
                        val cloudModelNode: INode = PNodeAdapter(modelNodeId, branch)
                        val dependenciesInCloud: Iterable<INode> =
                            cloudModelNode.getChildren(LINKS.`modelImports$8DOI`.getName())

                        // For each import in Cloud add it if not present in MPS or otherwise ensure all properties are the same
                        for (dependencyInCloud: INode in Sequence.fromIterable<INode>(dependenciesInCloud)) {
                            val modelImportedInCloud: INode? =
                                dependencyInCloud.getReferenceTarget(LINKS.`model$GJHn`.getName())
                            if (!(isNullModel(modelImportedInCloud))) {
                                val modelIDimportedInCloud: String? =
                                    modelImportedInCloud!!.getPropertyValue(PROPS.`id$lDUo`.getName())
                                val matchingDependencyInMps: INode? =
                                    ListSequence.fromList<ModelImportAsNode?>(dependenciesInMPS)
                                        .findFirst(object : IWhereFilter<ModelImportAsNode>() {
                                            public override fun accept(dependencyInMPS: ModelImportAsNode): Boolean {
                                                val modelImportedInMPS: INode? =
                                                    dependencyInMPS.getReferenceTarget(LINKS.`model$GJHn`.getName())
                                                if (modelImportedInMPS == null) {
                                                    return false
                                                }
                                                val modelIDimportedInMPS: String? =
                                                    modelImportedInMPS!!.getPropertyValue(PROPS.`id$lDUo`.getName())
                                                return Objects.equals(
                                                    modelIDimportedInCloud,
                                                    modelIDimportedInMPS
                                                )
                                            }
                                        })
                                if (matchingDependencyInMps == null) {
                                    // Model imports have to be added to the underlying SModel using MPS APIs, not the generic reflective INode APIs

                                    // First we build the Module Reference
                                    val moduleContainingModelImportedInCloud: INode? = modelImportedInCloud.parent
                                    val nameOfModuleContainingModelImportedInCloud: String? =
                                        moduleContainingModelImportedInCloud!!.getPropertyValue(
                                            PROPS.`name$MnvL`.getName()
                                        )
                                    val idOfModuleContainingModelImportedInCloud: String? =
                                        moduleContainingModelImportedInCloud.getPropertyValue(
                                            PROPS.`id$7MjP`.getName()
                                        )
                                    val moduleRef: SModuleReference = ModuleReference(
                                        nameOfModuleContainingModelImportedInCloud, ModuleId.fromString(
                                            (idOfModuleContainingModelImportedInCloud)!!
                                        )
                                    )

                                    // Then we build the ModelReference
                                    val modelID: SModelId =
                                        jetbrains.mps.smodel.SModelId.fromString(modelIDimportedInCloud)
                                    val modelName: String? =
                                        modelImportedInCloud.getPropertyValue(PROPS.`name$MnvL`.getName())
                                    val refToModelToImport: SModelReference =
                                        SModelReference(moduleRef, modelID, (modelName)!!)

                                    // We can now add the import
                                    val modelImports: ModelImports = ModelImports(model)
                                    modelImports.addModelImport(refToModelToImport)
                                } else {
                                    // no properties to set here
                                }
                            }
                        }

                        // For each import not in Cloud remove it
                        for (dependencyInMPS: ModelImportAsNode? in ListSequence.fromList<ModelImportAsNode?>(
                            dependenciesInMPS
                        )) {
                            val modelImportedInMPS: INode? =
                                dependencyInMPS!!.getReferenceTarget(LINKS.`model$GJHn`.getName())
                            if (modelImportedInMPS != null) {
                                val modelIDimportedInMPS: String? =
                                    modelImportedInMPS.getPropertyValue(PROPS.`id$lDUo`.getName())
                                val matchingDependencyInCloud: INode? =
                                    Sequence.fromIterable<INode>(dependenciesInCloud)
                                        .findFirst(object : IWhereFilter<INode>() {
                                            public override fun accept(dependencyInCloud: INode): Boolean {
                                                val modelImportedInCloud: INode? =
                                                    dependencyInCloud.getReferenceTarget(LINKS.`model$GJHn`.getName())
                                                if (isNullModel(modelImportedInCloud)) {
                                                    return false
                                                }
                                                val modelIDimportedInCloud: String? =
                                                    modelImportedInCloud!!.getPropertyValue(PROPS.`id$lDUo`.getName())
                                                return Objects.equals(
                                                    modelIDimportedInMPS,
                                                    modelIDimportedInCloud
                                                )
                                            }
                                        })
                                if (matchingDependencyInCloud == null) {
                                    val dsmd: SModelDescriptorStub? =
                                        (mpsModelNode.element as SModelDescriptorStub?)
                                    val depToRemove = dependencyInMPS
                                    val modelReferenceToRemove: org.jetbrains.mps.openapi.model.SModelReference =
                                        depToRemove.element.getReference()
                                    dsmd!!.deleteModelImport(modelReferenceToRemove)
                                }
                            }
                        }
                    }
                })
                Unit
            })
        }
    }
}
