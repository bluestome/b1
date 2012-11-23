
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.utils.HeaderCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * @ClassName: ContactsGroupSendAdapter
 * @Description: 群发联系人adapter
 * @author Lv.Lv
 * @date 2012-4-6 上午10:54:00
 */
public class ContactsGroupSendAdapter extends BaseAdapter {

    private final Context context;
    private final List<Contact> list;
    private final LayoutInflater mInflater;

    public ContactsGroupSendAdapter(Context context, List<Contact> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Contact getContact(int position) {
        return list.get(position);
    }

    public void update(int position, Contact contact) {
        list.set(position, contact);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contacts_group_send_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.icon_photo);
            holder.text = (TextView) convertView.findViewById(R.id.text_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String displayName = list.get(position).getDisplayname();
        holder.text.setText(displayName);

        HeaderCache.getInstance().getHeader(list.get(position).getPhotoId(),
                list.get(position).getDisplayname(), holder.image);
        return convertView;
    }

    static class ViewHolder {
        TextView text;
        ImageView image;
    }

}
