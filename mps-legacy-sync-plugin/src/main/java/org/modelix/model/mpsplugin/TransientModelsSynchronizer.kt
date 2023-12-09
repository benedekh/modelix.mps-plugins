package org.modelix.model.mpsplugin;

/*Generated by MPS */

import org.jetbrains.mps.openapi.model.SModel;
import org.jetbrains.mps.openapi.model.SModelId;

public class TransientModelsSynchronizer extends ModelsSynchronizer {


  public TransientModelsSynchronizer(long cloudParentId, CloudTransientModule module) {
    super(cloudParentId, module);
  }

  @Override
  public CloudTransientModule getModule() {
    return (CloudTransientModule) super.getModule();
  }

  @Override
  protected SModel createModel(String name, SModelId id, long modelNodeId) {
    CloudTransientModel model = new CloudTransientModel(getModule(), name, id, modelNodeId);
    getModule().registerModel(model);
    return model;
  }
}