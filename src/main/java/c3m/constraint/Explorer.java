package c3m.constraint;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;

import c3m.lod.SparqlQuerier;
import c3m.lod.Triplestore;

public class Explorer {
	
	private static Logger logger = Logger.getLogger(Explorer.class);
	
	private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(10000);
	private Triplestore triplestore;
	private double minLikelihood;
	private double delta;
	
	public Explorer(Triplestore triplestore, double minLikelihood, double delta) {
		this.triplestore = triplestore;
		this.minLikelihood = minLikelihood;
		this.delta = delta;
	}
	
	public void explore(int threadNb, int skipNb) {
		logger.debug("preparing the relations to explore...");
		findRelations();
		logger.debug(queue.size() + " relations to explore.");
		logger.debug("computing the hierarchy...");
		ContextExplorer.configure(triplestore, minLikelihood, delta);
		logger.debug("exploring the relations...");
		for (int i = 0; i < skipNb; i++)
			try {
				String element = queue.take();
				logger.debug("skip " + element);
			} catch (InterruptedException e) {
				logger.warn(e);
			}
		ArrayList<ExplorerThread> threads = new ArrayList<>();
		for (int i = 0; i < threadNb; i++)
			threads.add(new ExplorerThread());
		for (ExplorerThread et : threads)
			et.start();
		try {
			for (ExplorerThread et : threads)
				et.join();
		} catch (InterruptedException e) {
			logger.error(e, e);
		}
	}

	private void findRelations() {
		int minimalCount = (int) Math.floor(Hoeffding.getMinimalCount(minLikelihood, delta));
		String queryStr = "SELECT ?relation (COUNT(?entity) AS ?count) " + triplestore.getGraph() + 
				"WHERE { ?entity ?relation ?object }  " + 
				"GROUP BY ?relation "  + 
				"HAVING (COUNT(*) >= " + minimalCount + ") " + 
				"ORDER BY DESC(?count)";
		try {
			new SparqlQuerier(triplestore, queryStr) {
				
				@Override
				public boolean fact(QuerySolution qs) throws InterruptedException {
					queue.put(qs.getResource("relation").toString());
					return true;
				}
				
				@Override
				public void end() {
				}
				
				@Override
				public void begin() {
				}
			}.execute();
		} catch (InterruptedException e) {
			logger.error(e, e);
		}
	}
	
	public class ExplorerThread extends Thread {

		@Override
		public void run() {
			logger.info("start thread " + getName());
			while (queue.size() > 0) {
				String relation = "";
				try {
					relation = queue.take();
					logger.debug(relation + " (" + queue.size() + ")");
					ContextExplorer ce = new ContextExplorer(triplestore, relation, minLikelihood, delta);
					ce.explore();
				} catch (InterruptedException e) {
					logger.warn(e);
				}
			}
			logger.info("stop thread " + getName());
		}
	
	}

}
