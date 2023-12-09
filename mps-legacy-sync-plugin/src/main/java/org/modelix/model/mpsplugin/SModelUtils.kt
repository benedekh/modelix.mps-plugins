package org.modelix.model.mpsplugin

import jetbrains.mps.extapi.model.SModelDescriptorStub
import jetbrains.mps.internal.collections.runtime.ListSequence
import jetbrains.mps.internal.collections.runtime.Sequence
import org.jetbrains.mps.openapi.language.SLanguage
import org.jetbrains.mps.openapi.model.SModel
import org.jetbrains.mps.openapi.model.SNode
import org.jetbrains.mps.openapi.module.SModuleReference
import java.util.LinkedList

/*Generated by MPS */
object SModelUtils {
    fun getRootsAsList(_this: SModel): List<SNode> {
        val nodes: List<SNode> = ListSequence.fromList(LinkedList())
        for (node: SNode in Sequence.fromIterable(_this.getRootNodes())) {
            ListSequence.fromList(nodes).addElement(node)
        }
        return nodes
    }

    fun addDevKit(_this: SModel?, devKitModuleReference: SModuleReference?) {
        if (_this is SModelDescriptorStub) {
            _this.addDevKit(devKitModuleReference)
        } else {
            throw IllegalStateException("Unable to handle this model " + _this + " (class: " + _this!!.javaClass.getCanonicalName() + ")")
        }
    }

    fun addLanguageImport(_this: SModel?, sLanguage: SLanguage?, version: Int) {
        if (_this is SModelDescriptorStub) {
            val dsmd: SModelDescriptorStub = _this
            dsmd.addLanguage((sLanguage)!!)
            dsmd.setLanguageImportVersion((sLanguage)!!, version)
        } else {
            throw IllegalStateException("Unable to handle this model " + _this + " (class: " + _this!!.javaClass.getCanonicalName() + ")")
        }
    }
}
