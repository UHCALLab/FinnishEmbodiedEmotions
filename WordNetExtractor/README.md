## Extractor of emotion words from the FinnWordNet

This directory contains the code, files, and instructions for extracting emotion words from the FinnWordNet and adding them to the words chosen from the Tuovila thesis.

The tool is used to add emotion words to an existing list of emotions (EmotionWords_withAdditionsFromData.csv). The original list was compiled by taking words from the Tuovila thesis and adding to those words their respective verbs as well as variations of them found in the Finnish National Library data. The list also tells into which categories of emotions a word belongs. The tool extracts words from the FinnWordNet files _noun.feeling_ and _verb.emotion_. The tool assumes that those files are in the folder it is launched from. The output is in the same format as the existing list of emotion words, i.e. the tool assigns a category to new words.

To start the tool on command line:

```
java -jar EmotionFinnishWordNetExtractor.jar <path_to>EmotionWords_withAdditionsFromData.csv
```

Tuovila, S. (2005). _Kun on tunteet: Suomen kielen tunnesanojen semantiikkaa_. Oulu: Oulu University Press.
