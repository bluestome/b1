
package android.skymobi.messenger.bean;

import java.io.Serializable;

/**
 * @ClassName: Address
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-4-17 上午9:32:12
 */
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;
    private long id;
    private String phone;
    private int skyId;
    private long accountId;

    /**
     * @return the phones
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phones the phones to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the skyId
     */
    public int getSkyId() {
        return skyId;
    }

    /**
     * @param skyId the skyId to set
     */
    public void setSkyId(int skyId) {
        this.skyId = skyId;
    }

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

    // accountId废弃
    // /**
    // * @return the accountId
    // */
    // public long getAccountId() {
    // return accountId;
    // }
    //
    // /**
    // * @param accountId the accountId to set
    // */
    // public void setAccountId(long accountId) {
    // this.accountId = accountId;
    // }

}
