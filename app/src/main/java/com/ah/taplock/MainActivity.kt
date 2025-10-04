package com.ah.taplock

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            lockScreen(this, "launcher")

        finish()
    }
}
