
package android.skymobi.messenger.network;

import com.skymobi.android.sx.codec.beans.clientbean.NetBindChangeNotify;

/**
 * 绑定变更监听器
 * 
 * @ClassName: BindChangeListener
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-10-26 上午11:15:16
 */
public interface BindChangeListener {

    void onNotify(NetBindChangeNotify notify);
}
