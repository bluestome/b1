
package android.skymobi.messenger.ui;

import android.os.Bundle;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.widget.DescriptionScrollerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * @ClassName: DescActivity
 * @Description: 介绍页
 * @author Sean.Xie
 * @date 2012-2-7 上午11:21:33
 */
public class DescriptionActivity extends BaseActivity implements OnClickListener {
    private DescriptionScrollerView container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    public void initView() {
        container = new DescriptionScrollerView(this);
        container.setBackgroundResource(R.color.white);

        setContentView(container);

        View first, second, third, forth, five, six;

        first = LayoutInflater.from(this).inflate(R.layout.description, null);
        View imageView = first.findViewById(R.id.descriptionImage);
        imageView.setBackgroundResource(R.drawable.desc1);

        second = LayoutInflater.from(this).inflate(R.layout.description, null);
        imageView = second.findViewById(R.id.descriptionImage);
        imageView.setBackgroundResource(R.drawable.desc2);

        third = LayoutInflater.from(this).inflate(R.layout.description, null);
        imageView = third.findViewById(R.id.descriptionImage);
        imageView.setBackgroundResource(R.drawable.desc3);

        forth = LayoutInflater.from(this).inflate(R.layout.description, null);
        imageView = forth.findViewById(R.id.descriptionImage);
        imageView.setBackgroundResource(R.drawable.desc4);

        five = LayoutInflater.from(this).inflate(R.layout.description, null);
        five.setOnClickListener(this);
        imageView = five.findViewById(R.id.descriptionImage);
        imageView.setBackgroundResource(R.drawable.desc5);
        Button enterBtn1 = (Button) five.findViewById(R.id.desc_enter_btn1);
        enterBtn1.setVisibility(View.VISIBLE);
        enterBtn1.setOnClickListener(this);
        Button enterBtn2 = (Button) five.findViewById(R.id.desc_enter_btn2);
        enterBtn2.setVisibility(View.VISIBLE);
        enterBtn2.setOnClickListener(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);

        container.addView(first, lp);
        container.addView(second, new LinearLayout.LayoutParams(lp));
        container.addView(third, new LinearLayout.LayoutParams(lp));
        container.addView(forth, new LinearLayout.LayoutParams(lp));
        container.addView(five, new LinearLayout.LayoutParams(lp));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.menu_exit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.desc_enter_btn1 || v.getId() == R.id.desc_enter_btn2) {
            // anson.yang 20120920修改
            // 如果获取到当前的versionCode为0,则保存为-1,表示版本不合法
            int currentVersion = MainApp.i().getPi().versionCode == 0 ? -1 : MainApp
                    .i().getPi().versionCode;
            CommonPreferences.setLastDescVerion(currentVersion);
            finish();
            nextPage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
