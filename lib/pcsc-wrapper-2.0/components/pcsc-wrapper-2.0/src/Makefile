# ..........................................................................
include ../../../Makefile
# ..........................................................................

##################
### Variables  ###
##################

#inherited : JAVAC, JFLAGS, JAVA, JAR, JAVA_LIB, PACKAGE_PATH

PACKAGE_NAME := com.ibm.opencard.terminal

CURRENT_VERSION := $(shell grep Implementation-Version MANIFEST | sed 's/.*"\([^"]*\)".*/\1/')

JAR_NAME := pcsc-wrapper-$(CURRENT_VERSION).jar

SOURCES := $(shell find . -name \*.java)

##################
### Rules      ###
##################

# Java JAR file creation in ../../../lib
../../../lib/$(JAR_NAME) :
	$(JAR) cmf MANIFEST ../../../lib/$(JAR_NAME) $(PACKAGE_PATH)

##################
### Targets    ###
##################

#inherited : 'help', 'compile', 'clean', 'version'
#overloaded : 'compile' (creation of JAR file)
#added : 'install' (copy of JAR into Java zone)

compile ::
	$(JAR) cmf MANIFEST ../../../lib/$(JAR_NAME) $(PACKAGE_PATH)

install : ../../../lib/$(JAR_NAME)
	cp $< $(JAVA_LIB)
