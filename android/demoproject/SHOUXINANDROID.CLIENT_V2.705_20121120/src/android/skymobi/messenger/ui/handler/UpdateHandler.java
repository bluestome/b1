
package android.skymobi.messenger.ui.handler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.DownloadInfo;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.FileUtils;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.StringUtil;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: UpdateHandler
 * @Description: 自更新 Handler
 * @author Anson.Yang
 * @date 2012-3-19 下午1:57:01
 */
public class UpdateHandler extends Handler {
    private static final String TAG = "UpdateHandler";
    private final BaseActivity activity;
    private final Map<Integer, Dialog> dialogs = new HashMap<Integer, Dialog>();
    private DownloadInfo downloadInfo;
    private static final int INSTALL_APK = 10;
    private static final int SDCARD_NOTFOUND = 11;
    private File saveFile;

    public UpdateHandler(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CoreServiceMSG.MSG_LOGIN_CHECK_NET_ERROR:
            case CoreServiceMSG.MSG_LOGIN_CHECK_NOUPDATE:
            case CoreServiceMSG.MSG_LOGIN_CHECK_ERROR:
                SLog.d(TAG, "UpdateHandler MSG_NET_ERROR or no udpate");
                startNextPage();
                break;
            case CoreServiceMSG.MSG_LOGIN_CHECK_UPDATE:
                SLog.d(TAG, "check update resp");
                downloadInfo = (DownloadInfo) msg.obj;
                saveCheckDownloadInfo(downloadInfo);
                initDialogAndDeleteApks(CoreServiceMSG.MSG_LOGIN_CHECK_UPDATE);
                break;
            case CoreServiceMSG.MSG_LOGIN_CHECK_FORCE_UPDATE:
                SLog.d(TAG, "force update resp");
                downloadInfo = (DownloadInfo) msg.obj;
                saveForceDownloadInfo(downloadInfo);
                initDialogAndDeleteApks(CoreServiceMSG.MSG_LOGIN_CHECK_FORCE_UPDATE);
                break;
            case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_ERROR:
                SLog.d(TAG, "download error");
                initDialog(CoreServiceMSG.MSG_LOGIN_CHECK_DOWNLOAD_ERROR);
                break;
            case CoreServiceMSG.MSG_LOGIN_DOWNLOAD: // 下载完成
                SLog.d(TAG, "force download success");
                getSaveFile();
                completedDownload();
                initDialog(INSTALL_APK);
                boolean isForce = CommonPreferences.getIsForce();
                if (isForce) {
                    CommonPreferences.saveLoginTimes(0);
                }
                break;
            case CoreServiceMSG.MSG_LOGIN_FORCE_DOWNLOAD: // 强制更新
                forceDownlonding(msg.obj);
                break;
            case CoreServiceMSG.MSG_SETTINGS_UPDATE_APP: // 立即更新
                SLog.d(TAG, "settings update app");
                processDownload(false);
                break;
            case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_NET_ERROR:
                SLog.d(TAG, "download net error");
                initDialog(CoreServiceMSG.MSG_LOGIN_DOWNLOAD_NET_ERROR);
                break;
            case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_RETRY:
                SLog.d(TAG, "download retry");
                initDialog(CoreServiceMSG.MSG_LOGIN_DOWNLOAD_RETRY);
                break;
        }
    }

    /**
     * @param code
     */
    private void showDialog(int code) {
        Dialog dialog = dialogs.get(code);
        if (dialog == null) {
            dialog = createDialog(code);
            dialogs.put(code, dialog);
            dialog.show();
        } else {
            dialog.show();
        }
    }

    /**
     * @param code
     * @return
     */
    private Dialog createDialog(int code) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.update_tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        switch (code) {
            case CoreServiceMSG.MSG_LOGIN_CHECK_UPDATE:
                SLog.d(TAG, "check update");
                String feature = downloadInfo.getFeature();

                dialogBuilder.setMessage(feature);
                dialogBuilder.setPositiveButton(R.string.update_now,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (processDownload(false)) {
                                    startNextPage();
                                }
                            }
                        });
                dialogBuilder.setNegativeButton(R.string.update_later,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startNextPage();
                            }
                        });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {// 返回
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                startNextPage();
                            }
                        });
                break;
            case CoreServiceMSG.MSG_LOGIN_CHECK_FORCE_UPDATE:
                SLog.d(TAG, "check force update");
                String forceFeature = downloadInfo.getFeature();
                dialogBuilder.setMessage(forceFeature);
                dialogBuilder.setPositiveButton(R.string.update_now,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                processDownload(true);
                            }
                        });
                dialogBuilder.setNegativeButton(R.string.quit,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { // 退出
                                dialog.dismiss();
                                stopService();
                            }
                        });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        stopService();
                    }
                });
                break;
            case INSTALL_APK:
                SLog.d(TAG, "install apk");
                final boolean isForce = CommonPreferences.getIsForce();
                if (isForce) {
                    if (activity instanceof LaunchActivity) {
                        ((LaunchActivity) activity).enableProcess(false, 0);
                    }
                }
                dialogBuilder.setMessage(R.string.apk_download_success);
                dialogBuilder.setPositiveButton(R.string.install_now,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (!isForce) {
                                    startNextPage();
                                }
                                SLog.d(TAG, "saveFile:" + saveFile);
                                installApk(saveFile);
                                if (isForce) {
                                    stopService();
                                }
                            }
                        });
                dialogBuilder.setNegativeButton(R.string.install_later,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (isForce) {
                                    stopService();
                                } else {
                                    startNextPage();
                                }
                            }
                        });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        SLog.d(TAG, "cancel :" + isForce);
                        if (isForce) {
                            stopService();
                        }
                    }
                });
                break;
            case CoreServiceMSG.MSG_LOGIN_CHECK_DOWNLOAD_ERROR: // 重试
            case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_NET_ERROR:
            case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_RETRY:
                completedDownload();
                final boolean force = CommonPreferences.getIsForce();
                Log.d(TAG, "download retry:" + force);
                dialogBuilder.setMessage(activity.getString(R.string.download_error) + "\r\n["
                        + Constants.ERROR_TIP + ":0x"
                        + StringUtil.autoFixZero(ResultCode.getCode()) + "]");
                dialogBuilder.setPositiveButton(R.string.retry,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                processDownload(force);
                            }
                        });
                dialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (force) {
                                    stopService();
                                }
                            }
                        });
                break;
            case SDCARD_NOTFOUND:
                Log.d(TAG, "sdcard not found");
                final boolean download_force = CommonPreferences.getIsForce();
                dialogBuilder.setMessage(R.string.no_sdcard_mount);
                dialogBuilder.setPositiveButton(R.string.iknow,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (download_force) {
                                    stopService();
                                } else {
                                    startNextPage();
                                }
                            }
                        });
                break;
            default:
                break;
        }
        return dialogBuilder.create();
    }

    /**
     * @param isForce download by force or not
     * @return true if begin downloading, false if not.
     */
    private boolean processDownload(boolean isForce) {
        if (-1 == AndroidSysUtils.checkSDCard(activity)) {
            initDialog(SDCARD_NOTFOUND);
        } else {
            getSaveFile();

            int filelen = CommonPreferences.getAppFilelen();
            int startPos = 0;
            String md5 = null;
            if (saveFile.exists()) {
                startPos = (int) saveFile.length();
                md5 = CommonPreferences.getFileMd5().equals("null") ? null
                        : CommonPreferences.getFileMd5();
                SLog.d(TAG, "md5=" + md5 + ",startPos=" + startPos);
            } else {
                if (!saveFile.getParentFile().exists()) {
                    SLog.d(TAG, " create folder:" + saveFile.getAbsolutePath());
                    saveFile.getParentFile().mkdirs();
                }
            }

            if (filelen > 0 && startPos > 0 && filelen == startPos) {// 直接安装
                completedDownload();
                initDialog(INSTALL_APK);
            } else {
                if (isForce) {
                    if (activity instanceof LaunchActivity) { // 显示进度条
                        ((LaunchActivity) activity).enableProcess(true, filelen);
                    }
                }
                // 设置正在下载的标志
                setDownloding(true);

                SLog.d(TAG, " file size:" + filelen);
                String saveFileName = saveFile.getPath();
                CoreService.getInstance().getSettingsModule().download(md5,
                        startPos, filelen, isForce, activity, saveFileName);
                return true;
            }
        }
        return false;
    }

    /**
     * 
     */
    private void getSaveFile() {
        File folder = new File(Constants.APK_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String appVerion = CommonPreferences.getAppVerion();
        if (null == appVerion || "null".equals(appVerion)) {
            appVerion = Constants.PRE_APK_NAME
                    .concat(Constants.SUF_APK_NAME);
        } else {
            appVerion = Constants.PRE_APK_NAME.concat(appVerion)
                    .concat(Constants.SUF_APK_NAME);
        }
        SLog.d(TAG, "filePath = " + appVerion);
        saveFile = new File(folder, appVerion);
    }

    /**
     * 删除更新的以前不同版本apk
     */
    private void deleteApkFiles() {
        String localversion = MainApp.i().getPi().versionName;
        SLog.d(TAG, "localversion: " + localversion + ", version" + downloadInfo.getVersion());
        if (null != downloadInfo.getVersion() && null !=
                localversion
                && !localversion.equals(downloadInfo.getVersion())) {
            getSaveFile();
            FileUtils.deleteDirExceptSpefile(Constants.APK_PATH, saveFile);
            SLog.d(TAG, "delete files:" + Constants.APK_PATH);
        }
    }

    private void saveCheckDownloadInfo(DownloadInfo download) {
        CommonPreferences.saveAppVerion(download.getVersion());
        CommonPreferences.saveAppFilelen(download.getFileLength());
        CommonPreferences.saveIsNew(true);
        CommonPreferences.saveIsForce(false);// 非强制更新
        CommonPreferences.saveCheckAfterTimes(download.getCheckAfterTimes());
        CommonPreferences.saveCheckInterval(download.getCheckInterval());
    }

    private void saveForceDownloadInfo(DownloadInfo download) {
        CommonPreferences.saveAppVerion(download.getVersion());
        CommonPreferences.saveAppFilelen(download.getFileLength());
        CommonPreferences.saveIsForce(true);// 强制更新
        CommonPreferences.saveCheckAfterTimes(download.getCheckAfterTimes());
        CommonPreferences.saveCheckInterval(download.getCheckInterval());
    }

    /**
     * @param what
     */
    private void initDialogAndDeleteApks(int what) {
        if (!activity.isFinishing()) {
            showDialog(what);
            deleteApkFiles();
        }
    }

    private void initDialog(int what) {
        if (!activity.isFinishing()) {
            showDialog(what);
        }
    }

    private void startNextPage() {
        if (activity instanceof LaunchActivity) {
            ((LaunchActivity) activity).startNextPage();
        }
    }

    private void stopService() {
        MainApp.i().stopService();
    }

    private void forceDownlonding(Object obj) {
        if (null != obj) {
            int size = (Integer) obj;
            SLog.d(TAG, "force download size:" + size);
            if (activity instanceof LaunchActivity) {
                ((LaunchActivity) activity).progress(size);
            }
        }
    }

    private void installApk(File file) {
        if (null != file && file.exists()) {
            Intent apkIntent = new Intent(Intent.ACTION_VIEW);
            apkIntent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
            activity.startActivity(apkIntent);
        }
    }

    private void completedDownload() {
        setDownloding(false);
    }

    private void setDownloding(boolean downloading) {
        MainApp.i().setDownloading(downloading);
    }
}
