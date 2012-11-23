
package android.skymobi.messenger.bean;

/**
 * @ClassName: DownloadInfo
 * @Description: 自更新下载信息
 * @author Anson.Yang
 * @date 2012-3-19 下午3:32:26
 */
public class DownloadInfo {
    private int checkInterval;
    private int checkAfterTimes;
    private int fileLength;
    private String md5;
    private byte[] body;
    private int startPos;
    private String feature;
    private String version;

    /**
     * @return the checkInterval
     */
    public int getCheckInterval() {
        return checkInterval;
    }

    /**
     * @param checkInterval the checkInterval to set
     */
    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    /**
     * @return the checkAfterTimes
     */
    public int getCheckAfterTimes() {
        return checkAfterTimes;
    }

    /**
     * @param checkAfterTimes the checkAfterTimes to set
     */
    public void setCheckAfterTimes(int checkAfterTimes) {
        this.checkAfterTimes = checkAfterTimes;
    }

    /**
     * @return the fileLength
     */
    public int getFileLength() {
        return fileLength;
    }

    /**
     * @param fileLength the fileLength to set
     */
    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    /**
     * @return the md5
     */
    public String getMd5() {
        return md5;
    }

    /**
     * @param md5 the md5 to set
     */
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * @return the body
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * @return the startPos
     */
    public int getStartPos() {
        return startPos;
    }

    /**
     * @param startPos the startPos to set
     */
    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    /**
     * @return the feature
     */
    public String getFeature() {
        return feature;
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(String feature) {
        this.feature = feature;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
