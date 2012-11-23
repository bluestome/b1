
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.module.MessageModule;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.utils.SmileyParser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: MessageListAdapter
 * @Description: 消息列表adapter
 * @author Michael.Pan
 * @date 2012-2-9 下午02:34:53
 */
public class MessageListAdapter extends AbsBaseAdapter {

    protected LayoutInflater mInflater = null;
    protected List<Threads> mList;
    protected final List<Threads> mSelectList = new ArrayList<Threads>();
    protected final SmileyParser mParser = SmileyParser.getInstance();
    protected final Context mContext;
    protected MessageModule mMsgModule = null;

    public MessageListAdapter(Context ctx, LayoutInflater inflater, MessageModule msgModule) {
        mContext = ctx;
        mInflater = inflater;
        mMsgModule = msgModule;
        mList = MessageListCache.getInstance().getMessageList();
    }

    public List<Threads> getSelectList() {
        return mSelectList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateList() {
        mList = MessageListCache.getInstance().getMessageList();
        notifyDataSetChanged();
    }

    public Threads getThreads(int position) {
        if (position < 0 || position > mList.size() - 1)
            return null;
        return mList.get(position);
    }

    public String getDisplayName(int position) {
        return mMsgModule.getDisplayName(mList.get(position), mList.get(position).getAddressList());
    }

    public ArrayList<String> getPhotoIds(int position) {
        return mMsgModule.getPhotoIds(mList.get(position), mList.get(position).getAddressList());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.message_list_item, null);
            holder = new ViewHolder();
            holder.msg_head = (ImageView) convertView.findViewById(R.id.msg_head);
            holder.msg_tip_count = (TextView) convertView.findViewById(R.id.msg_tip_count);
            holder.msg_nickname = (TextView) convertView.findViewById(R.id.msg_nickname);
            holder.msg_lastmsg = (TextView) convertView.findViewById(R.id.msg_lastmsg);
            holder.msg_updatetime = (TextView) convertView.findViewById(R.id.msg_updatetime);
            holder.msg_state = (ImageView) convertView.findViewById(R.id.msg_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Threads curThreads = mList.get(position);
        // 手信启动前收到的未读会话，同步过来后，里面的message还未完成同步，调用getUnreadMessageCount()会返回0
        int unreadCnt = mMsgModule.getUnreadMessageCount(curThreads.getId());
        if (unreadCnt > 0 && MessagesColumns.READ_NO == curThreads.getRead()) {
            holder.msg_tip_count.setVisibility(View.VISIBLE);
            holder.msg_tip_count.setText(AndroidSysUtils.getUnreadCountStr(unreadCnt));
        } else {
            holder.msg_tip_count.setVisibility(View.GONE);
        }

        String displayName = getDisplayName(position);
        holder.msg_nickname.setText(displayName);

        ArrayList<String> ids = getPhotoIds(position);
        String photoId = null;
        if (ids != null && ids.size() > 1) {
            photoId = Constants.DEFAULT_MULTI_HEAD;
        } else if (ids != null && ids.size() == 1) {
            photoId = ids.get(0);
        } else {
            photoId = null;
        }
        HeaderCache.getInstance().getHeader(photoId, displayName, holder.msg_head);
        holder.msg_lastmsg.setText(mParser.addSmileySpans(curThreads.getContent()));
        holder.msg_updatetime.setText(DateUtil.formatTimeForMessageList(mContext,
                mList.get(position).getDate()));
        if (curThreads.getStatus() == MessagesColumns.STATUS_SENDING) {
            holder.msg_state.setImageResource(R.drawable.send_ing);
            holder.msg_state.setVisibility(View.VISIBLE);
        } else if (curThreads.getStatus() == MessagesColumns.STATUS_FAILED) {
            holder.msg_state.setImageResource(R.drawable.send_fail);
            holder.msg_state.setVisibility(View.VISIBLE);
        } else {
            holder.msg_state.setImageResource(R.drawable.sendok_state);
            holder.msg_state.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView msg_head;
        TextView msg_tip_count;
        TextView msg_nickname;
        TextView msg_lastmsg;
        TextView msg_updatetime;
        ImageView msg_state;
    }
}
