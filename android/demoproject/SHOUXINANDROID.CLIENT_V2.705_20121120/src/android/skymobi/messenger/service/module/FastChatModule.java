
package android.skymobi.messenger.service.module;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.bean.ResFile;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.database.dao.ResFilesDAO;
import android.skymobi.messenger.network.FastChatListener;
import android.skymobi.messenger.network.module.MessageNetModule;
import android.skymobi.messenger.network.module.NotifyNetModule;
import android.skymobi.messenger.network.module.SettingsNetModule;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.FastChatActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.PropertiesUtils;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.SettingsPreferences;

import com.skymobi.android.sx.codec.beans.clientbean.NetChatNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetChatResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetFsDownloadReq;
import com.skymobi.android.sx.codec.beans.clientbean.NetFsDownloadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetUploadResponse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @ClassName: FastChatModule
 * @Description: 快聊模块
 * @author Michael.Pan
 * @date 2012-10-18 下午02:42:31
 */
public class FastChatModule extends BaseModule implements FastChatListener {

    private final static String TAG = FastChatModule.class.getSimpleName();
    private final NotifyNetModule notifyNetModule;
    private final MessageNetModule messageNetModule;
    private final SettingsNetModule settingsNetModule;
    private ResFilesDAO mResFilesDAO = null;

    /**
     * @param service
     */
    public FastChatModule(CoreService service) {
        super(service);
        notifyNetModule = netWorkMgr.getNotifyNetModule();
        notifyNetModule.setFastChatListener(this);
        messageNetModule = netWorkMgr.getMessageNetModule();
        settingsNetModule = netWorkMgr.getSettingsNetModule();
        mResFilesDAO = daoFactory.getResfilesDAO();
    }

    @Override
    public void onNotify(int what, Object obj) {
        switch (what) {
            case CoreServiceMSG.MSG_FASTCHAT_APPLY_FAIL: {
                SLog.d("fastchat", "匹配失败");
                service.notifyObservers(what, obj);
            }
                break;
            case CoreServiceMSG.MSG_FASTCHAT_APPLY_SUCCESS: {
                int desSkyid = (Integer) obj;
                SLog.d("fastchat", "匹配成功   desskyid = " + desSkyid);
                // 设置连接成功后的对方的skyid, 然后将之前的聊天记录删除掉
                MainApp.getFastChatCache().clearAll();
                MainApp.getFastChatCache().setMatchedSkyid(desSkyid);
                service.notifyObservers(what, obj);
            }
                break;
            case CoreServiceMSG.MSG_FASTCHAT_ALREADY_LEAVE: {
                int desSkyid = (Integer) obj;
                SLog.d("fastchat", "对方已经离开 deSkyid = " + desSkyid);
                // 只有和连接的用户相同时 才处理对方离开的通知，否则不做处理
                if (MainApp.getFastChatCache().getMatchedSkyid() == desSkyid) {
                    MainApp.getFastChatCache().setMatchedSkyid(-1);
                    service.notifyObservers(what, obj);
                }
            }
                break;
            case CoreServiceMSG.MSG_FASTCHAT_RECEIVEVOICE: {
                SLog.d("fastchat", "收到对方的快聊语音");
                // 尚未匹配上，快聊消息拒收
                if (MainApp.getFastChatCache().getMatchedSkyid() != -1)
                    onReceiveFastChatMsg((NetChatNotify) obj);
            }
            default:
                break;
        }

    }

    /**
     * 发送快聊语音
     * 
     * @msg 消息体 @path 语音路径 @isResend是否为重发
     */
    public void sendFastChatVoice(final Message msg, final ResFile file,
            final int destSkyid, final boolean isResend) {
        if (isResend) {
            service.notifyObservers(CoreServiceMSG.MSG_FASTCHAT_RESENDVOICE_BEGIN, null);
        } else {
            service.notifyObservers(CoreServiceMSG.MSG_FASTCHAT_SENDVOICE_BEGIN, null);
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                FileInputStream in = null;
                byte[] body = null;
                try {
                    in = new FileInputStream(file.getPath());
                    body = new byte[in.available()];
                    in.read(body);
                    in.close();
                } catch (FileNotFoundException e) {
                    SLog.e("fastchat", "sendFastChatVoice--->FileNotFoundException");
                    e.printStackTrace();
                } catch (IOException e) {
                    SLog.e("fastchat", "sendFastChatVoice--->IOException");
                    e.printStackTrace();
                }

                String fileUrl = PropertiesUtils.getInstance().getFileURL();
                UserInfo info = CommonPreferences.getUserInfo();
                // 上传语音文件
                NetUploadResponse resp = settingsNetModule.uploadFs(fileUrl, info.skyid,
                        info.token, Constants.VOICE_EXT_NAME, body);
                SLog.i("fastchat",
                        "sendFastChatVoice uploadVoice-->resp.isSuccess() = " + resp.isSuccess()
                                + ",ResultCode = " + resp.getResultCode());
                // 发送语音文件
                NetChatResponse respSend = null;

                if (resp.isSuccess()) {
                    file.setUrl(resp.getMd5());
                    file.setSize(body.length);
                    respSend = messageNetModule.setFastChatVoiceMsg(
                            String.valueOf(destSkyid), file);
                    msg.setStatus(MessagesColumns.STATUS_SUCCESS);
                    SLog.i("fastchat", "respSend.isUserOnline() = " + respSend.isUserOnline());
                    SLog.i("fastchat", "respSend.isFastChatLeave() = " + respSend.isFastChatLeave());
                    // 发送语音消息时需要检查对方是否在线，或者是否已经将你抛弃，
                    // 解决你断网期间不能接收到对方抛弃你的系统通知
                    if (!respSend.isUserOnline()
                            || respSend.isFastChatLeave()) {
                        // 只有和连接的用户相同时 才处理对方离开的通知，否则不做处理
                        if (MainApp.getFastChatCache().getMatchedSkyid() == destSkyid) {
                            MainApp.getFastChatCache().setMatchedSkyid(-1);
                            service.notifyObservers(CoreServiceMSG.MSG_FASTCHAT_ALREADY_LEAVE,
                                    destSkyid);
                        }

                    }
                    SLog.i("fastchat", "respSend.getResultCode() = " + respSend.getResultCode());
                    SLog.i("fastchat", "md5 = " + resp.getMd5());
                    SLog.i("fastchat", "body.length = " + body.length);
                    SLog.i("fastchat",
                            "sendFastChatVoice-->respSend.isSuccess() =  " + respSend.isSuccess());
                    SLog.i("fastchat", "sendFastChatVoice-->resp = " + respSend.getResultHint());
                } else {
                    msg.setStatus(MessagesColumns.STATUS_FAILED);
                    if (resp.getResultCode() == -1) {
                        resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                    }
                    ResultCode.setCode(resp.getResultCode());
                }
                if (isResend) {
                    service.notifyObservers(CoreServiceMSG.MSG_FASTCHAT_RESENDVOICE_END, null);
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_FASTCHAT_SENDVOICE_END, null);
                }
            }
        });
    }

    private void onReceiveFastChatMsg(final NetChatNotify nfy) {
        SLog.i(TAG, "receive fast chat voice msg.....");
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String nickname = nfy.getNickname();
                int skyID = nfy.getSkyid();
                long receiveTime = DateUtil.getLongTimeByStamp(nfy.getTimestamp());
                String md5 = nfy.getAudio().getMd5();
                UserInfo info = CommonPreferences.getUserInfo();
                ArrayList<NetFsDownloadReq> downloadList = new ArrayList<NetFsDownloadReq>();
                downloadList.add(new NetFsDownloadReq(md5, 0));
                String fileUrl = PropertiesUtils.getInstance().getFileURL();
                String path = MainApp.i().createNewSoundFile(md5);
                Message message = new Message();
                NetFsDownloadResponse resp = null;

                // 下载语音
                ResFile file = new ResFile();
                file.setPath(path);
                resp = settingsNetModule.downloadFs(fileUrl,
                        info.skyid,
                        info.token, downloadList);
                // 语音下载失败，则尝试重新下载一次，避免语音漏收的情况
                if (resp != null && resp.isFailed()) {
                    resp = settingsNetModule.downloadFs(fileUrl,
                            info.skyid,
                            info.token, downloadList);
                }
                // 两次都下载失败 ，太悲剧了
                if (resp == null || resp.isFailed() || path == null) {
                    SLog.e("fastchat", "两次下载快聊语音失败");
                    return;
                }

                byte[] data = resp.getFileList().get(0).getFileData();
                try {
                    FileOutputStream out = new FileOutputStream(path);
                    out.write(data);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                message.setStatus(MessagesColumns.STATUS_SUCCESS);
                file.setSize(resp.getFileList().get(0).getFileSize());
                file.setLength(nfy.getAudio().getAudioLen());

                // 保存文件数据
                file.setVersion(ResFile.VERSION);
                file.setFormat(Constants.VOICE_EXT_NAME);
                file.setUrl(md5);
                long fileId = mResFilesDAO.addFile(file);

                // 添加消息数据
                message.setContent(String.valueOf(fileId));
                message.setDate(receiveTime);
                message.setType(MessagesColumns.TYPE_VOICE);
                message.setTalkReason(nfy.getTalkReason());
                message.setOpt(MessagesColumns.OPT_FROM);
                message.setRead(MessagesColumns.READ_NO);
                message.setResFile(file);
                MainApp.getFastChatCache().addChatMsg(message);
                if (MainApp.getFastChatCache().isLeave()) {
                    showFastChatNotification();
                }
                service.notifyObservers(CoreServiceMSG.MSG_FASTCHAT_RECEIVEVOICE, message);
            }
        });
    }

    // 显示收到快聊信息的条数，点击后显示快聊界面
    private void showFastChatNotification() {

        NotificationManager nManager = service.getNotificationManager();
        String title = MainApp.i().getResources().getString(R.string.app_name);
        String notifiTail = MainApp.i().getResources().getString(R.string.notifi_content_fastchat);
        String notifiContent = null;
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        int unreadMessageCnt = MainApp.getFastChatCache().getUnreadVoiceCount();

        // 没有未读消息或者没有未读会话
        if (unreadMessageCnt <= 0)
            return;
        intent.setClass(MainApp.i().getApplicationContext(), FastChatActivity.class);
        notifiContent = String.valueOf(unreadMessageCnt) + notifiTail;

        String tickerText = title + ":" + notifiContent;
        Notification notification = new Notification(
                R.drawable.new_message_notification, tickerText,
                System.currentTimeMillis());

        notification.defaults = Notification.DEFAULT_LIGHTS;

        if (SettingsPreferences.getSoundStatus()) {
            notification.defaults = notification.defaults
                    | Notification.DEFAULT_SOUND;
        }

        if (SettingsPreferences.getVibrateStatus()) {
            long[] vibrate = {
                    0, 200, 100, 200, 100, 200
            };
            notification.vibrate = vibrate;
        }
        notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;

        PendingIntent pt = PendingIntent.getActivity(MainApp.i(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(MainApp.i(), title, notifiContent, pt);
        nManager.notify(CoreService.NEW_FASTCHAT_NOTIFICATION_ID, notification);

    }

    // 取消快聊的通知显示
    public void cancelFastChatNotification() {
        NotificationManager nManager = service.getNotificationManager();
        if (nManager != null) {
            nManager.cancel(CoreService.NEW_FASTCHAT_NOTIFICATION_ID);
        }
    }

}
