package com.example.myapplication;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.inline.InlineContentView;

import androidx.annotation.NonNull;

import java.io.IOException;

public class ShowCamera  extends SurfaceView  implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceHolder holder;

    public ShowCamera(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);

    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Camera.Parameters params = camera.getParameters();


        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            params.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
            params.setRotation(90);
        }
        else
        {
            params.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
            params.setRotation(0);
        }

        camera.setParameters(params);
        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch(IOException e) {
            e.printStackTrace();
        }



    }


}
