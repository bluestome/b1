
package android.skymobi.messenger.comparator;

import android.skymobi.messenger.bean.Contact;

/**
 * @ClassName: CompCallBack
 * @Description: CompCallBack,比较后回调函数，用于列表的比较后处理
 * @author Michael.Pan
 * @date 2012-9-5 下午05:39:47
 */
public interface CompCallBack {

    public void onCmpMore(Contact lhs, Contact rhs); // 左边>右边

    public void onCmpLess(Contact lhs, Contact rhs); // 左边<右边

    public void onCmpEqual(Contact lhs, Contact rhs); // 相等

    public void onCmpLTail(Contact lhs); // 左边尾巴处理

    public void onCmpRTail(Contact rhs); // 右边尾巴处理

}
