
package android.skymobi.messenger.ui;

import android.os.Bundle;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ViewsAdapter;
import android.skymobi.messenger.bean.Traffic;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.database.dao.TrafficDAO;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.FileUtils;
import android.skymobi.messenger.widget.CornerListView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SettingsTrafficActivity
 * @Description: 手信流量统计
 * @author Lv.Lv
 * @date 2012-3-27 上午10:14:46
 */
public class SettingsTrafficActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_traffic);
        init();
    }

    private void init() {
        String date = DateUtil.getCurrentYMDate();
        TrafficDAO trafficDao = DaoFactory.getInstance(mContext).getTrafficDAO();
        Traffic traffic = trafficDao.getSum(date);

        // 标题
        TextView listTitle = (TextView) findViewById(R.id.list_title);
        listTitle.setText(R.string.settings_traffic_current_month);

        LayoutInflater inflater = getLayoutInflater();

        List<View> views = new ArrayList<View>();
        View gprs = inflater.inflate(R.layout.settings_traffic_item, null);
        TextView name = (TextView) gprs.findViewById(R.id.text_traffic_name);
        name.setText(R.string.settings_traffic_gprs);
        TextView value = (TextView) gprs.findViewById(R.id.text_traffic_value);
        value.setText(FileUtils.getFormatSizeMB(traffic.getAppMobile()));
        views.add(gprs);

        View wifi = inflater.inflate(R.layout.settings_traffic_item, null);
        name = (TextView) wifi.findViewById(R.id.text_traffic_name);
        name.setText(R.string.settings_traffic_wifi);
        value = (TextView) wifi.findViewById(R.id.text_traffic_value);
        value.setText(FileUtils.getFormatSizeMB(traffic.getAppWifi()));
        views.add(wifi);

        ViewsAdapter adapter = new ViewsAdapter(views);
        CornerListView lv = (CornerListView) findViewById(R.id.item_traffic);
        lv.setAdapter(adapter);
    }
}
