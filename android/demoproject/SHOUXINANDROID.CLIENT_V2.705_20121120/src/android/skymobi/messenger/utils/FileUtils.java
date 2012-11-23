
package android.skymobi.messenger.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @ClassName: BitmapUtils
 * @Description: TODO
 * @author Lv.Lv
 * @date 2012-3-20 下午3:39:41
 */
public class FileUtils {

    public static byte[] Bitmap2Bytes(Bitmap bmp) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            bytes = stream.toByteArray();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static boolean SaveBitmap2File(Bitmap bmp, String filepath) {
        if (bmp == null || filepath == null)
            return false;

        OutputStream stream = null;
        try {
            File file = new File(filepath);
            File dir = new File(file.getParent());
            if (!dir.exists())
                dir.mkdirs();
            if (file.exists())
                file.delete();

            stream = new FileOutputStream(filepath);

            if (bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream)) {
                stream.flush();
                stream.close();
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static Bitmap Bytes2Bitmap(byte[] body) {
        if (body != null && body.length > 0)
            return BitmapFactory.decodeByteArray(body, 0, body.length);
        else
            return null;
    }

    public static Bitmap getBitmapWithOrientation(ContentResolver cr, Uri url, int maxNumOfPixels) {
        Bitmap bitmap = null;
        ImageItem item = null;

        String targetScheme = url.getScheme();
        if (targetScheme.equals("content")) {
            item = createImageItemFromUri(cr, url);
        } else if (targetScheme.equals("file")) {
            item = new ImageItem();
            item.path = url.getPath();
            item.orientation = getExifOrientation(item.path);
        }

        if (item != null) {
            bitmap = getBitmap(item.path, maxNumOfPixels);
            bitmap = rotate(bitmap, item.orientation);
        }
        return bitmap;
    }

    public static ImageItem createImageItemFromUri(ContentResolver cr, Uri target) {
        ImageItem item = null;
        long id = 0;
        try {
            id = ContentUris.parseId(target);
        } catch (NumberFormatException e1) {
            e1.printStackTrace();
            return null;
        }
        String whereClause = Images.ImageColumns._ID + "=" + Long.toString(id);
        try {
            final Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
            final String[] PROJECTION_IMAGES = new String[]
            {
                    Images.ImageColumns.DATA,
                    Images.ImageColumns.ORIENTATION
            };

            Cursor cursor = cr.query(uri, PROJECTION_IMAGES, whereClause, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    item = new ImageItem();
                    item.path = cursor.getString(0);
                    item.orientation = cursor.getInt(1);
                }
                cursor.close();
                cursor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    public static int getExifOrientation(String path) {
        int exifOrientation = 0;
        try {
            ExifInterface exif = new ExifInterface(path);
            exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public static Bitmap getBitmap(String filePath, int maxNumOfPixels) {
        Bitmap bitmap = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        opts.inSampleSize = computeSampleSize(opts, -1, maxNumOfPixels);
        opts.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFile(filePath, opts);
        return bitmap;
    }

    // Rotates the bitmap by the specified degree.
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options,
                minSideLength, maxNumOfPixels);
        int roundedSize = 0;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 删除指定的目录下的文件
     * 
     * @param path
     */
    public static void deleteDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File subFile : files) {
                    if (subFile.isDirectory())
                        deleteDir(subFile.getPath());
                    else
                        subFile.delete();
                }
            }
            file.delete();
        }
    }

    public static void deleteDirExceptSpefile(String path, File spefile) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File subFile : files) {
                    if (!(subFile.getName().equals(spefile.getName()))) {
                        subFile.delete();
                    }
                }
            }
        }
    }

    /**
     * @param size in unit of Byte
     * @return
     */
    public static String getFormatSizeMB(int size) {
        final int baseMB = 1024 * 1024;
        float value = size;
        value /= baseMB;

        return String.format("%.02f", value).concat("MB");
    }

    public static class ImageItem {
        public String path;
        public int orientation;
    }
}
