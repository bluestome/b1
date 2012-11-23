
package android.skymobi.messenger.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: Threads
 * @Description: 手信消息会话
 * @author Sean.Xie
 * @date 2012-2-9 上午11:20:40
 */
public class Threads extends SMSThreads {
    private static final long serialVersionUID = 1L;
    /** 本地短信会话ID */
    private long localThreadsID;
    /** Account ID */
    private String draft;// 当前会话草稿，使用废弃的account_ids列
    private String displayName;
    private String nickName;
    private String addressIds;
    private List<Address> addressList = new ArrayList<Address>();

    public long getLocalThreadsID() {
        return localThreadsID;
    }

    public void setLocalThreadsID(long localThreadsID) {
        this.localThreadsID = localThreadsID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAddressIds() {
        return addressIds;
    }

    public void setAddressIds(String addressIds) {
        this.addressIds = addressIds;
    }

    public void addAddress(Address address) {
        addressList.add(address);
    }

    public void setAddressList(List<Address> list) {
        addressList = list;
    }

    public List<Address> getAddressList() {
        return this.addressList;
    }

    public String getDraft() {
        return draft;
    }

    public void setDraft(String draft) {
        this.draft = draft;
    }

    public void setAddressId(long id) {
        if (addressList.size() <= 0) {
            Address address = new Address();
            address.setId(id);
            addressList.add(address);
        } else {
            addressList.get(0).setId(id);
        }
    }

    public void setPhone(String phone) {
        if (addressList.size() <= 0) {
            Address address = new Address();
            address.setPhone(phone);
            addressList.add(address);
        } else {
            addressList.get(0).setPhone(phone);
        }
    }

    public void setSkyId(int skyId) {
        if (addressList.size() <= 0) {
            Address address = new Address();
            address.setSkyId(skyId);
            addressList.add(address);
        } else {
            addressList.get(0).setSkyId(skyId);
        }
    }
}
