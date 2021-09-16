package hangman;

import com.sun.source.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame
{
    private final TreeSet<String> wordBank;
    private int guessesLeft;

    /**
     * Constructor with empty parameters, necessary for our passoff tests
     */
    public EvilHangmanGame()
    {
        wordBank = new TreeSet<>();
        guessesLeft = 0;
    }

    /**
     * Constructor for EvilHangmanGame() with parameters
     * @param guesses number of guesses available to player before they lose
     */
    public EvilHangmanGame(int guesses)
    {
        wordBank = new TreeSet<>();
        guessesLeft = guesses;
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

        if (wordLength < 2)
            throw new EmptyDictionaryException();

        // File Input
        try(Scanner scanner = new Scanner(dictionary))
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
        TreeSet<String> correctWordLengthBank = new TreeSet<>();

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
        return null;
    }

    /**
     * Returns the set of previously guessed letters, in alphabetical order.
     *
     * @return the previously guessed letters.
     */
    @Override
    public SortedSet<Character> getGuessedLetters()
    {
        return null;
    }
}
