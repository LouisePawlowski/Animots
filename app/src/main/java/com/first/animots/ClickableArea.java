package com.first.animots;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClickableArea {
    public LinearLayout layout;
    public TextView textView;
    public ImageView imageView;

    public ClickableArea(LinearLayout layout, TextView textView, ImageView imageView) {
        this.layout = layout;
        this.textView = textView;
        this.imageView = imageView;
    }

    public LinearLayout getLayout() {
        return this.layout;
    }
    public TextView getTextView() {
        return this.textView;
    }

    public ImageView getImageView() {
        return this.imageView;
    }
}