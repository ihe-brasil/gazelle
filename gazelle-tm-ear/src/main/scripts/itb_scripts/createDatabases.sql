DROP DATABASE "gazelle-dev";
DROP DATABASE "gazelle-proxy";
DROP USER gazelle;
CREATE USER gazelle;
ALTER USER gazelle WITH ENCRYPTED PASSWORD 'gazelle';
ALTER ROLE gazelle WITH CREATEDB;
CREATE DATABASE "gazelle-dev" ENCODING 'UTF-8' OWNER gazelle;
CREATE DATABASE "gazelle-proxy" ENCODING 'UTF-8' OWNER gazelle;