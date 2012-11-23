
package android.skymobi.messenger.service.module;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.DownloadInfo;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.cache.APPCache;
import android.skymobi.messenger.database.dao.ContactsDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.network.UpdateListener;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.network.module.NotifyNetModule;
import android.skymobi.messenger.network.module.SettingsNetModule;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.FileUtils;
import android.skymobi.messenger.utils.ImageUtils;
import android.skymobi.messenger.utils.PropertiesUtils;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.skymobi.android.sx.codec.TerminalInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetDownloadImageRespInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetSetRecommendResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetImageDownloadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetRestorableContactsResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetRestoreContactsResp;
import com.skymobi.android.sx.codec.beans.clientbean.NetSetUserInfoResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetSupResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetUploadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfoResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetmodifyPwdResponse;
import com.skymobi.android.sx.codec.beans.common.RestorableContacts;
import com.skymobi.android.sx.codec.util.MD5Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @ClassName: SettingsModule
 * @Description:
 * @author Michael.Pan
 * @date 2012-3-8 下午02:26:07
 */
public class SettingsModule extends BaseModule {
    private final String TAG = SettingsModule.class.getSimpleName();
    private final SettingsNetModule mSettingsNetModule;
    private final NotifyNetModule notifyNetModule;
    private final ContactsNetModule mContactsNetModule;
    private final TerminalInfo tInfo;
    private NotificationManager mNotifManager;
    private Notification mDownNotification;
    private RemoteViews mContentView; // 下载进度View
    private PendingIntent mDownPendingIntent;

    private static final int CUSTOM_NOTIFIATION_ID = 0x1000;
    private static final int MSGCODE_NOUPDATE = 404;

    /**
     * @param service
     */
    public SettingsModule(CoreService service) {
        super(service);
        mSettingsNetModule = netWorkMgr.getSettingsNetModule();
        notifyNetModule = netWorkMgr.getNotifyNetModule();
        mContactsNetModule = netWorkMgr.getContactsNetModule();
        tInfo = netWorkMgr.getTerminalInfo();

    }

    // 注销
    public void logout() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                CommonPreferences.setLogoutedStatus(true);
                APPCache.getInstance().setManualLogined(false);
                service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_LOGOUT_SUCCESS, null);
                // 注销设置同步时间,下次登录执行同步
                MainApp.i().setLastSyncTime(0);
                MainApp.i().setLastSyncThreadsTime(0);
                UserInfo userInfo = CommonPreferences.getUserInfo();
                if (userInfo != null) {
                    final String token = userInfo.token;
                    if (null != token) {
                        mSettingsNetModule.logout(token);
                    }
                }
            }

        });
    }

    // 设置昵称
    public void setNickname(final String nickName) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String token = "";
                UserInfo userInfo = CommonPreferences.getUserInfo();
                if (userInfo != null) {
                    token = userInfo.token;
                }
                int skyid = CommonPreferences.getUserInfo().skyid;
                NetResponse response = netWorkMgr.getCommonModule().setNickname(skyid, token,
                        nickName);
                if (response.isNetError() || response.isFailed()) {
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_NICKNAME_FAIL,
                            response.getResultCode());
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_NICKNAME_SUCCESS, null);
                }
            }
        });
    }

    // 定义设置的用户信息的类别
    public static final int TYPE_HEADEPHOT = 1; // 头像
    public static final int TYPE_SEX = 2; // 性别
    public static final int TYPE_PERSONNICKNAME = 3; // 个性昵称
    public static final int TYPE_SCHOOL = 4; // 学校
    public static final int TYPE_CORPORATION = 5; // 公司
    public static final int TYPE_SIGNATURE = 6; // 签名
    public static final int TYPE_BIRTHDAY = 7; // 生日
    public static final int TYPE_HOMETOWN = 8; // 地区

    // 设置用户自定义信息
    public void setNetUserInfo(final NetUserInfo netuserinfo, final int type) {
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                String token = "";
                UserInfo userInfo = CommonPreferences.getUserInfo();
                if (userInfo != null) {
                    token = userInfo.token;
                }
                NetUserInfo updateInfo = new NetUserInfo();
                switch (type) {
                    case TYPE_HEADEPHOT:
                        updateInfo.setUuidPortrait(netuserinfo.getUuidPortrait());
                        break;
                    case TYPE_SEX:
                        updateInfo.setUsex(netuserinfo.getUsex());
                        break;
                    case TYPE_PERSONNICKNAME:
                        updateInfo.setPersonnickname(netuserinfo.getPersonnickname());
                        break;
                    case TYPE_SCHOOL:
                        updateInfo.setUschoolgraduated(netuserinfo.getUschoolgraduated());
                        break;
                    case TYPE_CORPORATION:
                        updateInfo.setUcorporation(netuserinfo.getUcorporation());
                        break;
                    case TYPE_SIGNATURE:
                        updateInfo.setUsignature(netuserinfo.getUsignature());
                        break;
                    case TYPE_BIRTHDAY:
                        updateInfo.setUbirthday(netuserinfo.getUbirthday());
                        break;
                    case TYPE_HOMETOWN:
                        updateInfo.setUprovince(netuserinfo.getUprovince());
                        updateInfo.setUcity(netuserinfo.getUcity());
                        break;
                    default:
                        SLog.e(TAG, "setNetUserInfo 接口使用错误");
                        break;
                }
                NetSetUserInfoResponse response = mSettingsNetModule.setNetUserInfo(token,
                        updateInfo);
                SLog.i(TAG, "setNetUserInfo response.isNetError() = " + response.isNetError());
                SLog.i(TAG, "setNetUserInfo response.isFailed() = " + response.isFailed());
                if (response.isNetError() || response.isFailed()) {
                    // service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_FAIL,
                    // ""
                    // +
                    // response.getResultHint()+"\r\n[错误码:0x"+StringUtil.autoFixZero(response.getResultCode())+"]");
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_FAIL,
                            response.getResultCode());
                } else {
                    if (!response.isNicknameLegal()) {
                        // 昵称不合法
                        service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_ILLEGAL,
                                null);
                        return;
                    }
                    MainApp.i().setUserInfo(netuserinfo);
                    switch (type) {
                        case TYPE_HEADEPHOT:
                            SettingsPreferences.saveHeadPhoto(netuserinfo.getUuidPortrait());
                            break;
                        case TYPE_SEX:
                            SettingsPreferences.saveSex(netuserinfo.getUsex());
                            break;
                        case TYPE_PERSONNICKNAME:
                            SettingsPreferences.saveNickname(netuserinfo.getPersonnickname());
                            break;
                        case TYPE_SCHOOL:
                            SettingsPreferences.saveSchool(netuserinfo.getUschoolgraduated());
                            break;
                        case TYPE_CORPORATION:
                            SettingsPreferences.saveCorporation(netuserinfo.getUcorporation());
                            break;
                        case TYPE_SIGNATURE:
                            SettingsPreferences.saveSignature(netuserinfo.getUsignature());
                            break;
                        case TYPE_BIRTHDAY:
                            SettingsPreferences.saveBirthday(netuserinfo.getUbirthday());
                            break;
                        case TYPE_HOMETOWN:
                            SettingsPreferences.savePlace(netuserinfo.getUprovince(),
                                    netuserinfo.getUcity());
                            break;
                        default:
                            break;
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_SUCCESS,
                            netuserinfo);
                }
            }
        });
    }

    // 修改密码
    public void modifyPwd(final String oldPwd, final String newPwd) {
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                String token = "";
                UserInfo userInfo = CommonPreferences.getUserInfo();
                if (userInfo != null) {
                    token = userInfo.token;
                }
                NetmodifyPwdResponse response = mSettingsNetModule.modifyPwd(token, oldPwd, newPwd);
                if (response.isNetError() || response.isFailed()) {
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    ResultCode.setCode(response.getResultCode());
                    boolean bPasswordError = response.isPasswordError();
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_MODIFYPWD_FAIL,
                            bPasswordError);
                } else {
                    byte[] encryptPasswd = response.getEncryptPasswd();
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_MODIFYPWD_SUCCESS,
                            encryptPasswd);
                }
            }
        });
    }

    // 用户反馈
    public void feedBack(final String nickName, final String content) {
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                NetResponse response = mSettingsNetModule.feedBack(nickName, content);
                if (response.isNetError() || response.isFailed()) {
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_FEEDBACK_FAIL,
                            response.getResultCode());
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_FEEDBACK_SUCCESS, null);
                }
            }
        });
    }

    // 是否推荐
    public void setRecommend(final boolean isRecommend, final boolean isShareLBS) {
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                NetResponse response = mSettingsNetModule.setRecommend(isRecommend, isShareLBS);
                if (response.isNetError() || response.isFailed()) {
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    ResultCode.setCode(response.getResultCode());
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_RECOMMEND_FAIL,
                            response.getResultCode());
                } else {
                    SettingsPreferences.saveRecommend(isRecommend);
                    SettingsPreferences.saveShareLBS(isShareLBS);
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_SET_RECOMMEND_SUCCESS,
                            isRecommend);
                }
            }
        });
    }

    // 获取是否推荐
    public void getRecommend() {
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                NetGetSetRecommendResponse response = mSettingsNetModule.getRecommend();
                if (response.isNetError() || response.isFailed()) {
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    ResultCode.setCode(response.getResultCode());
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_GET_RECOMMEND_FAIL, true);
                } else {
                    SettingsPreferences.saveRecommend(response.isRecommend());
                    SettingsPreferences.saveShareLBS(!response.isHideLBS());
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_GET_RECOMMEND_SUCCESS,
                            response.isRecommend());
                }
            }
        });
    }

    // 异步获取用户信息,只有用户在注册、绑定,换绑时，登录用户名显示为手机号码
    public void getUserInfo(final int... what) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String token = "";
                UserInfo userInfo = CommonPreferences.getUserInfo();
                if (userInfo != null) {
                    token = userInfo.token;
                }
                NetUserInfoResponse response = mSettingsNetModule.getUserInfo(token);
                if (response.isNetError() || response.isFailed()) {
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_FAIL,
                            response.getResultCode());
                } else {
                    NetUserInfo netuserInfo = response.getUserInfo();
                    if (netuserInfo != null) {
                        // 设置登录用户名
                        setLastLoginName(netuserInfo, what);
                        MainApp.i().setUserInfo(netuserInfo);
                        SettingsPreferences.updateUseInfo(netuserInfo);
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_SUCCESS,
                            response.getUserInfo());
                }
            }
        });
    }

    /**
     * 检查更新
     */
    public void checkUpdate(final boolean isCheck) {
        // isCheck 区分是否为检查更新的请求
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                NetSupResponse resp = null;
                SLog.d(TAG, "connect to sup server,time:" + System.currentTimeMillis());
                try {
                    String supUrl = PropertiesUtils.getInstance().getSupURL();
                    resp = mSettingsNetModule.checkSupUpdate(supUrl, tInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SLog.d(TAG, "resp from sup server,time:" + System.currentTimeMillis());

                DownloadInfo download = new DownloadInfo();
                if (null == resp || resp.isNetError()) {// 网络错误
                    if (resp.getResultCode() == -1) {
                        resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                    }
                    SLog.d(TAG, "check net error");
                    service.notifyObservers(CoreServiceMSG.MSG_LOGIN_CHECK_NET_ERROR,
                            resp.getResultCode());
                } else if (resp.isSuccess()) {
                    if (resp.isForce2Update() && isCheck) { // 强制
                        SLog.d(TAG, "force update");
                        // 初始化下载的信息
                        initDownloadInfo(download, resp);
                        service.notifyObservers(CoreServiceMSG.MSG_LOGIN_CHECK_FORCE_UPDATE,
                                download);
                    } else if (resp.isNeedUpdate() || (resp.isForce2Update() && !isCheck)) { // 可选
                        SLog.d(TAG, "need update");
                        initDownloadInfo(download, resp);
                        service.notifyObservers(CoreServiceMSG.MSG_LOGIN_CHECK_UPDATE,
                                download);
                    }
                } else if (resp.getResultCode() == MSGCODE_NOUPDATE) {
                    SLog.d(TAG, "no update");
                    service.notifyObservers(CoreServiceMSG.MSG_LOGIN_CHECK_NOUPDATE, download);
                } else {// 检查失败
                    if (resp.getResultCode() == -1) {
                        resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                    }
                    SLog.d(TAG, "checkUpdate error");
                    service.notifyObservers(CoreServiceMSG.MSG_LOGIN_CHECK_ERROR,
                            resp.getResultCode());
                }
            }
        });
    }

    /**
     * 下载更新 //TODO 需要改进:强制更新和可选更新分开,Notification放到updateHandler中去处理
     */
    public void download(final String md5, final int startPos, final int endPos,
            final boolean isForce,
            final BaseActivity activity, final String saveFileName) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 然后定时获取数据
                NetSupResponse resp = null;
                try {
                    String supUrl = PropertiesUtils.getInstance().getSupURL();
                    resp = mSettingsNetModule.nupdate(tInfo, supUrl,
                            saveFileName, md5,
                            startPos, endPos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final DownloadInfo download = new DownloadInfo();
                if (null == resp || resp.isNetError()) {
                    service.notifyObservers(CoreServiceMSG.MSG_LOGIN_DOWNLOAD_NET_ERROR, download);
                } else if (resp.isSuccess()) {
                    initDownloadInfo(download, resp);
                    CommonPreferences.saveFileMd5(resp.getMd5());

                    if (isForce) {
                        SLog.d(TAG, "force download process");
                        notifyNetModule.setUpdateListener(new UpdateListener() {// 下载中断监听
                                    @Override
                                    public void onNotify() {
                                        SLog.d(TAG, "force download fail");
                                        service.notifyObservers(
                                                CoreServiceMSG.MSG_LOGIN_DOWNLOAD_ERROR,
                                                null);
                                    }
                                });
                        int fileLength = CommonPreferences.getAppFilelen();
                        File file = null;
                        boolean downSuccess = false;
                        while (!downSuccess) {
                            SystemClock.sleep(900);
                            file = new File(saveFileName);
                            if (file.exists()) {
                                service.notifyObservers(CoreServiceMSG.MSG_LOGIN_FORCE_DOWNLOAD,
                                        (int) file.length());
                                if (file.length() >= fileLength) {// 已经下载完成
                                    downSuccess = true;
                                }
                            }
                        }

                        if (downSuccess) {
                            boolean isMd5 = getFileMD5(saveFileName);
                            if (isMd5) {
                                service.notifyObservers(CoreServiceMSG.MSG_LOGIN_DOWNLOAD,
                                        download);
                            } else {
                                service.notifyObservers(
                                        CoreServiceMSG.MSG_LOGIN_DOWNLOAD_RETRY,
                                        null);
                            }
                        }
                    }
                    else {
                        // TODO Notification放到Handler中处理
                        // 显示进度
                        File file = null;
                        boolean downSuccess = false;
                        int progress = 0;
                        int fileLength = CommonPreferences.getAppFilelen();
                        int tempProgress = 0;

                        initNotification(activity);

                        notifyNetModule.setUpdateListener(new UpdateListener() { // 下载失败通知
                                    @Override
                                    public void onNotify() {
                                        SLog.d(TAG, "update error");
                                        errorNotification(activity);
                                        service.notifyObservers(
                                                CoreServiceMSG.MSG_LOGIN_DOWNLOAD_ERROR,
                                                null);

                                        MainApp.i().setDownloading(false);
                                    }
                                });

                        while (!downSuccess && MainApp.i().isDownloading()) {
                            SystemClock.sleep(500);
                            file = new File(saveFileName);
                            if (file.exists()) {
                                progress = (int) (file.length() * 100.0 / fileLength);
                                if (file.length() >= fileLength) {
                                    downSuccess = true;
                                }

                                else if (progress != tempProgress) {
                                    // 下载进度发生改变，则发送Message
                                    SLog.d(TAG, "progress" + progress);
                                    progressNotification(progress);
                                    tempProgress = progress;
                                }
                            }
                        }
                        if (downSuccess) { // 下载完成
                            boolean isMd5 = getFileMD5(saveFileName);
                            // anson.yang
                            // @20121013,界面在后台时,没有接受到成功resp.暂时在module中设置
                            MainApp.i().setDownloading(false);
                            if (isMd5) {
                                successNotification(activity, saveFileName);
                                service.notifyObservers(CoreServiceMSG.MSG_LOGIN_DOWNLOAD,
                                        download);
                            }
                            // else {
                            // service.notifyObservers(
                            // CoreServiceMSG.MSG_LOGIN_DOWNLOAD_RETRY,
                            // null);
                            //
                            // MainApp.getInstance().setDownloading(false);
                            // }
                        } else {
                            errorNotification(activity);
                            service.notifyObservers(
                                    CoreServiceMSG.MSG_LOGIN_DOWNLOAD_ERROR,
                                    download);

                            MainApp.i().setDownloading(false);
                        }
                    }
                } else { // 下载失败
                    if (resp.getResultCode() == -1) {
                        resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                    }
                    ResultCode.setCode(resp.getResultCode());
                    service.notifyObservers(CoreServiceMSG.MSG_LOGIN_DOWNLOAD_ERROR, null);

                    MainApp.i().setDownloading(false);
                }
            }
        });
    }

    // 上传图片
    public void uploadImage(final byte[] body) {
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                int skyid = 0;
                String token = "";
                UserInfo userInfo = CommonPreferences.getUserInfo();
                if (userInfo != null) {
                    skyid = userInfo.skyid;
                    token = userInfo.token;
                }
                String fileUrl = PropertiesUtils.getInstance().getFileURL();
                NetUploadResponse response = mSettingsNetModule.uploadImage(fileUrl,
                        body, "jpg", skyid, token);
                if (response.isNetError() || response.isFailed()) {
                    Log.i(TAG, "" + response.getResultHint());
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_UPLOAD_HEADPHOTO_FAIL,
                            response.getResultCode());
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_UPLOAD_HEADPHOTO_SUCCESS,
                            response.getUuid());
                }
            }
        });
    }

    // 通过url下载对应的小图头像
    public Bitmap downloadHeader(String url) {
        if (url == null)
            return null;
        int skyid = 0;
        String token = "";
        UserInfo userInfo = CommonPreferences.getUserInfo();
        if (userInfo != null) {
            skyid = userInfo.skyid;
            token = userInfo.token;
        }
        Bitmap bm = null;
        String path = Constants.HEAD_PATH + url;
        File file = new File(path);
        // 如果文件存在，则直接解码并且返回
        if (file.exists()) {
            Bitmap headBitmap = ImageUtils.JpegToBitmap(path);
            return ImageUtils.getRoundedCornerBitmap(headBitmap);
        }

        ArrayList<NetDownloadImageRespInfo> rlist = new ArrayList<NetDownloadImageRespInfo>();
        NetDownloadImageRespInfo req = new NetDownloadImageRespInfo();
        req.setUuid(url);
        req.setStartPos(0);
        req.setWidth(Constants.SETTINGS_SMALL_HEAD_WIDTH);
        req.setFileExtName("jpg");
        rlist.add(req);
        String fileUrl = PropertiesUtils.getInstance().getFileURL();
        NetImageDownloadResponse response = netWorkMgr.getSettingsNetModule().downloadImage(
                fileUrl, skyid, token, rlist);
        if (null == response || response.isNetError()) {// 网络错误
            Log.w(TAG, " downloadHeader : 网络错误");
            bm = null;
            if (response.getResultCode() == -1) {
                response.setResult(Constants.NET_ERROR, response.getResultHint());
            }
            ResultCode.setCode(response.getResultCode());
        } else if (response.isSuccess() && response.getFileNum() > 0) {
            byte[] body = response.getFileList().get(0).getFileData();
            bm = FileUtils.Bytes2Bitmap(body);
            FileUtils.SaveBitmap2File(bm, path);
        }
        return ImageUtils.getRoundedCornerBitmap(bm);
    }

    // 异步下载图片
    public void downloadImage(final String fileName, final int width) {
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                int skyid = 0;
                String token = "";
                UserInfo userInfo = CommonPreferences.getUserInfo();
                if (userInfo != null) {
                    skyid = userInfo.skyid;
                    token = userInfo.token;
                }
                ArrayList<NetDownloadImageRespInfo> rlist = new ArrayList<NetDownloadImageRespInfo>();
                NetDownloadImageRespInfo req = new NetDownloadImageRespInfo();
                req.setUuid(fileName);
                req.setStartPos(0);
                req.setWidth(width);
                req.setFileExtName("jpg");
                rlist.add(req);
                String fileUrl = PropertiesUtils.getInstance().getFileURL();
                NetImageDownloadResponse response = mSettingsNetModule.downloadImage(
                        fileUrl,
                        skyid, token, rlist);
                if (null == response || response.isNetError()) {// 网络错误
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    ResultCode.setCode(response.getResultCode());
                    service.notifyObservers(CoreServiceMSG.MSG_NET_ERROR, null);
                } else if (response.isSuccess() && response.getFileNum() > 0) {
                    byte[] body = response.getFileList().get(0).getFileData();
                    Bitmap bmp = FileUtils.Bytes2Bitmap(body);
                    if (width == Constants.SETTINGS_LARGE_HEAD_WIDTH) {
                        FileUtils.SaveBitmap2File(bmp, Constants.LARGE_HEAD_PATH + fileName);
                    } else {
                        FileUtils.SaveBitmap2File(bmp, Constants.HEAD_PATH + fileName);
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_DOWNLOAD_HEADPHOTO_SUCCESS,
                            response.getFileNum());
                } else {
                    Log.i(TAG, "" + response.getResultHint());
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_DOWNLOAD_HEADPHOTO_FAIL,
                            response.getResultCode());
                }
            }
        });
    }

    private void initDownloadInfo(DownloadInfo download, NetSupResponse resp) {
        download.setCheckInterval(resp.getCheckInterval());
        download.setCheckAfterTimes(resp.getCheckAfterTimes());
        download.setFeature(resp.getFeature());
        download.setMd5(resp.getMd5());
        download.setFileLength(resp.getFileLength());
        download.setVersion(resp.getAppOutVersion());
    }

    private boolean getFileMD5(String saveFileName) {
        try {
            InputStream is = new FileInputStream(saveFileName);
            String sMd5 = MD5Util.getFileMD5(is);
            if (null != sMd5 && sMd5.equals(CommonPreferences.getFileMd5())) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void setLastLoginName(NetUserInfo netuserInfo, int... what) {
        if (what.length == 1) {
            int msgCode = what[0];
            if (msgCode == CoreServiceMSG.MSG_REGISTER_SUCESS
                    || msgCode == CoreServiceMSG.MSG_BIND_SUCESS
                    || msgCode == CoreServiceMSG.MSG_REBIND_SUCESS) {
                SLog.d(TAG, "save lastlogin name by phone");
                if (!TextUtils.isEmpty(netuserInfo.getUmobile())) {
                    CommonPreferences.setLastLoginName(netuserInfo.getUmobile());
                }
            }
        }
    }

    /**
     * 分页获取时,用作转换
     * 
     * @ClassName: TransationRestorableConacts
     * @date 2012-4-13 上午11:39:21
     */
    class TransationRestorableConacts {
        ArrayList<RestorableContacts> list;
        int totalSize;
        boolean isSuccess;
    }

    /**
     * 按页获取联系人列表(页码从1开始)
     */
    private TransationRestorableConacts getRestorableConactsByPage(int page) {
        TransationRestorableConacts tranContacts = new TransationRestorableConacts();
        tranContacts.list = new ArrayList<RestorableContacts>();
        NetRestorableContactsResponse resp = mSettingsNetModule.getRestorableConacts(page,
                Constants.PAGESIZE);
        if (resp.isSuccess()) {
            tranContacts.list = resp.getRestorableContacts();
            tranContacts.isSuccess = true;
            tranContacts.totalSize = resp.getTotalSize();
        } else {
            tranContacts.isSuccess = false;
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            SLog.e(TAG, "getRestorableConacts Fail Page =  " + page);
        }
        return tranContacts;
    }

    /**
     * 查看可恢复联系人列表
     * 
     * @param start 起始页
     * @param pageSize 每页显示数量
     * @return
     */
    public void getRestorableConacts() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_GET_RESTORECONTACTS_BEGIN, null);
                ArrayList<RestorableContacts> list = new ArrayList<RestorableContacts>();
                ArrayList<Integer> notSuccessPage = new ArrayList<Integer>(); // 记录未能正常获取的页码
                int current = 1;
                TransationRestorableConacts transationRestorableConacts = getRestorableConactsByPage(current);
                int totalsize = transationRestorableConacts.totalSize;
                int totalPage = ((totalsize / Constants.PAGESIZE) + (totalsize % Constants.PAGESIZE > 0 ? 1
                        : 0));
                SLog.d(TAG, "totalsize = " + totalsize + " , totalPage = " + totalPage);
                current++;
                list.addAll(transationRestorableConacts.list);
                TransationRestorableConacts tran = null;
                while (current <= totalPage) {
                    tran = getRestorableConactsByPage(current);
                    if (tran.isSuccess) {
                        list.addAll(tran.list);
                        SLog.d(TAG, "获取恢复联系人 success page = " + current);
                    } else {
                        notSuccessPage.add(current);
                    }
                    current++;
                }
                // 将错误的页数再次获取，再失败那就无语了
                for (int page : notSuccessPage) {
                    tran = getRestorableConactsByPage(page);
                    if (tran.isSuccess) {
                        list.addAll(tran.list);
                        SLog.d(TAG, "重新获取恢复联系人 success page = " + page);
                    } else {
                        SLog.d(TAG, "重现获取仍然失败  那就没救了  page = " + page);
                    }
                }

                if (totalsize != list.size() || !transationRestorableConacts.isSuccess) {
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_GET_RESTORECONTACTS_FAIL,
                            null);
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_GET_RESTORECONTACTS_END,
                            list);
                }
            }
        });
    }

    /**
     * 批量恢复联系人
     * 
     * @param rids 联系人列表
     * @return
     */
    public void restoreContacts(final ArrayList<RestorableContacts> restorableContacts) {
        // 第一步 调用云端恢复联系人接口
        // 第二步 调用增量联系人列表获取接口（只关心增加的人）
        // 第三步 调用联系人详情接口
        // 第四步 调用手信本地写入接口
        mThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_BEGIN, null);

                // step 1: 调用云端恢复联系人接口
                ArrayList<Integer> rids = new ArrayList<Integer>();
                for (RestorableContacts c : restorableContacts) {
                    rids.add(c.getRestoreId());
                }
                NetRestoreContactsResp resp = mSettingsNetModule.restoreContacts(rids);
                if (!resp.isSuccess()) {
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_FAIL,
                            null);
                    return;
                }
                // step 2: 调用增量联系人列表获取接口
                ArrayList<Contact> restoreList = null;
                try {
                    restoreList = mContactsNetModule.getIncContactsList();
                } catch (Exception e) {
                    service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_FAIL,
                            null);
                    return;
                }
                // step 3: 调用联系人详情接口,获取头像，帐号，在线状态
                if (null != restoreList && restoreList.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Contact c : restoreList) {
                        if (c.getCloudId() > 0 && null != c.getAccounts()
                                && c.getAccounts().size() > 0) {
                            for (Account account : c.getAccounts()) {
                                if (account.getSkyId() > 0) {
                                    sb.append(c.getCloudId()).append(",")
                                            .append(account.getSkyId())
                                            .append("|");
                                }
                            }
                        }
                    }

                    String skyids = sb.toString();
                    if (null != skyids && !skyids.equals("") && skyids.endsWith("|")) {
                        skyids = skyids.substring(0, skyids.length() - 1);
                    }

                    ArrayList<Contact> list = mContactsNetModule.getSpecifiedContactsStatus(skyids,
                            null, 0);

                    for (Contact c : restoreList) {
                        for (Contact c1 : list) {
                            if (c.getCloudId() == c1.getCloudId()) {
                                c.setPhotoId(c1.getPhotoId());
                                break;
                            }
                        }
                    }
                }
                // 写入数据库
                ContactsDAO contactsDAO = DaoFactory.getInstance(
                        MainApp.i().getApplicationContext()).getContactsDAO();
                ArrayList<Contact> contactsList = contactsDAO.getContactInfoForList();
                HashMap<Long, Contact> contactMap = new HashMap<Long, Contact>();
                for (Contact contact : contactsList) {
                    if (contactMap.get(contact.getCloudId()) == null) {
                        contactMap.put(contact.getCloudId(), contact);
                    }
                }
                // 2012-09-20 新增对联系人数据在本地的操作区分新增还是更新
                for (Contact c : restoreList) {
                    Contact contactOld = contactMap.get(c.getCloudId());
                    if (contactOld == null) {
                        contactsDAO.addContact(c);
                    } else {
                        for (Account account : c.getAccounts()) {
                            account.setContactId(contactOld.getId());
                        }
                        contactOld.setAccounts(c.getAccounts());
                        contactsDAO.updateContactAccounts(contactOld);
                    }
                }
                // 2012-09-20 在写数据库之后更新文件中的联系人版本号
                long contactVersion = APPCache.getInstance().getContactVersion();
                if (contactVersion != 0) {
                    CommonPreferences.saveContactsLastTimeUpdate(contactVersion);
                }
                // 刷新联系人列表cache
                service.notifyObservers(CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_END,
                        null);
            }
        });
    }

    private void initNotification(Activity activity) {
        mNotifManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);

        mDownNotification = new Notification(
                android.R.drawable.stat_sys_download, activity
                        .getString(R.string.notifiy_down_file), System
                        .currentTimeMillis());

        mDownNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mDownNotification.flags = Notification.FLAG_AUTO_CANCEL;

        mContentView = new RemoteViews(activity.getPackageName(),
                R.layout.custom_notification);
        // 设置图片
        mContentView.setImageViewResource(R.id.downLoadIcon,
                android.R.drawable.stat_sys_download);
        mDownPendingIntent = PendingIntent
                .getActivity(activity, 0, new Intent(), 0);
    }

    private void successNotification(Activity activity, String saveFileName) {
        mDownNotification = new Notification(
                R.drawable.new_message_notification, activity
                        .getString(R.string.download_success),
                System.currentTimeMillis());
        mDownNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mDownNotification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(saveFileName)),
                "application/vnd.android.package-archive");
        PendingIntent contentIntent = PendingIntent.getActivity(
                activity, 0, intent, 0);
        mDownNotification.setLatestEventInfo(activity, activity
                .getString(R.string.download_success), null,
                contentIntent);
        mNotifManager.notify(CUSTOM_NOTIFIATION_ID, mDownNotification);
    }

    private void progressNotification(int progress) {
        mContentView.setTextViewText(R.id.progressPercent,
                progress + "%");
        mContentView.setProgressBar(R.id.downLoadProgress,
                100, progress, false);
        mDownNotification.contentView = mContentView;
        mDownNotification.contentIntent = mDownPendingIntent;
        mNotifManager.notify(CUSTOM_NOTIFIATION_ID, mDownNotification);
    }

    private void errorNotification(Activity activity) {
        mDownNotification = new Notification(
                R.drawable.new_message_notification, activity
                        .getString(R.string.download_failed),
                System.currentTimeMillis());
        mDownNotification.flags = Notification.FLAG_AUTO_CANCEL;
        PendingIntent contentIntent = PendingIntent.getActivity(
                activity, 0, new Intent(), 0);
        mDownNotification.setLatestEventInfo(activity, activity
                .getString(R.string.download_failed), null,
                contentIntent);
        mNotifManager.notify(CUSTOM_NOTIFIATION_ID,
                mDownNotification);
    }
}
