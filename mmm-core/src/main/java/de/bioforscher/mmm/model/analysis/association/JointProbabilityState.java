package de.bioforscher.mmm.model.analysis.association;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author fk
 */
public class JointProbabilityState {

    private final int firstMaxVal;
    private final int secondMaxVal;
    private final int jointMaxVal;
    private final Map<Integer, Double> firstProbMap;
    private final Map<Integer, Double> secondProbMap;
    private final Map<Integer, Double> jointProbMap;

    public JointProbabilityState(double[] observations1, double[] observations2) {

        firstProbMap = new HashMap<>();
        secondProbMap = new HashMap<>();
        jointProbMap = new HashMap<>();
        int observationLength = observations1.length;
        int[] normalizedObservations1 = new int[observationLength];
        int[] normalizedObservations2 = new int[observationLength];
        this.firstMaxVal = ProbabilityState.normaliseArray(observations1, normalizedObservations1);
        this.secondMaxVal = ProbabilityState.normaliseArray(observations2, normalizedObservations2);
        this.jointMaxVal = this.firstMaxVal * this.secondMaxVal;
        HashMap<Integer, Integer> var13 = new HashMap<>();
        HashMap<Integer, Integer> var14 = new HashMap<>();
        HashMap<Integer, Integer> var15 = new HashMap<>();

        for (int i = 0; i < observationLength; i++) {
            int var3 = normalizedObservations1[i];
            int var4 = normalizedObservations2[i];
            int var5 = var3 + this.firstMaxVal * var4;
            Integer var6 = var5;
            Integer var7 = var13.remove(var6);
            if (var7 == null) {
                var13.put(var6, 1);
            } else {
                var13.put(var6, var7 + 1);
            }

            var6 = var3;
            var7 = var14.remove(var6);
            if (var7 == null) {
                var14.put(var6, 1);
            } else {
                var14.put(var6, var7 + 1);
            }

            var6 = var4;
            var7 = var15.remove(var6);
            if (var7 == null) {
                var15.put(var6, 1);
            } else {
                var15.put(var6, var7 + 1);
            }
        }

        Iterator var18 = var13.keySet().iterator();

        Integer var17;
        while (var18.hasNext()) {
            var17 = (Integer) var18.next();
            this.jointProbMap.put(var17, (double) var13.get(var17) / observationLength);
        }

        var18 = var14.keySet().iterator();

        while (var18.hasNext()) {
            var17 = (Integer) var18.next();
            this.firstProbMap.put(var17, (double) var14.get(var17) / observationLength);
        }

        var18 = var15.keySet().iterator();

        while (var18.hasNext()) {
            var17 = (Integer) var18.next();
            this.secondProbMap.put(var17, (double) var15.get(var17) / observationLength);
        }

    }

    public int getFirstMaxVal() {
        return firstMaxVal;
    }

    public int getSecondMaxVal() {
        return secondMaxVal;
    }

    public int getJointMaxVal() {
        return jointMaxVal;
    }

    public Map<Integer, Double> getFirstProbMap() {

        return firstProbMap;
    }

    public Map<Integer, Double> getSecondProbMap() {
        return secondProbMap;
    }

    public Map<Integer, Double> getJointProbMap() {
        return jointProbMap;
    }
}
