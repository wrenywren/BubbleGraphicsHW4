package course.examples.Graphics.CanvasBubbleSurfaceView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Matrix;
import android.os.Handler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/* @author A. Porter
 * Revised by S. Anderson
 */
public class BubbleActivity extends Activity {

	BubbleView mBubbleView;
    RelativeLayout relativeLayout;
    TextView frameRate;
    TextView score;
    double startTime;
    int frameCount = 0;
    int scoreCount = 0;
    double scoreStartTime;



	/** Simply create layout and view. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		relativeLayout = (RelativeLayout) findViewById(R.id.frame);
		// decode resource into a bitmap
		final BubbleView bubbleView = new BubbleView(getApplicationContext(),
				BitmapFactory.decodeResource(getResources(), R.drawable.b256));
		relativeLayout.addView(bubbleView);


        // Create text View for Frame Rate
        frameRate = (TextView) getLayoutInflater().inflate(R.layout.text_view, null);
		relativeLayout.addView(frameRate);
        setContentView(relativeLayout);

        //Create text View for Score
        score = (TextView) getLayoutInflater().inflate(R.layout.score_text_view,null);
        relativeLayout.addView(score);
        setContentView(relativeLayout);
	}

    private Handler mHandler = new Handler();
    @Override
    public void onResume(){
        super.onResume();
        mHandler.removeCallbacks(mUpdateTask);
        mHandler.postDelayed(mUpdateTask, 500);
    }
    @Override
    public void onPause(){
        super.onPause();
        mHandler.removeCallbacks(mUpdateTask);
    }
    private Runnable mUpdateTask = new Runnable() {
        @Override
        public void run() {
            //calculateFPS(); //update();
            //posts the runnable after 500msec
            mHandler.postDelayed(mUpdateTask, 500);
        }
    };


    public void calculateFPS(){
        double fps = 0;
        double currentTime = System.currentTimeMillis();
        fps = frameCount/((currentTime - startTime)/1000);
        //Log.d(TAG,"FPS is " + fps);
		startTime = System.currentTimeMillis();
        frameCount = 0;
        frameRate.setText(Double.toString(fps));
    }



    /*
	  SurfaceView is dedicated drawing surface in the view hierarchy.
      SurfaceHolder.Callback determines changes to SurfaceHolder via surfaceXXX
      callbacks.
	 */
	private class BubbleView extends SurfaceView implements
			SurfaceHolder.Callback {

		private Bitmap mBitmap;
        private Bitmap resizedBitmap;
		private int mBitmapHeightAndWidth;
		private int mBitmapHeightAndWidthAdj;
		private final DisplayMetrics mDisplay;
		private final int mDisplayWidth, mDisplayHeight;
		private float mX, mY, mDx, mDy, mRotation;
		private SurfaceHolder mSurfaceHolder;
		private final Paint mPainter = new Paint(); // control style and color
		private Thread mDrawingThread;

		private static final int MOVE_STEP = 1;
		private static final float ROT_STEP = 0.5f;

		public void setGravity(int gravity){
			relativeLayout.setGravity(gravity);
		}


        Boolean touchEvent = false;
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            scoreCount++;
            score.setText(Integer.toString(scoreCount));

            increaseSpeedByTenPercent();
            Double d = 0.10; //Percentage to increase speed by
            mBitmapHeightAndWidth = (int) (mBitmapHeightAndWidth - (mBitmapHeightAndWidth * d)); //decrease radius by 10%
            mBitmap = getResizedBitmap(mBitmap,mBitmapHeightAndWidth, mBitmapHeightAndWidth);
            touchEvent = true;
            return super.onTouchEvent(event);
        }
        public void increaseSpeedByTenPercent(){
            Double d = 0.10; //Percentage to increase speed by
            mBitmapHeightAndWidthAdj = (int) ( mBitmapHeightAndWidthAdj + (mBitmapHeightAndWidthAdj * d)); //increase speed by 10 percent
        }


        public BubbleView(Context context, Bitmap bitmap) {
			super(context);

            mBitmapHeightAndWidth = (int) getResources().getDimension(R.dimen.image_height);
            this.mBitmap = Bitmap.createScaledBitmap(bitmap,mBitmapHeightAndWidth/1, mBitmapHeightAndWidth/1,false);


            mBitmapHeightAndWidthAdj = mBitmapHeightAndWidth / 2; //Changed from 2 to reduce movement
			//for testing

			mDisplay = new DisplayMetrics();
			// get display width/height
			BubbleActivity.this.getWindowManager().getDefaultDisplay().getMetrics(mDisplay);
			mDisplayWidth = mDisplay.widthPixels;
			mDisplayHeight = mDisplay.heightPixels;

			// Give bubble random cords and speed at creation
			Random r = new Random();
			mX = (float) r.nextInt(mDisplayHeight);
			mY = (float) r.nextInt(mDisplayWidth);
			mDx = (float) r.nextInt(mDisplayHeight) / mDisplayHeight; // movement
			mDx *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mDy = (float) r.nextInt(mDisplayWidth) / mDisplayWidth;
			mDy *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mDx *= 0.1;
			mDy *= 0.1;
			mRotation = 1.0f;

			mPainter.setAntiAlias(true); // smooth edges of bitmap
			// This will take care of changes to the bitmap
			mSurfaceHolder = getHolder();
			mSurfaceHolder.addCallback(this);
		}

		/** drawing and rotation */
		private void drawBubble(Canvas canvas) {
			canvas.drawColor(Color.DKGRAY);
			mRotation += ROT_STEP;
			canvas.rotate(mRotation, mY + mBitmapHeightAndWidthAdj, mX + mBitmapHeightAndWidthAdj);
			canvas.drawBitmap(mBitmap, mY, mX, mPainter);
            frameCount++; //increment frameRate!!
		}


        //Function to randomly re-draw bubble at location
        private void drawBubbleAtRandom(Canvas canvas) {

            Random r = new Random();
            mX = (float) r.nextInt(mDisplayHeight);
            mY = (float) r.nextInt(mDisplayWidth);
            mDx = (float) r.nextInt(mDisplayHeight) / mDisplayHeight;
            mDx *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
            mDy = (float) r.nextInt(mDisplayWidth) / mDisplayWidth;
            mDy *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
            mDx *= 0.1;
            mDy *= 0.1;
            mRotation = 1.0f;

            frameCount++;

        }

        //Function to resize current Bitmap
        public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
        {
            if (newHeight <= 0 || newWidth <= 0){
                Toast.makeText(BubbleActivity.this,"Bubble Is Too Small To See", Toast.LENGTH_SHORT).show();
                return mBitmap;
            }
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale(scaleWidth, scaleHeight);
            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
            return resizedBitmap;
        }



		/** True iff bubble can move. */
		private boolean move() {
                //setGravity(20); //set gravity of relative layout
                //Can only modify views in UI Thread

                mX += mDx;
                mY += mDy;
                if (mX < 0 - mBitmapHeightAndWidth
                        || mX > mDisplayHeight + mBitmapHeightAndWidth
                        || mY < 0 - mBitmapHeightAndWidth
                        || mY > mDisplayWidth + mBitmapHeightAndWidth) {
                    return false;
                } else {
                    return true;
                }

        }


		/** Does nothing for surface change */
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		/** When surface created, this creates its thread AND starts it running. */
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
                // Run as separate thread.
                mDrawingThread = new Thread(new Runnable() {
                    public void run() {
                        startTime = (int) System.currentTimeMillis();
                        scoreStartTime = (int) System.currentTimeMillis();
                        Canvas canvas = null;
                        Random r = new Random();
                        // While bubble within view, lock and draw.
                        while (!Thread.currentThread().isInterrupted() && move()) {
                            canvas = mSurfaceHolder.lockCanvas();
                            if (null != canvas) { // Lock canvas while updating bitmap
                                drawBubble(canvas);
                                mSurfaceHolder.unlockCanvasAndPost(canvas);
                            }
                            //When the screen is touched it makes bubble appear in random location
                            if (touchEvent == true) {
                                drawBubbleAtRandom(canvas);
                                touchEvent = false;
                            }
                        }
                    }



                });
                mDrawingThread.start();
            }

		/** Surface destroyed; stop thread. */
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (null != mDrawingThread)
				mDrawingThread.interrupt();
		}

	}
}