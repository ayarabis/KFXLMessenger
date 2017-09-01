package p2pclient.view

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import p2pclient.util.setAsStageDraggable
import java.io.File

/**
 * Created by Arabis on 10/24/2016.
 */
class ImageViewer {

    private val stage = Stage()

    private var root: StackPane
    @FXML private lateinit var header: HBox
    @FXML private lateinit var iView: ImageView

    private var file: File? = null

    init {
        val loader = FXMLLoader(ImageViewer::class.java.getResource("/view/ImageViewer.fxml"))
        loader.setController(this)
        root = loader.load()
        stage.scene = Scene(root, Color.TRANSPARENT)
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.centerOnScreen()
        header.setAsStageDraggable()
    }

    fun show(file: File) {
        this.file = file
        val img = Image(file.inputStream())
        val w = img.width
        iView.apply {
            fitWidth = if (w < 500.0) w else 500.0
            isPreserveRatio = true
            image = img
        }
        stage.show()
    }

    @FXML
    private fun saveImage(e: ActionEvent) {
        e.consume()
        val home = System.getProperty("user.home")
        val dialog = FileChooser().apply {
            initialDirectory = File(home + "/Downloads")
            initialFileName = file?.name
            extensionFilters.add(FileChooser.ExtensionFilter("Image", "*.${file?.extension}"))
            selectedExtensionFilter = extensionFilters[0]
        }
        val dir = dialog.showSaveDialog(stage)
        if (dir != null) {
            file?.copyTo(dir)
            println("Image saved to " + dir)
        }
    }

    @FXML
    private fun exit(e: ActionEvent) {
        e.consume()
        stage.hide()
    }

    companion object {
        private var view: ImageViewer? = null
        fun show(img: File) {
            if (view == null) view = ImageViewer()
            view?.show(img)
        }
    }
}
