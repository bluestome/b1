
package android.skymobi.messenger.location;

/*CellIDInfoManager.java 可获取所有的CellIDInfo */
import android.content.Context;
import android.skymobi.common.log.SLog;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CellIDInfoManager
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-21 上午10:20:14
 */
public class CellIDInfoManager {
    private static CellIDInfoManager instance = null;
    private TelephonyManager manager;
    private PhoneStateListener listener;
    private GsmCellLocation gsm;
    private CdmaCellLocation cdma;
    int lac;
    String current_ci, mcc, mnc;

    public CellIDInfoManager() {
    }

    public static CellIDInfoManager getInstance() {
        if (instance == null)
            instance = new CellIDInfoManager();

        return instance;
    }

    public ArrayList<CellIDInfo> getCellIDInfo(Context context) {
        manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        listener = new PhoneStateListener();
        manager.listen(listener, 0);
        ArrayList<CellIDInfo> CellID = new ArrayList<CellIDInfo>();
        CellIDInfo currentCell = new CellIDInfo();
        int type = manager.getNetworkType();
        if (type == TelephonyManager.NETWORK_TYPE_GPRS
                || type == TelephonyManager.NETWORK_TYPE_EDGE
                || type == TelephonyManager.NETWORK_TYPE_UMTS
                || type == TelephonyManager.NETWORK_TYPE_HSUPA
                || type == TelephonyManager.NETWORK_TYPE_HSPA
                || type == TelephonyManager.NETWORK_TYPE_HSDPA) {

            gsm = ((GsmCellLocation) manager.getCellLocation());
            if (gsm == null)
                return null;
            lac = gsm.getLac();
            String netWorkOperator = manager.getNetworkOperator();
            SLog.d("CellManager",
                    "netWorkOperator = " + netWorkOperator);
            try {
                mcc = netWorkOperator.substring(0, 3);
                mnc = netWorkOperator.substring(3, 5);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return null;
            }
            currentCell.cellId = String.valueOf(gsm.getCid());
            currentCell.mobileCountryCode = Short.valueOf(mcc);
            currentCell.mobileNetworkCode = Integer.valueOf(mnc);

            currentCell.locationAreaCode = lac;
            currentCell.radioType = "gsm";
            CellID.add(currentCell);

            List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();

            if (list != null) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    CellIDInfo info = new CellIDInfo();
                    info.cellId = String.valueOf(list.get(i).getCid());
                    info.mobileCountryCode = Short.valueOf(mcc);
                    info.mobileNetworkCode = Integer.valueOf(mnc);
                    info.locationAreaCode = lac;
                    CellID.add(info);
                }
            }
        } else if (type == TelephonyManager.NETWORK_TYPE_CDMA
                || type == TelephonyManager.NETWORK_TYPE_1xRTT
                || type == TelephonyManager.NETWORK_TYPE_EVDO_0
                || type == TelephonyManager.NETWORK_TYPE_EVDO_A) {

            cdma = ((CdmaCellLocation) manager.getCellLocation());
            if (cdma == null)
                return null;
            int lac = cdma.getNetworkId();
            try {
                mcc = manager.getNetworkOperator().substring(0, 3);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return null;
            }
            mnc = String.valueOf(cdma.getSystemId());
            int cid = cdma.getBaseStationId();

            currentCell.cellId = String.valueOf(cid);
            currentCell.mobileCountryCode = Short.valueOf(mcc);
            currentCell.mobileNetworkCode = Integer.valueOf(mnc);
            currentCell.locationAreaCode = lac;

            currentCell.radioType = "cdma";

            CellID.add(currentCell);

            // 获得邻近基站信息
            List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();
            int size = list.size();
            for (int i = 0; i < size; i++) {

                CellIDInfo info = new CellIDInfo();
                info.cellId = String.valueOf(list.get(i).getCid());
                info.mobileCountryCode = Short.valueOf(mcc);
                info.mobileNetworkCode = Integer.valueOf(mnc);
                info.locationAreaCode = lac;
                CellID.add(info);
            }
        }
        return CellID;
    }
}
