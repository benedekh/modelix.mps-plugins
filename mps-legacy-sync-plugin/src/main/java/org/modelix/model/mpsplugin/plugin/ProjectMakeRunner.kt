package org.modelix.model.mpsplugin.plugin

import jetbrains.mps.baseLanguage.tuples.runtime.MultiTuple
import jetbrains.mps.baseLanguage.tuples.runtime.Tuples
import jetbrains.mps.ide.make.actions.MakeActionParameters
import jetbrains.mps.internal.collections.runtime.ISelector
import jetbrains.mps.internal.collections.runtime.ITranslator2
import jetbrains.mps.internal.collections.runtime.IterableUtils
import jetbrains.mps.internal.collections.runtime.ListSequence
import jetbrains.mps.internal.collections.runtime.Sequence
import jetbrains.mps.make.IMakeService
import jetbrains.mps.make.MakeServiceComponent
import jetbrains.mps.make.MakeSession
import jetbrains.mps.make.resources.IResource
import jetbrains.mps.make.script.IResult
import jetbrains.mps.messages.IMessage
import jetbrains.mps.messages.IMessageHandler
import jetbrains.mps.project.Project
import jetbrains.mps.smodel.ModelAccessHelper
import jetbrains.mps.smodel.resources.MResource
import jetbrains.mps.util.Computable
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.mps.openapi.model.SModel
import org.jetbrains.mps.openapi.module.SModule
import java.util.LinkedList
import java.util.concurrent.Future
import java.util.function.Consumer

/*Generated by MPS */
object ProjectMakeRunner {
    private val LOG: Logger = LogManager.getLogger(ProjectMakeRunner::class.java)
    var DEFAULT_SUCCESS_CONSUMER: Consumer<Tuples._2<String?, List<IMessage?>?>?> =
        object : Consumer<Tuples._2<String, List<IMessage>?>> {
            public override fun accept(res: Tuples._2<String, List<IMessage>?>) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Make messages:")
                }
                for (message: IMessage in ListSequence.fromList(res._1())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("  <MAKE> " + message.getKind() + " " + message.getText())
                    }
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Make Project Success: " + res._0())
                }
            }
        }
    var DEFAULT_FAILURE_CONSUMER: Consumer<Tuples._2<String?, List<IMessage?>?>?> =
        object : Consumer<Tuples._2<String, List<IMessage>?>> {
            public override fun accept(res: Tuples._2<String, List<IMessage>?>) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Make messages:")
                }
                for (message: IMessage in ListSequence.fromList(res._1())) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("  <MAKE> " + message.getKind() + " " + message.getText())
                    }
                }
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Make Project Failure: " + res._0())
                }
            }
        }

    @JvmOverloads
    fun execute(
        mpsProject: Project,
        cleanMake: Boolean,
        modulesToBuild: List<SModule?>? = null,
        success: Consumer<Tuples._2<String?, List<IMessage?>?>?> = DEFAULT_SUCCESS_CONSUMER,
        failure: Consumer<Tuples._2<String?, List<IMessage?>?>?> = DEFAULT_FAILURE_CONSUMER
    ) {
        var modulesToBuild: List<SModule?>? = modulesToBuild
        val messageHandler: MyMessageHandler = MyMessageHandler()
        val session: MakeSession = MakeSession(mpsProject, messageHandler, cleanMake)
        if (modulesToBuild == null) {
            modulesToBuild =
                ListSequence.fromListWithValues(ArrayList(), mpsProject.getProjectModules() as Iterable<SModule?>?)
        }
        val params: MakeActionParameters = MakeActionParameters(mpsProject).modules(modulesToBuild).cleanMake(cleanMake)
        val makeService: IMakeService = mpsProject.getComponent(MakeServiceComponent::class.java).get()
        if (makeService.openNewSession(session)) {
            // empty collection is fine, it's up to make service to report there's nothing to do (odd, but fine for now. Action could have do that instead)
            //
            // ModelValidatorAdapter needs to be refactored not to mix model checking code with UI, which might request
            // write access e.g. on focus lost and eventually lead to 'write from read' issue like
            // FIXME https://youtrack.jetbrains.com/issue/MPS-24020. Proper fix is to split model check into read, and results reporting into EDT.
            // For 3.4 RC, we decided to go with a hack and let SModel instances cross model read boundary
            var inputRes: List<IResource?>? = null
            val models: ArrayList<SModel> = ArrayList()
            try {
                inputRes = ModelAccessHelper(mpsProject.getModelAccess()).runReadAction<List<IResource?>>(object :
                    Computable<List<IResource>> {
                    public override fun compute(): List<IResource> {
                        val rv: List<IResource> = Sequence.fromIterable(params.collectInput()).toListSequence()
                        models.addAll(ListSequence.fromList(rv).translate(object : ITranslator2<IResource, SModel>() {
                            public override fun translate(it: IResource): Iterable<SModel> {
                                return (it as MResource).models()
                            }
                        }).toListSequence())
                        return rv
                    }
                })
            } catch (e: RuntimeException) {
                makeService.closeSession(session)
                e.printStackTrace()
                failure.accept(MultiTuple.from(e.message, messageHandler.messages))
                return
            }
            if (inputRes != null) {
                val result: Future<IResult> = makeService.make(session, inputRes)
                val t: Thread = Thread(object : Runnable {
                    public override fun run() {
                        try {
                            val resultValue: IResult = result.get()
                            val resDesc: String = IterableUtils.join(
                                Sequence.fromIterable(resultValue.output())
                                    .select(object : ISelector<IResource, String>() {
                                        public override fun select(it: IResource): String {
                                            return it.describe()
                                        }
                                    }), ", "
                            )
                            if (resultValue.isSucessful()) {
                                success.accept(
                                    MultiTuple.from(
                                        "make succeeded. Resource: " + resDesc,
                                        messageHandler.messages
                                    )
                                )
                            } else {
                                failure.accept(
                                    MultiTuple.from(
                                        "make failed. Resources: " + resDesc,
                                        messageHandler.messages
                                    )
                                )
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            failure.accept(MultiTuple.from(t.message, messageHandler.messages))
                        }
                    }
                })
                t.start()
            } else {
                makeService.closeSession(session)
                failure.accept(MultiTuple.from("no input", messageHandler.messages))
            }
        }
    }

    private class MyMessageHandler() : IMessageHandler {
        val messages: List<IMessage?> = ListSequence.fromList(LinkedList())
        public override fun handle(message: IMessage) {
            ListSequence.fromList(messages).addElement(message)
        }
    }
}
