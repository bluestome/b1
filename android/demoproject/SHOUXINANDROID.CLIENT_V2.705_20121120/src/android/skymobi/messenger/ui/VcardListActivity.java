
package android.skymobi.messenger.ui;

import android.app.Dialog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactsBaseAdapter;
import android.skymobi.messenger.adapter.VcardListAdapter;
import android.skymobi.messenger.bean.Account;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;

/**
 * 类说明： 消息会话列表
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class VcardListActivity extends ContactsListActivity {

    /**
     * 初始化
     */
    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.search_contacts_send_vcard);
    }

    @Override
    protected ContactsBaseAdapter createAdapter() {
        return new VcardListAdapter(this);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        switch (what) {
            case FLASH_DATA:
                super.notifyObserver(what, obj);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.clear();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        Account destAccount = (Account) getIntent()
                .getSerializableExtra("account");
        dialog.setTitle(destAccount.getNickName());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
