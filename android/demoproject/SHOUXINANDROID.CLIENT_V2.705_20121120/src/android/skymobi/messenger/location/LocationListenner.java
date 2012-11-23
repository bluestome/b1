
package android.skymobi.messenger.location;

import android.skymobi.messenger.service.CoreService;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.skymobi.android.sx.codec.beans.common.Location;

/**
 * @ClassName: LocationListenner
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-9-9 下午9:14:34
 */
public class LocationListenner implements BDLocationListener {

    private static final String TAG = LocationListenner.class.getSimpleName();

    @Override
    public void onReceiveLocation(BDLocation lc) {
        Log.i(TAG, "onReceiveLocation");
        Location location = new Location();
        location.setLatitude(String.valueOf(lc.getLatitude()));
        location.setLongitude(String.valueOf(lc.getLongitude()));
        if (lc.getLocType() == BDLocation.TypeNetWorkLocation) {
            location.setCity(lc.getCity());
            location.setProvince(lc.getProvince());
            location.setStreet(lc.getStreet());
        }
        CoreService.getInstance().getNearUserModule()
                .getNearUsersBaibu(1, 120, location);
    }

    @Override
    public void onReceivePoi(BDLocation lc) {

    }

}
