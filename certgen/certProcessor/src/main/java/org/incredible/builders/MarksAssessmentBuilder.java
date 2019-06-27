package org.incredible.builders;

import org.incredible.pojos.MarksAssessment;

public class MarksAssessmentBuilder implements IBuilder<MarksAssessment> {
  private   MarksAssessment marksAssessment;


    public MarksAssessmentBuilder setMinValue(float minValue) {
        marksAssessment.setMinValue(minValue);
        return this;
    }


    public MarksAssessmentBuilder setMaxValue(float maxValue) {
        marksAssessment.setMaxValue(maxValue);
        return this;
    }


    public MarksAssessmentBuilder setPassValue(float passValue) {
        marksAssessment.setPassValue(passValue);
        return this;
    }

    @Override
    public MarksAssessment build() {
        return this.marksAssessment;
    }
}
