echo 'start'
cd ..
cd gazelle-tools
mvn clean install
cd ..
cd gazelle-model
mvn clean install -DskipTests
cd ..
cd gazelle-tm-tools
mvn clean install
cd ..
cd gazelle-tm
mvn clean install
echo 'finished'
#sh deploy7.sh
#echo 'deployed'
#tail -f /var/log/jboss/gazelle.log
