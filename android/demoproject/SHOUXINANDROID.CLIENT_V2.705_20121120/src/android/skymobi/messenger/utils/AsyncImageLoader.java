
package android.skymobi.messenger.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.skymobi.messenger.service.CoreService;

import java.util.concurrent.ExecutorService;

/**
 * @ClassName: AsyncImageLoader
 * @Description: 异步加载图片的类
 * @author Michael.Pan
 * @date 2012-5-9 下午08:49:26
 */
public class AsyncImageLoader {

    private static final String TAG = AsyncImageLoader.class.getSimpleName();
    private LruCache<String, Bitmap> mImageCache = null;
    private ExecutorService mPool = null;
    // 统计图片头像的命中概率
    private long mImageGetCount = 0; // 图片头像获取次数
    private long mImageHitCount = 0; // 图片头像命中次数

    public AsyncImageLoader(final LruCache<String, Bitmap> imageCache, ExecutorService pool) {
        mImageCache = imageCache;
        mPool = pool;
    }

    public Bitmap loadHeader(final String photoID, final ImageCallback imageCallback) {
        if (!ImageUtils.isImageUrl(photoID)) {
            return null;
        }
        mImageGetCount++;
        if (mImageCache.containsKey(photoID)) {
            Bitmap bm = mImageCache.get(photoID);
            if (bm != null) {
                mImageHitCount++;
                // SLog.d(TAG, "mImageHitCount = " +
                // mImageHitCount
                // + " , mImageGetCount = " + mImageGetCount);
                return bm;
            }
        }
        if (imageCallback != null) {
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    imageCallback.onLoadImage((Bitmap) message.obj, photoID);
                }
            };
            mPool.execute(new Runnable() {

                @Override
                public void run() {
                    // SLog.d(TAG, "解码开始  photoID = " + photoID);
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    Bitmap bm = loadHeaderFromPhotoID(photoID);
                    if (bm != null) {
                        mImageCache.put(photoID, bm);
                        Message message = handler.obtainMessage(0, bm);
                        handler.sendMessage(message);
                    }
                    // SLog.d(TAG, "解码结束  photoID = " + photoID);
                }
            });
        }
        return null;
    }

    public static Bitmap loadHeaderFromPhotoID(String url) {
        CoreService service = CoreService.getInstance();
        if (service != null)
            return service.getSettingsModule().downloadHeader(url);
        else {
            return null;
        }
    }

}
