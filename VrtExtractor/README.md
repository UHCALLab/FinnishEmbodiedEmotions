## VRT extraction from the Finnish newspaper corpus

This directory contains the codes and instructions for extracting the data from the Finnish National Library's newspaper and periodicals corpus. 

The text corpus is available for research purposes through the Language Bank of Finland’s (LBF) download service (https://www.kielipankki.fi/download/klk/fi-v2/1771-2021/), but requires rights for access that can be applied for at https://www.kielipankki.fi/access/.

The data is available in the so-called VeRticalized Text (VRT) format. We used the vrt-files for the years 1960-1993 and extracted sentences for 7 newspapers and 11 periodicals (see wantedPapers.txt).

The tool (EmotionsFinnishVrtExtractor.jar) assumes the following files in the directory where the tool is launched from:
- finnishStopWords.txt which is a list of stop words from the Natural Language Toolkit (NLTK)
- mappedEmotionWords.csv contains the emotion words from Tuovila, the data, and the FinnWordNet. All the forms of the same word have been mapped to one lemma. This file has to first be created by using the WordNet extractor. Alternatively change the name of the file mappedEmotionWords__example to mappedEmotionWords.csv.
- wantedPapers.txt is a list of the newspapers and periodicals that were chosen to represent the corpus. Only sentences from these papers are used.
- paperNames.txt is a list of the actual paper names for each sentence in the corpora. The vrt files contain errors, but these names have been extracted by using the issn numbers.

Start the tool from the command line:

```
java -jar EmotionsFinnishVrtExtractor.jar <path_to_the directory_with_all_the_vrt_files>
```

The resulting file _sentences.txt_ contains one sentence per line. Only lines that have no automatically detectable words that had been broken by the OCRing and that have at least 10 words have been included. In the beginning of each line is the name of the newspaper/periodical (as they are in the paperNames.txt list). Also included is the label-tag with the page_nr-tag from the <text> line giving information on the text. When using the PMI-Embeddings tool the sentences are first stripped of the paper information (i.e. the first 2 columns) and then arranged one word per line with a '#' between sentences. The command to do this on the unix command line is:

```
cut -d$'\t' -f3 sentences.txt | tr '[:space:]' '[\n*]' | sed 's/^$/#/g' > <desired_name_of_file>
```


