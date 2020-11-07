package c3m.constraint;

import java.util.ArrayList;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;

import com.github.andrewoma.dexx.collection.HashMap;

import c3m.lod.SparqlQuerier;
import c3m.lod.Triplestore;

public class Hierarchy extends SparqlQuerier {
	
	private static Logger logger = Logger.getLogger(Hierarchy.class);
	
	private  HashMap<String, ArrayList<String>> subclasses = new HashMap<String, ArrayList<String>>();
	String previous = "";
	ArrayList<String> subs = new ArrayList<>();


	public Hierarchy(Triplestore triplestore, double minLikelihood, double delta) {
		super(triplestore);
		int minimalCount = (int) Math.floor(Hoeffding.getMinimalCount(minLikelihood, delta));
		String queryStr = "SELECT DISTINCT ?sub ?sup " + triplestore.getGraph() +
				"WHERE { " + 
				"?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?sup . " + 
				"{ " + 
				"SELECT ?sub (COUNT(*) AS ?count) WHERE {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?sub} " + 
				"GROUP BY ?sub " + 
				"HAVING (COUNT(*) >= " + minimalCount + ") " + 
				"ORDER BY DESC(?count) " + 
				"} " + 
				"} " + 
				"ORDER BY ?sup";
		setQuery(queryStr);
		//logger.debug(queryStr);
		try {
			this.execute();
		} catch (InterruptedException e) {
			logger.error(e, e);
		}
	}
	
	public ArrayList<String> getSubclasses(String sup) {
		return subclasses.get(sup);
	}

	@Override
	public void begin() {
	}

	@Override
	public void end() {
	}

	@Override
	public boolean fact(QuerySolution qs) throws InterruptedException {
		String sub = qs.get("sub").asResource().toString();
		String sup = qs.get("sup").asResource().toString();
		if (sup.equals(previous))
			subs.add(sub);
		else {
			subclasses = subclasses.put(previous, subs);
			previous = sup;
			subs = new ArrayList<>();
			subs.add(sub);
		}
		return true;
	}
	
}
