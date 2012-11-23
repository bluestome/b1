
package android.skymobi.messenger.utils;

import java.util.ArrayList;

/**
 * @ClassName: FixedArrayList
 * @Description: 固定大小的链表结构，后面加进来的元素，去掉第一个元素
 * @author Michael.Pan
 * @date 2012-5-29 下午01:49:50
 */
@SuppressWarnings("serial")
public class FixedArrayList {

    @SuppressWarnings("unused")
    private ArrayList<Integer> mList = null;
    private int mCapacity = 0;

    public FixedArrayList(int capacity) {
        mList = new ArrayList<Integer>(capacity);
        mCapacity = capacity;
    }

    // 添加数据后，发现列表大小已经等于capacity, 直接删除掉第0个元素
    public int add(Integer object) {
        mList.add(object);
        if (mList.size() >= mCapacity) {
            mList.remove(0);
        }
        return avg();
    }

    // 中位值平均滤波法（又称防脉冲干扰平均滤波法）
    private int avg() {
        if (mList.size() < 3) {
            return 0;
        }
        int max = 0;
        int min = 0;
        int sum = 0;
        for (Integer integer : mList) {
            int v = integer.intValue();
            sum += v;
            if (v > max) {
                max = v;
            }
            if (v < min) {
                min = v;
            }
        }

        return (sum - max - min) / (mList.size() - 2);
    }
}
