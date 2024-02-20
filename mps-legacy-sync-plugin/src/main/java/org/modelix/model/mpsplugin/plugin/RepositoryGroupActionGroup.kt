package org.modelix.model.mpsplugin.plugin

import jetbrains.mps.plugins.actions.GeneratedActionGroup
import jetbrains.mps.workbench.action.ApplicationPlugin

/*Generated by MPS */
class RepositoryGroupActionGroup(plugin: ApplicationPlugin) : GeneratedActionGroup("RepositoryGroup", ID, plugin) {
    init {
        setIsInternal(false)
        isPopup = false
        this@RepositoryGroupActionGroup.addAction("org.modelix.model.mpsplugin.plugin.LoadHistoryForRepository_Action")
        this@RepositoryGroupActionGroup.addAction("org.modelix.model.mpsplugin.plugin.RemoveRepository_Action")
        this@RepositoryGroupActionGroup.addAction("org.modelix.model.mpsplugin.plugin.GetCloudRepositorySize_Action")
    }

    companion object {
        val ID: String = "org.modelix.model.mpsplugin.plugin.RepositoryGroup_ActionGroup"
    }
}
