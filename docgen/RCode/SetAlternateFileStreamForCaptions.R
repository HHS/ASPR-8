library(tidyverse)
input_csv_file_name<-"RCode/ImageManifest/ImageManifest.csv"

image_manifest <- read_csv(input_csv_file_name) %>% 
  mutate(file_name=gsub('/', '\\',glue("'{getwd()}/InputImages/{ref}.svg'"), fixed=TRUE), clean_caption=glue('\\\"{caption}\\\"')) %>% 
  select(file_name, clean_caption)

apply_captions_to_ads<-function(df){
  system2("powershell.exe", args = c("-command", "Set-Content -Path", df["file_name"], "-Stream 'caption'", "-value", df["clean_caption"]), stdout = TRUE)  
}

apply(X=image_manifest, MARGIN=1, FUN=apply_captions_to_ads)
