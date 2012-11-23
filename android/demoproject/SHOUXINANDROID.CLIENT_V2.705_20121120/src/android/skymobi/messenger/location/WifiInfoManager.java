
package android.skymobi.messenger.location;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.util.ArrayList;

/**
 * @ClassName: WifiInfoManager
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-21 上午10:02:23
 */
public class WifiInfoManager {
    WifiManager wm;
    private static WifiInfoManager instance = null;

    public WifiInfoManager() {
    }

    public static WifiInfoManager getInstance() {
        if (instance == null)
            instance = new WifiInfoManager();
        return instance;
    }

    public ArrayList<WifiInfo> getWifiInfo(Context context) {
        ArrayList<WifiInfo> wifi = new ArrayList<WifiInfo>();
        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        android.net.wifi.WifiInfo wi = wm.getConnectionInfo();
        if (wi != null) {
            WifiInfo info = new WifiInfo();
            info.mac = wi.getBSSID();
            info.signalStrength = (short) WifiManager.calculateSignalLevel(wi.getRssi(), 8);
            wifi.add(info);
        }
        // List<ScanResult> scanResults = wm.getScanResults();
        // if (scanResults != null && scanResults.size() > 0) {
        // for (ScanResult s : scanResults) {
        // WifiInfo info = new WifiInfo();
        // info.mac = s.BSSID;
        // info.signalStrength = (short)
        // WifiManager.calculateSignalLevel(s.level, 8);
        //
        // wifi.add(info);
        // }
        // }

        return wifi;
    }
}
