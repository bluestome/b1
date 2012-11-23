
package android.skymobi.messenger.bean;

import android.content.Context;

/**
 * @ClassName: SettingsItem
 * @Description: 设置界面单项数据结构
 * @author Lv.Lv
 * @date 2012-2-27 下午5:08:35
 */
public class SettingsItem {

    private String mTitle = null;
    private String mContent = null;

    private boolean mIsContainMoreBtn = true;
    private boolean mIsContainCheckBtn = false;
    private boolean mIsCheckedCheckBtn = false;
    private int mImageID = 0;

    public SettingsItem(String title, String content, boolean isContainMoreBtn,
            boolean isContainCheckbtn, int imageID, boolean isCheckedCheckBtn) {
        mTitle = title;
        mContent = content;
        mIsContainMoreBtn = isContainMoreBtn;
        mIsContainCheckBtn = isContainCheckbtn;
        mIsCheckedCheckBtn = isCheckedCheckBtn;
        mImageID = imageID;
    }

    public SettingsItem(Context context, int titleID, int contentID,
            boolean isContainMoreBtn, boolean isContainCheckbtn, int imageID,
            boolean isCheckedCheckBtn) {
        if (titleID > 0) {
            mTitle = context.getString(titleID);
        }
        if (contentID > 0) {
            mContent = context.getString(contentID);
        }
        mIsContainMoreBtn = isContainMoreBtn;
        mIsContainCheckBtn = isContainCheckbtn;
        mImageID = imageID;
        mIsCheckedCheckBtn = isCheckedCheckBtn;
    }

    /**
     * @return the Title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * @param title the Title to set
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * @return the Content
     */
    public String getContent() {
        return mContent;
    }

    /**
     * @param content the Content to set
     */
    public void setContent(String content) {
        this.mContent = content;
    }

    /**
     * @return the ImageID
     */
    public int getImageID() {
        return mImageID;
    }

    /**
     * @param imageID the ImageID to set
     */
    public void setmImageID(int imageID) {
        this.mImageID = imageID;
    }

    /**
     * @return if contains CheckBtn or not
     */
    public boolean isContainCheckBtn() {
        return mIsContainCheckBtn;
    }

    /**
     * @param isContainCheckBtn the mIsContainCheckBtn to set
     */
    public void setContainCheckBtn(boolean isContainCheckBtn) {
        this.mIsContainCheckBtn = isContainCheckBtn;
    }

    /**
     * 返回check bt的选中状态
     * 
     * @return
     */
    public boolean isCheckedCheckBtn() {
        return mIsCheckedCheckBtn;
    }

    /**
     * @param isChecked
     */
    public void setCheckedCheckBtn(boolean isChecked) {
        this.mIsCheckedCheckBtn = isChecked;
    }

    /**
     * @return if contains MoreBtn or not
     */
    public boolean isContainMoreBtn() {
        return mIsContainMoreBtn;
    }

    /**
     * @param isContainCheckBtn the mIsContainCheckBtn to set
     */
    public void setContainMoreBtn(boolean isContainMoreBtn) {
        this.mIsContainMoreBtn = isContainMoreBtn;
    }

}
