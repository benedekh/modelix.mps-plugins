package org.modelix.model.mpsadapters.mps

import jetbrains.mps.baseLanguage.closures.runtime._FunctionTypes._return_P1_E0
import jetbrains.mps.internal.collections.runtime.ISelector
import jetbrains.mps.internal.collections.runtime.ITranslator2
import jetbrains.mps.internal.collections.runtime.LinkedListSequence
import jetbrains.mps.internal.collections.runtime.ListSequence
import jetbrains.mps.internal.collections.runtime.NotNullWhereFilter
import jetbrains.mps.internal.collections.runtime.Sequence
import jetbrains.mps.smodel.MPSModuleRepository
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory
import org.jetbrains.mps.openapi.language.SConcept
import org.jetbrains.mps.openapi.language.SContainmentLink
import org.jetbrains.mps.openapi.language.SProperty
import org.jetbrains.mps.openapi.model.SModel
import org.jetbrains.mps.openapi.model.SModelReference
import org.jetbrains.mps.openapi.module.SModule
import org.jetbrains.mps.openapi.module.SModuleId
import org.jetbrains.mps.openapi.module.SModuleReference
import org.jetbrains.mps.openapi.module.SRepository
import org.modelix.model.api.IChildLink
import org.modelix.model.api.IConcept
import org.modelix.model.api.IConceptReference
import org.modelix.model.api.INode
import org.modelix.model.api.INodeReference
import org.modelix.model.api.IProperty
import org.modelix.model.api.IReferenceLink
import org.modelix.model.api.PNodeAdapter.Companion.wrap
import org.modelix.model.area.IArea
import java.util.LinkedList
import java.util.Objects

/*Generated by MPS */
class DevKitDependencyAsNode : INode {
    private var moduleReference: SModuleReference
    private var moduleImporter: SModule? = null
    private var modelImporter: SModel? = null

    constructor(moduleReference: SModuleReference, importer: SModule?) {
        this.moduleReference = moduleReference
        moduleImporter = importer
    }

    constructor(moduleReference: SModuleReference, importer: SModel?) {
        this.moduleReference = moduleReference
        modelImporter = importer
    }

    public override fun getArea(): IArea {
        return MPSArea()
    }

    public override val isValid: Boolean
        get() {
            return true
        }

    public override val reference: INodeReference
        get() {
            if (moduleImporter != null) {
                return NodeReference(moduleImporter!!.getModuleReference(), moduleReference.getModuleId())
            }
            if (modelImporter != null) {
                return NodeReference(modelImporter!!.getReference(), moduleReference.getModuleId())
            }
            throw IllegalStateException()
        }

    public override val concept: IConcept?
        get() {
            return SConceptAdapter.Companion.wrap(CONCEPTS.`DevkitDependency$Ns`)
        }

    public override val roleInParent: String?
        get() {
            if (moduleImporter != null) {
                return LINKS.`languageDependencies$vKlY`.getName()
            }
            if (modelImporter != null) {
                return LINKS.`usedLanguages$QK4E`.getName()
            }
            throw IllegalStateException()
        }

    public override val parent: INode?
        get() {
            if (moduleImporter != null) {
                SModuleAsNode.Companion.wrap(moduleImporter)
            }
            if (modelImporter != null) {
                SModelAsNode.Companion.wrap(modelImporter)
            }
            throw IllegalStateException()
        }

    public override fun getChildren(role: String?): Iterable<INode> {
        return LinkedListSequence.fromLinkedListNew(LinkedList())
    }

    public override val allChildren: Iterable<INode>
        get() {
            val concept: IConcept? = concept
            if (concept == null) {
                return emptyList()
            }
            val links: Iterable<IChildLink> = concept.getAllChildLinks()
            return Sequence.fromIterable(links).select(object : ISelector<IChildLink, Iterable<INode>>() {
                public override fun select(it: IChildLink): Iterable<INode> {
                    return getChildren(it.name)
                }
            }).translate(object : ITranslator2<Iterable<INode>, INode>() {
                public override fun translate(it: Iterable<INode>): Iterable<INode> {
                    return it
                }
            })
        }

    public override fun moveChild(string: String?, i: Int, node: INode) {
        throw UnsupportedOperationException()
    }

    public override fun addNewChild(string: String?, i: Int, concept: IConcept?): INode {
        throw UnsupportedOperationException()
    }

    public override fun addNewChild(string: String?, i: Int, reference: IConceptReference?): INode {
        throw UnsupportedOperationException()
    }

    public override fun removeChild(node: INode) {
        throw UnsupportedOperationException()
    }

    public override fun getReferenceTarget(role: String): INode? {
        return null
    }

    public override fun getReferenceTargetRef(string: String): INodeReference? {
        return null
    }

    public override fun setReferenceTarget(string: String, node: INode?) {
        throw UnsupportedOperationException()
    }

    public override fun setReferenceTarget(string: String, reference: INodeReference?) {
        throw UnsupportedOperationException()
    }

    public override fun getPropertyValue(propertyName: String): String? {
        if (Objects.equals(PROPS.`name$lpYq`.getName(), propertyName)) {
            return moduleReference.getModuleName()
        } else if (Objects.equals(PROPS.`uuid$lpJp`.getName(), propertyName)) {
            return moduleReference.getModuleId().toString()
        } else {
            return null
        }
    }

    public override fun setPropertyValue(string: String, string1: String?) {
        throw UnsupportedOperationException()
    }

    public override fun getPropertyRoles(): List<String> {
        val concept: IConcept? = concept
        if (concept == null) {
            return emptyList()
        }
        val allProperties: List<IProperty> = concept.getAllProperties()
        return ListSequence.fromList(allProperties).select(object : ISelector<IProperty, String>() {
            public override fun select(it: IProperty): String {
                return it.name
            }
        }).toListSequence()
    }

    public override fun getReferenceRoles(): List<String> {
        val concept: IConcept? = concept
        if (concept == null) {
            return emptyList()
        }
        val allReferenceLinks: List<IReferenceLink> = concept.getAllReferenceLinks()
        return ListSequence.fromList(allReferenceLinks).select(object : ISelector<IReferenceLink, String>() {
            public override fun select(it: IReferenceLink): String {
                return it.name
            }
        }).toListSequence()
    }

    class NodeReference : INodeReference {
        private var userModuleReference: SModuleReference? = null
        private var userModel: SModelReference? = null
        private var usedModuleId: SModuleId?

        constructor(userModuleReference: SModuleReference?, usedModuleId: SModuleId?) {
            this.userModuleReference = userModuleReference
            this.usedModuleId = usedModuleId
        }

        constructor(userModel: SModelReference?, usedModuleId: SModuleId?) {
            this.userModel = userModel
            this.usedModuleId = usedModuleId
        }

        public override fun resolveNode(area: IArea?): INode? {
            var repo: SRepository? = null
            if (area != null) {
                val areas: List<IArea> = area.collectAreas()
                repo = areas.filterIsInstance<MPSArea>().map { it.repository }.filterNotNull().firstOrNull()
            }
            if (repo == null) {
                repo = MPSModuleRepository.getInstance()
            }
            if (userModuleReference != null) {
                val user: SModule? = userModuleReference!!.resolve((repo)!!)
                if (user == null) {
                    return null
                }
                return SModuleAsNode(user).findDevKitDependency(usedModuleId)
            } else if (userModel != null) {
                val model: SModel = userModel!!.resolve(repo)
                return SModelAsNode(model).findDevKitDependency(usedModuleId)
            } else {
                throw IllegalStateException()
            }
        }

        public override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o == null || this.javaClass != o.javaClass) {
                return false
            }
            val that: NodeReference = o as NodeReference
            if (Objects.equals(userModuleReference, that.userModuleReference)) {
                return false
            }
            if (Objects.equals(userModel, that.userModel)) {
                return false
            }
            if (Objects.equals(usedModuleId, that.usedModuleId)) {
                return false
            }
            return true
        }

        public override fun hashCode(): Int {
            var result: Int = 0
            result = 31 * result + ((if (userModuleReference != null) (userModuleReference as Any).hashCode() else 0))
            result = 11 * result + ((if (usedModuleId != null) (usedModuleId as Any).hashCode() else 0))
            result = 37 * result + ((if (userModel != null) (userModel as Any).hashCode() else 0))
            return result
        }
    }

    public override fun getConceptReference(): IConceptReference? {
        return check_d9amqo_a0a64(concept, this)
    }

    private object CONCEPTS {
        /*package*/
        val `DevkitDependency$Ns`: SConcept = MetaAdapterFactory.getConcept(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x7c527144386aca16L,
            "org.modelix.model.repositoryconcepts.structure.DevkitDependency"
        )
    }

    private object LINKS {
        /*package*/
        val `languageDependencies$vKlY`: SContainmentLink = MetaAdapterFactory.getContainmentLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50fL,
            0x1e9fde9535299187L,
            "languageDependencies"
        )

        /*package*/
        val `usedLanguages$QK4E`: SContainmentLink = MetaAdapterFactory.getContainmentLink(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x4aaf28cf2092e98eL,
            "usedLanguages"
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
    }

    companion object {
        private fun check_d9amqo_a0a64(
            checkedDotOperand: IConcept?,
            checkedDotThisExpression: DevKitDependencyAsNode
        ): IConceptReference? {
            if (null != checkedDotOperand) {
                return checkedDotOperand.getReference()
            }
            return null
        }
    }
}
