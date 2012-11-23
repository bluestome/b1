
package android.skymobi.messenger.location;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @ClassName: CellIDInfo
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-21 上午10:03:58
 */
public class CellIDInfo {
    public String cellId;
    public short mobileCountryCode;
    public int mobileNetworkCode;
    public int locationAreaCode;
    public String radioType;

    public CellIDInfo() {
    }

    /**
     * @return the cellId
     */
    public String getCellId() {
        return cellId;
    }

    /**
     * @param cellId the cellId to set
     */
    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    /**
     * @return the mobileCountryCode
     */
    public short getMobileCountryCode() {
        return mobileCountryCode;
    }

    /**
     * @param mobileCountryCode the mobileCountryCode to set
     */
    public void setMobileCountryCode(short mobileCountryCode) {
        this.mobileCountryCode = mobileCountryCode;
    }

    /**
     * @return the mobileNetworkCode
     */
    public int getMobileNetworkCode() {
        return mobileNetworkCode;
    }

    /**
     * @param mobileNetworkCode the mobileNetworkCode to set
     */
    public void setMobileNetworkCode(int mobileNetworkCode) {
        this.mobileNetworkCode = mobileNetworkCode;
    }

    /**
     * @return the locationAreaCode
     */
    public int getLocationAreaCode() {
        return locationAreaCode;
    }

    /**
     * @param locationAreaCode the locationAreaCode to set
     */
    public void setLocationAreaCode(int locationAreaCode) {
        this.locationAreaCode = locationAreaCode;
    }

    /**
     * @return the radioType
     */
    public String getRadioType() {
        return radioType;
    }

    /**
     * @param radioType the radioType to set
     */
    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
