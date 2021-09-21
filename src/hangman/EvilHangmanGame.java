package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame
{
    // Word Bank that holds all possible words that the program can pick from
    // Updates after every guess, getting smaller each time the player makes a guess
    private SortedSet<String> wordBank;
    // Set of letters that has already been guessed by the player
    private final SortedSet<Character> guessedLetters;
    // Amount of guesses remaining before the player loses
    private int guessesLeft;
    private String bestStringPattern;
    private String currentRevealedWord;

    /**
     * Constructor with empty parameters, necessary for our passoff tests
     */
    public EvilHangmanGame()
    {
        wordBank = new TreeSet<>();
        guessedLetters = new TreeSet<>();
        guessesLeft = 0;
        bestStringPattern = "";
        currentRevealedWord = "";
    }

    /**
     * Constructor for EvilHangmanGame() with parameters
     *
     * @param guesses number of guesses available to player before they lose
     */
    public EvilHangmanGame(int guesses, int wordLength)
    {
        wordBank = new TreeSet<>();
        guessedLetters = new TreeSet<>();
        guessesLeft = guesses;

        currentRevealedWord = "-".repeat(Math.max(0, wordLength));
    }

    /**
     * Starts a new game of evil hangman using words from <code>dictionary</code>
     * with length <code>wordLength</code>.
     * <p>
     * This method should set up everything required to play the game,
     * but should not actually play the game. (i.e., There should not be
     * a loop to prompt for input from the user.)
     *
     * @param dictionary Dictionary of words to use for the game
     * @param wordLength Number of characters in the word to guess
     * @throws IOException              if the dictionary does not exist or an error occurs when reading it.
     * @throws EmptyDictionaryException if the dictionary does not contain any words.
     */
    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException
    {
        // If dictionary is null or empty
        if (dictionary == null || dictionary.length() == 0)
            throw new EmptyDictionaryException();

        // Words must have at least two characters (single character words don't count)
        if (wordLength < 2)
            throw new EmptyDictionaryException();

        // File Input
        try (Scanner scanner = new Scanner(dictionary))
        {
            // While there's input to scan
            while (scanner.hasNext())
                // Add each word to our word bank in lower case format
                wordBank.add(scanner.next().toLowerCase());
        }

        // If our word bank is empty (file had a size (i.e., .length()) but had no words)
        if (wordBank.isEmpty())
            throw new EmptyDictionaryException();

        // Create a new TreeSet to hold all the words with the right wordLength
        SortedSet<String> correctWordLengthBank = new TreeSet<>();

        // For every word in our entire wordBank
        for (String word : wordBank)
        {
            // If it has the right amount of characters
            if (word.length() == wordLength)
                // Add that word to our usable word bank
                correctWordLengthBank.add(word);
        }

        // If our word bank doesn't contain a word with the requested amount of characters (i.e., it's empty)
        if (correctWordLengthBank.isEmpty())
            throw new EmptyDictionaryException();

        // Update our wordBank TreeSet to only hold the correct wordLength
        updateWordBank(correctWordLengthBank);
    }

    /**
     * This function updates our wordBank to only hold the words that have not been eliminated yet
     * These are words which the program can pick from and still be correct
     *
     * @param remainingWordsBank Set containing the words with the correct wordLength
     */
    private void updateWordBank(SortedSet<String> remainingWordsBank)
    {
        this.wordBank = remainingWordsBank;
    }

    /**
     * Make a guess in the current game.
     *
     * @param guess The character being guessed, case-insensitive
     * @return The set of strings that satisfy all the guesses made so far
     * in the game, including the guess made in this call. The game could claim
     * that any of these words had been the secret word for the whole game.
     * @throws GuessAlreadyMadeException if the character <code>guess</code>
     *                                   has already been guessed in this game.
     */
    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException
    {
        // Standardize all guesses to lowercase
        guess = Character.toLowerCase(guess);

        // Character has been guessed already
        for (Character character : guessedLetters)
        {
            if (character == guess)
                throw new GuessAlreadyMadeException();
        }

        // Character has not been guessed yet, add it to the Set
        guessedLetters.add(guess);

        // Partition wordBank relative to the guessed letter
        Map<String, SortedSet<String>> partition = new TreeMap<>();
        partitionWordBank(partition, guess);

        // Choose the largest subset in the partition to be the new wordBank
        bestStringPattern = getLargestSubsetKey(partition, guess);

        // Current revealed word pattern
        currentRevealedWord = makeCurrentRevealedWord(bestStringPattern);

        // If the subset key doesn't contain our guess, player loses a guess
        if (Boolean.FALSE.equals((contains(bestStringPattern, guess))))
            guessesLeft--;

        // Update our wordBank to just hold this new subset of words
        updateWordBank(partition.get(bestStringPattern));

        // Return the set of all possible words
        return wordBank;
    }

    /**
     * From our current wordBank, we will partition each word found
     * to build a partition of subsets of words that have the guessed
     * letter in the same position(s).
     *
     * @param partition a map that keeps track of the words in each subset in the partition
     * @param guess     character guessed by player
     */
    private void partitionWordBank(Map<String, SortedSet<String>> partition, char guess)
    {
        // For every word in our wordBank
        for (String word : wordBank)
        {
            // What is the letter pattern for our guess
            String pattern = getPatternString(guess, word);
            // If our partition already contains a set using this pattern
            if (partition.containsKey(pattern))
            {
                // Add this word to the correct pattern set
                partition.get(pattern).add(word);
            }
            // If we haven't seen this pattern before
            else
            {
                // Create a new set for the words with this pattern
                SortedSet<String> newPatternSet = new TreeSet<>();
                // Add our word option to this new set
                newPatternSet.add(word);
                // Add our newly found pattern to the partition
                partition.put(pattern, newPatternSet);
            }
        }
    }

    /**
     * This method will find the largest subset of potential words so that the
     * program has the maximum number of options when it picks the word at the
     * end of the game. If multiple subsets have the largest size, use the
     * tiebreakers in the project specification.
     *
     * @param partition Map containing all the possible subsets of words
     * @param guess     Player's guessed character
     * @return The key (pattern) of the largest subset in our map
     */
    private String getLargestSubsetKey(Map<String, SortedSet<String>> partition, char guess)
    {
        // Size of the largest subset (size = the amount of words)
        int maxSubsetSize = 0;
        // Key of the largest subset
        String largestSubsetKey = "";

        // For every key (pattern) in our map
        for (String pattern : partition.keySet())
        {
            // Do we need to update our size and key?
            boolean replaceSubsetKey = false;

            // If this pattern contains more strings than the current max
            if (partition.get(pattern).size() > maxSubsetSize)
            {
                // This pattern is our new largest subset key
                replaceSubsetKey = true;
            }
            // If this pattern contains the same amount of strings as the current max subset
            else if (partition.get(pattern).size() == maxSubsetSize)
            {
                // If this pattern has more missing characters
                if (count(pattern, '-') > count(largestSubsetKey, '-'))
                    // Make pattern our new key
                    replaceSubsetKey = true;
                    // If this pattern has less guessed characters
                else if (count(pattern, guess) < count(largestSubsetKey, guess))
                    // Make pattern our new key
                    replaceSubsetKey = true;
                    // If they have the same amount of correct characters and blanks
                else if (count(pattern, guess) == count(largestSubsetKey, guess))
                {
                    // If pattern's first occurrence of guess is further right than the current max
                    if (largestSubsetKey.indexOf(guess) < pattern.indexOf(guess))
                        // Make pattern our new key
                        replaceSubsetKey = true;
                }
            }
            // If we need to update our size and subset key
            if (replaceSubsetKey)
            {
                maxSubsetSize = partition.get(pattern).size();
                largestSubsetKey = pattern;
            }
        }
        return largestSubsetKey;
    }

    /**
     * Function that calculates the subset key for a word
     * with the respect to a guessed letter
     *
     * @param guess the guessed character
     * @param word  the word we want to build our pattern from
     * @return pattern string for the word based on its characters
     */
    private String getPatternString(char guess, String word)
    {
        // Using a StringBuilder to build our pattern
        StringBuilder patternBuilder = new StringBuilder();
        // For every character in our word
        for (int i = 0; i < word.length(); i++)
        {
            // If the character guessed is in our word
            if (guess == word.charAt(i))
                // Add it to our pattern in the correct position
                patternBuilder.insert(i, guess);
            else
                // If not found, add a '-' instead of the letter as a placeholder
                patternBuilder.insert(i, '-');
        }

        return patternBuilder.toString();
    }

    /**
     * Function to count the number of occurrences of
     * a character in a string
     *
     * @param word      word to inspect
     * @param character character to compare
     * @return number of occurrences of specified character in your word
     */
    private int count(String word, char character)
    {
        int count = 0;
        for (int i = 0; i < word.length(); i++)
        {
            if (word.charAt(i) == character)
                count++;
        }
        return count;
    }

    /**
     * Returns the set of previously guessed letters, in alphabetical order.
     *
     * @return the previously guessed letters.
     */
    @Override
    public SortedSet<Character> getGuessedLetters()
    {
        return guessedLetters;
    }

    public int getGuessesLeft()
    {
        return guessesLeft;
    }

    /**
     * How many times does a character appear in our subset key?
     *
     * @param guess Character to count
     * @return number of character occurrences in our subset pattern
     */
    public int getCharacterCount(char guess)
    {
        int count = 0;

        // For every character in our subset key
        for (int i = 0; i < this.bestStringPattern.length(); i++)
        {
            // Keep a count of how many times we see our guess
            if (this.bestStringPattern.charAt(i) == guess)
                count++;
        }

        return count;
    }

    /**
     * Function to determine if the player has won
     * @return true if current revealed word contains no '-', false otherwise
     */
    public Boolean hasWon()
    {
        return !(currentRevealedWord.contains("-"));
    }

    /**
     * Function to check if a certain char is found in a String
     * @param bestStringPattern String to check for our char
     * @param guess char to check
     * @return true if the character is found at least once, false otherwise
     */
    public Boolean contains(String bestStringPattern, char guess)
    {
        for (int i = 0; i < bestStringPattern.length(); i++)
        {
            if (guess == bestStringPattern.charAt(i))
                return true;
        }

        return false;
    }

    /**
     * @return arbitrary word (last word of available word bank) to pretend the computer "chose"
     */
    public String getValidWord()
    {
        return this.wordBank.last();
    }

    /**
     * Build our current revealed word pattern
     * @param bestStringPattern current subset key
     * @return current revealed word pattern
     */
    private String makeCurrentRevealedWord(String bestStringPattern)
    {
        StringBuilder stringBuilder = new StringBuilder(this.currentRevealedWord);

        for (int i = 0; i < stringBuilder.toString().length(); i++)
        {
            if (stringBuilder.charAt(i) == '-' && bestStringPattern.charAt(i) != '-')
            {
                stringBuilder.setCharAt(i, bestStringPattern.charAt(i));
            }
        }
        return stringBuilder.toString();
    }

    public String getCurrentRevealedWord()
    {
        return this.currentRevealedWord;
    }
}
