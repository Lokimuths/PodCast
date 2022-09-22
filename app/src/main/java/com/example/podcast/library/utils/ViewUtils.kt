package com.example.podcast.library.utils

import android.view.View

fun View.show() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun View.hide() {
    if (visibility != View.GONE) visibility = View.GONE
}


