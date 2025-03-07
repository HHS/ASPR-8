
# Program Header --------------------------------------------------------------------------------------------------
#
# Title       : CreateCodeBlocksFromSource.R 
#
# Inputs:     : 1. root_dir - the root directory where java source code is to be searched in, recursively. 
#             : 2. file_ext - the file extensions to be searched (usually ".java" for this application).
#             : 3. start_delim - the regex delimiter that denotes the start of the java code block.
#             : 4. end_delim - the regex delimiter that denotes the end of the java code block.
#             : 5. file_ext <- the enxtension that designates output files (".txt" in this application).
#
# Primary
# Outputs:    : 1. fig - an R list containing a list of java code blocks, the names, and their captions.
#             : 2. graphic - a list of images references and their corresponding captions
#             : 3. metadata - a list of output references, the raw output, and their corresponding captions.
#
# Description : This Program finds java files contains start and end tags, extracts the code between the tags
#               names the code, and extracts the corresponding code caption for piping into the Quarto book
#               book chapters that make up the GCM documentation.  This program also extacts java output,
#               and pulls Figure Captions from graphicsData.RData
#
# Author      : Mark J. Lamias, Leidos, Inc. - https://github.com/mlamias
# Date        : Originally Created on 09/11/2023.  See file attributes or github for subsequent release dates
# Copyright   : 2023, US Department of Health and Human Services
#
# -----------------------------------------------------------------------------------------------------------------

library(tidyverse)
library(glue)
################################### Create Copies of Nucleus Files in Lessons ###################################
nucleas_directory<-"../simulation/src/main/java/gov/hhs/aspr/ms/gcm/nucleus/"
miscdir <- "misc/"
nucleas_files<-list.files(nucleas_directory, pattern="PluginData.java|PluginDataBuilder.java", full.names = TRUE)
# Make a copy of "off-path" nucleus file to misc directory in root for parsing
file.copy(from=nucleas_files, to=miscdir, overwrite=TRUE)


#Change this to the ASPR-8 tutorial lesson directory
root_dir <- "../lessons/"
file_ext <- ".java"
start_delim <- ".*/*.*start.*\\*/"
end_delim <- "\\*./*.*end.*\\*/"

# Find all files with the given extension in the root directory and its sub directories
input_code_path=tolower(
  list.files(
    path = c(root_dir, miscdir), 
    pattern = file_ext, 
    recursive = TRUE, 
    ignore.case = TRUE, 
    include.dirs = FALSE, 
    full.names = TRUE
  )
)


# Reads text file/source code from a given path and returns list of a matrix containing the position of the start/end
# tags along with the full text of the text file/source code
read_source_code_files<-function(path){
  
  # Need to account for start tags comments that break across several lines
  source_code_text <- readLines(path, warn=FALSE) %>% 
    tibble() %>% 
    setNames("file_line") %>% 
    mutate(
      # Mark TRUE those lines that start with /* or * as these are comments
      in_comment=grepl("^\\s*/\\*|^\\s*\\*", file_line),
      in_comment=case_when(
        grepl("^\\s*/\\*\\s*end", file_line)~FALSE, .default=in_comment)
    ) %>% 
    group_by(
      # Create Group numbers when in_comment changes from FALSE to TRUE or TRUE to FALSE
      group = consecutive_id(in_comment), in_comment) %>% 
    reframe(
      # Collapse only lines where we're inside of comments so the comment is contained on a single line for easier parsing
      source_code_line = 
        if_else(
          in_comment==TRUE, 
          unique(paste0(file_line, collapse = '\n')), 
          file_line)
    ) %>% 
    group_by(group, in_comment) %>% 
    filter(in_comment==FALSE | in_comment==TRUE & row_number()==which.max(row_number())) %>% 
    # Get the collapsed source code lines
    pluck(3) ->source_code_list
  
  # Place indices of the start/end tags in a matrix where each row designates the location of the code block to document
  begin_end<-grepl(".*/*.*start.*\\*/|.*/*.*end.*\\*/", source_code_list, fixed = FALSE) %>% which() %>% 
    matrix(ncol=2, byrow=T)
  
  # Wrap up the start/end tag matrix and the source code in a list and return it
  source_plus_indices<-list(start_stop_indices=begin_end, source_code=source_code_list)
  
  return(source_plus_indices)
}

# Remove extraneous blank lines that might appear after a start tag and before any other code appears in the code block
removeLeadingBlanks <- function(char_vector) {
  while (length(char_vector) > 0 && grepl("^\\s*$", char_vector[1])) {
    char_vector <- char_vector[-1]
  }
  return(char_vector)
}

# For a pair of start/end tag indices, extract the code_ref and code_cap that appears in the start tag, and extract
# the text found between the start_stop_indices
extract_code_blocks<-function(start_stop_indices, source_code){

    # Extract the source code between (and not including) the start/end tag indices and save into extracted_code
    extracted_code<-source_code[(start_stop_indices[1]+1):(start_stop_indices[2]-1)]
    # Extract the first line which contains the complete start tag, now all one one line
    start_line<-source_code[(start_stop_indices[1])]
    
    # Parse our the start tag to isolate the code_ref= and code_cap= portions of the tag
    start_line_string<-str_remove_all(start_line, "^\\s*\\/\\*\\s*start\\s+|\\s*\\*\\/$") %>% 
      str_trim(., side="both") %>% 
      str_split_1(pattern=fixed("|")) %>% 
      stringr::str_split(pattern="=") 
    
    # Pluck out the code_ref (code reference) name.
    pre_code_reference <- start_line_string %>% 
      pluck(1,2) %>% 
      str_trim()
    # Pluck out the code_cap (code caption) name.
    pre_code_caption <- start_line_string %>% 
      pluck(2,2) %>% 
      gsub("(?<=^\\s|^)(\\n|\\t|\\*)|\\s*(\\n|\\t|\\*)", "", ., perl = TRUE) %>% 
      str_trim()
    
    # Extract first line that immediately follows the start tag line, which is the first actual source 
    # code line (not a tag)
    first_code_line <- source_code[(start_stop_indices[1]+1):(start_stop_indices[1]+1)]
    
    # Extract all tabs that start at the beginning of that first line so they can be removed from subsequent lines
    # in this extracted source code block.  This allows the documented code to start flush to the left of the code
    # block in the documentation.
    tab_strings_to_remove<-paste0("^", str_extract_all(first_code_line, regex("^\t+")))
    
    # Remove from all lines of code the same number of tabs as contained in the first line of code to make for 
    # better printing in the documentation
    extracted_code_notabs<-sub(tab_strings_to_remove, "", extracted_code) 
      # Remove any // at the ends of lines of code.
      #gsub("\\s*//$", "", .)    -- Added back in per shawn
    
    # If there entire first line of code is blank
    if (grepl("^\\s*$", extracted_code_notabs[1])) {
      #Remove any continuous (line to line) blank lines at the beginning of extracted code
      extracted_code_notabs <- removeLeadingBlanks(extracted_code_notabs)
    } 
    returned_named_list <- list(list(
      code_ref=pre_code_reference,
      #performed gsub to remove apostrophes in caption strings from corruptions HTML code documentation.
      code_cap=paste0("id=lst-", pre_code_reference, " lst-cap='",  gsub("'", "&#39;", pre_code_caption), "'"),  #modified for quarto bug
      code=paste0(extracted_code_notabs, collapse="\n")
    ))
    names(returned_named_list)<-pre_code_reference
    
    return(returned_named_list)
}


# Check each piece of source code for start/end tags. If the source code has some, for each set of start/end tags,
# run the function to extract the text between the tags.
create_source_code_variables<-function(source_and_indicies_list){
  # Only process source code files that have start/end tags in them
  if (nrow(source_and_indicies_list$start_stop_indices) > 0)
  {
    # For each row of the start/end indices matrix run the function that extracts the text between the start/end tags
    apply(
      X=source_and_indicies_list$start_stop_indices, MARGIN=1, 
      FUN=extract_code_blocks, 
      source_and_indicies_list$source_code
    ) %>% unlist(., recursive = FALSE) # unlist the first level to make it easier to work with the resulting list.
  }
}


# First, For every file found in the source code directory read in the source code and find the start/end tag indices.

# Then, For each element consisting of the start/end tag matrix and the source code, run extract the source code for
# documentaiton and assign the code reference name and caption to the text
fig<-lapply(input_code_path, FUN=read_source_code_files) %>% 
  sapply(., create_source_code_variables) %>% discard(is.null) %>% 
  unlist(recursive = FALSE) %>% 
  discard(is.na(names(.)))

################################### Load Graphic Files ###################################

#The next lin assumes all captions ADFs have been set and are stored in graphicsData.RData in project root.  
#See GetAlternateFileStreamForGraphicCaptions.R
load("graphicsData.RData")

################################### Load Output Files ###################################

root_dir <- "../lessons/"
file_ext <- ".txt"
output_files<-list.files(root_dir, pattern = file_ext, recursive = TRUE, full.names = TRUE) %>% tibble(file=.) %>% 
  filter(!grepl("target", file))

metadata<-sapply(output_files$file, read_file, USE.NAMES = FALSE) %>% tibble(file_text=.) %>%  
  mutate(
    code_ref=str_extract(file_text, "(?<=code_ref=)[^|]+(?=\\|)") %>%  gsub("'", "&#39;", .),
    code_cap=str_extract(file_text, "(?<=code_cap=)[^(\\*\\/)]+(?=\\*\\/)") %>%  gsub("'", "&#39;", .),
    code_out=gsub("^/\\*\\s*start\\s*.*\\*\\/\\r\\n|\\r\\n/\\*\\s*end\\s*\\*\\/$", '', file_text, perl = TRUE),
    new_name=code_ref
  ) %>% select(-file_text) %>% column_to_rownames("new_name") %>%  split(row.names(.))


