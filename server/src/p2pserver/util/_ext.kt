package p2pserver.util

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.text.DecimalFormat

/**
 * Created by Arabis on 8/15/2016.
 */

fun runLater(function: () -> Unit) {
    Platform.runLater {
        function.invoke()
    }
}

data class Changed<out T>(val oldValue: T, val newValue: T)

/**
 * Create a ChangeListener for the Observable value
 * */
fun <T> ObservableValue<T>.onValueChanged(function: Changed<T>.() -> Unit) {
    addListener { observableValue, oldVal, newVal ->
        if (oldVal != newVal) {
            Changed(oldVal, newVal).function()
        }
    }
}


fun Parent.loadStyle(location: String) {
    stylesheets.add(javaClass.getResource(location).toExternalForm())
}

/**
 * reload the stylesheets of parent
 * */
fun Parent.reloadStyle() {
    val styles = stylesheets.toMutableList()
    stylesheets.clear()
    stylesheets.addAll(styles)
}


/**********
 * AnchorPane helper
 **********/

/**
 * fit the node with its AnchorPane parent
 * */
fun Node.fitAnchor() {
    this.anchor(0, 0, 0, 0)
}

/**
 * Set the anchor of the node within AnchorPane
 * @param t Top
 * @param r Right
 * @param b Bottom
 * @param l Left
 * */
fun Node.anchor(t: Int = -1, r: Int = -1, b: Int = -1, l: Int = -1) {
    if (t >= 0) topAnchor(t)
    if (r >= 0) rightAnchor(r)
    if (b >= 0) bottomAnchor(b)
    if (l >= 0) leftAnchor(l)
}

/**
 * Set the left anchor of the node within AnchorPane
 * */
fun Node.leftAnchor(v: Int) {
    AnchorPane.setLeftAnchor(this, v + 0.0)
}

/**
 * Set the right anchor of the node within AnchorPane
 * */
fun Node.rightAnchor(v: Int) {
    AnchorPane.setRightAnchor(this, v + 0.0)
}

/**
 * Set the top anchor of the node within AnchorPane
 * */
fun Node.topAnchor(v: Int) {
    AnchorPane.setTopAnchor(this, v + 0.0)
}

/**
 * Set the bottom anchor of the node within AnchorPane
 * */
fun Node.bottomAnchor(v: Int) {
    AnchorPane.setBottomAnchor(this, v + 0.0)
}

/**
 * auto format text field accepting only numbers and period
 * */
fun TextField.asNumberFormatField() {
    var value = ""
    val df = DecimalFormat("#,##0.##")
    this.addEventHandler(KeyEvent.KEY_RELEASED, {
        if (!it.code.isDigitKey && it.code != KeyCode.BACK_SPACE) {
            if (it.code == KeyCode.PERIOD) {
                if (text.count { it.equals('.') } > 1) text = value
            } else {
                text = value
            }
        } else {
            value = text
        }
        if (text.isNotEmpty() && !text.endsWith(".")) {
            text = df.format(value.split(",").joinToString("").toDouble())
            positionCaret(text.count())
        }
    })
}

fun parseNum(tf: TextField): Double {
    var value: Double = 0.0
    if (tf.text.isNotEmpty()) {
        value = tf.text.split(",").joinToString("").toDouble()
    }
    return value
}

@Suppress("unchecked_cast")
inline fun <reified T : Node> Node.find(selector: String): T? {
    val result = lookupAll(selector).first()
    if (result is T) {
        return result
    }
    return null
}

operator fun <T> T.invoke(op: T.() -> Unit): T {
    op.invoke(this)
    return this
}

fun <T : TextInputControl> T.isEmpty(n: T): Boolean {
    return this.text.isNullOrEmpty()
}

/**
 * use the node to drag and move the window
 * */
fun Node.setAsStageDraggable(toggleMaximize: Boolean = true) {
    var mouseDragOffsetX = 0.0
    var mouseDragOffsetY = 0.0
    var stage: Stage? = null
    if (toggleMaximize) {
        setOnMouseClicked {
            if (it.clickCount == 2)
                toggleState(this)
            it.consume()
        }
    }
    setOnMousePressed {
        if (stage == null) stage = scene.window as Stage
        mouseDragOffsetX = it.sceneX
        mouseDragOffsetY = it.sceneY
        it.consume()
    }
    setOnMouseDragged {
        cursor = Cursor.MOVE
        stage!!.apply {
            if (!isMaximized) {
                x = it.screenX - mouseDragOffsetX
                y = it.screenY - mouseDragOffsetY
                if (scene.root.opacity == 1.0 && !isMaximized) {
                    fade(0.3, scene.root) {
                        toValue = 0.8
                    }.play()
                }
            }
        }
        it.consume()
    }
    setOnMouseReleased {
        cursor = Cursor.DEFAULT
        fade(0.3, scene.root) {
            toValue = 1.0
        }.play()
    }
}

fun toggleState(node: Node) {
    val stage = node.scene.window as Stage
    if (stage.isMaximized) stage.isMaximized = false else stage.isMaximized = true
}

fun <E> ObservableList<E>.updateWhere(predicate: (E) -> Boolean, update: E.() -> Unit) {
    this.forEach {
        if (predicate.invoke(it)) {
            update.invoke(it)
            return
        }
    }
}

fun <E> ObservableList<E>.removeWhere(function: (E) -> Boolean) {
    this.forEach {
        if (function.invoke(it)) {
            this.remove(it)
            return
        }
    }
}
