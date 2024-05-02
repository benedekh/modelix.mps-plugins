/*
 * Copyright (c) 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modelix.mps.sync.transformation.modelixToMps.incremental

import jetbrains.mps.project.AbstractModule
import mu.KotlinLogging
import org.modelix.kotlin.utils.UnstableModelixFeature
import org.modelix.model.api.IBranch
import org.modelix.model.api.ITreeChangeVisitorEx
import org.modelix.model.api.PropertyFromName
import org.modelix.model.api.getNode
import org.modelix.model.client2.ReplicatedModel
import org.modelix.model.mpsadapters.MPSLanguageRepository
import org.modelix.mps.sync.mps.ActiveMpsProjectInjector
import org.modelix.mps.sync.mps.notifications.InjectableNotifierWrapper
import org.modelix.mps.sync.tasks.SyncDirection
import org.modelix.mps.sync.tasks.SyncLock
import org.modelix.mps.sync.tasks.SyncQueue
import org.modelix.mps.sync.transformation.ModelixToMpsSynchronizationException
import org.modelix.mps.sync.transformation.cache.MpsToModelixMap
import org.modelix.mps.sync.transformation.modelixToMps.transformers.ModelTransformer
import org.modelix.mps.sync.transformation.modelixToMps.transformers.ModuleTransformer
import org.modelix.mps.sync.transformation.modelixToMps.transformers.NodeTransformer
import org.modelix.mps.sync.util.getModule
import org.modelix.mps.sync.util.isDevKitDependency
import org.modelix.mps.sync.util.isModel
import org.modelix.mps.sync.util.isModelImport
import org.modelix.mps.sync.util.isModule
import org.modelix.mps.sync.util.isModuleDependency
import org.modelix.mps.sync.util.isSingleLanguageDependency
import org.modelix.mps.sync.util.nodeIdAsLong

@UnstableModelixFeature(reason = "The new modelix MPS plugin is under construction", intendedFinalization = "This feature is finalized when the new sync plugin is ready for release.")
class ModelixTreeChangeVisitor(
    private val replicatedModel: ReplicatedModel,
    languageRepository: MPSLanguageRepository,
) : ITreeChangeVisitorEx {

    private val logger = KotlinLogging.logger {}
    private val nodeMap = MpsToModelixMap
    private val syncQueue = SyncQueue
    private val notifierInjector = InjectableNotifierWrapper

    private val branch: IBranch
        get() = replicatedModel.getBranch()

    private val nodeTransformer = NodeTransformer(branch, languageRepository)
    private val modelTransformer = ModelTransformer(branch, languageRepository)
    private val moduleTransformer = ModuleTransformer(branch, languageRepository)

    override fun referenceChanged(nodeId: Long, role: String) {
        syncQueue.enqueue(linkedSetOf(SyncLock.MPS_WRITE, SyncLock.MODELIX_READ), SyncDirection.MODELIX_TO_MPS) {
            val sNode = nodeMap.getNode(nodeId)
            if (sNode == null) {
                val message =
                    "Node ($nodeId) is not mapped yet, therefore its Reference Link called $role cannot be changed."
                notifyAndLogError(message)
                return@enqueue null
            }

            val sReferenceLink = sNode.concept.referenceLinks.find { it.name == role }
            if (sReferenceLink == null) {
                val message =
                    "Node ($nodeId)'s Concept (${sNode.concept.name}) does not have Reference Link called $role."
                notifyAndLogError(message)
                return@enqueue null
            }

            val iNode = getNode(nodeId)
            val usesRoleIds = iNode.usesRoleIds()
            val iReferenceLink = iNode.getReferenceLinks().find {
                if (usesRoleIds) {
                    role == it.getUID()
                } else {
                    role == it.getSimpleName()
                }
            }
            val targetINode = iReferenceLink?.let { iNode.getReferenceTarget(it) }
            val targetSNode = targetINode?.let { nodeMap.getNode(it.nodeIdAsLong()) }

            val oldValue = sNode.getReferenceTarget(sReferenceLink)
            if (oldValue != targetSNode) {
                sNode.setReferenceTarget(sReferenceLink, targetSNode)
            }

            null
        }
    }

    override fun propertyChanged(nodeId: Long, role: String) {
        syncQueue.enqueue(linkedSetOf(SyncLock.MPS_WRITE, SyncLock.MODELIX_READ), SyncDirection.MODELIX_TO_MPS) {
            val isMapped = nodeMap.isMappedToMps(nodeId)
            if (!isMapped) {
                val message =
                    "Element represented by Node ($nodeId) is not mapped yet, therefore its $role property cannot be changed."
                notifyAndLogError(message)
                return@enqueue null
            }

            val iNode = getNode(nodeId)
            val usesRoleIds = iNode.usesRoleIds()
            val iProperty = PropertyFromName(role)
            val newValue = iNode.getPropertyValue(iProperty)

            val sNode = nodeMap.getNode(nodeId)
            sNode?.let {
                nodeTransformer.nodePropertyChanged(sNode, role, nodeId, newValue, usesRoleIds)
                return@enqueue null
            }

            val sModel = nodeMap.getModel(nodeId)
            sModel?.let {
                modelTransformer.modelPropertyChanged(sModel, role, newValue, nodeId, usesRoleIds)
                return@enqueue null
            }

            val sModule = nodeMap.getModule(nodeId)
            sModule?.let {
                moduleTransformer.modulePropertyChanged(role, nodeId, sModule, newValue, usesRoleIds)
                return@enqueue null
            }

            val message = "Property setting case for Node ($nodeId) and property ($role) was missed."
            notifyAndLogError(message)

            null
        }
    }

    override fun nodeRemoved(nodeId: Long) {
        syncQueue.enqueue(linkedSetOf(SyncLock.MPS_WRITE), SyncDirection.MODELIX_TO_MPS) {
            val isMapped = nodeMap.isMappedToMps(nodeId)
            if (!isMapped) {
                logger.info { "Element represented by Node ($nodeId) is already removed." }
                return@enqueue null
            }

            val sNode = nodeMap.getNode(nodeId)
            sNode?.let {
                nodeTransformer.nodeDeleted(it, nodeId)
                return@enqueue null
            }

            val sModel = nodeMap.getModel(nodeId)
            sModel?.let {
                modelTransformer.modelDeleted(sModel, nodeId)
                return@enqueue null
            }

            val sModule = nodeMap.getModule(nodeId)
            sModule?.let {
                moduleTransformer.moduleDeleted(sModule, nodeId)
                return@enqueue null
            }

            val outgoingModelReference = nodeMap.getOutgoingModelReference(nodeId)
            outgoingModelReference?.let {
                modelTransformer.modeImportDeleted(it)
                return@enqueue null
            }

            val outgoingModuleReferenceFromModel = nodeMap.getOutgoingModuleReferenceFromModel(nodeId)
            outgoingModuleReferenceFromModel?.let {
                modelTransformer.moduleDependencyOfModelDeleted(it, nodeId)
                return@enqueue null
            }

            val outgoingModuleReferenceFromModule = nodeMap.getOutgoingModuleReferenceFromModule(nodeId)
            outgoingModuleReferenceFromModule?.let {
                moduleTransformer.outgoingModuleReferenceFromModuleDeleted(outgoingModuleReferenceFromModule, nodeId)
                return@enqueue null
            }

            val message = "A removal case for Node ($nodeId) was missed."
            notifyAndLogError(message)

            null
        }
    }

    override fun nodeAdded(nodeId: Long) {
        syncQueue.enqueue(linkedSetOf(SyncLock.MPS_WRITE, SyncLock.MODELIX_READ), SyncDirection.MODELIX_TO_MPS) {
            val isMapped = nodeMap.isMappedToMps(nodeId)
            if (isMapped) {
                logger.info { "Node ($nodeId) is already mapped, therefore it cannot be added again." }
                return@enqueue null
            }

            val iNode = getNode(nodeId)
            if (iNode.isModule()) {
                moduleTransformer.transformToModule(nodeId)
            } else if (iNode.isModuleDependency()) {
                val moduleNodeId = iNode.getModule()?.nodeIdAsLong()
                val parentModule = nodeMap.getModule(moduleNodeId)!!
                require(parentModule is AbstractModule) {
                    val message =
                        "Parent Module ($moduleNodeId) of Node (${iNode.nodeIdAsLong()}) is not an AbstractModule. Therefore Node cannot be added."
                    notifyAndLogError(message)
                    message
                }
                moduleTransformer.transformModuleDependency(nodeId, parentModule)
            } else if (iNode.isModel()) {
                modelTransformer.transformToModel(nodeId)
            } else if (iNode.isModelImport()) {
                modelTransformer.transformModelImport(nodeId)
            } else if (iNode.isSingleLanguageDependency()) {
                nodeTransformer.transformLanguageDependency(nodeId)
            } else if (iNode.isDevKitDependency()) {
                nodeTransformer.transformDevKitDependency(nodeId)
            } else {
                nodeTransformer.transformNode(nodeId)
            }
        }
    }

    /**
     * TODO rethink if we have to limit childrenChanged operation further
     * it is expected to be called after the nodeAdded methods and thereby we have to resolve the modelImports and references
     * However, this method can be also called before/after the nodeDeleted operation. Where, however it does not make sense to resolve the references...
     * (Moreover, there is no guarantee in which order the method of this class will be called, due to the undefined order of changes after the Diff calculation.)
     */
    override fun childrenChanged(nodeId: Long, role: String?) {
        syncQueue.enqueue(linkedSetOf(SyncLock.MPS_WRITE), SyncDirection.MODELIX_TO_MPS) {
            val project = ActiveMpsProjectInjector.activeMpsProject!!
            modelTransformer.resolveModelImports(project.repository)
            nodeTransformer.resolveReferences()
            null
        }
    }

    override fun containmentChanged(nodeId: Long) {
        syncQueue.enqueue(linkedSetOf(SyncLock.MPS_WRITE, SyncLock.MODELIX_READ), SyncDirection.MODELIX_TO_MPS) {
            val nodeIsMapped = nodeMap.isMappedToMps(nodeId)
            if (!nodeIsMapped) {
                val message = "Node ($nodeId) is not mapped yet, therefore it cannot be moved to a new parent."
                notifyAndLogError(message)
                return@enqueue null
            }

            val iNode = getNode(nodeId)
            val newParent = iNode.parent
            if (newParent == null) {
                val message = "Node ($nodeId)'s new parent is null."
                notifyAndLogError(message)
                return@enqueue null
            }
            val newParentId = newParent.nodeIdAsLong()
            val parentIsMapped = nodeMap.isMappedToMps(newParentId)
            if (!parentIsMapped) {
                val message =
                    "Node ($nodeId)'s new parent ($newParentId) is not mapped yet. Therefore Node cannot be moved to a new parent."
                notifyAndLogError(message)
                return@enqueue null
            }

            val containmentLink = iNode.getContainmentLink()
            if (containmentLink == null) {
                val message = "Node ($nodeId)'s containment link is null."
                notifyAndLogError(message)
                return@enqueue null
            }

            val sNode = nodeMap.getNode(nodeId)
            sNode?.let {
                nodeTransformer.nodeMovedToNewParent(newParentId, sNode, containmentLink, nodeId)
                return@enqueue null
            }

            val sModel = nodeMap.getModel(nodeId)
            sModel?.let {
                modelTransformer.modelMovedToNewParent(newParentId, nodeId, sModel)
                return@enqueue null
            }

            val message = "A containment changed case for Node ($nodeId) was missed."
            notifyAndLogError(message)

            null
        }
    }

    private fun getNode(nodeId: Long) = branch.getNode(nodeId)

    private fun notifyAndLogError(message: String) {
        val exception = ModelixToMpsSynchronizationException(message)
        notifierInjector.notifyAndLogError(message, exception, logger)
    }
}
