package com.yg.grabadoraaudio;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class animacion {

    private Animation pulse;

    public void startRecording(Context context, ImageView imgmicro) {

        pulse = AnimationUtils.loadAnimation(context, R.anim.pulse);
        imgmicro.startAnimation(pulse);


    }

    public void stopRecording(ImageView imgmicro) {
        imgmicro.clearAnimation();


    }
}
