
package android.skymobi.messenger.dataaccess.lcs;

import android.skymobi.messenger.dataaccess.BasicDA;
import android.skymobi.messenger.utils.CommonPreferences;

/**
 * @ClassName: LcsDA
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-10-24 下午04:21:05
 */
public class LcsDA extends BasicDA implements ILcsDA {

    // 文件名
    private static final String MESSENGER_LCS_PREFERENCES = "messenger_lcs_preferences";

    // 消息类型
    private static final String SINGLE_SMS_COUNT = "single_sms_count"; // 传统单聊短信
    private static final String MASS_SMS_COUNT = "mass_sms_count"; // 群发短信
    private static final String MASS_NET_COUNT = "mass_net_count"; // 群发网络消息
    private static final String MASS_MULTI_COUNT = "mass_multi_count"; // 群发混合消息（短信+网络消息）
    private static final String NET_TEXT_COUNT = "net_text_count"; // 网络文本消息
    private static final String NET_CARD_COUNT = "net_card_count"; // 网络名片消息
    private static final String NET_VOICE_COUNT = "net_voice_count"; // 网络语音消息
    private static final String CLICK_BUDDY_COUNT = "click_buddy_count"; // 点击打招呼次数
    private static final String CLICK_LBS_COUNT = "click_lbs_count"; // 点击lbs次数
    private static final String BUDDY_PEOPLE_COUNT = "buddy_people_count"; // 打招呼对象的个数
    private static final String CLICK_FASTCHAT_COUNT = "click_fastchat_count"; // 点击快聊的次数

    @Override
    public void saveSingleSmsCount(int count) {
        saveData(SINGLE_SMS_COUNT, count);
    }

    @Override
    public int getSingleSmsCount() {
        return (Integer) getData(SINGLE_SMS_COUNT, 0);
    }

    // 群发短信
    @Override
    public void saveMassSmsCount(int count) {
        saveData(MASS_SMS_COUNT, count);
    }

    @Override
    public int getMassSmsCount() {
        return (Integer) getData(MASS_SMS_COUNT, 0);
    }

    // 群发网络消息
    @Override
    public void saveMassNetCount(int count) {
        saveData(MASS_NET_COUNT, count);
    }

    @Override
    public int getMassNetCount() {
        return (Integer) getData(MASS_NET_COUNT, 0);
    }

    // 群发混合消息（短信+网络消息）
    @Override
    public void saveMassMULTICount(int count) {
        saveData(MASS_MULTI_COUNT, count);
    }

    @Override
    public int getMassMULTICount() {
        return (Integer) getData(MASS_MULTI_COUNT, 0);
    }

    // 网络文本消息
    @Override
    public void saveNetTextCount(int count) {
        saveData(NET_TEXT_COUNT, count);
    }

    @Override
    public int getNetTextCount() {
        return (Integer) getData(NET_TEXT_COUNT, 0);
    }

    // 网络名片消息
    @Override
    public void saveNetCARDCount(int count) {
        saveData(NET_CARD_COUNT, count);
    }

    @Override
    public int getNetCARDCount() {
        return (Integer) getData(NET_CARD_COUNT, 0);
    }

    // 网络语音消息
    @Override
    public void saveNetVOICECount(int count) {
        saveData(NET_VOICE_COUNT, count);
    }

    @Override
    public int getNetVOICECount() {
        return (Integer) getData(NET_VOICE_COUNT, 0);
    }

    // 点击打招呼次数
    @Override
    public void saveClickBuddyCount(int count) {
        saveData(CLICK_BUDDY_COUNT, count);
    }

    @Override
    public int getClickBuddyCount() {
        return (Integer) getData(CLICK_BUDDY_COUNT, 0);
    }

    // 点击lbs次数
    @Override
    public void saveClickLbsCount(int count) {
        saveData(CLICK_LBS_COUNT, count);
    }

    @Override
    public int getClickLbsCount() {
        return (Integer) getData(CLICK_LBS_COUNT, 0);
    }

    // 打招呼对象的个数
    @Override
    public void saveBuddyPeopleCount(int count) {
        saveData(BUDDY_PEOPLE_COUNT, count);
    }

    @Override
    public int getBuddyPeopleCount() {
        return (Integer) getData(BUDDY_PEOPLE_COUNT, 0);
    }

    // 点击快聊次数
    @Override
    public void saveClickFastChatCount(int count) {
        saveData(CLICK_FASTCHAT_COUNT, count);
    }

    @Override
    public int getClickFastChatCount() {
        return (Integer) getData(CLICK_FASTCHAT_COUNT, 0);
    }

    /**
     * 保存数据
     * 
     * @param key
     * @param status
     */
    private void saveData(String key, Object status) {
        CommonPreferences.saveData(MESSENGER_LCS_PREFERENCES, key, status);
    }

    /**
     * 获取数据
     * 
     * @param key
     * @param defValue
     * @return
     */
    private Object getData(String key, Object defValue) {
        return CommonPreferences.getData(MESSENGER_LCS_PREFERENCES, key, defValue);
    }

    /**
     * 清除所有数据
     */
    @Override
    public void clear() {
        CommonPreferences.clear(MESSENGER_LCS_PREFERENCES);
    }
}
