package com.kusand.graphview;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.kusand.graphview.compatible.ScaleGestureDetector;
import com.kusand.graphview.labels.DefaultLabelGenerator;
import com.kusand.graphview.labels.LabelGenerator;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs.
 * This is the abstract base class for all graphs. Extend this class and implement {@link #drawSeries(Canvas, GraphViewData[], float, float, float, double, double, double, double, float)} to display a custom graph.
 * Use {@link LineGraphView} for creating a line chart.
 *
 * @author kusand - jonas gehring - http://www.kusand.com
 *
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 */
public abstract class GraphView extends View {

    private Float minY;
    private Float maxY;

    /*static final private class GraphViewConfig {
        static final float BORDER = 20;
        static final float VERTICAL_LABEL_WIDTH = 100;
        static final float HORIZONTAL_LABEL_HEIGHT = 80;
    }*/
    private float lowerBorder = 20;
    private float leftBorder = 50;
    private float verticalLabelWidth = 100;
    private float horizontalLabelHeight = 80;

    // Label generation
    private LabelGenerator horizontalLabelGenerator = new DefaultLabelGenerator(verticalLabelWidth);
    private LabelGenerator verticalLabelGenerator = new DefaultLabelGenerator(horizontalLabelHeight);

    // Graph coloring
    private int verticalLabelColor = Color.WHITE;
    private int verticalLabelTextSize = 15;
    private int horizontalLabelColor = Color.WHITE;
    private int titleColor = Color.WHITE;


    protected GraphView(Context context) {
        super(context);

        paint = new Paint();
        labelPaint = new Paint();
    }

    protected GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pullAttributes(context, attrs);

        paint = new Paint();
        labelPaint = new Paint();
    }

    protected GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        pullAttributes(context, attrs);

        paint = new Paint();
        labelPaint = new Paint();
    }

    protected void pullAttributes(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.GraphView, 0, 0);
        try {
            if(a.hasValue(R.styleable.GraphView_maxY)) {
                maxY = a.getFloat(R.styleable.GraphView_maxY, Float.MAX_VALUE);
            }
            if(a.hasValue(R.styleable.GraphView_minY)) {
                minY = a.getFloat(R.styleable.GraphView_minY, Float.MIN_VALUE);
            }
            if(a.hasValue(R.styleable.GraphView_border)) {
                lowerBorder = a.getFloat(R.styleable.GraphView_border, lowerBorder);
            }
            if(a.hasValue(R.styleable.GraphView_verticalLabelWidth)) {
                verticalLabelWidth = a.getFloat(R.styleable.GraphView_verticalLabelWidth, verticalLabelWidth);
            }
            if(a.hasValue(R.styleable.GraphView_horizontalLabelHeight)) {
                horizontalLabelHeight = a.getFloat(R.styleable.GraphView_horizontalLabelHeight, horizontalLabelHeight);
            }
            if(a.hasValue(R.styleable.GraphView_verticalLabelColor)) {
                verticalLabelColor = a.getColor(R.styleable.GraphView_verticalLabelColor, Color.WHITE);
            }
            if(a.hasValue(R.styleable.GraphView_verticalLabelTextSize)) {
                verticalLabelTextSize = a.getDimensionPixelSize(R.styleable.GraphView_verticalLabelTextSize, verticalLabelTextSize);
            }
            if(a.hasValue(R.styleable.GraphView_horizontalLabelColor)) {
                horizontalLabelColor = a.getColor(R.styleable.GraphView_horizontalLabelColor, Color.WHITE);
            }
            if(a.hasValue(R.styleable.GraphView_titleColor)) {
                titleColor = a.getColor(R.styleable.GraphView_titleColor, Color.WHITE);
            }
        }
        finally {
            a.recycle();
        }
    }

    private float lastTouchEventX;
    private float graphwidth;

    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        drawLabels(canvas);
        drawAxes(canvas);
    }

    private void drawAxes(Canvas canvas) {
        paint.setAntiAlias(true);

        // normal
        paint.setStrokeWidth(0);

        float height = getHeight();
        float width = getWidth() - 1;
        double maxY = getMaxY();
        double minY = getMinY();
        double diffY = maxY - minY;
        double maxX = getMaxX(false);
        double minX = getMinX(false);
        double diffX = maxX - minX;
        float graphheight = height - (2 * lowerBorder);
        graphwidth = width - leftBorder;

        if (horlabels == null) {
            horlabels = generateHorlabels(graphwidth);
        }
        if (verlabels == null) {
            verlabels = generateVerlabels(graphheight);
        }

        // vertical lines
        paint.setTextAlign(Align.LEFT);
        int vers = verlabels.length - 1;
        for (int i = 0; i < verlabels.length; i++) {
            paint.setColor(Color.DKGRAY);
            float y = ((graphheight / vers) * i) + lowerBorder;
            canvas.drawLine(leftBorder, y, width, y, paint);
        }

        // horizontal labels + lines
        int hors = horlabels.length - 1;
        for (int i = 0; i < horlabels.length; i++) {
            paint.setColor(Color.DKGRAY);
            float x = ((graphwidth / hors) * i) + leftBorder;
            canvas.drawLine(x, height - lowerBorder, x, lowerBorder, paint);
            paint.setTextAlign(Align.CENTER);
            if (i==horlabels.length-1)
                paint.setTextAlign(Align.RIGHT);
            if (i==0)
                paint.setTextAlign(Align.LEFT);
            paint.setColor(horizontalLabelColor);
            canvas.drawText(horlabels[i], x, height - 4, paint);
        }

        paint.setTextAlign(Align.CENTER);
        paint.setColor(titleColor);
        canvas.drawText(title, (graphwidth / 2) + leftBorder, lowerBorder - 4, paint);

        if (maxY != minY) {
            paint.setStrokeCap(Paint.Cap.ROUND);

            for (int i=0; i<graphSeries.size(); i++) {
                paint.setStrokeWidth(graphSeries.get(i).style.thickness);
                paint.setColor(graphSeries.get(i).style.color);
                drawSeries(canvas, _values(i), graphwidth, graphheight, lowerBorder, minX, minY, diffX, diffY, leftBorder);
            }

            if (showLegend) drawLegend(canvas, height, width);
        }
    }

    private void onMoveGesture(float f) {
        // view port update
        if (viewportSize != 0) {
            viewportStart -= f*viewportSize/graphwidth;

            // minimal and maximal view limit
            double minX = getMinX(true);
            double maxX = getMaxX(true);
            if (viewportStart < minX) {
                viewportStart = minX;
            } else if (viewportStart+viewportSize > maxX) {
                viewportStart = maxX - viewportSize;
            }

            // labels have to be regenerated
            horlabels = null;
            verlabels = null;
        }
        invalidate();
    }

    /**
     * @param event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isScrollable()) {
            return super.onTouchEvent(event);
        }

        boolean handled = false;
        // first scale
        if (scalable && scaleDetector != null) {
            scaleDetector.onTouchEvent(event);
            handled = scaleDetector.isInProgress();
        }
        if (!handled) {
            // if not scaled, scroll
            if ((event.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN) {
                handled = true;
            }
            if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
                lastTouchEventX = 0;
                handled = true;
            }
            if ((event.getAction() & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE) {
                if (lastTouchEventX != 0) {
                    onMoveGesture(event.getX() - lastTouchEventX);
                }
                lastTouchEventX = event.getX();
                handled = true;
            }
            if (handled)
                invalidate();
        }
        return handled;
    }

    /**
     * one data set for a graph series
     */
    static public class GraphViewData {
        public final double valueX;
        public final double valueY;
        public GraphViewData(double valueX, double valueY) {
            super();
            this.valueX = valueX;
            this.valueY = valueY;
        }
    }

    public enum LegendAlign {
        TOP, MIDDLE, BOTTOM
    }

    private void drawLabels(Canvas canvas) {
        paint.setStrokeWidth(0);

        float height = getHeight();
        float graphheight = height - (2 * lowerBorder);

        if (verlabels == null) {
            verlabels = generateVerlabels(graphheight);
        }

        // vertical labels
        paint.setTextAlign(Align.LEFT);
        int vers = verlabels.length - 1;
        // draw from top to bottom
        for (int i = 1; i <= verlabels.length; i++) {
            int labelIdx = verlabels.length - i;
            float y = ((graphheight / vers) * labelIdx) + lowerBorder;
            labelPaint.setColor(verticalLabelColor);
            setLabelPaintSize(verticalLabelTextSize);
            canvas.drawText(verlabels[i-1], 0, y, labelPaint);
        }
    }

    protected final Paint paint;
    protected final Paint labelPaint;
    private String[] horlabels;
    private String[] verlabels;
    private String title = "";
    private boolean scrollable;
    private double viewportStart;
    private double viewportSize;
    private ScaleGestureDetector scaleDetector;
    private boolean scalable;
    private NumberFormat numberformatter;
    private final List<GraphViewSeries> graphSeries = new ArrayList<GraphViewSeries>();
    private boolean showLegend = false;
    private float legendWidth = 120;
    private LegendAlign legendAlign = LegendAlign.MIDDLE;
    private boolean manualYAxis;
    private double manualMaxYValue;
    private double manualMinYValue;

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    private GraphViewData[] _values(int idxSeries) {
        GraphViewData[] values = graphSeries.get(idxSeries).values;
        if (viewportStart == 0 && viewportSize == 0) {
            // all data
            return values;
        } else {
            // viewport
            List<GraphViewData> listData = new ArrayList<GraphViewData>();
            for (int i=0; i<values.length; i++) {
                if (values[i].valueX >= viewportStart) {
                    if (values[i].valueX > viewportStart+viewportSize) {
                        listData.add(values[i]); // one more for nice scrolling
                        break;
                    } else {
                        listData.add(values[i]);
                    }
                } else {
                    if (listData.isEmpty()) {
                        listData.add(values[i]);
                    }
                    listData.set(0, values[i]); // one before, for nice scrolling
                }
            }
            return listData.toArray(new GraphViewData[listData.size()]);
        }
    }

    public void addSeries(GraphViewSeries series) {
        series.addGraphView(this);
        graphSeries.add(series);
        // Flush all labels as this may alter mins/maxs
        verlabels = null;
        horlabels = null;
        // Redraw
        invalidate();
    }

    protected void drawLegend(Canvas canvas, float height, float width) {
        int shapeSize = 15;

        // rect
        paint.setARGB(180, 100, 100, 100);
        float legendHeight = (shapeSize+5)*graphSeries.size() +5;
        float lLeft = width-legendWidth - 10;
        float lTop;
        switch (legendAlign) {
            case TOP:
                lTop = 10;
                break;
            case MIDDLE:
                lTop = height/2 - legendHeight/2;
                break;
            default:
                lTop = height - lowerBorder - legendHeight -10;
        }
        float lRight = lLeft+legendWidth;
        float lBottom = lTop+legendHeight;
        canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, paint);

        for (int i=0; i<graphSeries.size(); i++) {
            paint.setColor(graphSeries.get(i).style.color);
            canvas.drawRect(new RectF(lLeft+5, lTop+5+(i*(shapeSize+5)), lLeft+5+shapeSize, lTop+((i+1)*(shapeSize+5))), paint);
            if (graphSeries.get(i).description != null) {
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Align.LEFT);
                canvas.drawText(graphSeries.get(i).description, lLeft+5+shapeSize+5, lTop+shapeSize+(i*(shapeSize+5)), paint);
            }
        }
    }

    abstract public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart);

    /**
     * formats the label
     * can be overwritten
     * @param value x and y values
     * @param isValueX if false, value y wants to be formatted
     * @return value to display
     */
    protected String formatLabel(double value, boolean isValueX) {
        if (numberformatter == null) {
            numberformatter = NumberFormat.getNumberInstance();
            double highestvalue = getMaxY();
            double lowestvalue = getMinY();
            if (highestvalue - lowestvalue < 0.1) {
                numberformatter.setMaximumFractionDigits(6);
            } else if (highestvalue - lowestvalue < 1) {
                numberformatter.setMaximumFractionDigits(4);
            } else if (highestvalue - lowestvalue < 20) {
                numberformatter.setMaximumFractionDigits(3);
            } else if (highestvalue - lowestvalue < 100) {
                numberformatter.setMaximumFractionDigits(1);
            } else {
                numberformatter.setMaximumFractionDigits(0);
            }
        }
        return numberformatter.format(value);
    }

    private String[] generateHorlabels(float graphwidth) {
        return horizontalLabelGenerator.generateLabels(graphwidth, getMinX(false), getMaxX(false));
    }

    synchronized private String[] generateVerlabels(float graphheight) {
        return verticalLabelGenerator.generateLabels(graphheight, getMinY(), getMaxY());
    }

    public void setHorizontalLabelGenerator(LabelGenerator horizontalLabelGenerator) {
        this.horizontalLabelGenerator = horizontalLabelGenerator;
    }

    public void setVerticalLabelGenerator(LabelGenerator verticalLabelGenerator) {
        this.verticalLabelGenerator = verticalLabelGenerator;
    }

    public LegendAlign getLegendAlign() {
        return legendAlign;
    }

    public float getLegendWidth() {
        return legendWidth;
    }

    /**
     * returns the maximal X value of the current viewport (if viewport is set)
     * otherwise maximal X value of all data.
     * @param ignoreViewport
     *
     * warning: only override this, if you really know want you're doing!
     */
    protected double getMaxX(boolean ignoreViewport) {
        // if viewport is set, use this
        if (!ignoreViewport && viewportSize != 0) {
            return viewportStart+viewportSize;
        } else {
            // otherwise use the max x value
            // values must be sorted by x, so the last value has the largest X value
            double highest = 0;
            if (graphSeries.size() > 0)
            {
                GraphViewData[] values = graphSeries.get(0).values;
                highest = values[values.length-1].valueX;
                for (int i=1; i<graphSeries.size(); i++) {
                    values = graphSeries.get(i).values;
                    highest = Math.max(highest, values[values.length-1].valueX);
                }
            }
            return highest;
        }
    }

    /**
     * returns the maximal Y value of all data.
     *
     * warning: only override this, if you really know want you're doing!
     */
    protected double getMaxY() {
        double largest;
        if (maxY != null) {
            largest = maxY;
        } else {
            largest = Integer.MIN_VALUE;
            for (int i=0; i<graphSeries.size(); i++) {
                GraphViewData[] values = _values(i);
                for (int ii=0; ii<values.length; ii++)
                    if (values[ii].valueY > largest)
                        largest = values[ii].valueY;
            }
        }
        return largest;
    }

    /**
     * returns the minimal X value of the current viewport (if viewport is set)
     * otherwise minimal X value of all data.
     * @param ignoreViewport
     *
     * warning: only override this, if you really know want you're doing!
     */
    protected double getMinX(boolean ignoreViewport) {
        // if viewport is set, use this
        if (!ignoreViewport && viewportSize != 0) {
            return viewportStart;
        } else {
            // otherwise use the min x value
            // values must be sorted by x, so the first value has the smallest X value
            double lowest = 0;
            if (graphSeries.size() > 0)
            {
                GraphViewData[] values = graphSeries.get(0).values;
                lowest = values[0].valueX;
                for (int i=1; i<graphSeries.size(); i++) {
                    values = graphSeries.get(i).values;
                    lowest = Math.min(lowest, values[0].valueX);
                }
            }
            return lowest;
        }
    }

    /**
     * returns the minimal Y value of all data.
     *
     * warning: only override this, if you really know want you're doing!
     */
    protected double getMinY() {
        double smallest;
        if (minY != null) {
            smallest = minY;
        } else {
            smallest = Integer.MAX_VALUE;
            for (int i=0; i<graphSeries.size(); i++) {
                GraphViewData[] values = _values(i);
                for (int ii=0; ii<values.length; ii++)
                    if (values[ii].valueY < smallest)
                        smallest = values[ii].valueY;
            }
        }
        return smallest;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public void redrawAll() {
        verlabels = null;
        horlabels = null;
        numberformatter = null;
        invalidate();
    }

    public void removeSeries(GraphViewSeries series)
    {
        graphSeries.remove(series);
    }

    public void removeSeries(int index)
    {
        if (index < 0 || index >= graphSeries.size())
        {
            throw new IndexOutOfBoundsException("No series at index " + index);
        }

        graphSeries.remove(index);
    }

    public void scrollToEnd() {
        if (!scrollable) throw new IllegalStateException("This GraphView is not scrollable.");
        double max = getMaxX(true);
        viewportStart = max-viewportSize;
        redrawAll();
    }

    /**
     * set's static horizontal labels (from left to right)
     * @param horlabels if null, labels were generated automatically
     */
    public void setHorizontalLabels(String[] horlabels) {
        this.horlabels = horlabels;
    }

    public void setLegendAlign(LegendAlign legendAlign) {
        this.legendAlign = legendAlign;
    }

    public void setLegendWidth(float legendWidth) {
        this.legendWidth = legendWidth;
    }

    /**
     * you have to set the bounds {@link #setManualYAxisBounds(float, float)}. That automatically enables manualYAxis-flag.
     * if you want to disable the menual y axis, call this method with false.
     * @param manualYAxis
     */
    public void setManualYAxis(boolean manualYAxis) {
        this.manualYAxis = manualYAxis;
    }

    /**
     * set manual Y axis limit
     * @param max
     * @param min
     */
    public void setManualYAxisBounds(float max, float min) {
        maxY = max;
        minY = min;
        manualYAxis = true;
        // Flush vertical labels
        verlabels = null;
    }

    /**
     * this forces scrollable = true
     * @param scalable
     */
    synchronized public void setScalable(boolean scalable) {
        this.scalable = scalable;
        if (scalable == true && scaleDetector == null) {
            scrollable = true; // automatically forces this
            scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    double center = viewportStart + viewportSize / 2;
                    viewportSize /= detector.getScaleFactor();
                    viewportStart = center - viewportSize / 2;

                    // viewportStart must not be < minX
                    double minX = getMinX(true);
                    if (viewportStart < minX) {
                        viewportStart = minX;
                    }

                    // viewportStart + viewportSize must not be > maxX
                    double maxX = getMaxX(true);
                    double overlap = viewportStart + viewportSize - maxX;
                    if (overlap > 0) {
                        // scroll left
                        if (viewportStart-overlap > minX) {
                            viewportStart -= overlap;
                        } else {
                            // maximal scale
                            viewportStart = minX;
                            viewportSize = maxX - viewportStart;
                        }
                    }
                    redrawAll();
                    return true;
                }
            });
        }
    }

    /**
     * the user can scroll (horizontal) the graph. This is only useful if you use a viewport {@link #setViewPort(double, double)} which doesn't displays all data.
     * @param scrollable
     */
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    /**
     * set's static vertical labels (from top to bottom)
     * @param verlabels if null, labels were generated automatically
     */
    public void setVerticalLabels(String[] verlabels) {
        this.verlabels = verlabels;
    }

    /**
     * set's the viewport for the graph.
     * @param start x-value
     * @param size
     */
    public void setViewPort(double start, double size) {
        viewportStart = start;
        viewportSize = size;
    }

    public void setLabelPaintSize(int dpSize) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        float newSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, dpSize, r.getDisplayMetrics());
        if(newSize != labelPaint.getTextSize()) {
            labelPaint.setTextSize(newSize);
        }
    }
}
