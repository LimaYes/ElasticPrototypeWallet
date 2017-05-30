----
# Welcome to XEL! #

----
## What is XEL? ##
XEL is a decentralized supercomputer based on cryptography and blockchain technology.

----
## Get it! ##

  - *dependencies*:
    - *general* - Java 8
    - *Ubuntu* - `http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html`
    - *Debian* - `http://www.webupd8.org/2014/03/how-to-install-oracle-java-8-in-debian.html`
    - *FreeBSD* - `pkg install openjdk8`

  - *repository* - `git clone git@github.com:OrdinaryDude/elastic-light.git`
  
----
## Run it! ##

  - click on the XEL icon, or start from the command line:
  - Unix: `./start.sh`
  - Mac: `./run.command`
  - Window: `run.bat`

  - wait for the JavaFX wallet window to open
  - on platforms without JavaFX, open http://localhost:17876/ in a browser

----
## Compile it! ##

  - if necessary with: `./compile.sh`
  - you need jdk-8 as well

----
## Improve it! ##

  - we love **pull requests**
  - we love issues (resolved ones actually ;-) )
  - in any case, make sure you leave **your ideas** at BitBucket
  - assist others on the issue tracker
  - **review** existing code and pull requests
  - cf. coding guidelines in DEVELOPERS-GUIDE.md

----
## Troubleshooting the NRS (XEL Reference Software) ##

  - How to Stop the NRS Server?
    - click on Nxt Stop icon, or run `./stop.sh`
    - or if started from command line, ctrl+c or close the console window

  - UI Errors or Stacktraces?
    - report on github

  - Permissions Denied?
    - no spaces and only latin characters in the path to the NRS installation directory
    - known jetty issue

----
## Further Reading ##

  - in this repository:
    - USERS-GUIDE.md
    - DEVELOPERS-GUIDE.md
    - OPERATORS-GUIDE.md

  - on the forums:
    - talk.elasticexplorer.org
    
----

