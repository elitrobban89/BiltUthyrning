package com.biltuthyrning.gui;

import com.biltuthyrning.BiltUthyrningApplication;
import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.model.User;
import com.biltuthyrning.service.BookingService;
import com.biltuthyrning.service.CarService;
import com.biltuthyrning.service.UserService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

public class CarRentalGUI extends Application {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final String PRIMARY   = "#1C4E80";
    private static final String BG        = "#EEF2F7";
    private static final String CARD      = "#FFFFFF";
    private static final String BORDER    = "#D5DCE8";
    private static final String TEXT      = "#2C3E50";
    private static final String MUTED     = "#7F8C8D";
    private static final String SEL_BG    = "#EBF5FB";
    private static final String SEL_BORD  = "#2E86C1";

    private static final String BTN_PRIMARY =
        "-fx-background-color: #1C4E80; -fx-text-fill: white; -fx-font-weight: bold; " +
        "-fx-padding: 8 18; -fx-background-radius: 5; -fx-cursor: hand;";
    private static final String BTN_DANGER =
        "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; " +
        "-fx-padding: 8 18; -fx-background-radius: 5; -fx-cursor: hand;";
    private static final String BTN_SECONDARY =
        "-fx-background-color: #ECF0F1; -fx-text-fill: " + TEXT + "; -fx-font-weight: bold; " +
        "-fx-padding: 8 18; -fx-background-radius: 5; -fx-cursor: hand;";

    // ── State ─────────────────────────────────────────────────────────────────
    private static ConfigurableApplicationContext springContext;

    private CarService carService;
    private BookingService bookingService;
    private UserService userService;
    private User loggedInUser;

    private ListView<Car> carListView;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Label priceLabel;
    private TableView<Booking> bookingTable;
    private TextField customerNameField;

    // ── JavaFX lifecycle ──────────────────────────────────────────────────────

    @Override
    public void init() throws Exception {
        springContext = new SpringApplicationBuilder(BiltUthyrningApplication.class)
                .headless(false).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeServices();

        loggedInUser = new LoginGUI(userService).showAndWait();
        if (loggedInUser == null) { Platform.exit(); return; }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: " + BG + ";");
        root.setTop(buildHeader(primaryStage));
        root.setCenter(buildContent());

        Scene scene = new Scene(root, 1060, 700);
        primaryStage.setTitle("BilUthyrning");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadCars();
        loadBookings();
    }

    @Override
    public void stop() throws Exception {
        if (springContext != null) springContext.close();
    }

    private void initializeServices() {
        try {
            carService     = springContext.getBean(CarService.class);
            bookingService = springContext.getBean(BookingService.class);
            userService    = springContext.getBean(UserService.class);
        } catch (Exception e) {
            System.err.println("Spring init error: " + e.getMessage());
        }
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private HBox buildHeader(Stage stage) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: " + PRIMARY + "; -fx-padding: 14 22;");

        Label icon  = label("🚗", "22", "white", true);
        Label title = label("BilUthyrning", "20", "white", true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLbl = label("👤  " + loggedInUser.getUsername(), "12", "#BDC3C7", false);

        Button logoutBtn = new Button("Logga ut");
        logoutBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; " +
            "-fx-border-color: rgba(255,255,255,0.45); -fx-border-radius: 4; " +
            "-fx-padding: 5 14; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> { stage.close(); Platform.exit(); });

        bar.getChildren().addAll(icon, title, spacer, userLbl, logoutBtn);
        return bar;
    }

    // ── Main layout ───────────────────────────────────────────────────────────

    private SplitPane buildContent() {
        SplitPane split = new SplitPane();
        split.setStyle("-fx-background-color: " + BG + "; -fx-box-border: transparent;");
        split.getItems().addAll(buildCarPanel(), buildRightPanel());
        split.setDividerPositions(0.40);
        return split;
    }

    // ── Left: car list ────────────────────────────────────────────────────────

    private VBox buildCarPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG + ";");

        Label heading = label("Tillgängliga bilar", "14", TEXT, true);

        carListView = new ListView<>();
        carListView.setCellFactory(lv -> new CarListCell());
        carListView.setStyle(
            "-fx-background-color: transparent; -fx-border-color: transparent; " +
            "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        VBox.setVgrow(carListView, Priority.ALWAYS);
        carListView.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, cur) -> updatePrice());

        panel.getChildren().addAll(heading, carListView);
        return panel;
    }

    // ── Right: booking form + table ───────────────────────────────────────────

    private VBox buildRightPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG + ";");

        VBox tableCard = buildBookingsTableCard();
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        panel.getChildren().addAll(buildBookingFormCard(), tableCard);
        return panel;
    }

    private VBox buildBookingFormCard() {
        VBox card = card("Gör en bokning");

        HBox datesRow = new HBox(16);

        VBox startBox = new VBox(4);
        startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setOnAction(e -> updatePrice());
        startDatePicker.setPrefWidth(160);
        startBox.getChildren().addAll(label("Startdatum", "11", MUTED, false), startDatePicker);

        VBox endBox = new VBox(4);
        endDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        endDatePicker.setOnAction(e -> updatePrice());
        endDatePicker.setPrefWidth(160);
        endBox.getChildren().addAll(label("Slutdatum", "11", MUTED, false), endDatePicker);

        datesRow.getChildren().addAll(startBox, endBox);

        VBox nameBox = new VBox(4);
        customerNameField = new TextField();
        customerNameField.setPromptText("Ange kundnamn");
        customerNameField.setPrefWidth(336);
        customerNameField.setStyle(
            "-fx-padding: 6 10; -fx-border-color: " + BORDER + "; " +
            "-fx-border-radius: 4; -fx-background-radius: 4;");
        nameBox.getChildren().addAll(label("Kundnamn", "11", MUTED, false), customerNameField);

        priceLabel = new Label("Totalt pris:  –");
        priceLabel.setStyle(
            "-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY + ";");

        Button confirmBtn = new Button("✔  Bekräfta bokning");
        confirmBtn.setStyle(BTN_PRIMARY);
        confirmBtn.setOnAction(e -> handleBooking());

        card.getChildren().addAll(datesRow, nameBox, priceLabel, confirmBtn);
        return card;
    }

    private VBox buildBookingsTableCard() {
        VBox card = card("Bokningar");
        VBox.setVgrow(card, Priority.ALWAYS);

        bookingTable = new TableView<>();
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bookingTable.setStyle("-fx-font-size: 12;");
        VBox.setVgrow(bookingTable, Priority.ALWAYS);

        bookingTable.getColumns().addAll(
            col("ID",     50,  b -> String.valueOf(b.getId())),
            col("Bil",    160, b -> b.getCar().getModel()),
            col("Kund",   130, b -> b.getCustomerName()),
            col("Start",  95,  b -> b.getStartDate().toString()),
            col("Slut",   95,  b -> b.getEndDate().toString()),
            col("Pris",   90,  b -> String.format("%.0f kr", b.getTotalPrice())),
            col("Status", 95,  b -> b.getStatus().toString())
        );

        HBox btnRow = new HBox(10);
        Button refreshBtn = new Button("↻  Uppdatera");
        refreshBtn.setStyle(BTN_SECONDARY);
        refreshBtn.setOnAction(e -> loadBookings());

        Button cancelBtn = new Button("✖  Avboka vald");
        cancelBtn.setStyle(BTN_DANGER);
        cancelBtn.setOnAction(e -> handleCancelBooking());

        btnRow.getChildren().addAll(refreshBtn, cancelBtn);
        card.getChildren().addAll(btnRow, bookingTable);
        return card;
    }

    // ── Car color / type mapping ──────────────────────────────────────────────

    static String carBadgeColor(String model) {
        String m = model.toUpperCase();
        if (m.contains("EX60"))  return "#1E8449"; // electric green
        if (m.contains("XC40"))  return "#2471A3"; // blue
        if (m.contains("XC60"))  return "#1A6090"; // darker blue
        if (m.contains("XC90"))  return "#154360"; // navy
        if (m.contains("S90"))   return "#6C3483"; // purple
        if (m.contains("V90"))   return "#0E6655"; // teal
        return "#2E86C1";
    }

    private static String getCarType(String model) {
        String m = model.toUpperCase();
        if (m.contains("S90"))  return "sedan";
        if (m.contains("V90"))  return "wagon";
        if (m.contains("XC90")) return "large-suv";
        return "suv"; // XC40, XC60, EX60
    }

    // ── Canvas car drawing ────────────────────────────────────────────────────

    static void drawCarOnCanvas(Canvas canvas, String model) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Color base  = Color.web(carBadgeColor(model));
        Color dark  = base.darker();
        Color glass = Color.color(0.74, 0.88, 0.97, 0.82);
        Color tyre  = Color.color(0.14, 0.14, 0.14);
        Color rim   = Color.color(0.78, 0.78, 0.78);

        String type = getCarType(model);

        // Ground shadow
        gc.setFill(Color.color(0, 0, 0, 0.10));
        gc.fillOval(9, 51, 72, 7);

        // Lower body (shared across types)
        gc.setFill(base);
        gc.fillRoundRect(4, 34, 82, 12, 6, 6);

        // Cabin polygon, windows — differ by type
        double[] roofX, roofY, w1x, w1y, w2x, w2y;

        if ("sedan".equals(type)) {
            // Three-box profile: shorter cabin, hood + trunk visible
            roofX = new double[]{18, 18, 27, 62, 70, 70};
            roofY = new double[]{34, 23, 17, 17, 23, 34};
            w1x   = new double[]{22, 24, 36, 36};
            w1y   = new double[]{34, 23, 18, 34};
            w2x   = new double[]{38, 38, 60, 62};
            w2y   = new double[]{34, 18, 18, 34};
        } else if ("wagon".equals(type)) {
            // Long flat roofline, three windows
            roofX = new double[]{11, 11, 18, 75, 79, 79};
            roofY = new double[]{34, 18, 13, 13, 18, 34};
            w1x   = new double[]{19, 21, 35, 35};
            w1y   = new double[]{34, 14, 14, 34};
            w2x   = new double[]{37, 37, 52, 52};
            w2y   = new double[]{34, 14, 14, 34};
        } else if ("large-suv".equals(type)) {
            // Tall, wide SUV
            roofX = new double[]{8, 8, 16, 74, 81, 81};
            roofY = new double[]{34, 16, 11, 11, 16, 34};
            w1x   = new double[]{17, 19, 38, 38};
            w1y   = new double[]{34, 12, 12, 34};
            w2x   = new double[]{40, 40, 70, 72};
            w2y   = new double[]{34, 12, 12, 34};
        } else {
            // Standard SUV/crossover
            roofX = new double[]{11, 11, 18, 72, 78, 78};
            roofY = new double[]{34, 18, 13, 13, 18, 34};
            w1x   = new double[]{19, 21, 36, 36};
            w1y   = new double[]{34, 14, 14, 34};
            w2x   = new double[]{38, 38, 68, 70};
            w2y   = new double[]{34, 14, 14, 34};
        }

        // Cabin fill
        gc.setFill(base);
        gc.fillPolygon(roofX, roofY, roofX.length);

        // Outlines
        gc.setStroke(dark);
        gc.setLineWidth(1.3);
        gc.strokeRoundRect(4, 34, 82, 12, 6, 6);
        gc.strokePolygon(roofX, roofY, roofX.length);

        // Windows
        gc.setFill(glass);
        gc.fillPolygon(w1x, w1y, w1x.length);
        gc.fillPolygon(w2x, w2y, w2x.length);
        if ("wagon".equals(type)) {
            gc.fillRect(54, 14, 18, 20);
        }

        gc.setStroke(dark.deriveColor(0, 1, 1.4, 0.6));
        gc.setLineWidth(0.8);
        gc.strokePolygon(w1x, w1y, w1x.length);
        gc.strokePolygon(w2x, w2y, w2x.length);
        if ("wagon".equals(type)) {
            gc.strokeRect(54, 14, 18, 20);
        }

        // Headlight (front = right)
        gc.setFill(Color.color(1.0, 0.97, 0.75));
        gc.fillOval(83, 37, 5, 4);

        // Taillight (rear = left)
        gc.setFill(Color.color(0.92, 0.12, 0.10));
        gc.fillOval(3, 37, 5, 4);

        // Wheels
        drawWheel(gc, 19, 46, 9, tyre, rim);
        drawWheel(gc, 71, 46, 9, tyre, rim);

        // EV badge for electric models
        if (model.toUpperCase().contains("EX")) {
            gc.setFill(Color.color(0.1, 0.1, 0.1, 0.65));
            gc.fillRoundRect(36, 21, 22, 12, 4, 4);
            gc.setFill(Color.web("#F1C40F"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
            gc.fillText("EV ⚡", 38, 31);
        }
    }

    private static void drawWheel(GraphicsContext gc, double cx, double cy, double r,
            Color tyre, Color rim) {
        gc.setFill(tyre);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
        gc.setStroke(Color.color(0.05, 0.05, 0.05));
        gc.setLineWidth(0.8);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        double rr = r * 0.60;
        gc.setFill(rim);
        gc.fillOval(cx - rr, cy - rr, rr * 2, rr * 2);
        gc.setStroke(Color.color(0.5, 0.5, 0.5));
        gc.strokeOval(cx - rr, cy - rr, rr * 2, rr * 2);
        double hr = r * 0.18;
        gc.setFill(tyre);
        gc.fillOval(cx - hr, cy - hr, hr * 2, hr * 2);
    }

    // ── Custom ListView cell ──────────────────────────────────────────────────

    private static class CarListCell extends ListCell<Car> {

        private final HBox   row       = new HBox(12);
        private final Canvas canvas    = new Canvas(90, 55);
        private final Label  nameLbl   = new Label();
        private final Label  detailLbl = new Label();
        private final Label  priceLbl  = new Label();

        CarListCell() {
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));

            StackPane canvasPane = new StackPane(canvas);
            canvasPane.setMinWidth(96);
            canvasPane.setMinHeight(58);
            canvasPane.setStyle(
                "-fx-background-color: #F0F4F8; -fx-background-radius: 8;");

            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: " + TEXT + ";");
            detailLbl.setStyle("-fx-font-size: 11; -fx-text-fill: " + MUTED + ";");
            priceLbl.setStyle(
                "-fx-background-color: #EBF5FB; -fx-text-fill: " + PRIMARY + "; " +
                "-fx-font-size: 11; -fx-font-weight: bold; " +
                "-fx-padding: 4 10; -fx-background-radius: 12;");

            VBox textBox = new VBox(3, nameLbl, detailLbl);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            row.getChildren().addAll(canvasPane, textBox, priceLbl);
            selectedProperty().addListener((obs, old, sel) -> applyRowStyle(sel));
        }

        @Override
        protected void updateItem(Car car, boolean empty) {
            super.updateItem(car, empty);
            setStyle("-fx-background-color: transparent; -fx-padding: 3 4;");
            if (empty || car == null) { setGraphic(null); return; }
            drawCarOnCanvas(canvas, car.getModel());
            nameLbl.setText(car.getModel());
            detailLbl.setText(car.getYear() + "  ·  " + car.getEngine());
            priceLbl.setText(car.getDailyRate().toPlainString() + " kr/dag");
            applyRowStyle(isSelected());
            setGraphic(row);
        }

        private void applyRowStyle(boolean selected) {
            row.setStyle(
                "-fx-background-color: " + (selected ? SEL_BG : CARD) + "; " +
                "-fx-border-color: "     + (selected ? SEL_BORD : BORDER) + "; " +
                "-fx-border-width: "     + (selected ? "2" : "1") + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        }
    }

    // ── Data operations ───────────────────────────────────────────────────────

    private void loadCars() {
        try {
            if (carService != null)
                carListView.getItems().setAll(carService.getAllCars());
        } catch (Exception e) {
            showError("Fel vid hämtning av bilar: " + e.getMessage());
        }
    }

    private void updatePrice() {
        try {
            Car car     = carListView.getSelectionModel().getSelectedItem();
            LocalDate s = startDatePicker.getValue();
            LocalDate e = endDatePicker.getValue();
            if (car == null || s == null || e == null || !e.isAfter(s)) {
                priceLabel.setText("Totalt pris:  –");
                return;
            }
            BigDecimal price = bookingService.calculateTotalPrice(car.getId(), s, e);
            priceLabel.setText(String.format("Totalt pris:  %.2f kr", price));
        } catch (Exception ex) {
            priceLabel.setText("Totalt pris:  –");
        }
    }

    private void handleBooking() {
        Car car = carListView.getSelectionModel().getSelectedItem();
        if (car == null || customerNameField.getText().isBlank()) {
            showError("Välj en bil och ange kundnamn.");
            return;
        }
        LocalDate start = startDatePicker.getValue();
        LocalDate end   = endDatePicker.getValue();
        if (!end.isAfter(start)) {
            showError("Slutdatumet måste vara efter startdatumet.");
            return;
        }
        try {
            Booking booking = bookingService.createBooking(
                car.getId(), customerNameField.getText(), start, end);
            showInfo("Bokning bekräftad!  Boknings-ID: " + booking.getId());
            customerNameField.clear();
            loadBookings();
        } catch (Exception e) {
            showError("Bokningsfel: " + e.getMessage());
        }
    }

    private void handleCancelBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Välj en bokning i tabellen att avboka.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Avboka bokning #" + selected.getId() +
            " för " + selected.getCustomerName() + "?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Bekräfta avbokning");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    bookingService.cancelBooking(selected.getId());
                    showInfo("Bokning avbokad!");
                    loadBookings();
                } catch (Exception e) {
                    showError("Avbokningsfel: " + e.getMessage());
                }
            }
        });
    }

    private void loadBookings() {
        try {
            if (bookingService != null)
                bookingTable.getItems().setAll(bookingService.getAllBookings());
        } catch (Exception e) {
            showError("Fel vid hämtning av bokningar: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VBox card(String title) {
        VBox box = new VBox(12);
        box.setStyle(
            "-fx-background-color: " + CARD + "; " +
            "-fx-border-color: " + BORDER + "; -fx-border-radius: 8; " +
            "-fx-background-radius: 8; -fx-padding: 16; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.5;");
        box.getChildren().addAll(label(title, "13", TEXT, true), sep);
        return box;
    }

    private Label label(String text, String size, String color, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: " + size + "; -fx-text-fill: " + color + ";" +
            (bold ? " -fx-font-weight: bold;" : ""));
        return l;
    }

    private TableColumn<Booking, String> col(String name, int width,
            Function<Booking, String> extractor) {
        TableColumn<Booking, String> c = new TableColumn<>(name);
        c.setPrefWidth(width);
        c.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(d.getValue())));
        return c;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Fel"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("OK"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
