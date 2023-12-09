package org.modelix.model.mpsplugin.history

import jetbrains.mps.ide.ui.tree.TextTreeNode
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SPropertyOperations
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory
import org.jetbrains.mps.openapi.language.SProperty
import org.jetbrains.mps.openapi.model.SNode
import org.modelix.model.api.IBranch
import org.modelix.model.area.PArea
import org.modelix.model.lazy.RepositoryId
import org.modelix.model.mpsplugin.CloudIcons
import org.modelix.model.mpsplugin.ModelServerConnection

/*Generated by MPS */
class CloudBranchTreeNode(private val modelServer: ModelServerConnection?, val branchInfo: SNode?) : TextTreeNode(
    CloudIcons.BRANCH_ICON, SPropertyOperations.getString(
        branchInfo, PROPS.`name$MnvL`
    )
) {

    init {
        setAllowsChildren(true)
    }

    fun updateChildren() {}
    public override fun doubleClick() {
        switchBranch()
    }

    fun switchBranch() {
        val treeTreeNode: RepositoryTreeNode = this.getAncestor(RepositoryTreeNode::class.java)
        val repositoryId: RepositoryId? = treeTreeNode.getRepositoryId()
        val infoBranch: IBranch? = modelServer.getInfoBranch()
        val branchName: String = PArea((infoBranch)!!).executeRead({
            SPropertyOperations.getString(
                branchInfo, PROPS.`name$MnvL`
            )
        })
        modelServer!!.getActiveBranch(repositoryId).switchBranch(branchName)
    }

    private object PROPS {
        /*package*/
        val `name$MnvL`: SProperty = MetaAdapterFactory.getProperty(
            -0x3154ae6ada15b0deL,
            -0x646defc46a3573f4L,
            0x110396eaaa4L,
            0x110396ec041L,
            "name"
        )
    }
}
