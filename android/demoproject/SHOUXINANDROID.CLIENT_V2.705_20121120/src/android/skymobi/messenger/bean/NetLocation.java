
package android.skymobi.messenger.bean;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import android.skymobi.messenger.location.CellIDInfo;
import android.skymobi.messenger.location.WifiInfo;

import com.skymobi.android.sx.codec.beans.common.Location;

/**
 * @ClassName: NetLocation
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-7-14 下午12:26:57
 */
public class NetLocation {
    protected Location location;
    protected List<CellIDInfo> cellIds;
    protected List<WifiInfo> wifis;

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the cellIds
     */
    public List<CellIDInfo> getCellIds() {
        return cellIds;
    }

    /**
     * @param cellIds the cellIds to set
     */
    public void setCellIds(List<CellIDInfo> cellIds) {
        this.cellIds = cellIds;
    }

    /**
     * @return the wifis
     */
    public List<WifiInfo> getWifis() {
        return wifis;
    }

    /**
     * @param wifis the wifis to set
     */
    public void setWifis(List<WifiInfo> wifis) {
        this.wifis = wifis;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
