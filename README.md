# Run QA system on your machine
This page provides instructions on how to run the QA system on your machine. The home page of the hackathon can be found [here](https://scdemo.techfak.uni-bielefeld.de/qahackathon/index.php/)

### Install QA system from dockerHub
Install [docker] (https://docs.docker.com/engine/install/)
1. Download the image
```
docker pull elahi/quegg-web:index
```
2. Run the image as a container.
```
docker run -p "8089:8089" -e "QUEGG_ALLOW_UPLOADS=true" elahi/quegg-web:index
```
Go to http://localhost:8089/quegg/ and the interface will be shown on your browser. Test the browser by typing 

### Or Install QA system from GitHub
1. Download the project
```
git clone https://github.com/ag-sc/QueGG-web.git -b extension
```
2. Go to the location QueGG-web and build the project
```
mvn clean package
```
3. Run the project
```
java -jar target/quegg-web-0.0.1-SNAPSHOT.jar
```

Go to http://localhost:8089/quegg/ and the interface will be shown on your browser. Test the browser by typing 

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


  

