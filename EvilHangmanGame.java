package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame{
    private Set<String> dictWords;
    private Map<String, Set<String>> partition;
    private SortedSet<Character> guessedLetters;
    private Map<String, Set<String>> largestPatterns;
    private String pattern;


    public EvilHangmanGame() {
        dictWords  = new HashSet<>();
        partition = new HashMap<>();
        guessedLetters = new TreeSet<Character>();
        largestPatterns  = new HashMap<>();
        pattern = "";
    }
    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        if(dictionary.length() == 0) {
            throw new EmptyDictionaryException("The dictionary is empty!");
        }

        Scanner scanner = new Scanner(dictionary);

        while(scanner.hasNext()) {
            String str = scanner.next().toLowerCase();
            if(str.length() == wordLength) {
                dictWords.add(str);
            }
        }

        if(dictWords.size() == 0) {
            throw new EmptyDictionaryException("The dictionary is empty!");
        }

        StringBuilder subSetKey = new StringBuilder();
        for(int i = 0; i < wordLength; i++) {
            subSetKey.append("-");
        }

        pattern = subSetKey.toString();
        partition.put(subSetKey.toString(), dictWords);
    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        guess = Character.toLowerCase(guess);
        partition.clear();
        //if char hasn't been guessed, add to guessed letters
         if(!guessedLetters.contains(guess)) {
             guessedLetters.add(guess);
         }
         else {
             throw new GuessAlreadyMadeException("Guess already made!");
         }

         //take the guessed char and find all the words with that char
        //create patterns where that char is located in each word
        //if that pattern doesn't exist add it, and word its value (set)
        //else add that pattern exists, add that word to its value (set)
        for(String word : dictWords) {
            StringBuilder pattern =  new StringBuilder(getPattern());
            for(int i = 0; i < word.length(); i++) {
                if(word.charAt(i) == guess) {
                    pattern.replace(i,i+1, String.valueOf(guess));
                }
            }
            if(!partition.containsKey(pattern.toString())) {
                Set<String> patternWords = new HashSet<>();
                patternWords.add(word);
                partition.put(pattern.toString(), patternWords);;
            }
            else {
                Set<String> patternWords = partition.get(pattern.toString());
                patternWords.add(word);
                partition.replace(pattern.toString(), patternWords);
            }
        }

//        System.out.println("Partition Patterns");
//        for(Map.Entry<String,Set<String>> wordPattern : partition.entrySet()) {
//            System.out.println(wordPattern.getKey() + " = " + wordPattern.getValue());
//        }

        int setSize = 0;
        for(Map.Entry<String,Set<String>> wordPattern : partition.entrySet()) {
            if (largestPatterns.size() == 0) {
                setSize = wordPattern.getValue().size();
                largestPatterns.put(wordPattern.getKey(), wordPattern.getValue());
            }
            else if(setSize < wordPattern.getValue().size()) {
                largestPatterns.clear();
                setSize = wordPattern.getValue().size();
                largestPatterns.put(wordPattern.getKey(), wordPattern.getValue());
            }
            else if(setSize == wordPattern.getValue().size()) {
                largestPatterns.put(wordPattern.getKey(), wordPattern.getValue());
            }
        }

//        System.out.println("Large Patterns");
//        for(Map.Entry<String,Set<String>> largePattern : largestPatterns.entrySet()) {
//            System.out.println(largePattern.getKey() + " = " + largePattern.getValue());
//        }

        if(largestPatterns.size() == 1) {
            for(Map.Entry<String,Set<String>> words : largestPatterns.entrySet()) {
                pattern = words.getKey();
                partition.clear();
                dictWords = words.getValue();
                return words.getValue();
            }
        }
        else if(largestPatterns.size() > 1){
            return tieBreakers(guess);
        }

         return null;
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return guessedLetters;
    }

    private Set<String> tieBreakers(char guess) {
        Map<String, Set<String>> fewestAppearances = new HashMap<>();
        int appearances = 10;
        for(Map.Entry<String,Set<String>> largePattern : largestPatterns.entrySet()) {
            int letterAppearances = 0;
            String wordPattern = largePattern.getKey();
            for(int i = 0; i < wordPattern.length(); i++) {
                if(wordPattern.charAt(i) == guess) {
                    letterAppearances++;
                }
            }

            if(letterAppearances == 0) {
                pattern = largePattern.getKey();
                partition.clear();
                dictWords = largePattern.getValue();
                return largePattern.getValue();
            }
            else if(letterAppearances < appearances) {
                appearances = letterAppearances;
                fewestAppearances.clear();
                fewestAppearances.put(largePattern.getKey(), largePattern.getValue());
            }
            else if(letterAppearances == appearances) {
                fewestAppearances.put(largePattern.getKey(), largePattern.getValue());
            }
        }

        if(fewestAppearances.size() == 1) {
            for(Map.Entry<String,Set<String>> appearance : fewestAppearances.entrySet()) {
                pattern = appearance.getKey();
                partition.clear();
                dictWords = appearance.getValue();
                return appearance.getValue();
            }
        }

        //largestPatterns.clear();
        return doubleTieBreaker(fewestAppearances, guess);
    }

    private Set<String> doubleTieBreaker(Map<String, Set<String>> fewestAppearances, char guess) {
        Map<String, Set<String>> rightMost = new HashMap<>();
        int pos = 0;
        for(Map.Entry<String,Set<String>> appearances : fewestAppearances.entrySet()) {
            int rightMostPos= 0;
            String wordPattern = appearances.getKey();
            for(int i = 0; i < wordPattern.length(); i++) {
                if(wordPattern.charAt(i) == guess) {
                    rightMostPos = i;
                }
            }

            if(rightMostPos > pos) {
                pos = rightMostPos;
                rightMost.clear();
                rightMost.put(appearances.getKey(), appearances.getValue());
            }
            else if(rightMostPos == pos) {
                rightMost.put(appearances.getKey(), appearances.getValue());
            }
        }

        if(rightMost.size() == 1) {
            for(Map.Entry<String,Set<String>> right : rightMost.entrySet()) {
                pattern = right.getKey();
                partition.clear();
//                dictWords = right.getValue();
                dictWords.clear();
                return right.getValue();
            }
        }
        return multipleRightMost(rightMost, guess);
    }

    private Set<String> multipleRightMost(Map<String, Set<String>> rightMost, char guess) {
        //System.out.println("got to the multiple right func");
        Map<String, List<Integer>> multipleRight = new HashMap<>();
        for(Map.Entry<String,Set<String>> right : rightMost.entrySet()) {
            List<Integer> guessPos = new ArrayList<>();
            String wordPattern = right.getKey();
            for (int i = wordPattern.length() - 1 ; i > 0; i--) {
                if (wordPattern.charAt(i) == guess) {
                    guessPos.add(i);
                }
            }
            multipleRight.put(wordPattern, guessPos);
        }

//        System.out.println(multipleRight);

        List<String> winningPattern = new ArrayList<>();
        while(winningPattern.size() != 1) {
            int pos = 0;
            for(Map.Entry<String,List<Integer>> multiple : multipleRight.entrySet()) {
                List<Integer> guessPos = multiple.getValue();
                for(int i = 0; i < guessPos.size(); i++) {
                    if(guessPos.get(i) > pos) {
                        pos = guessPos.get(i);
                        guessPos.remove(i);
                        winningPattern.clear();
                        winningPattern.add(multiple.getKey());
                    }
                    else if(guessPos.get(i) == pos) {
                        guessPos.remove(i);
                        winningPattern.add(multiple.getKey());
                    }
                    else if (guessPos.get(i) < pos){
                        guessPos.remove(i);
                        winningPattern.remove(multiple.getKey());
                    }
                }
            }
        }

        String wordPattern = winningPattern.get(0);
        Set<String> patternWords = largestPatterns.get(wordPattern);
        System.out.println(largestPatterns);

        winningPattern.clear();
        largestPatterns.clear();
        pattern = wordPattern;
        partition.clear();
        dictWords.clear();
        return patternWords;
    }

    public String getPattern() {
        return pattern;
    }
}
