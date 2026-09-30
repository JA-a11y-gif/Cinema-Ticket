import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {

    // ================= DATA =================

    static Scanner input = new Scanner(System.in);
    static ArrayList<Booking> bookings = new ArrayList<Booking>();

    static final double PRICE_VIP = 500.00;
    static final double PRICE_PREMIUM = 400.00;
    static final double PRICE_REGULAR = 300.00;
    static final double SERVICE = 20.00;

    static final int W = 50; // width inside the boxes

    static String[] movies = {"Avengers: Endgame", "Spider-Man: No Way Home", "Inside Out 2"};
    static String[] dates = {"October 5, 2026", "October 6, 2026", "October 7, 2026"};
    static String[] times = {"6:00 PM", "7:00 PM", "5:30 PM"};
    static String[] rooms = {"Cinema 1", "Cinema 2", "Cinema 3"};

    static String[] typeNames = {"Regular", "Student", "Senior", "Child"};
    static double[] typeDiscounts = {0.00, 0.10, 0.20, 0.15};

    // [movie][row][seat]  ->  3 movies, 5 rows (A-E), 8 seats
    static boolean[][][] seats = new boolean[3][5][8];

    static class Booking {
        String reference;
        int movie;
        ArrayList<String> seat = new ArrayList<String>();
        ArrayList<Integer> type = new ArrayList<Integer>();

        Booking(String reference, int movie) {
            this.reference = reference;
            this.movie = movie;
        }
    }

    // ================= MAIN MENU =================

    public static void main(String[] args) {

        // Sample booked seats
        seats[0][0][0] = true; // Avengers A1
        seats[1][1][3] = true; // Spider-Man B4
        seats[2][2][6] = true; // Inside Out C7

        int choice;

        do {
            title("CINEMA TICKET SYSTEM");
            row("1. Browse Movies");
            row("2. Book Movie Ticket");
            row("3. View Seat Layout");
            row("4. Cancel / Change Booking");
            row("5. View My Bookings");
            row("6. Exit");
            line();

            choice = getInt("Choose: ");

            if (choice == 1) {
                browseMovies();
            } else if (choice == 2) {
                book();
            } else if (choice == 3) {
                int movie = movieChoice();
                if (movie >= 0) {
                    showSeats(movie);
                }
            } else if (choice == 4) {
                manageBooking();
            } else if (choice == 5) {
                viewBookings();
            } else if (choice == 6) {
                message("Thank you!");
            } else {
                message("Invalid choice.");
            }

        } while (choice != 6);
    }

    // ================= BOX OUTPUT =================

    static void line() {
        System.out.println("+" + repeat('-', W + 2) + "+");
    }

    static void row(String text) {
        if (text.length() > W) {
            text = text.substring(0, W);
        }
        System.out.println("| " + String.format("%-" + W + "s", text) + " |");
    }

    static void title(String text) {
        System.out.println();
        line();
        row(repeat(' ', (W - text.length()) / 2) + text);
        line();
    }

    static void message(String text) {
        line();
        row(text);
        line();
    }

    static String repeat(char c, int n) {
        String s = "";
        for (int i = 0; i < n; i++) {
            s += c;
        }
        return s;
    }

    // ================= MOVIES =================

    static void browseMovies() {
        title("AVAILABLE MOVIES");

        for (int i = 0; i < movies.length; i++) {
            row((i + 1) + ". " + movies[i]);
            row("   Date: " + dates[i] + "   Time: " + times[i]);
            row("   Room: " + rooms[i]);
            line();
        }
    }

    static int movieChoice() {
        browseMovies();

        int choice = getInt("Select movie: ");

        if (choice < 1 || choice > movies.length) {
            message("Invalid movie.");
            return -1;
        }

        return choice - 1;
    }

    // ================= SEATS =================

    // Row A = VIP, Rows B-C = Premium, Rows D-E = Regular
    static String getCategory(int row) {
        if (row == 0) {
            return "VIP";
        }
        if (row <= 2) {
            return "Premium";
        }
        return "Regular";
    }

    static double getSeatPrice(int row) {
        if (row == 0) {
            return PRICE_VIP;
        }
        if (row <= 2) {
            return PRICE_PREMIUM;
        }
        return PRICE_REGULAR;
    }

    static double getTicketPrice(String seat, int type) {
        double base = getSeatPrice(position(seat)[0]);
        return base - (base * typeDiscounts[type]) + SERVICE;
    }

    // "B4" -> {1, 3}. Returns null if invalid.
    static int[] position(String seat) {
        if (seat == null || seat.length() < 2) {
            return null;
        }

        int row = Character.toUpperCase(seat.charAt(0)) - 'A';

        try {
            int col = Integer.parseInt(seat.substring(1)) - 1;

            if (row >= 0 && row < 5 && col >= 0 && col < 8) {
                return new int[] {row, col};
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }

    static int freeSeats(int movie) {
        int count = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                if (!seats[movie][r][c]) {
                    count++;
                }
            }
        }
        return count;
    }

    static void showSeats(int movie) {
        title("SEAT LAYOUT - " + movies[movie]);

        row("               1   2   3   4   5   6   7   8");

        for (int r = 0; r < 5; r++) {
            String text = String.format("%-14s ", (char) ('A' + r) + " (" + getCategory(r) + ")");

            for (int c = 0; c < 8; c++) {
                text += seats[movie][r][c] ? "[X] " : "[O] ";
            }

            row(text);
        }

        row("");
        row("[O] Available   [X] Booked");
        line();
        row(String.format("VIP     (Row A)    : P%.2f", PRICE_VIP));
        row(String.format("Premium (Rows B-C) : P%.2f", PRICE_PREMIUM));
        row(String.format("Regular (Rows D-E) : P%.2f", PRICE_REGULAR));
        line();
    }

    // Keeps asking until the seat is valid, free, and not already in the booking
    static String askSeat(int movie, Booking b, String prompt) {
        while (true) {
            String seat = getString(prompt).toUpperCase();
            int[] p = position(seat);

            if (p == null) {
                message("Invalid seat. Use A1-E8.");
            } else if (seats[movie][p[0]][p[1]]) {
                message("Seat already booked.");
            } else if (b.seat.contains(seat)) {
                message("Seat already in this booking.");
            } else {
                return seat;
            }
        }
    }

    static void freeSeat(int movie, String seat) {
        int[] p = position(seat);
        seats[movie][p[0]][p[1]] = false;
    }

    // ================= TICKET TYPE =================

    static int askTicketType(String seat) {
        title("TICKET TYPE FOR " + seat);

        for (int i = 0; i < typeNames.length; i++) {
            row(String.format("%d. %-8s - %d%% discount",
                    i + 1, typeNames[i], Math.round(typeDiscounts[i] * 100)));
        }
        line();

        while (true) {
            int choice = getInt("Choose: ");

            if (choice >= 1 && choice <= typeNames.length) {
                return choice - 1;
            }

            message("Invalid choice.");
        }
    }

    // ================= BOOK =================

    static void book() {
        int movie = movieChoice();

        if (movie == -1) {
            return;
        }

        showSeats(movie);

        int free = freeSeats(movie);
        int count = getInt("Number of tickets: ");

        if (count <= 0) {
            message("Invalid number.");
            return;
        }
        if (count > free) {
            message("Only " + free + " seat(s) available.");
            return;
        }

        Booking b = new Booking(makeReference(), movie);

        for (int i = 0; i < count; i++) {
            String seat = askSeat(movie, b, "Seat #" + (i + 1) + ": ");
            int[] p = position(seat);

            seats[movie][p[0]][p[1]] = true;
            b.seat.add(seat);
            b.type.add(askTicketType(seat));
        }

        double total = calculateTotal(b);

        System.out.println();
        message(String.format("TOTAL: P%.2f", total));

        double payment;

        do {
            payment = getDouble("Payment: P");

            if (payment < total) {
                message("Insufficient payment.");
            }
        } while (payment < total);

        bookings.add(b);

        message("Booking successful!");
        receipt(b, payment, payment - total);
    }

    // ================= CANCEL / CHANGE =================

    static void manageBooking() {
        if (bookings.isEmpty()) {
            message("No bookings.");
            return;
        }

        title("CANCEL / CHANGE BOOKING");
        row("1. Cancel Entire Booking");
        row("2. Cancel Individual Seat");
        row("3. Change Seat / Ticket Type");
        row("4. Back");
        line();

        int choice = getInt("Choose: ");

        if (choice == 4) {
            return;
        }
        if (choice < 1 || choice > 3) {
            message("Invalid choice.");
            return;
        }

        Booking b = findBooking(getString("Booking reference: ").toUpperCase());

        if (b == null) {
            message("Booking not found.");
        } else if (choice == 1) {
            cancelAll(b);
        } else if (choice == 2) {
            cancelOne(b);
        } else {
            changeTicket(b);
        }
    }

    static void cancelAll(Booking b) {
        for (String s : b.seat) {
            freeSeat(b.movie, s);
        }

        bookings.remove(b);
        message("Entire booking cancelled.");
    }

    static void cancelOne(Booking b) {
        title("YOUR TICKETS");
        listTickets(b);
        line();

        int choice = getInt("Select seat to cancel: ") - 1;

        if (choice < 0 || choice >= b.seat.size()) {
            message("Invalid selection.");
            return;
        }

        String removed = b.seat.remove(choice);
        b.type.remove(choice);
        freeSeat(b.movie, removed);

        if (b.seat.isEmpty()) {
            bookings.remove(b);
            message("Booking cancelled.");
        } else {
            message(removed + " cancelled. New total: P" + String.format("%.2f", calculateTotal(b)));
        }
    }

    static void changeTicket(Booking b) {
        title("YOUR TICKETS");
        listTickets(b);
        line();

        int choice = getInt("Select ticket to change: ") - 1;

        if (choice < 0 || choice >= b.seat.size()) {
            message("Invalid selection.");
            return;
        }

        String oldSeat = b.seat.get(choice);

        showSeats(b.movie);

        String newSeat = askSeat(b.movie, b, "Enter new seat: ");
        int[] p = position(newSeat);

        freeSeat(b.movie, oldSeat);
        seats[b.movie][p[0]][p[1]] = true;

        b.seat.set(choice, newSeat);
        b.type.set(choice, askTicketType(newSeat));

        title("TICKET CHANGED");
        row("Old seat : " + oldSeat);
        row("New seat : " + newSeat);
        row(String.format("New total: P%.2f", calculateTotal(b)));
        line();
    }

    // ================= VIEW / RECEIPT =================

    static void viewBookings() {
        if (bookings.isEmpty()) {
            message("No bookings.");
            return;
        }

        for (Booking b : bookings) {
            title("MY BOOKING");
            details(b);
            row(String.format("Total: P%.2f", calculateTotal(b)));
            line();
        }
    }

    static void receipt(Booking b, double payment, double change) {
        title("CINEMA TICKET");
        details(b);
        row(String.format("TOTAL  : P%.2f", calculateTotal(b)));
        row(String.format("PAYMENT: P%.2f", payment));
        row(String.format("CHANGE : P%.2f", change));
        line();
    }

    // Booking info + tickets (used by both view and receipt)
    static void details(Booking b) {
        row("Reference: " + b.reference);
        row("Movie    : " + movies[b.movie]);
        row("Date     : " + dates[b.movie]);
        row("Time     : " + times[b.movie]);
        row("Room     : " + rooms[b.movie]);
        line();
        listTickets(b);
        line();
    }

    static void listTickets(Booking b) {
        for (int i = 0; i < b.seat.size(); i++) {
            String seat = b.seat.get(i);
            int type = b.type.get(i);

            row(String.format("%d. %s (%s) - %s - P%.2f",
                    i + 1, seat, getCategory(position(seat)[0]),
                    typeNames[type], getTicketPrice(seat, type)));
        }
    }

    // ================= HELPERS =================

    static double calculateTotal(Booking b) {
        double total = 0;

        for (int i = 0; i < b.seat.size(); i++) {
            total += getTicketPrice(b.seat.get(i), b.type.get(i));
        }

        return total;
    }

    static Booking findBooking(String ref) {
        for (Booking b : bookings) {
            if (b.reference.equalsIgnoreCase(ref)) {
                return b;
            }
        }
        return null;
    }

    static String makeReference() {
        return "CIN" + (10000 + new Random().nextInt(90000));
    }

    // ================= INPUT =================

    static int getInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                message("Please enter a valid number.");
            }
        }
    }

    static double getDouble(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Double.parseDouble(input.nextLine().trim());
            } catch (NumberFormatException e) {
                message("Please enter a valid amount.");
            }
        }
    }

    static String getString(String message) {
        System.out.print(message);
        return input.nextLine().trim();
    }
}
