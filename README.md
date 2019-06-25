# C3M: Contextual Cardinality Constraint Mining

Semantic Web connects huge knowledge bases whose content has been generated from collaborative platforms and by integration of heterogeneous databases. Naturally, these knowledge bases are incomplete and contain erroneous data. Knowing their data quality is an essential long-term goal to guarantee that querying them returns reliable results. Having cardinality constraints for roles would be an important advance to distinguish correctly and completely described individuals from those having data either incorrect or insufficiently informed. This work proposes a method for automatically discovering from the knowledge base's content the maximum cardinality of roles for each concept, when it exists. This method is robust thanks to the use of Hoeffding's inequality. More precisely, C3M is designed for an exhaustive search of such constraints in a knowledge base benefiting from pruning properties that drastically reduce the search space. 

## Publication

*Mining Significant Maximum Cardinalities in Knowledge Bases.*
Arnaud Giacometti, Béatrice Markhoff and Arnaud Soulet, ISWC19 (research track).

## Running C3M

Just run the C3M class of the c3m package for launching the algorithm (in c3m-code directory). By default, the program is running on DBpedia with an error threshold of 0.01 (`delta`) and a minimum likelihood threshold of 0.97 (`minLikelihood `).

Alternatively, there is a runnable jar in the [runnable directory](https://github.com/asoulet/c3m/tree/master/runnable).

For using another SPARQL endpoint, you just have to change the line 14 in `c3m-code/src/c3m/C3M.java` for modifying the variable `triplestore` (e.g., value `Triplestore.YAGO` for mining constraint in YAGO).

## Results

The [result directory] (https://github.com/asoulet/c3m/tree/master/results) provides the execution of C3M on 4 SPARQL endpoints: DBpedia, YAGO, BNF, EUROPEANA. The results are given in CSV and TTL format. The file `c3mOntology.owl` presents the ontology used in TTL files (which is useful for publishing results). Note that c3m only outputs the constraints in CSV format.
