
package android.skymobi.messenger.dataaccess.lcs;

import android.skymobi.messenger.dataaccess.IDA;

/**
 * @ClassName: ILcsDA
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-10-24 下午04:11:57
 */
public interface ILcsDA extends IDA {
    /**
     * Data Code定义
     */
    public static final int MSGCODE_DATA_STATISTIC = 1; // 普通统计相关
    public static final int MSGCODE_DATA_LBSSTATIC = 2; // LBS统计相关
    /**
     * MSG_DEST (1:单聊短信, 2:单聊网络消息, 3:群聊纯短信, 4:群聊纯网络消息, 5:群聊混合消息)
     */
    public static final byte MSG_DEST_DEFAULT = 0; // 打招呼或者LBS点击次数
    public static final byte MSG_DEST_SINGLE_SMS = 1; //
    public static final byte MSG_DEST_SINGLE_NET = 2; //
    public static final byte MSG_DEST_MASS_SMS = 3;
    public static final byte MSG_DEST_MASS_NET = 4;
    public static final byte MSG_DEST_MASS_MULTI = 5;

    /**
     * MSG_TYPE (1:文本消息；2:名片；3:语音；4: buddy: 打招呼次数; 5: 使用LBS功能次数(点击附近的人次数); 6:
     * 打招呼对象个数(对几个陌生人打了招呼))
     */
    public static final byte MSG_TYPE_TEXT = 1;
    public static final byte MSG_TYEP_CARD = 2;
    public static final byte MSG_TYPE_VOICE = 3;
    public static final byte MSG_TYPE_CLICK_BUDDY = 4;
    public static final byte MSG_TYPE_CLICK_LBS = 5;
    public static final byte MSG_TYPE_BUDDY_PEOPLE = 6;

    // 传统单聊短信
    void saveSingleSmsCount(int count);

    int getSingleSmsCount();

    // 群发短信
    void saveMassSmsCount(int count);

    int getMassSmsCount();

    // 群发网络消息
    void saveMassNetCount(int count);

    int getMassNetCount();

    // 群发混合消息（短信+网络消息）
    void saveMassMULTICount(int count);

    int getMassMULTICount();

    // 网络文本消息
    void saveNetTextCount(int count);

    int getNetTextCount();

    // 网络名片消息
    void saveNetCARDCount(int count);

    int getNetCARDCount();

    // 网络语音消息
    void saveNetVOICECount(int count);

    int getNetVOICECount();

    // 点击打招呼次数
    void saveClickBuddyCount(int count);

    int getClickBuddyCount();

    // 点击lbs次数
    void saveClickLbsCount(int count);

    int getClickLbsCount();

    // 打招呼对象的个数
    void saveBuddyPeopleCount(int count);

    int getBuddyPeopleCount();

    // 点击快聊次数
    void saveClickFastChatCount(int count);

    int getClickFastChatCount();

    // 清除所有数据
    void clear();

}
