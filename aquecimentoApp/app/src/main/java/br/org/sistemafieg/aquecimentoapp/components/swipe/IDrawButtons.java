package br.org.sistemafieg.aquecimentoapp.components.swipe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;

public interface IDrawButtons {

    static final float buttonWidth = 300;

    public Context getContext();

    public RectF drawButtonLeft(Canvas c, RecyclerView.ViewHolder viewHolder);

    public RectF drawButtonRight(Canvas c, RecyclerView.ViewHolder viewHolder);

    default void drawText(String text, Canvas c, RectF button, Paint p, int icon) {
        float textSize = 30;
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        float textWidth = p.measureText(text);
        Bitmap bmp = drawableToBitmap(ContextCompat.getDrawable(getContext(), icon));

        float spaceHeight = 10; // change this to whatever looks good to you
        Rect bounds = new Rect();
        p.getTextBounds(text, 0, text.length(), bounds);
        float combinedHeight = bmp.getHeight() + spaceHeight + bounds.height();
        c.drawBitmap(bmp, button.centerX() - (bmp.getWidth() / 2), button.centerY() - (combinedHeight / 2), null);
        c.drawText(text, button.centerX() - (textWidth / 2), button.centerY() + (combinedHeight / 2), p);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
