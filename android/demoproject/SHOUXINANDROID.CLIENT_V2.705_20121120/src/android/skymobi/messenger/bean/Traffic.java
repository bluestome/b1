
package android.skymobi.messenger.bean;


/**
 * 流量表对象
 * 
 * @ClassName: Traffic
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-3-20 下午03:55:05
 */
public class Traffic implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID **/
    private Integer id;

    /** 日期 **/
    private String date;

    /** 无线网络数据 **/
    private Integer wifi = 0;

    /** 最新无线数据 **/
    private Integer wifiLatest = 0;

    /** 移动网络数据 **/
    private Integer mobile = 0;

    /** 最新移动网络数据 **/
    private Integer mobileLatest = 0;

    /** 应用使用的无线网络数据 **/
    private Integer appWifi = 0;

    /** 应用使用的移动网络数据 **/
    private Integer appMobile = 0;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the wifi
     */
    public Integer getWifi() {
        return wifi;
    }

    /**
     * @param wifi the wifi to set
     */
    public void setWifi(Integer wifi) {
        this.wifi = wifi;
    }

    /**
     * @return the wifiLatest
     */
    public Integer getWifiLatest() {
        return wifiLatest;
    }

    /**
     * @param wifiLatest the wifiLatest to set
     */
    public void setWifiLatest(Integer wifiLatest) {
        this.wifiLatest = wifiLatest;
    }

    /**
     * @return the mobile
     */
    public Integer getMobile() {
        return mobile;
    }

    /**
     * @param mobile the mobile to set
     */
    public void setMobile(Integer mobile) {
        this.mobile = mobile;
    }

    /**
     * @return the mobileLatest
     */
    public Integer getMobileLatest() {
        return mobileLatest;
    }

    /**
     * @param mobileLatest the mobileLatest to set
     */
    public void setMobileLatest(Integer mobileLatest) {
        this.mobileLatest = mobileLatest;
    }

    /**
     * @return the appWifi
     */
    public Integer getAppWifi() {
        return appWifi;
    }

    /**
     * @param appWifi the appWifi to set
     */
    public void setAppWifi(Integer appWifi) {
        this.appWifi = appWifi;
    }

    /**
     * @return the appMobile
     */
    public Integer getAppMobile() {
        return appMobile;
    }

    /**
     * @param appMobile the appMobile to set
     */
    public void setAppMobile(Integer appMobile) {
        this.appMobile = appMobile;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getId()).append("| date=").append(this.getDate()).append("|wifi=")
                .append(this.getWifi()).append("|wifi_latest=");
        sb.append(this.getWifiLatest()).append("|mobile=").append(this.getMobile())
                .append("|mobile_latest=").append(this.getMobileLatest()).append("|app_mobile=");
        sb.append(this.getAppMobile()).append("|app_wifi=").append(this.getAppWifi());
        return sb.toString();
    }

}
