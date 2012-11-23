package android.skymobi.messenger.database.dao;

import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Stranger;

/**
 * @ClassName: StrangerDAO
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-9-5 下午05:36:24
 */
public interface StrangerDAO {

    
    /**
     * 添加陌生人
     * @param stranger
     * @return
     */
    long addStranger(Stranger stranger);
    
    /**
     * 添加陌生人
     * @param contact 
     * @param nickName 用户昵称
     * @param skyName 帐号
     * @return
     */
    long addStranger(Contact contact,String nickName,String skyName);

    /**
     * 根据skyid查找陌生人
     * @param skyid
     * @return
     */
    Stranger fetch(int skyid);
    
    /**
     * 根据skyid删除联系人
     * @param skyid
     * @return
     */
    boolean delete(int skyid);
    
    /**
     * 更新陌生人
     * @param stranger
     * @return
     */
    boolean update(Stranger stranger);
    
    /**
     * 更新陌生人信息
     * @param contact 联系人对象
     * @param nickName 昵称
     * @param skyName 帐号
     * @return
     */
    boolean update(Contact contact,String nickName,String skyName);
    
    /**
     * 检查是否存在SKYID的陌生人数据
     * @param skyid
     * @return
     */
    boolean checkExistsSkyid(int skyid);
}
