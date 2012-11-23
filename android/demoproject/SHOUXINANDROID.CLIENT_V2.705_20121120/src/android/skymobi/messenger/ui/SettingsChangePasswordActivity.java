
package android.skymobi.messenger.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.utils.CommonPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * @ClassName: SettingsChangePasswordActivity
 * @Description: TODO
 * @author Lv.Lv
 * @date 2012-3-12 下午1:54:58
 */
public class SettingsChangePasswordActivity extends TopActivity {

    private static final int DIALOG_WAITING = 0;
    public static final String ACCOUNT = "account";
    private EditText mOldPwdEditText = null;
    private EditText mNewPwdEditText = null;
    private EditText mConfirmPwdEditText = null;
    private SettingsModule settingsModule = null;
    private static final int MIN_LEN_PWD = 6;
    private static final int MAX_LEN_PWD = 12;

    private static final String pswchars = "^[0-9A-Za-z\\*#]+$"; // 密码仅允许字母、数字、*#
    private static final Pattern pattern = Pattern.compile(pswchars);

    private ProgressDialog mWaitDialog = null;
    // Handle message
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SETTINGS_MODIFYPWD_FAIL:
                    hideWaitDialog();
                    boolean bPasswordError = (Boolean) msg.obj;
                    if (bPasswordError) {
                        showToast(R.string.settings_modify_pwd_error1);
                    } else {
                        showToast(R.string.settings_modify_pwd_fail);
                    }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_MODIFYPWD_SUCCESS: {
                    byte[] encryptPasswd = (byte[]) msg.obj;
                    UserInfo userInfo = CommonPreferences.getUserInfo();
                    if (userInfo != null) {
                        userInfo.encryptPasswd = encryptPasswd;
                        CommonPreferences.setUserInfo(userInfo);
                    }
                    hideWaitDialog();
                    showToast(R.string.settings_modify_pwd_success);
                    finish();
                }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_change_password);
        try {
            settingsModule = mService.getSettingsModule();
            initTopBar();
            init();
        } catch (Exception e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
    }

    private void init() {
        String accountID = getIntent().getStringExtra(ACCOUNT);
        UserInfo info = CommonPreferences.getUserInfo();
        if (accountID == null && info != null) {
            accountID = info.name;
        }

        // 帐号
        TextView accountTV = (TextView) findViewById(R.id.text_account);
        accountTV.setText(accountID);

        mOldPwdEditText = (EditText) findViewById(R.id.settings_old_password);
        mNewPwdEditText = (EditText) findViewById(R.id.settings_new_password);
        mConfirmPwdEditText = (EditText) findViewById(R.id.settings_confirm_password);
    }

    private void showWaitDialog() {
        mWaitDialog = new ProgressDialog(this);
        mWaitDialog.setMessage(getString(R.string.settings_waitting));
        mWaitDialog.setIndeterminate(true);
        mWaitDialog.setCancelable(false);
        mWaitDialog.show();
    }

    private void hideWaitDialog() {
        mWaitDialog.cancel();
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.settings_change_password);
        // 最好将类中的action使用起来
        OnClickListener click = new OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPwd = mOldPwdEditText.getText().toString();
                String newPwd = mNewPwdEditText.getText().toString();
                String confirmPwd = mConfirmPwdEditText.getText().toString();
                if (oldPwd.length() <= 0) {
                    showToast(R.string.settings_modify_pwd_error1);
                } else if (newPwd.length() < MIN_LEN_PWD) {
                    showToast(R.string.settings_modify_pwd_error2);
                } else if (newPwd.length() > MAX_LEN_PWD) {
                    showToast(R.string.settings_modify_pwd_error3);
                } else if (!pattern.matcher(newPwd).matches()) {
                    showToast(R.string.settings_modify_pwd_error4);
                } else if (!newPwd.equals(confirmPwd)) {
                    showToast(R.string.settings_modify_pwd_error5);
                } else {
                    settingsModule.modifyPwd(oldPwd, newPwd);
                    showWaitDialog();
                }
            }
        };
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_save, click);

    }
}
