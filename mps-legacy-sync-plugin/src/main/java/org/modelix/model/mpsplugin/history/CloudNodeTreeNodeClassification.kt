package org.modelix.model.mpsplugin.history

import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory
import org.jetbrains.mps.openapi.language.SConcept
import org.modelix.model.api.IConcept
import org.modelix.model.api.INode
import org.modelix.model.api.ITree
import org.modelix.model.api.PNodeAdapter
import org.modelix.model.api.PNodeAdapter.Companion.wrap
import org.modelix.model.mpsadapters.mps.SConceptAdapter
import kotlin.jvm.functions.Function0.invoke
import kotlin.jvm.functions.Function1.invoke

/*Generated by MPS */
object CloudNodeTreeNodeClassification {
    fun isCloudNodeRootNode(_this: CloudNodeTreeNode): Boolean {
        val node: INode? = _this.getNode()
        if (!(node is PNodeAdapter)) {
            return false
        }
        return node.nodeId == ITree.ROOT_ID
    }

    fun isCloudNodeModuleNode(_this: CloudNodeTreeNode): Boolean {
        val concept: IConcept? = _this.getConcept()
        if (concept == null) {
            return false
        }
        return concept.isSubConceptOf(SConceptAdapter.Companion.wrap(CONCEPTS.`Module$4i`))
    }

    fun isCloudNodeAProjectNode(_this: CloudNodeTreeNode): Boolean {
        val concept: IConcept? = _this.getConcept()
        if (concept == null) {
            return false
        }
        return concept.isSubConceptOf(SConceptAdapter.Companion.wrap(CONCEPTS.`Project$An`))
    }

    private object CONCEPTS {
        /*package*/
        val `Module$4i`: SConcept = MetaAdapterFactory.getConcept(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50fL,
            "org.modelix.model.repositoryconcepts.structure.Module"
        )

        /*package*/
        val `Project$An`: SConcept = MetaAdapterFactory.getConcept(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x37a0917d689de959L,
            "org.modelix.model.repositoryconcepts.structure.Project"
        )
    }
}
