import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final int WIDTH = 10;
    private static final int HEIGHT = 8;
    private static final ArrayList<int[]> snake = new ArrayList<>();
    private static volatile char currentDirection = 'D'; 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        boolean playAgain = true;

        while (playAgain) {
            System.out.println("=========================================");
            System.out.println("          TERMINAL SNAKE GAME            ");
            System.out.println("=========================================");

            int totalFoods = 0;
            boolean validOption = false;

            while (!validOption) {
                System.out.println("\nSelect Food Setup Option:");
                System.out.println("1. Random Power Foods (1 - 10)");
                System.out.println("2. Enter the number of Power Foods to collect");
                System.out.print("Choose option (1 or 2): ");

                if (scanner.hasNextInt()) {
                    int option = scanner.nextInt();
                    if (option == 1) {
                        totalFoods = random.nextInt(10) + 1;
                        System.out.println("Randomly generated " + totalFoods + " Power Food(s) to collect.");
                        validOption = true;
                    } else if (option == 2) {
                        System.out.print("Enter the number of Power Foods to collect: ");
                        if (scanner.hasNextInt()) {
                            totalFoods = scanner.nextInt();
                            if (totalFoods > 0) {
                                validOption = true;
                            } else {
                                System.out.println("Please enter a positive number greater than 0.");
                            }
                        } else {
                            System.out.println("Invalid input! Please enter a valid number.");
                            scanner.next();
                        }
                    } else {
                        System.out.println("Invalid option! Please enter 1 or 2.");
                    }
                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.next();
                }
            }

            snake.clear();
            snake.add(new int[]{3, 3}); 
            currentDirection = 'D'; 

            int score = 0;
            int foodsCollected = 0;
            int moves = 0;
            
            // AtomicBoolean enables safe use inside lambda thread
            AtomicBoolean isRunning = new AtomicBoolean(true);

            int foodX = random.nextInt(WIDTH - 2) + 1;
            int foodY = random.nextInt(HEIGHT - 2) + 1;

            // Background input thread
            Thread inputThread = new Thread(() -> {
                while (isRunning.get()) {
                    try {
                        if (System.in.available() > 0) {
                            char input = (char) System.in.read();
                            input = Character.toUpperCase(input);
                            if (input == 'W' || input == 'S' || input == 'A' || input == 'D' || input == 'Q') {
                                if ((input == 'W' && currentDirection != 'S') ||
                                    (input == 'S' && currentDirection != 'W') ||
                                    (input == 'A' && currentDirection != 'D') ||
                                    (input == 'D' && currentDirection != 'A') ||
                                    input == 'Q') {
                                    currentDirection = input;
                                }
                            }
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
            });
            inputThread.start();

            while (isRunning.get()) {
                System.out.println("\n-----------------------------------------");
                System.out.println("Score           : " + score);
                System.out.println("Foods Collected : " + foodsCollected + "/" + totalFoods);
                System.out.println("Foods Remaining : " + (totalFoods - foodsCollected));
                System.out.println("Snake Length    : " + snake.size());
                System.out.println("Moves           : " + moves);
                System.out.println("-----------------------------------------");

                for (int y = 0; y < HEIGHT; y++) {
                    StringBuilder row = new StringBuilder();
                    for (int x = 0; x < WIDTH; x++) {
                        boolean isSnakeBody = false;

                        for (int i = 1; i < snake.size(); i++) {
                            if (snake.get(i)[0] == x && snake.get(i)[1] == y) {
                                isSnakeBody = true;
                                break;
                            }
                        }

                        if ((x == 0 || x == WIDTH - 1) && (y == 0 || y == HEIGHT - 1)) {
                            row.append("+  ");
                        }
                        else if (y == 0 || y == HEIGHT - 1) {
                            row.append("-  ");
                        }
                        else if (x == 0 || x == WIDTH - 1) {
                            row.append("|  ");
                        }
                        else if (snake.get(0)[0] == x && snake.get(0)[1] == y) {
                            row.append("🐸 ");
                        }
                        else if (isSnakeBody) {
                            row.append("🟢 ");
                        } 
                        else if (x == foodX && y == foodY) {
                            row.append("🍎 ");
                        }
                        else {
                            row.append(".  ");
                        }
                    }
                    System.out.println(row.toString());
                }

                System.out.println("\nControls : W (Up), A (Left), S (Down), D (Right), Q (Exit)");
                System.out.println("Current Direction: " + currentDirection);

                if (currentDirection == 'Q') {
                    System.out.println("\n👋 You exited the game. See you next time!");
                    isRunning.set(false);
                    playAgain = false;
                    break;
                }

                int newHeadX = snake.get(0)[0];
                int newHeadY = snake.get(0)[1];

                switch (currentDirection) {
                    case 'W' -> newHeadY--;
                    case 'S' -> newHeadY++;
                    case 'D' -> newHeadX++; 
                    case 'A' -> newHeadX--;
                }

                moves++;

                // Wall Wrap-around
                if (newHeadX <= 0) {
                    newHeadX = WIDTH - 2;
                } else if (newHeadX >= WIDTH - 1) {
                    newHeadX = 1;
                }

                if (newHeadY <= 0) {
                    newHeadY = HEIGHT - 2;
                } else if (newHeadY >= HEIGHT - 1) {
                    newHeadY = 1;
                }

                // Self Collision Checks
                boolean selfCollide = false;
                for (int[] part : snake) {
                    if (part[0] == newHeadX && part[1] == newHeadY) {
                        selfCollide = true;
                        break;
                    }
                }
                if (selfCollide) {
                    System.out.println("\nGAME OVER! You collided with yourself!");
                    isRunning.set(false);
                    break;
                }

                snake.add(0, new int[]{newHeadX, newHeadY});

                // Food Collection logic
                if (newHeadX == foodX && newHeadY == foodY) {
                    score++;
                    foodsCollected++;
                    System.out.println("\n>>> Power Food Collected! <<<");

                    if (foodsCollected == totalFoods) {
                        System.out.println("\n=========================================");
                        System.out.println("              YOU WIN!                   ");
                        System.out.println("=========================================");
                        System.out.println("All Power Foods collected! Game completed!");
                        System.out.println("Final Score  : " + score);
                        System.out.println("Snake Length : " + snake.size());
                        System.out.println("Total Moves  : " + moves);
                        isRunning.set(false);
                        break;
                    } else {
                        foodX = random.nextInt(WIDTH - 2) + 1;
                        foodY = random.nextInt(HEIGHT - 2) + 1;
                    }
                } else {
                    snake.remove(snake.size() - 1);
                }

                try {
                    Thread.sleep(500); // Frame refresh speed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (playAgain) {
                System.out.print("\nPlay again? (Y/N): ");
                char retry = scanner.next().toUpperCase().charAt(0);
                playAgain = (retry == 'Y');
            }
        }

        System.out.println("Goodbye!");
        scanner.close();
    }
}