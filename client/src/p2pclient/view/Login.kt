package p2pclient.view

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import javafx.util.Duration
import p2pclient.core.ClientConnection
import p2pclient.core.message.Connection
import p2pclient.core.message.ConnectionType
import p2pclient.core.message.Message
import p2pclient.core.message.MessageType
import p2pclient.util.invoke
import p2pclient.util.onValueChanged
import p2pclient.util.runLater
import p2pclient.util.setAsStageDraggable
import java.util.*

/**
 * Created by Arabis on 10/26/2016.
 */
class Login {

    var root: StackPane
    @FXML private lateinit var background: AnchorPane
    @FXML private lateinit var header: HBox
    @FXML private lateinit var tfUser: TextField
    @FXML private lateinit var tfHost: TextField
    @FXML private lateinit var tfPort: TextField
    @FXML private lateinit var btnSubmit: Button

    init {
        val loader = FXMLLoader(Login::class.java.getResource("/view/Login.fxml"))
        loader.setController(this)
        root = loader.load()
        header.setAsStageDraggable(false)
        tfUser.apply {
            setOnAction { connect(null) }
            textProperty().onValueChanged {
                btnSubmit.isDisable = newValue.isNullOrEmpty()
            }
        }
        (1..30).forEach {
            generateAnimation()
        }
    }

    private fun generateAnimation() {

        val rand = Random()
        val sizeOfSquare = (rand.nextInt(50) + 1).toDouble()
        val speedOfSquare = (rand.nextInt(10) + 5).toDouble()
        val startXPoint = (rand.nextInt(420)).toDouble()
        val startYPoint = (rand.nextInt(350)).toDouble()
        val direction = rand.nextInt(5) + 1

        var moveXAxis: KeyValue? = null
        var moveYAxis: KeyValue? = null
        var r1: Rectangle? = null
        val pw = background.prefWidth
        val ph = background.prefHeight
        when (direction) {
            1 -> {
                // MOVE LEFT TO RIGHT
                r1 = Rectangle(0.0, startYPoint, sizeOfSquare, sizeOfSquare)
                moveXAxis = KeyValue(r1.xProperty(), pw - sizeOfSquare)
            }
            2 -> {
                // MOVE TOP TO BOTTOM
                r1 = Rectangle(startXPoint, 0.0, sizeOfSquare, sizeOfSquare)
                moveYAxis = KeyValue(r1.yProperty(), ph - sizeOfSquare)
            }
            3 -> {
                // MOVE LEFT TO RIGHT, TOP TO BOTTOM
                r1 = Rectangle(startXPoint, 0.0, sizeOfSquare, sizeOfSquare)
                moveXAxis = KeyValue(r1.xProperty(), pw - sizeOfSquare)
                moveYAxis = KeyValue(r1.yProperty(), ph - sizeOfSquare)
            }
            4 -> {
                // MOVE BOTTOM TO TOP
                r1 = Rectangle(startXPoint, ph - sizeOfSquare, sizeOfSquare, sizeOfSquare)
                moveYAxis = KeyValue(r1.yProperty(), 0)
            }
            5 -> {

                // MOVE RIGHT TO LEFT
                r1 = Rectangle(ph - sizeOfSquare, startYPoint, sizeOfSquare, sizeOfSquare)
                moveXAxis = KeyValue(r1.xProperty(), 0)
            }
            6 -> {
                //MOVE RIGHT TO LEFT, BOTTOM TO TOP
                r1 = Rectangle(startXPoint, 0.0, sizeOfSquare, sizeOfSquare)
                moveXAxis = KeyValue(r1.xProperty(), 0)
                moveYAxis = KeyValue(r1.yProperty(), 0)
            }
            else -> println("default")
        }

        r1!!.fill = Color.web("#F89406")
        r1.opacity = 0.1

        val keyFrame = KeyFrame(Duration.millis(speedOfSquare * 1000), moveXAxis, moveYAxis)
        val timeline = Timeline()
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.isAutoReverse = true
        timeline.keyFrames.add(keyFrame)
        timeline.play()
        background.children.add(r1)
    }

    private val connection = ClientConnection()

    @FXML
    private fun connect(e: ActionEvent?) {
        val userName = tfUser.text
        if (userName.isNotEmpty()) {
            val host = if (tfHost.text.isEmpty()) "localhost" else tfHost.text
            val port = if (tfPort.text.isEmpty()) 6666 else tfPort.text.toInt()
            try {
                connection {
                    if (connect(host, port)) {
                        start()
                        onReceive {
                            runLater {
                                if (it.type == MessageType.CONNECTION) {
                                    val conn = it.content as Connection
                                    if (conn.user == userName && conn.type == ConnectionType.GRANTED) {
                                        runLater {
                                            Client(userName, this, "all")
                                        }
                                        (root.scene.window as Stage).close()
                                    } else {
                                        println("username denied!")
                                    }
                                }
                            }
                        }
                        write(Message {
                            sender = userName
                            type = MessageType.CONNECTION_TEST
                        })
                    }
                }
            } catch (e: Exception) {
                println("connection error : $e")
            }
        }
    }

    @FXML
    private fun exit(e: ActionEvent?) {
        Platform.exit()
        System.exit(0)
    }

    @FXML
    private fun minimize(e: ActionEvent?) {
        (root.scene.window as Stage).isIconified = true
    }
}