package de.luksab.neuralCell;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.Manifest;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import processing.core.PApplet;

import static android.R.attr.id;

public class MainActivity extends Activity {
    NeuralCell fragment;
    private static final String MAIN_FRAGMENT_TAG = "main_fragment";
    private static final int REQUEST_PERMISSIONS = 1;
    int viewId = 0x1000;
    //SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        setContentView(R.layout.main);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main, null);
        final quadraticLayout frame = (quadraticLayout) view.findViewById(R.id.frame);

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

        Switch algoCells = (Switch) findViewById(R.id.algoCells);
        algoCells.setChecked(true);
        algoCells.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fragment.algoCells = isChecked;
                if (isChecked)
                    fragment.newAlCell();
            }
        });

        Button ccell = (Button) findViewById(R.id.ccell);
        ccell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fragment.spawnControlled();
            }
        });

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setChecked(true);
        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        fragment.cannibalism = isChecked;
                    }
                }
        );

        JoystickView joystick = (JoystickView) findViewById(R.id.joystick);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                fragment.cDir = (float)(((double)(angle)/360)*Math.PI*2);
                fragment.cSpeed = (float)(strength)/100;
            }
        });

        //mPrefs = getPreferences(MODE_PRIVATE);
    }

    @Override
    public void onBackPressed() {
        fragment.onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();
        /*SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(fragment.Cells);
        prefsEditor.putString("Cells", json);
        prefsEditor.commit();//TODO try out apply*/
    }

    @Override
    public void onStart() {
        super.onStart();
        /*Gson gson = new Gson();
        String json = mPrefs.getString("Cells", "");
        Log.v("info",json);
        ArrayList<NeuralCell.Cell> arr = gson.fromJson(json, new TypeToken<List<NeuralCell.Cell>>(){}.getType());
        if (!arr.isEmpty())
            fragment.Cells = arr;*/

        /*
        ArrayList<String> needed = new ArrayList<String>();
        int check;
        boolean danger = false;
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[needed.size()]), REQUEST_PERMISSIONS);
        } else if (danger) {
            fragment.onPermissionsGranted();
        }*/
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
