
package android.skymobi.messenger.service.module;

import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.NearUserInfo;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ResultCode;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetNearByFriendResponse;
import com.skymobi.android.sx.codec.beans.common.NearUser;

import java.util.ArrayList;

/**
 * @ClassName: NearUserModule
 * @Description: get nearuser from server by location
 * @author Sivan.LV
 * @date 2012-6-28 上午9:43:46
 */
public class NearUserModule extends BaseModule {
    private final static String TAG = NearUserModule.class.getSimpleName();
    public final static int STATE_INTI = 0;
    public final static int STATE_GET_LOCATION = 1;
    public final static int STATE_GET_USERS = 2;
    public final static int STATE_DONE = 3;
    private int querySex = MainApp.i().getNearFilter();
    private int state = -1;
    private LocationClient locClient;

    public NearUserModule(CoreService service) {
        super(service);
    }

    public void initLocation(LocationClient locClient) {
        state = STATE_INTI;
        this.locClient = locClient;
    }

    public void getLocation() {
        state = STATE_GET_LOCATION;
        if (!service.isNetConnected()) {
            ResultCode.setCode(Constants.NET_ERROR);
            service.notifyObservers(CoreServiceMSG.MSG_NET_ERROR,
                    null);
            return;
        }
        if (locClient != null) {
            if (locClient.isStarted())
                locClient.requestLocation();
            else {
                locClient.start();
            }
        }
        else {
            Log.i(TAG, "locClient is null or not started");
        }

    }

    public void getNearUsersBaibu(final int start, final int pageSize,
            final com.skymobi.android.sx.codec.beans.common.Location location) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (state == STATE_GET_LOCATION) {
                    if (!MainApp.isLoggedIn()) {
                        ResultCode.setCode(Constants.NET_ERROR);
                        service.notifyObservers(CoreServiceMSG.MSG_NET_ERROR,
                                null);
                        return;
                    }

                    state = STATE_GET_USERS;
                    ArrayList<NearUserInfo> nearUsers = null;
                    NetGetNearByFriendResponse resp = netWorkMgr.getNearUserNetModule()
                            .getNearUsers(1, 120, querySex, location);
                    if (resp.isSuccess()) {
                        nearUsers = new ArrayList<NearUserInfo>();
                        ArrayList<NearUser> users = resp.getUsers();
                        if (users != null) {
                            for (NearUser user : users) {
                                NearUserInfo userInfo = new NearUserInfo();
                                userInfo.setDistance(user.getDistance());
                                userInfo.setImageHead(user.getImageHead());
                                userInfo.setNearbyUserType(user.getNearbyUserType());
                                userInfo.setNickname(user.getNickname());
                                userInfo.setRecommendReason(user.getRecommendReason());
                                userInfo.setSkyId(user.getSkyId());
                                userInfo.setUsex(user.getUsex());
                                userInfo.setUsignature(user.getUsignature());
                                nearUsers.add(userInfo);
                            }
                        }
                        service.notifyObservers(CoreServiceMSG.MSG_NEARUSER_GET,
                                nearUsers);
                    } else {
                        if (resp.getResultCode() == -1) {
                            resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                        }
                        ResultCode.setCode(resp.getResultCode());
                        service.notifyObservers(CoreServiceMSG.MSG_NET_ERROR,
                                null);
                    }
                }
            }
        });

    }

    public void reset() {
        state = STATE_INTI;
    }

    public void finish() {
        state = -1;
        locClient.stop();
    }

    /**
     * @return the querySex
     */
    public int getQuerySex() {
        return querySex;
    }

    /**
     * @param querySex the querySex to set
     */
    public void setQuerySex(int querySex) {
        this.querySex = querySex;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state) {
        this.state = state;
    }
}
