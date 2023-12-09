package org.modelix.model.mpsplugin

import jetbrains.mps.internal.collections.runtime.IVisitor
import jetbrains.mps.internal.collections.runtime.ListSequence
import jetbrains.mps.internal.collections.runtime.MapSequence
import jetbrains.mps.internal.collections.runtime.Sequence
import jetbrains.mps.internal.collections.runtime.SetSequence
import org.modelix.model.api.ITree
import org.modelix.model.api.IWriteTransaction

/*Generated by MPS */ /**
 * Synchronizes an unordered list of children
 */
abstract class Synchronizer<MPSChildT>(cloudParentId: Long, cloudRole: String?) {
    val cloudParentId: Long
    private val cloudRole: String?

    init {
        if (cloudParentId == 0L) throw IllegalArgumentException("Illegal 'cloudParentId': " + cloudParentId)
        this.cloudParentId = cloudParentId
        this.cloudRole = cloudRole
    }

    open fun getCloudChildren(tree: ITree): Iterable<Long?>? {
        val children: Iterable<Long> = tree.getChildren(cloudParentId, cloudRole)
        return children
    }

    abstract val mPSChildren: Iterable<MPSChildT>?
    protected abstract fun createMPSChild(tree: ITree, cloudChildId: Long): MPSChildT
    abstract fun createCloudChild(t: IWriteTransaction, mpsChild: MPSChildT): Long
    abstract fun removeMPSChild(mpsChild: MPSChildT)
    abstract fun associate(
        tree: ITree,
        cloudChildren: List<Long>,
        mpsChildren: List<MPSChildT>?,
        direction: SyncDirection?
    ): Map<Long?, MPSChildT>

    open fun syncToMPS(tree: ITree): Map<Long?, MPSChildT>? {
        val expectedChildren: List<Long> = Sequence.fromIterable(getCloudChildren(tree)).toListSequence()
        val existingChildren: List<MPSChildT> = Sequence.fromIterable(
            mPSChildren
        ).toListSequence()
        val mappings: Map<Long?, MPSChildT> = associate(tree, expectedChildren, existingChildren, SyncDirection.TO_MPS)
        val toAdd: List<Long> =
            ListSequence.fromList(expectedChildren).subtract(SetSequence.fromSet(MapSequence.fromMap(mappings).keys))
                .toListSequence()
        val toRemove: List<MPSChildT> = ListSequence.fromList(existingChildren)
            .subtract(Sequence.fromIterable(MapSequence.fromMap(mappings).values)).toListSequence()
        ListSequence.fromList(toRemove).visitAll(object : IVisitor<MPSChildT>() {
            public override fun visit(it: MPSChildT) {
                removeMPSChild(it)
            }
        })
        ListSequence.fromList(toAdd).visitAll(object : IVisitor<Long>() {
            public override fun visit(it: Long) {
                MapSequence.fromMap(mappings).put(it, createMPSChild(tree, it))
            }
        })
        return mappings
    }

    fun syncToCloud(t: IWriteTransaction): Map<Long?, MPSChildT> {
        val expectedChildren: List<MPSChildT> = Sequence.fromIterable(
            mPSChildren
        ).toListSequence()
        val existingChildren: List<Long> = Sequence.fromIterable(getCloudChildren(t.tree)).toListSequence()
        val mappings: Map<Long?, MPSChildT> =
            associate(t.tree, existingChildren, expectedChildren, SyncDirection.TO_CLOUD)
        val toAdd: List<MPSChildT> = ListSequence.fromList(expectedChildren)
            .subtract(Sequence.fromIterable(MapSequence.fromMap(mappings).values)).toListSequence()
        val toRemove: List<Long> =
            ListSequence.fromList(existingChildren).subtract(SetSequence.fromSet(MapSequence.fromMap(mappings).keys))
                .toListSequence()
        ListSequence.fromList(toRemove).visitAll(object : IVisitor<Long?>() {
            public override fun visit(it: Long?) {
                t.moveChild(ITree.ROOT_ID, ITree.DETACHED_NODES_ROLE, -1, (it)!!)
            }
        })
        ListSequence.fromList(toAdd).visitAll(object : IVisitor<MPSChildT>() {
            public override fun visit(it: MPSChildT) {
                MapSequence.fromMap(mappings).put(createCloudChild(t, it), it)
            }
        })
        return mappings
    }
}
