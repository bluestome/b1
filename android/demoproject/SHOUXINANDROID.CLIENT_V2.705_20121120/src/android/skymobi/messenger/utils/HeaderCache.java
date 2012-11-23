
package android.skymobi.messenger.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.service.PriorityThreadFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName: HeaderCache
 * @Description: 头像Bitmap缓存
 * @author Michael.Pan
 * @date 2012-2-27 下午02:27:51
 */
public class HeaderCache {

    private static final String TAG = HeaderCache.class.getSimpleName();
    // 文字头像内存的Cache
    private static LruCache<Character, Bitmap> mTextCache;
    // 自定义头像的内存Cache
    private static LruCache<String, Bitmap> mImageCache;
    // 用于存放头像控件的Map
    private static Map<ImageView, String> mImageViews;
    // 网络下载和解码线程池
    private ExecutorService mPool = null;
    // 统计Cache命中率
    private long mTextGetCount = 0; // 文字获取次数
    private long mTextHitCount = 0; // 文字Cache命中次数

    private static Object classLock = HeaderCache.class;
    private static HeaderCache sInstance = null;
    private Bitmap mDefaultHeader = null; // 默认头像
    private Bitmap mDefaultMultiHeader = null; // 默认群发头像
    private Bitmap mHelperHeader = null; // 小助手头像
    private Bitmap mBgBitmap = null; // 背景缓存

    // 异步加载头像类
    protected AsyncImageLoader mImageLoader = null;

    public static HeaderCache getInstance() {
        synchronized (classLock) {
            if (sInstance == null) {
                sInstance = new HeaderCache();
            }
            return sInstance;
        }
    }

    public void getHeader(final String photoId, final String displayName,
            final ImageView imageView) {
        Bitmap cacheBitmap = null;
        if (photoId != null && photoId.equals(Constants.DEFAULT_MULTI_HEAD)) {
            cacheBitmap = mDefaultMultiHeader;
        } else {
            mImageViews.put(imageView, photoId);
            cacheBitmap = loadHeader(photoId,
                    new ImageCallback() {
                        @Override
                        public void onLoadImage(Bitmap bm, String photoID) {
                            String oldPhotoId = mImageViews.get(imageView);
                            if (!TextUtils.isEmpty(oldPhotoId) && oldPhotoId.equals(photoID)) {
                                setImage(imageView, bm, displayName);
                            }
                            // SLog.d(TAG, "oldPhotoId = " + oldPhotoId +
                            // ", photoID = " + photoID);
                        }
                    });
        }
        setImage(imageView, cacheBitmap, displayName);
    }

    /**
     * 设置头像
     * 
     * @param imageView
     * @param bm
     * @param item
     */
    private void setImage(final ImageView imageView, final Bitmap bm, final String displayName) {
        if (bm == null) {
            imageView.setImageBitmap(getTextHeader(displayName));
        } else {
            imageView.setImageBitmap(bm);
        }
    }

    /**
     * 获取文字头像，先从缓存中取头像，取到就返回，取不到再自绘，并保持到cache中
     * 
     * @param displayName
     * @return
     */
    public Bitmap getTextHeader(String displayName) {
        return drawHeader(displayName);
    }

    /*
     * 通过photoID获取头像，先从mImageLoader的cache中取，取到直接返回，取不到异步解码
     */
    public Bitmap loadHeader(String photoID, ImageCallback imageCallback) {
        return mImageLoader.loadHeader(photoID, imageCallback);
    }

    public void clearAll() {
        if (mTextCache != null) {
            mTextCache.clear();
        }
        if (mImageCache != null) {
            mImageCache.clear();
        }
        if (mImageViews != null) {
            mImageViews.clear();
        }
    }

    private HeaderCache() {

        // 计算Cache中存放头像的个数
        int size = getCacheSize();
        SLog.d(TAG, "head cache total size = " + size);
        mTextCache = new LruCache<Character, Bitmap>(size / 2);
        mImageCache = new LruCache<String, Bitmap>(size / 2);
        mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
        PriorityThreadFactory threadFactory = new PriorityThreadFactory("shouxin-headpool",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mPool = Executors.newFixedThreadPool(2, threadFactory); // 固定大小的线程池
        // 小助手头像Bitmap
        mHelperHeader = ImageUtils.getRoundedCornerBitmap(BitmapFactory.decodeResource(
                MainApp.i().getResources(),
                R.drawable.shouxin_helper).copy(Config.ARGB_8888, true));
        // 文字头像背景Bitmap
        mBgBitmap = BitmapFactory.decodeResource(MainApp.i().getResources(),
                R.drawable.default_head_bg);
        // 默认陌生人头像Bitmap
        mDefaultHeader = mergeHeader(R.drawable.default_head, R.drawable.default_head_bg2);
        // 默认群发头像
        mDefaultMultiHeader = mergeHeader(R.drawable.default_multi_head,
                R.drawable.default_head_bg2);
        // 图片头像加载器
        mImageLoader = new AsyncImageLoader(mImageCache, mPool);
    }

    private Bitmap mergeHeader(int srcResID, int bgResID) {
        Bitmap srcHeader = BitmapFactory.decodeResource(MainApp.i().getResources(),
                srcResID).copy(Config.ARGB_8888, true);
        Bitmap bgHeader = BitmapFactory.decodeResource(MainApp.i().getResources(),
                bgResID).copy(Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bgHeader);
        Rect dst = getDrawRect(bgHeader, srcHeader);
        canvas.drawBitmap(srcHeader, null, dst, null);
        return bgHeader;
    }

    private Rect getDrawRect(Bitmap bg, Bitmap src) {
        int left = (bg.getWidth() - src.getWidth()) / 2;
        int top = (bg.getHeight() - src.getHeight()) / 2;
        int right = left + src.getWidth();
        int bottom = top + src.getHeight();
        Rect dst = new Rect(left, top, right, bottom);
        return dst;
    }

    /**
     * 自绘头像
     * 
     * @param displayName
     * @return
     */
    private Bitmap drawHeader(String displayName) {
        Bitmap header = null;
        String regex = ".*[\u4e00-\u9fa5].*";
        if (displayName == null || displayName.equals(""))
            return mDefaultHeader;
        if (displayName.equalsIgnoreCase(Constants.HELPER_NAME)) {
            return mHelperHeader;
        } else if (displayName.matches(regex)) {
            int len = displayName.length();
            char name = '\0';
            for (int i = len - 1; i > -1; i--) {
                name = displayName.charAt(i);
                if (name >= '\u4e00' && name <= '\u9fa5') {
                    break;
                }
            }
            header = getFromCache(name);

            if (header == null) {
                header = mBgBitmap.copy(Config.ARGB_8888, true);
                Canvas canvas = new Canvas(header);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextAlign(Align.CENTER);
                paint.setColor(0xFFFFFFFF);
                paint.setTextSize((int) (MainApp.i().getDeviceInfo().screenHeight * 0.05));
                FontMetrics fontMetrics = paint.getFontMetrics();
                // 计算文字高度
                float fontHeight = fontMetrics.bottom - fontMetrics.top;
                // 计算文字baseline
                float textBaseY = header.getHeight() - (header.getHeight() - fontHeight) / 2
                        - fontMetrics.bottom;
                canvas.drawText(String.valueOf(name), header.getWidth() / 2, textBaseY, paint);
                addToCache(name, header);
            }
        } else {
            header = mDefaultHeader;
        }
        return header;
    }

    // 将汉字头像加入汉字头像缓存中
    private void addToCache(Character name, Bitmap bm) {
        mTextCache.put(name, bm);
    }

    // 在缓存中查找对应汉字的Bitmap缓存
    private Bitmap getFromCache(Character name) {
        // SLog.d(TAG, "getFromCache " + name);
        mTextGetCount++;
        if (name == null || name.equals("") || mTextCache.size() == 0) {
            return null;
        }
        Bitmap bm = null;
        if (mTextCache.containsKey(name)) {
            bm = mTextCache.get(name);
            if (bm != null) {
                mTextHitCount++;
                // SLog.d(TAG, "mTextHitCount = " +
                // mTextHitCount
                // + " , mTextGetCount = " + mTextGetCount);
            }
        }
        return bm;
    }

    private static int getCacheSize() {
        // App可以使用的最大内存的1/16用于缓存头像
        long max = MainApp.i().getMaxMemory() / 16;
        long headsize = Constants.SETTINGS_SMALL_HEAD_WIDTH * Constants.SETTINGS_SMALL_HEAD_WIDTH
                * 4;
        int size = (int) (max * 1024 * 1024 / headsize);
        return Math.max(size, Constants.DEFAULT_MIN_HEAD_CACHE_SIZE);
    }
}
