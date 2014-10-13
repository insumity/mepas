from os import system

# assumses logs directory is in the home directory
def clean_machine(username, host, privateKeyFile):
    system("ssh -i " + privateKeyFile + " " + username + "@" + host + " " + "rm -rf logs")

# copies the file in the home directory of host
def scp(file, username, host, privateKeyFile):
    system("scp -i " + privateKeyFile + " " + file + " " + username + "@" + host + ":")

# assumes executable JAR is on the home directory with name "mepas.jar" FIXME?
def start_machine(username, host, privateKeyFile):
    system("ssh -i " + privateKeyFile + " " + username + "@" + host + " " + "./foo.py")
    print "ssh -i " + privateKeyFile + " " + username + "@" + host + " " + "./foo.py"