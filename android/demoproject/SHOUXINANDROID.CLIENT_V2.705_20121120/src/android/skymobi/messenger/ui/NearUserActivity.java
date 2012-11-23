
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.NearListAdapter;
import android.skymobi.messenger.adapter.PopupMenuMultiAdapter;
import android.skymobi.messenger.bean.NearUserInfo;
import android.skymobi.messenger.bean.PopupMenuItem;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.NearUserModule;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: NearUserActivity
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-29 下午2:26:15
 */
public class NearUserActivity extends ContactBaseActivity implements OnItemClickListener {
    private static String TAG = NearUserActivity.class.getSimpleName();
    private ListView nearUserList;
    private NearListAdapter nearUserAdapter;
    private LinearLayout mTipGroup;
    private TextView mTip;
    private final static int PAGE_SIZE = 120;
    private NearUserModule mNearUserModule;
    private boolean mInited = false;

    // private int filterType = MainApp.getInstance().getNearFilter();

    private final static int DIALOG_FINDFRIEND_SHARELBS = 1;
    private final static int DIALOG_WAITING = 2;

    private TextView nearUserTypeView;
    private PopupMenuMultiAdapter mAdapter;

    private final Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_NEARUSER_GET: {
                    onGetNearUserDone();
                    MainApp.i().setNearFilter(mNearUserModule.getQuerySex());
                    setTopBarTitle();
                    if (msg.obj != null) {
                        ArrayList<NearUserInfo> nearUsers = (ArrayList<NearUserInfo>) msg.obj;

                        if (nearUsers.size() > 0) {
                            nearUserAdapter.updateList(nearUsers);
                        }
                        else {
                            nearUserAdapter.clear();
                            showTips(R.string.nearuser_getuser_none, true);
                        }
                    }
                }
                    break;
                case CoreServiceMSG.MSG_NET_ERROR:
                    onGetNearUserDone();
                    showToast(getString(R.string.nearuser_get_net_err) + "\r\n["
                                + Constants.ERROR_TIP + ":0x"
                                + StringUtil.autoFixZero(ResultCode.getCode()) + "]");
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearuser);
        try {
            mNearUserModule = mService.getNearUserModule();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
        initView();
        mNearUserModule.initLocation(MainApp.i().getBaiduLocation());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNearUserModule != null)
            mNearUserModule.finish();
    }

    public void initView() {
        nearUserList = (ListView) findViewById(R.id.nearuser_listview);
        mTipGroup = (LinearLayout)
                findViewById(R.id.nearuser_tip_group);
        mTip = (TextView) mTipGroup.findViewById(R.id.nearuser_tip);

        nearUserAdapter = new NearListAdapter(this);
        nearUserList.setAdapter(nearUserAdapter);

        nearUserList.setOnItemClickListener(this);
        initTopBar();
        setTopBarTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // showFilterBoard(false);
        if (!mInited) {
            mInited = true;
            boolean isShareLBS = SettingsPreferences.getShareLBS();
            if (isShareLBS) {
                getNearUsers();
            } else {
                showDialog(DIALOG_FINDFRIEND_SHARELBS);
            }
        }
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        h.sendMessage(Message.obtain(h, what, obj));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick position:" + position);
        NearUserInfo user = nearUserAdapter.getNearUser(position);
        showDetail(user.getSkyId(), null, user.getDistance(), Constants.CONTACT_TYPE_LBS);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tip);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        switch (id) {
            case DIALOG_FINDFRIEND_SHARELBS:
                builder.setMessage(R.string.nearuser_ask_for_share);
                builder.setCancelable(true);
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                builder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                SettingsModule settingsModule = mService.getSettingsModule();
                                settingsModule.setRecommend(SettingsPreferences.getRecommend(),
                                        true);
                                getNearUsers();
                            }
                        });
                break;
            case DIALOG_WAITING:
                ProgressDialog p = new ProgressDialog(this);
                p.setTitle(getString(R.string.tip));
                p.setMessage(getString(R.string.nearuser_waiting));
                p.setIndeterminate(true);
                p.setCancelable(true);
                p.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mNearUserModule.finish();
                    }
                });
                p.show();
                return p;

            default:
                break;
        }
        dialog = builder.create();
        return dialog;
    }

    private void getNearUsers() {
        showDialog(DIALOG_WAITING);
        showTips(0, false);
        // showFilterBoard(false);
        // getLocation();
        mNearUserModule.getLocation();
        // nearUserAdapter.clear();
        // mNearUserModule.getNearUsers(1, PAGE_SIZE, filterType);
    }

    private void setTopBarTitle() {
        int filter = MainApp.i().getNearFilter();
        switch (filter) {
            case ContactsColumns.SEX_UNKNOW:
                nearUserTypeView.setText(R.string.nearuser_title);
                break;
            case ContactsColumns.SEX_MALE:
                nearUserTypeView.setText(R.string.nearuser_title_male);
                break;
            case ContactsColumns.SEX_FEMALE:
                nearUserTypeView.setText(R.string.nearuser_title_female);
                break;
        }
    }

    private void showTips(int resId, boolean bVisible) {
        if (bVisible) {
            mTip.setText(resId);
            mTipGroup.setVisibility(View.VISIBLE);
        } else {
            mTipGroup.setVisibility(View.GONE);
        }
    }

    private void onGetNearUserDone() {
        mNearUserModule.reset();
        removeDialog(DIALOG_WAITING);
    }

    @Override
    public void initTopBar() {
        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        };
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        nearUserTypeView = (TextView) getTopBarView(TOPBAR_RELATIVELAYOUT_LEFTI, 0, l,
                R.id.topbat_textview_leftI);
        nearUserTypeView.setText(R.string.nearuser_title);

    }

    /**
     * @param mList
     */
    private void initList(List<PopupMenuItem> mList) {
        PopupMenuItem item = new PopupMenuItem();
        item.setResId(R.drawable.select_female);
        item.setText(getString(R.string.nearuser_select_female));
        mList.add(item);
        item = new PopupMenuItem();
        item.setResId(R.drawable.select_male);
        item.setText(getString(R.string.nearuser_select_male));
        mList.add(item);
        item = new PopupMenuItem();
        item.setResId(R.drawable.select_all);
        item.setText(getString(R.string.nearuser_select_all));
        mList.add(item);
    }

    private void showPopupMenu() {
        final List<PopupMenuItem> mList = new ArrayList<PopupMenuItem>();
        initList(mList);
        mAdapter = new PopupMenuMultiAdapter(NearUserActivity.this, mList);
        showLeftPopupMenu(NearUserActivity.this, mAdapter,
                findViewById(R.id.topbar_contact_select), new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
                        dismissPopupMenu();
                        PopupMenuItem item = mList.get(position);
                        if (null != item) {
                            switch (item.getResId()) {
                                case R.drawable.select_female:
                                    mNearUserModule.setQuerySex(ContactsColumns.SEX_FEMALE);
                                    break;
                                case R.drawable.select_male:
                                    mNearUserModule.setQuerySex(ContactsColumns.SEX_MALE);
                                    break;
                                case R.drawable.select_all:
                                    mNearUserModule.setQuerySex(ContactsColumns.SEX_UNKNOW);
                                    break;
                                default:
                                    break;
                            }
                            getNearUsers();
                        }
                    }
                });
    }
}
