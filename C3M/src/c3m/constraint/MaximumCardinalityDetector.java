package c3m.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

public class MaximumCardinalityDetector {
	
	private static Logger logger = Logger.getLogger(MaximumCardinalityDetector.class);

	
	public double delta = 0.01;
	public double minLower = 0.97;
	public double confidence = minLower;
	public int maximumCardinality = Integer.MAX_VALUE;
	
	private ArrayList<CountPerCardinality> countPerCardinalities = new ArrayList<>();


	public class CountPerCardinality  {
		public CountPerCardinality(int cardinality, int count) {
			this.cardinality = cardinality;
			this.count = count;
		}
		public int count = 0;
		public int cardinality = 0;
	}
	
	public void addCountPerCardinality(int cardinality, int count) {
		countPerCardinalities.add(new CountPerCardinality(cardinality, count));
	}

	public double getMinimalCount() {
		double epsilon = 1 - minLower;
		return 0.5 * Math.log(1 / delta) / (epsilon * epsilon);
	}

	public int analyze() {
		Collections.sort(countPerCardinalities, new Comparator<CountPerCardinality>() {
			@Override
			public int compare(CountPerCardinality o1, CountPerCardinality o2) {
				return o2.cardinality - o1.cardinality;
			}
		});
		confidence = minLower;
		maximumCardinality = Integer.MAX_VALUE;
		int n = 0;
		for (int i = 0; i < countPerCardinalities.size(); i++) {
			int cardinality = countPerCardinalities.get(i).cardinality;
			int count = countPerCardinalities.get(i).count;
			n += count;
			double avg = ((double)count) / n;
			double error = Math.sqrt(Math.log(1 / delta) / (2 * n));
			double lower = Math.max(avg - error, 0);
			if (lower > confidence) {
				maximumCardinality = cardinality;
				confidence = lower; 
			}
		}
		return maximumCardinality;
	}
	
	public int getMaximumCardinality() {
		return maximumCardinality;
	}

	public double getLikelihood() {
		return confidence;
	}

}
