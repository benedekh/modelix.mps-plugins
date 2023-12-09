package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import jetbrains.mps.ide.actions.MPSCommonDataKeys
import jetbrains.mps.workbench.action.BaseAction
import org.modelix.model.mpsplugin.CloudNodeTreeNodeCreationMethods
import org.modelix.model.mpsplugin.history.CloudNodeTreeNode
import org.modelix.model.mpsplugin.history.TreeNodeClassification
import javax.swing.Icon
import javax.swing.tree.TreeNode

/*Generated by MPS */
class AddModuleNode_Action() : BaseAction("Add Module", "", ICON) {
    init {
        setIsAlwaysVisible(false)
        setExecuteOutsideCommand(true)
    }

    public override fun isDumbAware(): Boolean {
        return true
    }

    public override fun isApplicable(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        return TreeNodeClassification.isRootNode(event.getData(MPSCommonDataKeys.TREE_NODE))
    }

    public override fun doUpdate(event: AnActionEvent, _params: Map<String, Any>) {
        setEnabledState(event.getPresentation(), isApplicable(event, _params))
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
        val nodeTreeNode: CloudNodeTreeNode? = event.getData(MPSCommonDataKeys.TREE_NODE) as CloudNodeTreeNode?
        val name: String? = Messages.showInputDialog(event.getData(CommonDataKeys.PROJECT), "Name", "Add Module", null)
        if ((name == null || name.length == 0)) {
            return
        }
        CloudNodeTreeNodeCreationMethods.createModule(nodeTreeNode, name)
    }

    companion object {
        private val ICON: Icon? = null
    }
}
