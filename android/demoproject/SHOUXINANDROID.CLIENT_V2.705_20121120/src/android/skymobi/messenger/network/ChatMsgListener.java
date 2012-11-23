
package android.skymobi.messenger.network;

/**
 * @ClassName: ChatMsgListener
 * @Description: 网络聊天消息监听接口
 * @author Michael.Pan
 * @date 2012-2-20 上午11:28:41
 */
public interface ChatMsgListener {
    // 聊天消息类型
    // 聊天消息类型（0：老版本未传入，默认；1：普通聊天消息；2：普通语音消息；3：加好友成功的聊天消息；4：快聊语音消息；5：图片消息）
    // wiki:http://wiki.sky-mobi.com:8090/pages/viewpage.action?pageId=593115
    public final static int CHAT_MSG_DEFAULT = 0;
    public final static int CHAT_MSG_NORMAL_TEXT = 1;
    public final static int CHAT_MSG_NORMAL_VOICE = 2;
    public final static int CHAT_MSG_ADD_NOTIFY = 3;
    public final static int CHAT_MSG_FASTCHAT_VOICE = 4;
    public final static int CHAT_MSG_PICTURE = 5;
    public void onNotify(int what, Object obj);
}
