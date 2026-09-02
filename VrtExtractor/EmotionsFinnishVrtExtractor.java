/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package emotionsfinnishvrtextractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Heidi Jauhiainen, University of Helsinki
 * Embodied Emotions: Ancient Mesopotamia and Today project
 * 
 * The tool extracts Finnish language sentences as lemmas from the National Library of Finland's 
 * digitized newspaper and periodicals corpus.
 * The corpus is in VRT-files.
 */
public class EmotionsFinnishVrtExtractor {

    /**
     * @param args the command line arguments 
     * args[0] = path and name of the folder with vrt-files
     * 
     * Assumes the following files in the folder where the tool is launched from:
     * wantedPapers.txt
     * finnishStopWords.txt
     * paperNames.txt
     * mappedEmotionWords.csv
     * 
     * Writes to the file sentences.txt the following columns:
     * name of the paper    paper with information on the issue     sentence
     */

    private static BufferedWriter textWriter;
    private static TreeMap<String, String> paperInfos;
    private static ArrayList<String> wanted;
    private static ArrayList<String> stopWords;
    private static TreeMap<String, String> emotions;

    public static void main(String[] args) throws IOException {
        String folderName = args[0];
        File folder = new File(folderName);
        readWanted("wantedPapers.txt");
        readStopWords("finnishStopWords.txt");
        readPaperInfo("paperNames.txt");
        //readPaperTypes("paperTypes");
        emotions = new TreeMap<>();
        readMappedEmotions("mappedEmotionWords.csv");
        try {
            //metaWriter = new BufferedWriter(new FileWriter("textMeta.txt"));
            textWriter = new BufferedWriter(new FileWriter("sentences.txt"));
            //read each vrt file in the folder given
            for (File file : folder.listFiles()) {
                if (file.getName().contains("vrt")) {
                    readNLPapers(file);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            //metaWriter.close();
            textWriter.close();
        }
    }

    //read the list of newspapers and periodicals that have been chosen (wantedPapers.txt)
    private static void readWanted(String filename) throws IOException {
        wanted = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                wanted.add(line);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
    
    //read the list of Finnish stop words (finnishSWs.txt)
    //from https://github.com/xiamx/node-nltk-stopwords/blob/master/data/stopwords/finnish with a few corrections
    private static void readStopWords(String filename) throws IOException {
        stopWords = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
    
    //read the list of the actual paper names for each sentence marked the sentence id
    //the vrt files have for some papers incorrectly a National Library internal MF-code instead of the name
    //the names have been extracted with the issn number using "https://portal.issn.org/resource/ISSN/" + issn + "?format=json"
    private static void readPaperInfo(String filename) throws IOException {
        paperInfos = new TreeMap<>();
        String[] lineArray;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line, id, name;
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\t");
                id = lineArray[0];
                name = lineArray[2];
                if (!paperInfos.containsKey(id)) {
                    paperInfos.put(id, name);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
    
    //read our list of emotions and their categories with the inflected words mapped to one emotion
    //the file mappedEmotionWords.csv also contains 4 plural body words that are mapped to their singular
    private static void readMappedEmotions(String filename) throws IOException {
        BufferedReader reader = null;
        String[] lineArray;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split(",");
                String emotionToUse = Normalizer.normalize(lineArray[0].trim(), Normalizer.Form.NFC);
                for (int i=0; i<lineArray.length; i++) {
                    if (!lineArray[i].equals("")) {
                        //emotions map contains emotions with the emotion their are mapped to
                        emotions.put(Normalizer.normalize(lineArray[i].trim(), Normalizer.Form.NFC), emotionToUse);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Reading emotions: "+e+"\t"+line);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    //read a vrt file
    private static void readNLPapers(File filename) throws IOException {
        BufferedReader reader = null;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader(filename));

            String label, page, text, lang, sentence, id, realPaper;
            Boolean first, used = false;
            String[] lineArray;
            int count, sentenceCount;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("<text ")) {
                    text = "";
                    first = false;
                    //get information about a text
                    label = getMatch(line, "label=\"([^\"]*)\"");
                    page = getMatch(line, "page_no=\"([^\"]*)\"");
                    realPaper = getMatch(line, "publ_title=\"([^\"]*)\"");
                    id = getMatch(line, "binding_id=\"([^\"]*)\"");

                    sentenceCount = 0;
                    count = 0;
                    if (!id.isBlank()) {
                        realPaper = paperInfos.get(id);
                    }
                    //continue if the paper is one of the ones we want
                    if (wanted.contains(realPaper)) {
                        while (!line.startsWith("<sentence")) {
                            line = reader.readLine();
                        }
                        while (!line.startsWith("</text")) {
                            //for each sentence
                            if (line.startsWith("<sentence")) {
                                first = false;
                                //get the language of the sentence
                                //this has been automatically classified with HeLI-OTS at the LBF
                                lang = getMatch(line, "lang=\"([^\"]*)\"");
                                sentence = "";
                                int xCount = 0;

                                while (!(line = reader.readLine()).startsWith("</sentence") && line != null) {
                                    lineArray = line.split("\t");
                                    //get the part-of-speech tag for the word
                                    String pos = lineArray[4];
                                    //leave out punctuation
                                    if (!pos.equals("Punct")) {
                                        //get the lemma for the word
                                        String lemma = Normalizer.normalize(lineArray[2], Normalizer.Form.NFC);
                                        //get the morphological analysis 
                                        //and change the lemma to "name" if it starts with SUBCAT_Prop indicating name
                                        String subPos = lineArray[5];
                                        if (subPos.startsWith("SUBCAT_Prop")) {
                                            lemma = "name";
                                        }
                                        //else check that it is a proper word (and get the correct emotion if applicable)
                                        else {
                                            lemma = checkWord(lemma);
                                        }
                                        //count the number of non-words
                                        if (lemma.equals("x")) {
                                            xCount++;
                                        }
                                        //ignore sentences without a verb in the beginning of the text 
                                        if (first == false && pos.equals("V")) {
                                            first = true;
                                        }
                                        //find the word combination "hyvä olle" 'feeling good' 
                                        //might replace a wrong hyvä if the sentence has several!
                                        if ((lemma.equals("olla") || lemma.equals("olo")) && sentence.endsWith(" hyvä ")) {
                                            sentence = sentence.replaceFirst("hyvä ", "hyväolla ");
                                        }
                                        //replace "vapauden tunne" with "vapaus"
                                        else if (lemma.equals("tunne") && sentence.endsWith(" vapauden ")) {
                                            sentence = sentence.replaceFirst("vapauden ", "vapaus ");
                                        }
                                        //check for stop words
                                        //from https://github.com/xiamx/node-nltk-stopwords/blob/master/data/stopwords/finnish with a few corrections
                                        else if (stopWords.contains(lemma)) {
                                            sentence += "<stop> ";
                                        }
                                        else {
                                            sentence += lemma + " ";
                                        }
                                        count++;
                                    }
                                }
                                sentence = sentence.replaceAll("name name", "name");
                                //if text has had a sentence with a verb 
                                //and the length of this sentence is 10 characters or longer
                                //and the language of this sentence is tagged as Finnish
                                if (first == true && sentence.split(" ").length > 9
                                        && lang.equals("fin")) {
                                    //furthermore, if sentence has content
                                    //and the sentence does not contain any known broken words
                                    //add the sentence to the text string
                                    if (!sentence.isBlank() && xCount < 1) {
                                        used = true;
                                        text += realPaper+"\t"+label + ", " + page + "\t" + sentence.toLowerCase() + "\n";
                                        sentenceCount++;
                                    }

                                }
                            }
                            line = reader.readLine();
                        }
                        //at the end ot the text write the text to file
                        //if it has any valid sentences
                        if (line.equals("</text>")) {
                            if (used) {
                                textWriter.write(text);
                                used = false;
                            }
                        }

                    }
                }

            }
        } catch (Exception e) {
            System.out.println(e+"\t"+line);
            e.printStackTrace(System.out);
        } finally {
            reader.close();
        }
    }
    
    //check the word to see if it is a valid Finnish word and return "x" if not
    private static String checkWord(String lemma) {
        if (
            // numerals + letters, hyphens
            lemma.matches(".*(?:[0-9]+\\p{L}|\\-[0-9]+|\\-\\-|\\-$|\\p{L}\\-\\p{L}{1,2}\\-).*") ||

            // too many consonants or vowels after each other
            lemma.toLowerCase().matches(".*(?:[b-df-hj-np-tv-z]{5,}|[aeiouäöå]{4,}).*") ||

            // special characters
            lemma.matches(".*[#:;\\*®©\\|~\\$€£™§¥±►□▼★♦\\^\\+°■•'’%/!\"”.,\\\\].*") ||

            // starts "00- "
            lemma.matches("00- .*") ||

            // HTML-entities
            lemma.contains("&amp") || lemma.contains("&gt") || lemma.contains("&alt")
            ) {
            return "x";
        }
        //if word does not contain any letters change it to NUM
        if (lemma.matches("[^\\p{L}]+")) {  
            return "NUM";
        }
        //word contains 3 or more consecutive characters of the same letter
        if (lemma.matches(".*(.)\1{2,}.*")) {
            return "x";
        }
        //renmove non-letters from the beginning of the word
        lemma = lemma.replaceFirst("^[^\\p{L}]*", "");
        //words with just on letter, words that start with more than one capital letter, words that have a capital letter after lowercase
        if (lemma.length()==1 || lemma.matches("\\p{Lu}{2,}\\p{Ll}.*") || lemma.matches(".*\\p{Ll}\\p{Lu}.*")) {
            //System.out.println(lemma);
            return "x";
        }
        //check if is one of the emotions that have to be mapped to the one on our list
        lemma = checkEmotion(lemma.toLowerCase());
        
        //Finnish words end in a vowel or n, t, or s
        if (lemma.toLowerCase().matches(".*[^aeiouyåäölnrst]")) {
            return "x";
        }
        
        return lemma;
    }
    
    //check if the word is one of the emotions that have to be mapped to the one on our list
    private static String checkEmotion(String lemma) {
        if (emotions.containsKey(lemma)) {
            return emotions.get(lemma);
        }
        return lemma;
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
