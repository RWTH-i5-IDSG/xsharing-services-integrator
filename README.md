Requirements
-----

1. Oracle JDK 1.8 (please use the most recent version for security reasons)

2. WildFly 9 (10.x will work aswell, 8.x wont because https://issues.jboss.org/browse/UNDERTOW-335)
   Please use the most recent minor release (e.g. 9.0.2 as of writing)

3. PostgreSQL 9.x + PostGIS 2.x (We use PostgreSQL 9.4.9 and PostGIS 2.2.2)

4. PostgreSQL JDBC Driver
   Download the JDBC driver postgresql-9.4-1201.jdbc41.jar from https://jdbc.postgresql.org/download/postgresql-9.4-1201.jdbc41.jar
   This _exact_ version is required.
   
Setup PostgreSQL
-----

Create a PostgreSQL database with PostGIS extension in schema public and create the specified user/password combinations. 
Grant all rights to the database / user configuration. 
e.g.:

```
~ # psql -U postgres
postgres=# CREATE USER adapter WITH PASSWORD 'changeme';
postgres=# CREATE DATABASE adapter WITH OWNER adapter ENCODING 'UTF8';
postgres=# \c adapter;
adapter=# CREATE EXTENSION postgis SCHEMA public;
adapter=# \q
```

Edit Properties File
-----
- Customize database configuration and email configuration (for notifications).
- The database configuration is used by both the adapter application and the database migration.

**When using an external properties file**

- Rename it to *mb-adapter.properties*
- Put the properties file under the base configuration directory of WildFly (jboss.server.config.dir property)
- Run the database initialisation and migration tool to setup the database using the correct path of the properties file.
    
    ```
    java -jar db-migration-tool.jar -p ${jboss.server.config.dir}/mb-adapter.properties
    ```

Configuring Wildfly
-----

1. The monitor Web page requires authentication. For this, add the following user to WildFly (group name is important, can be different from database user):

    ```
    ./bin/add-user.sh -a -u username -p password -g mobilitybroker-group
    ```

2. Put PostgreSQL jdbc driver under ${wildfly.home}/modules/system/layers/base/org/postgresql/main. Create the folder org/postgresql/main, if it does not exist.

3. Put the following as module.xml under the same directory:

    ```
    <?xml version="1.0" encoding="UTF-8"?>
    <module xmlns="urn:jboss:module:1.3" name="org.postgresql">
           <resources>  
                   <resource-root path="postgresql-9.4-1201.jdbc41.jar"/>  
           </resources>  
           <dependencies>  
                   <module name="javax.api"/>  
                   <module name="javax.transaction.api"/>  
           </dependencies>
    </module>
    ```
    
4. Edit ${wildfly.home}/standalone/configuration/standalone.xml and add the following in subsystem/datasources/drivers:

    ```
    <driver name="postgresql" module="org.postgresql">
        <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>  
    </driver>
    ```

5. Do one of the following

   - Add a management user to WildFly with ${wildfly.home}/bin/add-user.sh.  
     Start WildFly and go to the admin console (Default is http://127.0.0.1:9990).  
     Add a data source for the database under Configuration/Connector/Datasources:
     
       * Connection URL - jdbc:postgresql://host:5432/databasename 
       * JNDI - java:jboss/datasources/AdapterDS (this has to match the given JNDI in properties file!)
       * Enable Connection!
         
   - Alternatively, you can use the datasource configuration at the end of the document and insert it directly into the wildfly configuration.


Application Installation and Usage
------

1. Deploy the application (e.g., using jboss-cli.sh or the web interface). Context path is the name of the war file.
   The monitor / configuration interface is avilable under /context-path/monitor, e.g: /adapter/monitor and accessable 
   with the created credentials.

2. Now you can add partner to the adapter using "Manage Server Systems" and Add new.



Appendix
-----

Suggested data source configuration:

```
<datasource jta="true" jndi-name="java:jboss/datasources/AdapterDS" pool-name="mobility_broker_db" enabled="true" use-ccm="true">
    <connection-url>jdbc:postgresql://hostname:5432/databasename</connection-url>
    <driver-class>org.postgresql.Driver</driver-class>
    <driver>postgresql</driver>
    <transaction-isolation>SERIALIZABLE</transaction-isolation>
    <pool>
        <min-pool-size>4</min-pool-size>
        <initial-pool-size>4</initial-pool-size>
        <max-pool-size>20</max-pool-size>
        <prefill>true</prefill>
    </pool>
    <security>
        <user-name>username</user-name>
        <password>password</password>
    </security>
    <validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter" />
        <background-validation>true</background-validation>
        <background-validation-millis>10000</background-validation-millis>
    </validation>
    <timeout>
        <blocking-timeout-millis>30000</blocking-timeout-millis>
        <idle-timeout-minutes>10</idle-timeout-minutes>
        <query-timeout>300</query-timeout>
        <allocation-retry>1000</allocation-retry>
    </timeout>
    <statement>
        <share-prepared-statements>false</share-prepared-statements>
    </statement>
</datasource>
```