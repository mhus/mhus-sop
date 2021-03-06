====
    Copyright 2018 Mike Hummel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

feature:repo-add activemq 5.15.8
feature:repo-add cxf
feature:repo-add mvn:de.mhus.osgi/mhu-sop-feature/1.5.0-SNAPSHOT/xml/features

feature:install mhu-sop-base

feature:install mhu-sop-rest

feature:install mhu-sop-mailqueue

feature:install mhu-sop-jms

feature:install mhu-sop-vaadin

feature:install mhu-sop-full

====

install -s mvn:de.mhus.osgi/mhu-sop-api/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-core/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-jms/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-rest/1.3.2-SNAPSHOT

install -s mvn:de.mhus.osgi/mhu-sop-vault/1.3.4-SNAPSHOT


bundle:persistentwatch add mhu-sop-api
bundle:persistentwatch add mhu-sop-core
bundle:persistentwatch add mhu-sop-jms
bundle:persistentwatch add mhu-sop-rest


Test unique master handling:

registry set /system/master/test@seed 1


SOP Desktop:

feature:install mhu-osgi-vaadin

install -s mvn:de.mhus.ports/vaadin-refresher/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-vaadin-theme/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-sop-vaadin-desktop/1.3.2-SNAPSHOT



