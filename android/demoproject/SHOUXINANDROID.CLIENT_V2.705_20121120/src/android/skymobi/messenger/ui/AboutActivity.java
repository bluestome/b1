
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.DownloadInfo;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.FileUtils;
import android.skymobi.messenger.utils.StringUtil;
import android.skymobi.messenger.widget.SafeLinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @ClassName: AboutActivity
 * @author Lv.Lv
 * @date 2012-3-13 下午4:28:59
 */
public class AboutActivity extends TopActivity {
    private static final int IS_NEWEST = 10;
    private static final int IS_CHECKING = 20;

    private Button updateBtn = null;
    private Button checkBtn = null;
    private LinearLayout new_version = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        initTopBar();
        init();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            removeDialog(IS_CHECKING);
            switch (msg.what) {
                case CoreServiceMSG.MSG_LOGIN_CHECK_DOWNLOAD_ERROR: // 重试
                case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_NET_ERROR:
                case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_RETRY:
                case CoreServiceMSG.MSG_LOGIN_DOWNLOAD:
                case CoreServiceMSG.MSG_LOGIN_DOWNLOAD_ERROR:
                    SLog.d("AboutActivity", "download error");
                    enableDownloading();
                    break;
                case CoreServiceMSG.MSG_LOGIN_CHECK_FORCE_UPDATE:
                case CoreServiceMSG.MSG_LOGIN_CHECK_UPDATE:
                    SLog.d("AboutActivity", "download app completed");
                    DownloadInfo downloadInfo = (DownloadInfo) msg.obj;
                    displayDownloadView(downloadInfo);
                    break;
                case CoreServiceMSG.MSG_LOGIN_CHECK_NOUPDATE:
                    SLog.d("AboutActivity", "check download error");
                    showDialog(IS_NEWEST);
                    checkBtn.setEnabled(true);
                    break;
                case CoreServiceMSG.MSG_LOGIN_CHECK_NET_ERROR:
                case CoreServiceMSG.MSG_LOGIN_CHECK_ERROR:
                    checkBtn.setEnabled(true);
                    if (null != msg.obj) {
                        showToast(getString(R.string.check_error) + "\r\n[" + Constants.ERROR_TIP
                                + ":0x" + StringUtil.autoFixZero((Integer) msg.obj) + "]");
                    } else {
                        showToast(R.string.check_error);
                    }
                    break;
            }
        }
    };

    private void init() {

        initView();
        TextView versionTV = (TextView) findViewById(R.id.text_cur_version_number);
        String version = MainApp.i().getPi().versionName;
        versionTV.setText(version);

        TextView email = (TextView) findViewById(R.id.text_cs_email);
        email.setMovementMethod(SafeLinkMovementMethod.getInstance());

        // 没有新版本,显示立即更新按钮,
        // TODO 还需要比较本地的版本和服务端的版本,并改写配置文件
        // http://redmine.sky-mobi.com/redmine/issues/12776
        if (!CommonPreferences.getIsNew()) {
            enableCheckView(); // 显示检查更新
            checkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(IS_CHECKING);
                    checkBtn.setEnabled(false);
                    mService.getSettingsModule().checkUpdate(false);
                }
            });
            return;
        }

        String appVersion = CommonPreferences.getAppVerion();
        String appFileLength = FileUtils.getFormatSizeMB(CommonPreferences.getAppFilelen());

        // 新版本号
        TextView newVersionTV = (TextView) findViewById(R.id.text_new_version);
        if (appVersion != null) {
            newVersionTV.setText(appVersion);
        }

        // 文件大小
        TextView filesizeTV = (TextView) findViewById(R.id.text_file_size);
        if (appFileLength != null) {
            filesizeTV.setText(appFileLength);
        }

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDownloading()) {
                    // 正在更新过程
                    showToast(R.string.download_now_toast);
                } else {
                    updateHandler.sendMessage(Message.obtain(updateHandler,
                            CoreServiceMSG.MSG_SETTINGS_UPDATE_APP, null));
                }
            }
        });
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        dialogBuilder.setCancelable(true);
        switch (id) {
            case IS_NEWEST:
                dialogBuilder.setMessage(R.string.settings_version_isNewest);
                dialogBuilder.setNegativeButton(R.string.iknow, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                break;
            case IS_CHECKING:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.is_checking), true, true);
            default:
                break;
        }
        dialog = dialogBuilder.create();
        return dialog;
    }

    private void initView() {
        updateBtn = (Button) findViewById(R.id.settings_update_btn);
        checkBtn = (Button) findViewById(R.id.settings_check_btn);
        new_version = (LinearLayout) findViewById(R.id.layout_newVersion_display);

        updateBtn.setVisibility(View.VISIBLE);
        checkBtn.setVisibility(View.GONE);
        new_version.setVisibility(View.VISIBLE);
    }

    private void enableCheckView() {
        updateBtn.setVisibility(View.GONE);
        checkBtn.setVisibility(View.VISIBLE);
        new_version.setVisibility(View.GONE);
        checkBtn.setEnabled(true);
    }

    private void enableDownloading() {
        setDownloding(false);
        checkBtn.setEnabled(true);
    }

    private void displayDownloadView(DownloadInfo downloadInfo) {
        if (null != downloadInfo) {
            CommonPreferences.saveAppVerion(downloadInfo.getVersion());
            CommonPreferences.saveAppFilelen(downloadInfo.getFileLength());
        }
        CommonPreferences.saveIsNew(true);
        init();
        checkBtn.setEnabled(true);
    }

    private boolean isDownloading() {
        return MainApp.i().isDownloading();
    }

    private void setDownloding(boolean downloading) {
        MainApp.i().setDownloading(downloading);
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.about);
    }
}
