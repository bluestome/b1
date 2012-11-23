
package android.skymobi.messenger.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.logreport.SkymobiclickAgent;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.CommonPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * @ClassName: MainActivity
 * @Description: 主Activity，实现消息，联系人，找朋友，设置之间的切换
 * @author Michael.Pan
 * @date 2012-2-6 上午09:37:38
 */

public class MainActivity extends TabActivity implements Observer, OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAB_MESSAGE = "message";
    private static final String TAG_CONTACT = "contact";
    private static final String TAG_FINDFRIEND = "findfriend";
    private static final String TAG_SETTINGS = "settings";
    public static final String TAG_EXTRA = "extra_tag";

    private static final int PRIVATE_MSG_UPDATE_UNREAD = 0x0010;
    private static final int PRIVATE_MSG_DELAY_CHECK = 0x0011;

    private CoreService mService = null;
    private TextView mTextViewUnread = null;
    private View mContactsGuide = null;
    private Button mContactsGuideBtn = null;
    // Handle message
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_CHATMSG_SMSMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_TEXTMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_CARDMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_VOICEMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_MARKETMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_SYSTEMMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_FRIENDSMSG_RECEIVE:
                case CoreServiceMSG.MSG_THREADS_SYNC_END:
                case CoreServiceMSG.MSG_MESSAGES_SYNC_END:
                case CoreServiceMSG.MSG_THREADS_DELETE_END:
                    // 收到新消息;
                    checkUnread();
                    break;
                case PRIVATE_MSG_UPDATE_UNREAD:
                    int unreadCount = (Integer) msg.obj;
                    updateUnreadView(unreadCount);
                    break;
                case PRIVATE_MSG_DELAY_CHECK:
                    checkUnread();
                    break;
                case CoreServiceMSG.MSG_FASTCHAT_RECEIVEVOICE:
                    checkFastChatUnread();
                    break;
                default:
                    break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mService = CoreService.getInstance();
        setContentView(R.layout.main_tabs);
        int id = getIntent().getIntExtra(TAG_EXTRA, R.id.main_tab_message);
        initView(id);
        MainApp.i().addActivity(this);
        if (mService != null) {
            mService.registerCallBack(this);
        }
        SkymobiclickAgent.setSessionContinueMillis(60 * 1000 * 2);// 2分钟
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int id = intent.getIntExtra(TAG_EXTRA, R.id.main_tab_message);
        mRadioGroup.check(id);
    }

    private TabHost mMainTabHost = null;
    private RadioGroup mRadioGroup = null;

    private void initView(int id) {
        // 第一次使用联系人功能时
        initContactGuideView();
        // 未读会话条数
        mTextViewUnread = (TextView) findViewById(R.id.main_tab_msg_tip_count);

        mMainTabHost = getTabHost();
        mRadioGroup = (RadioGroup) findViewById(R.id.main_tab_group);

        mMainTabHost.addTab(mMainTabHost.newTabSpec(TAB_MESSAGE)
                .setIndicator(TAB_MESSAGE)
                .setContent(new Intent(this, MessageListActivity.class)));
        mMainTabHost.addTab(mMainTabHost.newTabSpec(TAG_CONTACT)
                .setIndicator(TAG_CONTACT)
                .setContent(new Intent(this, ContactsListActivity.class)));
        mMainTabHost.addTab(mMainTabHost.newTabSpec(TAG_FINDFRIEND)
                .setIndicator(TAG_FINDFRIEND)
                .setContent(new Intent(this, FindFriendActivity.class)));
        mMainTabHost.addTab(mMainTabHost.newTabSpec(TAG_SETTINGS)
                .setIndicator(TAG_SETTINGS)
                .setContent(new Intent(this, SettingsActivity.class)));
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.main_tab_message:
                        mMainTabHost.setCurrentTabByTag(TAB_MESSAGE);
                        break;
                    case R.id.main_tab_contact:
                        // 第一次使用联系人的功能时
                        if (isFirstContactsGuide() || (!isFirstContactsGuide() && !isSameVersion())) {
                            mContactsGuide.setVisibility(View.VISIBLE);
                        }
                        mMainTabHost.setCurrentTabByTag(TAG_CONTACT);
                        break;
                    case R.id.main_tab_findfriend:
                        mMainTabHost.setCurrentTabByTag(TAG_FINDFRIEND);
                        break;
                    case R.id.main_tab_settings:
                        /*
                         * // 剩余空间小于1M，提示 if
                         * (AndroidSysUtils.getAvailableStore() < 1024 * 1024) {
                         * Toast toast = Toast.makeText(MainActivity.this,
                         * R.string.sdcard_no, Toast.LENGTH_SHORT);
                         * toast.setGravity(Gravity.CENTER, 0, 0); toast.show();
                         * return; }
                         */

                        mMainTabHost.setCurrentTabByTag(TAG_SETTINGS);
                        break;
                    default:
                        break;
                }

            }
        });

        mRadioGroup.check(id);
    }

    /**
     * 初始化初次使用向导
     */
    private void initContactGuideView() {
        mContactsGuide = findViewById(R.id.first_contacts_guide);
        mContactsGuideBtn = (Button) findViewById(R.id.first_contacts_guide_btn);
        mContactsGuideBtn.setOnClickListener(this);
    }

    private void updateUnreadView(int unreadCount) {
        if (mTextViewUnread != null) {
            if (unreadCount > 0) {
                mTextViewUnread.setText(AndroidSysUtils.getUnreadCountStr(unreadCount));
                mTextViewUnread.setVisibility(View.VISIBLE);
            } else {
                mTextViewUnread.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean isChecking = false;

    private void checkUnread() {
        if (mService == null) {
            return;
        }

        if (isChecking) {
            // 正在检查，延时300ms再检查
            mHandler.removeMessages(PRIVATE_MSG_DELAY_CHECK);
            mHandler.sendEmptyMessageDelayed(PRIVATE_MSG_DELAY_CHECK, 300);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                isChecking = true;
                int unreadCount = mService.getMessageModule().getTotalUnreadMessageCount();
                isChecking = false;

                mHandler.sendMessage(Message.obtain(mHandler,
                        PRIVATE_MSG_UPDATE_UNREAD, unreadCount));
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "MainActivity  onDestroy");
        super.onDestroy();
        MainApp.i().popActivity(this);
        if (mService != null) {
            mService.unregisterCallBack(this);
        }
    }

    @Override
    protected void onResume() {
        SLog.d(TAG, "onResume");
        super.onResume();
        // 子类不要重复调用该方法，否则会重复统计数据。wing.hu edit at 2012-5-2
        checkUnread();
        checkFastChatUnread();
        SkymobiclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SLog.d(TAG, "onPause");
        // 子类不要重复调用该方法，否则会重复统计数据。wing.hu edit at 2012-5-2
        SkymobiclickAgent.onPause(this);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.first_contacts_guide_btn:
                mContactsGuide.setVisibility(View.GONE);
                CommonPreferences.setFirstContactsGuide(false);
                CommonPreferences.setContactsGuideVersion(getCurrentVersion());
                // anson.yang 释放mContactsGuide资源
                mContactsGuide = null;
                break;
            default:
                break;
        }
    }

    private boolean isSameVersion() {
        int contactsGuideVersion = CommonPreferences.getContactsGuideVersion();
        int currentVersion = getCurrentVersion();
        return contactsGuideVersion == currentVersion;
    }

    private int getCurrentVersion() {
        return MainApp.i().getPi().versionCode == 0 ? -1
                : MainApp.i().getPi().versionCode;
    }

    private boolean isFirstContactsGuide() {
        return CommonPreferences.getFirstContactsGuide();
    }

    // 显示未读快聊的信息条数
    private void checkFastChatUnread() {
        TextView countTv = (TextView) findViewById(R.id.main_tab_fastchat_count);
        int count = MainApp.getFastChatCache().getUnreadVoiceCount();
        if (count > 0) {
            countTv.setVisibility(View.VISIBLE);
            countTv.setText(AndroidSysUtils.getUnreadCountStr(count));
        } else {
            countTv.setVisibility(View.GONE);
        }
    }

    // 给activty 切换加上动画效果
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.move_right_in, R.anim.move_left_out);
    }

    // 给activty 切换加上动画效果
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.move_right_in, R.anim.move_left_out);
    }

    // 给activty 切换加上动画效果
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.move_left_in, R.anim.move_right_out);
    }

}
