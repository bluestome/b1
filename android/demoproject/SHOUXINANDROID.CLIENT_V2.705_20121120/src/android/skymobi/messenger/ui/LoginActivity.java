
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.database.observer.SMSObserver;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.LoginAction;
import android.skymobi.messenger.ui.handler.LoginHandler;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.skymobi.android.sx.codec.util.SysUtils;

import java.util.ArrayList;

public class LoginActivity extends TopActivity {

    private static final int DIALOG_REGISTER_CODE = LoginHandler.DIALOG_REGISTER_CODE;
    private static final int DIALOG_LOGIN_CODE = LoginHandler.DIALOG_LOGIN_CODE;
    private static final int DIALOG_LOGIN_OTHER_PHONE = LoginHandler.DIALOG_LOGIN_OTHER_PHONE;
    private static final int DIALOG_REBIND_CODE = LoginHandler.DIALOG_REBIND_CODE; // 换绑定
    private static final int DIALOG_BIND_CODE = LoginHandler.DIALOG_BIND_CODE; // 绑定
    private static final int DIALOG_FREEZE_CODE = LoginHandler.DIALOG_LOGIN_FREEZE;// 冻结
    public static final int RECEIVE_SUCCESS_SMS = 10;
    public static final int RECEIVE_FAIL_SMS = 11;
    public static final int FINDPASSWORD_REQUEST_CODE = 20;

    public static final int BUTTON_REGISTER = 1;

    private LoginAction action;
    private Dialog watiDialog = null;

    private final LoginHandler handler = new LoginHandler(this);
    protected ContentObserver smsObserver = null;

    private Button mRegisterBtn;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        init();
        initTopBar();
    }

    private void setUsername() {
        EditText username = (EditText) findViewById(R.id.login_username_editor);
        username.requestFocus();
        try {
            username.setText(CommonPreferences.getLastLoginName());
        } catch (Exception e) {
        }
    }

    /**
     * 初始化
     */
    private void init() {
        action = new LoginAction(this);

        // 登陆
        final Button login = (Button) findViewById(R.id.login_login_btn);
        login.setOnClickListener(action);

        // 密码框
        EditText password = (EditText) findViewById(R.id.login_password_editor);
        password.setOnClickListener(action);
        password.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_DOWN) {
                    login.performClick();
                    return true;
                }
                return false;

            }
        });

        // 找回密码
        TextView findPassword = (TextView) findViewById(R.id.login_find_password);
        findPassword.setOnClickListener(action);
        setUsername();

    }

    @Override
    public void notifyObserver(int what, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, obj));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        switch (id) {
            case DIALOG_REGISTER_CODE:
                dialogBuilder.setMessage(R.string.register_waitting);
                break;
            case DIALOG_REBIND_CODE:
                dialogBuilder.setMessage(R.string.rebind_waitting);
                break;
            case DIALOG_BIND_CODE:
                dialogBuilder.setMessage(R.string.bind_waitting);
                break;
            case DIALOG_LOGIN_CODE:
                dialogBuilder.setMessage(R.string.login_logining);
                break;
             // 2012-11-20 @bluestome.zhang QC_BUG #145
            case CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST:
                // 用户名不存在
                dialogBuilder.setMessage(R.string.login_account_error);
                dialogBuilder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IS_PHONE:
                // 提示用户使用该手机号码的手机进行注册
                dialogBuilder.setMessage(R.string.login_username_unbind_is_phone);
                dialogBuilder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST_IMSI_UNBIND:
                dialogBuilder.setMessage(R.string.login_register_by_username);
                dialogBuilder.setPositiveButton(R.string.login_free_register,
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                /*
                                 * showDialog(DIALOG_REGISTER_CODE);
                                 * CoreService.
                                 * getInstance().registerObserverForCommonSMS();
                                 * String username =
                                 * getEditorText(R.id.login_username_editor);
                                 * String pwd =
                                 * getEditorText(R.id.login_password_editor);
                                 * mService.getCommonModule().register(username,
                                 * pwd);
                                 */
                                Intent intent = new Intent(LoginActivity.this,
                                        RegisterActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME:

                final String[] result = handler.getUserInfo().resultHint.split(Constants.separator);
                dialogBuilder.setMessage(getString(R.string.login_imsi_not_same, new Object[] {
                        changeMobile(result[3]), result[2]
                }));
                dialogBuilder.setPositiveButton(R.string.rebound, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CoreService.getInstance().registerObserverForCommonSMS();
                        sendActivateSMSMsg(result[0], result[1]);
                        // CommonPreferences.saveChangeBindSendSMSTime(System.currentTimeMillis());
                        CommonPreferences.saveLoginStatus(handler.getUserInfo());
                        SettingsPreferences.saveBindInfo(SettingsPreferences.BIND_OTHER, "绑定其他手机");
                        MainApp.setLoggedIn(true);
                        dialog.cancel();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        setResult(RESULT_OK);
                        finish();
                    }
                }).setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainApp.setLoggedIn(false);
                        dialog.cancel();
                        // 目前处理，非本机绑定账号,取消换绑不能进入 anson.yang@20121010
                        // CommonPreferences.saveLoginStatus(handler.getUserInfo());
                        // startActivity(new Intent(LoginActivity.this,
                        // MainActivity.class));
                        // setResult(RESULT_OK);
                        // finish();
                        // 取消换绑,密码置空,防止自动登录 anson.yang@20121012
                        CommonPreferences.setLogoutedStatus(true);
                        CommonPreferences.clearPassword();
                        showToast(R.string.login_username_unbind_imsi_bind_toast);
                    }
                });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IMSI_BIND:
                final String[] result2 = handler.getUserInfo().resultHint
                        .split(Constants.separator);
                dialogBuilder.setMessage(getString(R.string.login_username_unbind_imsi_bind,
                        new Object[] {
                            result2[2]
                        }));
                dialogBuilder.setPositiveButton(R.string.bound, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CoreService.getInstance().registerObserverForCommonSMS();
                        sendActivateSMSMsg(result2[0], result2[1]);
                        dialog.cancel();
                        showDialog(DIALOG_REBIND_CODE);
                    }
                }).setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showToast(R.string.login_username_unbind_imsi_bind_toast);
                    }
                });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IMSI_UNBIND:
                final String[] result3 = handler.getUserInfo().resultHint
                        .split(Constants.separator);
                dialogBuilder.setMessage(getString(R.string.un_bound_tip));
                /*
                 * 按钮位置调换20121008@hzc
                 */
                dialogBuilder.setPositiveButton(R.string.cancel,
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                showToast(R.string.login_username_unbind_imsi_bind_toast);
                            }
                        }).setNegativeButton(R.string.bound, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CoreService.getInstance().registerObserverForCommonSMS();
                        handler.setFinished(true);
                        sendActivateSMSMsg(result3[0], result3[1]);
                        dialog.cancel();
                        showDialog(DIALOG_BIND_CODE);
                        setTimeout(getString(R.string.login_bind_error),
                                CoreServiceMSG.MSG_BIND_FAILURE);
                    }
                });

                /*
                 * dialogBuilder.setPositiveButton(R.string.bound, new
                 * Dialog.OnClickListener() {
                 * @Override public void onClick(DialogInterface dialog, int
                 * which) {
                 * CoreService.getInstance().registerObserverForCommonSMS();
                 * handler.setFinished(true); sendActivateSMSMsg(result3[0],
                 * result3[1]); dialog.cancel(); showDialog(DIALOG_BIND_CODE);
                 * setTimeout(getString(R.string.login_bind_error),
                 * CoreServiceMSG.MSG_BIND_FAILURE); }
                 * }).setNegativeButton(R.string.cancel, new
                 * Dialog.OnClickListener() {
                 * @Override public void onClick(DialogInterface dialog, int
                 * which) { dialog.cancel();
                 * showToast(R.string.login_username_unbind_imsi_bind_toast); }
                 * });
                 */
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST_IMSI_BIND:
                final String[] result5 = handler.getUserInfo().resultHint
                        .split(Constants.separator);
                dialogBuilder.setMessage(getString(R.string.login_account_error_imsi_bind,
                        result5[0], result5[1]));
                dialogBuilder.setPositiveButton(R.string.login_i_know,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_UNBIND:
                final String[] result4 = handler.getUserInfo().resultHint
                        .split(Constants.separator);
                dialogBuilder.setMessage(getString(R.string.login_username_bind_imsi_unbind,
                        result4[2]));
                dialogBuilder.setPositiveButton(R.string.rebound, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CoreService.getInstance().registerObserverForCommonSMS();
                        sendActivateSMSMsg(result4[0], result4[1]);
                        CommonPreferences.saveLoginStatus(handler.getUserInfo());
                        dialog.cancel();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        setResult(RESULT_OK);
                        finish();
                    }
                }).setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showToast(R.string.login_username_unbind_imsi_bind_toast);
                        // CommonPreferences.saveLoginStatus(handler.getUserInfo());
                        // startActivity(new Intent(LoginActivity.this,
                        // MainActivity.class));
                        // setResult(RESULT_OK);
                        // finish();
                    }
                });
                break;
            case CoreServiceMSG.MSG_LOGIN_PASSWORD_NOT_MACTH:
                dialogBuilder.setMessage(R.string.login_account_error_password_notmatch);
                dialogBuilder.setPositiveButton(R.string.login_i_know,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_IS_PHONENUMBER:
                dialogBuilder.setMessage(R.string.login_account_error);
                dialogBuilder.setPositiveButton(R.string.login_i_know,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case MessagesColumns.STATUS_FAILED:
                dialogBuilder.setMessage(R.string.register_send_sms_failed);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                break;
            case CoreServiceMSG.MSG_REGISTER_ISBIND:
                try {
                    dialogBuilder.setMessage(handler.getUserInfo().resultHint);
                    dialogBuilder.setPositiveButton(R.string.login_i_know,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DIALOG_LOGIN_OTHER_PHONE:
                dialogBuilder.setMessage(R.string.login_use_other_phone);
                dialogBuilder.setPositiveButton(R.string.login_i_know,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case CoreServiceMSG.MSG_FORGETPWD_ISBOUND:
                dialogBuilder.setMessage(R.string.forget_password_isbound);
                dialogBuilder.setPositiveButton(R.string.ok, new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        String newpwd = DateUtil.getRandomPwd();
                        handler.setFindPassword(true);
                        registerObserverForPassword();
                        mService.getCommonModule().forgetPwd(handler.getMobile(), newpwd);
                    }
                }).setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                break;
            case CoreServiceMSG.MSG_FORGETPWD_UNBOUND:
                dialogBuilder.setMessage(R.string.forget_password_unbound);
                dialogBuilder.setPositiveButton(R.string.login_i_know,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case RECEIVE_SUCCESS_SMS:
                ArrayList<String> split = handler.getSplit();
                String content = getString(R.string.forget_password_success);
                content = String.format(content, split.get(0), split.get(2), split.get(1));
                dialogBuilder.setMessage(content);
                dialogBuilder.setPositiveButton(R.string.login_now, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 立即登录
                        showDialog(LoginActivity.DIALOG_LOGIN_CODE);
                        // 使用手机号登录
                        mService.getCommonModule().login(handler.getSplit().get(2),
                                SysUtils.pwdEncrypt(handler.getSplit().get(1)));
                        dialog.cancel();
                    }
                }).setNegativeButton(R.string.settings_change_password,
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                // 修改密码
                                showDialog(LoginActivity.DIALOG_LOGIN_CODE);
                                mService.getCommonModule().login(handler.getSplit().get(2),
                                        SysUtils.pwdEncrypt(handler.getSplit().get(1)), true);
                            }
                        });
                break;
            case RECEIVE_FAIL_SMS:
                dialogBuilder.setMessage(handler.getSplit().get(0));
                dialogBuilder.setPositiveButton(R.string.login_i_know,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case DIALOG_FREEZE_CODE:
                dialogBuilder.setMessage(R.string.login_error_freeze);
                dialogBuilder.setPositiveButton(R.string.login_i_know,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).setNegativeButton(R.string.login_error_freeze_call,
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Uri uri = Uri.parse("tel:" + "0571-87966766");
                                Intent it = new Intent(Intent.ACTION_CALL, uri);
                                startActivity(it);
                            }
                        });
                break;
        }
        dialog = dialogBuilder.create();
        dialog.setCancelable(true);
        if (DIALOG_REGISTER_CODE == id || DIALOG_BIND_CODE == id || DIALOG_REBIND_CODE == id) {
            watiDialog = dialog;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case RECEIVE_SUCCESS_SMS:
                ArrayList<String> split = handler.getSplit();
                String content = getString(R.string.forget_password_success);
                content = String.format(content, split.get(0), split.get(2), split.get(1));
                ((AlertDialog) dialog).setMessage(content);
                break;
            default:
                super.onPrepareDialog(id, dialog);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FINDPASSWORD_REQUEST_CODE: // 启动到设置界面
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(MainActivity.TAG_EXTRA, R.id.main_tab_settings);
                startActivity(intent);
                this.finish();
                break;
        }
    }

    private void registerObserverForPassword() {
        if (smsObserver == null) {
            smsObserver = new SMSObserver(handler, mService);
        }
        getContentResolver().registerContentObserver(Uri.parse("content://sms"),
                true, smsObserver);
    }

    public void unregisterObserverForPassword() {
        if (null != smsObserver) {
            getContentResolver().unregisterContentObserver(smsObserver);
        }
    }

    /**
     * 
     */
    public void setTimeout(final String notice, final int code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 超时时将激活等待中的提示框去掉
                // 构造消息对象，用户传送给Handler中处理。
                UserInfo info = new UserInfo();
                info.resultHint = notice;
                Message msg = new Message();
                msg.obj = info;
                msg.what = code;
                SystemClock.sleep(40 * 1000L);
                if (watiDialog != null && watiDialog.isShowing()) {
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 重置完成状态
     */
    public void reset() {
        // findViewById(R.id.login_register_btn).setEnabled(false);
        // handler.setButtonId(R.id.login_register_btn);
        mRegisterBtn.setEnabled(false);
        handler.setButtonId(BUTTON_REGISTER);
        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                handler.sendEmptyMessage(LoginHandler.ENABLE_BUTTON);
            }
        }.start();
        handler.setFinished(false);
    }

    /**
     * 登录超时
     */
    public void setLoginTimeout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 超时时将激活等待中的提示框去掉
                // 构造消息对象，用户传送给Handler中处理。
                UserInfo info = new UserInfo();
                info.resultHint = getString(R.string.login_timeout);
                Message msg = new Message();
                msg.obj = info;
                msg.what = CoreServiceMSG.MSG_FAILED;
                SystemClock.sleep(40 * 1000L);
                if (watiDialog != null && watiDialog.isShowing()) {
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    @Override
    public void initTopBar() {
        mRegisterBtn = (Button) setTopBarButton(TOPBAR_BUTTON_RIGHTII,
                R.string.register_free_register,
                BUTTON_REGISTER,
                action);
        setTopBarTitle(R.string.register_login1);
    }
}
