package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import jetbrains.mps.ide.actions.MPSCommonDataKeys
import jetbrains.mps.workbench.action.BaseAction
import org.modelix.model.mpsplugin.history.ModelServerTreeNode
import javax.swing.Icon

/*Generated by MPS */
class Reconnect_Action : BaseAction("Reconnect", "", ICON) {
    init {
        setIsAlwaysVisible(false)
        setExecuteOutsideCommand(true)
    }

    override fun isDumbAware(): Boolean {
        return true
    }

    override fun isApplicable(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        if (event.getData(MPSCommonDataKeys.TREE_NODE) !is ModelServerTreeNode) {
            return false
        }
        val modelServer = (event.getData(MPSCommonDataKeys.TREE_NODE) as ModelServerTreeNode?).getModelServer()
        return !modelServer!!.isConnected()
    }

    public override fun doUpdate(event: AnActionEvent, _params: Map<String, Any>) {
        setEnabledState(event.presentation, isApplicable(event, _params))
    }

    override fun collectActionData(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        if (!super.collectActionData(event, _params)) {
            return false
        }
        run {
            val p = event.getData(CommonDataKeys.PROJECT) ?: return false
        }
        run {
            val p = event.getData(MPSCommonDataKeys.TREE_NODE) ?: return false
        }
        return true
    }

    public override fun doExecute(event: AnActionEvent, _params: Map<String, Any>) {
        val modelServer = (event.getData(MPSCommonDataKeys.TREE_NODE) as ModelServerTreeNode?).getModelServer()
        modelServer!!.reconnect()
    }

    companion object {
        private val ICON: Icon? = null
    }
}
