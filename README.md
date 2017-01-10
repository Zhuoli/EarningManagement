# Earning manager

The backend of Stock-shares assistant running at : http://zhuoliliang.com/em/, it performs the realtime data updating and Sync so that the front end could always read the latest data from AWS Relational Database

### Functions
 1. Parse HTML content of Nasdaq website to get the realtime Stock price from : http://www.nasdaq.com/symbol/
 2. Periodically read Yahoo email box to check if has new order confimations from Robinhood, then extract the data from this new email and update to local HashMap and AWS database
 3. Keep track of the latest changes of Stock info and sync up between local HsahMap with AWS using TimeStamp
 4. Use JOOQ as Object-relational mapping(ORP)
 5. Maven based, integrated with AWS continuous integration tool - CodePipeline


# Tips
###https://www.google.com/finance

### How to get jar: mvn clean compile assembly:single


https://zhuoli.github.io/EarningManagement/

### How to keep java application running after logout
 1. screen
 2. launch java application
 3. Ctrl+A, Ctrl+D
 4. log out
 ...
 To resume: screen -r
