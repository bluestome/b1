
package android.skymobi.messenger.bean;

import android.skymobi.messenger.comparator.ComparatorFactory;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.utils.ImageUtils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @ClassName: Contacts
 * @Description: 联系人
 * @author Sean.Xie
 * @date 2012-2-20 下午1:56:13
 */
public class Contact implements Serializable {
    private static final String SKYID_STRING = "skyid";

    private static final long serialVersionUID = 1L;

    protected long id; // id
    private long localContactId; // 本地联系人ID
    private long cloudId; // 云端id

    private String displayname; // 姓名
    private int sex; // 性别
    private String nickName; // 昵称
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

    private String phone; // 电话
    private int userType; // 联系人类型
    private int skyid; // skyID

    // 同步状态 1:已同步 0:未同步
    private int synced; // 是否同步

    private int deleted; // 删除

    private long lastUpdateTime; // 最后修改时间

    private ArrayList<Account> accounts = new ArrayList<Account>();
    /**
     * 1:新增2:删除 3:修改
     */
    private int actionShouXin;
    private int action;

    public final static int ACTION_ADD = 1; // 云端增加
    public final static int ACTION_DELETE = 2; // 云端删除
    public final static int ACTION_UPDATE = 3; // 云端修改
    public final static int ACTION_LOACL_ADD = 4; // 本地增加
    public static final int ACTION_LOACL_DELETE = 6; // 本地删除
    public static final int ACTION_SHOUXIN_LINK_LOCAL = 7; // 建立于本地和手信的关联
    public static final int ACTION_SHOUXIN_LINK_CLOUD = 8; // 建立于手信和云端的关联
    public static final int ACTION_CLOUD_AND_LOCAL = 9; // 本地和云端有同样的联系人,只保存手信中

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLocalContactId() {
        return localContactId;
    }

    public void setLocalContactId(long localContactId) {
        this.localContactId = localContactId;
    }

    public long getCloudId() {
        return cloudId;
    }

    public void setCloudId(long cloudId) {
        this.cloudId = cloudId;
    }

    public String getDisplayname() {
    	//@zzy 在2.6中将用户名中间的空格保留，之前版本是将所有空格去除，不符合用户需求
        return displayname == null ? "" : displayname.trim();
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSignature() {
        return signature == null ? "" : signature.trim();
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getNote() {
        return note == null ? "" : note.trim();
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPhotoId() {
        if (ImageUtils.isImageUrl(photoId)) {
            return photoId;
        } else {
            return "";
        }
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public int getBlackList() {
        return blackList;
    }

    public void setBlackList(int blackList) {
        this.blackList = blackList;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public boolean isSkyUser() {
        return userType != ContactsColumns.USER_TYPE_LOACL ? true : false;
    }

    public String getSortkey() {
        return sortkey;
    }

    public void setSortkey(String sortkey) {
        this.sortkey = sortkey;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getUserType() {
        return userType;
    }

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

    public int getSkyid() {
        return skyid;
    }

    public void setSkyid(int skyid) {
        this.skyid = skyid;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getActionShouXin() {
        return actionShouXin;
    }

    public void setActionShouXin(int actionShouXin) {
        this.actionShouXin = actionShouXin;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String toPhoneStr() {
        StringBuffer sbuf = new StringBuffer();
        // 对account进行排序
        ArrayList<Account> as = (ArrayList<Account>) accounts.clone();
        ComparatorFactory.SortAccountListCM1(as);
        for (Account account : as) {
            if (account.getPhone() != null)
                sbuf.append(account.getPhone());
        }
        return sbuf.toString();
    }

    public String toAllStr() {
        StringBuffer sbuf = new StringBuffer();
        // 对account进行排序
        ArrayList<Account> as = (ArrayList<Account>) accounts.clone();
        ComparatorFactory.SortAccountListCM2(as);
        for (Account account : as) {
            if (account.getPhone() != null) {
                sbuf.append(account.getPhone());
            } else if (account.getSkyId() > 0) {
                // 为了将skyid与电话区分开来，我们主动加前缀
                sbuf.append(SKYID_STRING + account.getSkyId());
            }
        }
        return sbuf.toString();
    }

    public boolean equals(Contact r) {
        return this.getDisplayname().equals(r.getDisplayname()) &&
                this.toPhoneStr().equals(r.toPhoneStr()) &&
                this.getNote().equals(r.getNote());
    }

}
