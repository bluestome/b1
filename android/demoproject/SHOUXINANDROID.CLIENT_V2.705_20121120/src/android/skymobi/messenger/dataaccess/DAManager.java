package android.skymobi.messenger.dataaccess;

import android.skymobi.common.log.SLog;
import android.skymobi.messenger.dataaccess.auth.AuthDA;
import android.skymobi.messenger.dataaccess.auth.IAuthDA;
import android.skymobi.messenger.dataaccess.lcs.ILcsDA;
import android.skymobi.messenger.dataaccess.lcs.LcsDA;

public class DAManager {

    private static final String TAG = DAManager.class.getSimpleName();
	private static final Object lock = new Object();

	/** 用户认证登录的数据访问 */
	public static final byte DA_USER_AUTH = 0x1;
	private static IAuthDA authDA = null;
    /** LCS数据访问对象 */
    private static ILcsDA lcdDA = null;
	/** 手信联系人数据访问 */
	public static final byte DA_SX_CONTACT = 0x2;
	/** 手信会话消息数据访问 */
	public static final byte DA_SX_MESSAGE = 0x3;
	/** 本地会话消息数据访问 */
	public static final byte DA_LOCAL_MESSAGE = 0x4;
	/** 本地联系人数据访问 */
	public static final byte DA_LOCAL_CONTACT = 0x5;
	/** 本地配置文件数据访问 */
	public static final byte DA_COMMON = 0x6;
	/** 操作好友的数据访问 */
	public static final byte DA_FRIENDS = 0x7;
    /** LCS数据访问 **/
    public static final byte DA_LCS = DA_FRIENDS + 1;

	/**
	 * 获取数据访问接口
	 * @param daFlag DAManager数据访问标识
	 * @return 数据访问接口
	 * 
	 * */
	public static IDA get(byte daFlag) {
		switch (daFlag) {
		case DA_USER_AUTH:
			synchronized (lock) {
				if (authDA == null) {
				    authDA = new AuthDA();
				    SLog.d(TAG, "已实例化AUTH DA接口!");
				}
			}
			return authDA;
            case DA_LCS:
                // 2012-10-24 bluestome.zhang
                synchronized (lock) {
                    if (null == lcdDA) {
                        lcdDA = new LcsDA();
                        SLog.d(TAG, "已实例化LCS DA接口!");
                    }
                }
                return lcdDA;

		}
		SLog.d(TAG, "无法实例化DA接口,未知的daFlag!");
		return null;
	}
	
	

}
