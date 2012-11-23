
package android.skymobi.messenger.ui;

import android.os.Bundle;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.SettingsListAdapter;
import android.skymobi.messenger.bean.SettingsItem;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.widget.CornerListView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SettingsPrivacyActivity
 * @Description: 隐私设置
 * @author Lv.Lv
 * @date 2012-7-5 上午10:00:29
 */
public class SettingsPrivacyActivity extends TopActivity implements OnItemClickListener {
    private boolean mIsRecommend = true;
    private boolean mIsShareLBS = true;
    private TextView mLbsShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_privacy);
        initTopBar();
        init();
    }

    private void init() {
        mIsRecommend = SettingsPreferences.getRecommend();
        mIsShareLBS = SettingsPreferences.getShareLBS();

        List<SettingsItem> list = new ArrayList<SettingsItem>();
        list.add(new SettingsItem(mContext, R.string.settings_recommendme,
                0, false, true, 0, mIsRecommend));
        list.add(new SettingsItem(mContext, R.string.settings_sharelbs,
                0, false, true, 0, mIsShareLBS));
        SettingsListAdapter adapter = new SettingsListAdapter(mContext, list);
        adapter.setResource(R.layout.settings_item);
        CornerListView lv = (CornerListView) findViewById(R.id.item_privacy);
        lv.setAdapter(adapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);

        mLbsShare = (TextView) findViewById(R.id.nearuser_tip_off);
        mLbsShare.setText(R.string.nearuser_tip_off);
        if (!mIsShareLBS)
            mLbsShare.setVisibility(View.VISIBLE);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.item_privacy) {

            // 是否推荐
            if (position == 0) {
                CheckBox btn = (CheckBox) view.findViewById(R.id.settings_item_checkbox);
                mIsRecommend = !btn.isChecked();
                btn.setChecked(mIsRecommend);
            }
            // 是否被附近人查看
            else if (position == 1) {
                CheckBox btn = (CheckBox) view.findViewById(R.id.settings_item_checkbox);
                mIsShareLBS = !btn.isChecked();
                btn.setChecked(mIsShareLBS);
                if (!mIsShareLBS)
                    mLbsShare.setVisibility(View.VISIBLE);
                else
                    mLbsShare.setVisibility(View.GONE);
            }
        }

    }

    @Override
    protected void onPause() {
        commitChange();

        super.onPause();
    }

    private void commitChange() {
        if (mIsRecommend != SettingsPreferences.getRecommend()
                || mIsShareLBS != SettingsPreferences.getShareLBS()) {
            SettingsModule settingsModule = mService.getSettingsModule();
            settingsModule.setRecommend(mIsRecommend, mIsShareLBS);
        }
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.settings_privacy);
    }
}
