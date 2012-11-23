
package android.skymobi.messenger.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.AppAdapter;
import android.skymobi.messenger.adapter.ChatAdapter;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.adapter.PopupMenuAdapter;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Friend;
import android.skymobi.messenger.bean.ResFile;
import android.skymobi.messenger.bean.Stranger;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.network.NetWorkMgr;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.MessageModule;
import android.skymobi.messenger.service.module.StrangerModule;
import android.skymobi.messenger.ui.chat.SmileyPanel;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Base64;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.MediaHelper;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.SmileyParser;
import android.skymobi.messenger.utils.StringUtil;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.skymobi.messenger.widget.ChatListView;
import android.skymobi.messenger.widget.ChatListView.OnRefreshListener;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skymobi.android.sx.codec.beans.clientbean.NetVCardNotify;
import com.skymobi.android.sx.codec.beans.common.VCardContent;
import com.skymobi.android.sx.codec.util.ParserUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ChatActivity
 * @Description: 聊天界面
 * @author Michael.Pan
 * @date 2012-2-10 下午03:25:20
 */
public class ChatActivity extends ContactBaseActivity implements OnItemClickListener,
        OnClickListener,
        SensorEventListener, OnTouchListener, onMediaStatusListener, TextWatcher {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private ChatListView mChatListView;
    private ChatAdapter mChatAdapter;
    private List<android.skymobi.messenger.bean.Message> mList;
    // 当前会话的ID
    private long mCurThreadsId = -1;

    private Threads mCurThreads = null;
    // 当前会话的用户电话
    private String mPhones = null;
    // 当前会话的 mAddressIDs
    private String mAddressIDs = null;
    // 当前用户的skyID
    private int mSkyID = -1;
    // 当前用户的sky_name
    private String mSkyName = null;
    // 当前用户的contact_id;
    private long mContact_id = -1;
    // 当前用户的姓名
    private String mName;
    // 输入内容
    private String mContent = null;
    // 切换扬声器模式提示
    private TextView mSwitchToast;

    // 黑名单，不能发起聊天提示
    private TextView mBlackListToast;
    // 表情布局
    private GridView mSmileyGridView;
    private SmileyPanel mSmileyInput;
    // 发送按钮
    private Button mSendBtn;
    // 切换语音式按钮
    private Button mVoiceBtn;
    // 输入框
    private EditText mChatEdit;
    // 录音按钮
    private Button mRecordBtn;

    private Button mAppBtn;
    // 输入法切换面板
    // private ListView mInputPanel;
    // 录音面板
    private ImageView mRecordBoardIV;
    private TextView mRecordBoardTV;
    // Intent输入参数
    public static final String CONTENT = "Content";
    public static final String ADDRESSIDS = "AddressIDs";
    public static final String ACCOUNTS = "Accounts";// 传递Account对象
    public static final String THREADS = "Threads";// 传递Threads对象
    public static final String CARD_ACCOUNTID = "Card_AccountId";// 发送名片的accoutId
    public static final String CONTACT = "CONTACT";
    public static final String ACTION = "ACTION";// 动作
    public static final String TALK_REASON = "Talk_Reason";

    public static final int ACTION_SENDCARD = 1;

    // 当前用户类型
    public static final int USER_TYPE_LOCAL = ContactsColumns.USER_TYPE_LOACL;// 本地联系人
    public static final int USER_TYPE_SHOUXIN = ContactsColumns.USER_TYPE_SHOUXIN;// 手信联系人
    public static final int USER_TYPE_RECOMMED = ContactsColumns.USER_TYPE_STRANGER;// 推荐的手信用户
    public static final int USER_TYPE_STRANGER_LOCAL = ContactsColumns.USER_TYPE_STRANGER + 1;// 只有phone
    public static final int USER_TYPE_STRANGER_SHOUXIN = ContactsColumns.USER_TYPE_STRANGER + 2;// 有skyid,没account&contact
    // 输入模式
    private int mInputType = INPUT_TEXT; // 0 :文字 1：表情 2：语音 3：短语
    public static final int INPUT_TEXT = 0;
    public static final int INPUT_VOICE = 1;
    // define limit low record sound time(ms)
    public static final int LIMIT_LOW_RECORD_TIME = 1000;
    // define limit high record sound time(ms)
    public static final int LIMIT_HIGH_RECORD_TIME = 60000;
    // define switch mode toast display time(ms)
    public static final int TOAST_DELAY_TIME = 2000;
    // context Menu's IDs
    public static final int MENU_DELETE_MSG = Menu.FIRST; // 删除选中消息
    public static final int MENU_COPY_MSG = Menu.FIRST + 1; // 删除选中消息
    public static final int MENU_FORWARD_MSG = Menu.FIRST + 2; // 删除选中消息
    public static final int MENU_RESEND_MSG = Menu.FIRST + 3; // 重发
    public static final int MENU_SAVE_MSG = Menu.FIRST + 4; // 保存
    public static final int MENU_RETRY_MSG = MENU_SAVE_MSG + 1; // 重试

    // Options Menu's IDs
    public static final int MENU_SWITCH_SPEAKER_MODE = Menu.FIRST + 1; // 切换扬声器模式
    public static final int MENU_VIEW_CONTACT = Menu.FIRST + 2; // 查看联系人信息

    // 当前语音模式
    private boolean mSpeakerOn = true; // true : 扬声器模式， false：听筒模式
    // 当前语音模式
    private boolean mVoicePlay = false; // true : 有语音播放， false：没有语音在播放
    // 音频管理器
    private AudioManager mAudioMgr;
    // 传感器管理器
    private SensorManager mSensorMgr;
    private Sensor mSensor;
    // 距离传感器的判断阈值
    private float maxSensorRange = 4.0f;
    private final SmileyParser mParser = SmileyParser.getInstance();
    private String mForwordContent; // 转发内容
    private MessageModule mMsgModule;
    private StrangerModule strangerModule;

    public static final int CHATLIST_PAGE_SIZE = 20;// 一次加载消息数量

    public static final int DIALOG_SAVE_CONTACT = 20;// 一次加载消息数量

    public static final int DIALOG_REMOVE_BLACK = 21;

    public static final int DIALOG_VOICE_INVITE = 22;// 点击录音邀请

    public static final int UPDATE_SELECTION_INIT = 1;// 消息初始化加载
    public static final int UPDATE_SELECTION_REFRESH = 2;// 下拉加载更多消息
    // public static final int UPDATE_STATE_DELETE = 3;// 删除刷新消息列表
    public static final int UPDATE_SELECTION_END = 3;// selection列表末
    public static final int UPDATE_SELECTION_NOCHANGE = 4;// selection不变

    private boolean hasSaved; // 已保存为联系人

    // InputModeAdapter mInputModeAdapter;

    private Contact mContact;

    private List<Address> mAddressList;

    private ArrayList<Account> mAccountList;

    private int mAction = -1;
    private long mCardAccountId;
    private int mUserType;
    private final int mSending = 0;

    private String mTalkReason;
    private String mNameFromAccount;
    private ImageButton mOptionBtn;

    private View mChatGuide;
    private View mChatGuideBtn;
    private View mAppPanel;
    private GridView mAppGrid;
    private AppAdapter mAppAdapter;
    // Handle message
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_THREADS_SYNC_BEGIN:
                    // showToast("同步会话开始");
                    break;
                case CoreServiceMSG.MSG_THREADS_SYNC_END:
                    // showToast("同步会话结束");
                    break;
                // case CoreServiceMSG.MSG_MESSAGES_SYNC_BEGIN:
                // 不处理同步结束事件
                // case CoreServiceMSG.MSG_MESSAGES_SYNC_END:
                // updateListForPage(0, CHATLIST_PAGE_SIZE,
                // UPDATE_SELECTION_NOCHANGE);
                // updateReadStatus();
                // break;
                case CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN:
                    // mSending++;
                    int curSelection = mChatAdapter.getCount();
                    mChatListView.setSelection(curSelection > 0 ?
                            curSelection : 0);

                    break;
                case CoreServiceMSG.MSG_CHATMSG_RESEND_BEGIN:
                    updateListForPage(0,
                            CHATLIST_PAGE_SIZE, UPDATE_SELECTION_NOCHANGE);
                    break;
                case CoreServiceMSG.MSG_CHATMSG_SMSMSG_SEND_END:
                case CoreServiceMSG.MSG_CHATMSG_TEXTMSG_SEND_END:
                case CoreServiceMSG.MSG_CHATMSG_CARDMSG_SEND_END:
                case CoreServiceMSG.MSG_CHATMSG_VOICEMSG_SEND_END:
                    // showToast("同步消息结束");
                    updateListForPage(0,
                            CHATLIST_PAGE_SIZE, UPDATE_SELECTION_NOCHANGE);
                    break;
                case CoreServiceMSG.MSG_CHATMSG_SMSMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_TEXTMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_CARDMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_VOICEMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_MARKETMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_SYSTEMMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_FRIENDSMSG_RECEIVE:
                    // showToast("收到信消息");
                    android.skymobi.messenger.bean.Message message = (android.skymobi.messenger.bean.Message) msg.obj;
                    if (showTemporaryChatLimit(R.string.chat_temporary_limit_recieve)) {
                        break;
                    }

                    showTalkReason();

                    if (message.getThreadsID() == mCurThreadsId) {
                        updateReadStatus();
                        updateListForPage(0,
                                CHATLIST_PAGE_SIZE, UPDATE_SELECTION_END);
                    }
                    break;
                case CoreServiceMSG.MSG_CHATMSG_RECODE_SOUND_CHANGE:
                    int level = (Integer) msg.obj;
                    updateRecordBoard(level);
                    break;
                case CoreServiceMSG.MSG_CHATMSG_SECOND_CHANGE:
                    int second = (Integer) msg.obj;
                    showCountDown(second);
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS:
                    // if (CONTACT_STRANGER.equals(mType)) {
                    // if (mUserType == USER_TYPE_RECOMMED || mUserType ==
                    // USER_TYPE_STRANGER_SHOUXIN) {
                    SLog.d(TAG, "add friend to contact success");
                    removeDialog(DIALOG_SAVE_CONTACT);
                    Contact contact = (Contact) msg.obj;
                    if (null != contact && contact.getId() > 0) {
                        // 更新 contact
                        mContact = contact;
                        mContact_id = contact.getId();
                        showToast(R.string.friend_save_success);
                        // mType = "";

                        hasSaved = true;
                        initOptionMenu(MENU_ADD_ADDRESSEE);
                        onResume();
                    } else {
                        showToast(R.string.friend_save_failed);
                    }
                    // }
                    break;
                case CoreServiceMSG.MSG_CONTACTS_IS_INCONTACT:
                    removeDialog(DIALOG_SAVE_CONTACT);
                    showToast(R.string.contacts_list_is_exist);
                    break;
                case CoreServiceMSG.MSG_CONTACTS_BLACKLIST_REMOVE:
                    Contact c = (Contact) msg.obj;
                    if (c != null) {
                        switch (c.getAction()) {
                            case ContactsNetModule.NET_SUCCESS:
                                mContact = c;
                                updateView();
                                ContactListCache.getInstance().recreateItems(
                                        mService.getContactsModule().getContactInfoForList());
                                showToast(R.string.contacts_list_blacklist_unblack_sucess);
                                break;
                            case ContactsNetModule.NET_ERR:
                                showToast(R.string.net_error);
                                break;
                            case ContactsNetModule.NET_FAILED:
                                showToast(R.string.contacts_list_blacklist_unblack_failed);
                                break;
                        }
                    }
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS:
                    updateView();
                    break;
                default:
                    break;
            }
        }
    };

    private void initAddressList() {
        if (null != mAddressIDs || mAddressList != null) {// 从会话进入
            if (mAddressList == null) {
                mAddressList = mMsgModule.getAddressList(mAddressIDs);
            }
            for (Address address : mAddressList) {
                Account account = mMsgModule.getAccountByAddress(address);
                if (null != account) {
                    if (null == mAccountList)
                        mAccountList = new ArrayList<Account>();
                    mAccountList.add(account);
                    address.setSkyId(account.getSkyId());
                    address.setPhone(account.getPhone());
                }
            }

        } else if (null == mAddressList && null != mAccountList) {// 从联系人等其他界面进入
            mAddressList = new ArrayList<Address>();

            for (Account account : mAccountList) {
                Address address = mMsgModule.getAddressByAccount(account);
                mAddressList.add(address);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        mSkyID = -1; // 重置skyid,hzc@20120911
        try {
            mMsgModule = mService.getMessageModule();
            strangerModule = mService.getStrangerModule();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
        Intent intent = getIntent();
        mAccountList = (ArrayList<Account>) intent.getSerializableExtra(ChatActivity.ACCOUNTS);
        mAddressIDs = intent.getStringExtra(ChatActivity.ADDRESSIDS);
        mCurThreads = (Threads) intent.getSerializableExtra(ChatActivity.THREADS);
        mContent = intent.getStringExtra(CONTENT);
        mAction = intent.getIntExtra(ACTION, -1);
        mTalkReason = intent.getStringExtra(TALK_REASON);
        Log.i(TAG, "mAction:" + mAction);
        mCardAccountId = intent.getLongExtra(CARD_ACCOUNTID, -1);

        mCurThreadsId = mCurThreads != null ? mCurThreads.getId() : -1;

        initAddressList();
        // 单聊
        if (null != mAccountList && mAccountList.size() == 1) {
            mSkyName = mAccountList.get(0).getSkyAccount();
            mNameFromAccount = mAccountList.get(0).getNickName() != null ? mAccountList.get(0)
                    .getNickName() : "";
            mContact_id = mAccountList.get(0).getContactId();
            if (mContact_id > 0) {
                mContact = DaoFactory.getInstance(mContext).getContactsDAO()
                        .getContactById(mContact_id);
                if (mContact != null && mContact.getDeleted() == ContactsColumns.DELETED_YES) {
                    mContact = null;
                }
            }
        }

        if (null != mAddressList && mAddressList.size() == 1) {
            mSkyID = mAddressList.get(0).getSkyId();
            mPhones = mAddressList.get(0).getPhone();
        }
        SLog.d(TAG, "mSkyid:" + mSkyID + ",mPhones:" + mPhones);

        if (null == mContact) {
            mContact = new Contact();
            mContact.setPhone(mPhones);
            mContact.setSkyid(mSkyID);
            Stranger stranger = strangerModule.fetch(mSkyID);
            if (null != stranger) {
                mSkyName = stranger.getSkyName();
            }
        }
        mUserType = getUserType();
        mName = mMsgModule.getDisplayName(mCurThreads, mAddressList);
        getThreads();
        initView();
        MediaHelper.getInstance().setLister(this);
    }

    @Override
    protected void onResume() {
        List<android.skymobi.messenger.bean.Message> messages = MainApp.i()
                .getNotifyMessages();
        int count = 0;
        for (android.skymobi.messenger.bean.Message message : messages) {
            if (message.getThreadsID() == mCurThreadsId) {
                count++;
            }
        }
        final int messageCount = count;
        if (messageCount > 0) {
            updateReadStatus();
            mChatListView.post(new Runnable() {

                @Override
                public void run() {
                    updateListForPage(0,
                            CHATLIST_PAGE_SIZE, UPDATE_SELECTION_END);

                }
            });

        }
        if (mService != null) {
            mService.cancelNotification();
        }
        if (mChatAdapter != null) {
            mChatAdapter.setPhotoID(reGetPhotoID());
        }
        updateView();
        mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mSensorMgr.unregisterListener(this);
        MediaHelper.getInstance().release();
        super.onPause();
    }

    private void initView() {
        initSensor();
        initChatListView();
        initInputView();
        showTemporaryChatLimit(R.string.chat_temporary_limit);
        showTalkReason();
        initTopBar();
        updateView();
        updateReadStatus();
    }

    /**
     * 初始化消息列表
     */
    private void initChatListView() {
        Log.i(TAG, "initChatListView");
        mChatListView = (ChatListView) findViewById(R.id.chat_listview);
        mChatListView.setOnItemClickListener(this);
        mChatAdapter = new ChatAdapter(this, getLayoutInflater(), mName, reGetPhotoID());
        mChatListView.setAdapter(mChatAdapter);
        mChatListView.setOnCreateContextMenuListener(this);
        mChatListView.setSelectionAfterHeaderView();
        mChatListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateListForPage(mChatAdapter.getCount(), CHATLIST_PAGE_SIZE,
                        UPDATE_SELECTION_REFRESH);
                // mChatListView.onRefreshComplete();
            }
        });

        updateListForPage(0, CHATLIST_PAGE_SIZE, UPDATE_SELECTION_INIT);
        // setSelection(mChatAdapter.getCount());
        // 当前会话的消息
        if (ACTION_SENDCARD == mAction) {
            mAction = -1;
            sendCard();
        }

    }

    // 重新获取头像的 UUID
    private String reGetPhotoID() {
        String photoId = "";
        ArrayList<String> list = mMsgModule.getPhotoIds(mCurThreads, mAddressList);
        if (list != null && list.size() > 1) {
            photoId = Constants.DEFAULT_MULTI_HEAD;
        } else if (list != null && list.size() == 1) {
            photoId = list.get(0);
        } else {
            photoId = null;
        }
        return photoId;
    }

    private void initAppGrid() {
        mAppGrid = (GridView) findViewById(R.id.chatting_app_grid);
        mAppAdapter = new AppAdapter(this, getLayoutInflater());
        mAppAdapter.addItem(R.drawable.app_grid_smiley, R.string.app_grid_item_smiley,
                APP_MODE_SMILEY);
        mAppAdapter.addItem(R.drawable.app_grid_phrase, R.string.app_grid_item_phrase,
                APP_MODE_PHRASE);
        if (!isBlackList() && mAddressList != null && mAddressList.size() == 1) {
            mAppAdapter.addItem(1, R.drawable.app_grid_card, R.string.app_grid_item_card,
                    APP_MODE_CARD);
        }

        mAppGrid.setAdapter(mAppAdapter);
        mAppGrid.setOnItemClickListener(this);
    }

    /**
     * 初始化输入界面
     */
    private void initInputView() {
        if (mSkyID < 1) {
            mInputType = INPUT_TEXT;
        } else {
            mInputType = MainApp.i().getInputType();
        }
        // 第一次使用会话时
        mChatGuide = findViewById(R.id.first_chat_guide);
        mChatGuideBtn = findViewById(R.id.first_chat_guide_btn);
        if (getFisrtChatGuide() || (!getFisrtChatGuide() && !isSameVersion())) {
            mChatGuide.setVisibility(View.VISIBLE);
            mChatGuideBtn.setOnClickListener(this);
        }

        // 切换语音播放模式的Toast背景
        mSwitchToast = (TextView) findViewById(R.id.speaker_mode_toast);

        mBlackListToast = (TextView) findViewById(R.id.blacklist_toast);

        // 输入面板
        LinearLayout mLyChatInputBar = (LinearLayout) findViewById(R.id.chat_input_bar);
        // 如果是手信小助手界面则隐藏输入框
        if (mSkyID == Constants.HELPER_SKY_ID)
            mLyChatInputBar.setVisibility(View.GONE);

        mSendBtn = (Button) findViewById(R.id.chat_send_btn);
        mSendBtn.setOnClickListener(this);
        if (mContent == null && mCurThreads != null)
            mContent = mCurThreads.getDraft();

        if (mContent != null && mContent.trim().length() > 0) {
            mSendBtn.setEnabled(true);
            mInputType = INPUT_TEXT;
        } else {
            mSendBtn.setEnabled(false);

        }

        mVoiceBtn = (Button) findViewById(R.id.chat_voice_btn);
        mVoiceBtn.setOnClickListener(this);

        mAppBtn = (Button) findViewById(R.id.chat_app_btn);
        mAppBtn.setOnClickListener(this);

        mChatEdit = (EditText) findViewById(R.id.chat_edit);
        if (mContent != null) {
            mChatEdit.setText(mParser.addSmileySpans(mContent));
        }
        mChatEdit.setOnClickListener(this);
        mChatEdit.addTextChangedListener(this);
        String ss = mChatEdit.getText().toString().trim();
        if (ss != null && ss.length() > 0) {
            mSendBtn.setEnabled(true);
        } else {
            mSendBtn.setEnabled(false);
        }
        mChatEdit.requestFocus();

        mRecordBtn = (Button) findViewById(R.id.chat_voice_record_btn);
        mRecordBtn.setOnTouchListener(this);
        showVoiceInputBar(false);
        // 表情面板
        mSmileyInput = (SmileyPanel) findViewById(R.id.chatting_smiley_panel);
        mSmileyInput.setVisibility(View.GONE);
        mAppPanel = findViewById(R.id.chatting_app_panel);

        // 录音面板
        mRecordBoardIV = (ImageView) findViewById(R.id.chat_record_board_iv);
        mRecordBoardIV.setVisibility(View.GONE);
        mRecordBoardTV = (TextView) findViewById(R.id.chat_record_board_tv);
        mRecordBoardTV.setVisibility(View.GONE);
        switchInputMode(mInputType);
    }

    /**
     * 初始化传感器
     */
    private void initSensor() {
        mAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioMgr.setMode(AudioManager.MODE_NORMAL);
        mSensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        maxSensorRange = mSensor.getMaximumRange() / 2;
    }

    /**
     * 长按menu的创建
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        int pos = getMenuPosition(menuInfo);
        menu.setHeaderTitle(mName);
        int type = mChatAdapter.getMsgType(pos);
        int status = mChatAdapter.getMsg(pos).getStatus();
        int opt = mChatAdapter.getMsg(pos).getOpt();
        if (type == MessagesColumns.TYPE_TEXT ||
                type == MessagesColumns.TYPE_SMS) {
            menu.add(0, MENU_DELETE_MSG, 0, R.string.chat_delete_msg);
            menu.add(0, MENU_COPY_MSG, 0, R.string.chat_copy_msg);
            menu.add(0, MENU_FORWARD_MSG, 0, R.string.chat_forward_msg);
            if (status == MessagesColumns.STATUS_FAILED)
                menu.add(0, MENU_RESEND_MSG, 0, R.string.chat_resend);
        } else if (type == MessagesColumns.TYPE_VOICE) {
            menu.add(0, MENU_DELETE_MSG, 0, R.string.chat_delete_voice);
            if (status == MessagesColumns.STATUS_FAILED)
                if (opt == MessagesColumns.OPT_TO) {
                    // 发送失败时,在选择列表中为重发
                    menu.add(0, MENU_RESEND_MSG, 0, R.string.chat_resend);
                } else if (opt == MessagesColumns.OPT_FROM) {
                    // 接收失败时,在选择列表中为重试
                    menu.add(0, MENU_RETRY_MSG, 0, R.string.retry);
                }
        } else if (type == MessagesColumns.TYPE_CARD) {
            menu.add(0, MENU_DELETE_MSG, 0, R.string.chat_delete_card);
            if (status == MessagesColumns.STATUS_FAILED)
                menu.add(0, MENU_RESEND_MSG, 0, R.string.chat_resend);
        } else if (type == MessagesColumns.TYPE_FRD) {
            menu.add(0, MENU_DELETE_MSG, 0, R.string.chat_delete_frd);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * 长按menu的处理
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pos = getMenuPosition(item);// 这里返回的pos为何会等于mChatAdapter的count，AdapterContextMenuInfo.position或者getHeaderViewsCount的值有问题？
        switch (item.getItemId()) {
            case MENU_DELETE_MSG: {
                mCurThreads = mMsgModule.getThreadsById(mCurThreadsId);
                mMsgModule.deleteMessage(mChatAdapter.getMsg(pos));

                List<android.skymobi.messenger.bean.Message> list = mMsgModule.getMessageList(
                        mCurThreadsId, -1, -1);
                if (list == null || list.size() == 0 && mCurThreads != null) {
                    mMsgModule.removeThreads(mCurThreads);
                    finish();
                    break;
                }
                // updateList();
                updateListForPage(0, CHATLIST_PAGE_SIZE, UPDATE_SELECTION_NOCHANGE);
                Log.i(TAG, "MENU_DELETE_MSG");
            }
                break;
            case MENU_COPY_MSG: {
                ClipboardManager cbMgr = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cbMgr.setText(mChatAdapter.getMsgContent(pos));
                Log.i(TAG, "MENU_COPY_MSG");
            }
                break;
            case MENU_FORWARD_MSG: {
                mForwordContent = mChatAdapter.getMsgContent(pos);
                Intent intent = new Intent(this, ContactForMessageListActivity.class);
                intent.putExtra(CONTENT, mForwordContent);
                startActivityForResult(intent, REQUEST_FORWARD_MSG);
                Log.i(TAG, "MENU_FORWARD_MSG");
            }
                break;
            case MENU_RESEND_MSG: {
                android.skymobi.messenger.bean.Message message = mChatAdapter.getMsg(pos);
                message.setStatus(MessagesColumns.STATUS_SENDING);
                message.setAddressList(mAddressList);
                message.setNickName(mName);
                message.setThreadsID(mCurThreadsId);
                int type = message.getType();
                switch (type) {
                    case MessagesColumns.TYPE_TEXT:
                        if (mAddressList.size() > 1) {
                            sendMultiMsg(message);
                        } else {
                            sendSingleMsg(message);
                        }
                        break;
                    case MessagesColumns.TYPE_SMS:
                        sendSingleMsg(message);
                        break;
                    case MessagesColumns.TYPE_CARD:
                        reSendCard(message);
                        break;
                    case MessagesColumns.TYPE_VOICE:
                        reSendVoice(message);
                        break;
                }

            }
                break;
            case MENU_SAVE_MSG: {
                showToast(mChatAdapter.getMsg(pos).getResFile().getPath());
            }
                break;
            case MENU_RETRY_MSG:
                // 判断SD是否可用
                if (!AndroidSysUtils.isAvailableSDCard(this)) {
                    ToastTool.showShort(this, R.string.no_sdcard_tip);
                } else {
                    // 异步下载语音消息
                    android.skymobi.messenger.bean.Message msg = mChatAdapter.getMsg(pos);
                    String fileID = mChatAdapter.getMsgContent(pos);
                    ResFile file = mMsgModule.getResFileByContent(fileID);
                    try {
                        mMsgModule.retryDownloadVoiceMsg(msg, file, pos);
                        // 将文件下载到file.getPath()路径下
                        updateListForPage(0, 20, UPDATE_SELECTION_NOCHANGE);
                    } catch (Exception e) {
                        showToast("\t" + e.getMessage());
                    }
                }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private final Runnable rHideToast = new Runnable() {
        @Override
        public void run() {
            mSwitchToast.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * 切换扬声器模式
     */
    private void switchSpeakerMode(boolean bSpeakerOn, boolean bShowToast) {
        if (!bSpeakerOn) {
            mSwitchToast.setText(R.string.chat_toast_speaker_off);
            mAudioMgr.setMode(AudioManager.MODE_IN_CALL);
        } else {
            mSwitchToast.setText(R.string.chat_toast_speaker_on);
            mAudioMgr.setMode(AudioManager.MODE_NORMAL);
        }
        initOptionMenu(0);
        if (bShowToast) {
            mSwitchToast.setVisibility(View.VISIBLE);
            mHandler.postDelayed(rHideToast, TOAST_DELAY_TIME);
        }
    }

    // 查看对方详情
    private void ViewHisContactDetail() {
        // 如果是小助手头像，直接返回，不显示详情页
        if (mSkyID == Constants.HELPER_SKY_ID) {
            return;
        }
        if (isBlackList()) { // 如果是黑名单直接到,黑名单页面
            Intent intent = new Intent(this, ContactsBlackListDetailActivity.class);
            intent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                    mContact.getId());
            startActivityForResult(intent, REQUEST_VIEW_CONTACT);
            return;
        }
        // 单个人的联系人详情
        // if (mAccountIdList.size() == 1 && mContact_id > 0) {
        if (mAddressList.size() == 1 && null != mContact) {
            SLog.d(TAG, "usertype:" + getUserType());
            switch (getUserType()) {
                case USER_TYPE_RECOMMED: // 推荐好友
                    SLog.d(TAG, "usertype : USER_TYPE_RECOMMED");
                    long friendId = mService.getFriendModule().getFriendIdByContactId(mContact_id);
                    if (friendId > 0) {
                        viewFriendDetail(friendId);
                    }
                    break;
                case USER_TYPE_LOCAL:
                case USER_TYPE_SHOUXIN:
                    SLog.d(TAG, "usertype : USER_TYPE_SHOUXIN");
                    Contact contact = mService.getContactsModule().getContactById(mContact_id);
                    if (null != contact) {
                        Intent intent = new Intent(this, ContactsDetailActivity.class);
                        intent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG, mContact_id);
                        startActivityForResult(intent, REQUEST_VIEW_CONTACT);
                    } else {
                        showToast(R.string.chat_no_contact_detail);
                    }
                    break;
                case USER_TYPE_STRANGER_SHOUXIN:
                    SLog.d(TAG, "usertype : USER_TYPE_STRANGER_SHOUXIN");
                    showDetail(mContact.getSkyid(), null, -1, Constants.CONTACT_TYPE_MANUAL);
                    break;
                case USER_TYPE_STRANGER_LOCAL: // 只有phone
                    // viewStrangerDetail(0, mName, mContact.getPhone());
                    SLog.d(TAG, "usertype : USER_TYPE_STRANGER_LOCAL");
                    Intent stranger = new Intent(this, ContactsStrangerDetailActivity.class);
                    stranger.putExtra(ContactsStrangerDetailActivity.STRANGER_PHONE_FLAG,
                            mContact.getPhone());
                    stranger.putExtra(ContactsStrangerDetailActivity.STRANGER_NICKNAME_FLAG,
                            mName);
                    startActivityForResult(stranger, REQUEST_VIEW_CONTACT);
                    break;
                default:
                    showToast(R.string.chat_no_contact_detail);
                    break;
            }
            // } else if (mAccountIdList.size() > 1) { // 群发详情页
        } else if (mAddressList.size() > 1) { // 群发详情页
            Intent intent = new Intent(this, ContactsGroupSendActivity.class);
            // intent.putExtra(ChatActivity.ACCOUNTIDS, mAccountIDs);
            intent.putExtra(ChatActivity.ACCOUNTS, mAccountList);
            startActivityForResult(intent, REQUEST_VIEW_CONTACT);
        } else {
            showToast(R.string.chat_no_contact_detail);
        }
    }

    // 查看自己详情
    private void ViewMyContactDetail() {
        Intent intent = new Intent(this, PersonalSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long rowId) {
        int id = parent.getId();
        switch (id) {
            case R.id.chat_listview: // 点击了聊天消息
                Log.i(TAG, "chat_listview onItemClick position = " + position);
                onChatItemClick((int) rowId);
                break;
            case R.id.smiley_panel_grid: // 点击了表情
                String strSmiley = mParser.getSmileyText(position);
                if (position == 14) {
                    final KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DEL);
                    mChatEdit.onKeyDown(KeyEvent.KEYCODE_DEL, keyEventDown);
                    break;
                }
                // String strEdit = mChatEdit.getText().toString() + strSmiley;
                String strOldEdit = mChatEdit.getText().toString();
                int selection = mChatEdit.getSelectionStart();
                String strEdit = strOldEdit.substring(0, mChatEdit.getSelectionStart()) + strSmiley
                        + strOldEdit.substring(mChatEdit.getSelectionStart(), strOldEdit.length());
                CharSequence chSpans = mParser.addSmileySpans(strEdit);
                // xml 中设置了最大值256，如果chSpans长度大于256，
                // mChatEdit.setSelection会导致crash
                if (chSpans.length() < 257) {
                    mChatEdit.setText(chSpans);
                    mChatEdit.setSelection(selection + strSmiley.length()); // 光标位置不变
                }
                Log.i(TAG, "smiley_gridview onItemClick position = " + position);
                break;
            case R.id.chatting_app_grid:
                switchAppMode(mAppAdapter.getMode(position));
                break;
            default:
                break;
        }

    }

    private static final int APP_MODE_SMILEY = 0;
    private static final int APP_MODE_CARD = 1;
    private static final int APP_MODE_PHRASE = 2;

    private void switchAppMode(int mode) {
        mAppPanel.setVisibility(View.GONE);
        switch (mode) {
            case APP_MODE_SMILEY:
                showVoiceInputBar(false);
                mChatEdit.requestFocus();
                mSmileyInput.setVisibility(View.VISIBLE);
                break;
            case APP_MODE_CARD:
                // 发名片
                Intent searchVCardIntent = new Intent(
                        ChatActivity.this,
                        VcardListActivity.class);
                Account account = null;
                if (mAccountList != null && mAccountList.size() == 1) {
                    account = mAccountList.get(0);
                } else if (mAccountList == null) {
                    account = new Account();

                    account.setPhone(mPhones);
                    account.setNickName(mName);
                    account.setSkyId(mSkyID);
                }
                searchVCardIntent.putExtra("account", account);
                startActivityForResult(searchVCardIntent,
                        ContactsDetailActivity.MESSAGE);
                break;
            case APP_MODE_PHRASE:
                Intent intent = new Intent(this, PickPhraseActivity.class);
                startActivityForResult(intent, REQUEST_PICK_PHRASE);
                break;
        }

    }

    private void hideFooterPanel() {
        mSmileyInput.setVisibility(View.GONE);
        mAppPanel.setVisibility(View.GONE);
    }

    private static final int REQUEST_PICK_PHRASE = 0x2000; // 短语
    private static final int REQUEST_FORWARD_MSG = 0x2001; // 转发
    private static final int REQUEST_PICK_CONTACT = 0x2002; // 添加选择联系人
    private static final int REQUEST_VIEW_CONTACT = 0x2005; // 查看陌生人资料
    // private static final int REQUEST_VIEW_GROUP = 0x2006;// 查看群聊
    protected static final int ADD_CONTACT = 33;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_PHRASE:
                if (resultCode == Activity.RESULT_OK) {
                    showVoiceInputBar(false);
                    String content = data.getStringExtra("Content");
                    mChatEdit.append(content);
                    Log.i(TAG, "Content = " + content);
                }
                break;
            case REQUEST_FORWARD_MSG:
                if (resultCode == Activity.RESULT_OK) {

                    ArrayList<Account> accounts = (ArrayList<Account>) data
                            .getSerializableExtra(ChatActivity.ACCOUNTS);
                    if (isTheSameForward(accounts)) {
                        mChatEdit.setText(mForwordContent);
                        mChatEdit.setSelection(mChatEdit.getText().toString().length());
                    } else {
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(ChatActivity.ACCOUNTS, accounts);
                        intent.putExtra(ChatActivity.CONTENT, mForwordContent);
                        startActivity(intent);
                        finish();
                    }
                }
                break;
            case REQUEST_PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    // String accountIDs =
                    // data.getStringExtra(ChatActivity.ACCOUNTIDS);
                    ArrayList<Account> accounts = (ArrayList<Account>) data
                            .getSerializableExtra(ChatActivity.ACCOUNTS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(ChatActivity.ACCOUNTS, accounts);
                    // intent.putExtra(ChatActivity.ACCOUNTIDS, accountIDs);
                    startActivity(intent);
                }
                break;
            case REQUEST_VIEW_CONTACT:
                SLog.d(TAG, "REQUEST_VIEW_STRANGER onActivityResult");
                if (resultCode == RESULT_OK) {
                    initOptionMenu(MENU_ADD_TO_CONTACT);
                    SLog.d(TAG, "REQUEST_VIEW_STRANGER result ok");
                    if (mContact == null) {
                        mContact = new Contact();
                    }

                    if (null != data) {
                        mContact = (Contact) data
                                .getSerializableExtra(FriendDetailActivity.CURRENT_CONTACT);
                        Log.e(TAG, "contact: " + mContact);
                        if (null != mContact) {
                            SLog.d(TAG, "disable AddFriendBtn");
                            mContact_id = mContact.getId();
                            initOptionMenu(MENU_ADD_ADDRESSEE);
                        }
                        // mUserType = getUserType();
                        initAddressList();
                        updateView();
                    }
                    mContact.setPhone(mPhones);
                    mContact.setSkyid(mSkyID);
                }
                break;
            default:
                break;
        }
    }

    private boolean isInputPanelVisiable = false;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.chat_voice_btn:
                hideFooterPanel();
                switchInputMode(Math.abs(mInputType - 1));
                break;
            case R.id.chat_app_btn:
                isInputPanelVisiable = !isInputPanelVisiable;
                mSmileyInput.setVisibility(View.GONE);
                if (mAppPanel.getVisibility() == View.GONE)
                    mAppPanel.setVisibility(View.VISIBLE);
                else if (mAppPanel.getVisibility() == View.VISIBLE)
                    mAppPanel.setVisibility(View.GONE);
                hideSystemSoftKeyboard(mChatEdit);
                Log.i(TAG, "onClick select button");
                break;
            case R.id.chat_send_btn:
                send();
                Log.i(TAG, "onClick send button");
                break;
            case R.id.chat_edit:
                hideFooterPanel();
                mChatEdit.requestFocus();
                showSystemSoftKeyboard(mChatEdit);
                Log.i(TAG, "onClick chat edit");
                break;
            case R.id.chat_add_friend:
                showDialog(DIALOG_SAVE_CONTACT);
                // Contact contact = (Contact)
                // getIntent().getSerializableExtra(CONTACT);
                // if (null != contact) { // 从推荐好友入口
                // mService.getContactsModule().addContact(contact, true);
                // 加为好友之后，userType需要重新获取
                switch (getUserType()) {
                    case USER_TYPE_STRANGER_LOCAL:
                        mContact.setDisplayname(mContact.getPhone());
                        mContact.setUserType(ContactsColumns.USER_TYPE_LOACL);
                        Account account = new Account();
                        account.setNickName(mName);
                        account.setPhone(mPhones);
                        ArrayList<Account> accounts = new ArrayList<Account>();
                        accounts.add(account);
                        mContact.setAccounts(accounts);
                        mService.getContactsModule().addContactForResult(mContact, true);
                        break;
                    case USER_TYPE_RECOMMED:
                        SLog.d(TAG, "add recommend friend");
                        long friendId = mService.getFriendModule().getFriendIdByContactId(
                                mContact_id);
                        mContact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                        // mService.getContactsModule().addContactByFriend(mContact,
                        // mAccountList.get(0));
                        if (friendId > 0) {
                            mService.getFriendModule().addContactByFriend(friendId);
                        }
                        break;
                    case USER_TYPE_STRANGER_SHOUXIN:
                        SLog.d(TAG, "add stranger shouxin friend");
                        // TODO 需要用异步
                        Contact contact = NetWorkMgr.getInstance().getContactsNetModule()
                                .getContactBySkyID(mContact.getSkyid());
                        // 网络错误会导致返回的对象为空
                        if (null != contact) {
                            contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                            contact.setDisplayname(contact.getNickName());
                            mService.getFriendModule().addFriendToCloud(contact,
                                    Constants.CONTACT_TYPE_MANUAL);
                        } else {
                            mHandler.sendMessage(mHandler.obtainMessage(
                                    CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS, contact));
                        }
                        break;
                }
                break;
            case R.id.chat_add_contacts: {
                Intent intent = new Intent(this, ContactForMessageListActivity.class);
                intent.putExtra(ChatActivity.ACCOUNTS, mAccountList);
                startActivityForResult(intent, REQUEST_PICK_CONTACT);
            }
                break;
            case R.id.chat_remove_blacklist:
                showDialog(DIALOG_REMOVE_BLACK);
                break;
            case R.id.chat_from_head:
                ViewHisContactDetail(); // 查看对方的详情
                break;
            case R.id.chat_to_head:
                ViewMyContactDetail(); // 查看自己的详情
                break;
            case R.id.first_chat_guide_btn: // 第一次使用聊天
                mChatGuide.setVisibility(View.GONE);
                CommonPreferences.setFirstChatGuide(false);
                CommonPreferences.setFirstChatGuideVersion(getCurrentVersion());
                // anson.yang 释放mChatGuide的资源(图片视图)
                mChatGuide = null;
                break;
            default:
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_SAVE_CONTACT:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_detail_saving), true, true);
            case DIALOG_REMOVE_BLACK:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setMessage(R.string.contacts_detail_blacklist_remove_tip);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mService.getContactsModule().removeContactFromBlackList(mContact_id);
                        dialog.cancel();

                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            case DIALOG_VOICE_INVITE:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setMessage(R.string.chat_voice_invite);
                dialogBuilder.setPositiveButton(R.string.invite,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switchInputMode(ChatActivity.INPUT_TEXT);
                                // 获取被邀请方的手机号码
                                String destPhone = null;
                                if (null != mPhones && !mPhones.equals("")) {
                                    destPhone = mPhones;
                                }
                                if (null == destPhone && null != mAddressList.get(0)) {
                                    destPhone = mAddressList.get(0).getPhone();
                                }
                                String phones = SettingsPreferences.getMobile();
                                if (!StringUtil.isBlank(destPhone)) {
                                    phones = phones + "," + destPhone;
                                }
                                // 新增将手机号码作为参数放入邀请内容中
                                String selfPhone = Base64.encode(AndroidSysUtils
                                        .inviteEncode(phones));
                                String inviteText = getInviteSting();
                                mChatEdit.setText(inviteText + selfPhone);
                                mChatEdit.requestFocus();
                                MainApp.i().setInviteEntrance(Constants.INVITE_ENTRANCE_UNKNOW);
                                MainApp.i().setInviteEntrance(
                                        Constants.INVITE_ENTRANCE_CHAT_VOICE);
                                dialog.cancel();
                            }
                        });
                dialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();

                            }
                        });
                break;
        }
        dialog = dialogBuilder.create();
        return dialog;

    }

    /**
     * 切换输入模式
     * 
     * @param inputType
     */
    private void switchInputMode(int inputType) {
        mInputType = inputType;
        if (mInputType == ChatActivity.INPUT_VOICE) {
            showVoiceInputBar(true);
            hideSystemSoftKeyboard(mChatEdit);
        } else {
            mSmileyInput.setVisibility(View.GONE);
            showVoiceInputBar(false);
            mChatEdit.requestFocus();
            showSystemSoftKeyboard(mChatEdit);
        }
    }

    private void showVoiceInputBar(boolean isShow) {
        mRecordBtn.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mSendBtn.setVisibility(isShow ? View.GONE : View.VISIBLE);
        mChatEdit.setVisibility(isShow ? View.GONE : View.VISIBLE);
        if (isShow)
            mVoiceBtn.setBackgroundResource(R.drawable.chat_text_btn_bg);
        else
            mVoiceBtn.setBackgroundResource(R.drawable.chat_voice_btn_bg);
    }

    /**
     * 返回键的处理
     */
    @Override
    public void onBackPressed() {
        // 如果表情输入存在先隐藏表情输入
        if (mSmileyInput.isShown()) {
            mSmileyInput.setVisibility(View.GONE);
            return;
        }

        if (mAppPanel.isShown()) {
            mAppPanel.setVisibility(View.GONE);
            return;
        }

        // 其他情况跳转界面
        onBackButtonPressed();

        super.onBackPressed();
    }

    @Override
    protected void onBackButtonPressed() {

        // 依据聊天界面来源来决定返回到那个界面
        Intent intent = new Intent(this, MainApp.i().getChatBackActivity());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        super.onBackButtonPressed();
    }

    @Override
    protected void onDestroy() {
        MainApp.i().setInputType(mInputType);
        MediaHelper.getInstance().release();
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing need to do
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!mVoicePlay)
            return;
        // >4.0f 为远离耳边 , <4.0f为接近耳边
        if (event.values[0] < maxSensorRange) {
            switchSpeakerMode(false, true); // 听筒模式
            showHhalfBoard(true);
        } else {
            switchSpeakerMode(mSpeakerOn, true); // 打开扬声器
            showHhalfBoard(false);
        }
        SLog.i(TAG, "maxSensorRange = " + maxSensorRange);
        SLog.i(TAG, "onSensorChanged = " + event.values[0]);
    }

    // 打开半透明挡板（1.屏幕变暗 2.脸部贴近屏幕也不会发生误操作）
    private void showHhalfBoard(boolean isOpen) {
        if (isOpen) {
            findViewById(R.id.chat_hhalf_board).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.chat_hhalf_board).setVisibility(View.GONE);
        }
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    /**
     * 分页刷新列表
     */
    private void updateListForPage(final int start, final int limit, int state) {
        getThreads();
        int lastCount = 0;
        mList = mMsgModule.getMessageList(mCurThreadsId, start, limit);
        Log.i(TAG, "mList = " + mList.size());
        if (mChatAdapter != null) {
            lastCount = mChatAdapter.getCount();
            if (0 == start)
                mChatAdapter.updateList(mList);
            else
                mChatAdapter.addToList(mList);
        }
        int curSelection = mChatAdapter.getCount() - lastCount - 1;
        if (mCurThreads == null || mChatAdapter.getCount() >= mCurThreads.getCount()) {
            // 已经会部加载
            mChatListView.setRefreshable(false);
        } else {
            mChatListView.setRefreshable(true);
        }

        switch (state) {
            case UPDATE_SELECTION_REFRESH:
                mChatListView.onRefreshComplete();
                if (0 == start)
                    mChatListView.setSelection(mChatAdapter.getCount());
                else
                    mChatListView.setSelection(curSelection >= 0 ? curSelection :
                            0);
                break;
            case UPDATE_SELECTION_NOCHANGE:
                mChatAdapter.notifyDataSetChanged();
                break;
            case UPDATE_SELECTION_END:
                mChatAdapter.notifyDataSetChanged();
                mChatListView.setSelection(mChatAdapter.getCount());
                break;
            default:
                break;
        }
    }

    /**
     * 发送短信
     */
    private void send() {
        if (showTemporaryChatLimit(R.string.chat_temporary_limit_send)) {
            return;
        }
        String content = mChatEdit.getText().toString().trim();
        if (mAddressList.size() <= 0) {
            return;
        }

        if (content.length() <= 0) {
            showToast(R.string.chat_enter_text_empty);
            return;
        }
        android.skymobi.messenger.bean.Message msg = new android.skymobi.messenger.bean.Message();
        msg.setContent(content);
        msg.setDate(System.currentTimeMillis());
        msg.setOpt(MessagesColumns.OPT_TO);
        msg.setRead(MessagesColumns.READ_YES);
        msg.setThreadsID(mCurThreadsId);
        msg.setStatus(MessagesColumns.STATUS_SENDING);
        msg.setTalkReason(mTalkReason);
        msg.setAddressList(mAddressList);
        msg.setNickName(mName);
        mTalkReason = null;
        mChatEdit.setText("");

        if (mAddressList.size() > 1) { // 群发
            msg.setType(MessagesColumns.TYPE_TEXT);
            sendMultiMsg(msg);
        } else { // 单聊
            String name = Thread.currentThread().getName();
            boolean status = MainApp.i().getUserOnlineStatus(mSkyID);
            SLog.d(TAG, ">>>> [" + name + "] sendMsg " + content + " |skyid:" + mSkyID
                    + "| phone:" + mPhones + "| status:" + status);
            // if (mAccountIDs == null || mSkyID <= 0
            if (mSkyID <= 0 || (!status && null != mPhones)) { // 如果没有skyid
                // 发短信
                msg.setPhones(mPhones);
                msg.setType(MessagesColumns.TYPE_SMS);
            } else {
                msg.setType(MessagesColumns.TYPE_TEXT);
            }
            sendSingleMsg(msg);
        }

        if (mChatAdapter != null)
            mChatAdapter.addToList(msg);

    }

    private void sendSingleMsg(android.skymobi.messenger.bean.Message msg) {
        if (msg.getId() > 0) {
            mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_RESEND_BEGIN,
                    null);
        } else {
            mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN,
                    null);
        }
        switch (msg.getType()) {
            case MessagesColumns.TYPE_SMS:
                mMsgModule.sendSMSMsg(msg);
                break;
            case MessagesColumns.TYPE_TEXT:
                mMsgModule.sendChatTextMsg(msg);
                break;

        }
    }

    private void sendMultiMsg(android.skymobi.messenger.bean.Message msg) {
        if (msg.getId() > 0) {
            mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_RESEND_BEGIN,
                    null);
        } else {
            mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN,
                    null);
        }
        mMsgModule.sendChatMultiMsg(msg, mAddressList);
    }

    private void updateReadStatus() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mMsgModule.updateReadStatus(mCurThreadsId);
            }
        });

    }

    private String mRecordPath = null;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() != R.id.chat_voice_record_btn)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 开始录音
                // 检查sd卡--begin ,hzc@20120927
                boolean isAvailable = AndroidSysUtils.isAvailableSDCard(this);
                if (!isAvailable) {
                    ToastTool.showShort(ChatActivity.this, R.string.no_sdcard_tip);
                } else if (AndroidSysUtils.getAvailableStore() < Constants.SDCARD_MIN_CAPACITY) {
                    ToastTool.showAtCenterShort(ChatActivity.this, R.string.sdcard_full);
                } else {
                    if (mUserType == USER_TYPE_LOCAL || mUserType == USER_TYPE_STRANGER_LOCAL) {
                        // 发送邀请
                        showDialog(DIALOG_VOICE_INVITE);
                    } else {
                        mRecordPath = recodeVoice();
                        showRecordBoard(true);
                    }
                }

                break;
            case MotionEvent.ACTION_UP: // 结束录音并且上传，发送
                showRecordBoard(false);
                sendVoice();
                break;
            default:
                break;
        }

        return false;
    }

    private String recodeVoice() {
        Log.i(TAG, "recodeVoice...");
        long curtime = System.currentTimeMillis();
        return MediaHelper.getInstance().startRecordVoice(curtime);
    }

    private void sendVoice() {
        Log.i(TAG, "sendVoice...");
        long curtime = System.currentTimeMillis();
        long recordTime = MediaHelper.getInstance().stopRecordVoice(curtime);
        // 处理录音文件不存在的情况,发送完成后path置为空
        if (mRecordPath == null) {
            // showToast(R.string.no_sdcard_mount);
            return;
        }
        // 处理录音文件太短的情况
        if (recordTime < LIMIT_LOW_RECORD_TIME) {
            // fix bug: http://redmine.sky-mobi.com/redmine/issues/13337
            if (recordTime > 0) {
                showToast(R.string.chat_record_voice_too_short);
            }
            return;
        }

        // 处理非手信用户不能发语音消息的情况'
        // if (mAccountIDs == null || mSkyID <= 0) {
        if (mSkyID <= 0) {
            showToast(R.string.chat_cannot_send_voice);
            return;
        }

        ResFile file = new ResFile();
        file.setPath(mRecordPath);
        file.setLength((int) recordTime / 1000);
        file.setVersion(ResFile.VERSION);
        file.setFormat(Constants.VOICE_EXT_NAME);
        file.setId(mMsgModule.addResFile(file));

        android.skymobi.messenger.bean.Message msg = new android.skymobi.messenger.bean.Message();
        msg.setContent(String.valueOf(file.getId()));
        msg.setDate(System.currentTimeMillis());
        msg.setOpt(MessagesColumns.OPT_TO);
        msg.setRead(MessagesColumns.READ_YES);
        msg.setType(MessagesColumns.TYPE_VOICE);
        msg.setThreadsID(mCurThreadsId);
        msg.setStatus(MessagesColumns.STATUS_SENDING);
        msg.setNickName(mName);
        msg.setResFile(file);
        if (mChatAdapter != null)
            mChatAdapter.addToList(msg);
        // mMsgModule.sendChatVoiceMsg(String.valueOf(mSkyID), msg, file,
        // mAccountIDs);
        mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN,
                null);
        mMsgModule.sendChatVoiceMsg(msg, file, mAddressList.get(0));
        mRecordPath = null; // 发送完成后文件路径置空
    }

    private void reSendVoice(android.skymobi.messenger.bean.Message message) {
        mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_RESEND_BEGIN,
                null);
        mMsgModule.sendChatVoiceMsg(message, message.getResFile(), mAddressList.get(0));

    }

    private void sendCard() {
        Log.i(TAG, "===sendCard===");
        android.skymobi.messenger.bean.Message msg = new android.skymobi.messenger.bean.Message();
        msg.setDate(System.currentTimeMillis());
        msg.setOpt(MessagesColumns.OPT_TO);
        msg.setRead(MessagesColumns.READ_YES);
        msg.setStatus(MessagesColumns.STATUS_SENDING);
        msg.setThreadsID(mCurThreadsId);

        Address address = mAddressList.get(0);
        Map<String, Object> cardMap = mMsgModule.getCardByContactId(mCardAccountId);
        /*
         * msg.setDate(msg.getDate()); msg.setOpt(msg.getOpt());
         * msg.setRead(msg.getRead()); msg.setType(msg.getType());
         */
        msg.setThreadsID(mCurThreadsId);
        msg.setAddressList(mAddressList);
        msg.setNickName(mName);
        if (address.getSkyId() > 0) { // 首先判断是否为手信激活用户
            if (MainApp.i().getUserOnlineStatus(address.getSkyId())) {
                // 激活用户，在线
                msg.setType(MessagesColumns.TYPE_CARD);
                msg.setContent(ParserUtils.encodeVCard(cardMap));
                if (null != mChatAdapter) {
                    mChatAdapter.addToList(msg);
                }
                mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN,
                        null);
                mMsgModule.sendCardMsg(address, msg, cardMap);
            } else {
                if (StringUtil.isBlank(address.getPhone())) {
                    // TODO 没有手机号码 发送离线消息
                    msg.setType(MessagesColumns.TYPE_CARD);
                    msg.setContent(ParserUtils.encodeVCard(cardMap));
                    if (null != mChatAdapter) {
                        mChatAdapter.addToList(msg);
                    }
                    mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN,
                            null);
                    mMsgModule.sendCardMsg(address, msg, cardMap);
                } else {
                    // TODO 有手机号码 发送SMS短信
                    msg.setType(MessagesColumns.TYPE_SMS);

                    String cardName = (String) cardMap.get(NetVCardNotify.CONTACT_NAME);
                    List<VCardContent> cardlist = (List) cardMap
                            .get(NetVCardNotify.CONTACT_DETAIL_LIST);

                    StringBuffer sBuffer = new StringBuffer();
                    sBuffer.append(MainApp.i().getResources()
                            .getString(R.string.card_detail_item_name)
                            + ":" + cardName + ";");
                    // 添加手机号
                    for (VCardContent vc : cardlist) {
                        if (vc.getPhone() != null && !vc.getPhone().equalsIgnoreCase("")) {
                            sBuffer.append("\n" + MainApp.i().getResources()
                                    .getString(R.string.card_detail_item_phone)
                                    + ":" + vc.getPhone() + ";");
                        }
                    }

                    msg.setContent(sBuffer.toString());
                    msg.setPhones(address.getPhone());
                    if (null != mChatAdapter) {
                        mChatAdapter.addToList(msg);
                    }
                    mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN,
                            null);
                    mMsgModule.sendSMSMsg(msg);
                }
            }

        } else if (!StringUtil.isBlank(address.getPhone())) {
            // 非手信激活用户，有对方号码，发送SMS短信
            msg.setType(MessagesColumns.TYPE_SMS);

            String cardName = (String) cardMap.get(NetVCardNotify.CONTACT_NAME);
            List<VCardContent> cardlist = (List) cardMap.get(NetVCardNotify.CONTACT_DETAIL_LIST);

            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append(MainApp.i().getResources()
                    .getString(R.string.card_detail_item_name)
                    + ":" + cardName + ";");

            // 添加手机号
            for (VCardContent vc : cardlist) {
                if (vc.getPhone() != null && !vc.getPhone().equalsIgnoreCase("")) {
                    sBuffer.append("\n" + MainApp.i().getResources()
                            .getString(R.string.card_detail_item_phone)
                            + ":" + vc.getPhone() + ";");
                }
            }

            msg.setContent(sBuffer.toString());
            msg.setPhones(address.getPhone());
            if (null != mChatAdapter)
                mChatAdapter.addToList(msg);
            mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SEND_BEGIN,
                    null);
            mMsgModule.sendSMSMsg(msg);
        }
    }

    private void reSendCard(android.skymobi.messenger.bean.Message message) {
        Log.i(TAG, "===reSendCard===");
        android.skymobi.messenger.bean.Message msg = new android.skymobi.messenger.bean.Message();
        msg.setDate(System.currentTimeMillis());
        msg.setOpt(MessagesColumns.OPT_TO);
        msg.setRead(MessagesColumns.READ_YES);
        msg.setStatus(MessagesColumns.STATUS_SENDING);
        msg.setNickName(mName);
        msg.setId(message.getId());
        Address address = mAddressList.get(0);
        Map<String, Object> cardMap = ParserUtils.decoderVCard(message.getContent());
        msg.setThreadsID(mCurThreadsId);
        if (address.getSkyId() > 0) { // 发网络名片
            msg.setType(MessagesColumns.TYPE_CARD);
            msg.setContent(message.getContent());
            mService.notifyObservers(CoreServiceMSG.MSG_CHATMSG_RESEND_BEGIN,
                    null);
            mMsgModule.sendCardMsg(address, msg, cardMap);
        }
    }

    private void onChatItemClick(int position) {
        hideSystemSoftKeyboard(mChatEdit); // fix bug:点击聊天界面的其他区域隐藏输入法面板
        hideFooterPanel();

        if (mChatAdapter == null) {
            return;
        }
        android.skymobi.messenger.bean.Message msg = mChatAdapter.getMsg(position);
        if (msg.getType() == MessagesColumns.TYPE_VOICE) {
            // 检查sd卡--begin ,hzc@20120927
            boolean isAvailable = AndroidSysUtils.isAvailableSDCard(this);
            if (!isAvailable) {
                ToastTool.showShort(ChatActivity.this, R.string.no_sdcard_tip);
                return;
            } else if (AndroidSysUtils.getAvailableStore() < Constants.SDCARD_MIN_CAPACITY) {
                ToastTool.showAtCenterShort(ChatActivity.this, R.string.sdcard_full);
                return;
            }

            if (msg.getStatus() == MessagesColumns.STATUS_FAILED) {
                if (msg.getOpt() == MessagesColumns.OPT_TO) {
                    showToast("语音消息发送失败，请长按后,选择重发!");
                } else if (msg.getOpt() == MessagesColumns.OPT_FROM) {
                    showToast("该语音消息接收失败，不能播放，请长按后选择\'重试\'重新下载语音文件!");
                } else {
                    showToast("该语音消息不可读,请检查网络!");
                }
            } else {
                String path = mMsgModule.getPathByContent(msg.getContent());
                MediaHelper.getInstance().playVoice(path, position);
            }
        } else if (msg.getType() == MessagesColumns.TYPE_CARD) {
            if (msg.getOpt() == MessagesColumns.OPT_FROM) {
                Intent intent = new Intent(this, CardDetailActivity.class);
                intent.putExtra(CardDetailActivity.CONTENT, msg.getContent());
                intent.putExtra(CardDetailActivity.TYPE, 1);
                startActivity(intent);
            } else if (msg.getOpt() == MessagesColumns.OPT_TO) {
                Intent intent = new Intent(this, CardDetailActivity.class);
                intent.putExtra(CardDetailActivity.CONTENT, msg.getContent());
                intent.putExtra(CardDetailActivity.TYPE, 0);
                startActivity(intent);
            }
        } else if (msg.getType() == MessagesColumns.TYPE_FRD) {
            if (msg.getOpt() == MessagesColumns.OPT_FROM) {
                Long friendId = Long.valueOf(msg.getContent());
                Friend friend = mService.getFriendModule().getFriendById(friendId);
                if (friend.getUserType() == ContactsColumns.USER_TYPE_STRANGER) {
                    Intent frdIntent = new Intent(this, FriendDetailActivity.class);
                    frdIntent.putExtra(FriendDetailActivity.FRIEND_ID_FLAG,
                            Long.valueOf(msg.getContent()));
                    this.startActivity(frdIntent);
                }
                if (friend.getUserType() == ContactsColumns.USER_TYPE_SHOUXIN) {
                    Intent contactIntent = new Intent(this, ContactsDetailActivity.class);
                    contactIntent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                            friend.getContactId());
                    this.startActivity(contactIntent);
                }
            }
        }
    }

    @Override
    public void onPlayCompletion(String path, int position) {
        int pos = position + mChatListView.getHeaderViewsCount();
        Log.i(TAG, "stopVoicePlayAnimation position = " + pos);
        if (mChatAdapter == null || path == null || position < 0)
            return;
        int firstPos = mChatListView.getFirstVisiblePosition();
        mChatAdapter.stopVoicePlayAnimation(mChatListView.getChildAt(pos - firstPos));
        mVoicePlay = false;
        switchSpeakerMode(mSpeakerOn, false);
        showHhalfBoard(false);
    }

    @Override
    public void onPlayStart(String path, int position) {
        int pos = position + mChatListView.getHeaderViewsCount();
        Log.i(TAG, "startVoicePlayAnimation position = " + pos);
        if (mChatAdapter == null || path == null || position < 0)
            return;
        Log.i(TAG, "total = " + mChatListView.getChildCount());
        Log.i(TAG, "mChatListView.getChildAt(position) = " + mChatListView.getChildAt(pos));
        int firstPos = mChatListView.getFirstVisiblePosition();
        mChatAdapter.startVoicePlayAnimation(mChatListView.getChildAt(pos - firstPos));
        mVoicePlay = true;
    }

    @Override
    public void onRecordSoundChanged(int level) {
        mHandler.sendMessage(Message
                .obtain(mHandler, CoreServiceMSG.MSG_CHATMSG_RECODE_SOUND_CHANGE, level));
    }

    @Override
    public void onSecondChanged(int second) {
        Log.i(TAG, "second = " + second);
        mHandler.sendMessage(Message
                .obtain(mHandler, CoreServiceMSG.MSG_CHATMSG_SECOND_CHANGE, second));
    }

    private void showRecordBoard(boolean bVisiable) {
        if (mRecordBoardIV != null) {
            mRecordBoardIV.setVisibility(bVisiable ? View.VISIBLE : View.GONE);
        }
        if (mRecordBoardTV != null && !bVisiable) {
            mRecordBoardTV.setVisibility(View.GONE);
        }
    }

    final int[] resID = {
            R.drawable.record_process_1,
            R.drawable.record_process_2,
            R.drawable.record_process_3,
            R.drawable.record_process_4,
            R.drawable.record_process_5,
            R.drawable.record_process_6,
            R.drawable.record_process_7,
            R.drawable.record_process_8,
            R.drawable.record_process_9,
    };

    private void updateRecordBoard(int level) {
        if (mRecordBoardIV == null || mRecordBoardIV.getVisibility() == View.GONE) {
            return;
        }
        mRecordBoardIV.setImageResource(resID[level]);
    }

    private int getMenuPosition(ContextMenuInfo menuInfo) {
        int pos = ((AdapterView.AdapterContextMenuInfo) menuInfo).position
                - mChatListView.getHeaderViewsCount();
        return pos;
    }

    private int getMenuPosition(MenuItem item) {
        int pos = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position
                - mChatListView.getHeaderViewsCount();
        return pos;
    }

    @Override
    public void finish() {
        if (hasSaved) {
            Intent data = new Intent();
            data.putExtra(CONTACT, mContact);
            setResult(RESULT_OK, data);
        }
        if (mMsgModule != null)
            mMsgModule.updateThreadsDraft(mCurThreadsId, mChatEdit.getText().toString().trim());
        super.finish();
    }

    // 显示录音倒计时(60s最后10秒倒计时)
    private void showCountDown(int second) {
        if (second > 0) {
            String msg = getResources().getString(R.string.chat_record_voice_too_long,
                    new Object[] {
                        String.valueOf(second)
                    });
            mRecordBoardTV.setVisibility(View.VISIBLE);
            mRecordBoardTV.setText(msg);
        } else {
            showRecordBoard(false);
            sendVoice();
        }
    }

    // 判断是否黑名单
    private boolean isBlackList() {
        /*
         * if(null!=mContact){ if(mContact.getBlackList() > 0){ return true; } }
         * return false;
         */
        return null != mContact && mContact.getBlackList() > 0 ? true : false;
    }

    private void updateView() {
        mName = mMsgModule.getDisplayName(mCurThreads, mAddressList);
        if (TextUtils.isEmpty(mName)) {
            // 打招呼,没有对应的会话
            mName = mNameFromAccount;
        }
        mUserType = getUserType();
        if (isBlackList()) {
            mSendBtn.setEnabled(false);
            mVoiceBtn.setEnabled(false);
            mChatEdit.setEnabled(false);
            mChatEdit.clearFocus();
            mBlackListToast.setText(R.string.chat_toast_blacklist);
            mBlackListToast.setVisibility(View.VISIBLE);
            initOptionMenu(MENU_REMOVE_BLACK);
        } else {
            mBlackListToast.setVisibility(View.GONE);
            if (mChatEdit.length() > 0) {
                mSendBtn.setEnabled(true);
            } else {
                mSendBtn.setEnabled(false);
            }
            mVoiceBtn.setEnabled(true);
            mChatEdit.setEnabled(true);
            if (mSkyID != Constants.HELPER_SKY_ID) {
                if (mAddressList.size() == 1) {

                    if (mUserType == USER_TYPE_STRANGER_LOCAL
                            || mUserType == USER_TYPE_STRANGER_SHOUXIN
                            || mUserType == USER_TYPE_RECOMMED) {
                        initOptionMenu(MENU_ADD_TO_CONTACT);
                    } else {
                        initOptionMenu(MENU_ADD_ADDRESSEE);
                    }
                } else {
                    initOptionMenu(MENU_ADD_ADDRESSEE);
                }
            }

        }
        // 重置在线状态和Title的显示
        onlineStatusChanged();
        initAppGrid();
    }

    private void getThreads() {
        if (-1 == mCurThreadsId) {
            String addressIds = getAddressIds();
            if (!TextUtils.isEmpty(addressIds)) {
                mCurThreads = mMsgModule.getThreadsByAddressIds(addressIds);
                if (null != mCurThreads)
                    mCurThreadsId = mCurThreads.getId();
            }
        } else {
            mCurThreads = mMsgModule.getThreadsById(mCurThreadsId);// 获取到最新的threads信息
        }
    }

    private String getAddressIds() {
        if (mAddressIDs != null) {
            return mAddressIDs;
        } else {
            StringBuilder addressIds = new StringBuilder();
            Long[] ids = new Long[mAddressList.size()];
            for (int i = 0; i < mAddressList.size(); i++) {
                Address address = mAddressList.get(i);
                Address a = mMsgModule.getAddressByAddress(address);
                if (null != a) {
                    ids[i] = a.getId();
                } else {
                    ids[i] = (long) 0;
                }
            }
            if (ids != null && ids.length > 0) {
                Arrays.sort(ids);
                for (int i = 0; i < ids.length; i++) {
                    if (ids[i] != null && (ids[i]) != 0) {
                        addressIds.append(ids[i] + ",");
                    } else {
                        addressIds.append(",");
                    }

                }
            }

            return addressIds.length() > 0 ? addressIds.substring(0, addressIds.length() - 1)
                    : null;
        }
    }

    /**
     * 获取用户类型
     * 
     * @return
     */
    private int getUserType() {
        if (null != mContact) {
            if (mContact.getId() > 0 && mContact.getDeleted() != 1) {
                // 云端返回的联系人
                return mContact.getUserType();
            } else {
                if (mContact.getSkyid() > 0)
                    // 只有SKYID的，说明没有添加好友的手信陌生人
                    return USER_TYPE_STRANGER_SHOUXIN;
                else
                    // 不是联系人的手机号码
                    return USER_TYPE_STRANGER_LOCAL;
            }
        }
        return -1;
    }

    /**
     * @param friendId
     */
    private void viewFriendDetail(long friendId) {
        Intent frdIntent = new Intent(this, FriendDetailActivity.class);
        frdIntent.putExtra(FriendDetailActivity.FRIEND_ID_FLAG, friendId);
        startActivityForResult(frdIntent, REQUEST_VIEW_CONTACT);
    }

    /**
     * 临时会话限制
     * 
     * @return
     */
    private boolean isTemporaryChatLimit() {
        /*
         * if ((mUserType == USER_TYPE_RECOMMED || mUserType ==
         * USER_TYPE_STRANGER_SHOUXIN) && mChatAdapter.getCount() >= 10) {
         * return true; } else { return false; }
         */
        // 需求先HOLD，陌生人临时会话没有10条的限制
        return false;
    }

    private boolean showTemporaryChatLimit(int resid) {
        if (isTemporaryChatLimit()) {
            mSwitchToast.setText(resid);
            mSwitchToast.setVisibility(View.VISIBLE);
            mHandler.postDelayed(rHideToast, TOAST_DELAY_TIME);
            return true;
        }
        return false;
    }

    private void showTalkReason() {
        String talkReason = MainApp.i().getTalkReason(mCurThreadsId);
        if (!TextUtils.isEmpty(talkReason)) {
            mSwitchToast.setText(talkReason);
            mSwitchToast.setVisibility(View.VISIBLE);
            mHandler.postDelayed(rHideToast, TOAST_DELAY_TIME);
        }
    }

    private boolean isTheSameForward(ArrayList<Account> accounts) {
        if (null == accounts)
            return false;

        if (null != mAccountList) {
            if (accounts.size() == mAccountList.size()) {
                for (Account account : accounts) {
                    boolean same = false;
                    for (Account a : mAccountList) {
                        if (!TextUtils.isEmpty(account.getPhone())
                                && account.getPhone().equals(a.getPhone())) {
                            same = true;
                        }

                        if (account.getSkyId() > 0 && account.getSkyId() == a.getSkyId()) {
                            same = true;
                        }
                    }
                    if (same == false)
                        return false;
                }
                return true;
            } else {
                return false;
            }

        } else if (null == mAccountList && accounts.size() == 1) {
            Account account = accounts.get(0);
            if (!TextUtils.isEmpty(account.getPhone())
                    && account.getPhone().equals(mContact.getPhone())) {
                return true;
            }

            if (account.getSkyId() > 0 && account.getSkyId() == mContact.getSkyid()) {
                return true;
            }

            return false;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mChatEdit == null || mSendBtn == null)
            return;
        String ss = mChatEdit.getText().toString().trim();
        if (ss != null && ss.length() > 0) {
            mSendBtn.setEnabled(true);
        } else {
            mSendBtn.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private void onlineStatusChanged() {
        boolean status = MainApp.i().getUserOnlineStatus(mSkyID);
        try {
            // 检查陌生人中是否存在该SKYID，如果有该SKYID，则将状态去掉
            if (mService.getFriendModule().checkStrangerExists(mSkyID) || isBlackList()) {
                status = false;
            }
        } catch (NullPointerException e) {
            status = false;
        }
        // 根据在线状态设置Title的显示字符串和颜色
        int color = getResources().getColor(R.color.chat_phone_online_color);
        String onlineStr = "";
        if (status) {
            onlineStr = getResources().getString(R.string.chat_online);
            color = getResources().getColor(R.color.chat_phone_online_color);
        } else {
            onlineStr = "";
            color = getResources().getColor(R.color.chat_phone_offline_color);
        }

        if (mPhones != null && mUserType != USER_TYPE_STRANGER_LOCAL) {
            setTopBarTitle(mName, mPhones + onlineStr, color);
            // } else if (mSkyID > 1 && mUserType != USER_TYPE_STRANGER_SHOUXIN)
            // {// 手信小助手，不显示\
        } else if (mSkyID > 0 && !StringUtil.isBlank(mSkyName)) {
            SLog.d(TAG, "\t>>>>>> 显示对话框中的帐号信息:" + mSkyName);
            SLog.d(TAG, "\t>>>>>> 显示对话框中的在线状态信息:" + onlineStr);
            setTopBarTitle(mName, (mSkyName != null) ? mSkyName + onlineStr : "", color);
        } else {
            setTopBarTitle(mName, null);
        }
        // 输入框提示进行变更
        if (mAddressList != null && mAddressList.size() > 1) {
            mChatEdit.setHint(R.string.chat_hint_msg); // 群发
        } else {
            if (mSkyID <= 0 || (!status && null != mPhones)) {
                mChatEdit.setHint(R.string.chat_hint_sms); // 单聊短信
            } else {
                mChatEdit.setHint(R.string.chat_hint_net); // 单聊网络消息
            }
        }
    }

    private PopupMenuAdapter mPopmenuAdapter;
    private int mMenus;

    @Override
    public void initTopBar() {
        if (mPhones != null && mUserType != USER_TYPE_STRANGER_LOCAL) {
            setTopBarTitle(mName, mPhones);
            // } else if (mSkyID > 1 && mUserType != USER_TYPE_STRANGER_SHOUXIN)
            // {// 手信小助手，不显示
        } else if (mSkyID > 0 && !StringUtil.isBlank(mSkyName)) {
            setTopBarTitle(mName, (mSkyName != null) ? mSkyName : "");
        } else {
            setTopBarTitle(mName, null);
        }

        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        mPopmenuAdapter = new PopupMenuAdapter(this, null);
        if (mSkyID == Constants.HELPER_SKY_ID)
            return;
        initOptionMenu(mMenus);
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_option,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRightPopupMenu(ChatActivity.this, mPopmenuAdapter,
                                findViewById(R.id.topbar_imageButton_rightII),
                                new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                                        String str = (String) mPopmenuAdapter.getItem(position);
                                        // 拨号
                                        if (str.equals(getString(R.string.chat_menu_call))) {
                                            Uri uri = Uri.parse("tel:" + mPhones);
                                            Intent it = new Intent(Intent.ACTION_CALL, uri);
                                            startActivity(it);
                                        } else if (str
                                                .equals(getString(R.string.chat_menu_send_vcard))) {
                                            // 发名片
                                            Intent searchVCardIntent = new Intent(
                                                    ChatActivity.this,
                                                    VcardListActivity.class);
                                            Account account = null;
                                            if (mAccountList != null && mAccountList.size() == 1) {
                                                account = mAccountList.get(0);
                                            } else if (mAccountList == null) {
                                                account = new Account();

                                                account.setPhone(mPhones);
                                                account.setNickName(mName);
                                                account.setSkyId(mSkyID);
                                            }
                                            searchVCardIntent.putExtra("account", account);
                                            startActivityForResult(searchVCardIntent,
                                                    ContactsDetailActivity.MESSAGE);
                                        } else if (str
                                                .equals(getString(R.string.chat_menu_add_addressee))) {
                                            // 添加收件人
                                            Intent intent = new Intent(ChatActivity.this,
                                                    ContactForMessageListActivity.class);
                                            intent.putExtra(ChatActivity.ACCOUNTS, mAccountList);
                                            startActivityForResult(intent, REQUEST_PICK_CONTACT);

                                        } else if (str
                                                .equals(getString(R.string.chat_menu_add_to_contact))) {
                                            // 加为联系人
                                            showDialog(DIALOG_SAVE_CONTACT);
                                            switch (getUserType()) {
                                                case USER_TYPE_STRANGER_LOCAL:
                                                    mContact.setDisplayname(mContact.getPhone());
                                                    mContact.setUserType(ContactsColumns.USER_TYPE_LOACL);
                                                    Account account = new Account();
                                                    account.setNickName(mName);
                                                    account.setPhone(mPhones);
                                                    ArrayList<Account> accounts = new ArrayList<Account>();
                                                    accounts.add(account);
                                                    mContact.setAccounts(accounts);
                                                    mService.getContactsModule()
                                                            .addContactForResult(mContact, true);
                                                    break;
                                                case USER_TYPE_RECOMMED:
                                                    SLog.d(TAG, "add recommend friend");
                                                    long friendId = mService.getFriendModule()
                                                            .getFriendIdByContactId(
                                                                    mContact_id);
                                                    mContact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                                                    // mService.getContactsModule().addContactByFriend(mContact,
                                                    // mAccountList.get(0));
                                                    if (friendId > 0) {
                                                        mService.getFriendModule()
                                                                .addContactByFriend(friendId);
                                                    }
                                                    break;
                                                case USER_TYPE_STRANGER_SHOUXIN:
                                                    SLog.d(TAG, "add stranger shouxin friend");
                                                    // TODO 需要用异步
                                                    Contact contact = NetWorkMgr.getInstance()
                                                            .getContactsNetModule()
                                                            .getContactBySkyID(mContact.getSkyid());
                                                    // 网络错误会导致返回的对象为空
                                                    if (null != contact) {
                                                        contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                                                        contact.setDisplayname(contact
                                                                .getNickName());
                                                        // TODO 这种添加为联系属于什么类型
                                                        mService.getFriendModule()
                                                                .addFriendToCloud(
                                                                        contact,
                                                                        Constants.CONTACT_TYPE_MANUAL);
                                                    } else {
                                                        mHandler.sendMessage(mHandler
                                                                .obtainMessage(
                                                                        CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS,
                                                                        contact));
                                                    }
                                                    break;
                                            }
                                        } else if (str
                                                .equals(getString(R.string.chat_menu_remove_black))) {
                                            // 解除黑名单
                                            showDialog(DIALOG_REMOVE_BLACK);
                                        }
                                        else if (str
                                                .equals(getString(R.string.chat_view_contacts))) {
                                            ViewHisContactDetail();
                                        }
                                        else if (str
                                                .equals(getString(R.string.chat_speaker_on)) || str
                                                .equals(getString(R.string.chat_speaker_off))) {
                                            mSpeakerOn = !mSpeakerOn;
                                            switchSpeakerMode(mSpeakerOn, true);
                                            SLog.d("SLog", "mSpeakerOn = " + mSpeakerOn);
                                        }

                                        dismissPopupMenu();
                                    }
                                });
                    }
                });
    }

    private final static int MENU_CALL = 0x01;// 拨号
    // private final static int MENU_SEND_VCARD = 0x02;// 发名片
    private final static int MENU_ADD_ADDRESSEE = 0x04;// 添加收件人
    private final static int MENU_ADD_TO_CONTACT = 0x08;// 加为联系人
    private final static int MENU_REMOVE_BLACK = 0x10;// 解除黑名单
    private final static int MENU_CONTACT_DETAIL = 0x20;// 解除黑名单

    private void initOptionMenu(int menus) {
        ArrayList<String> mList = new ArrayList<String>();
        if (menus != 0) {
            mMenus = menus;
        }
        if (!isBlackList()) {
            if (!TextUtils.isEmpty(mPhones))
                mMenus = mMenus | MENU_CALL;

            // if (mAddressList != null && mAddressList.size() == 1) {
            // mMenus = mMenus | MENU_SEND_VCARD;
            // }
        }
        mMenus = mMenus | MENU_CONTACT_DETAIL;

        if (0 != (mMenus & MENU_CALL)) {
            mList.add(getString(R.string.chat_menu_call));
        }
        // if (0 != (mMenus & MENU_SEND_VCARD)) {
        // mList.add(getString(R.string.chat_menu_send_vcard));
        // }
        if (0 != (mMenus & MENU_ADD_ADDRESSEE)) {
            mList.add(getString(R.string.chat_menu_add_addressee));
        }
        if (0 != (mMenus & MENU_ADD_TO_CONTACT)) {
            mList.add(getString(R.string.chat_menu_add_to_contact));
        }
        if (0 != (mMenus & MENU_REMOVE_BLACK)) {
            mList.add(getString(R.string.chat_menu_remove_black));
        }
        if (mAddressList.size() == 1 && mSkyID > 0) {
            if (mSpeakerOn) {
                mList.add(getString(R.string.chat_speaker_on));
            } else {
                mList.add(getString(R.string.chat_speaker_off));
            }
        }

        if (0 != (mMenus & MENU_CONTACT_DETAIL)) {
            mList.add(getString(R.string.chat_view_contacts));
        }

        mPopmenuAdapter.updateAdapter(mList);
    }

    private String getInviteSting() {
        String text = CommonPreferences
                .getInviteConfigContent(Constants.INVITE_CONFIGURATION_VOICE_TYPE);
        return TextUtils.isEmpty(text) ? getString(R.string.chat_voice_invite_context) : text;
    }

    private boolean getFisrtChatGuide() {
        return CommonPreferences.getFirstChatGuide();
    }

    public boolean isSameVersion() {
        int firstChatGuideVersion = CommonPreferences.getFirstChatGuideVersion();
        int currentVersion = getCurrentVersion();
        return firstChatGuideVersion == currentVersion;
    }
}
