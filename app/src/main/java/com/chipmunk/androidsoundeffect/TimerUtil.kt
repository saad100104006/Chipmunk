package com.chipmunk.androidsoundeffect

import java.util.*

class TimerUtil  {
    companion object {
        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

   enum class TimerState{
        Stopped, Paused, Running
    }
}
