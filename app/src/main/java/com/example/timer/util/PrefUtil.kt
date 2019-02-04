package com.example.timer.util

import android.content.Context
import android.preference.PreferenceManager
import com.example.timer.TimerState

class PrefUtil
{
    companion object {
        fun getTimerLength(context:Context) : Int{
            return 1
        }

        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.example.timer.previous_timer_length_seconds_id"

        fun getPreviousTimerLenghSeconds(context: Context) : Long
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID,0)
        }

        fun setPreviousTimerLenghSeconds(seconds:Long, context:Context)
        {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID,seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "com.example.timer.timer_state"

        fun getTimerState(context: Context): TimerState
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID,0)
            return TimerState.values()[ordinal]
        }

        fun setTimerState(timerState:TimerState,context: Context)
        {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(TIMER_STATE_ID,timerState.ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING_ID = "com.example.timer.seconds_remaining_id"

        fun getSecondsRemaining(context: Context) : Long
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID,0)
        }

        fun setSecondsRemaining(seconds:Long, context:Context)
        {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID,seconds)
            editor.apply()
        }
    }
}