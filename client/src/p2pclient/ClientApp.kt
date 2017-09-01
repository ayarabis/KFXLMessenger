package p2pclient

import p2pclient.notification.Notification
import p2pclient.view.Login
import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 * Created by Arabis on 10/20/2016.
 */

class ClientApp : Application() {

    override fun start(stage: Stage) {
        Notification.Notifier.setPopupLocation(null, Pos.BOTTOM_RIGHT)
        val view = Login()
        stage.apply {
            scene = Scene(view.root, 319.0, 508.0, Color.TRANSPARENT)
            initStyle(StageStyle.TRANSPARENT)
            sizeToScene()
            centerOnScreen()
            show()
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(ClientApp::class.java,*args)
}