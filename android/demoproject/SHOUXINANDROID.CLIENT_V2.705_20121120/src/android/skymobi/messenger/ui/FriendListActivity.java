
package android.skymobi.messenger.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.adapter.FriendListAdapter;
import android.skymobi.messenger.bean.Friend;
import android.skymobi.messenger.bean.User;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.FriendListAction;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * @ClassName: FriendListActivity
 * @Description: 推荐好友列表
 * @author Anson.Yang
 * @date 2012-2-28 下午8:53:00
 */
public class FriendListActivity extends TopActivity {

    private final static String TAG = FriendListActivity.class.getSimpleName();
    // 好友列表
    private ListView mFriendsListView = null;
    // action
    private FriendListAction mAction = null;
    // adapter
    private FriendListAdapter mAdapter = null;
    // dialog
    private final static int FRIEND_SYNCING = 1;
    // 最后一次获取好友列表的时间
    private long lastUpdate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);
        initTopBar();
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        mService.getFriendModule().getFriends();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        mAction = new FriendListAction(this);
        User user = DaoFactory.getInstance(MainApp.i()).getUsersDAO()
                .getUserBySkyID(SettingsPreferences.getSKYID());
        if (null != user) {
            lastUpdate = user.getLastFriendTime();
        }
        showDialog(FRIEND_SYNCING);
        mService.getFriendModule().getFriends(lastUpdate);
        mAdapter = new FriendListAdapter(this);
        mFriendsListView = (ListView) findViewById(R.id.friend_listview);
        mFriendsListView.setAdapter(mAdapter);
        mFriendsListView.setOnItemClickListener(mAction);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_FRIENDS_GET_LIST:
                    removeDialog(FRIEND_SYNCING);
                    ArrayList<Friend> friends = (ArrayList<Friend>) msg.obj;
                    notifyDataChange(friends);
                    if (null == friends || friends.isEmpty()) {
                        findViewById(R.id.friend_null_listview).setVisibility(View.VISIBLE);
                    }
                    break;
                case CoreServiceMSG.MSG_FRIENDS_GET_FROMDB_LIST:
                    ArrayList<Friend> friends1 = (ArrayList<Friend>) msg.obj;
                    notifyDataChange(friends1);
                    break;
            }
        }
    };

    /**
     * @param obj
     */
    protected void notifyDataChange(ArrayList<Friend> friends) {
        if (null != friends && !friends.isEmpty()) {
            findViewById(R.id.friend_null_listview).setVisibility(View.GONE);
            ArrayList<ContactsListItem> items = new ArrayList<ContactsListItem>();
            for (Friend friend : friends) {
                ContactsListItem item = new ContactsListItem();
                item.setDisplayname(friend.getNickName());
                item.setId(friend.getId());
                item.setContactID(friend.getContactId());
                item.setSignature(friend.getRecommendReason());
                item.setPhotoId(friend.getPhotoId());
                if (TextUtils.isEmpty(item.getSignature())) {
                    item.setSignature(friend.getDetailReason());
                }
                items.add(item);
                mAdapter.setItems(items);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        super.notifyObserver(what, obj);
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case FRIEND_SYNCING:
                return showProgressDialog(getString(R.string.friend_list_syncing));
        }
        return null;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.friend_know);
    }
}
