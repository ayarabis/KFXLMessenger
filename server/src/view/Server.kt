package view

import p2pserver.core.ServerConnection
import p2pserver.util.onValueChanged
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


/**
 * Created by Arabis on 10/20/2016.
 */
class Server {

    var root: BorderPane
    @FXML private lateinit var txtIP: TextField
    @FXML private lateinit var txtPort: TextField
    @FXML private lateinit var console: TextArea

    @FXML private lateinit var btnStart: Button
    @FXML private lateinit var btnClose: Button

    private var server = ServerConnection()

    init {
        val loader = FXMLLoader(Server::class.java.getResource("/Server.fxml"))
        loader.setController(this)
        root = loader.load()
        root.sceneProperty().onValueChanged {
            newValue?.let {
                (it.window as Stage?)?.setOnCloseRequest {
                    it.consume()
                    server.close()
                    System.exit(0)
                }
            }
        }
        txtIP.text = getMyIP().hostAddress
    }

    @FXML
    fun startConnection(e: ActionEvent) {
        if (txtPort.text.isNotEmpty()) {
            console.appendText("${Date()} : Connection started!")
            server.init(txtIP.text, Integer.parseInt(txtPort.text), console)
            val t = Thread(server)
            t.isDaemon = true
            t.start()
            btnStart.isDisable = true
            btnClose.isDisable = false
        }
        e.consume()
    }

    @FXML
    private fun closeConnection(e: ActionEvent) {
        server.close()
        btnClose.isDisable = true
        btnStart.isDisable = false
        server = ServerConnection()
        e.consume()
    }

    @FXML
    private fun clearConsole(e: ActionEvent) {
        console.clear()
        e.consume()
    }

    private fun getMyIP(): InetAddress {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val i = en.nextElement() as NetworkInterface
            val en2 = i.inetAddresses
            while (en2.hasMoreElements()) {
                val address = en2.nextElement() as InetAddress
                if (!address.isLoopbackAddress) {
                    if (address is Inet4Address) {
                        return address
                    }
                }
            }
        }
        return InetAddress.getByName("localhost")
    }
}