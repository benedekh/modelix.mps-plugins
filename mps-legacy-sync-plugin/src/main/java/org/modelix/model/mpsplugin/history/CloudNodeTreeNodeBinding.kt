package org.modelix.model.mpsplugin.history

import jetbrains.mps.internal.collections.runtime.ListSequence
import org.modelix.model.api.PNodeAdapter
import org.modelix.model.lazy.RepositoryId
import org.modelix.model.mpsplugin.CloudRepository
import org.modelix.model.mpsplugin.ModelServerConnection
import org.modelix.model.mpsplugin.TransientModuleBinding

/*Generated by MPS */
object CloudNodeTreeNodeBinding {
    fun isBoundAsAModule(_this: CloudNodeTreeNode): Boolean {
        val nodeId: Long = (_this.getNode() as PNodeAdapter?)!!.nodeId
        val repositoryId: RepositoryId? = _this.getAncestor(RepositoryTreeNode::class.java).getRepositoryId()
        if (_this.getModelServer().hasModuleBinding(repositoryId, nodeId)) {
            return true
        }
        return false
    }

    fun getTransientModuleBinding(_this: CloudNodeTreeNode): TransientModuleBinding? {
        val nodeId: Long = (_this.getNode() as PNodeAdapter?)!!.nodeId
        val repositoryId: RepositoryId? = _this.getAncestor(RepositoryTreeNode::class.java).getRepositoryId()
        val bindings: List<TransientModuleBinding> =
            ListSequence.fromList(_this.getModelServer().getModuleBinding(repositoryId, nodeId)).ofType(
                TransientModuleBinding::class.java
            ).toListSequence()
        if (ListSequence.fromList(bindings).count() == 0) {
            return null
        } else if (ListSequence.fromList(bindings).count() == 1) {
            return ListSequence.fromList(bindings).getElement(0)
        } else {
            throw IllegalStateException("Multiple transient bindings for the same module are not expected")
        }
    }

    fun getTreeInRepository(_this: CloudNodeTreeNode?): CloudRepository {
        val modelServer: ModelServerConnection? = _this!!.getAncestor(ModelServerTreeNode::class.java).getModelServer()
        val repositoryId: RepositoryId? = _this.getAncestor(RepositoryTreeNode::class.java).getRepositoryId()
        val treeInRepository: CloudRepository = CloudRepository(modelServer, repositoryId)
        return treeInRepository
    }
}
