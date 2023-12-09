package org.modelix.model.mpsplugin.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.Pair
import jetbrains.mps.internal.collections.runtime.Sequence
import jetbrains.mps.internal.collections.runtime.SetSequence
import jetbrains.mps.plugins.actions.GeneratedActionGroup
import jetbrains.mps.workbench.ActionPlace
import jetbrains.mps.workbench.action.ApplicationPlugin
import jetbrains.mps.workbench.action.BaseAction
import org.jetbrains.mps.openapi.model.SNode
import org.jetbrains.mps.util.Condition
import org.modelix.model.mpsplugin.CloudRepository
import org.modelix.model.mpsplugin.ModelServerConnections
import java.util.function.Consumer

/*Generated by MPS */
class CloudActionsForPhysicalProjectsCloudActions_ActionGroup(plugin: ApplicationPlugin) :
    GeneratedActionGroup("Cloud Actions", ID, plugin) {
    private val myPlaces: Set<Pair<ActionPlace, Condition<BaseAction>?>> = SetSequence.fromSet(HashSet())

    init {
        setIsInternal(false)
        setPopup(true)
    }

    public override fun doUpdate(event: AnActionEvent) {
        removeAll()
        for (treeInRepository: CloudRepository in Sequence.fromIterable<CloudRepository>(
            ModelServerConnections.instance.connectedTreesInRepositories
        )) {
            treeInRepository.processProjects(object : Consumer<SNode> {
                public override fun accept(pr: SNode) {
                    this@CloudActionsForPhysicalProjectsCloudActions_ActionGroup.addParameterizedAction(
                        CopyAndSyncPhysicalProjectOnCloud_Action(treeInRepository, pr),
                        treeInRepository,
                        pr
                    )
                }
            })
            this@CloudActionsForPhysicalProjectsCloudActions_ActionGroup.addParameterizedAction(
                CopyAndSyncPhysicalProjectOnCloud_Action(treeInRepository, null),
                treeInRepository,
                null
            )
        }
        for (p: Pair<ActionPlace, Condition<BaseAction>?> in myPlaces) {
            addPlace(p.first, p.second)
        }
    }

    public override fun addPlace(place: ActionPlace, cond: Condition<BaseAction>?) {
        SetSequence.fromSet(myPlaces).addElement(Pair(place, cond))
    }

    public override fun isStrict(): Boolean {
        return false
    }

    companion object {
        val ID: String = "org.modelix.model.mpsplugin.plugin.CloudActionsForPhysicalProjectsCloudActions_ActionGroup"
    }
}
