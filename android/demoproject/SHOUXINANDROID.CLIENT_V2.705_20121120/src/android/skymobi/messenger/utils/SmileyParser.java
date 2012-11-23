
package android.skymobi.messenger.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: SmileyParser
 * @Description: 表情解析类
 * @author Michael.Pan
 * @date 2012-2-16 上午10:26:34
 */
public class SmileyParser {
    private static Object classLock = SmileyParser.class;
    private static SmileyParser sInstance;

    public static SmileyParser getInstance() {
        synchronized (classLock) {
            if (sInstance == null) {
                sInstance = new SmileyParser(MainApp.i());
            }
            return sInstance;
        }
    }

    private final Context mContext;
    private final String[] mSmileyTexts;
    private final Pattern mPattern;
    private final HashMap<String, Integer> mSmileyToRes;

    private SmileyParser(Context context) {
        mContext = context;
        mSmileyTexts = mContext.getResources().getStringArray(R.array.default_smiley_texts);
        mSmileyToRes = buildSmileyToRes();
        mPattern = buildPattern();
    }

    // to the string arrays: default_smiley_texts and default_smiley_names in
    // res/values/arrays.xml

    public static final int[] DEFAULT_SMILEY_RES_IDS = {
            R.drawable.smiley_0, // 0
            R.drawable.smiley_1, // 1
            R.drawable.smiley_2, // 2
            R.drawable.smiley_3, // 3
            R.drawable.smiley_4, // 4
            R.drawable.smiley_5, // 5
            R.drawable.smiley_6, // 6
            R.drawable.smiley_7, // 7
            R.drawable.smiley_8, // 8
            R.drawable.smiley_9, // 9
            R.drawable.smiley_10, // 10
            R.drawable.smiley_11, // 11
            R.drawable.smiley_12, // 12
            R.drawable.smiley_13, // 13
            R.drawable.smiley_14, // 14
            R.drawable.smiley_15, // 15
            R.drawable.smiley_16, // 16
            R.drawable.smiley_17, // 17
            R.drawable.smiley_18, // 18
            R.drawable.smiley_19, // 19
            R.drawable.smiley_20, // 20
            R.drawable.smiley_21, // 21
            R.drawable.smiley_22, // 22
            R.drawable.smiley_23, // 23
            R.drawable.smiley_24, // 24
            R.drawable.smiley_25, // 25
            R.drawable.smiley_26, // 26
            R.drawable.smiley_27, // 27
            R.drawable.smiley_28, // 28
            R.drawable.smiley_29, // 29
            R.drawable.smiley_30, // 30
            R.drawable.smiley_31, // 31
    };

    /**
     * Builds the hashtable we use for mapping the string version of a smiley
     * (e.g. ":-)") to a resource ID for the icon version.
     */
    private HashMap<String, Integer> buildSmileyToRes() {
        if (DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
            // Throw an exception if someone updated DEFAULT_SMILEY_RES_IDS
            // and failed to update arrays.xml
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }

        HashMap<String, Integer> smileyToRes =
                new HashMap<String, Integer>(mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            smileyToRes.put(mSmileyTexts[i], DEFAULT_SMILEY_RES_IDS[i]);
        }

        return smileyToRes;
    }

    /**
     * Builds the regular expression we use to find smileys in
     * {@link #addSmileySpans}.
     */
    private Pattern buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.

        StringBuilder patternString = new StringBuilder(mSmileyTexts.length * 3);

        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies

        // properly so they will be interpreted literally by the regex matcher.

        patternString.append('(');
        for (String s : mSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        // Replace the extra '|' with a ')'

        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such as
     * :-) with a graphical version.
     * 
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any recognized
     *         emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text) {
        if (text == null || text.length() <= 0) {
            return text;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = mSmileyToRes.get(matcher.group());
            Drawable drawable = mContext.getResources().getDrawable(resId);
            // 估计值，原来是48*48 现在显示为32*32，否则输入框高度不够放表情，输入时输入框会
            // 被撑大
            drawable.setBounds(0, 0, 32, 32);
            builder.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE),
                    matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    /**
     * 返回表情对应的字符串
     */
    public String getSmileyText(int smileyId) {
        if (smileyId > mSmileyTexts.length - 1)
            return null;
        return mSmileyTexts[smileyId];
    }
}
