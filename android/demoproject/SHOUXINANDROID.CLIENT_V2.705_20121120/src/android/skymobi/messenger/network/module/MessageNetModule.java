
package android.skymobi.messenger.network.module;

import android.skymobi.app.net.event.ISXListener;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.bean.ResFile;
import android.skymobi.messenger.utils.SettingsPreferences;

import com.skymobi.android.sx.codec.beans.clientbean.NetChatRequest;
import com.skymobi.android.sx.codec.beans.clientbean.NetChatResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetRecommendedMsgNewResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetRecommendedMsgResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetRecommendedMsgTypeResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetResponse;

import java.util.Map;

/**
 * @ClassName: MessageNetModule
 * @author Sean.Xie
 * @date 2012-3-2 上午11:17:34
 */
public class MessageNetModule extends BaseNetModule {

    /**
     * @param netClient
     */
    public MessageNetModule(ISXListener netClient) {
        super(netClient);
    }

    /**
     * 发送文字信息
     * 
     * @param msg
     * @return
     */
    public NetResponse sendChatTextMsg(String destSkyids, String content, String talkReason) {
        NetChatRequest chat = new NetChatRequest();
        chat.setDestSkyids(destSkyids);
        chat.setNickName(SettingsPreferences.getNickname());
        chat.setMsgContent(content);
        chat.setTalkReason(talkReason);
		chat.setChatMsgType((byte)1);
        return netClient.getBiz().sendChatMsg(chat);
    }

    /**
     * 发送普通语音
     * 
     * @param msg
     * @return
     */
    public NetResponse sendChatVoiceMsg(String destSkyids, ResFile file) {
        return sendChatVoiceMsg(destSkyids, file, (byte) 2);
    }

    /**
     * 发送快聊语音消息
     * 
     * @param destSkyids
     * @param file
     * @param chatMsgType
     * @return
     */
    public NetChatResponse setFastChatVoiceMsg(String destSkyids, ResFile file) {
        return sendChatVoiceMsg(destSkyids, file, (byte) 4);
    }

    // 发送带类型的语音 2：普通语音消息；4：快聊语音消息
    // 定义将wiki:http://wiki.sky-mobi.com:8090/pages/viewpage.action?pageId=593115
    private NetChatResponse sendChatVoiceMsg(String destSkyids, ResFile file, byte chatMsgType) {
        NetChatRequest chat = new NetChatRequest();
        chat.setDestSkyids(destSkyids);
        chat.setNickName(SettingsPreferences.getNickname());
        chat.setAudioLen(file.getLength());
        chat.setAudioSize(file.getSize());
        chat.setFormat("amr");
        chat.setMd5(file.getUrl());
        chat.setChatMsgType(chatMsgType);
        SLog.i("fastchat", "chat = " + chat);
        return netClient.getBiz().sendChatMsg(chat);
    }

    /**
     * 发送名片
     */
    public NetResponse sendCardMsg(String destSkyids, Map<String, Object> mapCard) {
        return netClient.getBiz().sendVCard(destSkyids, SettingsPreferences.getNickname(), mapCard);
    }

    /**
     * 获取推荐短信
     * 
     * @param msgTypeID
     * @param start
     * @param page
     * @return
     */
    public NetGetRecommendedMsgResponse getRecommendedMsg(int msgTypeID, int start, int pageSize) {
        return netClient.getBiz().getRecommendedMsg(msgTypeID, start, pageSize);
    }

    /**
     * 根据短信时间戳获取短信类型列表
     * 
     * @param updateTime
     * @return
     */
    public NetGetRecommendedMsgTypeResponse getRecommendMsgType(long updateTime) {
        return netClient.getBiz().getRecommendMsgType(updateTime);
    }

    /**
     * 根据短信类型获取指定短信类型下的短信列表
     * 
     * @param msgType 短信类型ID
     * @param updateTime 短信类型版本号
     * @param start 开始页码
     * @param pageSize 每页数量
     * @param capacity 最大数量
     * @return
     */
    public NetGetRecommendedMsgNewResponse getRecommendMsgNew(int msgType, long updateTime,
            int start, int pageSize, int capacity) {
        return netClient.getBiz()
                .getRecommendMsgNew(msgType, updateTime, start, pageSize, capacity);
    }

}
