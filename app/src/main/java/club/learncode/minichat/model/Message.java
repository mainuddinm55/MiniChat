package club.learncode.minichat.model;

public class Message {
    private String id;
    private String from;
    private String message;
    private long sendTime;
    private boolean seen;

    public Message() {

    }

    public Message(String id, String from, String message, long sendTime, boolean seen) {
        this.id = id;
        this.from= from;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
