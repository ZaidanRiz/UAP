package com.example.program;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Kelas utama aplikasi yang meluncurkan aplikasi JavaFX dan menginisialisasi manajer menu.
 */
public class PemesananMakananApp extends Application {
    private static final int WINDOW_WIDTH = 830;
    private static final int WINDOW_HEIGHT = 600;
    private static final int MAX_NAME_LENGTH = 10;
    private static final double MAX_PAYMENT_AMOUNT = 1000000.0;

    /**
     * Metode utama yang dipanggil saat aplikasi diluncurkan.
     *
     * @param args Argumen baris perintah.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Metode yang dipanggil saat aplikasi diinisialisasi. Membuat objek MenuManager dan menampilkan antarmuka pengguna.
     *
     * @param primaryStage Objek Stage utama aplikasi.
     */
    @Override
    public void start(Stage primaryStage) {
        MenuManager menuManager = new MenuManager(primaryStage);
        menuManager.init();

        Scene scene = new Scene(menuManager.createLayout(), WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistem Pemesanan Menu");

        primaryStage.show();
    }

    /**
     * Kelas MenuManager mengelola menu, pesanan, dan antarmuka pengguna aplikasi.
     */
    public static class MenuManager {
        private final Stage primaryStage;
        private final Order order;
        private final ListView<String> displayListView;
        private final ComboBox<String> categoryComboBox;
        private final ComboBox<String> quantityComboBox;
        private final TextArea orderDetailsTextArea;
        private final Label priceLabel;
        private final TextField customerNameField;
        private static final int MAX_QUANTITY_PER_ITEM = 10;
        private int currentQuantity = 1; // Variabel untuk menyimpan jumlah pesanan saat ini


        public MenuManager(Stage primaryStage) {
            this.primaryStage = primaryStage;
            this.order = new Order();
            this.displayListView = new ListView<>();
            this.categoryComboBox = new ComboBox<>();
            this.quantityComboBox = new ComboBox<>();
            this.orderDetailsTextArea = new TextArea();
            this.priceLabel = new Label("Harga: ");
            this.customerNameField = new TextField();
            this.paymentField = new TextField();
        }

        /**
         * Menginisialisasi aplikasi dengan membuat tata letak dan menampilkan menu.
         */
        public void init() {
            BorderPane root = createLayout();
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Sistem Pemesanan Menu");
            primaryStage.show();

            displayMenu();
        }

        /**
         * Membuat tata letak utama aplikasi menggunakan BorderPane.
         *
         * @return Tata letak utama aplikasi.
         */
        public BorderPane createLayout() {
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));

            Label titleLabel = new Label("RESTORAN PELOPOR");
            titleLabel.setFont(new Font("Arial", 24));
            BorderPane.setAlignment(titleLabel, Pos.CENTER);
            root.setTop(titleLabel);

            root.setTop(createTopBox());
            root.setCenter(createCenterBox());
            root.setBottom(createBottomBox());
            root.setRight(createReceiptBox());

            return root;
        }
        private void saveOrderToFile() {
            String enteredName = customerNameField.getText().trim();
            if (isNameValid(enteredName)) {
                order.setCustomerName(enteredName);

                double totalPrice = calculateTotalPrice();
                double paymentAmount = totalPrice; // Anggap total harga sebagai jumlah pembayaran
                double change = 0; // Kembalian belum relevan saat menyimpan pesanan

                StringBuilder orderDetails = new StringBuilder("Nama Pemesan: " + order.getCustomerName() + "\n");
                for (Map.Entry<Menu, Integer> entry : order.getItems().entrySet()) {
                    Menu item = entry.getKey();
                    int quantity = entry.getValue();
                    double itemPrice = item.getPrice() * quantity;
                    orderDetails.append(item.getName()).append(" x").append(quantity)
                            .append(" - ").append(itemPrice).append("\n");
                }
                orderDetails.append("Total: ").append(totalPrice);

                // Tambahkan informasi hasil bayar dan kembalian ke dalam pesanan yang disimpan ke file
                orderDetails.append("\n\nTotal Bayar: ").append(paymentAmount);
                orderDetails.append("\nKembalian: ").append(change);

                String fileName = "order_details.txt";
                File file = new File(fileName);

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(orderDetails.toString());
                    System.out.println("Pesanan berhasil disimpan ke dalam file: " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Nama pemesan tidak boleh mengandung angka.");
            }
        }

        private boolean isNameValid(String name) {
            return !name.matches(".*\\d.*");
        }
        public static void main(String[] args) {
            launch(args);
        }

        private void decreaseQuantity() {
            if (currentQuantity > 1) {
                currentQuantity--;
                quantityComboBox.setValue(String.valueOf(currentQuantity));
            }
        }

        private void increaseQuantity() {
            currentQuantity++;
            quantityComboBox.setValue(String.valueOf(currentQuantity));
        }

        private void printReceiptToFile() {
            String enteredName = customerNameField.getText().trim();
            order.setCustomerName(enteredName);

            double totalPrice = 0;
            for (Map.Entry<Menu, Integer> entry : order.getItems().entrySet()) {
                Menu item = entry.getKey();
                int quantity = entry.getValue();
                double itemPrice = item.getPrice() * quantity;
                totalPrice += itemPrice;
            }

            double paymentAmount = totalPrice;
            double change = 0;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Cetak Struk ke File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    if (!paymentField.getText().isEmpty()) {
                        paymentAmount = Double.parseDouble(paymentField.getText());
                        if (paymentAmount >= totalPrice) {
                            change = paymentAmount - totalPrice;
                        } else {
                            System.out.println("Pembayaran tidak mencukupi.");
                            return;
                        }
                    }

                    StringBuilder orderDetails = new StringBuilder("Nama Pemesan: " + order.getCustomerName() + "\n");
                    for (Map.Entry<Menu, Integer> entry : order.getItems().entrySet()) {
                        Menu item = entry.getKey();
                        int quantity = entry.getValue();
                        double itemPrice = item.getPrice() * quantity;
                        orderDetails.append(item.getName()).append(" x").append(quantity)
                                .append(" - ").append(itemPrice).append("\n");
                    }
                    orderDetails.append("Total: ").append(totalPrice);

                    // Update orderDetails with payment and change
                    orderDetails.append("\n\nTotal Bayar: ").append(paymentAmount);
                    orderDetails.append("\nKembalian: ").append(change);

                    writer.write(orderDetails.toString());
                    System.out.println("Struk berhasil dicetak ke dalam file: " + file.getAbsolutePath());
                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }


        private final TextField paymentField;



        private VBox createTopBox() {
            VBox topBox = new VBox(10);
            topBox.setAlignment(Pos.CENTER_LEFT);

            categoryComboBox.setPromptText("Pilih Kategori");
            categoryComboBox.setOnAction(e -> displayCategory(categoryComboBox.getValue()));

            customerNameField.setPromptText("Nama Pemesan");
            customerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Batasan karakter pada nama pemesan menjadi maksimal 10
                if (newValue.length() > MAX_NAME_LENGTH) {
                    customerNameField.setText(oldValue);
                    displayWarning("Nama pemesan tidak boleh lebih dari 10 karakter.");
                }
            });

            // Menambahkan event handler untuk tombol Enter
            customerNameField.setOnAction(event -> {
                String enteredName = customerNameField.getText().trim();
                if (isNameValid(enteredName)) {
                    order.setCustomerName(enteredName);
                    saveOrderToFile(); // Panggil method untuk menyimpan ke file
                } else {
                    displayWarning("Nama pemesan tidak boleh mengandung angka.");
                }
            });

            topBox.getChildren().addAll(categoryComboBox, customerNameField);

            return topBox;
        }

        private VBox createCenterBox() {
            VBox centerBox = new VBox(10);
            centerBox.setAlignment(Pos.CENTER);

            displayListView.setPrefHeight(200);
            centerBox.getChildren().addAll(displayListView);

            Button removeButton = new Button("Hapus Pesanan");
            removeButton.setOnAction(e -> removeOrder());
            centerBox.getChildren().add(removeButton);

            Button clearButton = new Button("Clear");
            clearButton.setOnAction(e -> clearOrder());
            centerBox.getChildren().add(clearButton);

            Button orderButton = new Button("Pesan");
            orderButton.setOnAction(e -> {
                String selectedItem = displayListView.getSelectionModel().getSelectedItem();
                String selectedCategory = categoryComboBox.getValue();
                int quantity = Integer.parseInt(quantityComboBox.getValue());

                if (selectedItem != null && selectedCategory != null) {
                    String itemName = selectedItem.split(" - ")[0];
                    Menu foundItem = findItemByName(itemName, selectedCategory);
                    if (foundItem != null) {
                        addToOrder(foundItem, quantity);
                    }
                }
            });
            centerBox.getChildren().add(orderButton);

            return centerBox;
        }

        private void clearOrder() {
            order.getItems().clear();
            updateOrderDetails();
        }

        private HBox createBottomBox() {
            HBox bottomBox = new HBox(10);
            bottomBox.setAlignment(Pos.CENTER_LEFT);

            Button minusButton = new Button("-");
            minusButton.setOnAction(e -> decreaseQuantity());

            quantityComboBox.setEditable(true);
            quantityComboBox.setValue(String.valueOf(currentQuantity));
            quantityComboBox.setPrefWidth(40);

            Button plusButton = new Button("+");
            plusButton.setOnAction(e -> increaseQuantity());

            bottomBox.getChildren().addAll(minusButton, quantityComboBox, plusButton);
            return bottomBox;
        }

        private VBox createReceiptBox() {
            VBox rightBox = new VBox(10);
            rightBox.setAlignment(Pos.CENTER);

            orderDetailsTextArea.setEditable(false);
            orderDetailsTextArea.setPrefHeight(150);

            priceLabel.setStyle("-fx-font-size: 14;");

            TextField paymentField = new TextField();
            paymentField.setPromptText("Jumlah Uang");

            Button payButton = new Button("Bayar");
            payButton.setOnAction(e -> {
                String paymentText = paymentField.getText();
                try {
                    double paymentAmount = Double.parseDouble(paymentText);

                    // Validasi: Jumlah uang tidak boleh lebih dari 100.000
                    if (paymentAmount > MAX_PAYMENT_AMOUNT) {
                        displayWarning("Jumlah uang tidak boleh lebih dari 100.000.000");
                        return;
                    }

                    reduceTotalFromPayment(paymentAmount);
                } catch (NumberFormatException ex) {
                    displayWarning("Masukkan jumlah uang yang valid.");
                }

            });
            Button openReceiptButton = new Button("Buka Struk");
            openReceiptButton.setOnAction(e -> openReceiptFromFile());

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER); // Menempatkan tombol-tombol ke tengah

            Button printButton = new Button("Cetak Struk");
            printButton.setOnAction(e -> saveOrderToFile());

            Button printToFileButton = new Button("Cetak Struk ke File");
            printToFileButton.setOnAction(e -> printReceiptToFile());

            buttonBox.getChildren().addAll(printButton, printToFileButton, openReceiptButton);
            rightBox.getChildren().addAll(orderDetailsTextArea, priceLabel, paymentField, payButton, buttonBox);
            return rightBox;
        }

        /**
         * Membuka struk dari file dan menampilkannya di area rincian pesanan.
         */
        private void openReceiptFromFile() {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Buka Struk dari File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    Scanner scanner = new Scanner(file);
                    StringBuilder receiptContent = new StringBuilder();

                    while (scanner.hasNext()) {
                        receiptContent.append(scanner.nextLine()).append("\n");
                    }

                    orderDetailsTextArea.setText(receiptContent.toString());
                    displayInfo("Struk berhasil dibuka dari file: " + file.getAbsolutePath());

                    scanner.close();
                } catch (IOException e) {
                    displayWarning("Gagal membuka file struk.");
                }
            }
        }

        /**
         * Menampilkan peringatan informasi dengan pesan yang diberikan.
         *
         * @param message Pesan yang akan ditampilkan dalam peringatan.
         */
        private void displayInfo(String message) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Informasi");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
        private void reduceTotalFromPayment(double paymentAmount) {
            double totalPrice = calculateTotalPrice();
            if (paymentAmount >= totalPrice) {
                double change = paymentAmount - totalPrice;

                // Tampilkan hasil bayar dan kembalian di dalam area pesanan
                String paymentResult = "Total Bayar: " + paymentAmount;
                String changeResult = "Kembalian: " + change;
                orderDetailsTextArea.appendText("\n\n" + paymentResult + "\n" + changeResult);

                // Reset pesanan

            } else {
                displayWarning("Pembayaran tidak mencukupi. Jumlah uang yang dimasukkan kurang dari total harga.");
            }
        }

        private double calculateTotalPrice() {
            double totalPrice = 0;
            for (Map.Entry<Menu, Integer> entry : order.getItems().entrySet()) {
                Menu item = entry.getKey();
                int quantity = entry.getValue();
                double itemPrice = item.getPrice() * quantity;
                totalPrice += itemPrice;
            }
            return totalPrice;
        }


        private void updateOrderDetails() {
            StringBuilder orderDetails = new StringBuilder("Pesanan:\n");
            double totalPrice = 0;
            for (Map.Entry<Menu, Integer> entry : order.getItems().entrySet()) {
                Menu item = entry.getKey();
                int quantity = entry.getValue();
                double itemPrice = item.getPrice() * quantity;
                totalPrice += itemPrice;
                orderDetails.append(item.getName()).append(" x").append(quantity)
                        .append(" - ").append(itemPrice).append("\n");
            }
            orderDetails.append("Total: ").append(totalPrice);
            orderDetailsTextArea.setText(orderDetails.toString());
            priceLabel.setText("Harga: " + totalPrice);
        }

        private void addToOrder(Menu selectedMenu, int quantity) {
            if (quantity > 0 && quantity <= MAX_QUANTITY_PER_ITEM) {
                order.addItem(selectedMenu, quantity);
                updateOrderDetails();
            } else {
                displayWarning("Jumlah pesanan per item maksimal adalah " + MAX_QUANTITY_PER_ITEM);
            }
        }

        private void displayWarning(String message) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private void displayCategory(String categoryName) {
            Category selectedCategory = getCategoryByName(categoryName);
            if (selectedCategory != null) {
                displayListView.getItems().clear();
                for (Menu item : selectedCategory.getItems()) {
                    if (item != null) {
                        displayListView.getItems().add(item.toString() + " - " + item.getPrice());
                    }
                }
            }
        }

        private void displayMenu() {
            Category makananUtama = new Category("Makanan Utama", Arrays.asList(
                    new MenuItem("Nasi Goreng", 30000.0),
                    new MenuItem("Mie Goreng", 25000.0)
            ));

            Category makananPembuka = new Category("Makanan Pembuka", Arrays.asList(
                    new MenuItem("Sate Ayam", 15000.0),
                    new MenuItem("Martabak", 10000.0)
            ));

            Category desserts = new Category("Desserts", Arrays.asList(
                    new MenuItem("Dawet", 15000.0),
                    new MenuItem("Dadar Gulung", 10000.0)
            ));

            categoryComboBox.getItems().addAll(
                    makananUtama.getName(),
                    makananPembuka.getName(),
                    desserts.getName()
            );
        }

        private void removeOrder() {
            String selectedItem = displayListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String itemName = selectedItem.split(" - ")[0];
                for (Map.Entry<Menu, Integer> entry : order.getItems().entrySet()) {
                    Menu item = entry.getKey();
                    if (item.getName().equals(itemName)) {
                        int currentQuantity = entry.getValue();
                        if (currentQuantity > 1) {
                            order.getItems().put(item, currentQuantity - 1); // Mengurangi jumlah pesanan
                        } else {
                            order.getItems().remove(item); // Menghapus pesanan jika jumlahnya hanya 1
                        }
                        updateOrderDetails();
                        break;
                    }
                }
            }
        }


        private Menu findItemByName(String itemName, String categoryName) {
            Category selectedCategory = getCategoryByName(categoryName);
            if (selectedCategory != null) {
                for (Menu item : selectedCategory.getItems()) {
                    if (item.getName().equals(itemName)) {
                        return item;
                    }
                }
            }
            return null;
        }

        private Category getCategoryByName(String categoryName) {
            switch (categoryName) {
                case "Makanan Utama":
                    return new Category("Makanan Utama", Arrays.asList(
                            new MenuItem("Nasi Goreng", 30000.0),
                            new MenuItem("Mie Goreng", 25000.0)
                    ));
                case "Makanan Pembuka":
                    return new Category("Makanan Pembuka", Arrays.asList(
                            new MenuItem("Sate Ayam", 15000.0),
                            new MenuItem("Martabak", 10000.0)
                    ));
                case "Desserts":
                    return new Category("Desserts", Arrays.asList(
                            new MenuItem("Dawet", 15000.0),
                            new MenuItem("Dadar Gulung", 10000.0)
                    ));
                default:
                    return null;
            }
        }
    }

    /**
     * Kelas Order mewakili pesanan pelanggan, termasuk item-menu yang dipilih dan nama pelanggan.
     */
    public static class Order {
        private final Map<Menu, Integer> items;
        private String customerName;

        public Order() {
            this.items = new HashMap<>();
        }

        public void addItem(Menu item, int quantity) {
            if (item != null && quantity > 0) {
                items.put(item, items.getOrDefault(item, 0) + quantity);
            }
        }

        public Map<Menu, Integer> getItems() {
            return items;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
    }

    /**
     * Antarmuka Menu mendefinisikan struktur item menu dengan nama dan harga.
     */
    public interface Menu {
        String getName();

        double getPrice();
    }

    /**
     * Kelas Category mewakili kategori menu yang berisi daftar item menu.
     */
    public static class Category {
        private final String name;
        private final List<Menu> items;

        public Category(String name, List<Menu> items) {
            this.name = name;
            this.items = new ArrayList<>(items);
        }

        public void addItem(Menu item) {
            if (item != null) {
                items.add(item);
            }
        }

        public List<Menu> getItems() {
            return Collections.unmodifiableList(items);
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Kelas MenuItem mewakili item menu spesifik dengan nama dan harga.
     */
    public static class MenuItem implements Menu {
        private final String name;
        private final double price;

        public MenuItem(String name, double price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name;

        }
    }
}
