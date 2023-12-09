package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import jetbrains.mps.ide.actions.MPSCommonDataKeys
import jetbrains.mps.workbench.action.ActionAccess
import jetbrains.mps.workbench.action.BaseAction
import org.modelix.model.mpsplugin.ModelServerConnection
import org.modelix.model.mpsplugin.history.RepositoryTreeNode
import javax.swing.Icon
import javax.swing.tree.TreeNode

/*Generated by MPS */
class RemoveRepository_Action() : BaseAction("Remove Repository", "", ICON) {
    init {
        setIsAlwaysVisible(false)
        setActionAccess(ActionAccess.UNDO_PROJECT)
    }

    public override fun isDumbAware(): Boolean {
        return true
    }

    override fun collectActionData(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        if (!(super.collectActionData(event, _params))) {
            return false
        }
        run({
            val p: TreeNode? = event.getData(MPSCommonDataKeys.TREE_NODE)
            if (p == null) {
                return false
            }
        })
        return true
    }

    public override fun doExecute(event: AnActionEvent, _params: Map<String, Any>) {
        val repositoryNode: RepositoryTreeNode = (event.getData(MPSCommonDataKeys.TREE_NODE) as RepositoryTreeNode?)!!
        val modelServer: ModelServerConnection = repositoryNode.modelServer
        modelServer.removeRepository(repositoryNode.repositoryId.id)
    }

    companion object {
        private val ICON: Icon? = null
    }
}
