## Extractor of emotion words from the FinnWordNet

This directory contains the code, files, and instructions for extracting emotion words from the FinnWordNet and adding them to the words chosen from the Tuovila thesis.

The tool is used to add emotion words to an existing list of emotions (_EmotionWords_withAdditionsFromData.csv_). The original list was compiled by taking words from the Tuovila (2025) thesis and adding to those words their respective verbs as well as variations of them found in the Finnish National Library data. An 'X' in one of the first 14 columns tells into which category of emotions a word belongs. The categories are listed on the first row. This list is in Finnish in order to find categories for the words in FinnWordNet. If a hypernym (marked with ',@') on a line in the WordNet is one of our categories, the words on that line are extracted. For more information on the extraction process, see the comments in _EmotionFinnishWordNetExtractor.java_.

The tool extracts words from the FinnWordNet files _noun.feeling_ and _verb.emotion_. The tool assumes that those files are in the folder it is launched from. 

To start the tool on command line:

```
java -jar EmotionFinnishWordNetExtractor.jar <path_to>EmotionWords_withAdditionsFromData.csv > <desiredNameOfFile>.csv
```

The output is in the same format as the existing list of emotion words, i.e. the tool assigns the new words in one of the existing categories. This time the categories are in English. The resulting list was manually modified before used to produce the data that was used in the analysis of bodily emotions as described in the article. Some of the words are in two categories and these were combined and mapped to just one lemma (see _emotionsAfterWordNet.pdf_). Additionally, the category information was removed. See _../VrtExtractor/mappedEmotionWords.csv_

The emotion word list _EmotionWords_withAdditionsFromData_ maps all the different forms of the same word to one emotional lemma representing them.  

FinnWordNet = University of Helsinki (2010). The Downloadable Version of FinnWordNet 2.0 [data set]. Kielipankki. https://urn.fi/urn:nbn:fi:lb-2016042503.

Tuovila, S. (2005). _Kun on tunteet: Suomen kielen tunnesanojen semantiikkaa_. Oulu: Oulu University Press.
