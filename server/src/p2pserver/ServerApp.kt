package p2pserver

import p2pserver.util.invoke
import view.Server
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage


/**
 * Created by Arabis on 10/27/2016.
 */
class ServerApp : Application() {

    override fun start(stage: Stage) {
        val view = Server()
        stage {
            scene = Scene(view.root, Color.TRANSPARENT)
            title = "A's Lan Messenger Server"
            isResizable = false
            centerOnScreen()
            sizeToScene()
            show()
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(ServerApp::class.java,*args)
}