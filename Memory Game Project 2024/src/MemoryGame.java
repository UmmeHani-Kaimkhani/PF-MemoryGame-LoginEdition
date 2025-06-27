import java.util.*;
import java.io.*;
public class MemoryGame {
    private static final char HIDDEN = '*';
    private static final int SIZE = 4;
    private static char[][] board = new char[SIZE][SIZE];
    private static char[][] hiddenBoard = new char[SIZE][SIZE];
    private static boolean[][] revealed = new boolean[SIZE][SIZE];
    private static int row1, col1, row2, col2;
    private static List<Character> cards = Arrays.asList(
            'A', 'A', 'B', 'B', 'C', 'C', 'D', 'D',
            'E', 'E', 'F', 'F', 'G', 'G', 'H', 'H');
    private static final String USERS_FILE = "users.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, UserData> users = loadUserData();
        UserData currentUser = null;

        while (currentUser == null) {
            System.out.println("Do you want to (1) Sign up or (2) Sign in?");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (choice == 1) {
                currentUser = signUp(scanner, users);
            } else if (choice == 2) {
                currentUser = signIn(scanner, users);
            } else {
                System.out.println("Invalid choice. Please choose 1 or 2.");
            }
        }

        initializeBoard(hiddenBoard, SIZE, board);

        System.out.println("Hey! Do you want to play the memory game? (Y/N)");
        String YorN = scanner.next();
        if (YorN.equalsIgnoreCase("Y")) {
            System.out.println("Let's start!!");
            boolean wannaPlay = true;

            while (wannaPlay) {
                cardOne(scanner);
                cardTwo(scanner);
                printBoard(SIZE, board, hiddenBoard, revealed);

                if (row1 == row2 && col1 == col2) {
                    System.out.println("It's a match but both cards you entered were the same! So please...Enter different coordinates for the second card!");
                    currentUser.gamesPlayed++;
                } else if (board[row1][col1] != board[row2][col2]) {
                    System.out.println("Not a match. Try again.");
                    currentUser.gamesPlayed++;
                    currentUser.gamesLost++;
                    hideCard(revealed, row1, col1);
                    hideCard(revealed, row2, col2);
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
                String YORN = scanner.next();
                if (!YORN.equalsIgnoreCase("Y")) {
                    wannaPlay = false;
                }
            }
        }
        System.out.println("Come again in case you change your mind");

        saveUserData(users);
    }

    private static void cardOne(Scanner scanner) {
        printBoard(SIZE, board, hiddenBoard, revealed);
        try {
            System.out.println("Enter the coordinates of the first card to flip (row and column): ");
            row1 = scanner.nextInt() - 1;
            col1 = scanner.nextInt() - 1;
            revealCard(revealed, row1, col1);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please enter a number between 1-4!");
            cardOne(scanner);
        }
    }

    private static void cardTwo(Scanner scanner) {
        printBoard(SIZE, board, hiddenBoard, revealed);
        try {
            System.out.println("Enter the coordinates of the second card to flip (row and column): ");
            row2 = scanner.nextInt() - 1;
            col2 = scanner.nextInt() - 1;
            revealCard(revealed, row2, col2);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please enter a number between 1-4!");
            cardTwo(scanner);
        }
    }

    private static void initializeBoard(char[][] hiddenBoard, int SIZE, char[][] board) {
        Collections.shuffle(cards);
        Iterator<Character> it = cards.iterator();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = it.next();
                hiddenBoard[i][j] = HIDDEN;
            }
        }
    }

    private static void printBoard(int SIZE, char[][] board, char[][] hiddenBoard, boolean[][] revealed) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (revealed[i][j]) {
                    System.out.print(board[i][j] + " ");
                } else {
                    System.out.print(hiddenBoard[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    private static void revealCard(boolean[][] revealed, int row, int col) {
        revealed[row][col] = true;
    }

    private static void hideCard(boolean[][] revealed, int row, int col) {
        revealed[row][col] = false;
    }

    private static UserData signUp(Scanner scanner, Map<String, UserData> users) {
        System.out.println("Enter a username: ");
        String username = scanner.nextLine();
        if (users.containsKey(username)) {
            System.out.println("Username already exists. Try a different one.");
            return null;
        }
        System.out.println("Enter a password: ");
        String password = readPassword(scanner);
        UserData newUser = new UserData(username, password);
        users.put(username, newUser);
        System.out.println("Sign up successful. You can now sign in.");
        return null;
    }

    private static UserData signIn(Scanner scanner, Map<String, UserData> users) {
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        if (!users.containsKey(username)) {
            System.out.println("Username not found. Please sign up first.");
            return null;
        }
        System.out.println("Enter your password: ");
        String password = readPassword(scanner);
        UserData user = users.get(username);
        if (!user.password.equals(password)) {
            System.out.println("Incorrect password. Try again.");
            return null;
        }
        System.out.println("Sign in successful. Welcome back, " + username + "!");
        return user;
    }

    private static String readPassword(Scanner scanner) {
        Console console = System.console();
        if (console != null) {
            return new String(console.readPassword());
        } else {
            return scanner.nextLine();
        }
    }

    private static Map<String, UserData> loadUserData() {
        Map<String, UserData> users = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String password = parts[1];
                int gamesPlayed = Integer.parseInt(parts[2]);
                int gamesWon = Integer.parseInt(parts[3]);
                int gamesLost = Integer.parseInt(parts[4]);
                UserData user = new UserData(username, password, gamesPlayed, gamesWon, gamesLost);
                users.put(username, user);
            }
        } catch (IOException e) {
            System.out.println("Error loading user data.");
        }
        return users;
    }

    private static void saveUserData(Map<String, UserData> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (UserData user : users.values()) {
                writer.write(user.username + "," + user.password + "," + user.gamesPlayed + "," + user.gamesWon + "," + user.gamesLost);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving user data.");
        }
    }

    private static class UserData {
        String username;
        String password;
        int gamesPlayed;
        int gamesWon;
        int gamesLost;

        UserData(String username, String password) {
            this.username = username;
            this.password = password;
            this.gamesPlayed = 0;
            this.gamesWon = 0;
            this.gamesLost = 0;
        }

        UserData(String username, String password, int gamesPlayed, int gamesWon, int gamesLost) {
            this.username = username;
            this.password = password;
            this.gamesPlayed = gamesPlayed;
            this.gamesWon = gamesWon;
            this.gamesLost = gamesLost;
        }
    }
}
