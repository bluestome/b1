
package android.skymobi.messenger.bean;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @ClassName: SMSThread
 * @Description: 本地短信会话
 * @author Sean.Xie
 * @date 2012-2-9 上午11:21:17
 */
public class SMSThreads implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;

    /** 接收者 */
    private String phones;
    private String accountIds;

    /** 时间 */
    private long date;
    /** 显示内容 */
    private String content;
    /** 短信条数 */
    private int _count;

    /** 阅读状态 */
    private int read = -1;

    /** 消息状态 */
    private int status = -1;// default:-1

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public String getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(String accountIds) {
        this.accountIds = accountIds;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCount() {
        return _count;
    }

    public void setCount(int count) {
        this._count = count;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
