package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import jetbrains.mps.ide.actions.MPSCommonDataKeys
import jetbrains.mps.project.MPSProject
import jetbrains.mps.workbench.action.BaseAction
import org.modelix.model.mpsplugin.history.TreeNodeAccess
import org.modelix.model.mpsplugin.history.TreeNodeClassification
import javax.swing.Icon
import javax.swing.tree.TreeNode

/*Generated by MPS */
class DeleteModule_Action() : BaseAction("Delete Module", "", ICON) {
    init {
        setIsAlwaysVisible(false)
        setExecuteOutsideCommand(true)
    }

    public override fun isDumbAware(): Boolean {
        return true
    }

    public override fun isApplicable(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        return TreeNodeClassification.isModuleNode(event.getData(MPSCommonDataKeys.TREE_NODE))
    }

    public override fun doUpdate(event: AnActionEvent, _params: Map<String, Any>) {
        setEnabledState(event.getPresentation(), isApplicable(event, _params))
    }

    override fun collectActionData(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        if (!(super.collectActionData(event, _params))) {
            return false
        }
        run({
            val p: MPSProject? = event.getData(MPSCommonDataKeys.MPS_PROJECT)
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
        val dialogResult: Int = Messages.showOkCancelDialog(
            event.getData(MPSCommonDataKeys.MPS_PROJECT)!!.getProject(),
            "Are you sure you want to delete module '" + TreeNodeAccess.getName(event.getData(MPSCommonDataKeys.TREE_NODE)) + "' ?",
            "Delete Module",
            null
        )
        if (dialogResult != Messages.OK) {
            return
        }
        TreeNodeAccess.delete(event.getData(MPSCommonDataKeys.TREE_NODE))
    }

    companion object {
        private val ICON: Icon? = null
    }
}
