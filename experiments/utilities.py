from os import system

#FIXME ... code repeated ... put a common method with one SSH command, like execute_command

# assumses logs directory is in the home directory
def clean_machine(username, host, privateKeyFile):
    system("ssh -i " + privateKeyFile + " " + username + "@" + host + " " + "rm -rf logs")

def execute_command(username, host, privateKeyFile, command):
    system("ssh -i " + privateKeyFile + " " + username + "@" + host + " " + command)

# copies the file in the home directory of host
def scp_to(file, username, host, privateKeyFile):
    system("scp -i " + privateKeyFile + " " + file + " " + username + "@" + host + ":")

# copies the file from the host to the current directory
def scp_from(file, where, username, host, privateKeyFile):
    system("scp -i " + privateKeyFile + " " + username + "@" + host + ":" + file + " " + where)

# assumes executable JAR is on the home directory with name "mepas.jar" FIXME?
def start_machine(username, host, privateKeyFile):
    system("ssh -i " + privateKeyFile + " " + username + "@" + host + " " + "java -jar mepas.jar" + " " +
           "1>/tmp/server.out 2>>/tmp/server_errors.out &")

def start_client(username, host, privateKeyFile):
    system("ssh -i " + privateKeyFile + " " + username + "@" + host + " " + "java -jar mepas.jar" + " " +
           "1>/tmp/client.out 2>>/tmp/client_errors.out &")