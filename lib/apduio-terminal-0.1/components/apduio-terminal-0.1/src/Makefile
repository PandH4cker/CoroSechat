# ..........................................................................
include ../../../Makefile
# ..........................................................................

##################
### Variables  ###
##################

#inherited : JAVAC, JFLAGS, JAVA, JAR, JAVA_LIB, 

PACKAGE_NAME := com.gemplus.opencard.terminal.apduio

CURRENT_VERSION := $(shell grep Implementation-Version MANIFEST | sed 's/.*"\([^"]*\)".*/\1/')

JAR_NAME := apduio-terminal-$(CURRENT_VERSION).jar

SOURCES := $(shell find . -name \*.java)

OBJECTS = $(shell find $(PACKAGE_PATH) -name \*.class)

JFLAGS := -O -classpath `pwd`/../../../lib/apduio.jar

##################
### Rules      ###
##################

# Java JAR file creation in ../../../lib
../../../lib/$(JAR_NAME) :
	$(JAR) cmf MANIFEST ../../../lib/$(JAR_NAME) $(OBJECTS)

##################
### Targets    ###
##################

#inherited : 'help', 'compile', 'clean', 'version'
#overloaded : 'compile' (creation of JAR file)
#added : 'install' (copy of JAR into Java zone)

compile ::
	$(JAR) cmf MANIFEST ../../../lib/$(JAR_NAME) $(OBJECTS)

install : ../../../lib/$(JAR_NAME)
	cp $< $(JAVA_LIB)

