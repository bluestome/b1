
package android.skymobi.messenger.ui;

import android.os.Bundle;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.text.Html;
import android.widget.TextView;

/**
 * @ClassName: HelpActivity
 * @Description: 帮助界面
 * @author dylan.zhao
 * @date 2012-10-10 下午01:29:02
 */
public class HelpActivity extends TopActivity {

    private static final String TAG = HelpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        TextView help = (TextView) findViewById(R.id.help_text);
        help.setText(Html.fromHtml(getResources().getString(R.string.help_info)));
        initTopBar();
    }

    @Override
    public void initTopBar() {
        SLog.d(TAG, "initTopBar");
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.help);
    }

}
