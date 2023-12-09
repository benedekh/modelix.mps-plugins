package org.modelix.model.mpsplugin.history

import jetbrains.mps.baseLanguage.closures.runtime._FunctionTypes._return_P1_E0
import jetbrains.mps.ide.ui.tree.MPSTree
import jetbrains.mps.ide.ui.tree.MPSTreeNode
import jetbrains.mps.internal.collections.runtime.ISelector
import jetbrains.mps.internal.collections.runtime.NotNullWhereFilter
import jetbrains.mps.internal.collections.runtime.SetSequence
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.geom.Arc2D
import javax.swing.Icon
import javax.swing.Timer

/*Generated by MPS */
class LoadingIcon() : Icon {
    private val activeNodes: Set<MPSTreeNode> = SetSequence.fromSet(HashSet())
    private var angle: Double = 0.0
    private var timer: Timer? = null
    private var inactivity: Int = 0
    fun register(treeNode: MPSTreeNode) {
        SetSequence.fromSet(activeNodes).addElement(treeNode)
        ensureTimerRunning()
    }

    private fun ensureTimerRunning() {
        if (timer == null || !(timer!!.isRunning())) {
            timer = Timer(1000 / 60, object : ActionListener {
                public override fun actionPerformed(e: ActionEvent) {
                    rotate()
                    if (SetSequence.fromSet(activeNodes).isEmpty()) {
                        if (inactivity > 5000 / 60) {
                            SetSequence.fromSet(activeNodes).clear()
                            timer!!.stop()
                            timer = null
                        } else {
                            inactivity++
                        }
                        return
                    }
                    for (c: MPSTree in SetSequence.fromSet(activeNodes)
                        .select(object : ISelector<MPSTreeNode, MPSTree>() {
                            public override fun select(it: MPSTreeNode): MPSTree {
                                return it.getTree()
                            }
                        }).where(NotNullWhereFilter<Any?>() as _return_P1_E0<Boolean?, MPSTree>?).distinct()) {
                        c.repaint()
                    }
                }
            })
            timer!!.start()
        }
    }

    private fun rotate() {
        angle = (angle - 360.0 / 120.0) % 360.0
    }

    public override fun paintIcon(component: Component, graphics: Graphics, x: Int, y: Int) {
        inactivity = 0
        ensureTimerRunning()
        val w: Double = getIconWidth().toDouble()
        val h: Double = getIconHeight().toDouble()
        val g: Graphics2D = graphics.create() as Graphics2D
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g.setStroke(BasicStroke(3.0f))
            g.setColor(Color(80, 80, 80))
            g.draw(Arc2D.Double(2.0 + x, 2.0 + y, w - 4.0, h - 4.0, angle, 250.0, Arc2D.OPEN))
        } finally {
            g.dispose()
        }
    }

    public override fun getIconWidth(): Int {
        return 16
    }

    public override fun getIconHeight(): Int {
        return 16
    }

    companion object {
        private val INSTANCE: LoadingIcon = LoadingIcon()
        fun <T : MPSTreeNode?> apply(treeNode: T): T {
            treeNode!!.setIcon(INSTANCE)
            INSTANCE.register(treeNode)
            return treeNode
        }
    }
}
