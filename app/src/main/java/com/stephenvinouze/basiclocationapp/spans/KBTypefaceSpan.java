package com.stephenvinouze.basiclocationapp.spans;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class KBTypefaceSpan extends TypefaceSpan {
	
	private final Typeface mTypeface;

    public KBTypefaceSpan(Typeface type) {
        super("");
        mTypeface = type;
    }

    public KBTypefaceSpan(Context context, String assetFontName) {
        super("");
        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + assetFontName + ".ttf");
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, mTypeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, mTypeface);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }

}
