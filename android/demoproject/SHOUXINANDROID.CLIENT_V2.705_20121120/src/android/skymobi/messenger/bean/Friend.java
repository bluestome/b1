
package android.skymobi.messenger.bean;

/**
 * @ClassName: Friends
 * @Description: 推荐好友列表
 * @author Anson.Yang
 * @date 2012-2-29 上午10:56:41
 */
public class Friend extends Contact {
    private static final long serialVersionUID = 1L;

    private long contactId;
    private String recommendReason; // 推荐理由
    private String detailReason; // 详细理由
    private int contactType; // 朋友类型
    private String talkReason; // 打招呼提示语

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getRecommendReason() {
        return recommendReason;
    }

    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }

    public String getDetailReason() {
        return detailReason;
    }

    public void setDetailReason(String detailReason) {
        this.detailReason = detailReason;
    }

    public int getContactType() {
        return contactType;
    }

    public void setContactType(int contactType) {
        this.contactType = contactType;
    }

    public String getTalkReason() {
        return talkReason;
    }

    public void setTalkReason(String talkReason) {
        this.talkReason = talkReason;
    }

}
