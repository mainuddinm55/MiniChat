package club.learncode.minichat.model;

public class Friendship {
    private String userId;
    private String friendId;
    private String status;
    private String actionUserId;
    private String requestSendTime;
    private String actionTime;

    public Friendship(String userId, String friendId, String status, String actionUserId, String requestSendTime, String actionTime) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.actionUserId = actionUserId;
        this.requestSendTime = requestSendTime;
        this.actionTime = actionTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionUserId() {
        return actionUserId;
    }

    public void setActionUserId(String actionUserId) {
        this.actionUserId = actionUserId;
    }

    public String getRequestSendTime() {
        return requestSendTime;
    }

    public void setRequestSendTime(String requestSendTime) {
        this.requestSendTime = requestSendTime;
    }

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }
}
