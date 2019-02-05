package com.example.timer

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import com.example.timer.util.NotificationUtil
import com.example.timer.util.PrefUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.timer_layout.*
import java.util.*

class MainActivity : AppCompatActivity()
{

    companion object
    {
        fun setAlarm(context: Context, nowSeconds:Long, remainingSeconds:Long) : Long
        {
            val wakeUpTime = (nowSeconds + remainingSeconds) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds,context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context)
        {
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)
        }

        val nowSecondsSystem : Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    private lateinit var timer:CountDownTimer
    private var timerLeng  = 0L
    private var timeState = TimerState.Stopped

    private var secondsRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start.setOnClickListener{
            startTimer()
            timeState = TimerState.Running
            upDateButtons()
        }

        btn_pause.setOnClickListener {
            timer.cancel()
            timeState = TimerState.Paused
            upDateButtons()
        }

        btn_reset.setOnClickListener {
            timer.cancel()
            onTimerFinish()
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(secondsRemaining*1000,1000)
        {
            override fun onFinish() = onTimerFinish()


            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished/1000
                upDateCountDownUI()
            }

        }.start()
    }

    private fun onTimerFinish() {
        timeState = TimerState.Stopped

        setNewTimerLength()

        progress_time.progress = 0

        PrefUtil.setSecondsRemaining(timerLeng,this)
        secondsRemaining = timerLeng

        upDateButtons()
        upDateCountDownUI()
    }

    @SuppressLint("SetTextI18n")
    private fun upDateCountDownUI() {
        val minuteUntilFinished = secondsRemaining/60
        val secondsInMinuteUtilFinished = secondsRemaining - minuteUntilFinished * 60
        val secondStr = secondsInMinuteUtilFinished.toString()
        tv_time.text = "$minuteUntilFinished:${if(secondStr.length == 2)
        secondStr else "0$secondStr"}"
        progress_time.progress = (timerLeng - secondsRemaining).toInt()
    }

    private fun upDateButtons()
    {
        when(timeState)
        {
            TimerState.Running ->{
                btn_start.isEnabled = false
                btn_reset.isEnabled = true
                btn_pause.isEnabled = true
            }

            TimerState.Stopped -> {
                btn_start.isEnabled = true
                btn_reset.isEnabled = false
                btn_pause.isEnabled = false
            }

            TimerState.Paused -> {
                btn_start.isEnabled = true
                btn_reset.isEnabled = true
                btn_pause.isEnabled = false
            }
        }
    }

    private fun setNewTimerLength() {
        val lengthInMinute = PrefUtil.getTimerLength(this)
        timerLeng = lengthInMinute*60L
        progress_time.max = timerLeng.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLeng = PrefUtil.getPreviousTimerLenghSeconds(this)
        progress_time.max = timerLeng.toInt()
    }




    override fun onResume() {
        super.onResume()

        initTimer()

        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
    }

    private fun initTimer() {
        timeState = PrefUtil.getTimerState(this)

        if(timeState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining =
                if(timeState == TimerState.Running || timeState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else timerLeng

        val alarmSetTime = PrefUtil.getAlarmSetTIme(this)

        if(alarmSetTime > 0)
            secondsRemaining -= nowSecondsSystem - alarmSetTime
        else if(secondsRemaining <= 0)
            onTimerFinish()

        if(timeState == TimerState.Running)
            startTimer()

        upDateButtons()
        upDateCountDownUI()
    }



    override fun onPause() {
        super.onPause()

        if(timeState == TimerState.Running)
        {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSecondsSystem,secondsRemaining)
            NotificationUtil.showTimerRunning(this,wakeUpTime)
        }else if(timeState == TimerState.Paused)
        {
            NotificationUtil.showTimerPaused(this)
        }

        PrefUtil.setPreviousTimerLenghSeconds(timerLeng,this)
        PrefUtil.setSecondsRemaining(secondsRemaining,this)
        PrefUtil.setTimerState(timeState,this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_item,menu)
        return true
    }
}
