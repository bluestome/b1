
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.service.module.MessageModule;
import android.skymobi.messenger.ui.MessageMultiDeleteListActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * @ClassName: MessageMultiDeleteListAdapter
 * @Description: 批量删除会话界面的adapter
 * @author Michael.Pan
 * @date 2012-7-24 上午09:29:49
 */
public class MessageMultiDeleteListAdapter extends MessageListAdapter {

    private static final String TAG = MessageMultiDeleteListAdapter.class.getSimpleName();

    /**
     * @param ctx
     * @param inflater
     */
    public MessageMultiDeleteListAdapter(Context ctx, LayoutInflater inflater,
            MessageModule msgModule) {
        super(ctx, inflater, msgModule);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.message_list_item_checkbox);
        checkBox.setVisibility(View.VISIBLE);
        final Threads curThreads = mList.get(position);
        // SLog.d(TAG, "getView mSelectList = " + mSelectList.size());
        if (isSelected(curThreads)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
                if (checkBox.isChecked()) {
                    mSelectList.add(curThreads);
                } else {
                    mSelectList.remove(curThreads);
                }
                changeButtonText();
                view.invalidate();
            }
        });
        return view;
    }

    // 改变删除button的文字
    protected void changeButtonText() {
        MessageMultiDeleteListActivity activity = (MessageMultiDeleteListActivity) mContext;
        Button mDeleteBtn = (Button) activity
                .findViewById(R.id.message_list_delete);
        Button mSelectAllBtn = (Button) activity.findViewById(R.id.message_list_selectall);

        int selectCount = mSelectList.size();
        int totalCount = mList.size();
        String text = activity
                .getString(R.string.message_list_delete);
        if (selectCount > 0) {
            text = activity.getString(
                    R.string.contacts_multi_del_btn_text,
                    selectCount);
            mDeleteBtn.setEnabled(true);
        } else {
            mDeleteBtn.setEnabled(false);
        }
        mDeleteBtn.setText(text);

        SLog.d(TAG, "selectCount = " + selectCount + " ,totalCount = " + totalCount);
        if (selectCount > 0 && selectCount == totalCount) {
            mSelectAllBtn.setText(R.string.message_list_selectnone);
        } else {
            mSelectAllBtn.setText(R.string.message_list_selectall);
        }
    }

    // 全选 or 全不选
    public void selectAllorNone() {
        int selectCount = mSelectList.size();
        int totalCount = mList.size();
        if (selectCount != totalCount) {
            mSelectList.clear();
            for (Threads threads : mList) {
                mSelectList.add(threads);
            }
        } else {
            mSelectList.clear();
        }

        changeButtonText();
        notifyDataSetChanged();
    }

    private boolean isSelected(Threads curThreads) {
        boolean isSelected = false;
        if (curThreads == null) {
            return false;
        }
        for (Threads threads : mSelectList) {
            if (threads.getId() == curThreads.getId()) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }
}
