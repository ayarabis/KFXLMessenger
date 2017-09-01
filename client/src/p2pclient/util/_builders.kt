package p2pclient.util

import javafx.animation.*
import javafx.scene.Node
import javafx.util.Duration

/**
 * Created by Arabis on 6/10/2016.
 */
fun fade(duration: Double, node: Node, block: FadeTransition.() -> Unit = {}): FadeTransition {
    return FadeTransition(Duration.seconds(duration), node).apply({
        block()
    })
}

fun rotate(duration: Double, node: Node, block: RotateTransition.() -> Unit = {}): RotateTransition {
    return RotateTransition(Duration.seconds(duration), node).apply({
        block()
    })
}

fun translate(duration: Double, node: Node, block: TranslateTransition.() -> Unit = {}): TranslateTransition {
    return TranslateTransition(Duration.seconds(duration), node).apply({
        block()
    })
}

fun scale(duration: Double, node: Node, block: ScaleTransition.() -> Unit = {}): ScaleTransition {
    return ScaleTransition(Duration.seconds(duration), node).apply({
        block()
    })
}

fun Node.scale(duration: Double, block: ScaleTransition.() -> Unit = {}) {
    ScaleTransition(Duration.seconds(duration), this).apply({
        block()
    })
}

fun Animation.play(delay: Double) {
    this.delay = Duration.seconds(delay)
}