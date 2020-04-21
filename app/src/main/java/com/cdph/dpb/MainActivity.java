package com.cdph.dpb;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;

public class MainActivity extends Activity
{
	DottedProgressBar pb;
	Handler mainThread = new Handler();
	Runnable run = new Runnable() {
		@Override
		public void run()
		{
			updateProgressBar();
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		pb = (DottedProgressBar) findViewById(R.id.dpb);
		pb.setDotProgress(0);
		pb.setOnDotProgressChangeListener(new DottedProgressBar.OnDotProgressChangeListener() {
			@Override
			public void onDotProgressIncreased(int progress)
			{
				((TextView) findViewById(R.id.prog)).setText(String.valueOf("Progress: " + progress));
			}
			
			@Override
			public void onDotProgressFinished(int progress)
			{}
			
			@Override
			public void onDotProgressDecreased(int progress)
			{}
		});
		mainThread.postDelayed(run, 400);
    }
	
	public void updateProgressBar()
	{
		pb.setDotProgress(pb.getDotProgress() + 2);
		mainThread.removeCallbacks(run);
		mainThread.postDelayed(run, 400);
	}
}
