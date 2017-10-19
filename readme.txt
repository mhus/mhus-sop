
install -s mvn:de.mhus.osgi/mhu-sop-api/1.3.1-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-core/1.3.1-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-jms/1.3.1-SNAPSHOT

install -s mvn:de.mhus.ports/bonita-client/1.3.1-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-bonita/1.3.1-SNAPSHOT


bundle:persistentwatch add mhu-sop-api
bundle:persistentwatch add mhu-sop-core
bundle:persistentwatch add mhu-sop-jms

