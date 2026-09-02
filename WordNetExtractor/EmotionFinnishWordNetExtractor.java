/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package emotionfinnishwordnetextractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Heidi Jauhiainen, University of Helsinki
 * Embodied Emotions: Ancient Mesopotamia and Today project
 * 
 * Tool to extract emotion words from the FinnWordNet (Lindén & Niemi 2014) 
 * downloaded from the Language Bank of Finland https://www.kielipankki.fi/lexical-conceptual-resources/finnwordnet-group/
 * 
 */
public class EmotionFinnishWordNetExtractor {

    /**
     * @param args the command line arguments
     * args[0] = name of the file containing emotion words collected from 
     *      Tuovila, S. (2005). Kun on tunteet: Suomen kielen tunnesanojen semantiikkaa. Oulu: Oulu University Press.
     * args[1] = name of the nouns.feeling file
     * args[2] = name of the verbs.emotion file
     */
    
    private static TreeMap<String, ArrayList<String>> emotionsWithDerivatives;
    private static TreeMap<String, ArrayList<String>> categories;
    private static TreeMap<Integer, String> cat2number;
    private static TreeMap<String, Integer> number2cat;
    private static TreeMap<String, String> emotion2mapped;
    private static TreeMap<String, String> emotion2cat;
    private static TreeMap<String, TreeMap<String, ArrayList<String>>> abbreviations;
    
    public static void main(String[] args) throws IOException {
        // we use a list compiled from Tuovila and supplemented with verbs and wordforms from the data
        // the words are classified into 14 categories (Volonyts)
        readKnownEmotions(args[0]);
        //The Downloadable Version of the Finnish WordNet
        //FinnWordNet-dl 2.0
        //http://urn.fi/urn:nbn:fi:lb-2014052714
        //read noun.feeling
        readNouns(args[1]);
        //read verb.emotion
        readVerbs(args[2]);
        //make 3 letter abbreviations of the emotion words
        makeAbbreviations();
        //map the new words to the existing categories
        mapWords();
        //print the emotions as a table
        printAsTable();
        
    }
    
    //read the list of emotion words compiled from Tuovila and supplemented with verbs and wordforms from the data
    //the words are classified into 14 categories (Volonyts)
    //the file read has:
    //1. line 0 names of the categories
    //2.- X if the words on the line belong to the category in the column; then the emotions of a group
    private static void readKnownEmotions(String filename) throws IOException {
        //map with the emotion we use in the analysis and the derivatives that belong to its group in an arraylist
        emotionsWithDerivatives = new TreeMap<>();
        //emotion categories with the emotions we already had for each of them in an arraylist
        categories = new TreeMap<>();
        //map with a number for each category, the number reflects the order/place of the the category on the line read
        cat2number = new TreeMap<>();
        //map with category for each number, the number reflects the order/place of the the category on the line read
        number2cat = new TreeMap<>();
        //all the emotion words with the word that each of them is mapped to
        emotion2mapped = new TreeMap<>();
        //all the emotion words with the category they belong to
        emotion2cat = new TreeMap<>();
        ArrayList<String> otherEmotions;
        ArrayList<String> categoryEmotions;
        String line = "", emotion, emotionCategory = "";
        String[] lineArray;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            line = reader.readLine();
            lineArray = Normalizer.normalize(line, Normalizer.Form.NFC).split(",");
            int count = 0;
            //reading the 1. line, initialize the category maps
            for (String category : lineArray) {
                if (!category.isBlank()) {
                    categoryEmotions = new ArrayList<>();
                    categories.put(category, categoryEmotions);
                    cat2number.put(count, category);
                    number2cat.put(category, count++);
                }
            }
            //for all the other lines
            while ((line = reader.readLine()) != null) {
                otherEmotions = new ArrayList<>();
                lineArray = Normalizer.normalize(line, Normalizer.Form.NFC).split(",");
                //first 14 tabs, if X get the category for the words in the line
                for (int i=0; i<14; i++) {
                    if (lineArray[i].equals("X")) {
                        emotionCategory = cat2number.get(i);
                    }
                }
                //first emotion word is the one used for analysis
                String emotionToUse = Normalizer.normalize(lineArray[14].trim(), Normalizer.Form.NFC);
                //if mainEmotions contains the the first emotion word, get the list of emotions in its group
                //this should not happen?
                if (emotionsWithDerivatives.containsKey(emotionToUse)) {
                    otherEmotions = emotionsWithDerivatives.get(emotionToUse);
                }
                //add all the words from the line to emotions, main emotions and categories
                for (int i=14; i<lineArray.length; i++) {
                    if (!lineArray[i].equals("")) {
                        emotion = Normalizer.normalize(lineArray[i].trim(), Normalizer.Form.NFC);
                        if (!otherEmotions.contains(emotion)) {
                            otherEmotions.add(emotion);
                        }
                        categoryEmotions = categories.get(emotionCategory);
                        categoryEmotions.add(emotion);
                        categories.put(emotionCategory, categoryEmotions);
                        emotion2mapped.put(emotion, emotionToUse);
                        emotion2cat.put(emotion, emotionCategory);
                    }
                }
                emotionsWithDerivatives.put(emotionToUse, otherEmotions);
            }
        }
        catch (Exception e) {
            System.out.println(e+"\t"+line);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        } 
    }
    
    //read WordNet noun.feeling file
    private static void readNouns(String filename) throws IOException {
        BufferedReader reader = null;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader(filename));
            String bracketLine, newWord, otherWords, hypernym;
            String[] lineArray, bracketArray, wordArray;
            ArrayList<String> wordList;
            while ((line = reader.readLine()) != null) {
                line = Normalizer.normalize(line, Normalizer.Form.NFC).replaceAll("\\{ ", "");
                //process only lines that contain verb.emotion
                if ((line.contains("verb.") && line.contains(".emotion"))) {
                    line = line.replaceAll(" +", " ");
                    line = line.replaceAll(" \\}", "");
                    line = line.replaceAll("\\(.*\\)", "");
                    //if (!line.contains("noun.Tops:") && !line.startsWith("[")) {
                    //don't process lines that contain noun.Tops
                    if (!line.contains("noun.Tops:")) {
                        //System.out.println(line);

                        wordList = new ArrayList<>();
                        line = line.replaceAll("<[^>]+>", "");
                        //words ending in ',@' are hypernyms (https://wordnet.princeton.edu/documentation/wninput5wn)
                        //we take that to be the category for the words on the line
                        hypernym = getMatch(line, " ([^ ]+),@").replaceAll("[^\\p{L}]*", "");
                        //System.out.println(category);

                        bracketArray = line.split(",+\\]");
                        for (String bracket : bracketArray) {
                            bracket = bracket.replaceAll("[\\[\\]]", "").trim();
                            lineArray = bracket.split(" ");
                            for (String word : lineArray) {
                                //do not take antonyms ',!' or words starting with a capital letter
                                if (!word.endsWith(",!") && !word.matches("\\p{Lu}.*")) {
                                    wordArray = new String[2];
                                    wordArray[0] = word;
                                    //take all adjectives but only verbs that are categorized as 'emotion'
                                    if (word.contains(":") && (word.contains("adj.all") || word.contains("verb.emotion"))) {
                                        word = word.split(":")[1];
                                        wordArray = word.split("\\^");
                                    }
                                    for (String thisWord : wordArray) {
                                        if (thisWord != null && !thisWord.contains("_") && !(thisWord.contains("adj.") || thisWord.contains("verb.")) || word.contains("noun.")) {
                                            if (thisWord != null && !thisWord.isBlank()) {
                                                thisWord = thisWord.replaceAll("[^\\p{L}]+", "");
                                                wordList.add(thisWord);
                                            }   
                                        }
                                    } 
                                }
                            }
                            boolean found = false;
                            //if Volynets categories does not contain the hypernym
                            if (!categories.containsKey(hypernym)) {

                                //check if the extended Tuovila list contains the hypernym and get its category
                                if (emotion2mapped.containsKey(hypernym)) {
                                    hypernym = emotion2cat.get(hypernym);
                                    //System.out.println("Emotions: "+hypernym);
                                    found = true;
                                }
                            }
                            else {
                                found = true;
                            }
                            if (found) {
                                //add new words in the emotion - category map
                                for (String thisWord : wordList) {
                                    
                                    if (!emotion2cat.containsKey(thisWord)) {
                                        emotion2cat.put(thisWord, hypernym);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e+"\t"+line);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    //after nouns read WordNet verb.emotion file
    private static void readVerbs(String filename) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line, category;
            String[] lineArray, bracketArray, wordArray;
            ArrayList<String> wordList;
            while ((line = reader.readLine()) != null) {
                line = Normalizer.normalize(line, Normalizer.Form.NFC).replaceAll("\\{ ", "");
                //consider only lines that contain noun.feeling
                if ((line.contains("noun.") && line.contains(".feeling"))) {
                    line = line.replaceAll(" +", " ");
                    line = line.replaceAll("frames:.*$", "");
                    wordList = new ArrayList<>();
                    line = line.replaceAll("<[^>]+>", "");
                    bracketArray = line.split(",+\\]");
                    for (String bracket : bracketArray) {
                        bracket = bracket.replaceAll("[\\[\\]]", "").trim();
                        lineArray = bracket.split(" ");
                        for (String word : lineArray) {
                            //do not take antonyms ',!', compounds, or words starting with a capital letter
                            if (!word.endsWith(",!") && !word.contains("_") && !word.matches("\\p{Lu}.*")) {
                                wordArray = new String[2];
                                wordArray[0] = word;
                                //take all adjectives but only nouns that are categorized as 'feeling'
                                if (word.contains(":") && (word.contains("adj.all") || word.contains("noun.feeling"))) {
                                    word = word.split(":")[1];
                                    wordArray = word.split("\\^");
                                }
                                for (String thisWord : wordArray) {
                                    if (thisWord != null && !word.contains("_") && !(thisWord.contains("adj.") || thisWord.contains("noun.") || thisWord.contains("verb."))) {
                                        if (!thisWord.isBlank()) {
                                            thisWord = thisWord.replaceAll("[^\\p{L}]+", "");
                                            wordList.add(thisWord);
                                        }
                                    }   
                                }
                            }
                        }
                    }
                    for (String thisWord : wordList) {

                        //if the list of words from the line contains a word that is already in the emotion - emotionToUse map
                        //if (emotion2cat.containsKey(thisWord)) {
                        if (emotion2mapped.containsKey(thisWord)) {
                            //get the category of that word
                            category = emotion2cat.get(thisWord);
                            //add all new words from the line to the map with the category
                            for (String thatWord : wordList) {
                                
                                if (!emotion2cat.containsKey(thatWord)) {
                                    emotion2cat.put(thatWord, category);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    //make 3 letter abbreviations of the emotion words
    //use only the words from the expanded Tuovila list
    private static void makeAbbreviations() {
        abbreviations = new TreeMap<>();
        TreeMap<String, ArrayList<String>> cats;
        String emotion, category, abbr;
        ArrayList<String> words;
        //for each word in the expanded Tuovila list
        for (Map.Entry<String, String> entry : emotion2mapped.entrySet()) {
            emotion = entry.getKey();
            //get category
            category = emotion2cat.get(emotion);
            cats = new TreeMap<>();
            //get the first 3 letters from the emotion word
            abbr = emotion.substring(0, 3);
            words = new ArrayList<>();
            //add the word to the 3 letter abbrevision
            if (abbreviations.containsKey(abbr)) {
                //each abbreviation has categories with words
                cats = abbreviations.get(abbr);
                if (cats.containsKey(category)) {
                    words = cats.get(category);
                }
            }
            //if not the right category of the abbreviation of this word has the word yet, add it
            if (!words.contains(emotion)) {
                words.add(emotion);
            }
            cats.put(category, words);
            abbreviations.put(abbr, cats);
            
        }
    }
    
    //Adding the new words from FinnWordNet to list of main emotions
    //Using abbreviations to make sure the words with the same root are together
    private static void mapWords() {
        String newEmotion, emotionToUse, category;
        ArrayList<String> otherEmotions;
        TreeMap<String, ArrayList<String>> cats;
        String abbr;
        ArrayList<String> words;
        //new words were added to emotion2cat with the category they belong to
        for (Map.Entry<String, String> entry : emotion2cat.entrySet()) {
            newEmotion = entry.getKey();
            category = entry.getValue();
            if (!newEmotion.isBlank()) {
                //if map with emotions with the word they are mapped to already contains the emotion
                if (emotion2mapped.containsKey(newEmotion)) {
                    emotionToUse = emotion2mapped.get(newEmotion);
                    otherEmotions = emotionsWithDerivatives.get(emotionToUse);
                    //make sure that emotoionsWithDerivatives has this emotion 
                    //on the list of all the emotions mapped to emotion that is to used in analysis
                    if (!otherEmotions.contains(newEmotion)) {
                        otherEmotions.add(newEmotion);
                        emotionsWithDerivatives.put(emotionToUse, otherEmotions);
                    }
                }
                //if not
                else {
                    cats = new TreeMap<>();
                    words = new ArrayList<>();
                    abbr = newEmotion.substring(0, 3);
                    //if abbreviations contains the abbrevations of this emotion word
                    if (abbreviations.containsKey(abbr)) {
                        cats = abbreviations.get(abbr);
                        //if the categories of this abbreviation contains the category of the word
                        //get the words with this abbreviation and category
                        if (cats.containsKey(category)) {
                            words = cats.get(category);
                        }
                    }
                    //add the word if not there yet
                    if (!words.contains(newEmotion)) {
                        words.add(newEmotion);
                        cats.put(category, words);
                        abbreviations.put(abbr, cats);
                    }
                    //get the emotion to use for this word
                    if (emotion2mapped.containsKey(words.get(0))) {
                        emotionToUse = emotion2mapped.get(words.get(0));
                    }
                    else {
                        emotionToUse = words.get(0);
                    }
                    //add the derivative words with this word to the emotion to use in the map with all emotions to use with their derivatives
                    emotionsWithDerivatives.put(emotionToUse, words);
                }
            }
            
        }
        
    }
    
    //print out emotionsWithDerivatives as a table with the category of the words on the line marked with X
    private static void printAsTable() {
        String emotionToUse, category;
        ArrayList<String> otherEmotions;
        for (Map.Entry<String, ArrayList<String>> entry : emotionsWithDerivatives.entrySet()) {
            emotionToUse = entry.getKey();
            category = emotion2cat.get(emotionToUse);
            int catNr = number2cat.get(category);
            otherEmotions = entry.getValue();
            for (int i=0; i<14; i++) {
                if (catNr == i) {
                    System.out.print("X,");
                }
                else {
                    System.out.print(",");
                }
            }
            for (String word : otherEmotions) {
                System.out.print(word.trim()+",");
            }
            System.out.println("");
        }
    }
  
    //get the part of the sentence defined by the pattern
    private static String getMatch(String line, String pattern) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(line);
        
        String match = "";
        if (matcher.find()) {
            match = matcher.group(1);
        }
        return match;
    }
}
