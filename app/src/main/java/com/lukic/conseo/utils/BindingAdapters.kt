package com.lukic.conseo.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

@BindingAdapter("loadImage")
fun loadImage(imageView: ImageView, image: Drawable) {
    Glide.with(imageView.context).load(image).centerInside().into(imageView)
}

@BindingAdapter("loadImage")
fun loadImage(imageView: ImageView, image: String?) {
    if (image != null)
        Glide.with(imageView.context).load(image).centerInside()
            .into(imageView)
}

@BindingAdapter("setDateTimeVisibility")
fun setDateTimeVisibility(textView: TextView, dateTime: Any?) {
    if (dateTime == null)
        textView.visibility = View.GONE
    else
        textView.visibility = View.VISIBLE
}

@BindingAdapter("setRecyclerVisibility")
fun setRecyclerVisibility(rv: View, items: List<Any>?) {
    if (items.isNullOrEmpty())
        rv.visibility = View.GONE
    else
        rv.visibility = View.VISIBLE
}