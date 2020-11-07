package c3m;

import org.apache.log4j.Logger;

import c3m.constraint.Explorer;
import c3m.lod.Triplestore;

public class C3M {
	
	private static Logger logger = Logger.getLogger(C3M.class);
		
	public static String NAME = "C3M";
	public static String VERSION = "iswc19";
	private static Triplestore triplestore = Triplestore.DBPEDIA;
	private static double minLikelihood = 0.97;
	private static double delta = 0.01;


	public static void main(String[] args) {
		logger.info(NAME + " version " + VERSION);
		Explorer explorer = new Explorer(triplestore, minLikelihood, delta);
		explorer.explore(4, 2);
	}

}
