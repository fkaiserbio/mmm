package bio.fkaiser.mmm.model.analysis.association;

/**
 * @author fk
 */
public class ProbabilityState {

    private ProbabilityState() {

    }

    public static int normaliseArray(double[] input, int[] output) {
        int maxValue = 0;
        int inputLength = input.length;
        if (inputLength > 0) {
            int minvalue = (int) Math.floor(input[0]);
            maxValue = (int) Math.floor(input[0]);

            for (int i = 0; i < inputLength; ++i) {
                int currentValue = (int) Math.floor(input[i]);
                output[i] = currentValue;
                if (currentValue < minvalue) {
                    minvalue = currentValue;
                }

                if (currentValue > maxValue) {
                    maxValue = currentValue;
                }
            }

            for (int i = 0; i < inputLength; ++i) {
                output[i] -= minvalue;
            }

            maxValue = maxValue - minvalue + 1;
        }

        return maxValue;
    }
}
