
package android.skymobi.messenger.ui.action;

import android.content.Context;
import android.content.Intent;
import android.skymobi.messenger.R;
import android.skymobi.messenger.cache.APPCache;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.LoginActivity;
import android.skymobi.messenger.ui.RegisterActivity;
import android.skymobi.messenger.ui.handler.LoginHandler;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.CommonPreferences;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.skymobi.android.sx.codec.util.SysUtils;

/**
 * @ClassName: LoginAction
 * @Description: 登陆界面动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:40
 */
public class LoginAction extends BaseAction implements OnClickListener {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param activity
     */
    public LoginAction(BaseActivity activity) {
        super(activity);
    }

    private void focusEditor(int resId) {
        final EditText editor = (EditText) activity.findViewById(resId);
        editor.postDelayed(new Runnable() {
            @Override
            public void run() {
                editor.requestFocus();
                InputMethodManager imm = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case LoginActivity.BUTTON_REGISTER:
                // 点击按钮时把软件键盘隐藏掉
                activity.hideSystemSoftKeyboard((EditText) activity
                        .findViewById(R.id.login_username_editor));
                // activity.showDialog(LoginHandler.DIALOG_REGISTER_CODE);
                // mService.getCommonModule().activate();
                /*
                 * ((LoginActivity) activity).setTimeout(MainApp.getInstance()
                 * .getApplicationContext()
                 * .getString(R.string.login_free_register_timeout),
                 * CoreServiceMSG.MSG_FAILED);
                 */
                activity.finish();
                Intent intent = new Intent(activity, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.login_login_btn:
                String name = activity.getEditorText(R.id.login_username_editor);
                String pwd = activity.getEditorText(R.id.login_password_editor);
                if (TextUtils.isEmpty(name)) {
                    showToast(R.string.login_no_username);
                    activity.setTextViewValue(R.id.login_password_editor, "");
                    focusEditor(R.id.login_username_editor);
                } else if (name.length() < 3) {
                    activity.setTextViewValue(R.id.login_password_editor, "");
                    activity.setTextViewValue(R.id.login_username_editor, "");
                    focusEditor(R.id.login_username_editor);
                    showToast(R.string.login_notexist_username);
                } else if (TextUtils.isEmpty(pwd)) {
                    focusEditor(R.id.login_password_editor);
                    showToast(R.string.login_no_password);
                } else {
                    ((LoginActivity) activity).reset();
                    showDialog(LoginHandler.DIALOG_LOGIN_CODE);
                    // 记录手动登陆状态, 手动登陆后接到激活消息不做处理,因为收到激活消息,会清除数据,导致云端重复
                    APPCache.getInstance().setManualLogined(true);
                    if (mService != null)
                        mService.getCommonModule().login(name, SysUtils.pwdEncrypt(pwd));
                    ((LoginActivity) activity).setLoginTimeout();
                    if (activity != null && (!activity.isFinishing())) {
                        try {
                            activity.hideSystemSoftKeyboard((EditText) activity
                                    .findViewById(R.id.login_password_editor));
                        } catch (Exception e) {
                        }
                    }

                }
                break;
            case R.id.login_find_password:
                // 点击按钮时把软件键盘隐藏掉
                activity.hideSystemSoftKeyboard((EditText) activity
                        .findViewById(R.id.login_username_editor));
                boolean hasSimCard = AndroidSysUtils.getSimCard(activity);
                if (hasSimCard) {
                    mService.setSMSMaxID();
                    // 上次找回密码成功
                    long findpassword = CommonPreferences.getFindPassword();
                    long fiveMinute = 5 * DateUtils.MINUTE_IN_MILLIS;
                    boolean result = CommonPreferences.getFindPasswordResult();// 短信是否发送成功
                    boolean findSuccess = CommonPreferences.getFindPasswordSuccess(); // 是否收到验证短信
                    if (result) {
                        if (findSuccess) {
                            getBindAndInit();
                        } else {
                            if (findpassword == -1
                                        || System.currentTimeMillis() - findpassword > fiveMinute) {
                                getBindAndInit();
                            } else {
                                activity.showToast(R.string.forget_password_waiting);
                            }
                        }
                    } else {
                        getBindAndInit();
                    }
                } else {
                    activity.showToast(R.string.forget_password_no_simcard);
                }
                break;
        }
    }

    /**
     * 获取绑定状态和初始化绑定参数
     */
    private void getBindAndInit() {
        mService.getCommonModule().getBind();
        CommonPreferences.saveFindPassword(System.currentTimeMillis());
        CommonPreferences.saveFindPasswordSuccess(false);
        CommonPreferences.saveFindPasswordResult(false);
    }

}
