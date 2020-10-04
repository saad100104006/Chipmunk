package com.resocoder.timertutorial.util

import android.content.Context
import android.preference.PreferenceManager


class PrefUtil {
    companion object {
        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.chipmunk.androidsoundeffect.previous_timer_length_seconds"
        private const val ALARM_SET_TIME_ID = "com.chipmunk.androidsoundeffect.backgrounded_time"

        fun getPreviousTimerLengthSeconds(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        private const val SECONDS_REMAINING_ID = "com.chipmunk.androidsoundeffect.seconds_remaining"

        fun getSecondsRemaining(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }

        fun getAlarmSetTime(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return  preferences.getLong(ALARM_SET_TIME_ID, 0)
        }

    }
}