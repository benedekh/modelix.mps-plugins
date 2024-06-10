/*
 * Copyright (c) 2023-2024.
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

package org.modelix.mps.sync.transformation.cache

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import org.jetbrains.mps.openapi.model.SModel
import org.jetbrains.mps.openapi.model.SModelId
import org.jetbrains.mps.openapi.model.SModelReference
import org.jetbrains.mps.openapi.model.SNode
import org.jetbrains.mps.openapi.module.SModule
import org.jetbrains.mps.openapi.module.SModuleId
import org.jetbrains.mps.openapi.module.SModuleReference
import org.jetbrains.mps.openapi.module.SRepository
import org.modelix.kotlin.utils.UnstableModelixFeature
import org.modelix.mps.sync.transformation.cache.MpsToModelixMap.clear
import org.modelix.mps.sync.transformation.cache.MpsToModelixMap.isEmpty
import org.modelix.mps.sync.transformation.cache.MpsToModelixMap.isMappedToModelix
import org.modelix.mps.sync.transformation.cache.MpsToModelixMap.isMappedToMps
import org.modelix.mps.sync.transformation.cache.MpsToModelixMap.remove
import org.modelix.mps.sync.util.synchronizedLinkedHashSet
import org.modelix.mps.sync.util.synchronizedMap

/**
 * WARNING:
 * - use with caution, otherwise this cache may cause memory leaks
 * - if you add a new Map as a field in the class, then please also add it to the [remove], [isMappedToMps],
 * [isMappedToModelix], [isEmpty], [clear] methods below.
 * - if you want to persist the new field into a file, then add it to the [MpsToModelixMap.Serializer.serialize] and
 * [MpsToModelixMap.Serializer.deserialize] methods below.
 */
@UnstableModelixFeature(
    reason = "The new modelix MPS plugin is under construction",
    intendedFinalization = "This feature is finalized when the new sync plugin is ready for release.",
)
object MpsToModelixMap {

    private val nodeToModelixId = synchronizedMap<SNode, Long>()
    private val modelixIdToNode = synchronizedMap<Long, SNode>()

    private val modelToModelixId = synchronizedMap<SModel, Long>()
    private val modelixIdToModel = synchronizedMap<Long, SModel>()

    private val moduleToModelixId = synchronizedMap<SModule, Long>()
    private val modelixIdToModule = synchronizedMap<Long, SModule>()

    private val moduleWithOutgoingModuleReferenceToModelixId = synchronizedMap<ModuleWithModuleReference, Long>()
    private val modelixIdToModuleWithOutgoingModuleReference = synchronizedMap<Long, ModuleWithModuleReference>()

    private val modelWithOutgoingModuleReferenceToModelixId = synchronizedMap<ModelWithModuleReference, Long>()
    private val modelixIdToModelWithOutgoingModuleReference = synchronizedMap<Long, ModelWithModuleReference>()

    private val modelWithOutgoingModelReferenceToModelixId = synchronizedMap<ModelWithModelReference, Long>()
    private val modelixIdToModelWithOutgoingModelReference = synchronizedMap<Long, ModelWithModelReference>()

    private val objectsRelatedToAModel = synchronizedMap<SModel, MutableSet<Any>>()
    private val objectsRelatedToAModule = synchronizedMap<SModule, MutableSet<Any>>()

    fun put(node: SNode, modelixId: Long) {
        nodeToModelixId[node] = modelixId
        modelixIdToNode[modelixId] = node

        node.model?.let { putObjRelatedToAModel(it, node) }
    }

    fun put(model: SModel, modelixId: Long) {
        modelToModelixId[model] = modelixId
        modelixIdToModel[modelixId] = model

        putObjRelatedToAModel(model, model)
    }

    fun put(module: SModule, modelixId: Long) {
        moduleToModelixId[module] = modelixId
        modelixIdToModule[modelixId] = module

        putObjRelatedToAModule(module, module)
    }

    fun put(sourceModule: SModule, moduleReference: SModuleReference, modelixId: Long) {
        val moduleWithOutgoingModuleReference = ModuleWithModuleReference(sourceModule, moduleReference)
        moduleWithOutgoingModuleReferenceToModelixId[moduleWithOutgoingModuleReference] = modelixId
        modelixIdToModuleWithOutgoingModuleReference[modelixId] = moduleWithOutgoingModuleReference

        putObjRelatedToAModule(sourceModule, moduleReference)
    }

    fun put(sourceModel: SModel, moduleReference: SModuleReference, modelixId: Long) {
        val modelWithOutgoingModuleReference = ModelWithModuleReference(sourceModel, moduleReference)
        modelWithOutgoingModuleReferenceToModelixId[modelWithOutgoingModuleReference] = modelixId
        modelixIdToModelWithOutgoingModuleReference[modelixId] = modelWithOutgoingModuleReference

        putObjRelatedToAModel(sourceModel, moduleReference)
    }

    fun put(sourceModel: SModel, modelReference: SModelReference, modelixId: Long) {
        val modelWithOutgoingModelReference = ModelWithModelReference(sourceModel, modelReference)
        modelWithOutgoingModelReferenceToModelixId[modelWithOutgoingModelReference] = modelixId
        modelixIdToModelWithOutgoingModelReference[modelixId] = modelWithOutgoingModelReference

        putObjRelatedToAModel(sourceModel, modelReference)
    }

    private fun putObjRelatedToAModel(model: SModel, obj: Any?) {
        objectsRelatedToAModel.computeIfAbsent(model) { synchronizedLinkedHashSet() }.add(obj!!)
        // just in case, the model has not been tracked yet. E.g. @descriptor models that are created locally but were not synchronized to the model server.
        putObjRelatedToAModule(model.module, model)
    }

    private fun putObjRelatedToAModule(module: SModule, obj: Any?) =
        objectsRelatedToAModule.computeIfAbsent(module) { synchronizedLinkedHashSet() }.add(obj!!)

    operator fun get(node: SNode?) = nodeToModelixId[node]

    operator fun get(model: SModel?) = modelToModelixId[model]

    operator fun get(modelId: SModelId?) =
        modelToModelixId.filter { it.key.modelId == modelId }.map { it.value }.firstOrNull()

    operator fun get(module: SModule?) = moduleToModelixId[module]

    operator fun get(moduleId: SModuleId?) =
        moduleToModelixId.filter { it.key.moduleId == moduleId }.map { it.value }.firstOrNull()

    operator fun get(sourceModel: SModel, moduleReference: SModuleReference) =
        modelWithOutgoingModuleReferenceToModelixId[ModelWithModuleReference(sourceModel, moduleReference)]

    operator fun get(sourceModule: SModule, moduleReference: SModuleReference) =
        moduleWithOutgoingModuleReferenceToModelixId[ModuleWithModuleReference(sourceModule, moduleReference)]

    operator fun get(sourceModel: SModel, modelReference: SModelReference) =
        modelWithOutgoingModelReferenceToModelixId[ModelWithModelReference(sourceModel, modelReference)]

    fun getNode(modelixId: Long?) = modelixIdToNode[modelixId]

    fun getModel(modelixId: Long?) = modelixIdToModel[modelixId]

    fun getModule(modelixId: Long?) = modelixIdToModule[modelixId]

    fun getModule(moduleId: SModuleId) = objectsRelatedToAModule.keys.firstOrNull { it.moduleId == moduleId }

    fun getOutgoingModelReference(modelixId: Long?) = modelixIdToModelWithOutgoingModelReference[modelixId]

    fun getOutgoingModuleReferenceFromModel(modelixId: Long?) = modelixIdToModelWithOutgoingModuleReference[modelixId]

    fun getOutgoingModuleReferenceFromModule(modelixId: Long?) = modelixIdToModuleWithOutgoingModuleReference[modelixId]

    fun remove(modelixId: Long) {
        // is related to node
        modelixIdToNode.remove(modelixId)?.let { nodeToModelixId.remove(it) }

        // is related to model
        modelixIdToModel.remove(modelixId)?.let {
            modelToModelixId.remove(it)
            remove(it)
        }
        modelixIdToModelWithOutgoingModelReference.remove(modelixId)
            ?.let { modelWithOutgoingModelReferenceToModelixId.remove(it) }
        modelixIdToModelWithOutgoingModuleReference.remove(modelixId)
            ?.let { modelWithOutgoingModuleReferenceToModelixId.remove(it) }

        // is related to module
        modelixIdToModule.remove(modelixId)?.let { remove(it) }
        modelixIdToModuleWithOutgoingModuleReference.remove(modelixId)
            ?.let { moduleWithOutgoingModuleReferenceToModelixId.remove(it) }
    }

    fun remove(model: SModel) {
        modelToModelixId.remove(model)?.let { modelixIdToModel.remove(it) }
        objectsRelatedToAModel.remove(model)?.forEach {
            when (it) {
                is SModuleReference -> {
                    val target = ModelWithModuleReference(model, it)
                    modelWithOutgoingModuleReferenceToModelixId.remove(target)
                        ?.let { id -> modelixIdToModelWithOutgoingModuleReference.remove(id) }
                }

                is SModelReference -> {
                    val target = ModelWithModelReference(model, it)
                    modelWithOutgoingModelReferenceToModelixId.remove(target)
                        ?.let { id -> modelixIdToModelWithOutgoingModelReference.remove(id) }
                }

                is SNode -> {
                    nodeToModelixId.remove(it)?.let { modelixId -> modelixIdToNode.remove(modelixId) }
                }
            }
        }
    }

    fun remove(module: SModule) {
        moduleToModelixId.remove(module)?.let { modelixIdToModule.remove(it) }
        objectsRelatedToAModule.remove(module)?.forEach {
            if (it is SModuleReference) {
                val target = ModuleWithModuleReference(module, it)
                moduleWithOutgoingModuleReferenceToModelixId.remove(target)
                    ?.let { id -> modelixIdToModuleWithOutgoingModuleReference.remove(id) }
            } else if (it is SModel) {
                remove(it)
            }
        }
    }

    fun isMappedToMps(modelixId: Long?): Boolean {
        if (modelixId == null) {
            return false
        }
        val idMaps = arrayOf(
            modelixIdToNode,
            modelixIdToModel,
            modelixIdToModule,
            modelixIdToModuleWithOutgoingModuleReference,
            modelixIdToModelWithOutgoingModuleReference,
            modelixIdToModelWithOutgoingModelReference,
        )

        for (idMap in idMaps) {
            if (idMap.contains(modelixId)) {
                return true
            }
        }
        return false
    }

    fun isMappedToModelix(model: SModel) = this[model] != null

    fun isMappedToModelix(module: SModule) = this[module] != null

    fun isMappedToModelix(node: SNode) = this[node] != null

    fun isEmpty() = objectsRelatedToAModel.isEmpty() && objectsRelatedToAModule.isEmpty()

    fun clear() {
        nodeToModelixId.clear()
        modelixIdToNode.clear()
        modelToModelixId.clear()
        modelixIdToModel.clear()
        moduleToModelixId.clear()
        modelixIdToModule.clear()
        moduleWithOutgoingModuleReferenceToModelixId.clear()
        modelixIdToModuleWithOutgoingModuleReference.clear()
        modelWithOutgoingModuleReferenceToModelixId.clear()
        modelixIdToModelWithOutgoingModuleReference.clear()
        modelWithOutgoingModelReferenceToModelixId.clear()
        modelixIdToModelWithOutgoingModelReference.clear()
        objectsRelatedToAModel.clear()
        objectsRelatedToAModule.clear()
    }

    /**
     * Serializes the [MpsToModelixMap] class to JSON.
     *
     * Before using the serializer, do not forget to enable `allowStructuredMapKeys = true` in your JSON builder.
     * I.e. `Json { allowStructuredMapKeys = true }` or use [MpsToModelixMap.Serializer.DEFAULT_JSON_BUILDER].
     *
     * The serialized `MpsToModelixMap` will look like this:
     *
     * ```
     * {
     *    "FIELD_NAME_1":[{KEY_1_1}, VALUE_1_1, {KEY_1_2}, VALUE_1_2],
     *    "FIELD_NAME_2":[{KEY_2_1}, VALUE_2_1, {KEY_2_2}, VALUE_2_2],
     * }
     * ```
     *
     * where `FIELD_NAME_x` is the name of a field in [MpsToModelixMap]. Because all Map fields have a composite key,
     * therefore kotlinx serializes them in an array like `[{KEY_1}, VALUE_1, {KEY_2}, VALUE_2, ...]`. KEY_1 is the
     * JSON-serialized composite key and VALUE_1 is the serialized primitive value (in this case Long).
     *
     * Note, that if the serialized [MpsToModelixMap] is stored as a String, then `"` will be escaped as `&quot;` or
     * `\"`. Therefore, before debugging, replace all `&quot;` and `\"` with `"` to make the escaped serialized string
     * human-readable.
     */
    @UnstableModelixFeature(
        reason = "The new modelix MPS plugin is under construction",
        intendedFinalization = "This feature is finalized when the new sync plugin is ready for release.",
    )
    class Serializer(repository: SRepository) : KSerializer<MpsToModelixMap> {

        companion object {
            val DEFAULT_JSON_BUILDER = Json { allowStructuredMapKeys = true }
        }

        private val nodeToModelixIdSerializer = MapSerializer(SNodeSerializer(repository), LongAsStringSerializer)
        private val modelToModelixIdSerializer = MapSerializer(SModelSerializer(repository), LongAsStringSerializer)
        private val moduleToModelixIdSerializer = MapSerializer(SModuleSerializer(repository), LongAsStringSerializer)
        private val moduleWithOutgoingModuleReferenceToModelixIdSerializer =
            MapSerializer(ModuleWithModuleReferenceSerializer(repository), LongAsStringSerializer)
        private val modelWithOutgoingModuleReferenceToModelixIdSerializer =
            MapSerializer(ModelWithModuleReferenceSerializer(repository), LongAsStringSerializer)
        private val modelWithOutgoingModelReferenceToModelixIdSerializer =
            MapSerializer(ModelWithModelReferenceSerializer(repository), LongAsStringSerializer)

        override val descriptor = buildClassSerialDescriptor(MpsToModelixMap::class.simpleName!!) {
            element("nodeToModelixId", nodeToModelixIdSerializer.descriptor)
            element("modelToModelixId", modelToModelixIdSerializer.descriptor)
            element("moduleToModelixId", moduleToModelixIdSerializer.descriptor)
            element(
                "moduleWithOutgoingModuleReferenceToModelixId",
                moduleWithOutgoingModuleReferenceToModelixIdSerializer.descriptor,
            )
            element(
                "modelWithOutgoingModuleReferenceToModelixId",
                modelWithOutgoingModuleReferenceToModelixIdSerializer.descriptor,
            )
            element(
                "modelWithOutgoingModelReferenceToModelixId",
                modelWithOutgoingModelReferenceToModelixIdSerializer.descriptor,
            )
        }

        override fun serialize(encoder: Encoder, value: MpsToModelixMap) = encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, nodeToModelixIdSerializer, nodeToModelixId)
            encodeSerializableElement(descriptor, 1, modelToModelixIdSerializer, modelToModelixId)
            encodeSerializableElement(descriptor, 2, moduleToModelixIdSerializer, moduleToModelixId)
            encodeSerializableElement(
                descriptor,
                3,
                moduleWithOutgoingModuleReferenceToModelixIdSerializer,
                moduleWithOutgoingModuleReferenceToModelixId,
            )
            encodeSerializableElement(
                descriptor,
                4,
                modelWithOutgoingModuleReferenceToModelixIdSerializer,
                modelWithOutgoingModuleReferenceToModelixId,
            )
            encodeSerializableElement(
                descriptor,
                5,
                modelWithOutgoingModelReferenceToModelixIdSerializer,
                modelWithOutgoingModelReferenceToModelixId,
            )
        }

        override fun deserialize(decoder: Decoder): MpsToModelixMap {
            return decoder.decodeStructure(descriptor) {
                var nodeToModelixIdLocal = mapOf<SNode, Long>()
                var modelToModelixIdLocal = mapOf<SModel, Long>()
                var moduleToModelixIdLocal = mapOf<SModule, Long>()
                var moduleWithOutgoingModuleReferenceToModelixIdLocal = mapOf<ModuleWithModuleReference, Long>()
                var modelWithOutgoingModuleReferenceToModelixIdLocal = mapOf<ModelWithModuleReference, Long>()
                var modelWithOutgoingModelReferenceToModelixIdLocal = mapOf<ModelWithModelReference, Long>()

                // 1. deserialize all values locally
                loop@ while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        DECODE_DONE -> break@loop
                        0 -> nodeToModelixIdLocal = decodeSerializableElement(descriptor, 0, nodeToModelixIdSerializer)
                        1 ->
                            modelToModelixIdLocal =
                                decodeSerializableElement(descriptor, 1, modelToModelixIdSerializer)

                        2 ->
                            moduleToModelixIdLocal =
                                decodeSerializableElement(descriptor, 2, moduleToModelixIdSerializer)

                        3 -> moduleWithOutgoingModuleReferenceToModelixIdLocal = decodeSerializableElement(
                            descriptor,
                            3,
                            moduleWithOutgoingModuleReferenceToModelixIdSerializer,
                        )

                        4 -> modelWithOutgoingModuleReferenceToModelixIdLocal = decodeSerializableElement(
                            descriptor,
                            4,
                            modelWithOutgoingModuleReferenceToModelixIdSerializer,
                        )

                        5 -> modelWithOutgoingModelReferenceToModelixIdLocal = decodeSerializableElement(
                            descriptor,
                            5,
                            modelWithOutgoingModelReferenceToModelixIdSerializer,
                        )

                        else -> throw SerializationException("Unexpected index $index")
                    }
                }

                // 2. load these values into the map
                nodeToModelixIdLocal.forEach { put(it.key, it.value) }
                modelToModelixIdLocal.forEach { put(it.key, it.value) }
                moduleToModelixIdLocal.forEach { put(it.key, it.value) }
                moduleWithOutgoingModuleReferenceToModelixIdLocal.forEach {
                    put(
                        it.key.source,
                        it.key.moduleReference,
                        it.value,
                    )
                }
                modelWithOutgoingModuleReferenceToModelixIdLocal.forEach {
                    put(
                        it.key.source,
                        it.key.moduleReference,
                        it.value,
                    )
                }
                modelWithOutgoingModelReferenceToModelixIdLocal.forEach {
                    put(
                        it.key.source,
                        it.key.modelReference,
                        it.value,
                    )
                }

                MpsToModelixMap
            }
        }
    }
}

@UnstableModelixFeature(
    reason = "The new modelix MPS plugin is under construction",
    intendedFinalization = "This feature is finalized when the new sync plugin is ready for release.",
)
data class ModelWithModelReference(val source: SModel, val modelReference: SModelReference)

@UnstableModelixFeature(
    reason = "The new modelix MPS plugin is under construction",
    intendedFinalization = "This feature is finalized when the new sync plugin is ready for release.",
)
data class ModelWithModuleReference(val source: SModel, val moduleReference: SModuleReference)

@UnstableModelixFeature(
    reason = "The new modelix MPS plugin is under construction",
    intendedFinalization = "This feature is finalized when the new sync plugin is ready for release.",
)
data class ModuleWithModuleReference(val source: SModule, val moduleReference: SModuleReference)
