## VRT extraction from the Finnish newspaper corpus

This directory contains the codes and instructions for extracting the data from the Finnish National Library's newspaper and periodicals corpus. 

The text corpus is available for research purposes through the Language Bank of Finland’s (LBF) download service (https://www.kielipankki.fi/download/klk/fi-v2/1771-2021/), but requires rights for access that can be applied for at https://www.kielipankki.fi/access/.

The data is available in the so-called VeRticalized Text (VRT) format. We used the vrt-files for the years 1960-1990 and extracted sentences for 7 newspapers and 11 periodicals (see wantedPapers.txt).

The tool (EmotionsFinnishVrtExtractor.jar) assumes the following files in the directory where the tool is launched from (with java -jar EmotionsFinnishVrtExtractor.jar <path_to_the directory_with_all_the_vrt_files>):
- finnishStopWords.txt which is a list of stop words from the Natural Language Toolkit (NLTK)
- mappedEmotionWords.csv contains the emotion words from Tuovila, the data, and the FinnWordNet. All the forms of the same word have been mapped to one lemma.
- wantedPapers.txt is a list of the newspapers and periodicals that were chosen to represent the corpus. Only sentences from these papers are used.
- paperNames.txt is a list of the actual paper names for each sentence in the corpora. The vrt files contain errors, but these names have been extracted by using the issn numbers.


