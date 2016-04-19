web: java -cp target/classes:target/dependency/* -DPORT=5000 br.nom.abdon.heroku.Main
migrate: java -cp target/classes:target/dependency/* br.nom.abdon.gastoso.load.DBMigrate
debug: java -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1806 -DPORT=5000 -cp target/classes:target/dependency/* br.nom.abdon.heroku.Main 
