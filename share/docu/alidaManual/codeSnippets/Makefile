DEMODIR =  ../../../../src/main/de/unihalle/informatik/Alida/demo

DERIVED = NormalizeExperimentalDataOp-use.snippet \
		MatrixSum-declare.snippet \
		MatrixSum-parameters.snippet \
		MatrixSum-constructor.snippet \
		ApplyToMatrix-parameters.snippet \
		ALDArraySum-constructor.snippet \
		ExperimentalData.snippet

all: $(DERIVED)

%-use.snippet: $(DEMODIR)/%.java
	sed -n -e '/SNIPPETCODE:useBegin/,/SNIPPETCODE:useEnd/'p $< | sed -e 1d -e '$$d' > $@
%-declare.snippet: $(DEMODIR)/%.java
	sed -n -e '/SNIPPETCODE:declareBegin/,/SNIPPETCODE:declareEnd/'p $< | sed -e 1d -e '$$d' > $@
%-parameters.snippet: $(DEMODIR)/%.java
	sed -n -e '/SNIPPETCODE:parametersBegin/,/SNIPPETCODE:parametersEnd/'p $< | sed -e 1d -e '$$d' > $@
%-constructor.snippet: $(DEMODIR)/%.java
	sed -n -e '/SNIPPETCODE:constructorBegin/,/SNIPPETCODE:constructorEnd/'p $< | sed -e 1d -e '$$d' > $@

ExperimentalData.snippet: $(DEMODIR)/ExperimentalData.java
	sed -n -e '/SNIPPETCODE:Begin/,/SNIPPETCODE:End/'p $< | sed -e 1d -e '$$d' > $@
	
