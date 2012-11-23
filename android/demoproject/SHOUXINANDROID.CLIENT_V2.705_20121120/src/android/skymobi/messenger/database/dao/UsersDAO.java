
package android.skymobi.messenger.database.dao;

import android.skymobi.messenger.bean.User;

/**
 * @ClassName: CitysDAO
 * @Description: 城市数据库操作
 * @author Lv.Lv
 * @date 2012-3-8 下午2:54:59
 */
public interface UsersDAO {

    /**
     * 获取当前登录用户信息
     * 
     * @param skyID
     * @return
     */
    User getUserBySkyID(int skyID);

    /**
     * 保存当前登录用户信息
     * 
     * @param user
     * @return
     */
    long saveOrUpdateUserInfo(User user);

    /**
     * 修改最后同步联系人时间
     * 
     * @param lastTime
     */
    void saveContactLastUpdateTime(long lastTime);

    /**
     * 修改最后找朋友时间
     * 
     * @param lastTime
     */
    void saveFriendLastUpdateTime(long lastTime);
}
