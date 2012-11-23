
package android.skymobi.messenger.bean;

/**
 * @ClassName: ResFile
 * @Description: 资源文件
 * @author Sivan.LV
 * @date 2012-3-30 上午10:32:17
 */
public class ResFile {

    public static final int VERSION = 1;

    private long id;

    /**
     * 文件版本
     */
    private long version;

    /**
     * 文件路径
     */
    private String path;

    /**
     * url
     */
    private String url;

    /**
     * 文件大小
     */
    private int size;

    /**
     * 语音时长
     */
    private int length;

    /**
     * 文件格式
     */
    private String format;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
