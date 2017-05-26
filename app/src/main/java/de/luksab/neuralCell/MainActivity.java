package de.luksab.neuralCell;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.Manifest;
import android.widget.SeekBar;
import android.widget.Toast;

import processing.core.PApplet;

import static android.R.attr.id;

public class MainActivity extends Activity {
    NeuralCell fragment;
    private static final String MAIN_FRAGMENT_TAG = "main_fragment";
    private static final int REQUEST_PERMISSIONS = 1;
    int viewId = 0x1000;



/*    public class AndroidSeekBar extends Activity {
        /** Called when the activity is first created. */
/*        @Override
        public void onCreate(Bundle savedInstanceState)
       super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekbar);
        final TextView seekBarValue = (TextView)findViewById(R.id.seekbarvalue);

       seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }
}*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //FrameLayout frame = new FrameLayout(this);
        //frame.setId(viewId);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        //if(width > height)
        //setContentView(frame, new LayoutParams(height, height));
        //else
        //    setContentView(frame, new LayoutParams(width, width));
        //frame.setId(viewId);
        setContentView(R.layout.main);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main, null);
        FrameLayout frame = (FrameLayout) view.findViewById(R.id.frame);

        if (width > height)
            frame.setLayoutParams(new LayoutParams(height, height));
        else
            frame.setLayoutParams(new LayoutParams(width, width));
        view.setId(viewId);
        Log.v("error", "index=");
        fragment = new NeuralCell();
        if (savedInstanceState == null) {
            fragment = new NeuralCell();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(frame.getId(), fragment, MAIN_FRAGMENT_TAG).commit();
        } else {
            fragment = (NeuralCell) getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        }

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                progress -= 5;
                if (progress <= 0)
                    fragment.p = 1;
                else
                    fragment.p = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onBackPressed() {
        fragment.onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<String> needed = new ArrayList<String>();
        int check;
        boolean danger = false;
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[needed.size()]), REQUEST_PERMISSIONS);
        } else if (danger) {
            fragment.onPermissionsGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Some permissions needed by the app were not granted, so it might not work as intended.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
                fragment.onPermissionsGranted();
            }
        }
    }
}
