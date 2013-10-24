#
#  NAME
#	Makefile	-  manage project compilation
#
#  DESCRIPTION
#	This Makefile manages building part or all of the API
#	packages, command-line tools, and documentation.
#
#  PRINCIPAL AUTHOR
#	Lucas Ammon Gilbert, San Diego Supercomputer Center
#
#  TARGETS
#	Compiling:
#		all			-  make the class files
#		lib			-  make the libraries
#
#	Cleaning:
#		clean		-  clean out all .class's
#
#	Admin:
#		doc		-  build the API documentation
#
.POSIX:
#.SILENT:
.SUFFIXES:
.SUFFIXES:	.class .java
SHELL =	/bin/bash



########################################################################
#  HELP
########################################################################
.DEFAULT:
	echo "Unknown target:  $@"
	make help

usage:	help
	true

help:
	echo "Usage is:  make [targets...]"
	echo "  General build targets:"
	echo "    all          -  build all class files (with previous default)"
	echo "    gsi          -  build all class files with gsi enabled"
	echo "    notgsi       -  build all class files without gsi enabled"
	echo "    jar          -  create the jargon.jar"
	echo "  Admin targets:"
	echo "    doc          -  automatically build distributed documentation"
	echo "    clean        -  clean out built obj files for this host type"
	echo "    size         -  compute the number of lines of source code"




########################################################################
#  RELEASE TITLE AND VERSION
########################################################################
TITLE 	= jargon
VTAG 	= _v
VERSION	= 2.0

# change them both
PATCH		= 0
OLDVERSIONNAME	= 2.0


OLDRELEASENAME	= $(TITLE)$(VTAG)$(OLDVERSIONNAME)

VERSIONNAME	= $(VERSION)$(PATCH)
RELEASENAME	= $(TITLE)$(VTAG)$(VERSIONNAME)




########################################################################
#  JAVA CONTROLS
########################################################################

#Change to your path. Use java version 1.4 and higher.
#Uses the URI class from 1.4+  Without this class, using an old java version,
#you will see 99(+) errors
JAVADIR =
#/usr/local/apps/jdk1.4.1/bin/

JAVAC = $(JAVADIR)javac -d $(BIN) -source 1.5 -target 1.5 -classpath $(LIB)
JAVA = $(JAVADIR)java -classpath .:$(LIB)jargon.jar:$(BIN)




########################################################################
#  DIRECTORY AND FILE LOCATIONS
########################################################################
HOME 		=	./

BIN 		=	$(HOME)bin/
DOC 		=	$(HOME)doc/
LIB 		=	$(HOME)lib/
APISRCDIR 	=	$(HOME)src/api/
TESTSRCDIR 	=	$(HOME)src/test/

PACKAGES 	=	edu/sdsc/grid/
IOFTPDIR 	=	$(PACKAGES)io/ftp/
IOHTTPDIR 	=	$(PACKAGES)io/http/
IOSRBDIR 	=	$(PACKAGES)io/srb/
IOIRODSDIR 	=	$(PACKAGES)io/irods/

APISRC 		=	$(APISRCDIR)$(PACKAGES)io/local/*.java \
			$(APISRCDIR)$(IOFTPDIR)*.java \
			$(APISRCDIR)$(IOHTTPDIR)*.java \
			$(APISRCDIR)$(IOSRBDIR)*.java \
			$(APISRCDIR)$(IOIRODSDIR)*.java \
			$(APISRCDIR)$(PACKAGES)io/*.java \
			$(APISRCDIR)$(PACKAGES)gui/*.java


CLASSES 	=	$(BIN)$(PACKAGES)local/*.class \
			$(BIN)$(IOFTPDIR)*.class \
			$(BIN)$(IOHTTPDIR)*.class \
			$(BIN)$(IOSRBDIR)*.class \
			$(BIN)$(IOIRODSDIR)*.class \
			$(BIN)$(PACKAGES)io/*.class \
			$(BIN)$(PACKAGES)/gui/*.class \
			$(BIN)*.class


SOURCEJAR 	=	$(RELEASENAME)_source.jar
DOCJAR 		=	$(LIB)$(RELEASENAME)_doc.jar
WEBSITE 	=	/misc/www/server/srb/jargon/

JARGONJAR 	=	$(RELEASENAME).jar
JARPATH 	=	$(LIB)$(JARGONJAR)



########################################################################
#  MISC TARGETS
########################################################################
all:
	make api
	make test
#	make jar

api:
	$(JAVAC) -classpath $(BIN):$(LIB)gsi-classes.jar $(APISRC)


gsi:	lib/gsi-classes.jar
	rm -f $(APISRCDIR)$(IOSRBDIR)GSIAuth.java
	cp $(APISRCDIR)$(IOSRBDIR)GSIAuth.java_real $(APISRCDIR)$(IOSRBDIR)GSIAuth.java
	make all

lib/gsi-classes.jar:
	echo "For GSI Authentication, download the gsi-classes.jar"
	echo "Available at http://www.sdsc.edu/srb/jargon"
	false

notgsi:
	rm -f $(APISRCDIR)$(IOSRBDIR)GSIAuth.java
	cp $(APISRCDIR)$(IOSRBDIR)GSIAuth.java_fake $(APISRCDIR)$(IOSRBDIR)GSIAuth.java
	make all


#delete all the class and the jar files
clean:
	rm -rf $(CLASSES)
	rm -rf $(HOME)lib/$(JARGONJAR)

#delete everything created by this makefile
veryclean:
	make clean
	rm -rf $(BIN)edu/
	rm -rf $(DOC)*.html
	rm -rf $(DOC)package-list
	rm -rf $(DOC)edu/

size:
	echo "Calculating source file sizes (line numbers)"
	find $(APISRCDIR) -name '*.java' -exec cat {} \; | wc -l
#	find $(APISRCDIR) -name '*.java' -exec cat {} \; | grep "//" | wc -l
#	find $(APISRCDIR) -name '*.java' -exec cat {} \; | grep "*"  | wc -l
#	find $(APISRCDIR) -name '*.java' -exec cat {} \; | grep "*;" | wc -l



########################################################################
#  TEST TARGETS
########################################################################
test:
	$(JAVAC) -classpath $(BIN):$(LIB)gsi-classes.jar $(TESTSRCDIR)*.java




########################################################################
#  DOCUMENTATION BUILD TARGETS
########################################################################
docs:
	$(JAVADIR)javadoc -link http://java.sun.com/jdk/1.4/docs/api \
	-d $(DOC) \
	-stylesheetfile $(DOC)jargon.css \
	$(APISRC)
#	-overview $(APISRCDIR)overview.html \

pDocs:
	$(JAVADIR)javadoc -private -link http://java.sun.com/jdk/1.4/docs/api \
	-d $(DOC) \
	-overview $(APISRCDIR)overview.html \
	-stylesheetfile $(DOC)jargon.css \
	$(APISRC)



########################################################################
#  MULTI-PACKAGE BUILD TARGETS
########################################################################
jar:
	$(JAVADIR)jar cvfm $(LIB)$(JARGONJAR) $(LIB)MANIFEST.MF -C $(BIN) .
#need to add proper manifest
#	sign -o $(LIB)$(JARGONJAR) $(LIB)jargon_unsigned.jar




########################################################################
#  RELEASE TARGETS
########################################################################
full:
	make version 
	make veryclean 
	make gsi 
	make jar 
	make docs 
	cd doc
	jar cf $(DOCJAR) *
	cd ..
	mv doc/$(DOCJAR) $(WEBSITE)$(DOCJAR)
	mv doc $(WEBSITE)
	mv $(JARPATH) $(WEBSITE)$(JARGONJAR)
	make veryclean
	cp $(WEBSITE)$(JARGONJAR) $(JARPATH)
	jar cf $(SOURCEJAR) *
	mv $(SOURCEJAR) $(WEBSITE)$(SOURCEJAR)
	cp src/test/MetaDataTest.java $(WEBSITE)MetaDataTest.java
	cp src/test/Test.java $(WEBSITE)/Test.java
	make updateweb


source:
	make version
	make veryclean
	make gsi
	make jar
	mv $(JARPATH) $(WEBSITE)$(JARGONJAR)
	make clean
	cp $(WEBSITE)$(JARGONJAR) $(JARPATH)
	jar cf $(SOURCEJAR) *
	mv $(SOURCEJAR) $(WEBSITE)$(SOURCEJAR)
	cp src/test/MetaDataTest.java $(WEBSITE)MetaDataTest.java
	cp src/test/Test.java $(WEBSITE)Test.java
	make updateweb
	

version:
	cat $(HOME)src/api/edu/sdsc/grid/io/srb/SRBAccount.java | sed s/'static final String extraVersionInfo = "$(TITLE).*";'/'static final String extraVersionInfo = "$(RELEASENAME)";'/ > $(HOME)src/api/edu/sdsc/grid/io/srb/SRBAccount.java2
	mv $(HOME)src/api/edu/sdsc/grid/io/srb/SRBAccount.java2 $(HOME)src/api/edu/sdsc/grid/io/srb/SRBAccount.java
	
	
updateweb:
	cat $(WEBSITE)index.html | sed s/'$(OLDRELEASENAME)'/'$(RELEASENAME)'/ > $(WEBSITE)index.html2;
	mv $(WEBSITE)index.html2 $(WEBSITE)index.html
	cat $(WEBSITE)index.html | sed s/'Release $(OLDVERSIONNAME)'/'Release $(VERSIONNAME)'/ > $(WEBSITE)index.html2
	mv $(WEBSITE)index.html2 $(WEBSITE)index.html
