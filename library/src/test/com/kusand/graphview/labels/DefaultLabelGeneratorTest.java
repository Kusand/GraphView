package com.kusand.graphview.labels;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class DefaultLabelGeneratorTest {

    private final static float DEFAULT_SPACING = 80;

    private final static float DEFAULT_WIDTH = 320;
    private final static float DEFAULT_MIN = 0;
    private final static float DEFAULT_MAX = 1;

    private final static String[] EXPECTED_DEFAULT_ZERO_TO_ONE_LABELS = {"0", "0.25", "0.5", "0.75", "1"};

    @Test
    public void set_spacing_updates_value_correctly() throws Exception {
        DefaultLabelGenerator testGenerator = new DefaultLabelGenerator(0);
        testGenerator.setSpacing(DEFAULT_SPACING);
        assertEquals(DEFAULT_SPACING, testGenerator.getSpacing());
    }

    @Test
    public void correct_label_count_generated_for_default_width_and_spacing() throws Exception {
        DefaultLabelGenerator testGenerator = new DefaultLabelGenerator(DEFAULT_SPACING);
        String[] labels = testGenerator.generateLabels(DEFAULT_WIDTH, DEFAULT_MIN, DEFAULT_MAX);
        assertEquals(labels.length, EXPECTED_DEFAULT_ZERO_TO_ONE_LABELS.length);
    }

    @Test
    public void correct_labels_generated_for_zero_to_one_values_on_default_width_and_spacing() {
        DefaultLabelGenerator testGenerator = new DefaultLabelGenerator(DEFAULT_SPACING);
        String[] labels = testGenerator.generateLabels(DEFAULT_WIDTH, DEFAULT_MIN, DEFAULT_MAX);
        for(int i = 0; i < labels.length; i++) {
            assertEquals(labels[i], EXPECTED_DEFAULT_ZERO_TO_ONE_LABELS[i]);
        }
    }
}
