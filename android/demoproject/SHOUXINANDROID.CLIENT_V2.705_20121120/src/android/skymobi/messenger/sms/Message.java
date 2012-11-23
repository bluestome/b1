
package android.skymobi.messenger.sms;

public class Message {
    private int seq;
    private String content;
    private String to;

    public Message() {
    };

    public Message(int seq, String to, String content) {
        this.seq = seq;
        this.to = to;
        this.content = content;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}
