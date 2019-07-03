#!/bin/bash
clear

USR_LOCAL_PATH="/usr/local"
INSTALL_PATH="$USR_LOCAL_PATH/jboss"
JENKINS_JOB_RELEASE="release"
JENKINS_JOB_SNAPSHOT="snapshot"

# default to released version of gazelle
SELECTED_RELEASE="$JENKINS_JOB_RELEASE"
JENKINS_JOB="http://gazelle.ihe.net/jenkins/job/gazelle-public-$SELECTED_RELEASE"

function cecho {
  while [ "$1" ]; do
    case "$1" in
    -normal)        color="\033[00m" ;;
    -black)         color="\033[30;01m" ;;
    -red)           color="\033[31;01m" ;;
    -green)         color="\033[32;01m" ;;
    -yellow)        color="\033[33;01m" ;;
    -blue)          color="\033[34;01m" ;;
    -magenta)       color="\033[35;01m" ;;
    -cyan)          color="\033[36;01m" ;;
    -white)         color="\033[37;01m" ;;
    -n)             one_line=1;   shift ; continue ;;
    *)              echo -n "$1"; shift ; continue ;;
    esac

    shift
    echo -en "$color"
    echo -en "$1"
    echo -en "\033[00m"
    shift

  done
  if [ ! $one_line ]; then
    echo
  fi
}


function create_jboss_user() {
  cecho -green "Creating jboss user"
  useradd jboss
  groupadd jboss-admin
  adduser jboss jboss-admin
}

function cleanup_old_install() {
  if [ -d $INSTALL_PATH ]; then
    cecho -green "Clean old jboss install"
    rm -rf "$INSTALL_PATH"
    rm -rf "$USR_LOCAL_PATH/jboss-5.1.0.GA"
  fi
}

function extract_jboss(){
  cecho -green "Installing JBoss"
  wget -nv -O /tmp/jboss-5.1.0.GA-jdk6.zip http://freefr.dl.sourceforge.net/project/jboss/JBoss/JBoss-5.1.0.GA/jboss-5.1.0.GA-jdk6.zip
  cd "$USR_LOCAL_PATH"
  cp /tmp/jboss-5.1.0.GA-jdk6.zip "$USR_LOCAL_PATH"
  unzip ./jboss-5.1.0.GA-jdk6.zip > /dev/null
  rm ./jboss-5.1.0.GA-jdk6.zip
  ln -s jboss-5.1.0.GA jboss
}

function setup_jboss_startup() {
  wget -nv -O /tmp/jboss "$JENKINS_JOB/ws/gazelle-tm-ear/src/main/scripts/jboss"
  cecho -green "Creating jboss service"
  cp /tmp/jboss /etc/init.d/jboss
  chmod +x /etc/init.d/jboss
  update-rc.d jboss defaults > /dev/null
}

function create_gazelle_jboss_server() {
    cecho -green "Creating gazelle jboss server"

    cp -R "$INSTALL_PATH"/server/default "$INSTALL_PATH"/server/gazelle
    rm -rf "$INSTALL_PATH"/server/gazelle/deploy/admin-console.war
    rm -rf "$INSTALL_PATH"/server/gazelle/deploy/jmx-console.war
    rm -rf "$INSTALL_PATH"/server/gazelle/deploy/ROOT.war

    sudo sed -i 's/Connector/Connector URIEncoding="UTF-8"/' /usr/local/jboss/server/gazelle/deploy/jbossweb.sar/server.xml

    chown -R jboss:jboss-admin "$INSTALL_PATH/"
    chmod -R g+w "$INSTALL_PATH"/server/

    chown -R jboss:jboss-admin /var/log/jboss
    chmod -R g+w /var/log/jboss

    chown -R jboss:jboss-admin /opt/gazelle
    chmod -R g+w /opt/gazelle
}


function install_jboss() {
  if $INSTALL_JBOSS ; then

    mkdir -p $USR_LOCAL_PATH
    mkdir -p /var/log/jboss
    mkdir -p /opt/gazelle

    create_jboss_user
    cleanup_old_install
    extract_jboss
    setup_jboss_startup
    create_gazelle_jboss_server
  fi
}

function download_src() {
  cecho -green "Start download"
  wget -nv -O /tmp/tm-first-import.data "$JENKINS_JOB/ws/gazelle-tm-ear/src/main/scripts/tm-first-import.data"
  wget -nv -O /tmp/gazelle-tm.ear "$JENKINS_JOB/ws/gazelle-tm-ear/target/gazelle-tm.ear"
  wget -nv -O /tmp/gazelle-tm-dist.zip "$JENKINS_JOB/ws/gazelle-tm-ear/target/gazelle-tm-dist.zip"
   #wget -nv -O /tmp/3.3.14_update.sql "$JENKINS_JOB/ws/gazelle-tm-ear/src/main/scripts/template_file"
  echo "End download"
}

function configure_postgresql() {
  echo "Configure postgresql"
  echo "CREATE USER gazelle;" > /tmp/createGazelle.sql
  echo "ALTER USER gazelle WITH ENCRYPTED PASSWORD 'gazelle';" >> /tmp/createGazelle.sql
  echo "ALTER ROLE gazelle WITH CREATEDB;" >> /tmp/createGazelle.sql

  # The following is needed for nagios check_postgres_archive_ready test.
  echo "ALTER ROLE gazelle WITH SUPERUSER;" >> /tmp/createGazelle.sql
  su postgres -c "dropdb gazelle"
  echo "CREATE DATABASE "gazelle" ENCODING 'UTF-8' OWNER gazelle;" >> /tmp/createGazelle.sql
  su postgres -c "psql < /tmp/createGazelle.sql"
  su postgres -c "pg_restore -d gazelle /tmp/tm-first-import.data"
}


function run_sql_migrations() {
  echo "no migrations to run, everything ok"
  ##su postgres -c "psql  gazelle < /tmp/template_file.sql"
}

function deploy_ear() {
  echo "Deploy in progress"
  echo "Installing Gazelle"
  unzip -u /tmp/gazelle-tm-dist.zip -d /
  cp /tmp/gazelle-tm.ear "$INSTALL_PATH"/server/gazelle/deploy/
  echo "Deploy completed"
}

function stop_jboss() {
  cecho -green "Stopping Gazelle"
  if [[ -x "/etc/init.d/jboss" ]]; then
    /etc/init.d/jboss stop > /dev/null
  fi
}


function add_oracle_java6_repository() {
  if ! grep -q 'deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main' /etc/apt/sources.list ; then
    echo "configure ppa repository"
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list
    echo oracle-java6-installer shared/accepted-oracle-license-v1-1 boolean true | /usr/bin/debconf-set-selections
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886
  fi
}

function update_packages() {
  add_oracle_java6_repository
  cecho -green "update packages"
  apt-get update >/dev/null
}

function install_required_packages() {
  cecho -green "install packages"
  apt-get install -y oracle-java6-installer oracle-java6-set-default postgresql-9.1 unzip wget graphviz htop ncdu nmap lynx vim dcmtk logrotate fail2ban
  if $INSTALL_NAGIOS ; then
    apt-get install -y nagios3
  fi
}

function start_jboss() {
  cecho -green "Starting Gazelle"
  /etc/init.d/jboss start
}

function configure_locales(){
    cecho -green "Configure locales"
    locale|cut -d '=' -f 2|tr -d '"'|tr -d ':'|sed '/^$/d'| awk '{print "sudo locale-gen " $1 "> /dev/null"}' > locales_to_generate.txt
    bash locales_to_generate.txt
    rm locales_to_generate.txt
}

function check_gazelle_ihe_net(){
#check access to gazelle.ihe.net to fail early
    cecho -green "Checking access to http://gazelle.ihe.net"
    curl -s --head http://gazelle.ihe.net | head -n 1 | grep "HTTP/1.[01] [23].." > /dev/null

    if [ $? -eq 1 ]; then
        cecho -red "Cannot reach http://gazelle.ihe.net"
        cecho -red "Please check that: http://gazelle.ihe.net is available"
        cecho -red "Could be due to firewall issues"
        exit 1
    else
        cecho -green "http://gazelle.ihe.net is available"
    fi
}


# Make sure only root can run our script
if [[ $EUID -ne 0 ]]; then
  cecho -red "This script must be run as root" 1>&2
  exit 1
fi


INSTALL_JBOSS=false
INSTALL_NAGIOS=true
VAGRANT_RUNNING=false
while [ "$1" != "" ]; do
    case $1 in
        "-f")
        # force mode, non interactive
            cecho -green "non interactive mode, will install jboss"
            INSTALL_JBOSS=true
            VAGRANT_RUNNING=true
            ;;
        "-d")
        # -d or developper
            cecho -green "developper mode"
            SELECTED_RELEASE="$JENKINS_JOB_SNAPSHOT"
            ;;
        "-n")
        # -n don't install nagios
            cecho -green "won't install nagios"
            INSTALL_NAGIOS=false
            ;;
        *)
            cecho -red "unknown option $1"
            ;;
    esac
    shift
done

JENKINS_JOB="http://gazelle.ihe.net/jenkins/job/gazelle-public-$SELECTED_RELEASE"

if ! $INSTALL_JBOSS ; then
    read -p "Do you want to install jboss server? [y,n]"
    if [ "$REPLY" = "y" ] || [ "$REPLY" = "Y" ] || [ "$REPLY" = "yes" ] || [ "$REPLY" = "YES" ] || [ "$REPLY" = "Yes" ]; then
      INSTALL_JBOSS=true
    fi
fi

cecho -blue "This script will install Gazelle test managment version: $SELECTED_RELEASE"


update_packages
apt-get install -y curl
check_gazelle_ihe_net


configure_locales

stop_jboss
install_required_packages


PG_INSTALL_OK=`pg_lsclusters -h|wc -l`
if [ $PG_INSTALL_OK -eq 0 ]; then
  pg_createcluster 9.1 main --start
fi

PG_INSTALL_OK=`pg_lsclusters -h|wc -l`
if [ $PG_INSTALL_OK -eq 0 ]; then
  cecho -red "There is no postgresql cluster available"
  cecho -red "Please configure posgresql"
  cecho -red "You should use: pg_createcluster 9.1 main --start"
  cecho -red "If you encounter a locale error you should run: sudo dpkg-reconfigure locales, or generate the missing locale with: sudo locale-gen <locale_name> "
  exit 1
fi

install_jboss
download_src
configure_postgresql
deploy_ear
start_jboss

cecho -blue "waiting for jboss to start"
sleep 60
run_sql_migrations

if [ ! $VAGRANT_RUNNING ] ; then
    tail -f /var/log/jboss/gazelle.log
fi
