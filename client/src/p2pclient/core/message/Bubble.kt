package p2pclient.core.message

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import p2pclient.notification.Notification
import p2pclient.view.ImageViewer
import java.io.File

/**
 * Created by Arabis on 10/20/2016.
 */
class Bubble(var msg: Message, align: Pos? = Pos.TOP_LEFT) : HBox() {

    private val bubbleStyle = "-fx-background-color : -m-blue;" +
            "-fx-background-radius : 10px;" +
            "-fx-padding : 3 10 3 10;" +
            "-fx-text-fill : white;"

    init {
        alignment = align
        padding = Insets(3.0)
        children += VBox().apply {
            alignment = align
            children += Label(msg.sender).apply {
                alignment = align
                style = "-fx-font-size : 8pt"
            }

            children += VBox().apply msgBubble@ {
                alignment = align
                style = bubbleStyle
                if (msg.sender == "Server") style += "-fx-background-color : -m-teal;"
                if (msg.content is Text && !msg.content.toString().isEmpty()) {
                    children += Label(msg.content.toString()).apply {
                        isWrapText = true
                        println(msg.content)
                        style += "-fx-text-fill : white;-fx-padding : 0 5 0 5;"
                    }
                }
                msg.attachment?.let {
                    val file = it
                    val ext = file.extension
                    val fileFilters = arrayListOf("txt", "docx", "zip", "rar", "pptx", "xlsx")
                    val imgFilters = arrayListOf("png", "jpg", "gif")
                    when (true) {
                        ext.existIn(fileFilters) -> {
                            children += Hyperlink(it.name).apply {
                                style += "-fx-text-fill : white;"
                                setOnMouseClicked {
                                    val home = System.getProperty("user.home")
                                    val dialog = FileChooser().apply {
                                        initialDirectory = File(home + "/Downloads")
                                        initialFileName = file.name
                                        extensionFilters.add(FileChooser.ExtensionFilter("File", "*.${file.extension}"))
                                    }
                                    val dir = dialog.showSaveDialog(this@Bubble.scene.window)
                                    if (dir != null) {
                                        file.copyTo(dir)
                                        val notifier = Notification.Notifier.INSTANCE
                                        notifier.notify(Notification("Download", "File Save to '$dir'"))
                                    }
                                }
                            }
                        }
                        ext.existIn(imgFilters) -> {
                            val img = Image(it.inputStream())
                            children += ImageView().apply {
                                fitWidth = 200.0
                                isPreserveRatio = true
                                image = img
                                setOnMouseClicked {
                                    ImageViewer.show(file)
                                }
                            }
                            this@msgBubble.style += "-fx-padding : 10 0"
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    fun <T> T.existIn(list: List<T>): Boolean {
        list.forEach {
            if (it == this) return true
        }
        return false
    }
}