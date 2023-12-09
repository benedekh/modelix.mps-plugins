package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import jetbrains.mps.ide.actions.MPSCommonDataKeys
import jetbrains.mps.workbench.action.ActionAccess
import jetbrains.mps.workbench.action.BaseAction
import org.modelix.model.mpsplugin.history.CloudBranchTreeNode
import javax.swing.Icon
import javax.swing.tree.TreeNode

/*Generated by MPS */
class SwitchBranch_Action() : BaseAction("Switch to This Branch", "", ICON) {
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
            val p: Project? = event.getData(CommonDataKeys.PROJECT)
            if (p == null) {
                return false
            }
        })
        run({
            val p: TreeNode? = event.getData(MPSCommonDataKeys.TREE_NODE)
            if (p == null) {
                return false
            }
        })
        return true
    }

    public override fun doExecute(event: AnActionEvent, _params: Map<String, Any>) {
        val branchTreeNode: CloudBranchTreeNode? = (event.getData(MPSCommonDataKeys.TREE_NODE) as CloudBranchTreeNode?)
        branchTreeNode!!.switchBranch()
    }

    companion object {
        private val ICON: Icon? = null
    }
}
