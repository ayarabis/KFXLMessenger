package p2pclient.view

import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import p2pclient.core.ClientConnection
import p2pclient.core.message.*
import p2pclient.notification.Notification
import p2pclient.util.find
import p2pclient.util.runLater
import p2pclient.util.setAsStageDraggable
import java.io.File

/**
 * Created by Arabis on 10/20/2016.
 */
open class Client(var myName: String, conn: ClientConnection, var receiver: String) {

    private var root: StackPane
    @FXML private lateinit var chatBox: VBox
    @FXML private lateinit var messageBox: TextArea
    @FXML private lateinit var lblFileName: Label
    @FXML private lateinit var viewPort: ScrollPane

    private val attachProperty = SimpleObjectProperty<File>()
    private var attachFile: File?
        get() = attachProperty.get()
        set(value) {
            attachProperty.set(value)
            value?.let { lblFileName.text = attachFile?.name }
            lblFileName.isVisible = value != null
        }

    private data class User(val name: String, var status: StatusType)

    private var notifier = Notification.Notifier.INSTANCE
    private var msgSound: MediaPlayer? = null
    private var onHold = false

    private var connection: ClientConnection? = null //main connection

    private val stage = Stage()

    init {
        val loader = FXMLLoader(Client::class.java.getResource("/view/Client.fxml"))
        loader.setController(this)
        root = loader.load()
        myName = myName
        connection = conn
        connection?.onReceive {
            handleMessage(it)
        }

        messageBox.setOnKeyPressed {
            if (it.code == KeyCode.ENTER) {
                if (it.isShiftDown) {
                    messageBox.appendText("\n")
                    messageBox.positionCaret(messageBox.text.count())
                } else {
                    sendMessage(ActionEvent())
                }
            }
        }
        viewPort.apply {
            setOnScroll { onHold = true }
            setOnScrollFinished { onHold = false }
        }

        chatBox.heightProperty().addListener({ e ->
            if (!onHold) viewPort.vvalue = 1.0
        })
        root.find<Pane>(".header")?.setAsStageDraggable(false)
        notifier.popupLifetime = Duration.seconds(5.0)
        val clip = Media(javaClass.getResource("/sounds/notification.wav").toString())
        msgSound = MediaPlayer(clip)

        stage.apply {
            scene = Scene(root, Color.TRANSPARENT)
            initStyle(StageStyle.TRANSPARENT)
            centerOnScreen()
            show()
        }
        connection?.write(Message {
            sender = myName
            type = MessageType.CONNECTION
            content = Connection(ConnectionType.CONNECTED, myName)
        })
    }

    private val msgIcon = Image("/notification/message.png")
    private fun handleMessage(msg: Message) {
        runLater {
            when (msg.type) {
                MessageType.TEXT -> {
                    msgSound?.stop()
                    msgSound?.play()
                    if (stage.isIconified) {
                        //show tray notification if stage is minimize
                        notifier.notify(Notification(
                                "New Message from : ${msg.sender}",
                                "${msg.content as Text}",
                                msgIcon, {
                            stage.isIconified = false
                        }))
                    }
                    println("new message from ${msg.sender} : ${msg.content as Text}")
                    chatBox.children.add(Bubble(msg))
                }
                MessageType.STATUS -> {
                    println("user status changed")
                }
                MessageType.CONNECTION -> {
                    msgSound?.play()
                    val conn = msg.content as Connection
                    when (conn.type) {
                        ConnectionType.CONNECTED -> {
                            println("new user added")
                        }
                        ConnectionType.DISCONNECTED -> {
                            println("user disconnected")
                        }
                        else -> {
                            //TODO
                        }
                    }
                }
                else -> {
                    //TODO
                }
            }
        }
    }

    private fun composeMessage(op: Message.() -> Unit = {}): Message {
        return Message {
            sendTo = receiver
            sender = myName //thread identifier
            type = MessageType.TEXT //default type
            content = Text(messageBox.text.trim())
            attachment = attachFile
            op.invoke(this)
        }
    }

    @FXML
    private fun sendMessage(e: ActionEvent) {
        val msg = composeMessage()
        if (!messageBox.text.isNullOrBlank() || msg.attachment != null) {
            connection?.write(msg)
            runLater {
                chatBox.children.add(Bubble(msg.apply { sender = "You" }, Pos.TOP_RIGHT))
                messageBox.clear()
            }
        }
        messageBox.requestFocus()
        attachFile = null
    }

    @FXML
    fun minimize(e: ActionEvent) {
        stage.isIconified = true
    }

    @FXML
    fun exit(e: ActionEvent?) {
        System.exit(0)
    }

    @FXML
    private fun attachImage(e: ActionEvent) {
        val dialog = FileChooser()
        dialog.apply {
            extensionFilters.add(FileChooser.ExtensionFilter("Image File", "*.jpg", "*.png"))
        }
        val file = dialog.showOpenDialog(root.scene.window)
        if (file != null) {
            attachFile = file
        }
    }

    @FXML
    private fun attachFile(e: ActionEvent) {
        val dialog = FileChooser()
        dialog.apply {
            val ext = "*.txt,*.docx,*.zip,*.rar,*.php,*.html,*.java,*.jar"
            extensionFilters.add(FileChooser.ExtensionFilter("File", ext.split(",")))
        }
        val file = dialog.showOpenDialog(root.scene.window)
        if (file != null) {
            attachFile = file
        }
    }

    @FXML
    private fun removeAttachment(e: MouseEvent) {
        attachFile = null
    }
}
