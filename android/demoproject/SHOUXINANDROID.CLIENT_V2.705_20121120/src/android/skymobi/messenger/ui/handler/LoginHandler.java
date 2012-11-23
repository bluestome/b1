
package android.skymobi.messenger.ui.handler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.LoginActivity;
import android.skymobi.messenger.ui.MainActivity;
import android.skymobi.messenger.ui.SetNickNameActivity;
import android.skymobi.messenger.ui.SettingsChangePasswordActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.RegexUtil;
import android.skymobi.messenger.utils.StringUtil;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.skymobi.android.sx.codec.beans.clientbean.NetBindResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetForgetPwdResponse;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @ClassName: LoginHandler
 * @author Sean.Xie
 * @date 2012-3-14 上午10:39:23
 */
public class LoginHandler extends Handler {

    private static final String TAG = "LoginHandler";
    public static final int DIALOG_REGISTER_CODE = 0;
    public static final int DIALOG_LOGIN_CODE = 1;
    public static final int DIALOG_BIND_CODE = 3;
    public static final int DIALOG_REBIND_CODE = 4;
    public static final int DIALOG_LOGIN_OTHER_PHONE = 5;
    public static final int DIALOG_LOGIN_FREEZE = 6; // 你的帐号被冻结提示框

    public static int DIALOG_COUNT = 0;

    public static final int ENABLE_BUTTON = 310;

    private static final String puserchars = "^[0-9A-Za-z]+$"; // 用户名仅允许字母、数字
    private static final Pattern pattern = Pattern.compile(puserchars);

    private final BaseActivity activity;

    private UserInfo info;
    private String mobile;
    private ArrayList<String> split = new ArrayList<String>();
    private boolean findPassword = false;

    private ProgressDialog progressDialog;
    private final Handler timeHhander = new Handler();
    private Runnable r;
    private boolean finished;
    private int buttonId;

    public LoginHandler(BaseActivity activity) {
        this.activity = activity;
        finished = false;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            // 注册
            case CoreServiceMSG.MSG_SUCCESS:
                info = (UserInfo) msg.obj;
                break;
            case CoreServiceMSG.MSG_FAILED:
                if (!finished) {
                    info = (UserInfo) msg.obj;
                    activity.removeDialog(DIALOG_REGISTER_CODE);
                    activity.removeDialog(DIALOG_REBIND_CODE);
                    activity.removeDialog(DIALOG_BIND_CODE);
                    activity.showToast(info.resultHint);
                    finished = true;
                }
                break;
            case CoreServiceMSG.MSG_REGISTER_ISBIND:
                info = (UserInfo) msg.obj;
                if (null != info && !finished) {
                    activity.removeDialog(DIALOG_REGISTER_CODE);
                    activity.showDialog(CoreServiceMSG.MSG_REGISTER_ISBIND);
                }
                break;
            // 2012-11-20 @bluestome.zhang QC_BUG #145
            case CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST:
                // 用户名不存在
                activity.removeDialog(DIALOG_LOGIN_CODE);
                activity.showDialog(msg.what);
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IS_PHONE:
                // 提示用户使用该手机号码的手机进行注册
                info = (UserInfo) msg.obj;
                activity.removeDialog(DIALOG_LOGIN_CODE);
                activity.showDialog(msg.what);
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST_IMSI_BIND:
                info = (UserInfo) msg.obj;
                activity.removeDialog(DIALOG_LOGIN_CODE);
                activity.showDialog(msg.what);
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST_IMSI_UNBIND:
                info = (UserInfo) msg.obj;
                activity.removeDialog(DIALOG_LOGIN_CODE);
                String username = activity.getEditorText(R.id.login_username_editor);
                String pwd = activity.getEditorText(R.id.login_password_editor);
                if (TextUtils.isDigitsOnly(username) && username.length() == 11) {
                    activity.showDialog(CoreServiceMSG.MSG_LOGIN_USERNAME_IS_PHONENUMBER);
                } else if (!pattern.matcher(username).matches()) {
                    activity.showDialog(CoreServiceMSG.MSG_LOGIN_USERNAME_IS_PHONENUMBER);
                } else if (!(pwd.trim().length() > 5 && pwd.trim().length() < 13)) {
                    activity.showDialog(CoreServiceMSG.MSG_LOGIN_PASSWORD_NOT_MACTH);
                } else if (username.length() < 4 || username.length() > 12) {
                    activity.showDialog(CoreServiceMSG.MSG_LOGIN_USERNAME_IS_PHONENUMBER);
                } else {
                    activity.showDialog(msg.what);
                }
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IMSI_UNBIND:
                finished = true;
                info = (UserInfo) msg.obj;
                activity.removeDialog(DIALOG_LOGIN_CODE);
                String username1 = activity.getEditorText(R.id.login_username_editor);
                if (TextUtils.isDigitsOnly(username1) && username1.length() == 11) {
                    activity.showDialog(CoreServiceMSG.MSG_LOGIN_USERNAME_IS_PHONENUMBER);
                } else {
                    activity.showDialog(msg.what);
                }
                break;
            case CoreServiceMSG.MSG_LOGIN_NET_ERROR:
            case CoreServiceMSG.MSG_LOGIN_PASSWORD_ERROR:
                info = (UserInfo) msg.obj;
                final EditText password = (EditText) activity
                        .findViewById(R.id.login_password_editor);
                password.setText("");
                activity.removeDialog(DIALOG_LOGIN_CODE);
                activity.showToast(info.resultHint);

                password.requestFocus();
                InputMethodManager imm = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(password, 0);
                break;
            case CoreServiceMSG.MSG_LOGIN_FREEZE:
                // 如果被冻结，提示用户已经被冻结
                activity.removeDialog(DIALOG_LOGIN_CODE);
                activity.showDialog(DIALOG_LOGIN_FREEZE);
                break;
            case CoreServiceMSG.MSG_LOGIN_SUCCESS:
                info = (UserInfo) msg.obj;
                if (info.isFindPassword) {
                    Intent intent = new Intent(activity,
                            SettingsChangePasswordActivity.class);
                    intent.putExtra(SettingsChangePasswordActivity.ACCOUNT,
                            info.name);
                    activity.startActivityForResult(intent,
                            LoginActivity.FINDPASSWORD_REQUEST_CODE);
                } else {
                    Intent intentLogin = new Intent(activity, MainActivity.class);
                    activity.setResult(Activity.RESULT_OK);

                    activity.startActivity(intentLogin);
                    activity.finish();
                }
                activity.removeDialog(DIALOG_LOGIN_CODE);
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME:
            case CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IMSI_BIND:
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_UNBIND:
                info = (UserInfo) msg.obj;
                // 现在没有绑定的手机号，不允许登录，去掉保存用户信息，自动登录 --sivan.lv
                // if (null != info) {
                // CommonPreferences.saveLoginStatus(info);
                // }
                activity.removeDialog(DIALOG_LOGIN_CODE);
                activity.showDialog(msg.what);
                break;
            case CoreServiceMSG.MSG_LOGIN_ACCOUNT_ERROR:
                activity.removeDialog(DIALOG_REGISTER_CODE);
                activity.showToast(R.string.login_account_error);
                break;
            case CoreServiceMSG.MSG_FORGETPWD_SEND_SMS_START:
                SLog.d(TAG, "send sms start");
                long four = DateUtils.SECOND_IN_MILLIS * 40;
                progressDialog = new ProgressDialog(activity);
                progressDialog.setMessage(activity
                        .getString(R.string.forget_password_reset_waiting));
                progressDialog.setCancelable(true);
                progressDialog.show();
                r = new Runnable() {
                    @Override
                    public void run() {
                        if (null != activity && !activity.isFinishing()) {
                            if (null != progressDialog) {
                                SLog.d(TAG, "dimisss progressDialog");
                                progressDialog.dismiss();
                            }
                        }
                    }
                };
                // 设置一个定时器，半个小时后调用run方法
                timeHhander.postDelayed(r, four);
                break;
            case CoreServiceMSG.MSG_CHATMSG_SMSMSG_SEND_END:
                // 发送短信结束
                int status = (Integer) msg.obj;
                if (isFindPassword()) {
                    switch (status) {
                        case MessagesColumns.STATUS_SUCCESS:
                            SLog.d(TAG, "send success");
                            CommonPreferences.saveFindPasswordResult(true); // 发送短信成功
                            break;
                        case MessagesColumns.STATUS_FAILED:
                            SLog.d(TAG, "send fail");
                            CommonPreferences.saveFindPasswordResult(false); // 发送短信失败
                            break;
                    }
                } else {
                    if (status == MessagesColumns.STATUS_FAILED) {
                        activity.showDialog(MessagesColumns.STATUS_FAILED);
                        activity.removeDialog(DIALOG_REGISTER_CODE);
                        CoreService.getInstance().unregisterObserverForCommonSMS();
                    }
                }
                break;
            case CoreServiceMSG.MSG_REGISTER_SUCESS: // 激活成功
                activity.removeDialog(DIALOG_REGISTER_CODE);
                if (null != info && !finished) {
                    CommonPreferences.saveLoginStatus(info);
                    Intent intent = new Intent(activity, SetNickNameActivity.class);
                    intent.putExtra(SetNickNameActivity.INTENT_PARAM, info);
                    activity.startActivity(intent);
                    activity.setResult(BaseActivity.RESULT_OK, new Intent());
                    activity.finish();
                }
                break;
            case CoreServiceMSG.MSG_BIND_SUCESS:
                if (info != null) {
                    activity.removeDialog(DIALOG_BIND_CODE);
                    Intent intentBindSucess = new Intent(activity, MainActivity.class);
                    intentBindSucess.putExtra(SetNickNameActivity.INTENT_PARAM, info);
                    activity.startActivity(intentBindSucess);
                    CommonPreferences.saveLoginStatus(info);
                    activity.setResult(BaseActivity.RESULT_OK, new Intent());
                    activity.finish();
                }
                break;
            case CoreServiceMSG.MSG_BIND_FAILURE:
                // 绑定失败提示
                info = (UserInfo) msg.obj;
                activity.removeDialog(DIALOG_BIND_CODE);
                activity.showToast(info.resultHint);
                finished = true;
                break;
            case CoreServiceMSG.MSG_REBIND_SUCESS:
                if (info != null) {
                    activity.removeDialog(DIALOG_REBIND_CODE);
                    Intent intentRebindSucess = new Intent(activity, MainActivity.class);
                    intentRebindSucess.putExtra(SetNickNameActivity.INTENT_PARAM, info);
                    activity.startActivity(intentRebindSucess);
                    CommonPreferences.saveLoginStatus(info);
                    activity.setResult(BaseActivity.RESULT_OK, new Intent());
                    activity.finish();
                }
                break;
            case CoreServiceMSG.MSG_SMSMSG_RECEIVE_COMMON:
                if (activity.isFinishing()) {
                    break;
                }
                // 获取短信内容
                String findpwdFailed = activity.getString(R.string.sms_findpwd_failed);
                String findpwdFailed1 = activity.getString(R.string.sms_findpwd_failed_1);
                String findpwdSuccess = activity.getString(R.string.sms_findpwd_success);

                String splitFlag = activity.getString(R.string.sms_split_flag);
                @SuppressWarnings("unchecked")
                ArrayList<String> contents = (ArrayList<String>) msg.obj;
                for (String content : contents) {
                    if (content.contains(findpwdFailed) && content.contains(findpwdFailed1)) {
                        if (null != progressDialog) {
                            progressDialog.dismiss();
                            if (null != r) {
                                timeHhander.removeCallbacks(r);
                            }
                        }

                        ((LoginActivity) activity).unregisterObserverForPassword();
                        split.clear();
                        split.add(content.substring(0,
                                content.indexOf(splitFlag)));
                        activity.showDialog(LoginActivity.RECEIVE_FAIL_SMS);
                        CommonPreferences.saveFindPasswordSuccess(true);
                        break;
                    } else if (content.contains(findpwdSuccess)) {
                        if (null != progressDialog) {
                            progressDialog.dismiss();
                            if (null != r) {
                                timeHhander.removeCallbacks(r);
                            }
                        }

                        ((LoginActivity) activity).unregisterObserverForPassword();
                        CommonPreferences.saveFindPasswordSuccess(true);
                        split = RegexUtil.getSMSContent(content);
                        activity.showDialog(LoginActivity.RECEIVE_SUCCESS_SMS);
                    }
                    break;
                }
                break;
            case CoreServiceMSG.MSG_FORGETPWD_NET_ERROR:
                // network_error
                if (activity instanceof LoginActivity) {
                    ((LoginActivity) activity).unregisterObserverForPassword();
                    activity.findViewById(R.id.login_find_password).setClickable(true);
                    activity.showToast(R.string.network_timeout);
                }
                break;
            case CoreServiceMSG.MSG_FORGETPWD_ISBOUND:
                NetBindResponse isBindResp = (NetBindResponse) msg.obj;
                mobile = isBindResp.getMobile(); // 绑定的手机
                activity.showDialog(CoreServiceMSG.MSG_FORGETPWD_ISBOUND);
                break;
            case CoreServiceMSG.MSG_FORGETPWD_UNBOUND:
                activity.showDialog(CoreServiceMSG.MSG_FORGETPWD_UNBOUND);
                break;
            case CoreServiceMSG.MSG_FORGETPWD_ERROR:
                ((LoginActivity) activity).unregisterObserverForPassword();
                String resultHit = null;
                if (msg.obj instanceof NetBindResponse) {
                    NetBindResponse bindResp = (NetBindResponse) msg.obj;
                    if (bindResp.getResultCode() == -1) {
                        bindResp.setResult(Constants.NET_ERROR, bindResp.getResultHint());
                    }
                    resultHit = bindResp.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x"
                            + StringUtil.autoFixZero(bindResp.getResultCode()) + "]";
                } else if (msg.obj instanceof NetForgetPwdResponse) {
                    NetForgetPwdResponse forgetResp = (NetForgetPwdResponse) msg.obj;
                    if (forgetResp.getResultCode() == -1) {
                        forgetResp.setResult(Constants.NET_ERROR, forgetResp.getResultHint());
                    }
                    resultHit = forgetResp.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x"
                            + StringUtil.autoFixZero(forgetResp.getResultCode()) + "]";
                }
                activity.showToast(resultHit);
                break;
            case ENABLE_BUTTON:
                activity.findViewById(buttonId).setEnabled(true);
                break;
        }
    }

    public UserInfo getUserInfo() {
        return info;
    }

    /**
     * @return the mobile
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * @return the findPassword
     */
    public boolean isFindPassword() {
        return findPassword;
    }

    /**
     * @param findPassword the findPassword to set
     */
    public void setFindPassword(boolean findPassword) {
        this.findPassword = findPassword;
    }

    /**
     * @return the split
     */
    public ArrayList<String> getSplit() {
        return split;
    }

    /**
     * @param finished the finished to set
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setButtonId(int buttonId) {
        this.buttonId = buttonId;
    }
}
