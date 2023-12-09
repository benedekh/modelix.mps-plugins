package org.modelix.model.mpsplugin

import org.jetbrains.mps.openapi.model.SNode
import org.modelix.model.api.INode
import org.modelix.model.api.INode.reference
import org.modelix.model.api.INodeReference.serialize
import org.modelix.model.api.PNodeAdapter.Companion.wrap
import org.modelix.model.mpsadapters.mps.SNodeToNodeAdapter
import kotlin.jvm.functions.Function0.invoke
import kotlin.jvm.functions.Function1.invoke

/*Generated by MPS */
object MPSNodeMapping {
    private val MPS_NODE_ID_PROPERTY_NAME: String = ModelSynchronizer.Companion.MPS_NODE_ID_PROPERTY_NAME
    fun mapToMpsNode(_this: INode, mpsNode: SNode?) {
        _this.setPropertyValue(MPS_NODE_ID_PROPERTY_NAME, mpsNode!!.getNodeId().toString())
        _this.setPropertyValue("\$originalId", SNodeToNodeAdapter.Companion.wrap(mpsNode)!!.reference.serialize())
    }

    fun mappedMpsNodeID(_this: INode?): String? {
        try {
            return _this!!.getPropertyValue(MPS_NODE_ID_PROPERTY_NAME)
        } catch (e: RuntimeException) {
            throw RuntimeException(
                "Failed to retrieve the " + MPS_NODE_ID_PROPERTY_NAME + " property in mappedMpsNodeID. The INode is " + _this + ", concept: " + _this!!.concept,
                e
            )
        }
    }

    fun isMappedToMpsNode(_this: INode?): Boolean {
        return mappedMpsNodeID(_this) != null
    }
}
