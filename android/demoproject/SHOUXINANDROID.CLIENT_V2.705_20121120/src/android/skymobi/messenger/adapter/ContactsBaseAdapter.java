
package android.skymobi.messenger.adapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.widget.PinnedHeaderListView.PinnedHeaderAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-2-3
 * @version 1.0
 */
public class ContactsBaseAdapter extends BaseAdapter implements SectionIndexer, PinnedHeaderAdapter {

    protected ArrayList<ContactsListItem> items = new ArrayList<ContactsListItem>();
    protected final ArrayList<ContactsListItem> selectedItems = new ArrayList<ContactsBaseAdapter.ContactsListItem>();
    protected final ArrayList<Account> selectedAcconts = new ArrayList<Account>();
    protected final BaseActivity activity;

    public ContactsBaseAdapter(BaseActivity activity) {
        this.activity = activity;
        // 删除联系人 和 查看联系人列表中公用一个list 确保数据一致性 ContactsListAdapter 中一样
        items = ContactListCache.getInstance().getListItems();
    }

    public void setItems(ArrayList<ContactsListItem> children) {
        items = children;
    }

    @Override
    public boolean isEnabled(int position) {
        if (items.size() > position && items.get(position).isGroup()) {// #11222
            return false;
        }
        return super.isEnabled(position);
    }

    class Holder {
        public TextView groupNameView;
        public TextView displayNameView;
        public TextView signatureView;
        public ImageView headerView;

        public View groupLayout;
        public View infoLayout;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        try {
            final ContactsListItem item = items.get(position);
            if (view == null) {
                view = LayoutInflater.from(activity).inflate(R.layout.contacts_list_item_child,
                        null);
                Holder holder = new Holder();
                holder.displayNameView = (TextView) view
                        .findViewById(R.id.contacts_list_item_display_name);
                holder.groupLayout = view.findViewById(R.id.contacts_list_item_group_layout);
                holder.groupNameView = (TextView) view
                        .findViewById(R.id.contacts_list_item_group_name);
                holder.headerView = (ImageView) view.findViewById(R.id.contacts_list_item_head);
                holder.infoLayout = view.findViewById(R.id.contacts_list_item_info_layout);
                holder.signatureView = (TextView) view
                        .findViewById(R.id.contacts_list_item_signature);
                view.setTag(holder);
            }
            final Holder holder = (Holder) view.getTag();
            if (item.isGroup()) {
                holder.groupLayout.setVisibility(View.VISIBLE);
                holder.infoLayout.setVisibility(View.GONE);
                holder.groupNameView.setText(item.getGroupName());
            } else {
                // 分组栏下的列表栏
                holder.groupLayout.setVisibility(View.GONE);
                holder.infoLayout.setVisibility(View.VISIBLE);
                // 昵称
                holder.displayNameView.setText(item.getDisplayname());
                // 签名
                holder.signatureView.setText(item.getSignature());

                // 头像
                HeaderCache.getInstance().getHeader(item.getPhotoId(), item.getDisplayname(),
                        holder.headerView);
            }
            // view.requestLayout();
            // view.invalidate();
            return view;
        } catch (Exception e) {
            e.printStackTrace();
            SystemClock.sleep(300);
            return getView(position, view, parent);
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ContactsListItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    public static class ContactsListItem extends Contact {
        private static final long serialVersionUID = 1L;
        private long contactID;
        /**
         * 组名
         */
        private String groupName;

        /**
         * 是否是分组
         */
        private boolean isGroup;

        private String recommendReason; // 推荐理由

        private int position;

        private int hightlightType;

        private int[] positions;

        public int[] getPositions() {
            return positions;
        }

        public void setPositions(int[] positions) {
            this.positions = positions;
        }

        public int getHightlightType() {
            return hightlightType;
        }

        public void setHightlightType(int hightlightType) {
            this.hightlightType = hightlightType;
        }

        public long getContactID() {
            return contactID;
        }

        public void setContactID(long contactID) {
            this.contactID = contactID;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public boolean isGroup() {
            return isGroup;
        }

        public void setGroup(boolean isGroup) {
            this.isGroup = isGroup;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getRecommendReason() {
            return recommendReason;
        }

        public void setRecommendReason(String recommendReason) {
            this.recommendReason = recommendReason;
        }

    }

    @Override
    public Object[] getSections() {
        return selectedItems.toArray();
    }

    /**
     * 获取选中联系人列表
     * 
     * @return
     */
    public ArrayList<Contact> getSectionsList() {
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        for (ContactsListItem item : selectedItems) {
            if (item.getId() > 0) {
                Contact contact = new Contact();
                contact.setLocalContactId(item.getLocalContactId());
                contact.setCloudId(item.getCloudId());
                contact.setId(item.getId());
                contacts.add(contact);
            }
        }
        return contacts;
    }

    /**
     * 清空选中
     */
    public void clearSections() {
        selectedItems.clear();
        selectedAcconts.clear();
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < items.size(); i++) {
            String pinyin = items.get(i).getGroupName();
            if (pinyin == null || pinyin.length() < 1) {
                continue;
            }
            if ((pinyin.toUpperCase().charAt(0)) == section) {
                return i;
            }
        }
        return -1;
    }

    public ContactsListItem getPositionById(long id) {
        ContactsListItem item = new ContactsListItem();
        item.setPosition(-1);
        for (int i = 0; i < items.size(); i++) {
            long contactId = items.get(i).getId();
            if (contactId == id) {
                item.setGroupName(items.get(i).getGroupName());
                item.setDisplayname(items.get(i).getDisplayname());
                if (i != 0)
                    i--;
                item.setPosition(i);
                return item;
            }
        }
        return item;
    }

    @Override
    public int getSectionForPosition(int position) {
        return position;
    }

    /**
     * 批量删除
     * 
     * @param id
     */
    public void removeItems(ArrayList<Contact> contacts) {
        for (int i = 0; i < contacts.size(); i++) {
            removeItemById(contacts.get(i).getId());
        }
    }

    /**
     * @param contactId
     */
    public void removeItemById(long id) {
        for (int i = 0; i < items.size(); i++) {
            long itemId = items.get(i).getId();
            if (itemId == id) {
                items.remove(i);
                if (i > 0) {
                    if (items.get(i - 1).isGroup()
                            && (i == items.size() || items.get(i).isGroup())) {
                        items.remove(i - 1);
                    }
                }
                break;
            }
        }
    }

    final static class PinnedHeaderCache {
        public TextView titleView;
        public ColorStateList textColor;
        public Drawable background;
    }

    @Override
    public void configurePinnedHeader(View header, int position) {
        if (items.size() == 0) {
            return;
        }
        PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();
        if (cache == null) {
            cache = new PinnedHeaderCache();
            cache.titleView = (TextView) header.findViewById(R.id.contacts_list_item_group_name);
            cache.textColor = cache.titleView.getTextColors();
            cache.background = header.getBackground();
            header.setTag(cache);
        }

        String title = items.get(position).getGroupName();
        cache.titleView.setText(title);

        header.setBackgroundDrawable(cache.background);
        cache.titleView.setTextColor(cache.textColor);
    }

    /**
     * @param accounts
     */
    public void addSelectAccount(ArrayList<Account> accounts) {
        for (Account account : accounts) {
            if (!selectedAcconts.contains(account)) {
                selectedAcconts.add(account);
            }
        }
    }

    /**
     * 通过account获取该联系人的在线状态
     * 
     * @param accounts
     * @return
     */
    protected boolean getUserOnlineStatusByAccounts(ArrayList<Account> accounts) {
        if (accounts == null || accounts.size() <= 0) {
            return false;
        }
        boolean bOnline = false;
        for (Account account : accounts) {
            if (MainApp.i().getUserOnlineStatus(account.getSkyId())) {
                bOnline = true;
                break;
            }
        }
        return bOnline;
    }
}
