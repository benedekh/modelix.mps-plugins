package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import jetbrains.mps.ide.actions.MPSCommonDataKeys
import jetbrains.mps.ide.project.ProjectHelper
import jetbrains.mps.internal.collections.runtime.Sequence
import jetbrains.mps.openapi.navigation.NavigationSupport
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory
import jetbrains.mps.workbench.action.ActionAccess
import jetbrains.mps.workbench.action.BaseAction
import org.jetbrains.mps.openapi.language.SProperty
import org.jetbrains.mps.openapi.model.SModel
import org.jetbrains.mps.openapi.model.SNode
import org.jetbrains.mps.openapi.module.SModule
import org.jetbrains.mps.openapi.module.SRepository
import org.modelix.model.api.INode
import org.modelix.model.mpsplugin.CloudRepository
import org.modelix.model.mpsplugin.INodeUtils
import org.modelix.model.mpsplugin.MPSNodeMapping
import org.modelix.model.mpsplugin.history.CloudNodeTreeNode
import org.modelix.model.mpsplugin.history.CloudNodeTreeNodeBinding
import org.modelix.model.mpsplugin.history.TreeNodeClassification
import java.util.Objects
import javax.swing.Icon
import javax.swing.tree.TreeNode

/*Generated by MPS */
class NavigateToMpsNode_Action() : BaseAction("Navigate to Corresponding MPS Node", "", ICON) {
    init {
        setIsAlwaysVisible(false)
        setActionAccess(ActionAccess.NONE)
    }

    public override fun isDumbAware(): Boolean {
        return true
    }

    public override fun isApplicable(event: AnActionEvent, _params: Map<String, Any>): Boolean {
        if (!(TreeNodeClassification.isProperNode(event.getData(MPSCommonDataKeys.TREE_NODE)))) {
            return false
        }
        val nodeTreeNode = (event.getData(MPSCommonDataKeys.TREE_NODE) as CloudNodeTreeNode?)!!
        val treeInRepository = CloudNodeTreeNodeBinding.getTreeInRepository(nodeTreeNode)
        return treeInRepository.computeRead({ MPSNodeMapping.isMappedToMpsNode(nodeTreeNode.node) })
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
        val nodeTreeNode = (event.getData(MPSCommonDataKeys.TREE_NODE) as CloudNodeTreeNode?)!!
        val treeInRepository = CloudNodeTreeNodeBinding.getTreeInRepository(nodeTreeNode)
        // I need to know in which module to look for this node
        treeInRepository.runRead(object : Runnable {
            public override fun run() {
                val mpsNodeId: String? =
                    treeInRepository.computeRead({ MPSNodeMapping.mappedMpsNodeID(nodeTreeNode.node) })
                val cloudModule: INode? = INodeUtils.containingModule(nodeTreeNode.node)
                if (cloudModule == null) {
                    Messages.showErrorDialog(
                        event.getData(CommonDataKeys.PROJECT),
                        "No containing module found",
                        "Error navigating to MPS node"
                    )
                    return
                }
                val cloudModel: INode? = INodeUtils.containingModel(nodeTreeNode.node)
                if (cloudModel == null) {
                    Messages.showErrorDialog(
                        event.getData(CommonDataKeys.PROJECT),
                        "No containing model found",
                        "Error navigating to MPS node"
                    )
                    return
                }
                val moduleId: String? = cloudModule.getPropertyValue(PROPS.`id$7MjP`.getName())
                if (moduleId == null) {
                    Messages.showErrorDialog(
                        event.getData(CommonDataKeys.PROJECT),
                        "No module id",
                        "Error navigating to MPS node"
                    )
                    return
                }
                val modelId: String? = cloudModel.getPropertyValue(PROPS.`id$lDUo`.getName())
                if (modelId == null) {
                    Messages.showErrorDialog(
                        event.getData(CommonDataKeys.PROJECT),
                        "No model id",
                        "Error navigating to MPS node"
                    )
                    return
                }
                val repo: SRepository = ProjectHelper.toMPSProject(event.getData(CommonDataKeys.PROJECT))!!
                    .getRepository()
                repo.getModelAccess().runReadAction(object : Runnable {
                    public override fun run() {
                        for (module: SModule in Sequence.fromIterable<SModule>(repo.getModules())) {
                            if (Objects.equals(module.getModuleId().toString(), moduleId)) {
                                for (model: SModel in Sequence.fromIterable<SModel>(module.getModels())) {
                                    if (Objects.equals(model.getModelId().toString(), modelId)) {
                                        val node: SNode? = findNodeInModel(model, mpsNodeId, event)
                                        if (node == null) {
                                            Messages.showErrorDialog(
                                                event.getData(CommonDataKeys.PROJECT),
                                                "No node found: " + mpsNodeId,
                                                "Error navigating to MPS node"
                                            )
                                            return
                                        } else {
                                            NavigationSupport.getInstance().openNode(
                                                (ProjectHelper.toMPSProject(event.getData(CommonDataKeys.PROJECT)))!!,
                                                node,
                                                false,
                                                true
                                            )
                                            return
                                        }
                                    }
                                }
                                Messages.showErrorDialog(
                                    event.getData(CommonDataKeys.PROJECT),
                                    "No model found: " + modelId,
                                    "Error navigating to MPS node"
                                )
                                return
                            }
                        }
                        Messages.showErrorDialog(
                            event.getData(CommonDataKeys.PROJECT),
                            "No module found: " + moduleId,
                            "Error navigating to MPS node"
                        )
                    }
                })
            }
        })
    }

    /*package*/
    fun findNodeInModel(model: SModel, nodeId: String?, event: AnActionEvent?): SNode? {
        for (root: SNode in Sequence.fromIterable(model.getRootNodes())) {
            val res: SNode? = findNodeInNode(root, nodeId, event)
            if (res != null) {
                return res
            }
        }
        return null
    }

    /*package*/
    fun findNodeInNode(node: SNode, nodeId: String?, event: AnActionEvent?): SNode? {
        if (Objects.equals(node.getNodeId().toString(), nodeId)) {
            return node
        }
        for (child: SNode in Sequence.fromIterable(node.getChildren())) {
            val res: SNode? = findNodeInNode(child, nodeId, event)
            if (res != null) {
                return res
            }
        }
        return null
    }

    private object PROPS {
        /*package*/
        val `id$7MjP`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50fL,
            0x3aa34013f2a802e0L,
            "id"
        )

        /*package*/
        val `id$lDUo`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50cL,
            0x244b85440ee67212L,
            "id"
        )
    }

    companion object {
        private val ICON: Icon? = null
    }
}
