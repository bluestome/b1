
package android.skymobi.messenger.bean;

import java.io.Serializable;

/**
 * 网络状态对象,用于在网络状态广播和和SERVICE,ACTIVITY之间传递
 * 
 * @author bluestome
 */
public class NetState implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    // 是否连接 true:已连接 false:未连接
    private boolean isConnect = false;

    // 网络类型 0:移动网络 1:WIFI
    private int netType = 1;

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean isConnect) {
        this.isConnect = isConnect;
    }

    public int getNetType() {
        return netType;
    }

    public void setNetType(int netType) {
        this.netType = netType;
    }

}
