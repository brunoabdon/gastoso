web: java -cp target/classes:target/dependency/* -Djava.util.logging.config.file=src/test/resources/logging.properties  br.nom.abdon.heroku.Main
migrate: java -cp target/classes:target/dependency/* br.nom.abdon.gastoso.load.DBMigrate
debug: java -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1806 -cp target/classes:target/dependency/* -Djava.util.logging.config.file=src/test/resources/logging.properties br.nom.abdon.heroku.Main 
