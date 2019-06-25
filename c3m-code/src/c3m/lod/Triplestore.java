package c3m.lod;

public enum Triplestore {
	DBPEDIA("https://dbpedia.org/sparql","http://dbpedia.org"),
	YAGO("https://linkeddata1.calcul.u-psud.fr/sparql"),
	EUROPEANA("http://sparql.europeana.eu/"),
	BNF("https://data.bnf.fr/sparql");
	
	private String endpoint;
	private String graph;

	private Triplestore(String endpoint) {
		this.endpoint = endpoint;
		this.graph = null;
	}
	
	private Triplestore(String endpoint, String graph) {
		this.endpoint = endpoint;
		this.graph = graph;
	}
	
	public String getEndpoint() {
		return endpoint;
	}
	
	public String getGraph() {
		if (graph == null)
			return "";
		else
			return "FROM <" + graph + "> ";
	}
	
}
