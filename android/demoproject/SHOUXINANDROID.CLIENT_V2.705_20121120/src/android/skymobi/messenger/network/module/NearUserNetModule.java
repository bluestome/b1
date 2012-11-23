
package android.skymobi.messenger.network.module;

import android.skymobi.app.net.event.ISXListener;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.util.Log;

import com.skymobi.android.sx.codec.beans.clientbean.NetGetNearByFriendResponse;
import com.skymobi.android.sx.codec.beans.common.Location;
import com.skymobi.android.sx.codec.beans.common.MobileCell;
import com.skymobi.android.sx.codec.beans.common.WifiCell;
import com.skymobi.android.sx.codec.beans.sis.SxGetNearbyReq;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: NearUserNetModule
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-28 上午9:55:26
 */
public class NearUserNetModule extends BaseNetModule {
    private static final String TAG = NearUserNetModule.class.getSimpleName();

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param netClient.getBiz()
     */
    public NearUserNetModule(ISXListener netClient) {
        super(netClient);
    }

    public NetGetNearByFriendResponse getNearUsers(int start, int pageSize, int querySex,
            Location location) {
        Log.i(TAG, "getNearUsers start=" + start + " pageSize=" + pageSize + " Location="
                + location);
        SxGetNearbyReq req = new SxGetNearbyReq();
        req.setStart(start);
        req.setPageSize(pageSize);
        req.setQuerySex((byte) querySex);
        req.setUsex(SettingsPreferences.getSex());
        req.setLocation(location);

        List<MobileCell> mobileCell = new ArrayList<MobileCell>();
        List<WifiCell> wifiCell = new ArrayList<WifiCell>();

        if (start == 1) {
            req.setRecalculated((byte) 1);
            req.setMobileCellArray(mobileCell);
            req.setWifiCellArray(wifiCell);
        }

        if (req.getLocation() != null) {
            req.setAccurateLevel((byte) 1);
        } else {
            req.setAccurateLevel((byte) 0);
        }

        return netClient.getBiz().getNearByFriends(req);
    }
}
