FILENAME = plot

PDF = $(FILENAME).pdf
TEX = $(FILENAME).tex
SRC = $(FILENAME).Rnw

TRASH = figure $(FILENAME).log $(FILENAME).aux


$(PDF): $(TEX)
	pdflatex $(TEX)
	rm -rf $(TRASH)

$(TEX): $(SRC)
	Rscript -e 'library(knitr); knit("$(SRC)")'

.PHONY: clean
clean:
	rm -rf $(TEX) $(TRASH)
