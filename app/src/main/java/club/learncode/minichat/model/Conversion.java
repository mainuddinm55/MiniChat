package club.learncode.minichat.model;

public class Conversion {
    private String id;
    private String senderId;
    private String receiverId;
    private String message;
    private String sendTime;
    private boolean seen;

    public Conversion(String id, String senderId, String receiverId, String message, String sendTime, boolean seen) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.sendTime = sendTime;
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
