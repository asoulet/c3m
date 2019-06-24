package c3m.constraint;

public class Hoeffding {
	
	public static double getMinimalCount(double minLikelihood, double delta) {
		double epsilon = 1 - minLikelihood;
		return 0.5 * Math.log(1 / delta) / (epsilon * epsilon);
	}


}
