
package android.skymobi.messenger.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.NetLocation;
import android.skymobi.messenger.service.CoreService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName: MyLocationManager
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-19 下午3:41:32
 */
public class LBSLocationManager {
    private final String TAG = LBSLocationManager.class.getSimpleName();
    private final Context mContext;
    private static boolean init = false;
    private LocationManager locationManager;
    private static final int MINTIME = 1000;
    private static final int MININSTANCE = 0;
    private static LBSLocationManager instance;
    private Location lastLocation = null;
    private LocationCallBack mCallback;
    private List<String> mProviders;
    private CellIDInfoManager cellIDInfoManager;
    private WifiInfoManager wifiInfoManager;
    protected ExecutorService mThreadPool;

    public void init(Context c, LocationCallBack callback) {

        if (!init) {
            init = true;
            // Gps 定位
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);
            cellIDInfoManager = CellIDInfoManager.getInstance();
            wifiInfoManager = WifiInfoManager.getInstance();
            mThreadPool = CoreService.getInstance().getThreadPool();

        }
        mCallback = callback;

    }

    private LBSLocationManager(Context context, LocationCallBack callback) {
        mContext = context;
        mCallback = callback;
        // init(context, callback);
    }

    public static LBSLocationManager getInstance(Context context, LocationCallBack callback) {
        if (null == instance) {
            instance = new LBSLocationManager(context, callback);
        }
        return instance;
    }

    public void updateLocation(Location location) {
        lastLocation = location;
        if (mCallback != null)
            mCallback.onCurrentLocation(location, null);
    }

    private final LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onStatusChanged(String provider, int status, Bundle
                extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            SLog.d(TAG,
                    "onLocationChanged");
            updateLocation(location);
        }
    };

    public void getLocation() {
        // 不使用系统提供的地理位置信息
        // mProviders = locationManager.getProviders(true);
        // for (String provider : mProviders) {
        // locationManager.requestLocationUpdates(provider, MINTIME,
        // MININSTANCE, locationListener);
        // }
        final ArrayList<WifiInfo> wifi =
                wifiInfoManager.getWifiInfo(mContext);
        final ArrayList<CellIDInfo> cellID = cellIDInfoManager.getCellIDInfo(mContext);

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mCallback.onCurrentLocation(null, getLocationByCellWifi(wifi, cellID));
            }
        });

    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void removeUpdates() {
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    public Location getBestLocation() {
        // 获得location系统服务
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        crit.setPowerRequirement(Criteria.POWER_LOW);
        crit.setAltitudeRequired(false);
        crit.setBearingRequired(false);
        crit.setSpeedRequired(false);
        crit.setCostAllowed(true);

        // 获得最好的一个prodiver
        String pro = locationManager.getBestProvider(crit, true);
        Location location = null;
        if (pro != null)
            location = locationManager.getLastKnownLocation(pro);

        return location;
    }

    public Location getGpsLocation() {
        Location location = null;
        if (mProviders.contains(LocationManager.GPS_PROVIDER))
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return location;
    }

    public Location getNetLocation() {
        Location location = null;
        if (mProviders.contains(LocationManager.NETWORK_PROVIDER))
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        return location;
    }

    public interface LocationCallBack {
        /**
         * 当前位置
         * 
         * @param location
         */
        void onCurrentLocation(Location location,
                NetLocation netLocation);
    }

    public void destoryLocationManager() {
        SLog.d(TAG, "destoryLocationManager");
        locationManager.removeUpdates(locationListener);
    }

    public NetLocation getLocationByCellWifi(ArrayList<WifiInfo> wifi,
            ArrayList<CellIDInfo> cellID) {

        NetLocation netLocation = null;

        if (cellID != null && cellID.size() > 0) {
            netLocation = MainApp.i()
                    .getLocationBuf(
                            cellID.get(0).getCellId());
            SLog.i(
                    TAG,
                    "getLocationByCellWifi cellid=" + cellID.get(0).getCellId()
                            + "\r\n location buf="
                            + netLocation);
        }

        if (netLocation == null) {
            netLocation = new NetLocation();
            netLocation.setCellIds(cellID);
            netLocation.setWifis(wifi);
            netLocation.setLocation(callGear(wifi,
                    cellID));
            if (netLocation.getLocation() != null && cellID != null && cellID.size() > 0) {
                MainApp.i().putLoctionBuf(cellID.get(0).getCellId(), netLocation);
            }
        }
        return netLocation;
    }

    // 调用google gears的方法，该方法调用gears来获取经纬度
    private com.skymobi.android.sx.codec.beans.common.Location callGear(ArrayList<WifiInfo> wifi,
            ArrayList<CellIDInfo> cellID) {
        SLog.i(TAG, "callGear");
        if (cellID == null || cellID.size() == 0) {// 只要获取不到CellId就不请求，避免wifi信息中mac地址为null，Google会根据IP定位
            SLog.i(
                    TAG,
                    "callGear return");
            return null;
        }
        DefaultHttpClient client = new DefaultHttpClient();
        // 设置超时
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        HttpPost post = new HttpPost("http://www.google.com/loc/json");
        JSONObject holder = new JSONObject();
        JSONObject data, current_data;
        JSONArray array = new JSONArray();
        try {
            holder.put("version", "1.1.0");
            holder.put("host", "maps.google.com");
            if (cellID != null && cellID.size() > 0) {
                holder.put("home_mobile_country_code", cellID.get(0).mobileCountryCode);
                holder.put("home_mobile_network_code", cellID.get(0).mobileNetworkCode);
                holder.put("radio_type", cellID.get(0).radioType);
                holder.put("request_address", true);
                if ("460".equals(String.valueOf(cellID.get(0).mobileCountryCode)))
                    holder.put("address_language", "zh_CN");
                else
                    holder.put("address_language", "en_US");

                current_data = new JSONObject();
                current_data.put("cell_id", cellID.get(0).cellId);
                current_data.put("mobile_country_code", cellID.get(0).mobileCountryCode);
                current_data.put("mobile_network_code", cellID.get(0).mobileNetworkCode);
                current_data.put("location_area_code", cellID.get(0).locationAreaCode);
                current_data.put("age", 0);
                array.put(current_data);

                if (cellID.size() > 2) {
                    for (int i = 1; i < cellID.size(); i++) {
                        data = new JSONObject();
                        data.put("cell_id",
                                cellID.get(i).cellId);
                        data.put("location_area_code",
                                cellID.get(0).locationAreaCode);
                        data.put("mobile_country_code",
                                cellID.get(0).mobileCountryCode);
                        data.put("mobile_network_code",
                                cellID.get(0).mobileNetworkCode);
                        data.put("location_area_code", cellID.get(0).locationAreaCode);
                        data.put("age", 0);
                        array.put(data);
                    }
                }
                holder.put("cell_towers", array);
            } else {
                holder.put("request_address", true);
                holder.put("address_language", "zh_CN");
            }

            array = new
                    JSONArray();
            for (int i = 0; i < wifi.size(); i++) {
                data = new JSONObject();
                data.put("mac_address", wifi.get(i).mac);
                data.put("signal_strength", wifi.get(i).getSignalStrength());
                data.put("age", 0);
                array.put(data);
            }
            if (wifi.size() > 0 && wifi.get(0).mac != null) {
                holder.put("wifi_towers", array);
            }

            // String LBS =
            // "{\"address_language\":\"zh_CN\",\"host\":\"maps.google.com\",\"radio_type\":\"gsm\",\"home_mobile_country_code\":460,\"home_mobile_network_code\":1,\"request_address\":true,\"version\":\"1.1.0\"}";
            StringEntity se = new StringEntity(holder.toString());
            SLog.i(TAG, "send>>>>" + holder.toString());
            post.setEntity(se);
            HttpResponse resp = client.execute(post);
            HttpEntity entity = resp.getEntity();

            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));

            StringBuffer sb = new StringBuffer();
            String result = br.readLine();
            while (result != null) {
                SLog.i(TAG, "receive>>>>" + result);
                sb.append(result);
                result = br.readLine();
            }
            br.close();
            data = new JSONObject(sb.toString());

            data = (JSONObject) data.get("location");

            com.skymobi.android.sx.codec.beans.common.Location loc = new com.skymobi.android.sx.codec.beans.common.Location();

            loc.setLatitude(String.valueOf(data.get("latitude")));
            loc.setLongitude(String.valueOf(data.get("longitude")));

            if (data.has("address"))
                data = (JSONObject) data.get("address");

            if (data.has("city"))
                loc.setCity(String.valueOf(data.get("city")));

            if (data.has("country"))
                loc.setCounty(String.valueOf(data.get("country")));

            if (data.has("region"))
                loc.setProvince(String.valueOf(data.get("region")));

            if (data.has("street"))
                loc.setStreet(String.valueOf(data.get("street")));
            return loc;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLocationCallback(LocationCallBack cb) {
        mCallback = cb;
    }
}
