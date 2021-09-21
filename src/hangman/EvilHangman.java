package hangman;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class EvilHangman
{

    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            System.out.println("\nInvalid amount of command-line arguments");
            System.out.println("Usage: java [your main class name] dictionary wordLength guesses");
            System.exit(0);
        }
        else
        {
            File dictionary = new File(args[0]);
            int wordLength = Integer.parseInt(args[1]);
            int guesses = Integer.parseInt(args[2]);

            EvilHangmanGame game = new EvilHangmanGame(guesses, wordLength);

            try
            {
                game.startGame(dictionary, wordLength);
            }
            catch (EmptyDictionaryException | IOException e)
            {
                System.out.println("Empty Dictionary or some sort of I/O exception has occurred.");
                System.exit(0);
            }

            while (Boolean.FALSE.equals(game.hasWon()) && game.getGuessesLeft() > 0)
            {
                System.out.printf("%nYou have %d guesses left", game.getGuessesLeft());

                System.out.printf("%nUsed letters:");
                for (Character guessedLetter : game.getGuessedLetters())
                    System.out.print(" " + guessedLetter);
                System.out.print('\n');

                String currentRevealedWord = game.getCurrentRevealedWord();
                System.out.println("Word: " + currentRevealedWord);

                System.out.print("Enter guess: ");
                Scanner input = new Scanner(System.in);

                String stringGuess = input.next();

                if (stringGuess.length() != 1)
                {
                    System.out.println("Invalid input! You may only guess a single character!");
                    continue;
                }

                char guess = stringGuess.trim().charAt(0);

                if (!(Character.isLetter(guess)))
                {
                    System.out.println("That is not a valid character! Only characters between A-Z are acceptable.");
                    continue;
                }

                try
                {
                    game.makeGuess(guess);
                }
                catch (GuessAlreadyMadeException e)
                {
                    System.out.println("You already guessed that character!");
                    continue;
                }

                String newRevealedWord = game.getCurrentRevealedWord();

                if (newRevealedWord.equals(currentRevealedWord))
                {
                    guess = Character.toLowerCase(guess);
                    System.out.println("Sorry, there are no " + guess + "'s");
                    guesses--;
                }
                else
                {
                    System.out.println("Yes, there are " + game.getCharacterCount(guess) + " " + guess + "'s");
                }
            }

            if (Boolean.TRUE.equals(game.hasWon()))
                System.out.printf("You win! You guessed the word: %s%n", game.getCurrentRevealedWord());
            else
            {
                System.out.printf("%nYou lose!");
                System.out.printf("%nThe word was %s", game.getValidWord());
            }
        }
    }
}
