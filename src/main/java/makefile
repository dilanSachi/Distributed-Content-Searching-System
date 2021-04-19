JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./Node.java \
	./BootstrapServer.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class