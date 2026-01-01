package com.ai.Echo

import android.content.Intent

object ResultHolder {
    var callback: ((Intent?) -> Unit)? = null
}
