package com.sergey_kurochkin.pomodoro

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.sergey_kurochkin.core.PomodoroTimer
import com.sergey_kurochkin.core.Section
import com.sergey_kurochkin.pomodoro.custom_views.ProgressBarView
import oolong.Dispatch
import oolong.runtime


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        runtime(
            PomodoroTimer.init,
            PomodoroTimer.update,
            PomodoroTimer.view,
            this.render
        )
    }



    private val label by lazy { findViewById<AppCompatTextView>(R.id.label) }
    private val button by lazy { findViewById<AppCompatButton>(R.id.startStopBtn) }
    private val time by lazy { findViewById<AppCompatTextView>(R.id.time) }
    private val progress by lazy { findViewById<ProgressBarView>(R.id.progress) }

    private fun formatWithZero(seconds: Int): String {
        if (seconds < 10) return "0$seconds"
        return "$seconds"
    }

    private fun formatSeconds(seconds: Int): String {
        val secondsLeft = seconds % 60
        val minutes = (seconds - secondsLeft) / 60
        val formattedSecondsLeft = formatWithZero(secondsLeft)
        return if (minutes <= 0) {
            "00:$formattedSecondsLeft"
        } else {
            "${formatWithZero(minutes)}:$formattedSecondsLeft"
        }
    }

    @ColorInt
    private fun getColorCompat(@ColorRes id: Int): Int {
        return ContextCompat.getColor(this, id)
    }

    private fun setStatusBarColor(@ColorRes id: Int) {
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColorCompat(id)
    }

    private val render : (PomodoroTimer.Props, Dispatch<PomodoroTimer.Msg>) -> Any? = { props, dispatch ->
        time.text = formatSeconds(props.timeLeft)
        if (props.kind == Section.Model.Kind.WORK) {
            progress.changeProgressBarColor(getColorCompat(R.color.red))
            time.setTextColor(getColorCompat(R.color.red))
            label.setTextColor(getColorCompat(R.color.red))
            label.text = getString(R.string.workLabel)
            setStatusBarColor(R.color.red)
        }
        if (props.kind == Section.Model.Kind.REST) {
            progress.changeProgressBarColor(getColorCompat(R.color.green))
            time.setTextColor(getColorCompat(R.color.green))
            label.setTextColor(getColorCompat(R.color.green))
            label.text = getString(R.string.restLabel)
            setStatusBarColor(R.color.green)
        }

        val progressVal = (props.timeLeft.toFloat() / props.duration.toFloat() * 100)
        Log.d("PROGRESS_VAL", progressVal.toString())
        if (progress.progress != progressVal) {
            progress.changeProgress(progressVal)
        }

        button.text = if (props.model is PomodoroTimer.Model.Running) {
            getString(R.string.stop)
        } else {
            getString(R.string.start)
        }
        button.setOnClickListener {
            if (props.model is PomodoroTimer.Model.Running) {
                dispatch(PomodoroTimer.Msg.Pause)
            } else {
                dispatch(PomodoroTimer.Msg.Start)
            }
        }
    }
}