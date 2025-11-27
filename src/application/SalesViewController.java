package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import InventaireArticle.Caisse;
import InventaireArticle.Vente;
import InventaireArticle.LigneVente;   // ← ADD THIS LINE


public class SalesViewController implements Initializable {

    @FXML private TableView<Vente> salesTable;
    @FXML private TableColumn<Vente, String> saleIdColumn;
    @FXML private TableColumn<Vente, String> saleDateColumn;
    @FXML private TableColumn<Vente, Integer> saleItemsColumn;
    @FXML private TableColumn<Vente, Double> saleTotalColumn;
    @FXML private TableColumn<Vente, String> saleDetailsColumn;

    @FXML private TableView<TopProduct> topProductsTable;
    @FXML private TableColumn<TopProduct, String> productNameColumn;
    @FXML private TableColumn<TopProduct, Integer> quantitySoldColumn;
    @FXML private TableColumn<TopProduct, Double> revenueColumn;

    @FXML private Label summaryLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label transactionCountLabel;
    @FXML private Label averageSaleLabel;
    @FXML private Label totalItemsSoldLabel;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button applyDateFilterButton;
    @FXML private Button todayButton;
    @FXML private Button exportButton;
    @FXML private Button printButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private Caisse caisse;
    private ObservableList<Vente> allSales;
    private ObservableList<Vente> filteredSales;
    private ObservableList<TopProduct> topProducts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupDateFilters();
    }

    public void setCaisse(Caisse caisse) {
        this.caisse = caisse;
        loadSalesData();
    }

    private void setupTableColumns() {
        // Format sales table columns
        saleTotalColumn.setCellFactory(column -> new TableCell<Vente, Double>() {
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

        saleDetailsColumn.setCellFactory(column -> new TableCell<Vente, String>() {
            private final Button detailsButton = new Button("📋 Détails");

            {
                detailsButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                detailsButton.setOnAction(e -> {
                    Vente vente = getTableView().getItems().get(getIndex());
                    if (vente != null) {
                        showSaleDetails(vente);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });

        // Format top products table
        revenueColumn.setCellFactory(column -> new TableCell<TopProduct, Double>() {
            @Override
            protected void updateItem(Double revenue, boolean empty) {
                super.updateItem(revenue, empty);
                if (empty || revenue == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f€", revenue));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });

        quantitySoldColumn.setCellFactory(column -> new TableCell<TopProduct, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    setText(quantity.toString());
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3182ce;");
                }
            }
        });
    }

    private void setupDateFilters() {
        // Set default dates to today
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        applyDateFilterButton.setOnAction(e -> applyDateFilter());
        todayButton.setOnAction(e -> setTodayFilter());

        // Auto-apply filter when dates change
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
    }

    private void loadSalesData() {
        if (caisse == null) return;

        // Get all sales (you might need to modify Caisse class to get all sales, not just daily)
        List<Vente> ventes = caisse.getVentesJournalieres(); // This might need to be changed to getAllVentes()
        allSales = FXCollections.observableArrayList(ventes);
        filteredSales = FXCollections.observableArrayList(allSales);
        
        salesTable.setItems(filteredSales);
        calculateTopProducts();
        updateStatistics();
        updateSummary();
    }
    @FXML
    private void applyDateFilter() {
        if (allSales == null || startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            return;
        }

        filteredSales.clear();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        for (Vente vente : allSales) {
            LocalDate saleDate = vente.getDate();  // Assuming getDate() returns LocalDate

            // No parsing needed!
            if (!saleDate.isBefore(startDate) && !saleDate.isAfter(endDate)) {
                filteredSales.add(vente);
            }
        }

        calculateTopProducts();
        updateStatistics();
        updateSummary();
    }
    @FXML
    private void setTodayFilter() {
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        applyDateFilter();
    }

    private void calculateTopProducts() {
        if (filteredSales == null) return;

        Map<String, TopProduct> productSales = new HashMap<>();

        for (Vente vente : filteredSales) {
            for (LigneVente ligne : vente.getArticles()) {
                String productName = ligne.getArticle().getNom();
                int quantity = ligne.getQuantite();
                double revenue = ligne.getArticle().getPrix() * quantity;

                TopProduct topProduct = productSales.getOrDefault(productName, 
                    new TopProduct(productName, 0, 0.0));
                
                topProduct.setQuantiteVendue(topProduct.getQuantiteVendue() + quantity);
                topProduct.setChiffreAffaires(topProduct.getChiffreAffaires() + revenue);
                
                productSales.put(productName, topProduct);
            }
        }

        // Convert to list and sort by revenue descending
        List<TopProduct> topProductsList = new ArrayList<>(productSales.values());
        topProductsList.sort((p1, p2) -> Double.compare(p2.getChiffreAffaires(), p1.getChiffreAffaires()));

        // Take top 10 products
        topProducts = FXCollections.observableArrayList(
            topProductsList.subList(0, Math.min(topProductsList.size(), 10))
        );
        
        topProductsTable.setItems(topProducts);
    }

    private void updateStatistics() {
        if (filteredSales == null) return;

        double totalSales = 0;
        int transactionCount = filteredSales.size();
        int totalItemsSold = 0;

        for (Vente vente : filteredSales) {
            totalSales += vente.getTotal();
            totalItemsSold += vente.getArticles().size();
        }

        double averageSale = transactionCount > 0 ? totalSales / transactionCount : 0;

        totalSalesLabel.setText(String.format("%.2f€", totalSales));
        transactionCountLabel.setText(String.valueOf(transactionCount));
        averageSaleLabel.setText(String.format("%.2f€", averageSale));
        totalItemsSoldLabel.setText(String.valueOf(totalItemsSold));
    }

    private void updateSummary() {
        if (filteredSales == null) return;
        
        int total = filteredSales.size();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate.equals(endDate)) {
            summaryLabel.setText(total + " vente(s) le " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            summaryLabel.setText(total + " vente(s) du " + 
                startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " au " +
                endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    private void showSaleDetails(Vente vente) {
        StringBuilder details = new StringBuilder();
        details.append("=== FACTURE DÉTAILLÉE ===\n");
        details.append("ID Vente: ").append(vente.getIdVente()).append("\n");
        details.append("Date: ").append(vente.getDate()).append("\n\n");
        details.append("ARTICLES VENDUS:\n");
        
        for (LigneVente ligne : vente.getArticles()) {
            details.append(String.format("- %s x%d: %.2f€\n", 
                ligne.getArticle().getNom(), 
                ligne.getQuantite(), 
                ligne.getArticle().getPrix() * ligne.getQuantite()));
        }
        
        details.append("\nSOUS-TOTAL: ").append(String.format("%.2f€", vente.getTotal())).append("\n");
        details.append("TVA (20%): ").append(String.format("%.2f€", vente.getTotal() * 0.20)).append("\n");
        details.append("TOTAL: ").append(String.format("%.2f€", vente.getTotal() * 1.20)).append("\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la vente");
        alert.setHeaderText("Facture #" + vente.getIdVente());
        alert.setContentText(details.toString());
        alert.setWidth(400);
        alert.showAndWait();
    }

    @FXML
    private void handleExportButton() {
        showInformation("Export", "Fonctionnalité d'export des ventes à implémenter");
    }

    @FXML
    private void handlePrintButton() {
        showInformation("Impression", "Fonctionnalité d'impression à implémenter");
    }

    @FXML
    private void handleRefreshButton() {
        loadSalesData();
        showSuccess("Données des ventes actualisées");
    }

    @FXML
    private void handleBackButton() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for top products
    public static class TopProduct {
        private final String nom;
        private int quantiteVendue;
        private double chiffreAffaires;

        public TopProduct(String nom, int quantiteVendue, double chiffreAffaires) {
            this.nom = nom;
            this.quantiteVendue = quantiteVendue;
            this.chiffreAffaires = chiffreAffaires;
        }

        public String getNom() { return nom; }
        public int getQuantiteVendue() { return quantiteVendue; }
        public double getChiffreAffaires() { return chiffreAffaires; }
        
        public void setQuantiteVendue(int quantiteVendue) { this.quantiteVendue = quantiteVendue; }
        public void setChiffreAffaires(double chiffreAffaires) { this.chiffreAffaires = chiffreAffaires; }
    }
}