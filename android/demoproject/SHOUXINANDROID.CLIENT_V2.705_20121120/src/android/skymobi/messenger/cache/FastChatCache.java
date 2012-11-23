
package android.skymobi.messenger.cache;

import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;

import java.util.ArrayList;

/**
 * @ClassName: FastChatCache
 * @Description: 快聊状态的保持
 * @author Michael.Pan
 * @date 2012-10-18 下午01:54:43
 */
public class FastChatCache {

    // 匹配状态
    private boolean isMatched = false;
    // 匹配时对方的skyid
    private int matchedSkyid = -1;
    // 自己是否已经离开快聊界面，true 离开了 false没有离开
    private boolean isLeave = true;

    // 聊天记录
    private final ArrayList<Message> chatMsgList = new ArrayList<Message>();

    public FastChatCache() {

    }

    // 清除所有状态
    public void clearAll() {
        isMatched = false;
        matchedSkyid = -1;
        chatMsgList.clear();
    }

    // 获取快聊的列表
    public ArrayList<Message> getChatMsg() {
        return chatMsgList;
    }

    // 收到一条快聊消息，
    public void addChatMsg(Message chatMsg) {
        chatMsgList.add(chatMsg);
    }

    // 获取匹配状态
    public boolean isMatched() {
        return isMatched;
    }

    // 设置匹配状态
    public void setMatched(boolean isMatched) {
        this.isMatched = isMatched;
    }

    // 获取匹配的skyid
    public int getMatchedSkyid() {
        return matchedSkyid;
    }

    // 设置匹配的skyid
    public void setMatchedSkyid(int matchedSkyid) {
        this.matchedSkyid = matchedSkyid;
    }

    // 获取 自己是否已经离开快聊界面，true 离开了 false没有离开
    public boolean isLeave() {
        return isLeave;
    }

    // 设置 自己是否已经离开快聊界面，true 离开了 false没有离开
    public void setLeave(boolean isLeave) {
        this.isLeave = isLeave;
    }

    // 查询未读消息条数
    public int getUnreadVoiceCount() {
        int count = 0;
        for (Message msg : chatMsgList) {
            if (msg.getOpt() == MessagesColumns.OPT_FROM
                    && msg.getRead() == MessagesColumns.READ_NO) {
                count++;
            }
        }
        return count;
    }

}
