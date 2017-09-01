package p2pclient.core

import p2pclient.core.message.Message
import javafx.scene.control.Alert
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Created by Arabis on 10/22/2016.
 */
open class ClientConnection : Thread() {

    private var socket: Socket
    private lateinit var output: ObjectOutputStream
    private lateinit var input: ObjectInputStream

    private var receiveCallback: ((Message) -> Unit)? = null

    init {
        socket = Socket()
    }

    fun connect(host: String, port: Int): Boolean {
        try {
            socket.connect(InetSocketAddress(host, port))
            output = ObjectOutputStream(socket.outputStream)
            input = ObjectInputStream(socket.inputStream)
            return true
        } catch (e: Exception) {
            Alert(Alert.AlertType.ERROR, "No server running at $host:$port").show()
        }
        socket = Socket()
        return false
    }

    override fun run() {
        startReader()
    }

    private fun startReader() {
        while (socket.isConnected && !socket.isClosed) {
            try {
                val msg = input.readObject() as Message
                receiveCallback?.invoke(msg)
            } catch (e: Exception) {
                println("error at reader : $e")
                close()
            }
        }
    }

    fun isConnected(): Boolean {
        return socket.isConnected
    }

    fun write(msg: Message) {
        try {
            output.writeObject(msg)
            output.reset()
        } catch (ex: Exception) {
            println("sending failed! " + ex)
            close()
        }
    }

    fun close() {
        socket.close()
        output.close()
        input.close()
        System.exit(1)
    }

    fun host(): String {
        return socket.inetAddress.hostAddress
    }

    fun port(): Int {
        return socket.port
    }

    fun onReceive(function: ((Message) -> Unit)? = null) {
        receiveCallback = function
    }
}