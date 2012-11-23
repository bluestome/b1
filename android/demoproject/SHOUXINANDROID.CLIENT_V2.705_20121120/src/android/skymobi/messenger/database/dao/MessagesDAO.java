
package android.skymobi.messenger.database.dao;

import android.content.ContentValues;
import android.net.Uri;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.ui.Observer;

import java.util.List;

/**
 * @ClassName: ContactsDAO
 * @Description: 消息相关数据库操作
 * @author Sean.Xie
 * @date 2012-2-7 下午4:08:13
 */
public interface MessagesDAO {

    /**
     * 同步会话
     * 
     * @return
     */
    boolean syncLocalThreads(Observer ob);

    /**
     * 获取会话条数
     * 
     * @return
     */
    int getThreadsCount();

    /**
     * 获取全部会话
     * 
     * @return
     */
    List<Threads> getThreadsList();

    /**
     * 获取指定会话
     * 
     * @param start
     * @param count
     * @return
     */
    List<Threads> getThreadsList(int start, int count);

    /**
     * 获取指定会话
     * 
     * @param start
     * @param count
     * @param whereArgs 条件从句
     * @return
     */
    List<Threads> getThreadsList(int start, int count, String whereArgs);

    /**
     * 通过threadsId能获取对应的会话
     * 
     * @param threadsID
     * @return
     */
    Threads getThreadsByID(long threadsID);

    /**
     * 添加会话
     * 
     * @param threads
     * @return
     */
    long addThreads(Threads threads);

    /**
     * 删除会话
     * 
     * @param threads
     * @return
     */
    boolean removeThreads(Threads threads);

    /**
     * 删除会话
     * 
     * @param threadsID
     * @return
     */
    boolean removeThreads(long threadsID);

    /**
     * 删除本地会话
     * 
     * @param threadsID
     * @return
     */
    boolean removeLocalThreads(long threadsID);

    /**
     * 修改会话
     * 
     * @param threads
     * @return
     */
    int updateThreads(Threads threads);

    /**
     * 修改会话阅读状态
     * 
     * @param id
     * @param readStatus
     * @return
     */
    boolean updateThreadsReadStauts(long id, int readStatus);

    /**
     * 修改本地会话阅读状态
     * 
     * @param id
     * @param readStatus
     * @return
     */
    boolean updateLocalThreadsReadStauts(long id, int readStatus);

    /**
     * 根据会话ID修改本地短信的阅读状态
     * 
     * @param id
     * @param readStatus
     * @return
     */
    int updateLocalSMSReadStautsByThreadId(long id, int readStatus);

    /**
     * 同步全部消息
     * 
     * @return
     */
    boolean syncLocalMessage(boolean noInsert);

    /**
     * 同步指定会话消息
     * 
     * @param id
     * @return
     */
    boolean syncLocalMessageByThreadsID(long id);

    /**
     * 分页同步指定会话消息
     * 
     * @param id
     * @param start
     * @param limit
     * @return
     */
    boolean syncLocalMessageByThreadsID(long id, int start, int limit);

    /**
     * 获取全部消息
     * 
     * @return
     */
    List<Message> getMessageList();

    /**
     * 获取指定会话的指定消息
     * 
     * @param threadsID
     * @param start
     * @param count
     * @return
     */
    List<Message> getMessageList(long threadsID, int start, int count);

    /**
     * 获取指定会话的指定消息
     * 
     * @param threadsID
     * @param start
     * @param count
     * @param whereClause 条件从句
     * @return
     */
    List<Message> getMessageList(long threadsID, int start, int count,
            String whereClause);

    /**
     * 通过消息ID获取消息
     * 
     * @param msgID
     * @return
     */
    Message getMessageByID(long msgID);

    /**
     * 新增网络消息
     * 
     * @param message
     * @return
     */
    long addMessage(Message message);

    /**
     * 新增短信消息
     * 
     * @param values
     * @return 新消息URi
     */
    Uri addSMS(ContentValues values);

    /**
     * 新增短信消息
     * 
     * @param values
     * @return 包含短信ID 会话ID
     */
    Message addSMSForMessage(ContentValues values);

    /**
     * 更新消息的发送状态
     * 
     * @param message
     * @return
     */
    long updateMessage(Message message);

    /**
     * 删除消息
     * 
     * @param message
     * @return
     */
    boolean deleteMessage(Message message);

    /**
     * 删除消息
     * 
     * @param id
     * @return
     */
    boolean deleteMessage(long id);

    /**
     * 删除本地短信
     * 
     * @param smsId
     * @return
     */
    boolean deleteSMS(long smsId);

    /**
     * 获取未读的会话列表
     * 
     * @return
     */
    List<Threads> getUnreadThreads();

    /**
     * 获取未读的消息数
     * 
     * @return
     */
    int getTotalUnreadMessageCount();

    /**
     * 通过threadID获取未读消息列表
     * 
     * @return
     */
    List<Message> getUnreadMessageByThreadsID(long threadsID);

    /**
     * 通过phones或者accountid获取displayname
     * 
     * @param
     * @return
     */
    String getContactByPhonesOrAccountIds(String phones, String accountIds);

    /**
     * 通过accountId,phones,skyid获取displayname
     * 
     * @param accountId
     * @param phones
     * @param skyid
     * @return
     */
    String getContactByAccountIdOrSkyIdOrPhone(long accountId, String phone, int skyid);

    /**
     * 获取sequenceId
     * 
     * @param isSms
     * @param sms_id
     * @return
     * @author Sivan.LV
     */
    long getSequenceId(boolean isSms, long sms_id);

    /**
     * 获取本地短信最大ID
     * 
     * @return
     */
    long getMaxLocalSmsId();

    /**
     * 获取messages表最大sms_id
     * 
     * @return
     */
    long getMaxMessagesSmsId();

    /**
     * 根据AddressId获取Threads
     * 
     * @param AddressId
     * @return
     */
    Threads getThreadsByAddressId(String AddressId);

    Threads getThreadsByAddressId(long AddressId);

    void deleteUserData();

    /**
     * 根据会话ID，获取该会话最新的一条消息
     * 
     * @param id
     * @return
     */
    Message getLatestMessageByThreadsId(long id);

    int delete(String table, String whereClause, String[] whereArgs);

    boolean removeLocalThreadsList(String threadsIDs);

    void updateThreadsContent(long threadsId);
}
