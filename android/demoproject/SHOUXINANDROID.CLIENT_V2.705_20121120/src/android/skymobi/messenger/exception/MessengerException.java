
package android.skymobi.messenger.exception;

/**
 * @ClassName: MessengerException
 * @Description: MessengerException
 * @author Sean.Xie
 * @date 2012-2-13 下午3:21:53
 */
public class MessengerException extends RuntimeException {
    private static final long serialVersionUID = 5162710183389028792L;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     */
    public MessengerException() {
        super();
    }

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param detailMessage
     */
    public MessengerException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param detailMessage
     * @param throwable
     */
    public MessengerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param throwable
     */
    public MessengerException(Throwable throwable) {
        super(throwable);
    }

}
