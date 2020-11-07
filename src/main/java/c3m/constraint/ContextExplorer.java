package c3m.constraint;

import java.util.ArrayList;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;

import c3m.lod.SparqlQuerier;
import c3m.lod.Triplestore;

public class ContextExplorer {
	
	private static Logger logger = Logger.getLogger(ContextExplorer.class);
	
	private static Hierarchy hierarchy; 
	
	public class Type {
		
		public String label;
		public int maximumSubtypeCardinality = Integer.MAX_VALUE;
		
		public Type(String type, int maximumCardinality) {
			this.label = type;
			this.maximumSubtypeCardinality = maximumCardinality;
		}
	}

	private ArrayList<Type> types = new ArrayList<>();

	private String relation;

	private double minLikelihood;

	private double delta;

	private Triplestore triplestore;

	public ContextExplorer(Triplestore triplestore, String relation, double minLikelihood, double delta) {
		this.triplestore = triplestore;
		this.relation = relation;
		this.minLikelihood = minLikelihood;
		this.delta = delta;
	}
	
	public static void configure(Triplestore triplestore, double minLikelihood, double delta) {
		hierarchy = new Hierarchy(triplestore, minLikelihood, delta);
	}
	
	public void populate(int maximumCardinality) {
		int minimalCount = (int) Math.floor(Hoeffding.getMinimalCount(minLikelihood, delta));
		String queryStr = "SELECT ?type (COUNT(?entity) AS ?count) FROM <http://dbpedia.org> WHERE { " + 
				"?entity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type .  " + 
				"FILTER EXISTS {?entity <" + relation + "> ?object} " + 
				"}  " + 
				"GROUP BY ?type " + 
				"HAVING (COUNT(*) >= " + minimalCount + ") " + 
				"ORDER BY DESC(?count)";
		try {
		new SparqlQuerier(triplestore, queryStr) {
			
			@Override
			public boolean fact(QuerySolution qs) throws InterruptedException {
				types.add(new Type(qs.get("type").asResource().toString(), maximumCardinality));
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
			logger.error(e);
		}		
	}

	public ArrayList<Type> getTypes() {
		return types;
	}
	
	public void explore() {
		SparqlMaximumCardinalityDetector detector = new SparqlMaximumCardinalityDetector(triplestore);
		int maximumCardinality = detector.detect(null, relation);
		if (maximumCardinality < Integer.MAX_VALUE)
			generateContextualMaximumConstraint(null, relation, maximumCardinality, detector.getLikelihood());
		if (maximumCardinality > 1) {
			populate(maximumCardinality);
			int startIndex = 0;
			for (Type type : types) {
				if (type.maximumSubtypeCardinality > 1) {
					detector = new SparqlMaximumCardinalityDetector(triplestore);
					maximumCardinality = detector.detect(type.label, relation);
					if (maximumCardinality < type.maximumSubtypeCardinality) {
						generateContextualMaximumConstraint(type.label, relation, maximumCardinality, detector.getLikelihood());
						updateTypesHierarchy(type.label, maximumCardinality, startIndex);
					}
				}
				startIndex++;
			}
		}
	}

	private void generateContextualMaximumConstraint(String label, String r, int maximumCardinality, double likelihood) {
		String SEP = ";";
		logger.info(label + SEP + r + SEP + maximumCardinality + SEP + likelihood);
	}
	
	private void updateTypesHierarchy(String label, int maximumCardinality, int startIndex) {
		ArrayList<String> subclasses = hierarchy.getSubclasses(label);
		if (subclasses != null) {
			int k = 0;
			for (String sub : subclasses) {
				int index = typesIndexOf(sub, startIndex);
				if (index >= 0) {
					types.get(index).maximumSubtypeCardinality = Math.min(types.get(index).maximumSubtypeCardinality, maximumCardinality);
				}
				k++;
			}
		}
	}

	private void updateTypes(String label, int maximumCardinality, int startIndex) {
		int minimalCount = (int) Math.floor(Hoeffding.getMinimalCount(minLikelihood, delta));
		String queryStr = "SELECT ?type (COUNT(?entity) AS ?count) WHERE { " + 
				"?entity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type . " + 
				"FILTER EXISTS {?entity <" + relation + "> ?object} . " + 
				"{ " + 
				"SELECT ?type WHERE { " + 
				"?type <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ <" + label + "> " + 
				"} " + 
				"} " + 
				"} " + 
				"GROUP BY ?type " + 
				"HAVING (COUNT(*) >= " + minimalCount + ") " + 
				"ORDER BY DESC(?count)";
		try {
			new SparqlQuerier(triplestore, queryStr) {
				
				@Override
				public boolean fact(QuerySolution qs) throws InterruptedException {
					int index = typesIndexOf(qs.get("type").asResource().toString(), startIndex);
					if (index >= 0) {
						types.get(index).maximumSubtypeCardinality = Math.min(types.get(index).maximumSubtypeCardinality, maximumCardinality);
					}
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

	protected int typesIndexOf(String string, int startIndex) {
		for (int i = startIndex + 1; i < types.size(); i++)
			if (types.get(i).label.equals(string))
				return i;
		return -1;
	}

}
