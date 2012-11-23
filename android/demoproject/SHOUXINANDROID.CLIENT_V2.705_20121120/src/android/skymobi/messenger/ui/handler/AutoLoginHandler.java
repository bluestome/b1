
package android.skymobi.messenger.ui.handler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.LoginActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: LoginHandler
 * @author Sean.Xie
 * @date 2012-3-14 上午10:39:23
 */
public class AutoLoginHandler extends Handler {

    private final BaseActivity activity;

    private UserInfo info = null;
    private String messageForRegister = null;
    private final Map<Integer, Dialog> dialogs = new HashMap<Integer, Dialog>();

    private String smsTo = "";
    private String smsContent = "";

    public AutoLoginHandler(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CoreServiceMSG.MSG_LOGIN_NET_ERROR:
                MainApp.i().setOnline(false);
                // info = (UserInfo) msg.obj;
                // activity.showToast(info.resultHint);
                break;
            case CoreServiceMSG.MSG_LOGIN_FREEZE:
               // SLog.d("login", "autolongin hadler login freeze");
                MainApp.i().setOnline(false);
                CommonPreferences.setLogoutedStatus(true);
                activity.finish();
                activity.startActivity(new Intent(activity, LoginActivity.class));
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST:
            case CoreServiceMSG.MSG_LOGIN_PASSWORD_ERROR:
                MainApp.i().setOnline(false);
                info = (UserInfo) msg.obj;
                activity.showToast(info.resultHint);
                CommonPreferences.setLogoutedStatus(true);
                activity.finish();
                activity.startActivity(new Intent(activity, LoginActivity.class));
                break;
            case CoreServiceMSG.MSG_LOGIN_SUCCESS:
                MainApp.i().setOnline(true);
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME:
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_UNBIND:
                info = (UserInfo) msg.obj;
                messageForRegister = info.resultHint;
                showDialog(msg.what);
                break;
        }
    }

    /**
     * @param statusSendFail
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
     * @return
     */
    private Dialog createDialog(int code) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        switch (code) {
            case CoreServiceMSG.MSG_LOGIN_NET_ERROR:
                dialogBuilder.setMessage(R.string.autologin_net_error);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CoreService.getInstance().autoLogin();
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                break;
            case CoreServiceMSG.MSG_LOGIN_PASSWORD_ERROR:
                dialogBuilder.setMessage(R.string.autologin_password_error);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        dialog.dismiss();
                        activity.finish();
                    }
                });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME:
                final String[] result = messageForRegister.split(Constants.separator);
                dialogBuilder.setMessage(activity.getString(R.string.login_imsi_not_same,
                        new Object[] {
                                activity.changeMobile(result[3]), result[2]
                        }));
                dialogBuilder.setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        smsTo = result[0];
                        smsContent = result[1];
                        activity.sendActivateSMSMsg(smsTo, smsContent);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_UNBIND:
                final String[] result4 = messageForRegister.split(Constants.separator);
                dialogBuilder.setMessage(activity.getString(
                        R.string.login_username_bind_imsi_unbind,
                        result4[2]));
                dialogBuilder.setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        smsTo = result4[0];
                        smsContent = result4[1];
                        activity.sendActivateSMSMsg(smsTo, smsContent);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                break;
            case MessagesColumns.STATUS_FAILED:
                dialogBuilder.setMessage(R.string.register_send_sms_failed);
                dialogBuilder.setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                dialogBuilder.setNegativeButton(R.string.send_sms_again,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.sendActivateSMSMsg(smsTo, smsContent);
                                dialog.cancel();
                            }
                        });
                break;
        }
        return dialogBuilder.create();
    }

    public String getMessageForRegister() {
        return messageForRegister;
    }

}
