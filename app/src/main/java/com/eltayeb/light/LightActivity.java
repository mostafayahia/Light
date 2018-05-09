/*
 * Copyright (C) 2018 Yahia H. El-Tayeb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * free application
 * Yahia El-Tayeb
 * my simple applications
 * Simple Light application to give you some light in darkness 
 */
package com.eltayeb.light;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class LightActivity extends Activity implements OnClickListener/*, OnLongClickListener*/ {
	
	private static final int FLAG_FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN; 
	private Window mWindow;
	private boolean isFullScreen;
	private SharedPreferences mPrefs;
    private Boolean hasFlash;
    private Camera camera;

    private static final String IS_FULL_SCREEN_KEY = "IFSKEY";

    /** created when the first time this activity created */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // create view
        View vw = new View(this);

        // set background
        vw.setBackgroundColor(0xFFFFFF);

        // check whether or not the device has flash led or not
        hasFlash = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        
        
        // keep screen on & make full screen at starting the app
        mWindow = getWindow();
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (null != savedInstanceState && savedInstanceState.containsKey(IS_FULL_SCREEN_KEY)) {
            isFullScreen = savedInstanceState.getBoolean(IS_FULL_SCREEN_KEY);
        } else {
            isFullScreen = true;
        }
        makeAdjustments(isFullScreen);

        
        // attach view to content view of this activity (to the root view)
        setContentView(vw);
//		setContentView(R.layout.activity_main);

        // set event handler
        vw.setOnClickListener(this);
        //vw.setOnLongClickListener(this);
//		LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
//		layout.setOnClickListener(this);

        // my preferences
        mPrefs = getSharedPreferences("setting", Context.MODE_PRIVATE);

        // show my dialog at first time running the application
        if (mPrefs.getBoolean("isFirstTime", true)) showDialog(0);


        // turning on flash led when opening this app
        if (hasFlash) {

            // when rotate the device the flash light is turning off then turning on
            // so to solve this problem we fixed the screen orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            camera = Camera.open();
            Camera.Parameters p = camera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();
        }
        
        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_FULL_SCREEN_KEY, isFullScreen);
    }

    @Override
    protected void onDestroy() {
        // turning off flash led when closing this app
        if (hasFlash) {
            camera.stopPreview();
            camera.release();
        }

    	SharedPreferences.Editor ed = mPrefs.edit();
    	ed.putBoolean("isFirstTime", false);
    	ed.commit();
    	super.onDestroy();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(R.string.message);
    	builder.setCancelable(false);
    	builder.setNeutralButton(R.string.agree_str, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
    		
    	});
    	return builder.create();
    }
	@Override
	public void onClick(View v) {
		// toggle the state
		isFullScreen = !isFullScreen;
        makeAdjustments(isFullScreen);
	}

    /**
     * if in full screen mode then top menu disappear and set brightness to 100% otherwise
     * showing the top menu and set the brightness to the default brightness of android device
     * @param isFullScreen specify whether full screen mode or not
     */
	private void makeAdjustments(boolean isFullScreen) {
        if (isFullScreen) {
            mWindow.addFlags(FLAG_FULLSCREEN);
        } else {
            mWindow.clearFlags(FLAG_FULLSCREEN);
        }
        if (!hasFlash) adjustBrightness(isFullScreen);
    }

    /**
     * adjust brightness according to full screen mode
     * @param isFullScreen if the device is in full screen mode we make brightness to 100%
     *                     otherwise we use the default brightness in android system
     */
    private void adjustBrightness(boolean isFullScreen) {
        if (isFullScreen) {
            // set the brightness to 100% of the system
            setScreenBrightness(1.0f);
        } else {
            // set the brightness to the default value of android system
            try {
                float sysBrightnessValue = android.provider.Settings.System.getInt(
                        getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                // convert from range 0x00 -> 0xFF to decimal value from 0 -> 1
                sysBrightnessValue /= 255.0f;
                setScreenBrightness(sysBrightnessValue);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

	/**
	 * set the brightness of the screen
	 * @param brightness value in the range from 0 to 1
	 */
	private void setScreenBrightness(float brightness) {
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = brightness;
		getWindow().setAttributes(layoutParams);
	}

	/**
	 * display short toast for certain text
	 * @param text the string you want to display
	 */
	private void showMessage(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
	
	/*
	@Override
	public boolean onLongClick(View v) {
		Intent target = new Intent(Intent.ACTION_SEND)
		    .setType("text/plain")
		    .putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.eltayeb.light");
		startActivity(Intent.createChooser(target, getString(R.string.share_str)));
		// showMessage("long click");
		return true;
	}

	@SuppressWarnings("unused")
	private void showMessage(CharSequence text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	*/

}