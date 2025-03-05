library(tidyverse)
#Code assumes images are in project directory root under InputImages directory.

graphic_file <- paste0("'", gsub('/', '\\\\', list.files(path=paste0(getwd(), "/InputImages/"), full.names = TRUE)), "'")
get_captions_from_ads<-function(graphics_file_name){
  #browser()
  code_cap<-system2("powershell.exe", args = c("-command", "Get-Content", graphics_file_name, '-Stream "caption"'), stdout = TRUE)
}
code_ref=tools::file_path_sans_ext(gsub("'", "", gsub(".*\\\\", "", graphic_file)))
graphic<-lapply(X=graphic_file, FUN=get_captions_from_ads) %>% setNames(code_ref)


save(graphic, file = "graphicsData.RData")

