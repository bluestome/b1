
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.ui.ContactsDetailActivity;
import android.skymobi.messenger.ui.ContactsDetailEditActivity;
import android.skymobi.messenger.ui.ContactsListActivity;
import android.skymobi.messenger.ui.TopActivity;
import android.skymobi.messenger.widget.PinnedHeaderListView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class ContactsListAction extends BaseAction implements OnItemClickListener,
        OnScrollListener, OnClickListener, TextWatcher {

    private static final String TAG = ContactsListAction.class.getSimpleName();
    protected String currentSeacheText = ""; // 当前搜索文字
    private View pinnedHeader;

    public static final String SEARCH_ITEMS = "SEARCH_ITEMS";
    private boolean isOnlineType = false;
    private boolean mPreOnlineType = false;

    private ListView mContactsListView = null;

    public ContactsListAction(TopActivity activity) {
        super(activity);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent intent = new Intent(activity, ContactsDetailActivity.class);
        intent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                parent.getAdapter().getItemId(position));
        intent.putExtra(ContactsDetailEditActivity.ACTION_TYPE,
                ContactsDetailEditActivity.ACTION_NEW);
        activity.startActivityForResult(intent, ContactsDetailEditActivity.CONTACT_EDIT);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.topbar_imageButton_rightI:
                Intent intent = new Intent(activity,
                        ContactsDetailEditActivity.class);
                intent.putExtra(ContactsDetailEditActivity.ACTION_TYPE,
                        ContactsDetailEditActivity.ACTION_NEW);
                activity.startActivityForResult(intent,
                        ContactsDetailEditActivity.CONTACT_NEW);
                break;
            case R.id.topbar_imageButton_rightII:
                if (activity instanceof ContactsListActivity) {
                    ((ContactsListActivity) activity).showPopupMenu();
                }
                break;
            case R.id.topbar_linearLayout_leftI:
                if (activity instanceof ContactsListActivity) {
                    ((ContactsListActivity) activity).showLeftPopupMenu();
                }
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (view instanceof PinnedHeaderListView) {
            try {
                ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        SLog.d(TAG, "beforeTextChanged:" + s + ",count:" + count);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        SLog.d(TAG, "afterTextChanged:" + s + ",count:" + count);
        if (TextUtils.isEmpty(s)) {
            showPinnedHeader();
        } else {
            hidePinnedHeader();
        }

        // 区分英文输入键盘的文字变化,但是内容没有变的情况
        showResultList(s.toString().toLowerCase());
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /**
     * 搜索结果
     * 
     * @param context
     */
    public void showResultList(final String context) {
        currentSeacheText = context;
        mPreOnlineType = isOnlineType; // 上次的联系人类别
        CoreService.getInstance().getSyncPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<ContactsListItem> resultList = null;
                    ContactListCache itemsInstance = ContactListCache.getInstance();
                    resultList = itemsInstance.getContactsListWithSearchText(context);

                    SLog.d(TAG, "mPreOnlineType:" + mPreOnlineType + ",isOnlineType:"
                            + isOnlineType);
                    // 如果前一次是不同类别的搜索,搜索结果不会返回,防止刷新到另外一个类别
                    if (currentSeacheText.equals(context) && (mPreOnlineType == isOnlineType)) {
                        mPreOnlineType = isOnlineType;
                        // 返回的resultlist,存在潜在bug:
                        /*
                         * ava.lang.IndexOutOfBoundsException: Invalid index 4,
                         * size is 0; 20120920
                         */
                        activity.notifyObserver(ContactsListActivity.FLASH_DATA, resultList);
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void hidePinnedHeader() {
        if (null != pinnedHeader) {
            pinnedHeader.getBackground().setAlpha(0);
            // pinnedHeader.setVisibility(View.GONE);
        }
        activity.findViewById(R.id.sideBar).setVisibility(View.GONE);
        mContactsListView.setFastScrollEnabled(true);
        mContactsListView.setSmoothScrollbarEnabled(true);
    }

    public void showPinnedHeader() {
        if (null != pinnedHeader) {
            pinnedHeader.getBackground().setAlpha(255);
            // pinnedHeader.setVisibility(View.VISIBLE);
        }
        activity.findViewById(R.id.sideBar).setVisibility(View.VISIBLE);
        mContactsListView.setFastScrollEnabled(false);
        mContactsListView.setSmoothScrollbarEnabled(false);
    }

    /**
     * @param pinnedHeader the pinnedHeader to set
     */
    public void setPinnedHeader(View pinnedHeader) {
        this.pinnedHeader = pinnedHeader;
    }

    /**
     * @param isOnlineType the isOnlineType to set
     */
    public void setOnlineType(boolean isOnlineType) {
        this.isOnlineType = isOnlineType;
    }

    /**
     * @param mContactsListView the mContactsListView to set
     */
    public void setContactsListView(ListView mContactsListView) {
        this.mContactsListView = mContactsListView;
    }
}
