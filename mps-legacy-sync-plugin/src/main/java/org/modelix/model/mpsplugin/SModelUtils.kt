package org.modelix.model.mpsplugin;

/*Generated by MPS */

import java.util.List;
import org.jetbrains.mps.openapi.model.SNode;
import org.jetbrains.mps.openapi.model.SModel;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import java.util.LinkedList;
import jetbrains.mps.internal.collections.runtime.Sequence;
import org.jetbrains.mps.openapi.module.SModuleReference;
import jetbrains.mps.extapi.model.SModelDescriptorStub;
import org.jetbrains.mps.openapi.language.SLanguage;

public class SModelUtils {
  public SModelUtils() {
  }
  public static List<SNode> getRootsAsList(final SModel _this) {
    List<SNode> nodes = ListSequence.fromList(new LinkedList<SNode>());
    for (SNode node : Sequence.fromIterable(_this.getRootNodes())) {
      ListSequence.fromList(nodes).addElement(node);
    }
    return nodes;
  }
  public static void addDevKit(final SModel _this, SModuleReference devKitModuleReference) {
    if (_this instanceof SModelDescriptorStub) {
      SModelDescriptorStub dsmd = ((SModelDescriptorStub) _this);
      dsmd.addDevKit(devKitModuleReference);
    } else {
      throw new IllegalStateException("Unable to handle this model " + _this + " (class: " + _this.getClass().getCanonicalName() + ")");
    }
  }
  public static void addLanguageImport(final SModel _this, SLanguage sLanguage, int version) {
    if (_this instanceof SModelDescriptorStub) {
      SModelDescriptorStub dsmd = ((SModelDescriptorStub) _this);
      dsmd.addLanguage(sLanguage);
      dsmd.setLanguageImportVersion(sLanguage, version);
    } else {
      throw new IllegalStateException("Unable to handle this model " + _this + " (class: " + _this.getClass().getCanonicalName() + ")");
    }
  }
}