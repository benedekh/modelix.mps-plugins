package org.modelix.model.mpsadapters.mps

import org.jetbrains.mps.openapi.model.SModelReference
import org.jetbrains.mps.openapi.model.SNode
import org.jetbrains.mps.openapi.model.SNodeId
import org.jetbrains.mps.openapi.model.SNodeReference
import org.jetbrains.mps.openapi.module.SRepository
import org.modelix.model.api.PNodeAdapter.Companion.wrap

/*Generated by MPS */
class ANodeReference(private val ref: SNodeReference) : SNodeReference {
    public override fun getModelReference(): SModelReference? {
        return ref.getModelReference()
    }

    public override fun getNodeId(): SNodeId? {
        return ref.getNodeId()
    }

    public override fun resolve(repository: SRepository): SNode? {
        return ANode.Companion.wrap(ref.resolve(repository))
    }

    fun unwrap(): SNodeReference {
        return ref
    }
}
