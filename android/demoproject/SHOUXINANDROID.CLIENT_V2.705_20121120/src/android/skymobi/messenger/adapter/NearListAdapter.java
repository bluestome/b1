
package android.skymobi.messenger.adapter;

import java.util.ArrayList;

import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.NearUserInfo;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.HeaderCache;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @ClassName: NearListAdapter
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-6-26 下午1:46:19
 */
public class NearListAdapter extends BaseAdapter {
    private final ArrayList<NearUserInfo> items = new ArrayList<NearUserInfo>();
    private final ArrayList<NearUserInfo> all = new ArrayList<NearUserInfo>();
    private final BaseActivity activity;
    public final static int FILTER_ONLY_FEMALE = 1;
    public final static int FILTER_ONLY_MALE = 2;
    public final static int FILTER_ALL = 3;

    public NearListAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public NearUserInfo getNearUser(int position) {
        return items.get(position);
    }

    public void updateList(ArrayList<NearUserInfo> users) {
        items.clear();
        items.addAll(users);
        all.addAll(items);
        notifyDataSetChanged();
    }

    public void addList(ArrayList<NearUserInfo> users) {
        items.addAll(users);
        all.clear();
        all.addAll(items);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void filter(int filter) {
        items.clear();
        switch (filter) {
            case FILTER_ONLY_FEMALE:
                for (NearUserInfo user : all) {
                    if (user.getUsex().equals(String.valueOf(ContactsColumns.SEX_FEMALE))) {
                        items.add(user);
                    }
                }
                break;
            case FILTER_ONLY_MALE:
                for (NearUserInfo user : all) {
                    if (user.getUsex().equals(String.valueOf(ContactsColumns.SEX_MALE))) {
                        items.add(user);
                    }
                }
                break;
            case FILTER_ALL:
                items.addAll(all);
                break;
        }

        notifyDataSetChanged();
    }

    class Holder {
        public View groupLayout;
        public View infoLayout;
        public TextView displayNameView;// 昵称
        public ImageView genderView;// 性别
        public TextView distanceView;// 距离
        public TextView signatureView;// 签名
        public RelativeLayout signatureGroup;// 签名
        public ImageView headerView;// 头像
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NearUserInfo item = (NearUserInfo) getItem(position);
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.nearuser_list_item,
                    null);
            holder = new Holder();

            holder.displayNameView = (TextView) convertView
                    .findViewById(R.id.nearuser_display_name);
            holder.headerView = (ImageView) convertView.findViewById(R.id.nearuser_item_head);
            holder.signatureView = (TextView)
                    convertView.findViewById(R.id.nearuser_signature);
            holder.signatureGroup = (RelativeLayout)
                    convertView.findViewById(R.id.nearuser_signature_group);
            holder.groupLayout = convertView.findViewById(R.id.nearusr_group);
            holder.genderView = (ImageView) convertView.findViewById(R.id.nearuser_gender);
            holder.distanceView = (TextView) convertView.findViewById(R.id.nearuser_distance);
            convertView.setTag(holder);
        }

        holder = (Holder) convertView.getTag();
        if (item.getUsex().equals(String.valueOf(ContactsColumns.SEX_FEMALE)))
            holder.genderView.setImageResource(R.drawable.female);
        else if (item.getUsex().equals(String.valueOf(ContactsColumns.SEX_MALE)))
            holder.genderView.setImageResource(R.drawable.male);

        if (item.getNearbyUserType() == 0)
            holder.distanceView.setText(CoreService.getInstance().getMessageModule()
                    .getDistanceText(item.getDistance()));
        else
            holder.distanceView.setText(item.getRecommendReason());

        HeaderCache.getInstance().getHeader(item.getImageHead(), item.getNickname(),
                holder.headerView);
        holder.displayNameView.setText(item.getNickname());
        if (!TextUtils.isEmpty(item.getUsignature())) {
            holder.signatureView.setText(item.getUsignature());
            holder.signatureGroup.setVisibility(View.VISIBLE);
        } else {
            holder.signatureGroup.setVisibility(View.GONE);
        }
        return convertView;
    }

}
