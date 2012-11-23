
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.auth.LoginBU;
import android.skymobi.messenger.bizunit.auth.RegisterBU;
import android.skymobi.messenger.bizunit.lcs.LcsBU;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.dialog.DialogTool;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class FindFriendActivity extends TopActivity implements
        OnClickListener {

    private static final String TAG = FindFriendActivity.class.getSimpleName();
    public static final int DIALOG_FINDFRIEND_CODE = 0;
    public static final int DIALOG_FINDFRIEND_SHARELBS = 1;
    private final static int DIALOG_CHECK_BIND = 2;
    private final static int DIALOG_BIND_ING = 3;

    public static final int MENU_QUIT = Menu.FIRST + 1; // 退出

    LoginBU loginBU = null;
    LcsBU lcsBU = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findfriend);
        loginBU = new LoginBU(null);
        lcsBU = MainApp.i().getLcsBU();
        try {
            mService.getNearUserModule();

        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
        initTopBar();
        init();
    }

    /**
     * 
     */
    private void init() {

        findViewById(R.id.findfriend_nearby).setOnClickListener(this);

        findViewById(R.id.findfriend_fastchat).setOnClickListener(this);

        findViewById(R.id.recommended_friends).setOnClickListener(this);

        findViewById(R.id.search_friend).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.findfriend_nearby: {
                Intent intent = new Intent(this, NearUserActivity.class);
                startActivity(intent);
                lcsBU.getLcsDA()
                        .saveClickLbsCount(lcsBU.getLcsDA().getClickLbsCount() + 1);
                /*
                 * boolean isShareLBS = SettingsPreferences.getShareLBS(); if
                 * (isShareLBS) { Intent intent = new Intent(this,
                 * NearUserActivity.class); startActivity(intent); } else {
                 * showDialog(DIALOG_FINDFRIEND_SHARELBS); }
                 */
            }
                break;
            case R.id.findfriend_fastchat: {
                Intent intent = new Intent(this, FastChatActivity.class);
                startActivity(intent);
                lcsBU.getLcsDA().saveClickFastChatCount(
                        lcsBU.getLcsDA().getClickFastChatCount() + 1);

            }
                break;
            case R.id.recommended_friends:
                // 使用该功能前判断是否绑定
                boolean isBind = loginBU.isBindLocal();
                if (!isBind) {
                    RegisterBU ao = new RegisterBU(null);
                    if (ao.isCanSendBind()) {
                        showDialog(DIALOG_CHECK_BIND);
                        return;
                    } else {
                        showDialog(DIALOG_BIND_ING);
                        return;
                    }
                } else {
                    Intent intent = new Intent(this, FriendListActivity.class);
                    startActivity(intent);
                }

                break;
            case R.id.search_friend:
                Intent intent = new Intent(this, SearchFriendActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private ProgressDialog bindingDialog = null;

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        switch (id) {
            case DIALOG_FINDFRIEND_CODE:
                dialogBuilder.setMessage(R.string.findfriend_later_more);
                dialogBuilder.setNegativeButton(R.string.iknow, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                break;
            case DIALOG_CHECK_BIND:
                dialogBuilder.setMessage(R.string.usefun_unbound_tip);
                dialogBuilder.setCancelable(false);
                /* 按钮调换，将“绑定按钮”放在右边* */
                dialogBuilder.setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.setNegativeButton(R.string.bound,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                RegisterBU ao = new RegisterBU(null);
                                bindingDialog = DialogTool.createNormalProgressDialog(
                                        FindFriendActivity.this,
                                            android.R.drawable.ic_dialog_info,
                                            R.string.tip,
                                            getString(R.string.bounding), true);
                                bindingDialog.show();

                                ao.sendBindSMS();
                                waitBind();
                            }
                        });
                break;
            case DIALOG_BIND_ING:
                dialogBuilder.setMessage(R.string.bounding_detail_tip);
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                break;
            /*
             * case DIALOG_FINDFRIEND_SHARELBS:
             * dialogBuilder.setMessage(R.string.nearuser_ask_for_share);
             * dialogBuilder.setPositiveButton(android.R.string.ok, new
             * DialogInterface.OnClickListener() {
             * @Override public void onClick(DialogInterface dialog, int which)
             * { dialog.cancel(); Intent intent = new
             * Intent(FindFriendActivity.this, SettingsPrivacyActivity.class);
             * startActivity(intent); }
             * }).setNegativeButton(android.R.string.cancel, new
             * DialogInterface.OnClickListener() {
             * @Override public void onClick(DialogInterface dialog, int which)
             * { dialog.cancel(); } }); break;
             */
            default:
                break;
        }

        dialog = dialogBuilder.create();
        dialog.setCancelable(false);
        return dialog;
    }

    public void waitBind() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!loginBU.isBindLocal()) {
                        if (bindingDialog == null
                                || (bindingDialog != null && !bindingDialog.isShowing())) {
                            return;
                        }
                        SystemClock.sleep(Constants.WAIT_BIND_STATUS_INTERVAL);
                        continue;
                    } else
                        break;
                }
                if (bindingDialog != null) {
                    bindingDialog.dismiss();
                    bindingDialog = null;
                }
                Intent intent = new Intent(FindFriendActivity.this, FriendListActivity.class);
                startActivity(intent);
            }
        }).start();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_QUIT, 0, R.string.quit).setIcon(R.drawable.menu_quit);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_QUIT:
                showQuitDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        switchToHome();
    }

    @Override
    public void initTopBar() {
        setTopBarTitle(R.string.findfriend_title);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFastChatUnreadCount();
        updateFastChatNewWidget();
    }

    // 更新未对快聊信息的条数
    private void updateFastChatUnreadCount() {
        TextView countTV = (TextView) findViewById(R.id.findfriend_fastchat_count);
        int count = MainApp.getFastChatCache().getUnreadVoiceCount();
        if (count > 0) {
            countTV.setVisibility(View.VISIBLE);
            countTV.setText(AndroidSysUtils.getUnreadCountStr(count));
        } else {
            countTV.setVisibility(View.GONE);
        }

    }

    // 更新new字样显示逻辑
    private void updateFastChatNewWidget() {
        TextView newWidget = (TextView) findViewById(R.id.findfriend_fastchat_new);
        if (lcsBU.getLcsDA().getClickFastChatCount() > 0) {
            newWidget.setVisibility(View.GONE);
        } else {
            newWidget.setVisibility(View.VISIBLE);
        }
    }

    // Handle message
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_FASTCHAT_RECEIVEVOICE:
                    updateFastChatUnreadCount();
                    break;
                default:
                    break;
            }
        }
    };
}
