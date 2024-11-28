package org.example;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MainApp {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/hotel";  // Ваша база даних
    private static final String DB_USER = "postgres";  // Ваш користувач PostgreSQL
    private static final String DB_PASSWORD = "1234";  // Ваш пароль PostgreSQL

    private static int loginAttempts = 0;

    public static void main(String[] args) {
        try {
            ensureDatabaseExists();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
        showWelcomeScreen();
    }

    private static void ensureDatabaseExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn != null) {
                System.out.println("Database connection established. Path: " + DB_URL);
                createDatabase();
            }
        }
    }

    private static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Database connection established. Path: " + DB_URL);

            // Create tables if they don't exist (Customers, Rooms, Bookings)
            String createCustomersTable = """
            CREATE TABLE IF NOT EXISTS customers (
                id SERIAL PRIMARY KEY,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            );
        """;
            conn.createStatement().execute(createCustomersTable);

            String createRoomsTable = """
            CREATE TABLE IF NOT EXISTS rooms (
                id SERIAL PRIMARY KEY,
                room_type TEXT NOT NULL UNIQUE,
                capacity INTEGER NOT NULL,
                price_per_night REAL NOT NULL,
                availability TEXT NOT NULL DEFAULT 'available'
            );
        """;
            conn.createStatement().execute(createRoomsTable);

            String createBookingsTable = """
            CREATE TABLE IF NOT EXISTS bookings (
                id SERIAL PRIMARY KEY,
                customer_id INTEGER NOT NULL,
                room_id INTEGER NOT NULL,
                check_in_date TEXT NOT NULL,
                check_out_date TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'pending',
                breakfast BOOLEAN NOT NULL DEFAULT FALSE,
                transfer BOOLEAN NOT NULL DEFAULT FALSE,
                FOREIGN KEY (customer_id) REFERENCES customers(id),
                FOREIGN KEY (room_id) REFERENCES rooms(id)
            );
        """;
            conn.createStatement().execute(createBookingsTable);

            // Insert initial users with hashed passwords
            String passwordHashAdmin = EncryptionUtil.hashPassword("admin123"); // Hash the admin password
            String passwordHashUser = EncryptionUtil.hashPassword("user123"); // Hash the user password
            String insertUsers = """
            INSERT INTO customers (username, password, role)
            VALUES 
            ('admin', ?, 'admin'),
            ('user1', ?, 'user')
            ON CONFLICT (username) DO NOTHING;
        """;
            PreparedStatement stmt = conn.prepareStatement(insertUsers);
            stmt.setString(1, passwordHashAdmin);
            stmt.setString(2, passwordHashUser);
            stmt.executeUpdate();

            // Insert initial rooms
            String insertRooms = """
            INSERT INTO rooms (room_type, capacity, price_per_night)
            VALUES 
            ('Single', 1, 50.0),
            ('Double', 2, 100.0),
            ('Suite', 4, 250.0)
            ON CONFLICT (room_type) DO NOTHING;
        """;
            conn.createStatement().execute(insertRooms);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showWelcomeScreen() {
        JFrame frame = new JFrame("Welcome to Svit_Hotel");
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel welcomeLabel = new JLabel("WELCOME TO SVIT_HOTEL");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(welcomeLabel, gbc);

        JButton registerButton = new JButton("Login");
        registerButton.setToolTipText("Click to login to your account");
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(registerButton, gbc);

        JButton viewButton = new JButton("View Rooms Without Login");
        viewButton.setToolTipText("View available rooms without logging in");
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(viewButton, gbc);

        JButton contactButton = new JButton("Contacts");
        contactButton.setToolTipText("Contact us for more information");
        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(contactButton, gbc);

        JButton exitButton = new JButton("Exit");
        exitButton.setToolTipText("Exit the application");
        gbc.gridx = 0;
        gbc.gridy = 4;
        frame.add(exitButton, gbc);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        registerButton.addActionListener(e -> {
            frame.dispose();
            promptLicenseKey();
        });

        viewButton.addActionListener(e -> {
            frame.dispose();
            viewAvailableRooms();
        });

        contactButton.addActionListener(e -> {
            frame.dispose();
            showContactPage();
        });

        exitButton.addActionListener(e -> System.exit(0));
    }


    private static void showContactPage() {
        JFrame frame = new JFrame("Contact Information");
        frame.setLayout(null);

        JLabel phoneLabel = new JLabel("Phone: +38077777777");
        phoneLabel.setBounds(50, 50, 300, 30);
        frame.add(phoneLabel);

        JLabel facebookLabel = new JLabel("Facebook: @svit_hotel");
        facebookLabel.setBounds(50, 100, 300, 30);
        frame.add(facebookLabel);

        JLabel instagramLabel = new JLabel("Instagram: @svit_hotel");
        instagramLabel.setBounds(50, 150, 300, 30);
        frame.add(instagramLabel);

        JButton backButton = new JButton("Back");
        backButton.setBounds(50, 200, 300, 30);
        backButton.addActionListener(e -> {
            frame.dispose();
            showWelcomeScreen();
        });
        frame.add(backButton);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void promptLicenseKey() {
        String licenseKey = JOptionPane.showInputDialog("Enter your license key:");
        if ("SECRET123".equals(licenseKey)) {
            JOptionPane.showMessageDialog(null, "License key valid. Proceeding...");
            loginForm();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid license key. Exiting...");
            System.exit(0);
        }
    }

    private static void loginForm() {
        JFrame frame = new JFrame("Login");
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");

        frame.setLayout(null);
        userLabel.setBounds(50, 50, 80, 25);
        userField.setBounds(150, 50, 150, 25);
        passLabel.setBounds(50, 100, 80, 25);
        passField.setBounds(150, 100, 150, 25);
        loginButton.setBounds(50, 150, 100, 25);
        backButton.setBounds(200, 150, 100, 25);

        frame.add(userLabel);
        frame.add(userField);
        frame.add(passLabel);
        frame.add(passField);
        frame.add(loginButton);
        frame.add(backButton);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        loginButton.addActionListener(e -> {
            if (loginAttempts >= 3) {
                JOptionPane.showMessageDialog(frame, "Too many failed attempts. Try again later.");
            } else {
                String username = userField.getText();
                String password = new String(passField.getPassword());
                if (authenticateUser(username, password)) {
                    frame.dispose();
                    showMainApp(username, "user"); // Покажіть головне вікно після успішного входу
                } else {
                    loginAttempts++;
                    JOptionPane.showMessageDialog(frame, "Invalid credentials. Attempts left: " + (3 - loginAttempts));
                }
            }
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            showWelcomeScreen();
        });
    }

    private static int currentUserId = -1; // Змінна для зберігання ID користувача

    private static boolean authenticateUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT id, role, password FROM customers WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");
                String role = rs.getString("role");

                // Перевірка пароля за допомогою хешу
                if (EncryptionUtil.checkPassword(password, storedPasswordHash)) {
                    currentUserId = rs.getInt("id"); // Отримуємо ID користувача
                    showMainApp(username, role);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return false;
    }



    private static void showMainApp(String username, String role) {
        JFrame frame = new JFrame("Main App");
        JLabel label = new JLabel("Welcome, " + username + " (" + role + ")");
        label.setBounds(50, 20, 300, 25);
        frame.setLayout(null); // використовуємо абсолютне позиціонування
        frame.add(label);

        if ("admin".equals(role)) {
            JButton manageRoomsButton = new JButton("Add Room");
            manageRoomsButton.setBounds(50, 70, 150, 30);
            frame.add(manageRoomsButton);

            JButton viewRoomsButton = new JButton("View Rooms");
            viewRoomsButton.setBounds(50, 120, 150, 30);
            frame.add(viewRoomsButton);

            // Додавання кнопки "Back"
            JButton backButton = new JButton("Back");
            backButton.setBounds(50, 170, 150, 30);
            frame.add(backButton);

            manageRoomsButton.addActionListener(e -> {
                addRoomForm();
            });

            viewRoomsButton.addActionListener(e -> {
                viewAvailableRooms();
            });

            backButton.addActionListener(e -> {
                frame.dispose();
                showWelcomeScreen();
            });
        } else if ("user".equals(role)) {
            JButton viewRoomsButton = new JButton("View Rooms");
            viewRoomsButton.setBounds(50, 70, 150, 30);
            frame.add(viewRoomsButton);

            JButton backButton = new JButton("Back");
            backButton.setBounds(50, 120, 150, 30);
            frame.add(backButton);

            viewRoomsButton.addActionListener(e -> {
                frame.dispose();
                viewAvailableRooms();
            });

            backButton.addActionListener(e -> {
                frame.dispose();
                showWelcomeScreen();
            });
        }

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void addRoomForm() {
        JFrame frame = new JFrame("Add Room");
        frame.setLayout(null);

        JLabel roomTypeLabel = new JLabel("Room Type:");
        JTextField roomTypeField = new JTextField();
        JLabel capacityLabel = new JLabel("Capacity:");
        JTextField capacityField = new JTextField();
        JLabel priceLabel = new JLabel("Price per Night:");
        JTextField priceField = new JTextField();
        JButton addButton = new JButton("Add Room");
        JButton backButton = new JButton("Back");

        roomTypeLabel.setBounds(50, 50, 120, 25);
        roomTypeField.setBounds(200, 50, 150, 25);
        capacityLabel.setBounds(50, 100, 120, 25);
        capacityField.setBounds(200, 100, 150, 25);
        priceLabel.setBounds(50, 150, 120, 25);
        priceField.setBounds(200, 150, 150, 25);
        addButton.setBounds(50, 200, 100, 25);
        backButton.setBounds(200, 200, 100, 25);

        frame.add(roomTypeLabel);
        frame.add(roomTypeField);
        frame.add(capacityLabel);
        frame.add(capacityField);
        frame.add(priceLabel);
        frame.add(priceField);
        frame.add(addButton);
        frame.add(backButton);

        addButton.addActionListener(e -> {
            String roomType = roomTypeField.getText();
            String capacity = capacityField.getText();
            String price = priceField.getText();

            // Додати номер у базу даних
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String insertRoom = "INSERT INTO rooms (room_type, capacity, price_per_night) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insertRoom);
                stmt.setString(1, roomType);
                stmt.setInt(2, Integer.parseInt(capacity));
                stmt.setDouble(3, Double.parseDouble(price));
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Room added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error adding room: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            showMainApp("admin", "admin");
        });

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void viewAvailableRooms() {
        JFrame frame = new JFrame("Available Rooms");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT id, room_type, capacity, price_per_night FROM rooms WHERE availability = 'available'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String roomType = rs.getString("room_type");
                int capacity = rs.getInt("capacity");
                double price = rs.getDouble("price_per_night");
                int roomId = rs.getInt("id");

                JLabel roomLabel = new JLabel("Room:  " + roomType + " |   Capacity: " + capacity + " |  Price: " + price);
                frame.add(roomLabel);

                JButton bookButton = new JButton("Book Room");
                bookButton.addActionListener(e -> bookRoom(roomId, roomType, price));
                frame.add(bookButton);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available rooms: " + e.getMessage());
        }

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            frame.dispose();
            showMainApp("user", "user");
        });
        frame.add(backButton);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void bookRoom(int roomId, String roomType, double price) {
        // Вікно з підтвердженням бронювання
        JFrame frame = new JFrame("Booking Confirmation");

        // Виведення інформації про заброньований номер
        JLabel infoLabel = new JLabel("You have booked a " + roomType + " room. Price per night: $" + price);
        infoLabel.setBounds(50, 20, 350, 30);

        // Поле для введення дати бронювання
        JLabel dateLabel = new JLabel("Enter Check-in Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField();
        dateLabel.setBounds(50, 60, 250, 25);
        dateField.setBounds(50, 90, 250, 25);
        frame.add(dateLabel);
        frame.add(dateField);

        // Поле для введення дати виїзду
        JLabel checkOutLabel = new JLabel("Enter Check-out Date (YYYY-MM-DD):");
        JTextField checkOutField = new JTextField();
        checkOutLabel.setBounds(50, 120, 250, 25);
        checkOutField.setBounds(50, 150, 250, 25);
        frame.add(checkOutLabel);
        frame.add(checkOutField);

        // Кнопка для підтвердження бронювання
        JButton paymentButton = new JButton("Go to Payment");
        paymentButton.setBounds(50, 200, 150, 30);

        // Кнопка для вибору додаткових послуг
        JButton servicesButton = new JButton("Additional Services");
        servicesButton.setBounds(220, 200, 150, 30);

        // Додавання компонентів до вікна
        frame.setLayout(null);
        frame.add(infoLabel);
        frame.add(paymentButton);
        frame.add(servicesButton);

        // Дія для кнопки "Go to Payment"
        paymentButton.addActionListener(e -> {
            String inputDate = dateField.getText();
            String inputCheckOutDate = checkOutField.getText();

            // Перевірка формату дати та обмеження на 30 днів
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate checkInDate = LocalDate.parse(inputDate, formatter);
                LocalDate checkOutDate = LocalDate.parse(inputCheckOutDate, formatter);
                LocalDate currentDate = LocalDate.now();
                LocalDate maxAllowedDate = currentDate.plusDays(30);

                if (checkInDate.isBefore(currentDate) || checkInDate.isAfter(maxAllowedDate)) {
                    JOptionPane.showMessageDialog(frame, "Booking is only allowed within the next 30 days.");
                } else {
                    // Виклик процедури бронювання в базі даних
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        // Перевірка чи є ID користувача
                        if (currentUserId == -1) {
                            JOptionPane.showMessageDialog(frame, "User is not authenticated.");
                            return;
                        }

                        // Виклик збереженої процедури для бронювання
                        String sql = "CALL book_room(?, ?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, roomId);  // ID номера
                            stmt.setInt(2, currentUserId);  // ID користувача
                            stmt.setDate(3, Date.valueOf(checkInDate));  // Дата заїзду
                            stmt.setDate(4, Date.valueOf(checkOutDate));  // Дата виїзду
                            stmt.executeUpdate();
                        }
                    }
                    catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error booking room: " + ex.getMessage());
                    }

                    frame.dispose();
                    showPaymentScreen(roomId, price); // Відкриваємо екран оплати
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Please use YYYY-MM-DD.");
            }
        });

        // Дія для кнопки "Additional Services"
        servicesButton.addActionListener(e -> {
            frame.dispose();
            showAdditionalServicesScreen(); // Відкриваємо екран додаткових послуг
        });

        // Налаштування вікна
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    private static void showPaymentScreen(int roomId, double price) {
        JFrame frame = new JFrame("Payment");

        // Виведення суми до оплати
        JLabel priceLabel = new JLabel("Total Price: $" + price);
        priceLabel.setBounds(50, 20, 250, 30);

        // Кнопка для підтвердження оплати
        JButton payButton = new JButton("Pay Now");
        payButton.setBounds(50, 70, 150, 30);

        // Кнопка для скасування
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 70, 150, 30);

        // Додавання компонентів до вікна
        frame.setLayout(null);
        frame.add(priceLabel);
        frame.add(payButton);
        frame.add(cancelButton);

        // Дія для кнопки "Pay Now"
        payButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Payment Successful!");
            frame.dispose();
            showMainApp("user", "user");
        });

        // Дія для кнопки "Cancel"
        cancelButton.addActionListener(e -> {
            frame.dispose();
            showMainApp("user", "user");
        });

        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void showAdditionalServicesScreen() {
        JFrame frame = new JFrame("Additional Services");

        // Перелік доступних додаткових послуг
        String[] services = {"Breakfast", "Airport Transfer", "Laundry", "Spa", "Gym"};
        JList<String> servicesList = new JList<>(services);
        servicesList.setBounds(50, 20, 200, 100);

        // Кнопка для підтвердження вибору послуг
        JButton confirmButton = new JButton("Confirm Services");
        confirmButton.setBounds(50, 140, 150, 30);

        // Додавання компонентів до вікна
        frame.setLayout(null);
        frame.add(servicesList);
        frame.add(confirmButton);

        // Дія для кнопки "Confirm Services"
        confirmButton.addActionListener(e -> {
            String selectedService = servicesList.getSelectedValue();
            if (selectedService != null) {
                JOptionPane.showMessageDialog(frame, "You selected: " + selectedService);
                frame.dispose();
                showPaymentScreenWithAdditionalServices(selectedService); // Перехід до оплати після вибору послуг
            } else {
                JOptionPane.showMessageDialog(frame, "No service selected.");
            }
        });

        frame.setSize(300, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void showPaymentScreenWithAdditionalServices(String selectedService) {
        JFrame frame = new JFrame("Payment");

        // Виведення суми до оплати
        double totalPrice = 250.0; // Це буде ваш стандартний тариф за номер, його треба динамічно обчислювати
        String priceMessage = "Total Price: $" + totalPrice;

        // Якщо є додаткові послуги, додаємо їх до вартості
        if (selectedService != null) {
            priceMessage += "\nAdditional Service: " + selectedService + " ($20)";
            totalPrice += 20.0; // Додаємо вартість додаткової послуги
        }

        JLabel priceLabel = new JLabel(priceMessage);
        priceLabel.setBounds(50, 20, 250, 30);

        // Кнопка для підтвердження оплати
        JButton payButton = new JButton("Pay Now");
        payButton.setBounds(50, 70, 150, 30);

        // Кнопка для скасування
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 70, 150, 30);

        // Додавання компонентів до вікна
        frame.setLayout(null);
        frame.add(priceLabel);
        frame.add(payButton);
        frame.add(cancelButton);

        // Дія для кнопки "Pay Now"
        payButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Payment Successful!");
            frame.dispose();
            showMainApp("user", "user"); // Повертаємось до головного меню після успішної оплати
        });

        // Дія для кнопки "Cancel"
        cancelButton.addActionListener(e -> {
            frame.dispose();
            showMainApp("user", "user"); // Повертаємось до головного меню без оплати
        });

        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
