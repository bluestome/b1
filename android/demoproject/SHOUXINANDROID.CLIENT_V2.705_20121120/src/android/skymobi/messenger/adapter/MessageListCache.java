
package android.skymobi.messenger.adapter;

import android.skymobi.messenger.bean.Threads;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: MessageListCache
 * @Description: 将会话列表缓存起来
 * @author Michael.Pan
 * @date 2012-8-10 下午05:42:57
 */
public class MessageListCache {

    private static MessageListCache sInstance = null;
    private List<Threads> mList = new ArrayList<Threads>();;

    private MessageListCache() {
    }

    public List<Threads> getMessageList() {
        return mList;
    }

    public void UpdateList(List<Threads> list) {
        mList = list;
    }

    public static MessageListCache getInstance() {
        if (sInstance == null) {
            sInstance = new MessageListCache();
        }
        return sInstance;
    }
}
