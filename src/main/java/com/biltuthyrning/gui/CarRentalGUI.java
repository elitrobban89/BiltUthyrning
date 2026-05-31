package com.biltuthyrning.gui;

import com.biltuthyrning.BiltUthyrningApplication;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.User;
import com.biltuthyrning.service.CarService;
import com.biltuthyrning.service.BookingService;
import com.biltuthyrning.service.UserService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CarRentalGUI extends Application {

    private static ConfigurableApplicationContext springContext;

    private CarService carService;
    private BookingService bookingService;
    private UserService userService;
    private User loggedInUser;
    private ComboBox<Car> carComboBox;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Label priceLabel;
    private TextArea bookingListArea;
    private TextField customerNameField;
    
    @Override
    public void init() throws Exception {
        springContext = new SpringApplicationBuilder(BiltUthyrningApplication.class)
                .headless(false)
                .run();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeServices();

        LoginGUI loginGUI = new LoginGUI(userService);
        loggedInUser = loginGUI.showAndWait();

        if (loggedInUser == null) {
            Platform.exit();
            return;
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11;");

        root.setTop(createMenuBar(primaryStage));
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("BiltUthyrning - Inloggad som: " + loggedInUser.getUsername());
        primaryStage.setScene(scene);
        primaryStage.show();

        loadCars();
    }
    
    @Override
    public void stop() throws Exception {
        if (springContext != null) {
            springContext.close();
        }
    }
    
    private void initializeServices() {
        try {
            carService = springContext.getBean(CarService.class);
            bookingService = springContext.getBean(BookingService.class);
            userService = springContext.getBean(UserService.class);
        } catch (Exception e) {
            System.err.println("Could not load Spring services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("Arkiv");

        MenuItem logoutItem = new MenuItem("Logga ut");
        logoutItem.setOnAction(e -> {
            stage.close();
            Platform.exit();
        });

        MenuItem exitItem = new MenuItem("Avsluta");
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }
    
    private VBox createMainContent() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        
        // Title
        Label titleLabel = new Label("BiltUthyrning - Biluthyrningssystem");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        root.getChildren().add(titleLabel);
        
        // Car selection section
        VBox carSection = createCarSelectionSection();
        root.getChildren().add(createSectionBox("Välj en bil", carSection));
        
        // Booking section
        VBox bookingSection = createBookingSection();
        root.getChildren().add(createSectionBox("Gör en bokning", bookingSection));
        
        // Bookings list section
        VBox listSection = createBookingsListSection();
        root.getChildren().add(createSectionBox("Visa bokningar", listSection));
        
        return root;
    }
    
    private VBox createCarSelectionSection() {
        VBox section = new VBox(10);
        
        carComboBox = new ComboBox<>();
        carComboBox.setPrefWidth(300);
        carComboBox.setCellFactory(lv -> new ListCell<Car>() {
            @Override
            protected void updateItem(Car car, boolean empty) {
                super.updateItem(car, empty);
                setText(empty ? "" : car.getModel() + " (" + car.getYear() + ") - " + car.getEngine());
            }
        });
        carComboBox.setButtonCell(new ListCell<Car>() {
            @Override
            protected void updateItem(Car car, boolean empty) {
                super.updateItem(car, empty);
                setText(empty ? "" : car.getModel() + " (" + car.getYear() + ") - " + car.getEngine());
            }
        });
        
        carComboBox.setOnAction(e -> updatePrice());
        
        section.getChildren().addAll(
            new Label("Välj bil:"),
            carComboBox
        );
        
        return section;
    }
    
    private VBox createBookingSection() {
        VBox section = new VBox(10);
        
        HBox dateBox = new HBox(10);
        
        VBox startBox = new VBox(5);
        startBox.getChildren().addAll(
            new Label("Start Date:"),
            startDatePicker = new DatePicker(LocalDate.now())
        );
        startDatePicker.setOnAction(e -> updatePrice());
        
        VBox endBox = new VBox(5);
        endBox.getChildren().addAll(
            new Label("End Date:"),
            endDatePicker = new DatePicker(LocalDate.now().plusDays(1))
        );
        endDatePicker.setOnAction(e -> updatePrice());
        
        dateBox.getChildren().addAll(startBox, endBox);
        
        VBox customerBox = new VBox(5);
        customerBox.getChildren().addAll(
            new Label("Customer Name:"),
            customerNameField = new TextField()
        );
        customerNameField.setPrefWidth(300);
        
        priceLabel = new Label("Total Pris: 0.00 kr");
        priceLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #0066cc;");
        
        Button bookButton = new Button("Confirm Booking");
        bookButton.setPrefWidth(150);
        bookButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        bookButton.setOnAction(e -> handleBooking());
        
        section.getChildren().addAll(
            dateBox,
            customerBox,
            priceLabel,
            bookButton
        );
        
        return section;
    }
    
    private VBox createBookingsListSection() {
        VBox section = new VBox(10);
        
        HBox buttonBox = new HBox(10);
        Button refreshButton = new Button("Uppdatera");
        refreshButton.setOnAction(e -> loadBookings());
        
        Button cancelButton = new Button("Avboka vald bokning");
        cancelButton.setOnAction(e -> handleCancelBooking());
        
        bookingListArea = new TextArea();
        bookingListArea.setEditable(false);
        bookingListArea.setPrefHeight(150);
        bookingListArea.setWrapText(true);
        
        buttonBox.getChildren().addAll(refreshButton, cancelButton);
        section.getChildren().addAll(buttonBox, bookingListArea);
        
        return section;
    }
    
    private VBox createSectionBox(String title, VBox content) {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        
        box.getChildren().addAll(titleLabel, new Separator(), content);
        
        return box;
    }
    
    private void loadCars() {
        try {
            if (carService != null) {
                List<Car> cars = carService.getAllCars();
                carComboBox.getItems().setAll(cars);
            }
        } catch (Exception e) {
            showError("Error loading cars: " + e.getMessage());
        }
    }

    private void updatePrice() {
        try {
            Car selectedCar = carComboBox.getValue();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            // Prevent invalid date ranges
            if (start != null && end != null && !end.isAfter(start)) {
                priceLabel.setText("Total Pris: 0.00 kr");
                return;
            }

            if (selectedCar != null && start != null && end != null) {
                BigDecimal price = bookingService.calculateTotalPrice(
                        selectedCar.getId(),
                        start,
                        end
                );
                priceLabel.setText(String.format("Total Pris: %.2f kr", price));
            }
        } catch (Exception e) {
            priceLabel.setText("Fel vid prisberäkning");
        }
    }
    
    private void handleBooking() {
        try {
            if (carComboBox.getValue() == null || customerNameField.getText().isBlank()) {
                showError("Vänligen välj en bil och ange kundnamn");
                return;
            }
            
            Car car = carComboBox.getValue();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            
            if (start.isAfter(end) || start.equals(end)) {
                showError("Slutdatumet måste vara efter startdatumet");
                return;
            }
            
            Booking booking = bookingService.createBooking(
                car.getId(),
                customerNameField.getText(),
                start,
                end
            );
            
            showInfo("Bokning bekräftad! Boknings-ID: " + booking.getId());
            customerNameField.clear();
            loadBookings();
        } catch (Exception e) {
            showError("Bokningsfel: " + e.getMessage());
        }
    }
    
    private void handleCancelBooking() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Avboka");
        dialog.setHeaderText("Ange boknings-ID för avbokning:");
        dialog.setContentText("Boknings-ID:");

        dialog.showAndWait().ifPresent(bookingIdStr -> {
            try {
                Long bookingId = Long.parseLong(bookingIdStr);
                bookingService.cancelBooking(bookingId);
                showInfo("Bokning avbokad!");
                loadBookings();
            } catch (NumberFormatException e) {
                showError("Ogiltigt boknings-ID");
            } catch (Exception e) {
                showError("Avbokningsfel: " + e.getMessage());
            }
        });
    }
    
    private void loadBookings() {
        try {
            if (bookingService != null) {
                List<Booking> bookings = bookingService.getAllBookings();
                StringBuilder sb = new StringBuilder();
                
                for (Booking b : bookings) {
                    sb.append(String.format(
                        "ID: %d | Bil: %s | Kund: %s | Datum: %s till %s | Pris: %.2f kr | Status: %s%n",
                        b.getId(),
                        b.getCar().getModel(),
                        b.getCustomerName(),
                        b.getStartDate(),
                        b.getEndDate(),
                        b.getTotalPrice(),
                        b.getStatus()
                    ));
                }
                
                bookingListArea.setText(sb.toString().isEmpty() ? "Inga bokningar hittades" : sb.toString());
            }
        } catch (Exception e) {
            bookingListArea.setText("Fel vid hämtning av bokningar: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
