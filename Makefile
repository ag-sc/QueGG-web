run:
	docker-compose --env-file /dev/null -f docker-compose.yml up 

build:
	docker-compose --env-file /dev/null -f docker-compose.yml build

rebuild:
	docker-compose --env-file /dev/null -f docker-compose.yml build --no-cache

run.it:
	docker-compose --env-file /dev/null -f docker-compose.it.yml up 

build.it:
	docker-compose --env-file /dev/null -f docker-compose.it.yml build

rebuild.it:
	docker-compose --env-file /dev/null -f docker-compose.it.yml build --no-cache

example_dbpedia:
	cd ./example/dbpedia && ./upload.sh

example_wikidata:
	cd ./example/wikidata && ./upload.sh

example_arco:
	cd ./example/beniculturali && ./upload.sh

clean:
	echo "{}" > ./data/config.json
	echo "{}" > ./data/trie.cache

push:
	docker image tag quegg-web_web:latest agsc/quegg-web:latest && docker push agsc/quegg-web:latest

push.it:
	docker image tag quegg-web:italian agsc/quegg-web:italian && docker push agsc/quegg-web:italian

shell:
	docker exec -it "$$(docker ps | grep "quegg-web" | cut -d " " -f1)" bash
