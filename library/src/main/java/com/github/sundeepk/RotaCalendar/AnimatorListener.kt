package com.github.sundeepk.RotaCalendar


import android.animation.Animator

abstract class AnimatorListener : Animator.AnimatorListener {

    override fun onAnimationStart(animation: Animator) {}

    override fun onAnimationEnd(animation: Animator) {

    }

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationRepeat(animation: Animator) {}

}
