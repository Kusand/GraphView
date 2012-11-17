package com.kusand.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

/**
 * Line Graph View. This draws a line chart.
 * @author kusand - jonas gehring - http://www.kusand.com
 *
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 */
public class LineGraphView extends GraphView {
	private final Paint paintBackground;
	private boolean drawBackground;
    private boolean drawFilled;

    public LineGraphView(Context context) {
        super(context);
        paintBackground = buildPaintBackground();
    }

    public LineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintBackground = buildPaintBackground();
        pullLineAttributes(context, attrs);
    }

    private void pullLineAttributes(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.LineGraphView, 0, 0);
        try {
            drawFilled = a.getBoolean(R.styleable.LineGraphView_drawFilled, false);
        }
        finally {
            a.recycle();
        }
    }

    private Paint buildPaintBackground() {
        Paint paintBackground = new Paint();
        paintBackground.setARGB(255, 20, 40, 60);
        paintBackground.setStrokeWidth(4);
        if(drawFilled) {
            paintBackground.setStyle(Paint.Style.FILL);
        }
        return paintBackground;
    }

    @Override
	public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart) {
		// draw background
		double lastEndY = 0;
		double lastEndX = 0;
		if (drawBackground) {
			float startY = graphheight + border;
			for (int i = 0; i < values.length; i++) {
				double valY = values[i].valueY - minY;
				double ratY = valY / diffY;
				double y = graphheight * ratY;

				double valX = values[i].valueX - minX;
				double ratX = valX / diffX;
				double x = graphwidth * ratX;

				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight +2;

				if (i > 0) {
					// fill space between last and current point
					int numSpace = (int) ((endX - lastEndX) / 3f) +1;
					for (int xi=0; xi<numSpace; xi++) {
						float spaceX = (float) (lastEndX + ((endX-lastEndX)*xi/(numSpace-1)));
						float spaceY = (float) (lastEndY + ((endY-lastEndY)*xi/(numSpace-1)));

						// start => bottom edge
						float startX = spaceX;

						// do not draw over the left edge
						if (startX-horstart > 1) {
							canvas.drawLine(startX, startY, spaceX, spaceY, paintBackground);
						}
					}
				}

				lastEndY = endY;
				lastEndX = endX;
			}
		}

		// draw data
		lastEndY = 0;
		lastEndX = 0;
        float origX = 0;
        float origY = 0;
        Path areaPath = new Path();
		for (int i = 0; i < values.length; i++) {
			double valY = values[i].valueY - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;

			double valX = values[i].valueX - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

            if(i == 0 && drawFilled) {
                origX = (float) x + (horstart + 1);
                origY = (float) (border - y) + graphheight;
                areaPath.moveTo(origX, origY);
            }

			if (i > 0) {
                float startX = (float) lastEndX + (horstart + 1);
                float startY = (float) (border - lastEndY) + graphheight;
                float endX = (float) x + (horstart + 1);
                float endY = (float) (border - y) + graphheight;
                if(drawFilled){
                    areaPath.lineTo(endX, endY);
                } else {
				    canvas.drawLine(startX, startY, endX, endY, paint);
                }
			}
			lastEndY = y;
			lastEndX = x;
		}
        if(drawFilled) {
            areaPath.lineTo((float) lastEndX + (horstart + 1), graphheight + border);
            areaPath.lineTo((horstart + 1), graphheight + border);
            areaPath.lineTo(origX, origY);
            canvas.drawPath(areaPath, paint);
        }
	}

	public boolean getDrawBackground() {
		return drawBackground;
	}

	/**
	 * @param drawBackground true for a light blue background under the graph line
	 */
	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

    public void setDrawFilled(boolean drawFilled) {
        this.drawFilled = drawFilled;
    }
}
