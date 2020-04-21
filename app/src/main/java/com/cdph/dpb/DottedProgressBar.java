package com.cdph.dpb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DottedProgressBar extends View
{
	private static final int DOT_ACTIVE = 255;
	private static final int DOT_INACTIVE = 75;
	private static final int MAX_PERCENT = 100;
	private static final int MAX_DOTS = 5;
	private static final int MAX_GAP = 30;
	
	private OnDotProgressChangeListener listener;
	private Paint[] circlePaints;
	private float radius = 6.8f;
	private int delay = 200;
	private int alpha = DOT_INACTIVE;
	private int step = 0, lastStep = -1, progress = 0, lastProgress = -1;
	private int width, height, dots = 5, color = Color.BLUE;
	private boolean incremented = false, decremented = false;
	private float[][] pos;
	
	public DottedProgressBar(Context ctx)
	{
		super(ctx);
		
		init();
	}
	
	public DottedProgressBar(Context ctx, AttributeSet attrs)
	{
		super(ctx, attrs);
		
		TypedArray tarr = ctx.getTheme().obtainStyledAttributes(attrs, R.styleable.DottedProgressBar, 0, 0);
		dots = tarr.getInt(R.styleable.DottedProgressBar_dot_count, 5);
		color = tarr.getInt(R.styleable.DottedProgressBar_dot_color, Color.BLUE);
		radius = tarr.getFloat(R.styleable.DottedProgressBar_dot_radius, 6.8f);
		
		init();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		updateCanvas(canvas);
		postInvalidateDelayed(delay);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		width = w;
		height = w;
		updatePos();
	}
	
	public void setDotRadius(float radius)
	{
		this.radius = radius;
	}
	
	public void setDotColor(int color)
	{
		this.color = color;
	}
	
	public void setDotProgress(int progress)
	{
		if(progress > MAX_PERCENT || progress < 0)
			return;
		this.progress = progress;
		this.incremented = (progress > lastProgress);
		this.decremented = (progress < lastProgress);
	}
	
	public void setDotSize(int numOfDots)
	{
		if(numOfDots > MAX_DOTS)
			return;
		this.dots = numOfDots;
	}
	
	public void setOnDotProgressChangeListener(DottedProgressBar.OnDotProgressChangeListener listener)
	{
		this.listener = listener;
	}
	
	public int getDotProgress()
	{
		return progress;
	}
	
	public int getDotColor()
	{
		return color;
	}
	
	public float getDotRadius()
	{
		return radius;
	}
	
	private void init()
	{
		circlePaints = new Paint[dots];
		pos = new float[dots][3];
		
		for(int i = 0; i < circlePaints.length; i++)
		{
			circlePaints[i] = new Paint();
			circlePaints[i].setColor(color);
			circlePaints[i].setStyle(Paint.Style.FILL);
			circlePaints[i].setShadowLayer(2.8f, 0.5f, 0.5f, Color.BLACK);
			circlePaints[i].setAlpha((i == 0) ? DOT_ACTIVE : DOT_INACTIVE);
		}
		
		lastStep = step = 0;
	}
	
	private void updatePos()
	{
		int centerX = width / 2;
		int centerY = height / 2;
		
		int oddCenter = ((dots - 1) / 2) + 1;
		int evnCenter = dots / 2;
		int prgPerDot = MAX_PERCENT / dots;
		
		int startGap = (MAX_GAP * (((dots % 2) == 0) ? evnCenter : oddCenter));
		int center = ((dots % 2) == 0 ? evnCenter - 1 : oddCenter - 1);
		int counter = 1;
		
		for(int i = 0; i < pos.length; i++)
		{
			if((dots % 2) == 0)
			{
				//Left dots
				if(i < center)
					pos[i] = new float[]{centerX - (startGap - ((MAX_GAP / 2) * counter++)), centerY, prgPerDot * (i + 1)};
				
				//Center dots
				if(i == center)
				{
					pos[i] = new float[]{centerX - (MAX_GAP / 2), centerY, prgPerDot * (i + 1)};
					counter = 1;
				}
					
				if(i == (center + 1))
				{
					pos[i] = new float[]{centerX + (MAX_GAP / 2), centerY, prgPerDot * (i + 1)};
					counter = 1;
				}
				
				//Right dots
				if(i > (center + 1))
					pos[i] = new float[]{centerX + (startGap - ((MAX_GAP / 2) * counter++)), centerY, prgPerDot};
			}
			else
			{
				//Left dots
				if(i < center)
					pos[i] = new float[]{centerX - (startGap - (MAX_GAP * counter++)), centerY, prgPerDot * (i + 1)};

				//Center dot
				if(i == center)
				{
					pos[i] = new float[]{centerX, centerY, prgPerDot * (i + 1)};
					counter = 1;
				}

				//Right dots
				if(i > center)
					pos[i] = new float[]{centerX + (startGap - (MAX_GAP * counter++)), centerY, prgPerDot * (i + 1)};
			}
		}
	}
	
	private void updateCanvas(Canvas canvas)
	{
		if(alpha == 255)
			alpha = 75;
		
		if(incremented)
		{
			lastProgress = progress - 1;
			
			int currDotProg = (int) pos[step][2];
			int nextDotProg = (int) pos[((step + 1) == pos.length) ? step : step + 1][2];
			
			if(progress >= currDotProg && progress < nextDotProg)
				if(step < pos.length-1)
					lastStep = step++;
			
			//Set last dot to active
			circlePaints[lastStep].setAlpha(DOT_ACTIVE);
			
			if(listener != null)
				listener.onDotProgressIncreased(currDotProg);
			incremented = false;
		}
		
		if(decremented)
		{
			lastProgress = progress + 1;
			
			int currDotProg = (int) pos[step][2];
			int lastDotProg = (int) pos[((step - 1) != -1) ? step - 1 : step][2];
			
			if(progress < currDotProg && progress >= lastDotProg)
				if(step > 0)
					lastStep = step--;
			
			//Set last dot to inactive
			circlePaints[lastStep].setAlpha(DOT_INACTIVE);
			
			if(listener != null)
				listener.onDotProgressDecreased(currDotProg);
			decremented = false;
		}
		
		if(progress >= pos[dots-1][2])
		{
			listener.onDotProgressFinished(progress);
			circlePaints[step].setAlpha(DOT_ACTIVE);
		}
		else
			circlePaints[step].setAlpha(alpha += 9);
		
		for(int i = 0; i < pos.length; i++)
		{
			Paint circlePaint = circlePaints[i];
			float x = pos[i][0];
			float y = pos[i][1];
			
			canvas.drawCircle(x, y, radius, circlePaint);
		}
	}
	
	public interface OnDotProgressChangeListener
	{
		public void onDotProgressIncreased(int progress)
		public void onDotProgressDecreased(int progress)
		public void onDotProgressFinished(int progress)
	}
}
