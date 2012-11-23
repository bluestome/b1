
package android.skymobi.messenger.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.fastchat.FastChatBU;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.FastChatAction;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.MediaHelper;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.dialog.DialogTool;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.skymobi.messenger.widget.fastchat.MicrophoneView;
import android.skymobi.messenger.widget.fastchat.BackableScrollView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

/**
 * @ClassName: fastchatActivity
 * @Description:
 * @author Sean.Xie
 * @date 2012-10-17 上午9:38:53
 */
public class FastChatActivity extends TopActivity {

    public static final int DIALOG_MATCHING = 1; // 匹配对话框
    public static final int DIALOG_LEAVE = 2; // 离开对方框
    public static final int DIALOG_NET_ERROR = 3;// 联网失败
    public static final int DIALOG_MATCHFAIL = 4; // 匹配失败

    // define limit low record sound time(ms)
    public static final int LIMIT_LOW_RECORD_TIME = 1000;
    // define limit high record sound time(ms)
    public static final int LIMIT_HIGH_RECORD_TIME = 60000;

    // 传话筒
    private MicrophoneView microphoneView = null;
    // 消息列表
    private BackableScrollView msgListview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fastchat);
        try {
            mService.getFastChatModule();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
        init();
        initMicrophone();
        initTopBar();
        initView();
        if (CommonPreferences.isFastChatUsed()) {
            // 如果之前没有聊天记录并且没有匹配的用户时 才进行连接
            if (MainApp.getFastChatCache().getMatchedSkyid() < 0
                    && MainApp.getFastChatCache().getChatMsg().size() == 0) {
                fastChatBu.applyFastChat(SettingsPreferences.getSex());
            } else {
                microphoneView.movePosition(MicrophoneView.FALL_INVIEW, false);
                msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                msgListview.scrollDown();
            }
        }
    }

    // 快聊bizunit的初始化和action的初始化
    private FastChatBU fastChatBu = null;
    private FastChatAction fastChatAction = null;

    private void init() {
        // 初始化BizUnit
        fastChatBu = new FastChatBU(mHandler);
        fastChatAction = new FastChatAction(this);
        fastChatAction.setFastChatBu(fastChatBu, mHandler);
    }

    /**
     * s
     */
    private void initView() {
        // 设置录音面板的Touch事件响应
        findViewById(R.id.fastchat_guide_start).setOnClickListener(fastChatAction);
        // 消息列表
        msgListview = (BackableScrollView) findViewById(R.id.fastchat_scrollview);
        msgListview.setClickListener(fastChatAction);
    }

    /**
     * 初始化传话筒
     */
    private void initMicrophone() {
        LinearLayout microphoneLayout = (LinearLayout) findViewById(R.id.fastchat_microphone);
        microphoneView = new MicrophoneView(this);
        microphoneView.setOnFinishListener(fastChatAction);
        microphoneView.setOnTouchListener(fastChatAction);
        microphoneLayout.addView(microphoneView, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fastChatAction.registerSensor();
        updateTitleBtn();
        mService.getFastChatModule().cancelFastChatNotification();
        MediaHelper.getInstance().setLister(fastChatAction);
        MainApp.getFastChatCache().setLeave(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fastChatAction.unregisterSensor();
        MediaHelper.getInstance().release();
        MainApp.getFastChatCache().setLeave(true);
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.fastchat_title);
        updateTitleBtn();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_MATCHING:
                dialog = DialogTool.createNormalProgressDialog(this,
                        android.R.drawable.ic_dialog_info, R.string.tip,
                        getResources().getString(R.string.fastchat_connect_title));
                break;
            case DIALOG_LEAVE:
                // 确定“退出”
                DialogInterface.OnClickListener LeftListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                fastChatBu.leaveFastChat(MainApp.getFastChatCache()
                                        .getMatchedSkyid());
                                MainApp.getFastChatCache().clearAll();
                                finish();
                            }
                        };
                // 取消“退出”
                DialogInterface.OnClickListener RightListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        };
                dialog = DialogTool.createNormalDialog(this,
                        android.R.drawable.ic_dialog_info, R.string.tip,
                        getResources().getString(R.string.fastchat_leave),
                        R.string.ok, LeftListener,
                        R.string.cancel, RightListener, true);
                break;
            case DIALOG_NET_ERROR:
                dialog = DialogTool.createNormalDialog(this,
                        android.R.drawable.ic_dialog_info, R.string.tip,
                        getResources().getString(R.string.fastchat_connect_fail),
                        R.string.ok, null);
                break;
            case DIALOG_MATCHFAIL:
                dialog = DialogTool.createNormalDialog(this,
                        android.R.drawable.ic_dialog_info, R.string.tip,
                        getResources().getString(R.string.fastchat_match_fail),
                        R.string.ok, null);
                break;
            default:
                break;
        }
        return dialog;
    }

    private final Runnable rRemoveWaitDialog = new Runnable() {

        @Override
        public void run() {
            // SLog.d("fastchat", "rRemoveWaitDialog ......");
            removeDialog(DIALOG_MATCHING);
        }
    };
    protected Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_FASTCHAT_APPLY_BEGIN: {
                    // showToast("正在匹配");
                    removeCallbacks(rRemoveWaitDialog);
                    showDialog(DIALOG_MATCHING);
                    // 最多让用户等30s时间，自动关闭等待对方框
                    postDelayed(rRemoveWaitDialog, 45 * 1000L);
                    // 清除所有聊天记录
                    MainApp.getFastChatCache().clearAll();
                    updateTitleBtn();
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                    microphoneView.movePosition(MicrophoneView.FALL_INVISIBLE);
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_APPLY_FAIL: {
                    updateTitleBtn();
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                    removeDialog(DIALOG_MATCHING);
                    microphoneView.movePosition(MicrophoneView.FALL_OUTVIEW_MATCHFAIL);
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_APPLY_REQ_FAIL: {
                    updateTitleBtn();
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                    removeDialog(DIALOG_MATCHING);
                    microphoneView.movePosition(MicrophoneView.FALL_OUTVIEW_NET_ERROR);
                }
                    break;
                case CoreServiceMSG.MSG_NET_ERROR: {
                    updateTitleBtn();
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                    removeDialog(DIALOG_MATCHING);
                    microphoneView.movePosition(MicrophoneView.FALL_OUTVIEW_NET_ERROR);
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_APPLY_SUCCESS: {
                    removeDialog(DIALOG_MATCHING);
                    removeDialog(DIALOG_MATCHFAIL);
                    removeDialog(DIALOG_NET_ERROR);
                    updateTitleBtn();
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                    showToast(R.string.fastchat_match_success);
                    microphoneView.movePosition(MicrophoneView.FALL_INVIEW);
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_ALREADY_LEAVE: {
                    updateTitleBtn();
                    ToastTool.showWithPicAtCenter(FastChatActivity.this,
                            R.drawable.fastchat_already_leave,
                            R.string.fastchat_already_leave, false, true);
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_LEAVE_SUCCESS: {
                    SLog.d("fastchat", "fastchat leave success");
                    // showToast("离开成功");
                    // findViewById(R.id.fastchat_recorder).setVisibility(View.GONE);
                    // MainApp.getFastChatCache().clearAll();
                    // updateTitleBtn();
                    // microphoneView.movePosition(MicrophoneView.FALL_INVISIBLE);
                    // msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_SENDVOICE_BEGIN: {
                    // showToast("语音发送开始");
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                    msgListview.scrollDown();
                }
                    break;

                case CoreServiceMSG.MSG_FASTCHAT_SENDVOICE_END: {
                    // showToast("语音发送结束");
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_RESENDVOICE_BEGIN: {
                    // showToast("重新发送语音开始");
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_RESENDVOICE_END: {
                    // showToast("重新发送语音结束");
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                }
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_RECEIVEVOICE: {
                    // showToast("收到语音消息");
                    msgListview.refreshList(MainApp.getFastChatCache().getChatMsg());
                    msgListview.scrollDown();
                }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    // 获取话筒
    public MicrophoneView getMicrophoneView() {
        return microphoneView;
    }

    // 获取消息列表
    public BackableScrollView getMyScrollView() {
        return msgListview;
    }

    // 更新顶部Title的右边按钮名称
    public void updateTitleBtn() {
        if (!CommonPreferences.isFastChatUsed()) {
            setTopBarButton(TOPBAR_BUTTON_RIGHTII, R.string.fastchat_btn_end, fastChatAction);
            setTopBarBtnVisible(TOPBAR_BUTTON_RIGHTII, false);
            findViewById(R.id.fastchat_guide).setVisibility(View.VISIBLE);
            findViewById(R.id.fastchat_guide).setOnClickListener(fastChatAction);
        } else if (MainApp.getFastChatCache().getMatchedSkyid() < 0) {
            setTopBarBtnVisible(TOPBAR_BUTTON_RIGHTII, true);
            setTopBarButton(TOPBAR_BUTTON_RIGHTII, R.string.fastchat_btn_refresh, fastChatAction);
            microphoneView.setDisableStatus(true);
        } else {
            setTopBarBtnVisible(TOPBAR_BUTTON_RIGHTII, true);
            setTopBarButton(TOPBAR_BUTTON_RIGHTII, R.string.fastchat_btn_end, fastChatAction);
            microphoneView.setDisableStatus(false);
        }
    }

    // 重发处理
    private static int MENU_RESEND = 1; // "重发"菜单
    private int selPos = -1; // 重发选择的位置

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        selPos = (Integer) v.getTag();
        if (selPos >= 0
                && MainApp.getFastChatCache().getChatMsg().get(selPos).getStatus() == MessagesColumns.STATUS_FAILED)
            menu.add(0, MENU_RESEND, 0, R.string.fastchat_menu_resend);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_RESEND && selPos >= 0) {
            SLog.d("fastchat", "selPos = " + selPos);
            fastChatBu.reSendFastChatVoice(selPos);
        }
        return super.onContextItemSelected(item);
    }

}
