
The following steps need to be carried out for a setup where the Gazelle Proxy is on a separate machine then Gazelle TM. This is necessary for JMS to work. Please note this is only applicable if the proxy is hosted on a separate server from the Gazelle TM application (copied from HITTI-201: https://jira.aegis.net/browse/HITTI-201):

    enable remote access to Gazelle TM and Proxy databases on Postgresql servers hosting Gazelle TM and Gazelle Proxy. Change password of postgres user to something secure. Following files need modification as postgres user:
        postgresql.conf (/var/lib/pgsql/ {postgres_version}/data/
            uncomment property: listen_addresses and set its value to '*'
        pg_hba.conf (/var/lib/pgsql/{postgres_version}/data/

            under Ipv4 host entries add entries for your LAN ip address range by using subnet:
            e.g. 10.0.2.0/24 or 10.0.2.0 255.255.255.0
            A sample entry would look like this:
            host all all 10.0.4.0/24 md5

        restart the postgresql service by using the following command:
        sudo /etc/init.d/postgresql restart
        You should get greeen okay for stop and restart. If not review logs for errors.

    Modify the pom.xml for Gazelle TM and Gazelle Proxy (and other applications using the same database server). Instead of localhost for database enter the actual IP address of the server hosting the Gazelle TM database (gazelle) and Gazelle Proxy database (gazelleproxy). You will modify the database entry like this:
        <jdbc.connection.url>jdbc:postgresql:// {ip_address_of_machine}:5432/ {name_of_database}</jdbc.connection.url>


            On the machine hosting Gazelle TM:

        -in the proxy-ds.xml file located in the {jboss_install_dir}/server/gazelle/deploy folder modify the host for proxy database to the actual IP-address of the proxy database.

        -Restart application server.

    On the machine hosting the Gazelle Proxy:
          -in the components.xml file located in the {gazelle_code_base}/gazelle-proxy/gazelle-proxy-war/src/main/webapp/WEB-INF file modify the property: <property name="javaNamingProviderUrl"> {enter_ip_address_of_the_location_where_JMS_queue_is}</property>

          e.g. <property name="javaNamingProviderUrl">10.0.4.67</property>

    Perform a new build of the Gazelle TM and Gazelle Proxy applications (and other applications using IP address for database) and deploy to application server.

    If the IP addresses used don't have associated DNS records, then manually add the IP address and related DNS entries in the /etc/hosts file of the machine where the proxy is running. This is due to issue with the the way the QueueConnectionFactory tries to map a IP address. It looks for the valid DNS entry of the IP address provided and fails if it doesn't exist.

    In your initiating system e.g. PIX Source, for Responder System (PIX Manager etc.) or Target Application, enter the IP address of the Proxy and the Proxy Port assigned to the responding system. E.g. If actual IP of responding system is 10.0.4.71, port 3600 and proxy assigned port is 10011, then based on proxy ip the IP address of the target or responder system would be {proxy_ip}:10011. The proxy will automatically intercept the message and send it to the configured responder.

    In Gazelle TM, make the following configuration changes:
    -Top Menu: Configurations > Network Configuration Overview. Click on third tab for "Configure ips and ports for proxy". Enter the remote IP address of the proxy. 

    Ensure you have proper IP addresses entered for your Initiator and Responder systems. In Gazelle TM host configurations you will enter the actual IP addresses of the Initiator and Responders.

    Review the configurations by going to: Top Menu: Configurations > All Configurations. Make note of the proxy port for the responder system as that is the port will you will need to specify with the proxy ip address as part of your configuration on the initiator system for target application.