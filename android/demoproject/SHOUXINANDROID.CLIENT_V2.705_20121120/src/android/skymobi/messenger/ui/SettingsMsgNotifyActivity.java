
package android.skymobi.messenger.ui;

import android.os.Bundle;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.SettingsListAdapter;
import android.skymobi.messenger.bean.SettingsItem;
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
 * @ClassName: SettingsMsgNotifyActivity
 * @Description: 设置消息提醒
 * @author Lv.Lv
 * @date 2012-3-9 下午1:46:23
 */
public class SettingsMsgNotifyActivity extends TopActivity implements OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_msg_notify);
        initTopBar();
        init();
    }

    private void init() {
        List<SettingsItem> list = new ArrayList<SettingsItem>();
        list.add(new SettingsItem(mContext, R.string.settings_notify_sound,
                0, false, true, 0, SettingsPreferences.getSoundStatus()));
        list.add(new SettingsItem(mContext, R.string.settings_notify_vibrate,
                0, false, true, 0, SettingsPreferences.getVibrateStatus()));
        SettingsListAdapter adapter = new SettingsListAdapter(mContext, list);
        adapter.setResource(R.layout.settings_item);
        CornerListView lv = (CornerListView) findViewById(R.id.item_msg_notify);
        lv.setAdapter(adapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.item_msg_notify) {
            // 声音提醒
            if (position == 0) {
                CheckBox btn = (CheckBox) view.findViewById(R.id.settings_item_checkbox);
                btn.setChecked(!btn.isChecked());
                SettingsPreferences.saveSoundStatus(btn.isChecked());
            }
            // 振动提醒
            else if (position == 1) {
                CheckBox btn = (CheckBox) view.findViewById(R.id.settings_item_checkbox);
                btn.setChecked(!btn.isChecked());
                SettingsPreferences.saveVibrateStatus(btn.isChecked());
            }
        }

    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.settings_msg_notify);
    }
}
