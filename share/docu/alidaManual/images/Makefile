all: analyzeExp.txt

analyzeData-collapsed-edited-withProperties.pdf:
	xwd -screen | convert - $@

normExp.data:
	java de.unihalle.informatik.Alida.tools.ALDOpRunner NormalizeExperimentalDataOp \
		experiment='{data=@data,description="Demo experiment"}' result=@$@

pcaExp.data: normExp.data
	java de.unihalle.informatik.Alida.tools.ALDOpRunner ALDPCAOp \
		experiment=@$< result=@$@

analyzeExp.txt: normExp.data
	java de.unihalle.informatik.Alida.tools.ALDOpRunner AnalyzeExperiment \
		experiment=@$< \
