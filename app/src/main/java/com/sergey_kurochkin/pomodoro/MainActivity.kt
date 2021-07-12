package com.sergey_kurochkin.pomodoro

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private val button by lazy { findViewById<FloatingActionButton>(R.id.button) }
    private val time by lazy { findViewById<AppCompatTextView>(R.id.time) }
    private val progress by lazy { findViewById<ProgressBarView>(R.id.progress) }
    private val sectionsLabel by lazy { findViewById<AppCompatTextView>(R.id.sections) }

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

    private fun calculateProgressVal(timeLeft: Int, duration: Int): Float = (timeLeft.toFloat() / duration.toFloat()) * 100

    private fun setProgress(props: PomodoroTimer.Props) {
        val progressVal = calculateProgressVal(props.timeLeft, props.duration)
        progress.changeProgress(progressVal)
    }


    private val render : (PomodoroTimer.Props, Dispatch<PomodoroTimer.Msg>) -> Any? = { props, dispatch ->
        time.text = formatSeconds(props.timeLeft)
        val colorRed = getColorCompat(R.color.red)
        val colorGreen = getColorCompat(R.color.green)

        sectionsLabel.text = "${props.currentSection + 1}/${props.model.sections.size}"
        if (props.kind == Section.Model.Kind.WORK) {
            button.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.red)
            progress.changeProgressBarColor(colorRed)
            time.setTextColor(colorRed)
            label.setTextColor(colorRed)
            label.text = getString(R.string.workLabel)
            sectionsLabel.setTextColor(colorRed)
            setStatusBarColor(R.color.red)
        }
        if (props.kind == Section.Model.Kind.REST) {
            button.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.green)
            progress.changeProgressBarColor(colorGreen)
            time.setTextColor(colorGreen)
            label.setTextColor(colorGreen)
            sectionsLabel.setTextColor(colorGreen)
            label.text = getString(R.string.restLabel)
            setStatusBarColor(R.color.green)
        }

        setProgress(props)

        if (props.model is PomodoroTimer.Model.Running) {
            button.setImageResource(R.drawable.outline_pause_24)
        } else {
            button.setImageResource(R.drawable.outline_play_arrow_24)
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