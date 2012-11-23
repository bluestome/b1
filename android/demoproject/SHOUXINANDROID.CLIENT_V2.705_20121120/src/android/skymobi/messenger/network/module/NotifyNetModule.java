
package android.skymobi.messenger.network.module;

import android.app.Activity;
import android.content.Intent;
import android.skymobi.app.notify.INetListener;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.cache.APPCache;
import android.skymobi.messenger.network.BindChangeListener;
import android.skymobi.messenger.network.ChatMsgListener;
import android.skymobi.messenger.network.FastChatListener;
import android.skymobi.messenger.network.NetWorkListener;
import android.skymobi.messenger.network.UpdateListener;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.LoginActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.util.Log;
import android.widget.Toast;

import com.skymobi.android.sx.codec.beans.clientbean.NetBindChangeNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetChangeStatNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetChatNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetFastTalkNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetFriendsMsgNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetMarketingMessageNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetOnlineStateChangeNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetStateNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetStateNotify.ConnectStatus;
import com.skymobi.android.sx.codec.beans.clientbean.NetSysMsgNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetVCardNotify;

import java.util.ArrayList;

/**
 * @ClassName: MessageNetModule
 * @author Sean.Xie
 * @date 2012-3-2 上午11:17:34
 */
public class NotifyNetModule implements INetListener {

    private static final String TAG = NotifyNetModule.class.getSimpleName();
    // 监听网络状态
    private NetWorkListener mListener = null;

    // 监听接收到的消息
    private ChatMsgListener mChatMsgListener = null;

    // 在线状态
    private NetWorkListener statusListener = null;

    private UpdateListener updateListener = null;

    // 监听快聊的通知
    private FastChatListener fastChatListener = null;

    // 绑定变更通知监听器
    private BindChangeListener bindChangeListener = null;

    public NotifyNetModule() {
    }

    /**
     * 注册网络监听
     * 
     * @param listener
     */
    public void setNetWorkListener(NetWorkListener listener) {
        mListener = listener;
    }

    /**
     * 注册网络消息监听
     * 
     * @param listener
     */
    public void setChatMsgListener(ChatMsgListener listener) {
        mChatMsgListener = listener;
    }

    /**
     * @param updateListener the updateListener to set
     */
    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public void setContactOnlineStatusListener(NetWorkListener listener) {
        this.statusListener = listener;
    }

    // 接收到聊天消息
    @Override
    public void onChatNotify(NetChatNotify nfy) {
        if (null != mChatMsgListener) {
            if (nfy.getAudio() == null) {
                mChatMsgListener.onNotify(CoreServiceMSG.MSG_CHATMSG_TEXTMSG_RECEIVE, nfy);
            } else {
                if (nfy.getChatMsgType() == ChatMsgListener.CHAT_MSG_FASTCHAT_VOICE) {
                    fastChatListener.onNotify(CoreServiceMSG.MSG_FASTCHAT_RECEIVEVOICE, nfy);
                } else {
                    mChatMsgListener.onNotify(CoreServiceMSG.MSG_CHATMSG_VOICEMSG_RECEIVE, nfy);
                }
            }
        }
    }

    // 推荐营销消息
    @Override
    public void onMarketingMsgNotify(NetMarketingMessageNotify arg0) {
        if (null != mChatMsgListener) {
            mChatMsgListener.onNotify(CoreServiceMSG.MSG_CHATMSG_MARKETMSG_RECEIVE, arg0);
        }
    }

    // 系统消息通知
    @Override
    public void onSysMsgNotify(NetSysMsgNotify arg0) {
        if (null != mChatMsgListener) {
            mChatMsgListener.onNotify(CoreServiceMSG.MSG_CHATMSG_SYSTEMMSG_RECEIVE, arg0);
        }
    }

    // 推荐好友通知
    @Override
    public void onFriendsMsgNotify(NetFriendsMsgNotify arg0) {
        if (null != mChatMsgListener) {
            mChatMsgListener.onNotify(CoreServiceMSG.MSG_CHATMSG_FRIENDSMSG_RECEIVE, arg0);
        }
    }

    // 状态通知响应
    @Override
    public void onChangeStatNotifyResp(NetChangeStatNotify status) {
    }

    // 在线状态通知
    @Override
    public void onOnlineStateChange(NetOnlineStateChangeNotify status) {
        if (statusListener != null) {
            statusListener.onNotify(CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS, status);
        }
    }

    // 接收名片
    @Override
    public void onVCardNotify(NetVCardNotify arg0) {
        if (null != mChatMsgListener) {
            mChatMsgListener.onNotify(CoreServiceMSG.MSG_CHATMSG_CARDMSG_RECEIVE, arg0);
        }
    }

    @Override
    public void onNetStateNotify(final NetStateNotify arg0) {
        if (mListener == null) {
            Log.e(TAG, "onNetStateNotify mListener = " + mListener);
            return;
        }
        SLog.d(TAG, "onNetStateNotify = " + arg0.getState());
        if (ConnectStatus.CONNECTED == arg0.getState()) {
            mListener.onNotify(NetWorkListener.ON_LINE, null);
        } else if (ConnectStatus.RECONNECTED == arg0.getState()) {
            mListener.onNotify(NetWorkListener.RE_ON_LINE, null);
        } else {
            mListener.onNotify(NetWorkListener.OFF_LINE, null);
        }
    }

    @Override
    public void onNewAccessConfigNotify(String arg0, int arg1) {
    }

    @Override
    public void ticketOutNotify() {
        SettingsPreferences.clear();
        CommonPreferences.setLogoutedStatus(true);
        APPCache.getInstance().setManualLogined(false);
        MainApp.i().setUserInfo(null);
        MainApp.setLoggedIn(false);
        try {
            final ArrayList<Activity> activitys = MainApp.i().getAllActiveActivity();
            final Activity activity = MainApp.i().getCurrentActivity();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, R.string.ticketout, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                    for (Activity act : activitys) {
                        if (activity != null && !activity.isFinishing())
                            act.finish();
                    }
                    activitys.clear();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 流量通知接口
     */
    @Override
    public void trafficNotify(long pack) {
        if (null != mChatMsgListener) {
            mChatMsgListener.onNotify(CoreServiceMSG.MSG_TRAFFIC_NOTIFY_MSG, Long.valueOf(pack));
        }
    }

    @Override
    public void onUpdateError() {
        Log.i(TAG, " 更新失败通知!");
        updateListener.onNotify();
    }

    @Override
    public void createFastChatNotify(NetFastTalkNotify arg0) {
        if (fastChatListener != null) {
            if (arg0.isSuccess()) {
                // 匹配成功，反馈匹配后对方的skyid
                fastChatListener.onNotify(CoreServiceMSG.MSG_FASTCHAT_APPLY_SUCCESS,
                        arg0.getDestSkyid());
            } else {
                // 悲剧了,匹配失败
                fastChatListener.onNotify(CoreServiceMSG.MSG_FASTCHAT_APPLY_FAIL, null);
            }
        }
    }

    @Override
    public void leaveFastChatNotify(NetFastTalkNotify arg0) {
        if (fastChatListener != null) {
            fastChatListener.onNotify(CoreServiceMSG.MSG_FASTCHAT_ALREADY_LEAVE,
                    arg0.getDestSkyid());
        }
    }

    /**
     * 设置快聊的监听接口
     */
    public void setFastChatListener(FastChatListener fastChatListener) {
        this.fastChatListener = fastChatListener;
    }

    /**
     * @return the bindChangeListener
     */
    public BindChangeListener getBindChangeListener() {
        return bindChangeListener;
    }

    /**
     * @param bindChangeListener the bindChangeListener to set
     */
    public void setBindChangeListener(BindChangeListener bindChangeListener) {
        this.bindChangeListener = bindChangeListener;
    }

    @Override
    public void bindChangeNotify(NetBindChangeNotify notify) {
        if (null != bindChangeListener) {
            bindChangeListener.onNotify(notify);
        }
    }
}
