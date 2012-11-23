
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.MessageListAdapter;
import android.skymobi.messenger.adapter.MessageListCache;
import android.skymobi.messenger.adapter.PopupMenuAdapter;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.MessageModule;
import android.skymobi.messenger.service.module.StrangerModule;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.TimeUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: MessageListActivity
 * @Description: 消息列表
 * @author Michael.Pan
 * @date 2012-2-6 下午01:57:56
 */
public class MessageListActivity extends TopActivity implements OnItemClickListener {

    private static final String TAG = MessageListActivity.class.getSimpleName();
    protected MessageModule mMessageModule;
    protected StrangerModule strangerModule;
    protected ListView mListView = null;
    protected MessageListAdapter mMsgAdapter;

    protected LinearLayout mLyNomessage; // 无消息的layoutout
    protected static final int PRIVATE_MSG_UPDATE = 0x000a;
    protected static final int PRIVATE_MSG_DELAY_UPDATELIST = 0x000b;

    // 网络状态
    LinearLayout mLayoutNet;
    // ListView Context Menu's IDs
    public static final int MENU_DELETE_THREADS = Menu.FIRST; // 删除会话

    // Options Menu's IDs
    public static final int MENU_MULTI_DELETE_THREADS = Menu.FIRST + 1; // 批量删除
    public static final int MENU_QUIT = Menu.FIRST + 2; // 退出
    public static final int MENU_ABOUT = Menu.FIRST + 3; // 关于（查看版本信息）
    public static final int MENU_CALL = Menu.FIRST + 4; // 拨号

    // Dialog ID
    public static final int DIALOG_CLEAR_ALL = 0x1000;
    public static final int DIALOG_PROCEESS = 0x1001;
    public static final int DIALOG_ABOUT = 0x1002;
    public static final int DIALOG_SYNC = 0x1003;
    public static final int DIALOG_DELETE_SMS_THREARDS = 0x1004; // 删除会话时，看看是否是含有电话的会话，如果是那么弹出删除会话对话框

    // Handle message
    protected final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_REBIND_SUCESS:
                    syncWaitDialog();
                    break;
                case CoreServiceMSG.MSG_THREADS_SYNC_BEGIN:
                    updateCurMaxProcess(0);
                    break;
                case CoreServiceMSG.MSG_THREADS_SYNC_PROGRESS:
                    if (msg.obj != null && msg.obj instanceof Integer) {
                        int progress = (Integer) msg.obj;
                        SLog.d(TAG, "progress = " + progress);
                        updateCurMaxProcess(progress);
                    }
                    break;
                case CoreServiceMSG.MSG_THREADS_SYNC_END:
                    // showToast("同步会话结束");
                    updateList();
                    break;
                case CoreServiceMSG.MSG_MESSAGES_SYNC_BEGIN:
                    // showToast("同步消息开始");
                    break;
                case CoreServiceMSG.MSG_MESSAGES_SYNC_END:
                    // showToast("同步消息结束");
                    break;
                case CoreServiceMSG.MSG_CONTACTS_SYNC_END:
                    updateCurMaxProcess(100);
                    updateList();
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END:
                    updateList();
                    break;
                case CoreServiceMSG.MSG_CHATMSG_SMSMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_TEXTMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_CARDMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_VOICEMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_MARKETMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_SYSTEMMSG_RECEIVE:
                case CoreServiceMSG.MSG_CHATMSG_FRIENDSMSG_RECEIVE:
                    // showToast("收到新消息");
                    updateList();
                    break;
                case CoreServiceMSG.MSG_NET_STATUE_CHANGE:
                    Log.i(TAG, " >> 网络状态通知:" + msg.obj);
                    // 网络广播会通知到此方法中，改变列表中网络提示块
                    netStatusChanged(mService.isNetConnected());
                    break;
                case CoreServiceMSG.MSG_THREADS_CLEARALL: {
                    updateList();
                    multiDeleteDialog.setProgress(msg.arg2);
                }
                    break;
                case CoreServiceMSG.MSG_THREADS_CLEARALL_END:
                    removeDialog(DIALOG_PROCEESS);
                    finish();
                    break;
                case CoreServiceMSG.MSG_THREADS_CLEARALL_BEGIN:
                    showDialog(DIALOG_PROCEESS);
                    break;
                case PRIVATE_MSG_UPDATE:
                    redrawListview();
                    if ((mSyncProgress != null && mSyncProgress.getProgress() == 100)
                            || (!MainApp.i().getStatusSyncThreads() && !MainApp
                                    .i()
                                    .isSyncContacts())) {
                        removeDialog(DIALOG_SYNC);
                    }
                    break;
                case PRIVATE_MSG_DELAY_UPDATELIST:
                    updateList();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list);

        try {
            initTopBar();
            init();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
    }

    /**
     * 初始化消息列表
     */
    protected void init() {
        Log.i(TAG, "初始化消息列表...");
        final long bTime = System.currentTimeMillis();
        // 初始化网络状态
        mLayoutNet = (LinearLayout) findViewById(R.id.network_status);
        // 无消息的layoutout
        mLyNomessage = (LinearLayout) findViewById(R.id.message_list_nomessage_ly);
        // 初始化列表
        mListView = (ListView) findViewById(R.id.message_listview);
        mMessageModule = mService.getMessageModule();
        strangerModule = mService.getStrangerModule();
        mMsgAdapter = new MessageListAdapter(this, getLayoutInflater(), mMessageModule);
        mListView.setAdapter(mMsgAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(this);
        final long eTime = System.currentTimeMillis();
        SLog.d(TIME_TAG, "初始化消息列表[init()]总耗时:" + TimeUtils.getTimeconsuming(bTime, eTime) + " sec");
    }

    @Override
    protected void onResume() {
        super.onResume();
        onOwnResume();
    }

    protected void onOwnResume() {
        // 刷新列表
        updateList();
        // 去掉notification
        mService.cancelNotification();
        // 初始化网络状态
        netStatusChanged(mService.isNetConnected());

        Intent parentIntent = getParent().getIntent();
        String addressIDs = parentIntent.getStringExtra(ChatActivity.ADDRESSIDS);
        if (!TextUtils.isEmpty(addressIDs)) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.ADDRESSIDS, addressIDs);
            startActivity(intent);
            parentIntent.putExtra(ChatActivity.ADDRESSIDS, "");
        }
        syncWaitDialog();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Log.i(TAG, "onListItemClick  position:" + position);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.THREADS, mMsgAdapter.getThreads(position));
        intent.putExtra(ChatActivity.ADDRESSIDS, mMsgAdapter.getThreads(position).getAddressIds());
        startActivity(intent);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    /**
     * 删除陌生人数据
     * 
     * @param threads
     */
    private void deleteStrangerData(Threads threads) {
        if (null != threads) {
            List<Address> addressList = threads.getAddressList();
            for (Address addre : addressList) {
                if (null != addre && addre.getSkyId() > 0) {
                    if (strangerModule.delete(addre.getSkyId())) {
                        SLog.d(TAG, "\t>>>>>> 删除会话 根据skyid删除陌生人成功|skyid:" + addre.getSkyId());

                    }
                }
            }
        }
    }

    /**
     * 刷新列表数据
     */
    private boolean isUpdating = false;

    protected void updateList() {
        if (MainApp.i().getLastSyncThreadsTime() == 0) {
            mMessageModule.syncSMSThreads(true);
            mService.getContactsModule().syncContacts();
        }
        if (isUpdating) {
            // 正在刷新，延时500ms再刷新
            mHandler.removeMessages(PRIVATE_MSG_DELAY_UPDATELIST);
            mHandler.sendEmptyMessageDelayed(PRIVATE_MSG_DELAY_UPDATELIST, 500);
            return;
        }
        isUpdating = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final long bTime = System.currentTimeMillis();
                // 如果从登录界面登录，则在展示消息 列表界面后，再进行同步操作，防止进入的时候黑屏
                final List<Threads> list = mMessageModule.getThreadsList();
                MessageListCache.getInstance().UpdateList(list);
                isUpdating = false;
                final long eTime = System.currentTimeMillis();
                SLog.d(TIME_TAG, "获取本地会话列表耗时:" + TimeUtils.getTimeconsuming(bTime, eTime) + " sec");
                mHandler.sendEmptyMessage(PRIVATE_MSG_UPDATE);

            }
        });
    }

    /**
     * 刷新列表视图
     */
    private void redrawListview() {
        List<Threads> list = MessageListCache.getInstance().getMessageList();
        if (list.size() == 0) {
            mLyNomessage.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mLyNomessage.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
        if (mMsgAdapter != null) {
            mMsgAdapter.updateList();
        }
    }

    // 记录长按时对应的会话,用于长按menu的显示和之后的删除操作，拨号操作等
    private Threads mSelectThreads = null;

    /**
     * 长按menu的创建
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        int pos = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        menu.setHeaderTitle(mMsgAdapter.getDisplayName(pos));
        mSelectThreads = mMsgAdapter.getThreads(pos);
        List<Address> addressList = mSelectThreads.getAddressList();
        if (addressList != null && addressList.size() == 1) {
            if (TextUtils.isEmpty(addressList.get(0).getPhone())) {
                Account account = mMessageModule.getAccountByAddress(addressList.get(0));
                if (null != account && !TextUtils.isEmpty(account.getPhone())) {
                    addressList.get(0).setPhone(account.getPhone());
                }
            }

            if (!TextUtils.isEmpty(addressList.get(0).getPhone())) {
                menu.add(0, MENU_CALL, 0, R.string.contacts_detail_call);
            }
        }

        menu.add(0, MENU_DELETE_THREADS, 0, R.string.message_delete_threads);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * 长按menu的处理
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE_THREADS: {
                List<Address> addressList = mSelectThreads.getAddressList();
                // 带电话的删除需要提示将删除本地对应短信
                if (addressList != null && addressList.size() == 1
                        && !TextUtils.isEmpty(addressList.get(0).getPhone())) {
                    showDialog(DIALOG_DELETE_SMS_THREARDS);
                } else {
                    deleteThreads(mSelectThreads);
                }
            }
                break;
            case MENU_CALL: {
                List<Address> addressList = mSelectThreads.getAddressList();
                if (addressList != null && addressList.size() == 1) {
                    if (TextUtils.isEmpty(addressList.get(0).getPhone())) {
                        Account account = mMessageModule.getAccountByAddress(addressList.get(0));
                        if (null == account || TextUtils.isEmpty(account.getPhone())) {
                            break;
                        }
                        addressList.get(0).setPhone(account.getPhone());
                    }
                    Uri uri = Uri.parse("tel:" + addressList.get(0).getPhone());
                    Intent it = new Intent(Intent.ACTION_CALL, uri);
                    startActivity(it);

                }
            }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Menu Key后的选择menu的创建
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_QUIT, 0, R.string.quit).setIcon(R.drawable.menu_quit);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Menu Key后的选择menu处理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_MULTI_DELETE_THREADS:
                SLog.d(TAG, "goto multi Delete activity");
                Intent intent = new Intent(this, MessageMultiDeleteListActivity.class);
                startActivityForResult(intent, MENU_MULTI_DELETE_THREADS);
                break;
            case MENU_QUIT:
                showQuitDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 删除会话
     */
    private void deleteThreads(Threads threads) {
        if (threads == null || threads.getId() < 0) {
            return;
        }
        SLog.i(TAG, "deleteThreads start = " + threads.getId());
        mMessageModule.removeThreads(threads);
        updateList();
        deleteStrangerData(threads);
        mService.notifyObservers(CoreServiceMSG.MSG_THREADS_DELETE_END, null);
        SLog.i(TAG, "deleteThreads end = " + threads.getId());
    }

    /**
     * 清空所有会话
     */
    protected void clearAllSelectThreads() {
        Log.i(TAG, "clearAllThreads start");
        mHandler.sendMessage(Message
                .obtain(mHandler, CoreServiceMSG.MSG_THREADS_CLEARALL_BEGIN, 0, 0, null));
        List<Threads> list = mMsgAdapter.getSelectList();
        // mMessageModule.removeThreads(list);
        int total = list.size();
        for (int i = 0; i < total; i++) {
            mMessageModule.removeThreads(list.get(i));
            mHandler.sendMessage(Message
                    .obtain(mHandler, CoreServiceMSG.MSG_THREADS_CLEARALL, total, i,
                            null));
        }
        mHandler.sendMessage(Message
                .obtain(mHandler, CoreServiceMSG.MSG_THREADS_CLEARALL_END, 0, 0, null));
        mService.notifyObservers(CoreServiceMSG.MSG_THREADS_DELETE_END, null);
        Log.i(TAG, "clearAllThreads end");
    }

    /**
     * 网络广播通知最终会通知到此类用于控制消息会话列表中网络状态提示
     * 
     * @param isContect
     */
    private void netStatusChanged(final boolean isContect) {
        Log.i(TAG, "netStatusChanged isConnected:" + isContect);
        if (isContect) {
            if (View.GONE != mLayoutNet.getVisibility()) {
                mLayoutNet.setVisibility(View.GONE);
            }
        } else {
            if (View.VISIBLE != mLayoutNet.getVisibility()) {
                mLayoutNet.setVisibility(View.VISIBLE);
            }
            int type = AndroidSysUtils.getNetworkType(MainApp.i()
                    .getApplicationContext());
            TextView tv = (TextView) mLayoutNet.findViewById(R.id.net_status_tv);
            ImageView iv = (ImageView) mLayoutNet.findViewById(R.id.net_status_iv);
            ProgressBar progressBar = (ProgressBar) mLayoutNet.findViewById(R.id.net_progressbar);
            if (type != -1) {
                tv.setText(R.string.net_reconnect);
                tv.setTextColor(getResources().getColor(R.color.black));
                iv.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                tv.setText(R.string.net_unconnect);
                tv.setTextColor(getResources().getColor(R.color.red));
                iv.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private final static int PROCESS_MESSAGE = 0x100;
    private final long delayMillis = 300;
    private int mCurMaxProcess = 0; // 当前进度的最大值
    private int mCurMinProcess = 0; // 当前进度的最小值

    private void updateCurMaxProcess(int process) {
        mCurMinProcess = mCurMaxProcess; // 保存上次的最大值（即现在进度条的起始点）
        if (process > mCurMaxProcess) {
            mCurMaxProcess = process;
        }
    }

    private final Handler processHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROCESS_MESSAGE: {
                    if (mCurMinProcess < 100) {
                        processHandler.sendMessageDelayed(
                                processHandler.obtainMessage(PROCESS_MESSAGE), delayMillis);
                    }
                    int curProcess = mSyncProgress.getProgress() + 1;
                    if (curProcess < mCurMinProcess) {
                        curProcess = mCurMinProcess;
                    }
                    if (curProcess <= mCurMaxProcess && mSyncProgress != null) {
                        mSyncProgress.setProgress(curProcess);
                    }

                    // 修改同步到标题
                    if (curProcess >= Constants.SYNC_PROCESS_CONTACTS_BEGIN) {
                        if (mSyncTip != null) {
                            mSyncTip.setText(R.string.message_list_sync_contacts);
                        }
                    }
                }
                    break;
                default:
                    break;
            }
        }
    };
    private ProgressDialog multiDeleteDialog = null;
    // 第一次启动时消息同步进度条
    private ProgressBar mSyncProgress = null;
    private TextView mSyncTip = null;

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_PROCEESS: {
                multiDeleteDialog = new ProgressDialog(this);
                multiDeleteDialog.setTitle(R.string.tip);
                multiDeleteDialog.setMessage(getString(R.string.message_list_dialog_wait));
                multiDeleteDialog.setIndeterminate(false);
                multiDeleteDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                multiDeleteDialog.setCancelable(false);
                multiDeleteDialog.setMax(mMsgAdapter.getSelectList().size());
                multiDeleteDialog.setProgress(0);
                dialog = multiDeleteDialog;
            }
                break;
            case DIALOG_CLEAR_ALL: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle(R.string.tip);
                builder.setMessage(R.string.message_list_dialog_content);
                builder.setPositiveButton(
                        getResources().getString(R.string.message_list_btn_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                new Thread() {
                                    @Override
                                    public void run() {
                                        clearAllSelectThreads();
                                    }
                                }.start();
                            }
                        });
                builder.setNegativeButton(getResources()
                        .getString(R.string.message_list_btn_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }

                        });
                dialog = builder.create();
                dialog.setCancelable(false);

            }
                break;
            case DIALOG_DELETE_SMS_THREARDS: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle(R.string.tip);
                builder.setMessage(R.string.message_list_dialog_content);
                builder.setPositiveButton(
                        getResources().getString(R.string.message_list_btn_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                deleteThreads(mSelectThreads);
                            }
                        });
                builder.setNegativeButton(getResources()
                        .getString(R.string.message_list_btn_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }

                        });
                dialog = builder.create();
                dialog.setCancelable(false);

            }
                break;

            case DIALOG_SYNC: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View view = LayoutInflater.from(this).inflate(R.layout.sync_progressbar, null);
                mSyncProgress = (ProgressBar) view.findViewById(R.id.sync_progressbar);
                mSyncTip = (TextView) view.findViewById(R.id.sync_progressbar_tip);

                dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
                dialog.getWindow().setContentView(view);
            }
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_SYNC: {
                mSyncTip.setText(R.string.message_list_sync_sms);
                processHandler.sendMessageDelayed(
                        processHandler.obtainMessage(PROCESS_MESSAGE), delayMillis);
            }
                break;
        }
        super.onPrepareDialog(id, dialog);
    }

    private void syncWaitDialog() {
        if ((MainApp.i().getStatusSyncThreads()
                && CommonPreferences.getSyncThreadsCount() < 1)
                || (MainApp.i().isSyncContacts()
                && CommonPreferences.getSyncContactsCount() < 1)) {
            showDialog(DIALOG_SYNC);
        }
    }

    @Override
    public void onBackPressed() {
        switchToHome();
    }

    @Override
    public void initTopBar() {
        setTopBarTitle(R.string.message_list_title);
        // 最好将类中的action使用起来
        OnClickListener click = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageListActivity.this,
                        ContactForMessageListActivity.class);
                startActivity(intent);
            }
        };
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTI, R.drawable.message_list_new, click);

        ArrayList<String> mList = new ArrayList<String>();
        mList.add(getString(R.string.message_multi_delete_threads));
        final PopupMenuAdapter rightMenuAapter = new PopupMenuAdapter(this, mList);
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_option,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRightPopupMenu(MessageListActivity.this, rightMenuAapter,
                                findViewById(R.id.topbar_imageButton_rightII),
                                new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                                        Intent intent = new Intent(MessageListActivity.this,
                                                MessageMultiDeleteListActivity.class);
                                        startActivity(intent);
                                        dismissPopupMenu();
                                    }
                                });
                    }
                });
    }
}
