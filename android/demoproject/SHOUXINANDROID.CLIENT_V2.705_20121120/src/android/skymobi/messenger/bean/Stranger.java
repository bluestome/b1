
package android.skymobi.messenger.bean;

import java.io.Serializable;

/**
 * 陌生人POJO
 * 
 * @ClassName: Stranger
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-9-5 下午05:36:58
 */
public class Stranger implements Serializable {

    private static final long serialVersionUID = 1L;
    protected long id; // id
    private String displayname; // 姓名
    private int sex; // 性别
    private String signature; // 签名
    private long birthday; // 生日
    private String organization; // 单位
    private String hometown; // 地区
    private String note; // 备注
    private String school; // 学校
    private String photoId; // 头像
    private int blackList; // 黑名单
    private String pinyin; // 拼音
    private String sortkey; // 排序
    private int skyid; // skyID
    private String nickname; // 昵称
    private String skyName; // 斯凯帐号
    private long lastUpdateTime; // 最后修改时间

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the displayname
     */
    public String getDisplayname() {
        return displayname == null ? null : displayname.trim();
    }

    /**
     * @param displayname the displayname to set
     */
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    /**
     * @return the sex
     */
    public int getSex() {
        return sex;
    }

    /**
     * @param sex the sex to set
     */
    public void setSex(int sex) {
        this.sex = sex;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * @return the birthday
     */
    public long getBirthday() {
        return birthday;
    }

    /**
     * @param birthday the birthday to set
     */
    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    /**
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @return the hometown
     */
    public String getHometown() {
        return hometown;
    }

    /**
     * @param hometown the hometown to set
     */
    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the school
     */
    public String getSchool() {
        return school;
    }

    /**
     * @param school the school to set
     */
    public void setSchool(String school) {
        this.school = school;
    }

    /**
     * @return the photoId
     */
    public String getPhotoId() {
        return photoId;
    }

    /**
     * @param photoId the photoId to set
     */
    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    /**
     * @return the blackList
     */
    public int getBlackList() {
        return blackList;
    }

    /**
     * @param blackList the blackList to set
     */
    public void setBlackList(int blackList) {
        this.blackList = blackList;
    }

    /**
     * @return the pinyin
     */
    public String getPinyin() {
        return pinyin;
    }

    /**
     * @param pinyin the pinyin to set
     */
    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    /**
     * @return the sortkey
     */
    public String getSortkey() {
        return sortkey;
    }

    /**
     * @param sortkey the sortkey to set
     */
    public void setSortkey(String sortkey) {
        this.sortkey = sortkey;
    }

    /**
     * @return the skyid
     */
    public int getSkyid() {
        return skyid;
    }

    /**
     * @param skyid the skyid to set
     */
    public void setSkyid(int skyid) {
        this.skyid = skyid;
    }

    /**
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return the skyName
     */
    public String getSkyName() {
        return skyName;
    }

    /**
     * @param skyName the skyName to set
     */
    public void setSkyName(String skyName) {
        this.skyName = skyName;
    }

    /**
     * @return the lastUpdateTime
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * @param lastUpdateTime the lastUpdateTime to set
     */
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
