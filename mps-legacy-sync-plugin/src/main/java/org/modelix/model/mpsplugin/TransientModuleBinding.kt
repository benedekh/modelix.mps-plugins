package org.modelix.model.mpsplugin

import jetbrains.mps.project.ModuleId
import jetbrains.mps.smodel.MPSModuleRepository
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory
import org.jetbrains.mps.openapi.language.SProperty
import org.jetbrains.mps.openapi.model.SModel
import org.modelix.model.api.IBranch
import org.modelix.model.api.PNodeAdapter
import org.modelix.model.area.PArea
import java.util.concurrent.atomic.AtomicInteger

/*Generated by MPS */
class TransientModuleBinding(moduleNodeId: Long) : ModuleBinding(moduleNodeId, SyncDirection.TO_MPS) {
    private var transientModule: CloudTransientModule? = null
    override val module: CloudTransientModule get() = transientModule!!

    override fun doActivate() {
        val branch: IBranch? = this.branch
        var moduleName: String? = PArea((branch)!!).executeRead({
            PNodeAdapter(moduleNodeId, (branch)).getPropertyValue(
                PROPS.`name$MnvL`.getName()
            )
        })
        val moduleIdStr: String? = PArea((branch)!!).executeRead({
            PNodeAdapter(moduleNodeId, (branch)).getPropertyValue(
                PROPS.`id$7MjP`.getName()
            )
        })
        if ((moduleName == null || moduleName.length == 0)) {
            moduleName = "cloud.module" + NAME_SEQUENCE.incrementAndGet()
        }
        var moduleId: ModuleId =
            ModuleId.foreign(cloudRepository!!.completeId() + "-" + java.lang.Long.toHexString(moduleNodeId))
        if (moduleIdStr != null) {
            val temptativeModuleId: ModuleId = ModuleId.fromString(moduleIdStr)
            // This could happen because someone clone a module to Modelix and then try to bind it.
            // In this case we want to give a warning to the user
            if (CloudTransientModules.instance.isModuleIdUsed(temptativeModuleId)) {
                ModelixNotifications.notifyWarning(
                    "Module ID already used",
                    "We cannot load the module with the ID " + temptativeModuleId + " as the module id seems to be already used. We will load it with module id " + moduleId + " instead"
                )
            } else {
                moduleId = temptativeModuleId
            }
        }
        transientModule = CloudTransientModules.instance.createModule(moduleName, moduleId)
        super.doActivate()
    }

    override fun doDeactivate() {
        super.doDeactivate()
        SharedExecutors.FIXED.execute(object : Runnable {
            public override fun run() {
                synchronized(this@TransientModuleBinding, {
                    MPSModuleRepository.getInstance().getModelAccess().runWriteAction(object : Runnable {
                        public override fun run() {
                            CloudTransientModules.instance.disposeModule(module)
                        }
                    })
                })
            }
        })
    }

    protected override val modelsSynchronizer: Synchronizer<SModel>
        protected get() {
            return TransientModelsSynchronizer(moduleNodeId, module!!)
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

        /*package*/
        val `id$7MjP`: SProperty = MetaAdapterFactory.getProperty(
            0xa7577d1d4e5431dL,
            -0x674e051c70651180L,
            0x69652614fd1c50fL,
            0x3aa34013f2a802e0L,
            "id"
        )
    }

    companion object {
        private val NAME_SEQUENCE: AtomicInteger = AtomicInteger(0)
    }
}
