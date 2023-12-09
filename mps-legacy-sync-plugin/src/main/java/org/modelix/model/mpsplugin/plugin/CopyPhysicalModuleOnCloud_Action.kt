package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import jetbrains.mps.ide.actions.MPSCommonDataKeys
import jetbrains.mps.progress.ProgressMonitorAdapter
import jetbrains.mps.workbench.action.ActionAccess
import jetbrains.mps.workbench.action.BaseAction
import org.jetbrains.mps.openapi.module.SModule
import org.modelix.model.mpsplugin.CloudRepository
import org.modelix.model.mpsplugin.ModelCloudImportUtils
import javax.swing.Icon

/*Generated by MPS */
class CopyPhysicalModuleOnCloud_Action(private val treeInRepository: CloudRepository) :
    BaseAction("Copy on Cloud", "", ICON) {
    init {
        setIsAlwaysVisible(false)
        setActionAccess(ActionAccess.UNDO_PROJECT)
    }

    public override fun isDumbAware(): Boolean {
        return true
    }

    public override fun isApplicable(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        val connected: Boolean = treeInRepository.isConnected()
        event.getPresentation().setText("Copy on Cloud -> " + treeInRepository.presentation())
        try {
            return connected && !(ModelCloudImportUtils.containsModule(
                treeInRepository,
                event.getData(MPSCommonDataKeys.MODULE)
            ))
        } catch (e: RuntimeException) {
            // This could happen because of repositories in invalid state. In this case let's ignore those repositories without preventing usage of other repositories
            return false
        }
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
            val p: SModule? = event.getData(MPSCommonDataKeys.MODULE)
            if (p == null) {
                return false
            }
        })
        return true
    }

    public override fun doExecute(event: AnActionEvent, _params: Map<String, Any>) {
        object : Task.Modal(event.getData(CommonDataKeys.PROJECT), "Copy on Cloud", false) {
            public override fun run(indicator: ProgressIndicator) {
                ModelCloudImportUtils.copyInModelixAsIndependentModule(
                    treeInRepository,
                    event.getData(MPSCommonDataKeys.MODULE),
                    event.getData(CommonDataKeys.PROJECT),
                    ProgressMonitorAdapter(indicator)
                )
            }
        }.queue()
    }

    public override fun getActionId(): String {
        val res: StringBuilder = StringBuilder()
        res.append(super.getActionId())
        res.append("#")
        res.append(treeInRepository_State(treeInRepository))
        res.append("!")
        return res.toString()
    }

    companion object {
        private val ICON: Icon? = null
        fun treeInRepository_State(`object`: CloudRepository): String? {
            return `object`.presentation()
        }
    }
}
