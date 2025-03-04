renv::restore()
library(quarto)
# Run Java Code Parser and script used to extract Code blocks from source code
source("RCode/CreateCodeBlocksFromSource.R")
# Compile qmd files and build book.  Book is written to a directory specified by "output-dir:" in _quarto.yml
quarto::quarto_render()


