
1. GENERAL DESCRIPTION 
This is a simple named entity disambiguation project, a.k.a entity linking, using Wikipedia as a reference.
The entire project is implemented in JAVA.


2. EXAMPLE 
Example query: [Texas] is a famous pop music band from Glasgow, Scotland, they are famous with album Careful What You Wish For
Wikipedia entity for Texas above: Texas_(band).


3. MAIN COMPONENTS IN CODE:
	We can group the classes into following 4 categories first:
	A. Online: Query.java, NamedEntity.java, Candidate.java, ProSolver.java, SimpleSolver.java, App.java
	B. Offline: DocumentFrequencyFinder.java
	C. Helper: Database.java, PageParser.java
	D. Tune-Test: Tuner.java, Test.java

4. SHORT DESCRIPTIONS FOLLOWING THE SYSTEM'S PIPELINE
Although each class has a decent amount of explanations in comments in the source code, we provide brief descriptions below. The order in which the explanations/descriptions for classes are given follows the pipeline order of the system:

	ONLINE:
	1-) User feeds a QUERY string into the system.
		a) Query is captured by Query object from Query.java class.
		b) A member function of Query class fetches all the named entities provided in between brackets
		c) Also another member function of Query class parses the distinct words in query and records it along with occurrence counts.
	2-) Named entites detected in Query class are captured by NamedEntity.java class' object.
		a) For each NamedEntity, a member function of NamedEntity.java retrieves all the candidate entities for this NamedEntity.
		b) Each candidate Wikipedia entity is stored in Canidadate.java class' object.
		c) For each candidate, popularity and context similarity scores are computed by member functions of this class.
	3-) The list of named entities and their corresponding candidate lists are fed into ProSolver implementing Solver interface.
		a) ProSolver basically computes the final score of each Candidate for a NamedEntity by considering normalization of 
			both popularity and context similarity scores to [0,1].
		b) And, ProSolver returns the best candidate for each NamedEntity detected in query by Query class.
	4-) App.java class basically puts everything explained above together and make the whole PIPELINE work as ONE.
		
	OFFLINE:
	1-) DocumentFrequencyFinder.java is an offline process that we run once for all as a precomputation.
		a) As can be understod from the name, in this class, we basically compute the document 
			frequencies of the words appearing in the whole Wikipedia corpus by scanning through
			Wikipedia articles using JWPL API's useful tools, and retrieving the desired content using PageParser.java

	HELPER:
	1-) PageParser.java is one of the most useful classes as it is a must for both offline and online processes we need to perform in this project. 
	   Function: Given a plain text stored in String, Static method parse below fetches every distinct word and numbers in special format along 
	   with the count of their occurrences, and returns them in a Map<String, Integer> structure.

	2-) Database.java: allows us to query and update all the needed tables in the database in a compact and more neat way.


	TUNER-TESTER:
	1-) Test.java: This class basically tests the performance of the entire system on given test files consisting of 
		query texts in the format we require and corresponding ground truth Wikipedia Links. It uses the optimal linear
		combination parameter found in Tuner.java in it scoring function while testing.

	2-) Tune.java: finds the optimal combination scalar alpha based on the provided training data examples.

