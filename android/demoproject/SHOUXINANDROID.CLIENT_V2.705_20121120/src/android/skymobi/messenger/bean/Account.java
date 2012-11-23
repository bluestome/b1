
package android.skymobi.messenger.bean;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @ClassName: Account
 * @author Sean.Xie
 * @date 2012-2-23 下午3:46:44
 */
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long contactId;
    private int skyId;
    private String skyAccount;
    private String nickName;
    private String phone;
    private int isMain;

    private String data1;
    private boolean isOnline;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public int getSkyId() {
        return skyId;
    }

    public void setSkyId(int skyId) {
        this.skyId = skyId;
    }

    public String getSkyAccount() {
        return skyAccount;
    }

    public void setSkyAccount(String skyAccount) {
        this.skyAccount = skyAccount;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isMain() {
        return isMain == 1 ? true : false;
    }

    public void setMain(int isMain) {
        this.isMain = isMain;
    }

    public int getMain() {
        return this.isMain;
    }

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Account other = (Account) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
