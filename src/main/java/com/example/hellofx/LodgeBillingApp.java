package com.example.hellofx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Optional;
import java.util.function.Function;

public class LodgeBillingApp extends Application {

    static class Bill {
        String id, guest, room;
        int nights;
        double total, paid, balance;
    }

    // Practice login details - change them here if you like
    private static final String USERNAME = "Group90";
    private static final String PASSWORD = "G1234";

    private Stage stage;
    private String currentUser = "";

    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private int counter = 0;

    private TextField guestField, nightsField, extrasField, paidField;
    private ComboBox<String> roomBox;
    private TextArea notesArea;
    private Label status;

    private static String money(double v) {
        return String.format("K %.2f", v);
    }

    // Shows a green (ok) or red (error) message on a label
    private static void tell(Label label, String msg, boolean error) {
        label.setText(msg);
        label.getStyleClass().removeAll("status-ok", "status-error");
        label.getStyleClass().add(error ? "status-error" : "status-ok");
    }

    // Puts a page on the window and loads the dark blue style
    private void showPage(Parent root) {
        Scene scene = new Scene(root, 1050, 740);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Lodge Billing Lab");
        showLogin("");
        stage.show();
    }

    // ================= LOGIN PAGE =================
    private void showLogin(String message) {
        Label title = new Label("Lodge Billing Lab");
        title.getStyleClass().add("login-title");
        Label sub = new Label("Please log in to continue");
        sub.getStyleClass().add("muted");

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("field-label");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("field-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");

        Label msg = new Label("");
        if (!message.isEmpty()) tell(msg, message, false);

        Button loginBtn = new Button("Log in");
        loginBtn.getStyleClass().add("primary-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Label hint = new Label("Practice login: Group90 / G1234");
        hint.getStyleClass().add("muted");

        Runnable doLogin = () -> {
            String u = userField.getText().trim();
            if (USERNAME.equals(u) && PASSWORD.equals(passField.getText())) {
                currentUser = u;
                showBilling();
            } else {
                tell(msg, "Wrong username or password.", true);
                passField.clear();
            }
        };
        loginBtn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());   // pressing Enter also logs in

        VBox card = new VBox(10, title, sub, userLabel, userField, passLabel, passField,
                loginBtn, msg, hint);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(360);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("login-bg");
        showPage(root);
    }

    // ================= BILLING PAGE =================
    private void showBilling() {
        // ---- Header ----
        Label title = new Label("Lodge Billing Lab");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Mulungushi University, Great North Road Campus");
        subtitle.getStyleClass().add("header-sub");
        VBox titles = new VBox(2, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label user = new Label("Logged in as: " + currentUser);
        user.getStyleClass().add("header-sub");
        Button logoutBtn = new Button("Log out");
        logoutBtn.getStyleClass().add("light-button");

        HBox header = new HBox(15, titles, spacer, user, logoutBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");

        // ---- Sidebar (room prices) ----
        Label pricesTitle = new Label("Room prices");
        pricesTitle.getStyleClass().add("sidebar-title");
        Label rules = new Label(
                "5 nights or more: 5% off the room charge.\n\n"
                        + "Extras get no discount. No tax in this lab.\n\n"
                        + "Saved bills last only until you close the app.");
        rules.setWrapText(true);
        rules.getStyleClass().add("sidebar-text");

        VBox sidebar = new VBox(14, pricesTitle,
                priceRow("Standard", "K 350.00 / night"),
                priceRow("Deluxe", "K 550.00 / night"),
                priceRow("Family", "K 750.00 / night"),
                rules);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(270);

        // ---- Action buttons ----
        Button newBtn = new Button("New guest");
        newBtn.getStyleClass().add("light-button");
        Button receiptBtn = new Button("View receipt");
        receiptBtn.getStyleClass().add("light-button");
        Button deleteBtn = new Button("Delete selected bill");
        deleteBtn.getStyleClass().add("danger-button");
        HBox actions = new HBox(10, newBtn, receiptBtn, deleteBtn);

        // ---- Form ----
        guestField = new TextField();
        roomBox = new ComboBox<>(FXCollections.observableArrayList("Standard", "Deluxe", "Family"));
        roomBox.setValue("Standard");
        roomBox.setMaxWidth(Double.MAX_VALUE);
        nightsField = new TextField();
        extrasField = new TextField();
        paidField = new TextField();
        notesArea = new TextArea();
        notesArea.setPrefRowCount(2);

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(12);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        form.getColumnConstraints().addAll(half, half);
        form.add(field("Guest name", guestField), 0, 0);
        form.add(field("Room type", roomBox), 1, 0);
        form.add(field("Nights", nightsField), 0, 1);
        form.add(field("Extras in K", extrasField), 1, 1);
        form.add(field("Amount paid in K", paidField), 0, 2);
        form.add(field("Notes", notesArea), 1, 2);

        Button calcBtn = new Button("Calculate");
        calcBtn.getStyleClass().add("light-button");
        Button saveBtn = new Button("Save bill");
        saveBtn.getStyleClass().add("primary-button");
        status = new Label("");
        HBox buttons = new HBox(12, calcBtn, saveBtn, status);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox formCard = new VBox(15, form, buttons);
        formCard.getStyleClass().add("card");

        // ---- Saved bills table ----
        Label savedTitle = new Label("Saved bills");
        savedTitle.getStyleClass().add("section-title");

        TableView<Bill> table = new TableView<>(bills);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        addCol(table, "Bill", b -> b.id);
        addCol(table, "Guest", b -> b.guest);
        addCol(table, "Room", b -> b.room);
        addCol(table, "Nights", b -> String.valueOf(b.nights));
        addCol(table, "Total", b -> money(b.total));
        addCol(table, "Paid", b -> money(b.paid));
        addCol(table, "Balance", b -> money(b.balance));
        table.setMinHeight(120);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ---- Button actions ----
        calcBtn.setOnAction(e -> {
            Bill b = buildBill();
            if (b != null) tell(status, "Total: " + money(b.total) + "   Balance: " + money(b.balance), false);
        });

        saveBtn.setOnAction(e -> {
            Bill b = buildBill();
            if (b == null) return;
            counter++;
            b.id = String.format("B%03d", counter);
            bills.add(b);
            tell(status, "Saved " + b.id + ": Balance: " + money(b.balance), false);
        });

        newBtn.setOnAction(e -> {
            guestField.clear();
            nightsField.clear();
            extrasField.clear();
            paidField.clear();
            notesArea.clear();
            roomBox.setValue("Standard");
            status.setText("");
        });

        receiptBtn.setOnAction(e -> {
            Bill b = table.getSelectionModel().getSelectedItem();
            if (b == null) {
                tell(status, "Select a bill in the table first.", true);
                return;
            }
            String text = "Bill: " + b.id + "\nGuest: " + b.guest + "\nRoom: " + b.room
                    + "\nNights: " + b.nights + "\nTotal: " + money(b.total)
                    + "\nPaid: " + money(b.paid) + "\nBalance: " + money(b.balance);
            Alert a = new Alert(Alert.AlertType.INFORMATION, text);
            a.setHeaderText("Receipt");
            a.showAndWait();
        });

        deleteBtn.setOnAction(e -> {
            Bill b = table.getSelectionModel().getSelectedItem();
            if (b == null) {
                tell(status, "Select a bill in the table first.", true);
                return;
            }
            bills.remove(b);
            tell(status, "Deleted " + b.id, false);
        });

        // ---- LOG OUT ----
        logoutBtn.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to log out?");
            a.setHeaderText("Log out");
            Optional<ButtonType> result = a.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                currentUser = "";
                showLogin("You have been logged out.");
            }
        });

        // ---- Put everything together ----
        VBox center = new VBox(14, actions, formCard, savedTitle, table);
        center.setPadding(new Insets(18));
        center.getStyleClass().add("content-bg");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(center);
        showPage(root);
    }

    // ================= HELPERS =================
    private HBox priceRow(String room, String price) {
        Label name = new Label(room);
        name.getStyleClass().add("sidebar-text");
        Label amount = new Label(price);
        amount.getStyleClass().add("sidebar-price");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return new HBox(name, spacer, amount);
    }

    private VBox field(String labelText, Control input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(5, label, input);
    }

    // Reads the form, checks it, and does the maths. Returns null if something is wrong.
    private Bill buildBill() {
        String guest = guestField.getText().trim();
        if (guest.isEmpty()) {
            tell(status, "Please enter the guest name.", true);
            return null;
        }
        int nights;
        double extras, paid;
        try {
            nights = Integer.parseInt(nightsField.getText().trim());
            extras = extrasField.getText().isBlank() ? 0 : Double.parseDouble(extrasField.getText().trim());
            paid = paidField.getText().isBlank() ? 0 : Double.parseDouble(paidField.getText().trim());
        } catch (NumberFormatException ex) {
            tell(status, "Nights, extras and paid must be numbers.", true);
            return null;
        }
        if (nights < 1 || extras < 0 || paid < 0) {
            tell(status, "Nights must be 1 or more. Money cannot be negative.", true);
            return null;
        }

        String room = roomBox.getValue();
        double rate = switch (room) {
            case "Deluxe" -> 550;
            case "Family" -> 750;
            default -> 350;
        };
        double roomCharge = rate * nights;
        if (nights >= 5) roomCharge = roomCharge * 0.95;   // 5% discount

        Bill b = new Bill();
        b.guest = guest;
        b.room = room;
        b.nights = nights;
        b.total = roomCharge + extras;   // extras: no discount
        b.paid = paid;
        b.balance = b.total - paid;
        return b;
    }

    private void addCol(TableView<Bill> table, String name, Function<Bill, String> getter) {
        TableColumn<Bill, String> col = new TableColumn<>(name);
        col.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        table.getColumns().add(col);
    }

    public static void main(String[] args) {
        launch(args);
    }
}