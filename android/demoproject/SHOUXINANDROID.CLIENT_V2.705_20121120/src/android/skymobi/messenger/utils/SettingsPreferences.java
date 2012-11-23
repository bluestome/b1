
package android.skymobi.messenger.utils;

import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;

/**
 * @ClassName: SettingsPreferences
 * @Description: 设置界面的配置文件
 * @author Michael.Pan
 * @date 2012-3-15 上午10:33:28
 */
public class SettingsPreferences {
    // 文件名
    private static final String MESSENGER_SETTINGS_PREFERENCES = "messenger_settings_preferences";
    private static final String HEADPHOTO = "headphoto"; // 头像
    private static final String SEX = "sex"; // 性别
    private static final String NICKNAME = "nickname"; // 昵称
    private static final String BIRTHDAY = "birthday"; // 生日
    private static final String PROVINCE = "province"; // 省
    private static final String CITY = "city"; // 城市
    private static final String SCHOOL = "school"; // 学校
    private static final String CORPORATION = "corpartion"; // 公司
    private static final String SIGNATURE = "signature"; // 个性签名
    private static final String RECOMMEND = "recommend"; // 是否推荐 true 是 false否
    private static final String SHARELBS = "sharelbs"; // 是否被附近人查看 true 是
                                                       // false否
    private static final String SOUND = "sound"; // 消息提醒是否有声音
    private static final String VIBRATE = "vibarte"; // 消息提醒是否振动
    private static final String MOBILE = "mobile"; // 绑定的手机号
    private static final String BINDSTATUS = "bindstatus"; // 绑定状态
    private static final String BINDMESSAGE = "bindmessage"; // 用于绑定的短信内容
    private static final String SKYID = "skyid"; // 用于记录当前账号
    public static final String Male = "1"; // 性别：男
    public static final String Female = "2"; // 性别：女
    public static final String UNBIND = "0"; // 绑定状态：未绑定
    public static final String BIND_LOCAL = "1"; // 绑定状态：绑定本地
    public static final String BIND_OTHER = "2"; // 绑定状态：绑定其它手机
    
    private static final String RESERVE0 = "reserve0"; // 保留位,hzc@20120919
    
    public static void saveReserve0(String reserve0) {
        saveData(RESERVE0, reserve0);
    }

    public static String getReserve0() {
        return (String) getData(RESERVE0, "");
    }

    // 设置消息提醒是否有声音
    public static void saveSoundStatus(boolean status) {
        saveData(SOUND, status);
    }

    public static boolean getSoundStatus() {
        return (Boolean) getData(SOUND, true);
    }

    // 设置消息提醒是否有振动
    public static void saveVibrateStatus(boolean status) {
        saveData(VIBRATE, status);
    }

    public static boolean getVibrateStatus() {
        return (Boolean) getData(VIBRATE, true);
    }

    // 设置头像
    public static void saveHeadPhoto(String headphoto) {
        saveData(HEADPHOTO, headphoto);
    }

    public static String getHeadPhoto() {
        return (String) getData(HEADPHOTO, "");
    }

    // 设置nickname
    public static void saveNickname(String nickname) {
        saveData(NICKNAME, nickname);
    }

    public static String getNickname() {
        return (String) getData(NICKNAME, "");
    }

    // 设置recommend是否推荐
    public static void saveRecommend(boolean isRecommend) {
        saveData(RECOMMEND, isRecommend);
    }

    public static boolean getRecommend() {
        return (Boolean) getData(RECOMMEND, true);
    }

    // 设置是否被附近人查看
    public static void saveShareLBS(boolean isShareLBS) {
        saveData(SHARELBS, isShareLBS);
    }

    public static boolean getShareLBS() {
        return (Boolean) getData(SHARELBS, false);
    }

    // 设置性别
    public static void saveSex(String sex) {
        saveData(SEX, sex);
    }

    public static String getSex() {
        return (String) getData(SEX, Female);
    }

    // 个性签名
    public static void saveSignature(String signature) {
        saveData(SIGNATURE, signature);
    }

    public static String getSignature() {
        return (String) getData(SIGNATURE, "");
    }

    // 绑定的手机号
    public static void saveMobile(String mobile) {
        saveData(MOBILE, mobile);
    }

    public static String getMobile() {
        return (String) getData(MOBILE, "");
    }

    // SCHOOL
    public static void saveSchool(String school) {
        school = removeNULL(school);
        saveData(SCHOOL, school);
    }

    public static String getSchool() {
        return (String) getData(SCHOOL, "");
    }

    // CORPORATION
    public static void saveCorporation(String corporation) {
        corporation = removeNULL(corporation);
        saveData(CORPORATION, corporation);
    }

    public static String getCorporation() {
        return (String) getData(CORPORATION, "");
    }

    // PROVINCE and CITY
    public static void savePlace(String province, String city) {
        province = removeNULL(province);
        city = removeNULL(city);
        saveData(PROVINCE, province);
        saveData(CITY, city);
    }

    public static String getProvince() {
        return (String) getData(PROVINCE, "");
    }

    public static String getCity() {
        return (String) getData(CITY, "");
    }

    // BIRTHDAY
    public static void saveBirthday(String birthday) {
        saveData(BIRTHDAY, birthday);
    }

    public static String getBirthday() {
        return (String) getData(BIRTHDAY, "");
    }

    public static void saveSKYID(int skyid) {
        saveData(SKYID, skyid);
    }

    public static int getSKYID() {
        return (Integer) getData(SKYID, 0);
    }

    // bind info
    public static void saveBindInfo(String status, String bindmessage) {
        saveData(BINDSTATUS, status);
        saveData(BINDMESSAGE, bindmessage);
        
       // CommonPreferences.saveChangeBindSendSMSTime(UNBIND.equals(status)?-1:0);
    }

    public static String getBindStatus() {
        return (String) getData(BINDSTATUS, BIND_LOCAL);
        
    }

    public static String getBindMessage() {
        return (String) getData(BINDMESSAGE, "");
    }

    /**
     * 清除所有数据
     */
    public static void clear() {
        CommonPreferences.clear(MESSENGER_SETTINGS_PREFERENCES);
    }

    public static void updateUseInfo(NetUserInfo useinfo) {
        // 更新昵称
        String newNickname = useinfo.getPersonnickname();
        if (newNickname.equals("")) {
            newNickname = null;
        }
        String oldNickname = getNickname();
        if (newNickname != null && !newNickname.equals(oldNickname)) {
            saveNickname(newNickname);
        }
        // 更新头像
        String newHead = useinfo.getUuidPortrait();
        String oldHead = getHeadPhoto();
        if (newHead != null && !newHead.equals(oldHead)) {
            saveHeadPhoto(newHead);
        }
        // 更新绑定的手机号
        String newMobile = useinfo.getUmobile();
        String oldMobile = getMobile();
        if (newMobile != null && !newMobile.equals(oldMobile)) {
            saveMobile(newMobile);
        }
        // 更新性别
        String newSex = useinfo.getUsex();
        String oldSex = getSex();
        if (newSex != null && !newSex.equals(oldSex)) {
            saveSex(newSex);
        }
        // 更新签名
        String newSignature = useinfo.getUsignature();
        String oldSignature = getSignature();
        if (newSignature != null && !newSignature.equals(oldSignature)) {
            saveSignature(newSignature);
        }
        // 更新生日
        String newBirthday = useinfo.getUbirthday();
        String oldBirthday = getBirthday();
        if (newBirthday != null && !newBirthday.equals(oldBirthday)) {
            saveBirthday(newBirthday);
        }
        // 更新城市
        String newProvince = useinfo.getUprovince();
        String oldProvince = getProvince();
        String newCity = useinfo.getUcity();
        String oldCity = getCity();
        if ((newProvince != null && !newProvince.equals(oldProvince))
                || (newCity != null && !newCity.equals(oldCity))) {
            savePlace(newProvince, newCity);
        }
        // 更新学校
        String newSchool = useinfo.getUschoolgraduated();
        String oldSchool = getSchool();
        if (newSchool != null && !newSchool.equals(oldSchool)) {
            saveSchool(newSchool);
        }
        // 更新单位
        String newCorporation = useinfo.getUcorporation();
        String oldCorporation = getCorporation();
        if (newCorporation != null && !newCorporation.equals(oldCorporation)) {
            saveCorporation(newCorporation);
        }
    }

    //
    private static void saveData(String key, Object status) {
        CommonPreferences.saveData(MESSENGER_SETTINGS_PREFERENCES, key, status);
    }

    private static Object getData(String key, Object defValue) {
        return CommonPreferences.getData(MESSENGER_SETTINGS_PREFERENCES, key, defValue);
    }

    /**
     * 如果str为空或者值为“null”，返回值为“”，否则返回原str
     * 
     * @param str
     * @return 原str或者“”
     */
    private static String removeNULL(String str) {
        if (str == null || "null".equals(str)) {
            return "";
        }
        return str;
    }
}
