
package android.skymobi.messenger.adapter;

import java.util.ArrayList;
import java.util.List;

import com.skymobi.android.sx.codec.beans.common.RestorableContacts;

/**
 * @ClassName: ContactsRestoreListCache
 * @Description: 可恢复的联系人列表缓存
 * @author Lv.Lv
 * @date 2012-8-31 下午12:21:56
 */
public class ContactsRestoreListCache {

    private static ContactsRestoreListCache sInstance = null;
    private List<RestorableContacts> mList = new ArrayList<RestorableContacts>();

    public List<RestorableContacts> getList() {
        return mList;
    }

    public void setList(List<RestorableContacts> list) {
        mList = list;
    }

    public static ContactsRestoreListCache getInstance() {
        if (sInstance == null) {
            sInstance = new ContactsRestoreListCache();
        }
        return sInstance;
    }
}
