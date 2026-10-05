Det här är en skol uppgift för en devops kurs.

Det här är ett repo för tjänsten deployad här:
Production: booking-microservice-production-cc97.up.railway.app
Staging: booking-microservice-staging-984b.up.railway.app

Vi har valt att använda Github flow som våran branchstrategi då vi har använt det tidigare och tyckte att det skulle fungerar bra för den kommande uppgiften.

Vårat arbetsflöde har varit:
Startmöte för att skapa alla tickets/issues på Kanban som vi tror kommer att behövas ->
grupp medlemmar jobbar enskilt på valda tickets ->
när ticket är klar skapas en pull request som en annan medlem behöver granska ->
om allt är grönt mergas den in i main och vår CI/CD pusher det till dockerhub ->
våran staging railway automatiskt updatera vid ny docker image medas production kräver en manuel push.

Under hela arbetsflödet har vi en öppen chat om vi skulle behöva ha kontakt med en annan medlem / boka möten för att prata om problem, ny issues m.m..

Våran rollback görs via GitHub då vi kan pusha en gammal och fungerande commit och vår CI/CD pushar då den gamla koden som ny.

Vi har skapat en merge-konflikt genom att två av oss arbetade på samma del av koden med små skillnader (src/main/resources/application.properties rad 15)(PR 15,17)
när andra branchen skulle bli merged skapades konflikten som blev löst genom att acceptera inkommande ändringar.
Vi senare märkte att merge konflikten löstest fel och inkommande ändringar blev bortkastade och blev sedan fixad i PR 23.