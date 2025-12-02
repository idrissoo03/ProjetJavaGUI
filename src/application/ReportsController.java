package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;


import InventaireArticle.ArticleEpicerie;
import InventaireArticle.ArticlePerissable;
import InventaireArticle.Caisse;
import InventaireArticle.Inventaire;
import InventaireArticle.Vente;
import UtilisateurApplication.Administrateur;

public class ReportsController implements Initializable {

    // Report Type Selection
    @FXML private ToggleGroup reportTypeGroup;
    @FXML private RadioButton salesReportRadio;
    @FXML private RadioButton inventoryReportRadio;
    @FXML private RadioButton expiredReportRadio;
    @FXML private RadioButton lowStockReportRadio;

    // Date Filters
    @FXML private DatePicker startDateField;
    @FXML private DatePicker endDateField;

    // Generate Button
    @FXML private Button generateReportButton;

    // Report Results Section
    @FXML private VBox reportResultsSection;
    @FXML private Label reportTitle;

    // Sales Report Components
    @FXML private VBox salesReportContent;
    @FXML private Label totalSalesLabel;
    @FXML private Label transactionCountLabel;
    @FXML private Label averageSaleLabel;
    @FXML private TableView<Object> salesTable; // Changed to Object to handle different data types
    @FXML private TableColumn<Object, String> saleIdColumn;
    @FXML private TableColumn<Object, LocalDate> saleDateColumn;
    @FXML private TableColumn<Object, Integer> saleItemsColumn;
    @FXML private TableColumn<Object, Double> saleTotalColumn;

    // Inventory Report Components
    @FXML private VBox inventoryReportContent;
    @FXML private Label totalItemsLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label lowStockCountLabel;

    // Export Button
    @FXML private Button exportReportButton;

    // Back Button
    @FXML private Button backButton;

    private Inventaire inventaire;
    private Caisse caisse;
    private Administrateur admin;

    // Custom class for inventory items display
    public static class InventoryTableItem {
        private String id;
        private String name;
        private int quantity;
        private double price;
        private double totalValue;

        public InventoryTableItem(String id, String name, int quantity, double price, double totalValue) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.totalValue = totalValue;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotalValue() { return totalValue; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ReportsController initialized!");
        setupTableColumns();
        setupEventHandlers();
        initializeDateFilters();
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
        System.out.println("Inventaire set with " + (inventaire != null ? inventaire.getArticles().size() : 0) + " articles");
    }

    public void setCaisse(Caisse caisse) {
        this.caisse = caisse;
        System.out.println("Caisse set with " + (caisse != null ? caisse.getVentesJournalieres().size() : 0) + " sales");
    }

    private void setupTableColumns() {
        // Sales table columns - will be reused for different data types
    	saleIdColumn.setCellValueFactory(new PropertyValueFactory<>("idVente"));
    	saleDateColumn.setCellValueFactory(new PropertyValueFactory<>("date")); 
    	saleItemsColumn.setCellValueFactory(new PropertyValueFactory<>("nombreArticles"));
    	saleTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));


        // Format total column
        saleTotalColumn.setCellFactory(column -> new TableCell<Object, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f€", total));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });

        // Format items column
        saleItemsColumn.setCellFactory(column -> new TableCell<Object, Integer>() {
            @Override
            protected void updateItem(Integer items, boolean empty) {
                super.updateItem(items, empty);
                if (empty || items == null) {
                    setText(null);
                } else {
                    setText(items.toString());
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        // Format name/date column
        saleDateColumn.setCellFactory(column -> new TableCell<Object, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // Format LocalDate as string
                    setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        
    }
    

    private void setupEventHandlers() {
        generateReportButton.setOnAction(e -> generateReport());
        exportReportButton.setOnAction(e -> exportReport());
        backButton.setOnAction(e -> goBack());

        // Auto-generate report when report type changes
        reportTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                generateReport();
            }
        });
    }

    private void initializeDateFilters() {
        startDateField.setValue(LocalDate.now().minusDays(7)); // Default: last 7 days
        endDateField.setValue(LocalDate.now());
    }

    @FXML
    private void generateReport() {
        if (inventaire == null && caisse == null) {
            showError("Aucune donnée disponible pour générer le rapport!");
            return;
        }

        reportResultsSection.setVisible(true);
        
        if (salesReportRadio.isSelected()) {
            generateSalesReport();
        } else if (inventoryReportRadio.isSelected()) {
            generateInventoryReport();
        } else if (expiredReportRadio.isSelected()) {
            generateExpiredReport();
        } else if (lowStockReportRadio.isSelected()) {
            generateLowStockReport();
        }
    }

    private void generateSalesReport() {
        reportTitle.setText("Rapport des ventes");
        showReportSection(salesReportContent);
        updateTableColumnsForSales(); // Set columns for sales data

        if (caisse == null || caisse.getVentesJournalieres().isEmpty()) {
            showNoDataMessage("Aucune vente enregistrée");
            salesTable.setItems(FXCollections.observableArrayList());
            return;
        }

        LocalDate startDate = startDateField.getValue();
        LocalDate endDate = endDateField.getValue();

        if (startDate == null || endDate == null) {
            showError("Veuillez sélectionner une période valide");
            return;
        }

        List<Vente> ventes = caisse.getVentesJournalieres();
        ObservableList<Vente> filteredSales = FXCollections.observableArrayList();

        double totalSales = 0.0;
        int transactionCount = 0;

        for (Vente vente : ventes) {
            LocalDate saleDate = vente.getDate();

            if ((saleDate.isEqual(startDate) || saleDate.isAfter(startDate)) &&
                (saleDate.isEqual(endDate) || saleDate.isBefore(endDate))) {

                filteredSales.add(vente);
                totalSales += vente.getTotal();
                transactionCount++;
            }
        }

        double averageSale = transactionCount > 0 ? totalSales / transactionCount : 0.0;

        totalSalesLabel.setText(String.format("%.2f €", totalSales));
        transactionCountLabel.setText(String.valueOf(transactionCount));
        averageSaleLabel.setText(String.format("%.2f €", averageSale));

        salesTable.setItems(FXCollections.observableArrayList(filteredSales));
    }

    private void generateInventoryReport() {
        reportTitle.setText("Rapport d'inventaire");
        showReportSection(salesReportContent); // Use the same section with table
        updateTableColumnsForInventory(); // Set columns for inventory data

        if (inventaire == null) {
            showNoDataMessage("Aucun inventaire disponible");
            salesTable.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<Object> inventoryItems = FXCollections.observableArrayList();
        int totalItems = inventaire.getArticles().size();
        double totalValue = 0;
        int lowStockCount = 0;

        for (ArticleEpicerie article : inventaire.getArticles().values()) {
            double itemTotalValue = article.getPrix() * article.getQuantiteStock();
            inventoryItems.add(new InventoryTableItem(
                article.getId(),
                article.getNom(),
                article.getQuantiteStock(),
                article.getPrix(),
                itemTotalValue
            ));
            totalValue += itemTotalValue;
            if (article.getQuantiteStock() <= 10) {
                lowStockCount++;
            }
        }

        // Update statistics
        totalItemsLabel.setText(String.valueOf(totalItems));
        totalValueLabel.setText(String.format("%.2f€", totalValue));
        lowStockCountLabel.setText(String.valueOf(lowStockCount));

        // Set table data
        salesTable.setItems(inventoryItems);
    }

    @FXML
    private void generateExpiredReport() {
        reportTitle.setText("Articles périmés");
        showReportSection(salesReportContent); // Use the same section with table
        updateTableColumnsForInventory(); // Set columns for inventory data

        if (inventaire == null) {
            showNoDataMessage("Aucun inventaire disponible");
            salesTable.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<Object> expiredItems = FXCollections.observableArrayList();
        int expiredCount = 0;
        int expiringSoonCount = 0;
        double totalValue = 0;

        for (ArticleEpicerie article : inventaire.getArticles().values()) {
            if (article instanceof ArticlePerissable) {
                ArticlePerissable perissable = (ArticlePerissable) article;
                if (perissable.estPerime()) {
                    expiredCount++;
                    double itemValue = article.getPrix() * article.getQuantiteStock();
                    totalValue += itemValue;
                    expiredItems.add(new InventoryTableItem(
                        article.getId(),
                        article.getNom() + " (Expiré: " + perissable.getDateExpiration() + ")",
                        article.getQuantiteStock(),
                        article.getPrix(),
                        itemValue
                    ));
                } else if (perissable.getJoursRestants() <= 7) {
                    expiringSoonCount++;
                }
            }
        }

        // Update statistics for expired items
        totalSalesLabel.setText(String.valueOf(expiredCount));
        transactionCountLabel.setText(String.valueOf(expiringSoonCount));
        averageSaleLabel.setText(String.format("%.2f€", totalValue));

        // Set table data
        salesTable.setItems(expiredItems);
    }

    private void generateLowStockReport() {
        reportTitle.setText("Articles à faible stock");
        showReportSection(salesReportContent); // Use the same section with table
        updateTableColumnsForInventory(); // Set columns for inventory data

        if (inventaire == null) {
            showNoDataMessage("Aucun inventaire disponible");
            salesTable.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<Object> lowStockItems = FXCollections.observableArrayList();
        int lowStockCount = 0;
        int outOfStockCount = 0;
        double totalValueAtRisk = 0;

        for (ArticleEpicerie article : inventaire.getArticles().values()) {
            if (article.getQuantiteStock() == 0) {
                outOfStockCount++;
                double itemValue = article.getPrix() * article.getQuantiteStock();
                lowStockItems.add(new InventoryTableItem(
                    article.getId(),
                    article.getNom() + " (RUPTURE DE STOCK)",
                    article.getQuantiteStock(),
                    article.getPrix(),
                    itemValue
                ));
            } else if (article.getQuantiteStock() <= 10) {
                lowStockCount++;
                double itemValue = article.getPrix() * article.getQuantiteStock();
                totalValueAtRisk += itemValue;
                lowStockItems.add(new InventoryTableItem(
                    article.getId(),
                    article.getNom() + " (Stock faible: " + article.getQuantiteStock() + ")",
                    article.getQuantiteStock(),
                    article.getPrix(),
                    itemValue
                ));
            }
        }

        // Update statistics
        totalItemsLabel.setText(String.valueOf(lowStockCount));
        totalValueLabel.setText(String.format("%.2f€", totalValueAtRisk));
        lowStockCountLabel.setText(String.valueOf(outOfStockCount));

        // Set table data
        salesTable.setItems(lowStockItems);
    }

    private void updateTableColumnsForSales() {
        saleIdColumn.setText("ID Vente");
        saleDateColumn.setText("Date");
        saleItemsColumn.setText("Articles");
        saleTotalColumn.setText("Total");
    }

    private void updateTableColumnsForInventory() {
        saleIdColumn.setText("ID Article");
        saleDateColumn.setText("Nom Article");
        saleItemsColumn.setText("Quantité");
        saleTotalColumn.setText("Valeur Totale");
    }

    private void showReportSection(VBox sectionToShow) {
        // Hide all report sections
        salesReportContent.setVisible(false);
        inventoryReportContent.setVisible(false);

        // Show the selected section
        sectionToShow.setVisible(true);
    }

    private void showNoDataMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aucune donnée");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void exportReport() {
        if (salesTable.getItems().isEmpty()) {
            showError("Aucune donnée à exporter !");
            return;
        }

        // Choose file location using FileChooser
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Exporter le rapport en CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = fileChooser.showSaveDialog(exportReportButton.getScene().getWindow());

        if (file == null) {
            return; // User cancelled
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // Write header based on selected report type
            if (salesReportRadio.isSelected()) {
                writer.println("ID Vente,Date,Nombre Articles,Total");
                for (Object obj : salesTable.getItems()) {
                    if (obj instanceof Vente vente) {
                        String line = String.format("%s,%s,%d,%.2f",
                                vente.getIdVente(),
                                vente.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                vente.getNombreArticles(),
                                vente.getTotal());
                        writer.println(line);
                    }
                }
            } else { // Inventory, expired, low stock reports
                writer.println("ID Article,Nom,Quantité,Prix,Valeur Totale");
                for (Object obj : salesTable.getItems()) {
                    if (obj instanceof InventoryTableItem item) {
                        String line = String.format("%s,%s,%d,%.2f,%.2f",
                                item.getId(),
                                item.getName(),
                                item.getQuantity(),
                                item.getPrice(),
                                item.getTotalValue());
                        writer.println(line);
                    }
                }
            }

            showInformation("Export réussi", "Rapport exporté avec succès :\n" + file.getAbsolutePath());

        } catch (Exception e) {
            showError("Erreur lors de l'exportation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    

    @FXML
    private void goBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public void setAdmin(Administrateur admin) {
        this.admin = admin;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}