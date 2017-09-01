package p2pserver.core

import javafx.scene.control.TextArea
import p2pclient.core.message.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerConnection : Runnable {

    private var serverSocket: ServerSocket = ServerSocket()
    private var connections = HashMap<String, ConnectionThread>()
    private var console: TextArea? = null

    override fun run() {
        log("Accepting connections..")
        acceptConnections()
    }

    fun init(ip: String, port: Int, console: TextArea?) {
        this.console = console
        log("ServerConnection running at [$ip : $port]")
        serverSocket.bind(InetSocketAddress(port))
    }

    fun log(msg: String) {
        console?.appendText("\n ${Date()} : " + msg)
        println("${Date()} :" + msg)
    }

    private fun acceptConnections() {
        try {
            while (!Thread.interrupted()) {
                handleConnection(serverSocket.accept())
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    System.exit(1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            System.exit(1)
        }
    }

    /**
     * Handle connection request
     * */
    private fun handleConnection(socket: Socket) {
        log("Connection from " + socket.inetAddress)
        try {
            val conThread = ConnectionThread(socket)
            conThread.start()
        } catch (e: Exception) {
            e.printStackTrace()
            System.exit(1)
        }
    }

    /**
     * Connection Listener
     * */
    inner class ConnectionThread(var socket: Socket) : Thread() {
        var input = ObjectInputStream(socket.inputStream)
        var output = ObjectOutputStream(socket.outputStream)
        var userName: String = "anonymous"

        init {
            isDaemon = true
        }

        override fun run() {
            //listen for incoming messages
            while (!socket.isClosed) {
                try {
                    val msg = input.readObject() as Message
                    if (msg.type == MessageType.CONNECTION_TEST) {
                        log("${socket.inetAddress} : Connection test -> [${msg.sender}] ")
                        if (connections.containsKey(msg.sender)) {
                            write(Message {
                                sender = "Server"
                                type = MessageType.CONNECTION
                                content = Connection(ConnectionType.DENIED, msg.sender)
                            })
                            log("${socket.inetAddress} : Connection denied!")
                        } else {
                            userName = msg.sender
                            //send grant response
                            write(Message {
                                sender = "Server"
                                type = MessageType.CONNECTION
                                content = Connection(ConnectionType.GRANTED, msg.sender)
                            })
                            connections.put(msg.sender, this) //add to users list
                            log("${socket.inetAddress} : Connection granted!")
                        }
                    } else {
                        handleMessage(msg, userName)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    close()
                }
            }
        }

        /**
         * write to this connection
         * */
        fun write(msg: Message) {
            try {
                output.writeObject(msg)
                output.reset()
            } catch (ex: Exception) {
                ex.printStackTrace()
                close()
            }
        }

        /**
         * close this connection
         * */
        @Suppress("DEPRECATION")
        fun close() {
            if (socket.isConnected) {
                try {
                    broadCast(Message {
                        sender = "Server"
                        content = Text("$userName disconnected!")
                    }, userName)
                    broadCast(Message() {
                        type = MessageType.CONNECTION
                        content = Connection(ConnectionType.DISCONNECTED, userName)
                    }, userName)
                    socket.close()
                    log("${socket.inetAddress} : [$userName] Connection closed")
                    connections.remove(userName)
                } catch (e: Exception) {
                    e.printStackTrace()
                    System.exit(1)
                }
            }
        }
    }

    /**
     * Handle new Connection
     * */
    private fun handleNewConnection(user: String, thread: ConnectionThread) {
        try {
            //welcome new user
            thread.write(Message {
                sender = "Server"
                content = Text("Welcome " + user)
            })
            //send all connected user
            connections.forEach { s, conn ->
                if (s !== user) {
                    thread.write(Message {
                        sender = "Server"
                        type = MessageType.CONNECTION
                        content = Connection(ConnectionType.CONNECTED, s)
                    })
                }
            }
            //notify others for the new connection
            broadCast(Message {
                sender = "Server"
                type = MessageType.CONNECTION
                content = Connection(ConnectionType.CONNECTED, user)
            }, user)
            broadCast(Message {
                sender = "Server"
                content = Text("$user joined the chat room.")
            }, user)

            log("${thread.socket.inetAddress} : [$user] connected!")
        } catch (e: Exception) {
            log("Error handling connection : " + e)
            thread.close()
        }
    }

    /**
     * Handle new received message
     * */
    private fun handleMessage(msg: Message, msgSender: String) {
        when (msg.type) {
            MessageType.CONNECTION -> {
                val conn = msg.content as Connection
                when (conn.type) {
                    ConnectionType.CONNECTED -> {
                        handleNewConnection(msgSender, connections[msgSender]!!)
                    }
                    else -> {
                    }
                }
            }
            MessageType.TEXT -> {
                when (msg.sendTo) {
                    "all" -> broadCast(msg, msgSender)
                    else -> sendTo(msg.sendTo, msg)
                }
            }
            else -> {

            }
        }
    }

    /**
     * send to all except from the given user
     * */
    fun broadCast(msg: Message, user: String) {
        connections.forEach { key, thread ->
            if (key != user) {
                thread.write(msg)
            }
        }
    }

    /**
     * send to all connection
     * */
    fun broadCast(msg: Message) {
        connections.forEach { key, thread ->
            thread.write(msg)
        }
    }

    /**
     * send to specific connection
     * */
    fun sendTo(user: String, msg: Message) {
        connections[user]?.write(msg)
    }

    /**
     * disconnect client
     * */
    fun disconnectClient(name: String) {
        connections[name]?.let { it.close() }
    }

    fun close() {
        if (!serverSocket.isClosed) {
            log("Closing all connections..")
            val users = ArrayList<String>()
            connections.keys.forEach { users.add(it) }
            users.forEach {
                connections[it]?.close()
            }
            log("Connection closed!!")
            serverSocket.close()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val s = ServerConnection()
            s.init("localhost", 6666, null)
            Thread(s).start()
        }
    }
}