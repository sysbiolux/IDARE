# IDARE

IDARE is a Cytoscape 3 app (versions 3.4 and above), for subnetwork generation and data visualisation.

##Features
* Automated layouting of data using a variety of layout types
* Available Lyouts include scatter plots, graphs, heatmaps, time series and bar charts
* Parseable formats include excel sheets, tab/comma separated value files and GEO SOFT files
* Generation of connected subnetworks and clickable links between these networks
* Addition of gene nodes and further information based on information from COBRA annotations in sbml files (see [Schellenberger et al 2011](http://www.nature.com/nprot/journal/v6/n9/abs/nprot.2011.308.html) )

##Availability
**Cytoscape App store**: http://apps.cytoscape.org/apps/IDARE
**Bundle Plugin** http://idare-server.uni.lu/IDARE-1.0.jar
**User Manual** http://idare-server.uni.lu/UserManual.pdf
**Example/QuickStart** http://sysbiolux.github.com/Sample
**API**: http://idare-server.uni.lu/Doc
**Source Code**: https://github.com/sysbiolux/IDARE 
**Bug Tracker**: https://github.com/sysbiolux/IDARE/issues
 
##Installation
There are three options available:
* Use the Cytoscape Application manager. In Cytoscape open `Apps → App Manager → Install Apps` and search for `IDARE`. 
* Download the Jar from the [Cytoscape App store](http://apps.cytoscape.org/apps/IDARE)
* Download the Jar from the [University of Luxembourg](http://idare-server.uni.lu/IDARE-1.0.jar)
If you download it manually put it into the apps/installed folder of your Cytoscape configuration folder (commonly %HOME/CytoscapeConfiguration/3/apps/installed).

##Building
To build the app, you will need to also download the cy3sbml app (http://apps.cytoscape.org/apps/cy3sbml).
Determine the path of the jar file (likely in the CytoscapeConfiguration folder in your home directory) and install it to your maven repository.

```
mvn -e install:install-file -Dfile=cy3sbml-v0.2.X.jar -DgroupId=org.cysbml -DartifactId=cy3sbml -Dversion=0.2.1 -Dpackaging=jar
```
Adjust the X to the version you downloaded. IDARE works with version 0.2.1 but we can't guarantee that it works with newer versions.

To build IDARE:
```
git clone https://github.com/sysbiolux/IDARE.git IDARE
cd IDARE/METANODE-CREATOR
mvn install
```
## Plugin Development

To develope plugins, you will have to install the application into your maven repository.
Navigate to the location of the IDARE-1.0.jar file and run
```
mvn -e install:install-file -Dfile=IDARE-1.0.jar -DgroupId=IDARE -DartifactId=IDARE -Dversion=1.0 -Dpackaging=jar
```

In your pom.xml add
```
<Cytoscape-App-Dependencies>IDARE;1.0</Cytoscape-App-Dependencies>
```
to the properties field.

The IDARE dependency is added as follows:
```
<dependency>
	<groupId>IDARE</groupId>
	<artifactId>IDARE</artifactId>
	<version>1.0</version>
	<scope>provided</scope>	
</dependency>
```


### Relevant Literature
Galhardo, M.; Sinkkonen, L.; Berninger, P.; Lin, J.; Sauter, T. & Heinäniemi, M. Integrated analysis of transcript-level regulation of metabolism reveals disease-relevant nodes of the human metabolic network. Nucleic Acids Res, 2014, 42, 1474-1496
http://dx.doi.org/10.1093/nar/gkt998
