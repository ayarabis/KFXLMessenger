package p2pclient.core.message

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.File
import java.io.Serializable

/**
 * Created by Arabis on 10/20/2016.
 */
enum class MessageType {
    TEXT,
    STATUS,
    CONNECTION,
    CONNECTION_TEST
}

class Message(op: Message.() -> Unit = {}) : Serializable {

    var sendTo: String = "all"
    var sender: String = ""
    var content: MessageContent = EmptyContent()
    var type: MessageType = MessageType.TEXT
    var attachment: File? = null

    init {
        op.invoke(this)
    }

    override fun toString(): String {
        return "[$sender : $content]"
    }
}

open class MessageContent() : Serializable
class EmptyContent() : MessageContent()
class Text(str: String) : MessageContent() {
    private var value: String = str
    override fun toString(): String {
        return value
    }
}

enum class ConnectionType {
    CONNECTED,
    DISCONNECTED,
    GRANTED,
    DENIED
}

class Connection(val type: ConnectionType, val user: String) : MessageContent()
enum class StatusType(var image: Image) {
    ONLINE(Image("/view/images/online.png")),
    BUSY(Image("/view/images/busy.png")),
    AWAY(Image("/view/images/away.png"));

    fun icon(): ImageView {
        return ImageView(image).apply {
            fitWidth = 10.0
            fitHeight = 10.0
        }
    }
}

class Status(var type: StatusType = StatusType.ONLINE, var user: String) : MessageContent()