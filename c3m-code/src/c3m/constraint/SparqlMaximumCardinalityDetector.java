package c3m.constraint;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;

import c3m.lod.SparqlQuerier;
import c3m.lod.Triplestore;

public class SparqlMaximumCardinalityDetector extends SparqlQuerier {
	
	private static Logger logger = Logger.getLogger(SparqlMaximumCardinalityDetector.class);

	private MaximumCardinalityDetector mcd = new MaximumCardinalityDetector();
	
	public SparqlMaximumCardinalityDetector(Triplestore triplestore) {
		super(triplestore);
	}

	@Override
	public void begin() {
	}

	@Override
	public void end() {
	}

	@Override
	public boolean fact(QuerySolution qs) throws InterruptedException {
		mcd.addCountPerCardinality(qs.get("cardinality").asLiteral().getInt(), qs.get("count").asLiteral().getInt());
		return true;
	}
	
	public int detect(String type, String relation) {
		String restriction = "";
		if (type !=null && type.length() > 0)
			restriction = "?entity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + type + "> .";
		String queryStr = ""
				+ "SELECT ?cardinality (COUNT(?entity) AS ?count) " + triplestore.getGraph() + "WHERE {" + 
				"SELECT ?entity (COUNT(?relation) AS ?cardinality) WHERE {" + restriction + " ?entity <" + relation + "> ?relation} GROUP BY ?entity" + 
				"} " + 
				"GROUP BY ?cardinality " + 
				"ORDER BY DESC(?cardinality) ";
		this.setQuery(queryStr);
		mcd = new MaximumCardinalityDetector();
		try {
			this.execute();
		} catch (InterruptedException e) {
			logger.error(e);
		}
		return mcd.analyze();
	}

	public double getLikelihood() {
		return mcd.getLikelihood();
	}

}
