import java.util.*;
import java.io.*;

public class NewMemoryGame {
    private static final char HIDDEN_SYMBOL = '*';
    private static final int BOARD_SIZE = 4;
    private static char[][] actualBoard = new char[BOARD_SIZE][BOARD_SIZE];
    private static char[][] hiddenBoard = new char[BOARD_SIZE][BOARD_SIZE];
    private static boolean[][] revealedCards = new boolean[BOARD_SIZE][BOARD_SIZE];
    private static int firstRow, firstCol, secondRow, secondCol;
    private static List<Character> cards = Arrays.asList(
            'A', 'A', 'B', 'B', 'C', 'C', 'D', 'D',
            'E', 'E', 'F', 'F', 'G', 'G', 'H', 'H');
    private static final String USERS_FILE = "users.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, User> userMap = loadUserData();
        User currentUser = null;

        while (currentUser == null) {
            System.out.println("Do you want to (1) Sign up or (2) Sign in?");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                if (choice == 1) {
                    currentUser = signUp(scanner, userMap);
                } else if (choice == 2) {
                    currentUser = signIn(scanner, userMap);
                } else {
                    System.out.println("Invalid choice. Please choose 1 or 2.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (1 or 2).");
                scanner.next(); // clear the invalid input
            }
        }

        initializeBoard();

        System.out.println("Hey! Do you want to play the memory game? (Y/N)");
        String playResponse = scanner.next();
        if (playResponse.equalsIgnoreCase("Y")) {
            System.out.println("Let's start!!");
            boolean keepPlaying = true;

            while (keepPlaying) {
                getFirstCard(scanner);
                getSecondCard(scanner);
                displayBoard();

                if (firstRow == secondRow && firstCol == secondCol) {
                    System.out.println("It's a match but both cards you entered were the same! So please...Enter different coordinates for the second card!");
                    currentUser.gamesPlayed++;
                } else if (actualBoard[firstRow][firstCol] != actualBoard[secondRow][secondCol]) {
                    System.out.println("Not a match. Try again.");
                    currentUser.gamesPlayed++;
                    currentUser.gamesLost++;
                    hideCard(firstRow, firstCol);
                    hideCard(secondRow, secondCol);
                } else {
                    System.out.println("It's a match!");
                    currentUser.gamesPlayed++;
                    currentUser.gamesWon++;
                }

                System.out.println("Score Board: ");
                System.out.println("Games Played: " + currentUser.gamesPlayed);
                System.out.println("Games Won: " + currentUser.gamesWon);
                System.out.println("Games Lost: " + currentUser.gamesLost);
                System.out.println("Do you want to play again? (Y/N)");
                String playAgainResponse = scanner.next();
                if (!playAgainResponse.equalsIgnoreCase("Y")) {
                    keepPlaying = false;
                }
            }
        }
        System.out.println("Come again if you change your mind");

        saveUserData(userMap);
    }

    private static void getFirstCard(Scanner scanner) {
        displayBoard();
        try {
            System.out.println("Enter the coordinates of the first card to flip (row and column): ");
            firstRow = scanner.nextInt() - 1;
            firstCol = scanner.nextInt() - 1;
            revealCard(firstRow, firstCol);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter numbers for the row and column.");
            scanner.next(); // clear the invalid input
            getFirstCard(scanner);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please enter a number between 1-4!");
            getFirstCard(scanner);
        }
    }

    private static void getSecondCard(Scanner scanner) {
        displayBoard();
        try {
            System.out.println("Enter the coordinates of the second card to flip (row and column): ");
            secondRow = scanner.nextInt() - 1;
            secondCol = scanner.nextInt() - 1;
            revealCard(secondRow, secondCol);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter numbers for the row and column.");
            scanner.next(); // clear the invalid input
            getSecondCard(scanner);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please enter a number between 1-4!");
            getSecondCard(scanner);
        }
    }

    private static void initializeBoard() {
        Collections.shuffle(cards);
        Iterator<Character> it = cards.iterator();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                actualBoard[i][j] = it.next();
                hiddenBoard[i][j] = HIDDEN_SYMBOL;
            }
        }
    }

    private static void displayBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (revealedCards[i][j]) {
                    System.out.print(actualBoard[i][j] + " ");
                } else {
                    System.out.print(hiddenBoard[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    private static void revealCard(int row, int col) {
        revealedCards[row][col] = true;
    }

    private static void hideCard(int row, int col) {
        revealedCards[row][col] = false;
    }

    private static User signUp(Scanner scanner, Map<String, User> userMap) {
        System.out.println("Enter a username: ");
        String username = scanner.nextLine();
        if (userMap.containsKey(username)) {
            System.out.println("Username already exists. Try a different one.");
            return null;
        }
        System.out.println("Enter a password: ");
        String password = scanner.nextLine();
        User newUser = new User(username, password);
        userMap.put(username, newUser);
        System.out.println("Sign up successful. You can now sign in.");
        return null;
    }

    private static User signIn(Scanner scanner, Map<String, User> userMap) {
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        if (!userMap.containsKey(username)) {
            System.out.println("Username not found. Please sign up first.");
            return null;
        }
        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        User user = userMap.get(username);
        if (!user.password.equals(password)) {
            System.out.println("Incorrect password. Try again.");
            return null;
        }
        System.out.println("Sign in successful. Welcome back, " + username + "!");
        return user;
    }

    private static Map<String, User> loadUserData() {
        Map<String, User> userMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String password = parts[1];
                int gamesPlayed = Integer.parseInt(parts[2]);
                int gamesWon = Integer.parseInt(parts[3]);
                int gamesLost = Integer.parseInt(parts[4]);
                User user = new User(username, password, gamesPlayed, gamesWon, gamesLost);
                userMap.put(username, user);
            }
        } catch (IOException e) {
            System.out.println("Error loading user data.");
        }
        return userMap;
    }

    private static void saveUserData(Map<String, User> userMap) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : userMap.values()) {
                writer.write(user.username + "," + user.password + "," + user.gamesPlayed + "," + user.gamesWon + "," + user.gamesLost);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving user data.");
        }
    }

    private static class User {
        String username;
        String password;
        int gamesPlayed;
        int gamesWon;
        int gamesLost;

        User(String username, String password) {
            this.username = username;
            this.password = password;
            this.gamesPlayed = 0;
            this.gamesWon = 0;
            this.gamesLost = 0;
        }

        User(String username, String password, int gamesPlayed, int gamesWon, int gamesLost) {
            this.username = username;
            this.password = password;
            this.gamesPlayed = gamesPlayed;
            this.gamesWon = gamesWon;
            this.gamesLost = gamesLost;
        }
    }
}
