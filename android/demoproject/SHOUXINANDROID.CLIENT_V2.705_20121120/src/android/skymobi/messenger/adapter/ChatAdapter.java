
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Friend;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.module.FriendModule;
import android.skymobi.messenger.service.module.MessageModule;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.SmileyParser;
import android.skymobi.messenger.widget.DrawClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skymobi.android.sx.codec.beans.clientbean.NetVCardNotify;
import com.skymobi.android.sx.codec.beans.common.VCardContent;
import com.skymobi.android.sx.codec.util.ParserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ChatAdapter
 * @author Michael.Pan
 * @date 2012-2-10 下午03:25:54
 */
public class ChatAdapter extends BaseAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final SmileyParser mParser = SmileyParser.getInstance();
    private final List<Message> mMsgList = new ArrayList<Message>();
    private String mMyPhotoID = "";
    private MessageModule mMsgModule = null;
    private FriendModule mFrdModule = null;
    private String mYourDisplayName = null;
    private String mYourPhotoID = "";
    // 语音各项坐标，长度值
    private final int headWidth;
    private final int scrWidth;
    private final int maxWidth;
    private final int minWidth;
    private final int addWdith;
    private final static int VIEW_TYPE_FROM_TEXT = 0;
    private final static int VIEW_TYPE_FROM_VOICE = 1;
    private final static int VIEW_TYPE_FROM_CARD = 2;
    private final static int VIEW_TYPE_FROM_FRD = 3;
    private final static int VIEW_TYPE_TO_TEXT = 4;
    private final static int VIEW_TYPE_TO_VOICE = 5;
    private final static int VIEW_TYPE_TO_CARD = 6;
    private final static int VIEW_TYPE_MAX = 7;

    public ChatAdapter(Context ctx, LayoutInflater inflater, String displayName, String photoId) {
        mContext = ctx;
        mInflater = inflater;
        mYourDisplayName = displayName;
        mYourPhotoID = photoId;
        headWidth = 55;
        scrWidth = AndroidSysUtils.getScreenWidthForDip(mContext);
        maxWidth = scrWidth - 2 * headWidth;
        minWidth = headWidth;
        addWdith = (maxWidth - minWidth) / 59;

        mMyPhotoID = SettingsPreferences.getHeadPhoto();

        mMsgModule = CoreService.getInstance().getMessageModule();
        mFrdModule = CoreService.getInstance().getFriendModule();
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setPhotoID(String photoID) {
        if (!TextUtils.isEmpty(photoID)) {
            mYourPhotoID = photoID;
            notifyDataSetChanged();
        }
    }

    public void updateList(List<Message> list) {
        mMsgList.clear();
        mMsgList.addAll(list);
        // notifyDataSetChanged();
    }

    public void addToList(Message newMsg) {
        mMsgList.add(newMsg);
        notifyDataSetChanged();
    }

    public void addToList(List<Message> list) {
        mMsgList.addAll(0, list);
        // notifyDataSetChanged();
    }

    public Message getThreads(int position) {
        if (position < 0 || position > mMsgList.size() - 1)
            return null;
        return mMsgList.get(position);
    }

    class ViewHolderNormal {
        TextView chatTime;
        ImageView head;
        TextView content;
    }

    class ViewHolderToNormal extends ViewHolderNormal {
        DrawClock sending;
        ImageView status;
    }

    class ViewHolderVoiceNormal {
        ImageView voicePlay;
        TextView voiceLength;
        TextView content;
    }

    class ViewHolderCardNormal {
        TextView cardName;
        TextView cardContent;
        TextView cardValue;
    }

    class ViewHolderFrdNormal {
        ImageView frd_head;
        TextView frd_reason;
        TextView frd_nickname;
        TextView frd_signature;
        ImageView frd_sex;
    }

    class VH_FromText {
        ViewHolderNormal normal = new ViewHolderNormal();
    }

    class VH_FromVoice {
        ViewHolderNormal normal = new ViewHolderNormal();
        ViewHolderVoiceNormal voiceNormal = new ViewHolderVoiceNormal();
    }

    class VH_FromCard {
        ViewHolderNormal normal = new ViewHolderNormal();
        ViewHolderCardNormal carNormal = new ViewHolderCardNormal();
    }

    class VH_FromFrd {
        ViewHolderNormal normal = new ViewHolderNormal();
        ViewHolderFrdNormal frdNormal = new ViewHolderFrdNormal();
    }

    class VH_ToText {
        ViewHolderToNormal toNormal = new ViewHolderToNormal();
    }

    class VH_ToVoice {
        ViewHolderToNormal toNormal = new ViewHolderToNormal();
        ViewHolderVoiceNormal voiceNormal = new ViewHolderVoiceNormal();
    }

    class VH_ToCard {
        ViewHolderToNormal toNormal = new ViewHolderToNormal();
        ViewHolderCardNormal cardNormal = new ViewHolderCardNormal();
    }

    private void initViewHolderNormal(ViewHolderNormal viewHolder, View convertView, boolean from) {
        if (from) {
            viewHolder.head = (ImageView) convertView
                    .findViewById(R.id.chat_from_head);
        } else {
            viewHolder.head = (ImageView) convertView
                    .findViewById(R.id.chat_to_head);
        }
        viewHolder.content = (TextView) convertView
                .findViewById(R.id.chat_content);
        viewHolder.chatTime = (TextView)
                convertView.findViewById(R.id.chat_time);
        // return viewHolder;
    }

    private void initToViewHolderNormal(ViewHolderToNormal viewHolder, View convertView,
            boolean from) {
        viewHolder.sending = (DrawClock) convertView.findViewById(R.id.chat_sending_status);
        viewHolder.status = (ImageView) convertView.findViewById(R.id.chat_send_status);
        initViewHolderNormal(viewHolder, convertView, false);
    }

    private void initVoiceView(ViewHolderVoiceNormal viewHolder, View converView) {
        viewHolder.voiceLength = (TextView) converView
                .findViewById(R.id.chat_voice_length);
        viewHolder.content = (TextView) converView
                .findViewById(R.id.chat_content);
    }

    private void initCardView(ViewHolderCardNormal viewHolder, View convertView) {
        viewHolder.cardName = (TextView) convertView
                .findViewById(R.id.chat_card_sub_item_content_name_value);
        viewHolder.cardContent = (TextView) convertView
                .findViewById(R.id.chat_card_sub_item_content_contact);
        viewHolder.cardValue = (TextView) convertView
                .findViewById(R.id.chat_card_sub_item_content_contact_value);
    }

    private void initFrdView(ViewHolderFrdNormal viewHolder, View convertView) {
        viewHolder.frd_head = (ImageView) convertView.findViewById(R.id.chat_frd_header);
        viewHolder.frd_reason = (TextView) convertView.findViewById(R.id.chat_frd_reason);
        viewHolder.frd_reason = (TextView) convertView.findViewById(R.id.chat_frd_nickname);
        viewHolder.frd_nickname = (TextView) convertView.findViewById(R.id.chat_frd_signature);
        viewHolder.frd_sex = (ImageView) convertView.findViewById(R.id.chat_frd_sex);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH_FromText vh_FromText;
        VH_FromVoice vh_FromVoice;
        VH_FromCard vh_FromCard;
        VH_FromFrd vh_FromFrd;
        VH_ToText vh_ToText;
        VH_ToVoice vh_ToVoice;
        VH_ToCard vh_ToCard;
        int type = getItemViewType(position);

        if (convertView == null) {
            SLog.d(TAG, "-----convertView == null");

            switch (type) {
                case VIEW_TYPE_FROM_TEXT:
                    convertView = mInflater.inflate(R.layout.chat_from_list_text_item,
                            null);
                    vh_FromText = new VH_FromText();
                    initViewHolderNormal(vh_FromText.normal, convertView, true);
                    convertView.setTag(vh_FromText);
                    break;
                case VIEW_TYPE_FROM_VOICE:
                    convertView = mInflater.inflate(R.layout.chat_from_list_voice_item,
                            null);
                    vh_FromVoice = new VH_FromVoice();
                    initViewHolderNormal(vh_FromVoice.normal, convertView, true);
                    initVoiceView(vh_FromVoice.voiceNormal, convertView);
                    convertView.setTag(vh_FromVoice);
                    break;
                case VIEW_TYPE_FROM_CARD:
                    convertView = mInflater.inflate(R.layout.chat_from_list_card_item,
                            null);
                    vh_FromCard = new VH_FromCard();
                    initViewHolderNormal(vh_FromCard.normal, convertView, true);
                    initCardView(vh_FromCard.carNormal, convertView);
                    convertView.setTag(vh_FromCard);
                    break;
                case VIEW_TYPE_FROM_FRD:
                    convertView = mInflater.inflate(R.layout.chat_from_list_frd_item,
                            null);
                    vh_FromFrd = new VH_FromFrd();
                    initViewHolderNormal(vh_FromFrd.normal, convertView, true);
                    initFrdView(vh_FromFrd.frdNormal, convertView);
                    convertView.setTag(vh_FromFrd);
                    break;
                case VIEW_TYPE_TO_TEXT:
                    convertView = mInflater.inflate(R.layout.chat_to_list_text_item,
                            null);
                    vh_ToText = new VH_ToText();
                    initToViewHolderNormal(vh_ToText.toNormal, convertView, false);
                    convertView.setTag(vh_ToText);
                    break;
                case VIEW_TYPE_TO_VOICE: {
                    convertView = mInflater.inflate(R.layout.chat_to_list_voice_item,
                            null);
                    vh_ToVoice = new VH_ToVoice();
                    initToViewHolderNormal(vh_ToVoice.toNormal, convertView, false);
                    initVoiceView(vh_ToVoice.voiceNormal, convertView);
                    convertView.setTag(vh_ToVoice);
                }
                    break;
                case VIEW_TYPE_TO_CARD:
                    convertView = mInflater.inflate(R.layout.chat_to_list_card_item,
                            null);
                    vh_ToCard = new VH_ToCard();
                    initToViewHolderNormal(vh_ToCard.toNormal, convertView, false);
                    initCardView(vh_ToCard.cardNormal, convertView);
                    convertView.setTag(vh_ToCard);
                    break;

            }
        }

        switch (type) {
            case VIEW_TYPE_FROM_TEXT:
                vh_FromText = (VH_FromText) convertView.getTag();
                vh_FromText.normal.content.setText(mParser.addSmileySpans(mMsgList.get(position)
                        .getContent()));
                updateFromNormalView(vh_FromText.normal, position);
                break;
            case VIEW_TYPE_FROM_VOICE:
                vh_FromVoice = (VH_FromVoice) convertView.getTag();
                updateVoiceView(vh_FromVoice.voiceNormal, position);
                updateFromNormalView(vh_FromVoice.normal, position);

                break;
            case VIEW_TYPE_FROM_CARD:
                vh_FromCard = (VH_FromCard) convertView.getTag();
                updateFromNormalView(vh_FromCard.normal, position);
                updateCardView(vh_FromCard.carNormal, position);
                break;
            case VIEW_TYPE_FROM_FRD:
                vh_FromFrd = (VH_FromFrd) convertView.getTag();
                updateFromNormalView(vh_FromFrd.normal, position);
                updateFrdView(vh_FromFrd.frdNormal, position);
                break;
            case VIEW_TYPE_TO_TEXT:
                vh_ToText = (VH_ToText) convertView.getTag();
                vh_ToText.toNormal.content.setText(mParser.addSmileySpans(mMsgList.get(
                        position)
                        .getContent()));
                updateToNormalView(vh_ToText.toNormal, position);
                break;
            case VIEW_TYPE_TO_VOICE: {
                vh_ToVoice = (VH_ToVoice) convertView.getTag();
                updateVoiceView(vh_ToVoice.voiceNormal, position);
                updateToNormalView(vh_ToVoice.toNormal, position);
            }
                break;
            case VIEW_TYPE_TO_CARD:
                vh_ToCard = (VH_ToCard) convertView.getTag();
                updateToNormalView(vh_ToCard.toNormal, position);
                updateCardView(vh_ToCard.cardNormal, position);
                break;

        }
        return convertView;
    }

    private void updateFromNormalView(ViewHolderNormal v, int position) {
        // 更新头像

        HeaderCache.getInstance().getHeader(mYourPhotoID, mYourDisplayName,
                v.head);
        v.head.setOnClickListener((OnClickListener) mContext);
        // 更新时间
        checkTimeTV(v.chatTime, position);
    }

    private void updateToNormalView(ViewHolderToNormal v, int position) {
        // 发送状态更新
        int status = mMsgList.get(position).getStatus();
        if (status == MessagesColumns.STATUS_SENDING) {
            v.status.setVisibility(View.GONE);
            v.sending.setVisibility(View.VISIBLE);
        } else if (status == MessagesColumns.STATUS_FAILED) {
            v.status.setVisibility(View.VISIBLE);
            v.sending.stopAnimation();
            v.sending.setVisibility(View.GONE);
            v.status.setBackgroundResource(R.drawable.send_fail);
        } else {
            v.sending.stopAnimation();
            v.sending.setVisibility(View.GONE);
            if (getMsgType(position) == MessagesColumns.TYPE_SMS) {
                v.status.setVisibility(View.VISIBLE);
                v.status.setBackgroundResource(R.drawable.chat_sms);
            } else {
                v.status.setVisibility(View.GONE);
            }
        }
        // 头像更新
        HeaderCache.getInstance().getHeader(mMyPhotoID, null, v.head);
        v.head.setOnClickListener((OnClickListener) mContext);
        // 时间更新
        checkTimeTV(v.chatTime, position);
    }

    private void updateVoiceView(ViewHolderVoiceNormal viewHolder, int position) {
        Message msg = mMsgList.get(position);
        viewHolder.content.setWidth(AndroidSysUtils.dip2px(mContext,
                getVoiceWidth(msg.getResFile().getLength())));
        viewHolder.voiceLength.setText(msg.getResFile().getLength() + "\"");
    }

    private void updateCardView(ViewHolderCardNormal viewHolder, int position) {
        String content = mMsgList.get(position).getContent();
        Map<String, Object> cardMap = ParserUtils.decoderVCard(content);
        String cardName = (String) cardMap.get(NetVCardNotify.CONTACT_NAME);
        List<VCardContent> cardlist = (List) cardMap
                .get(NetVCardNotify.CONTACT_DETAIL_LIST);

        viewHolder.cardName.setText(cardName);
        int max = Constants.MAX_CARD_LIST;
        if (cardlist.size() == max) {
            String phones = cardlist.get(0).getPhone();
            if (phones != null && !phones.equalsIgnoreCase("")) {
                viewHolder.cardContent
                        .setText(R.string.chat_card_sub_item_content_contact_1);
                viewHolder.cardValue.setText(phones);
            } else {
                viewHolder.cardContent
                        .setText(R.string.chat_card_sub_item_content_contact_2);
                viewHolder.cardValue.setText(cardlist.get(max - 1).getNickname());
            }

        }
    }

    private void updateFrdView(ViewHolderFrdNormal viewHolder, int position) {
        String frdId = mMsgList.get(position).getContent();
        ArrayList<Friend> friends = mFrdModule.getFriendsByIds(frdId);
        if (null != friends && friends.size() > 0) {
            Friend friend = friends.get(0);
            String photoID = friend.getPhotoId();
            HeaderCache.getInstance().getHeader(photoID, friend.getNickName(), viewHolder.frd_head);
            viewHolder.frd_reason.setText(friend.getRecommendReason());

            viewHolder.frd_nickname.setText(friend.getNickName());

            viewHolder.frd_signature.setText(friend.getSignature());

            if (friend.getSex() == 1) {
                viewHolder.frd_sex.setImageResource(R.drawable.male);
            } else {
                viewHolder.frd_sex.setImageResource(R.drawable.female);
            }
        }
    }

    /**
     * 获取该条对话的类型
     * 
     * @return
     */
    public int getMsgType(int pos) {
        return mMsgList.get(pos).getType();
    }

    /**
     * 获取该条对话的id
     * 
     * @param pos
     * @return
     */
    public long getMsgId(int pos) {
        return mMsgList.get(pos).getId();
    }

    /**
     * 获取该条对话的内容
     * 
     * @param pos
     * @return
     */
    public String getMsgContent(int pos) {
        return mMsgList.get(pos).getContent();
    }

    /**
     * 获取消息
     * 
     * @param pos
     * @return
     */
    public Message getMsg(int pos) {
        return mMsgList.get(pos);
    }

    private static final int LIMIT_DELTA_TIME = 5 * 60 * 1000; // 5分钟

    private void checkTimeTV(TextView tv, int position) {
        TextView tvTime = tv;
        if (tv == null) {
            return;
        }
        long lastMsgTime = (position == 0) ? 0 : mMsgList.get(position - 1).getDate();
        long time = mMsgList.get(position).getDate();

        // 5分钟之内就不显示时间
        if (Math.abs(time - lastMsgTime) > LIMIT_DELTA_TIME) {
            tvTime.setVisibility(View.VISIBLE);
            tvTime.setText(DateUtil.formatTimeForChatList(mContext, time));
        } else {
            tvTime.setVisibility(View.GONE);
        }
    }

    public void startVoicePlayAnimation(View itemView) {
        if (itemView == null)
            return;
        ImageView ivVoicePlay = (ImageView) itemView.findViewById(R.id.chat_voice_play);
        if (ivVoicePlay == null)
            return;
        AnimationDrawable anim = null;
        if (itemView.getId() == R.id.chat_from_list_voice_item) {
            anim = (AnimationDrawable) MainApp.i().getResources()
                    .getDrawable(R.drawable.voice_from_image);
        } else {
            anim = (AnimationDrawable) MainApp.i().getResources()
                    .getDrawable(R.drawable.voice_to_image);
        }
        if (anim == null)
            return;
        ivVoicePlay.setBackgroundDrawable(anim);
        anim.start();
    }

    public void stopVoicePlayAnimation(View itemView) {
        if (itemView == null)
            return;
        ImageView ivVoicePlay = (ImageView) itemView.findViewById(R.id.chat_voice_play);
        if (ivVoicePlay == null)
            return;
        if (itemView.getId() == R.id.chat_from_list_voice_item) {
            ivVoicePlay.setBackgroundResource(R.drawable.chat_from_voice_play);
        } else {
            ivVoicePlay.setBackgroundResource(R.drawable.chat_to_voice_play);
        }
    }

    private int getVoiceWidth(int length) {
        int width = minWidth + addWdith
                * length;
        /*
         * int headWidth = AndroidSysUtils.px2dip(mContext, 72); int scrWidthDip
         * = AndroidSysUtils.getScreenWidthForDip(mContext); int maxWidth =
         * scrWidthDip - headWidth;
         */

        return width > maxWidth ? maxWidth : width;
    }

    @Override
    public int getItemViewType(int position) {
        int type = 0;
        if (mMsgList.get(position).getOpt() == MessagesColumns.OPT_FROM) {
            switch (mMsgList.get(position).getType()) {
                case MessagesColumns.TYPE_TEXT:
                case MessagesColumns.TYPE_SMS:
                    type = VIEW_TYPE_FROM_TEXT;
                    break;
                case MessagesColumns.TYPE_VOICE:
                    type = VIEW_TYPE_FROM_VOICE;
                    break;
                case MessagesColumns.TYPE_CARD:
                    type = VIEW_TYPE_FROM_CARD;
                    break;
                case MessagesColumns.TYPE_FRD:
                    type = VIEW_TYPE_FROM_FRD;
                default:
                    break;
            }
        } else {
            switch (mMsgList.get(position).getType()) {
                case MessagesColumns.TYPE_TEXT:
                case MessagesColumns.TYPE_SMS:
                    type = VIEW_TYPE_TO_TEXT;
                    break;
                case MessagesColumns.TYPE_VOICE:
                    type = VIEW_TYPE_TO_VOICE;
                    break;
                case MessagesColumns.TYPE_CARD:
                    type = VIEW_TYPE_TO_CARD;
                    break;
                default:
                    break;
            }
        }
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_MAX;
    }
}
