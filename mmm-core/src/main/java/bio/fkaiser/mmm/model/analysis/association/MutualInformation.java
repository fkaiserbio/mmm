package bio.fkaiser.mmm.model.analysis.association;


import java.util.Iterator;

/**
 * @author fk
 */
public class MutualInformation {

    public static double calculate(double[] var0, double[] var1) {
        JointProbabilityState var4 = new JointProbabilityState(var0, var1);
        int var5 = var4.getFirstMaxVal();
        double var12 = 0.0D;
        Iterator var14 = var4.getJointProbMap().keySet().iterator();

        while (var14.hasNext()) {
            Integer var15 = (Integer) var14.next();
            double var6 = var4.getJointProbMap().get(var15);
            double var8 = var4.getFirstProbMap().get(var15 % var5);
            double var10 = var4.getSecondProbMap().get(var15 / var5);
            if (var6 > 0.0D && var8 > 0.0D && var10 > 0.0D) {
                var12 += var6 * Math.log(var6 / var8 / var10);
            }
        }
        var12 /= Math.log(2.0);
        return var12;
    }
}
