What has to be installed in an Amazon instance?
-----------------------------------------------

[ALL THE MIDDLEWARES connect to the DB so the ned
the .pgpass file as well!!]

openjdk-7-jdk [for Java 7] VASIKA .. thes kai ton compiler
ton katarameno gia kalo kai gia kako

postgresql [for the database]

sudo -i -u postgres --> to login as postgres user
and then you can simply do psql

I followed this: http://www.cyberciti.biz/faq/howto-add-postgresql-user-account/
to create a role "ubuntu", a database "mepas" and granted all permission for
this database to user "ubuntu"

For Database to be accessed from external machines:
add this line to file (/etc/postgresql/9.3/main/pg_hba.conf)
host    all             all             0.0.0.0/0            md5

and to accept TCP connections add
change file /etc/postgresql/9.3/main/postgresql.conf the line
listen_addresses='localhost'
to listen_addresses='*'

then sudo service postgresql restart

In your local computer you have to create a .pgpass [in your home directory] file with:
*:*:*:*:mepas$1$2$3$
so you can issue a psql command without aving to provide a password
(file should have stric permission, issue  chmod 0600 ~/.pgpass)
otherwise the file is ignored.

In local computer boto has to be installed for the pyton script
as well as psycopg2.


PLUS
----

security group was made ... for EVERYBODY in EC2