package com.kusand.graphview.labels;

import java.text.NumberFormat;

public class DefaultLabelGenerator implements LabelGenerator {

    private float spacing;
    private NumberFormat numberFormatter = NumberFormat.getNumberInstance();

    public DefaultLabelGenerator(float spacing) {
        this.spacing = spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public float getSpacing() {
        return spacing;
    }

    @Override
    public String[] generateLabels(float rangeWidth, double min, double max) {
        String[] labels = new String[getLabelCount(rangeWidth)];
        for(int labelIdx = 0; labelIdx < labels.length; labelIdx++) {
            labels[labelIdx] = formatLabel(min + ((max-min)*labelIdx/(labels.length-1)), min, max);
        }
        return labels;
    }

    private String formatLabel(double value, double lowestValue, double highestValue) {
        if (highestValue - lowestValue < 0.1) {
            numberFormatter.setMaximumFractionDigits(6);
        } else if (highestValue - lowestValue < 1) {
            numberFormatter.setMaximumFractionDigits(4);
        } else if (highestValue - lowestValue < 20) {
            numberFormatter.setMaximumFractionDigits(3);
        } else if (highestValue - lowestValue < 100) {
            numberFormatter.setMaximumFractionDigits(1);
        } else {
            numberFormatter.setMaximumFractionDigits(0);
        }
        return numberFormatter.format(value);
    }

    private int getLabelCount(float rangeWidth) {
        return ((int) (rangeWidth/spacing) + 1);
    }
}
