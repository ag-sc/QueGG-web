# Run QA system on your machine
This page provides instructions on how to run the QA system on your machine. The home page of the hackathon can be found [here](https://scdemo.techfak.uni-bielefeld.de/qahackathon/index.php/)

### Install QA system
Install [docker] (https://docs.docker.com/engine/install/)
1. Download the image
```
docker pull agsc/quegg-web:latest
```
2. Run the image as a container.
```
docker run -p "8089:8089" -e "QUEGG_ALLOW_UPLOADS=true" agsc/quegg-web:latest
```
Go to http://localhost:8089/quegg/ and the interface will be shown on your browser. It will initially be empty, a minimal example to get data into the running instance would be:

### Add questions to the QA system
3. Download the file containing lexical entries.  The file [nounppframe.csv](https://raw.githubusercontent.com/ag-sc/QueGG-web/main/example/nounppframe.csv) shows an example of the lexical entry.  
```
wget -O nounppframe.csv https://raw.githubusercontent.com/ag-sc/QueGG-web/main/example/nounppframe.csv
```
Post the file
```
curl -X "POST" -F "file=@nounppframe.csv" "http://localhost:8089/quegg/import"      
```
### Add  more questions to the QA system
a) add lexical entry at Google XSL [sheet](https://docs.google.com/spreadsheets/d/1NgH7GdFcAqQuYU3ziIXpq0Yybt4lZIR15DpPgaoXF4M/edit?usp=sharing). See the [guideline](https://scdemo.techfak.uni-bielefeld.de/qahackathon/tutorial/coverage.php#id4) of writing a lexical entry for a grammar type.      
b) download the Google XSL sheet as csv.  File>Download>Comma-separated values [.csv, current sheet].\
c) repeat step 3. The questions of newly added lexical entry will be visible in QA system.

Please use the following citation:
```
@inproceedings{Buono-LREC2020,
	title = {{Generating Grammars from lemon lexica for Questions Answering over Linked Data: a Preliminary Analysis}},
	author = {Viktoria Benz, Philipp Cimiano, Mohammad Fazleh Elahi, Basil Ell},
	booktitle = {In: NLIWOD workshop at ISWC 2020},
	pages = {40–55},
	year = {2020},
	link = {http://ceur-ws.org/Vol-2722/nliwod2020-paper-2.pdf}
}
```

Script run:


## Developers
* **Frank Grimm**
* **Mohammad Fazleh Elahi**
### Supervisors:
* **Dr. Philipp Cimiano**
* **Dr. Basil Ell**


  

