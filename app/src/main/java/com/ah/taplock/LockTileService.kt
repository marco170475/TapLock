package com.ah.taplock

import android.content.Intent
import android.service.quicksettings.TileService

class LockTileService : TileService() {

    override fun onClick() {
        super.onClick()

        lockScreen(applicationContext, "tile")
    }
}
