
package android.skymobi.messenger.cache;

/**
 * @ClassName: APPCache
 * @Description: 用于存储临时数据
 * @author Sean.Xie
 * @date 2012-9-19 下午3:43:29
 */
public final class APPCache {

    private static APPCache appCache = null;

    private APPCache() {
    }

    public static APPCache getInstance() {
        if (appCache == null) {
            appCache = new APPCache();
        }
        return appCache;
    }

    /**
     * 云端联系人版本
     */
    private long contactVersion;
    
    /**
     * 是否手动登陆过
     */
    private boolean logined;

    public long getContactVersion() {
        return contactVersion;
    }

    public void setContactVersion(long contactVersion) {
        this.contactVersion = contactVersion;
    }

    /**
     * 设置手动登陆过,手动登陆过,接收到注册成功消息,不做处理
     * @param flag
     */
    public void setManualLogined(boolean flag) {
        logined = flag;
    }

    public boolean isManualLogined() {
        return logined;
    }

}
