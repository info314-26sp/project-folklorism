package Card_And_Message_Stuff;
import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;
    private final String sender;
    private final String receiver;
    private final String content;

    public Message(MessageType type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }
}
