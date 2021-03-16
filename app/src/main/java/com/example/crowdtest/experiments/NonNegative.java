package com.example.crowdtest.experiments;

import com.example.crowdtest.Experimenter;

import java.util.ArrayList;
import java.util.Collection;

public class NonNegative extends Experiment {
    private Collection <NonNegativeTrial> trials;

    /**
     * Experiment constructor
     *
     * @param owner         Owner of the experiment
     * @param experimentID  A unique ID for this experiment
     */
    public NonNegative(Experimenter owner, String experimentID) {
        super(owner, experimentID);
        trials = new ArrayList<>();
    }

    @Override
    public void addTrial(Trial trial) {
        trials.add((NonNegativeTrial) trial);
    }
}
