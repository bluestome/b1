
package android.skymobi.messenger.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

/**
 * @ClassName: Message
 * @Description: 消息
 * @author Sean.Xie
 * @date 2012-2-9 上午11:20:56
 */
public class Message implements Parcelable {

    private long id;

    /** 短信内容 */
    private String content;
    /** 图片路径 */
    private String picture;
    /** 音视频路径 */
    private String media;
    /** 阅读状态 */
    private int read;

    /** 消息类型 */
    private int type;
    /** 操作类型 */
    private int opt;
    /** 发送状态 */
    private int status;
    /** 本地短信ID */
    private long sms_id;

    /** 手机号码 */
    private String phones;
    /** 时间 */
    private long date;
    /** 本地短信会话ID */
    private long localThreadsID;
    /** 手信消息会话ID */
    private long threadsID;

    /** 序列ID，用于排序 */
    private long sequence_id;

    private String talkReason;

    private List<Address> addressList;

    private String nickName;

    private ResFile resFile;

    public Message() {
    }

    public Message(Parcel source) {
        id = source.readLong();
        content = source.readString();
        picture = source.readString();
        media = source.readString();
        read = source.readInt();
        type = source.readInt();
        opt = source.readInt();
        status = source.readInt();
        sms_id = source.readLong();
        phones = source.readString();
        date = source.readLong();
        localThreadsID = source.readLong();
        threadsID = source.readLong();
        sequence_id = source.readLong();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOpt() {
        return opt;
    }

    public void setOpt(int opt) {
        this.opt = opt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getSms_id() {
        return sms_id;
    }

    public void setSms_id(long sms_id) {
        this.sms_id = sms_id;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getLocalThreadsID() {
        return localThreadsID;
    }

    public void setLocalThreadsID(long localThreadsID) {
        this.localThreadsID = localThreadsID;
    }

    public long getThreadsID() {
        return threadsID;
    }

    public void setThreadsID(long threadsID) {
        this.threadsID = threadsID;
    }

    public long getSequence_id() {
        return sequence_id;
    }

    public void setSequence_id(long sequence_id) {
        this.sequence_id = sequence_id;
    }

    public String getTalkReason() {
        return talkReason;
    }

    public void setTalkReason(String talkReason) {
        this.talkReason = talkReason;
    }

    /**
     * @return the addressList
     */
    public List<Address> getAddressList() {
        return addressList;
    }

    /**
     * @param addressList the addressList to set
     */
    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
    }

    /**
     * @return the nickName
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * @param nickName the nickName to set
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * @return the resFile
     */
    public ResFile getResFile() {
        return resFile;
    }

    /**
     * @param resFile the resFile to set
     */
    public void setResFile(ResFile resFile) {
        this.resFile = resFile;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(content);
        parcel.writeString(picture);
        parcel.writeString(media);
        parcel.writeInt(read);
        parcel.writeInt(type);
        parcel.writeInt(opt);
        parcel.writeInt(status);
        parcel.writeLong(sms_id);
        parcel.writeString(phones);
        parcel.writeLong(date);
        parcel.writeLong(localThreadsID);
        parcel.writeLong(threadsID);
        parcel.writeLong(sequence_id);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
