package org.modelix.model.mpsadapters.mps

import jetbrains.mps.baseLanguage.closures.runtime.Wrappers._T
import jetbrains.mps.baseLanguage.closures.runtime._FunctionTypes._return_P1_E0
import jetbrains.mps.internal.collections.runtime.CollectionSequence
import jetbrains.mps.internal.collections.runtime.ISelector
import jetbrains.mps.internal.collections.runtime.ListSequence
import jetbrains.mps.internal.collections.runtime.NotNullWhereFilter
import jetbrains.mps.internal.collections.runtime.Sequence
import jetbrains.mps.smodel.DefaultSModelDescriptor
import jetbrains.mps.smodel.MPSModuleRepository
import jetbrains.mps.smodel.ModelImports
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory
import org.jetbrains.mps.openapi.language.SConcept
import org.jetbrains.mps.openapi.language.SContainmentLink
import org.jetbrains.mps.openapi.language.SLanguage
import org.jetbrains.mps.openapi.language.SProperty
import org.jetbrains.mps.openapi.model.SModel
import org.jetbrains.mps.openapi.model.SModelReference
import org.jetbrains.mps.openapi.model.SNode
import org.jetbrains.mps.openapi.module.SModule
import org.jetbrains.mps.openapi.module.SModuleId
import org.jetbrains.mps.openapi.module.SModuleReference
import org.jetbrains.mps.openapi.module.SRepository
import org.modelix.model.api.IConcept
import org.modelix.model.api.INode
import org.modelix.model.api.INodeReference
import org.modelix.model.api.PNodeAdapter.Companion.wrap
import org.modelix.model.area.IArea
import java.util.LinkedList
import java.util.Objects
import kotlin.jvm.functions.Function0.invoke
import kotlin.jvm.functions.Function1.invoke

/*Generated by MPS */
class SModelAsNode(model: SModel) : TreeElementAsNode<SModel?>(model), INode {
    public override fun getArea(): IArea {
        return MPSArea(getElement().getRepository())
    }

    override val concept: IConcept
        get() {
            return (SConceptAdapter.Companion.wrap(CONCEPTS.`Model$2P`))!!
        }

    override fun getChildAccessor(role: String?): IChildAccessor<SModel?>? {
        if ((role == LINKS.`rootNodes$jxXY`.getName())) {
            return rootNodesAccessor
        }
        if ((role == LINKS.`modelImports$8DOI`.getName())) {
            return modelImportsAccessor
        }
        if ((role == LINKS.`usedLanguages$QK4E`.getName())) {
            return usedLanguagesAccessor
        }
        return super.getChildAccessor(role)
    }

    override fun getPropertyAccessor(role: String): IPropertyAccessor<SModel?>? {
        if ((role == PROPS.`name$MnvL`.getName())) {
            return nameAccessor
        }
        if ((role == PROPS.`id$lDUo`.getName())) {
            return idAccessor
        }
        if ((role == PROPS.`stereotype$h7NZ`.getName())) {
            return stereotypeAccessor
        }
        return super.getPropertyAccessor(role)
    }

    override fun getReferenceAccessor(role: String): IReferenceAccessor<SModel?>? {
        return super.getReferenceAccessor(role)
    }

    override val parent: INode?
        get() {
            return SModuleAsNode.Companion.wrap(getElement().getModule())
        }
    override val roleInParent: String
        get() {
            return LINKS.`models$h3QT`.getName()
        }

    fun findSingleLanguageDependency(dependencyId: SModuleId?): SingleLanguageDependencyAsNode? {
        if (getElement() is DefaultSModelDescriptor) {
            val sdmd: DefaultSModelDescriptor? = (getElement() as DefaultSModelDescriptor?)
            for (entry: SLanguage in CollectionSequence.fromCollection(sdmd!!.importedLanguageIds())) {
                if (Objects.equals(check_v4c8ud_a0a0b0a0y(entry.getSourceModule()), dependencyId)) {
                    return SingleLanguageDependencyAsNode(
                        entry.getSourceModuleReference(),
                        sdmd.getLanguageImportVersion(entry),
                        getElement()
                    )
                }
            }
        }
        return null
    }

    fun findDevKitDependency(dependencyId: SModuleId?): DevKitDependencyAsNode? {
        if (getElement() is DefaultSModelDescriptor) {
            val sdmd: DefaultSModelDescriptor? = (getElement() as DefaultSModelDescriptor?)
            for (devKit: SModuleReference in ListSequence.fromList(sdmd!!.importedDevkits())) {
                if (Objects.equals(devKit.getModuleId(), dependencyId)) {
                    return DevKitDependencyAsNode(devKit, getElement())
                }
            }
        }
        return null
    }

    override val reference: INodeReference
        get() {
            return NodeReference(getElement().getReference())
        }

    class NodeReference(val modelRef: SModelReference?) : INodeReference {

        public override fun serialize(): String {
            return "mps-model:" + modelRef
        }

        public override fun resolveNode(area: IArea?): SModelAsNode? {
            val repo: _T<SRepository?> = _T(null)
            if (area != null) {
                val areas: List<IArea> = area.collectAreas()
                repo.value = ListSequence.fromList(areas).ofType(MPSArea::class.java)
                    .select(object : ISelector<MPSArea, SRepository?>() {
                        public override fun select(it: MPSArea): SRepository? {
                            return it.getRepository()
                        }
                    }).where(NotNullWhereFilter<Any?>() as _return_P1_E0<Boolean?, SRepository>?).first()
            }
            if (repo.value == null) {
                repo.value = MPSModuleRepository.getInstance()
            }
            val resolved: _T<SModel> = _T(null)
            if (repo.value!!.getModelAccess().canRead()) {
                resolved.value = modelRef!!.resolve(repo.value)
            } else {
                repo.value!!.getModelAccess().runReadAction(object : Runnable {
                    public override fun run() {
                        resolved.value = modelRef!!.resolve(repo.value)
                    }
                })
            }
            return SModelAsNode(resolved.value)
        }

        public override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o == null || this.javaClass != o.javaClass) {
                return false
            }
            val that: NodeReference = o as NodeReference
            if (!(Objects.equals(modelRef, that.modelRef))) {
                return false
            }
            return true
        }

        public override fun hashCode(): Int {
            var result: Int = 0
            result = 31 * result + ((if (modelRef != null) modelRef.hashCode() else 0))
            return result
        }
    }

    private object CONCEPTS {
        /*package*/
        val `Model$2P`: SConcept = MetaAdapterFactory.getConcept(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            "org.modelix.model.repositoryconcepts.structure.Model"
        )
    }

    private object LINKS {
        /*package*/
        val `rootNodes$jxXY`: SContainmentLink = MetaAdapterFactory.getContainmentLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x69652614fd1c514L,
            "rootNodes"
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
        val `usedLanguages$QK4E`: SContainmentLink = MetaAdapterFactory.getContainmentLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x4aaf28cf2092e98eL,
            "usedLanguages"
        )

        /*package*/
        val `models$h3QT`: SContainmentLink = MetaAdapterFactory.getContainmentLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50fL,
            0x69652614fd1c512L,
            "models"
        )
    }

    private object PROPS {
        /*package*/
        val `name$MnvL`: SProperty = MetaAdapterFactory.getProperty(
            -0x3154ae6ada15b0deL,
            -0x646defc46a3573f4L,
            0x110396eaaa4L,
            0x110396ec041L,
            "name"
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
        val `stereotype$h7NZ`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x35307994bbd9588dL,
            "stereotype"
        )
    }

    companion object {
        private val nameAccessor: IPropertyAccessor<SModel?> = object : IPropertyAccessor<SModel> {
            public override fun get(element: SModel): String? {
                return element.getName().getLongName()
            }

            public override fun set(element: SModel, value: String?): String? {
                throw UnsupportedOperationException("Model name is read only")
            }
        }
        private val stereotypeAccessor: IPropertyAccessor<SModel?> = object : IPropertyAccessor<SModel> {
            public override fun get(element: SModel): String? {
                val value: String = element.getName().getStereotype()
                if (("" == value)) return null // default value is returned as not being set to avoid unnecessary synchronization
                return value
            }

            public override fun set(element: SModel, value: String?): String? {
                throw UnsupportedOperationException("Stereotype is read only")
            }
        }
        private val idAccessor: IPropertyAccessor<SModel?> = object : IPropertyAccessor<SModel> {
            public override fun get(element: SModel): String? {
                return element.getModelId().toString()
            }

            public override fun set(element: SModel, value: String?): String? {
                throw UnsupportedOperationException("Model ID is read only")
            }
        }
        private val rootNodesAccessor: IChildAccessor<SModel?> = object : IChildAccessor<SModel> {
            public override fun get(element: SModel): Iterable<INode> {
                val nodes: Iterable<SNode> = element.getRootNodes()
                return Sequence.fromIterable<SNode>(nodes).select<INode>(object : ISelector<SNode?, INode?>() {
                    public override fun select(it: SNode?): INode? {
                        return SNodeToNodeAdapter.Companion.wrap(it)
                    }
                })
            }
        }
        private val modelImportsAccessor: IChildAccessor<SModel?> = object : IChildAccessor<SModel> {
            public override fun get(element: SModel): Iterable<INode> {
                val importedModelRefs: Iterable<SModelReference> = ModelImports(element).getImportedModels()
                val importedModels: Iterable<SModel> =
                    Sequence.fromIterable(importedModelRefs).select(object : ISelector<SModelReference, SModel>() {
                        public override fun select(it: SModelReference): SModel {
                            return it.resolve(element.getRepository())
                        }
                    }).where(NotNullWhereFilter<Any?>())
                return Sequence.fromIterable(importedModels)
                    .select<INode>(object : ISelector<SModel, ModelImportAsNode>() {
                        public override fun select(it: SModel): ModelImportAsNode {
                            return ModelImportAsNode(it, element)
                        }
                    })
            }
        }
        private val usedLanguagesAccessor: IChildAccessor<SModel?> = object : IChildAccessor<SModel> {
            public override fun get(element: SModel): Iterable<INode> {
                if (element is DefaultSModelDescriptor) {
                    val sdmd: DefaultSModelDescriptor = element
                    val res: List<INode> = ListSequence.fromList(LinkedList())
                    for (languageId: SLanguage in CollectionSequence.fromCollection<SLanguage>(sdmd.importedLanguageIds())) {
                        val languageVersion: Int = sdmd.getLanguageImportVersion(languageId)
                        ListSequence.fromList(res).addElement(
                            SingleLanguageDependencyAsNode(
                                languageId.getSourceModuleReference(),
                                languageVersion,
                                element
                            )
                        )
                    }
                    for (devKit: SModuleReference in ListSequence.fromList<SModuleReference>(sdmd.importedDevkits())) {
                        ListSequence.fromList(res).addElement(DevKitDependencyAsNode(devKit, element))
                    }
                    return res
                }
                return Sequence.fromIterable(emptyList())
            }

            public override fun remove(element: SModel, childToRemove: INode) {
                if (element is DefaultSModelDescriptor) {
                    if (childToRemove is SingleLanguageDependencyAsNode) {
                        val languageToRemove: SLanguage =
                            MetaAdapterFactory.getLanguage(childToRemove.getModuleReference())
                        element.deleteLanguageId(languageToRemove)
                    } else {
                        throw IllegalStateException("The language to remove is not  SingleLanguageDependencyAsNode. Class: " + childToRemove.javaClass)
                    }
                } else {
                    throw IllegalStateException("The model is not a DefaultSModelDescriptor. Class: " + element.javaClass)
                }
            }
        }

        fun wrap(model: SModel?): SModelAsNode? {
            return (if (model == null) null else SModelAsNode(model))
        }

        private fun check_v4c8ud_a0a0b0a0y(checkedDotOperand: SModule?): SModuleId? {
            if (null != checkedDotOperand) {
                return checkedDotOperand.getModuleId()
            }
            return null
        }
    }
}
