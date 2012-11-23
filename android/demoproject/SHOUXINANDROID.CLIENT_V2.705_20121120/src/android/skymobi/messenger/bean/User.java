
package android.skymobi.messenger.bean;


/**
 * @ClassName: User
 * @author Sean.Xie
 * @date 2012-3-30 下午9:05:22
 */
public class User extends Contact {
    private static final long serialVersionUID = 1L;

    private long lastFriendTime;

    public long getLastFriendTime() {
        return lastFriendTime;
    }

    public void setLastFriendTime(long lastFriendTime) {
        this.lastFriendTime = lastFriendTime;
    }

}
