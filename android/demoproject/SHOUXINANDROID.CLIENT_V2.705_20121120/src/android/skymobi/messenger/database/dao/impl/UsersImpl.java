
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.skymobi.messenger.bean.User;
import android.skymobi.messenger.database.dao.UsersDAO;
import android.skymobi.messenger.provider.SocialMessenger.UsersColumns;
import android.skymobi.messenger.utils.SettingsPreferences;

/**
 * @ClassName: UsersImpl
 * @author Sean.Xie
 * @date 2012-3-30 下午9:07:24
 */
public class UsersImpl extends BaseImpl implements UsersDAO {

    private int skyId;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param context
     */
    public UsersImpl(Context context) {
        super(context);
    }

    @Override
    public User getUserBySkyID(int skyID) {
        this.skyId = skyID;
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append(UsersColumns._ID).append(",")
                .append(UsersColumns.SKYID).append(",")
                .append(UsersColumns.LAST_FRIEND_TIME).append(" as lastFriendTime,")
                .append(UsersColumns.LAST_UPDATE_TIME).append(" as lastUpdateTime ")
                .append(" from ")
                .append(UsersColumns.TABLE_NAME)
                .append(" where ")
                .append(UsersColumns.SKYID).append("=").append(skyID);
        return queryForObject(User.class, sql.toString());
    }

    @Override
    public long saveOrUpdateUserInfo(User user) {
        ContentValues values = new ContentValues();
        if (user.getLastUpdateTime() != 0)
            values.put(UsersColumns.LAST_UPDATE_TIME, user.getLastUpdateTime());
        if (user.getLastFriendTime() != 0)
            values.put(UsersColumns.LAST_FRIEND_TIME, user.getLastFriendTime());
        values.put(UsersColumns.SKYID, user.getSkyid());
        long result = update(UsersColumns.TABLE_NAME, values, null, null);
        if (result == 0) {
            result = insert(UsersColumns.TABLE_NAME, null, values);
        }
        return result;
    }

    @Override
    public void saveContactLastUpdateTime(long lastTime) {
        if (this.skyId < 1) {
            this.skyId = SettingsPreferences.getSKYID();
        }
        User user = new User();
        user.setSkyid(this.skyId);
        user.setLastUpdateTime(lastTime);
        saveOrUpdateUserInfo(user);
    }

    @Override
    public void saveFriendLastUpdateTime(long lastTime) {
        if (this.skyId < 1) {
            this.skyId = SettingsPreferences.getSKYID();
        }
        User user = new User();
        user.setSkyid(this.skyId);
        user.setLastFriendTime(lastTime);
        saveOrUpdateUserInfo(user);
    }

}
