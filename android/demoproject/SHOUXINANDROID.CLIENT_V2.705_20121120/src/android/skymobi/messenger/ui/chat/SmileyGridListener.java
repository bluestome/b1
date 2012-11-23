
package android.skymobi.messenger.ui.chat;

import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.SmileyAdapter;
import android.skymobi.messenger.ui.ChatActivity;
import android.skymobi.messenger.utils.SmileyParser;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

/**
 * @ClassName: SmileyGridListener
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-10-17 下午3:13:37
 */
public class SmileyGridListener implements OnItemClickListener {
    private final int page;
    private final Context mContext;

    public SmileyGridListener(int page, Context context) {
        this.page = page;
        mContext = context;
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ChatActivity activity = (ChatActivity) mContext;
        EditText editText = (EditText) activity.findViewById(R.id.chat_edit);
        SmileyParser smileyParser = SmileyParser.getInstance();

        if ((position + 1) % SmileyAdapter.PAGE_SIZE == 0) {
            final KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_DEL);
            editText.onKeyDown(KeyEvent.KEYCODE_DEL, keyEventDown);
            return;
        }
        String strSmiley = smileyParser.getSmileyText(page * SmileyAdapter.PAGE_SIZE - page
                + position);
        if (strSmiley == null)
            return;
        String strOldEdit = editText.getText().toString();
        int selection = editText.getSelectionStart();
        String strEdit = strOldEdit.substring(0, editText.getSelectionStart()) + strSmiley
                + strOldEdit.substring(editText.getSelectionStart(), strOldEdit.length());
        CharSequence chSpans = smileyParser.addSmileySpans(strEdit);
        if (chSpans.length() < 257) {
            editText.setText(chSpans);
            editText.setSelection(selection + strSmiley.length()); // 光标位置不变
        }
    }

}
