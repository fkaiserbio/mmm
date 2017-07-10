package de.bioforscher.mmm.model.analysis.association;

import de.bioforscher.mmm.model.Itemset;

import java.util.Random;

/**
 * @author fk
 */
@Deprecated
public class MutualInformation {

    private Itemset<?> itemsetOne;
    private Itemset<?> itemsetTwo;

    public MutualInformation(Itemset<?> itemsetOne, Itemset<?> itemsetTwo) {
        this.itemsetOne = itemsetOne;
        this.itemsetTwo = itemsetTwo;
        calculateMutualInformation();
    }

    private static double getProbabilityForItemset(Itemset<?> itemset) {
        return new Random().nextDouble() <= itemset.getSupport() ? 1.0 : 0.0;
    }

    private double calculateJointProbabilty() {
        if (getProbabilityForItemset(itemsetOne) == 1.0 && getProbabilityForItemset(itemsetTwo) == 1.0) {
            return itemsetOne.getSupport();
        } else if (getProbabilityForItemset(itemsetOne) == 1.0 && getProbabilityForItemset(itemsetTwo) == 0.0) {
            return 0.0;
        } else if (getProbabilityForItemset(itemsetOne) == 0.0 && getProbabilityForItemset(itemsetTwo) == 1.0) {
            return itemsetTwo.getSupport() - itemsetOne.getSupport();
        } else {
            return 1.0 - itemsetTwo.getSupport();
        }
    }

    private void calculateMutualInformation() {
        double mutualInformation = 0.0;
        for (int i = 0; i < 10000; i++) {
            double frac = getProbabilityForItemset(itemsetOne) * getProbabilityForItemset(itemsetTwo);
            System.out.println("frac:" + frac);
            double jointProbability = calculateJointProbabilty();
            System.out.println("jp:" + jointProbability);
            mutualInformation += jointProbability * (Math.log10(jointProbability / (frac)) / Math.log(2));
            System.out.println("mi:" + mutualInformation);
        }
        System.out.println(mutualInformation);
    }
}
