package com.lukic.conseo.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("loadImage")
fun loadImage(imageView: ImageView, image: Drawable){
    Glide.with(imageView.context).load(image).centerInside().into(imageView)
}

@BindingAdapter("loadImage")
fun loadImage(imageView: ImageView, image: String?){
    if(image != null)
        Glide.with(imageView.context).load(image).centerInside().into(imageView)
}