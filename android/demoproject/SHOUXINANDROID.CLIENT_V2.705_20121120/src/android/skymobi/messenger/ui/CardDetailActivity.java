
package android.skymobi.messenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.CardDetailAdapter;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.database.dao.ContactsDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.widget.CornerListView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.skymobi.android.sx.codec.beans.clientbean.NetVCardNotify;
import com.skymobi.android.sx.codec.beans.common.VCardContent;
import com.skymobi.android.sx.codec.util.ParserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: CardDetailActivity
 * @Description: 名片详情页
 * @author Michael.Pan
 * @date 2012-3-12 下午03:15:14
 */
public class CardDetailActivity extends TopActivity implements OnClickListener {

    public static final String CONTENT = "Content";
    public static final String ACCOUNT = "Account";
    public static final String CONTACT_ID = "Contact_id";
    public static final String TYPE = "Type";
    private static final String TAG = CardDetailActivity.class.getSimpleName();
    private CardDetailAdapter mAdapter = null;
    LinearLayout addContactsBtn;
    private String mContent = null;
    private int mType = 0;
    private Map<String, Object> cardMap;

    // 0: 表示聊天界面发送到名片，没有加为联系人的按键，没有发送名片的按键
    // 1：表示聊天界面接收到名片，有加为联系人的按键， 没有发送名片的按键
    // 2: 表示点击名片列表后调用的名片详情页，具有发送名片的按键，但是没有加为联系人的按键
    private Account mDestAccount = null;
    private long mContactID = 0;
    private ContactsDAO mContactsDAO = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail);
        Intent intent = getIntent();
        mContent = intent.getStringExtra(CONTENT);
        mType = intent.getIntExtra(TYPE, 0);
        mDestAccount = (Account) intent.getSerializableExtra(ACCOUNT);
        mContactID = intent.getLongExtra(CONTACT_ID, 0);
        mContactsDAO = DaoFactory.getInstance(MainApp.i()).getContactsDAO();
        initView();
        initTopBar();
    }

    private void initView() {
        addContactsBtn = (LinearLayout) findViewById(R.id.add_contacts_btn);
        LinearLayout sendCardBtn = (LinearLayout) findViewById(R.id.send_card_btn);
        addContactsBtn.setOnClickListener(this);
        sendCardBtn.setOnClickListener(this);
        if (mType == 0) {
            addContactsBtn.setVisibility(View.GONE);
            sendCardBtn.setVisibility(View.GONE);
        } else if (mType == 1) {
            cardMap = ParserUtils.decoderVCard(mContent);
            if (isContact(cardMap)) {
                addContactsBtn.setVisibility(View.GONE);
            } else {
                addContactsBtn.setVisibility(View.VISIBLE);
            }
            sendCardBtn.setVisibility(View.GONE);
        } else if (mType == 2) {
            addContactsBtn.setVisibility(View.GONE);
            sendCardBtn.setVisibility(View.VISIBLE);
        }

        CornerListView list = (CornerListView) findViewById(R.id.card_detail_list);
        mAdapter = new CardDetailAdapter(this, getLayoutInflater());
        list.setAdapter(mAdapter);
        updateList();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_contacts_btn) {
            Log.i(TAG, "add_contacts_btn click!");
            Contact contact = new Contact();
            ArrayList<Account> accounts = new ArrayList<Account>();

            String cardName = (String) cardMap.get(NetVCardNotify.CONTACT_NAME);
            List<VCardContent> cardlist = (List) cardMap.get(NetVCardNotify.CONTACT_DETAIL_LIST);
            contact.setNickName(cardName);
            contact.setDisplayname(cardName);
            // 添加手机号
            for (VCardContent vc : cardlist) {
                if (!TextUtils.isEmpty(vc.getPhone())) {
                    Account account = new Account();
                    account.setPhone(vc.getPhone());
                    accounts.add(account);
                }
            }
            if (accounts.isEmpty()) { // 只有skyid,则调用addfriend的接口
                // 添加手信号
                getLastCardContent(contact, accounts, cardName, cardlist);
                mService.getFriendModule().addFriendToCloud(contact, Constants.CONTACT_TYPE_VCARD);
            } else {
                getLastCardContent(contact, accounts, cardName, cardlist);
                mService.getContactsModule().addContact(contact, true);
            }
            finish();
        } else if (v.getId() == R.id.send_card_btn) {
            Log.i(TAG, "send_card_btn click!");
            // mService.getMessageModule().sendCard(mDestAccount, mContactID);
            Intent intent = new Intent(this, ChatActivity.class);
            // intent.putExtra(ChatActivity.ACCOUNTIDS,
            // String.valueOf(mDestAccount.getId()));
            ArrayList<Account> accountList = new ArrayList<Account>();
            accountList.add(mDestAccount);
            intent.putExtra(ChatActivity.ACCOUNTS, accountList);
            intent.putExtra(ChatActivity.CARD_ACCOUNTID, mContactID);
            intent.putExtra(ChatActivity.ACTION, ChatActivity.ACTION_SENDCARD);
            startActivity(intent);
        }
    }

    private void updateList() {
        if (mContent != null) {
            Map<String, Object> cardMap = ParserUtils.decoderVCard(mContent);
            String cardName = (String) cardMap.get(NetVCardNotify.CONTACT_NAME);
            List<VCardContent> cardlist = (List) cardMap.get(NetVCardNotify.CONTACT_DETAIL_LIST);
            if (cardName != null && !cardName.equalsIgnoreCase(""))
                mAdapter.addItem(R.string.card_detail_item_name, cardName);
            // 手信号
            VCardContent lastCardContent = cardlist.get(Constants.MAX_CARD_LIST - 1);
            if (lastCardContent.getSkyid() != null
                    && !lastCardContent.getSkyid().equalsIgnoreCase("")) {
                mAdapter.addItem(R.string.card_detail_item_shouxin, lastCardContent.getNickname());
            }
            for (VCardContent vc : cardlist) {
                if (vc.getPhone() != null && !vc.getPhone().equalsIgnoreCase("")) {
                    mAdapter.addItem(R.string.card_detail_item_phone, vc.getPhone());
                    checkAddContactBtn(vc.getPhone());
                }
            }
        } else if (mContactID > 0) {
            Map<String, Object> cardMap = mService.getMessageModule()
                    .getCardByContactId(mContactID);
            String cardName = (String) cardMap.get(NetVCardNotify.CONTACT_NAME);
            List<VCardContent> cardlist = (List) cardMap.get(NetVCardNotify.CONTACT_DETAIL_LIST);
            if (cardName != null && !cardName.equalsIgnoreCase(""))
                mAdapter.addItem(R.string.card_detail_item_name, cardName);
            // 手信号
            VCardContent lastCardContent = cardlist.get(Constants.MAX_CARD_LIST - 1);
            if (lastCardContent.getSkyid() != null
                    && !lastCardContent.getSkyid().equalsIgnoreCase("")) {
                mAdapter.addItem(R.string.card_detail_item_shouxin, lastCardContent.getNickname());
            }
            for (VCardContent vc : cardlist) {
                if (vc.getPhone() != null && !vc.getPhone().equalsIgnoreCase(""))
                    mAdapter.addItem(R.string.card_detail_item_phone, vc.getPhone());
            }
        }
    }

    private boolean checkAddContactBtn(String phone) {
        Address address = new Address();
        address.setPhone(phone);
        Account account = mContactsDAO.getAccoutByAddress(address);
        if (null != account) {
            Contact contact = mContactsDAO.getContactById(account.getContactId());
            if (contact.getUserType() != ContactsColumns.USER_TYPE_STRANGER) {
                addContactsBtn.setVisibility(View.GONE);
                return true;
            }
        }
        return false;
    }

    /**
     * @param cardMap
     */
    @SuppressWarnings("unchecked")
    private boolean isContact(Map<String, Object> cardMap) {
        List<VCardContent> cardlist = (List<VCardContent>) cardMap
                .get(NetVCardNotify.CONTACT_DETAIL_LIST);
        VCardContent lastCardContent = cardlist.get(Constants.MAX_CARD_LIST - 1);
        return mService.getContactsModule().isContact(cardlist, lastCardContent.getSkyid());
    }

    /**
     * @param contact
     * @param accounts
     * @param cardName
     * @param cardlist
     */
    private void getLastCardContent(Contact contact, ArrayList<Account> accounts, String cardName,
            List<VCardContent> cardlist) {
        VCardContent lastCardContent = cardlist.get(Constants.MAX_CARD_LIST - 1);
        if (!TextUtils.isEmpty(lastCardContent.getSkyid())) {
            contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
            Account account = new Account();
            account.setNickName(cardName);
            account.setSkyAccount(lastCardContent.getNickname());
            account.setSkyId(Integer.valueOf(lastCardContent.getSkyid()));
            accounts.add(account);
        }
        contact.setAccounts(accounts);
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.card_detail_title);
    }
}
