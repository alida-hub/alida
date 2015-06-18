TEXMAIN = AlidaManual.tex
PDFMAIN = AlidaManual.pdf

TEXFILES = 	Chipory.tex	\
			defines.tex	\
			frontmatter.tex	\
			intro.tex		\
			quickstart.tex	\
			oprunnergui-user.tex	\
			operator-user.tex \
			grappa-user.tex				\
			oprunner-user.tex \
			history-user.tex \
			configure-user.tex	\
			operator-programmer.tex \
			provider-programmer.tex	\
			history-programmer.tex \
			xmlprovider-programmer.tex \
			converter-programmer.tex \
			configure-programmer.tex 

all:	$(PDFMAIN)

$(PDFMAIN):	$(TEXMAIN) $(TEXFILES)
	pdflatex $(TEXMAIN)
	pdflatex $(TEXMAIN)
	pdflatex $(TEXMAIN)
	
%.wiki: %.tex
	java de/unihalle/informatik/Alida/admin/texToWiki/Tex2Wiki defs $*

install: $(PDFMAIN)

	cp $(PDFMAIN) ../../../../exportonrelease/
