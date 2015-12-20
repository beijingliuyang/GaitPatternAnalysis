package gaitpattern.androidsensors;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ApplicationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;



public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mAccelerometer;        
    private Sensor mGravity;
    private Sensor mGyroscope;
    public List<Sensor> listSensor;
    private TextView accelerometerx, accelerometery, accelerometerz;
    private TextView gravityx, gravityy, gravityz;
    private TextView gyroscopex, gyroscopey, gyroscopez;
    private Switch mySwitch;
    private String heightvalue;
    private String weightvalue;
    private String agevalue;
    private String gender;
    private int index = 0;
    private float accx = 0;
    private float accy = 0;
    private float accz = 0;
    private float grax = 0;
    private float gray = 0;
    private float graz = 0;
    private float gyrx = 0;
    private float gyry = 0;
    private float gyrz = 0;
    private String filename = "DataCollection.txt";
    private String newLine = "\n";
    private String email ="beijingliuyang@gmail.com";
    static float[] reading = new float[7];     
    static boolean record;
    long mCurrentThreadTime;
    private Runnable textFileLogger;
    private android.os.Handler mHandler = new android.os.Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeViews();
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

		sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	        sensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
	        sensorManager.registerListener(this, mGravity,  SensorManager.SENSOR_DELAY_NORMAL);
    
	        String firstline ="time accelerometerx accelerometery accelerometerz gyroscopex gyroscopey gyroscopez \n";
	    //Write the first line in txt file
	    // create a file with filename in the external storage under Android/data/gaipatternandroidsensors/files folder
	    try {
	        FileOutputStream fos = openFileOutput(filename,
	                Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
	        String storageState = Environment.getExternalStorageState();
	        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
	            File file = new File(getExternalFilesDir(null),
	                    filename);
	            FileOutputStream fos2 = new FileOutputStream(file);
	            fos2.write(firstline.getBytes());
	            fos2.close();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	 	
		
        // Switch to capture data
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        //set the switch to OFF 
        mySwitch.setChecked(false);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
       
         @Override
         public void onCheckedChanged(CompoundButton buttonView,
           boolean isChecked) {
       
          if(isChecked){
          record = true;
        // write to DataCollect.txt every second
	   	  textFileLogger = new Runnable() {
	         @Override
	         public void run() {	             
	             if(index<500 && record == true){
	        	 writeToFile(reading);
	        	 index++;}
	             //Repeats the logging every 0.05 second
	             mHandler.postDelayed(this, 50);
	         }
	     };
	        //Starts the logging after 10 second
	       mHandler.postDelayed(textFileLogger, 50);
          }else{
           record = false;
           }
         }
        });
           
        Button send =(Button)findViewById(R.id.button1);  
        
        send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				EditText height = (EditText) findViewById(R.id.heightinfo);
		                EditText weight = (EditText) findViewById(R.id.weightinfo);
		                EditText age = (EditText) findViewById(R.id.ageinfo);
				heightvalue = height.getText().toString();
	            		weightvalue = weight.getText().toString();
	            		agevalue = age.getText().toString();
				
	            try {
	    	        FileOutputStream fos = openFileOutput(filename,
	    	                Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
	    	        String storageState = Environment.getExternalStorageState();
	    	        if (storageState.equals(Environment.MEDIA_MOUNTED) && heightvalue != null && ! heightvalue.isEmpty() && weightvalue != null && ! weightvalue.isEmpty() 
    	            		&& agevalue != null && ! agevalue.isEmpty() && (gender.equals("M") || gender.equals("F")))
	    	        {
	    	            File file = new File(getExternalFilesDir(null), filename);
	    	            FileOutputStream fos2 = new FileOutputStream(file, true);
	    	            fos2.write("Height (cm): ".getBytes());
	    	            fos2.write(heightvalue.getBytes());
	    	            fos2.write(" Weight (kg): ".getBytes());
	    	            fos2.write(weightvalue.getBytes());
	    	            fos2.write(" Age (yrs): ".getBytes());
	    	            fos2.write(agevalue.getBytes());
	    	            fos2.write(" Gender: ".getBytes());
	    	            fos2.write(gender.getBytes());
	    	            fos2.write(newLine.getBytes());
	    	            fos2.close();
	    	            // send the DataCollection.txt by email  
	    	            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                            String[] recipients = new String[]{email, "",};
                            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
                            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DataCollection");
                            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "This is my data!");
                            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            emailIntent.setType("text/plain");
                            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                            finish();
	    	            
    	              }
	    	    } catch (Exception e) {
	    	        e.printStackTrace();
	    	    }		            
			}
		});
    	         
	}	
	
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radio_male:
	            if (checked)
	                gender = "M"; // male
	            break;
	        case R.id.radio_female:
	            if (checked)
	                gender = "F"; // female
	            break;
	    }
	}

	private void writeToFile(float[] f ) {
		try {
	        FileOutputStream fos = openFileOutput(filename,
	                Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
	        String storageState = Environment.getExternalStorageState();
	        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
	            File file = new File(getExternalFilesDir(null), filename);
	            FileOutputStream fos2 = new FileOutputStream(file, true);
	            String s = Arrays.toString (f);
	            fos2.write(s.substring(1,(s.length()-2)).getBytes());
	            fos2.write(newLine.getBytes());
	            fos2.close();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	
	}

	public void initializeViews() {
		accelerometerx = (TextView) findViewById(R.id.accelerometerx);
		accelerometery = (TextView) findViewById(R.id.accelerometery);
		accelerometerz = (TextView) findViewById(R.id.accelerometerz);
		
		gravityx = (TextView) findViewById(R.id.gravityx);
		gravityy = (TextView) findViewById(R.id.gravityy);
		gravityz = (TextView) findViewById(R.id.gravityz);
		
		gyroscopex = (TextView) findViewById(R.id.gyroscopex);
		gyroscopey = (TextView) findViewById(R.id.gyroscopey);
		gyroscopez = (TextView) findViewById(R.id.gyroscopez);
	}

	//onResume() register the accelerometer for listening the events
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
	    	sensorManager.registerListener(this, mGravity,  SensorManager.SENSOR_DELAY_NORMAL);
	}

	//onPause() unregister the accelerometer for stop listening the events
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
		   {accx = event.values[0];
		    accelerometerx.setText(Float.toString(accx));
		    accy = event.values[1];
	            accelerometery.setText(Float.toString(accy));
		    accz = event.values[2];
		    accelerometerz.setText(Float.toString(accz));}
	    if (event.sensor.getType() ==  Sensor.TYPE_GYROSCOPE)
           {gyrx = event.values[0];
	    	gyroscopex.setText(Float.toString(gyrx));
	    	gyry = event.values[1];
	    	gyroscopey.setText(Float.toString(gyry));
	    	gyrz = event.values[2];
	    	gyroscopez.setText(Float.toString(gyrz));}
	    if (event.sensor.getType() ==  Sensor.TYPE_GRAVITY)
	       {grax = event.values[0];
	        gravityx.setText(Float.toString(grax));
 	        gray = event.values[1];
 	        gravityy.setText(Float.toString(gray));
 	        graz = event.values[2];
 	        gravityz.setText(Float.toString(graz));}
	 
	    // Remove the gravity contribution with the high-pass filter.
	    	reading[0] = ((float)index)/20;
	        reading[1] = accx-grax;
	    	reading[2] = accy-gray;
	    	reading[3] = accz-graz;
	    	
	    	reading[4] = gyrx;
	    	reading[5] = gyry;
	    	reading[6] = gyrz;
	    };
}

