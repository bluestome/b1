
package android.skymobi.messenger.location;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @ClassName: WifiInfo
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-21 上午10:04:11
 */
public class WifiInfo {
    public String mac;
    public short signalStrength;

    /**
     * @return the signalStrength
     */
    public short getSignalStrength() {
        return signalStrength;
    }

    /**
     * @param signalStrength the signalStrength to set
     */
    public void setSignalStrength(short signalStrength) {
        this.signalStrength = signalStrength;
    }

    public WifiInfo() {
    }

    /**
     * @return the mac
     */
    public String getMac() {
        return mac;
    }

    /**
     * @param mac the mac to set
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
