/**
 * Copyright (C) 2014-2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.csv;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.application.AppLocale;
import org.jevis.application.resource.ResourceLoader;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.NumberSpinner;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVImportDialog {

    public static String ICON = "1403727005_gnome-mime-application-vnd.lotus-1-2-3.png";

    private String _encloser = "";
    private String _seperator = "";

    private double LEFT_PADDING = 30;

    final Button ok = new Button(I18n.getInstance().getString("csv.ok"));
    final Button automatic = new Button(I18n.getInstance().getString("csv.automatic"));//, JEConfig.getImage("1403018303_Refresh.png", 15, 15));
    final Button fileButton = new Button(I18n.getInstance().getString("csv.file_select"));
    final Button saveFormat = new Button(I18n.getInstance().getString("csv.save_formate"));
    Button reload = new Button(I18n.getInstance().getString("csv.reload"));//, JEConfig.getImage("1403018303_Refresh.png", 20, 20));
    final NumberSpinner count = new NumberSpinner(BigDecimal.valueOf(0), BigDecimal.valueOf(1));

    RadioButton tab = new RadioButton(I18n.getInstance().getString("csv.seperators.tab"));
    RadioButton semicolon = new RadioButton(I18n.getInstance().getString("csv.seperators.semi"));
    RadioButton comma = new RadioButton(I18n.getInstance().getString("csv.seperators.comma"));
    RadioButton space = new RadioButton(I18n.getInstance().getString("csv.seperators.space"));
    RadioButton otherLineSep = new RadioButton(I18n.getInstance().getString("csv.seperators.other"));
    final ToggleGroup sepGroup = new ToggleGroup();

    RadioButton apostrop = new RadioButton(I18n.getInstance().getString("csv.enclosed.apostrophe"));
    RadioButton ditto = new RadioButton(I18n.getInstance().getString("csv.enclosed.ditto"));
    RadioButton enc2 = new RadioButton(I18n.getInstance().getString("csv.enclosed.gravis"));
    RadioButton none = new RadioButton(I18n.getInstance().getString("csv.enclosed.none"));
    RadioButton otherTextSep = new RadioButton(I18n.getInstance().getString("csv.enclosed.other"));

    final ToggleGroup textDiGroup = new ToggleGroup();
    ObservableList<String> formatOptions;

    final VBox tableRootPane = new VBox(10);

    TextField otherColumnF = new TextField();
    TextField otherTextF = new TextField();
    private File _csvFile;
    private JEVisDataSource _ds;
    private CSVTable table;
    private Stage stage;
    private Charset charset = Charset.defaultCharset();

    public Response show(Stage owner, JEVisDataSource ds) {
        stage = new Stage();
        _ds = ds;

        stage.setTitle(I18n.getInstance().getString("csv.title"));
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
//        stage.setWidth(1024);
//        stage.setHeight(768);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
//        scene.setCursor(Cursor.DEFAULT);

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(I18n.getInstance().getString("csv.top_title"));
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        ImageView imageView = ResourceLoader.getImage(ICON, 64, 64);

        stage.getIcons().add(imageView.getImage());

        VBox vboxLeft = new VBox();
        VBox vboxRight = new VBox();
        vboxLeft.getChildren().add(topTitle);
        vboxLeft.setAlignment(Pos.CENTER_LEFT);
        vboxRight.setAlignment(Pos.CENTER_LEFT);
        vboxRight.getChildren().add(imageView);

        header.setLeft(vboxLeft);

        header.setRight(vboxRight);

        HBox buttonPanel = new HBox(8);

        ok.setDefaultButton(true);
//        ok.setDisable(true);

        saveFormat.setDisable(true);//Disabled as long its not working

        Button cancel = new Button(I18n.getInstance().getString("csv.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().setAll(ok, cancel);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(5));

        GridPane gp = new GridPane();
//        gp.setPadding(new Insets(10));
//        gp.setHgap(10);
//        gp.setVgap(5);

        //check allowed
        int x = 0;

        Node filePane = buildFileOptions();
        Node seperatorPane = buildSeparatorPane();
        Node tablePane = buildTablePane();

        gp.add(filePane, 0, 0);
        gp.add(seperatorPane, 0, 1);
        gp.add(tablePane, 0, 2);

        GridPane.setVgrow(filePane, Priority.NEVER);
        GridPane.setVgrow(seperatorPane, Priority.NEVER);
        GridPane.setVgrow(tablePane, Priority.ALWAYS);

        VBox content = new VBox(10);

        content.getChildren().setAll(
                buildTitle(I18n.getInstance().getString("csv.tab.title.file_options")), filePane,
                buildTitle(I18n.getInstance().getString("csv.tab.title.seperator_options")), seperatorPane,
                buildTitle(I18n.getInstance().getString("csv.tab.title.field_options")), tablePane);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);
        Region spacer = new Region();

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), content, spacer, buttonPanel);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(header, Priority.NEVER);

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;
            }
        });

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (table.doImport()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(I18n.getInstance().getString("csv.export.dialog.success.header"));
                    alert.setHeaderText(null);
                    alert.setContentText(I18n.getInstance().getString("csv.export.dialog.success.message"));
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(I18n.getInstance().getString("csv.export.dialog.failed.title"));
                    alert.setHeaderText(null);
                    alert.setContentText(I18n.getInstance().getString("ccsv.export.dialog.failed.message"));
                    alert.showAndWait();
                }
            }
        });

        stage.sizeToScene();
        stage.showAndWait();

        return response;
    }

    private void updateTree() {
//        System.out.println("UpdateTree");
        if (_csvFile != null) {
            Platform.runLater(() -> {
                final CSVParser parser = parseCSV();
                table = new CSVTable(_ds, parser);
                tableRootPane.getChildren().setAll(table);
                tableRootPane.heightProperty().addListener(new ChangeListener<Number>() {

                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                        if (table != null) {
                            table.setPrefHeight(t1.doubleValue());
                        }
                    }
                });
                VBox.setVgrow(table, Priority.ALWAYS);
            });

        }

    }

    private Response response = Response.CANCEL;

    private Node buildTablePane() {

        tableRootPane.setPadding(new Insets(10, 10, 5, LEFT_PADDING));

        TableView placeholderTree = new TableView();
        TableColumn firstNameCol = new TableColumn(I18n.getInstance().getString("csv.table.first_col"));
        TableColumn lastNameCol = new TableColumn(I18n.getInstance().getString("csv.table.second_col"));
        firstNameCol.prefWidthProperty().bind(placeholderTree.widthProperty().multiply(0.5));
        lastNameCol.prefWidthProperty().bind(placeholderTree.widthProperty().multiply(0.5));
        placeholderTree.getColumns().addAll(firstNameCol, lastNameCol);

        tableRootPane.getChildren().setAll(placeholderTree);

        return tableRootPane;
    }

    private CSVParser parseCSV() {
        CSVParser parser = new CSVParser(_csvFile, getEncloser(), getSeperator(), getStartLine(), charset);
        return parser;
    }

    private Node buildFileOptions() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(0, 10, 10, LEFT_PADDING));
//        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(5);

//        gp.setStyle("-fx-background-color: #EBED50;");
        Label fileL = new Label(I18n.getInstance().getString("csv.file"));
        Label formatL = new Label(I18n.getInstance().getString("csv.format"));
        Label charSetL = new Label(I18n.getInstance().getString("csv.charset"));
        Label fromRow = new Label(I18n.getInstance().getString("csv.from_rowFrom"));
        final Label fileNameL = new Label();

        ObservableList<Charset> options = FXCollections.observableArrayList();
        for (Charset set : Charset.availableCharsets().values()) {
            options.add(set);
        }

        Callback<ListView<Charset>, ListCell<Charset>> cellFactory = new Callback<ListView<Charset>, ListCell<Charset>>() {
            @Override
            public ListCell<Charset> call(ListView<Charset> param) {
                final ListCell<Charset> cell = new ListCell<Charset>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(Charset item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.displayName(AppLocale.getInstance().getLocale()));
                            setGraphic(null);

                        }
                    }
                };
                return cell;
            }
        };

        final ComboBox<Charset> charsetBox = new ComboBox<Charset>(options);
        charsetBox.setCellFactory(cellFactory);
        charsetBox.setButtonCell(cellFactory.call(null));
        charsetBox.valueProperty().addListener(new ChangeListener<Charset>() {
            @Override
            public void changed(ObservableValue<? extends Charset> observable, Charset oldValue, Charset newValue) {
                charset = newValue;
            }
        });
        charsetBox.getSelectionModel().select(Charset.defaultCharset());

        formatOptions = FXCollections.observableArrayList();
        for (Format format : Format.values()) {
            formatOptions.add(format.name());
        }

//        formatOptions = FXCollections.observableArrayList("MS Office, ARA01, Custom");
        final ComboBox<String> formats = new ComboBox<String>(formatOptions);
        formats.getSelectionModel().selectFirst();

        Node title = buildTitle(I18n.getInstance().getString("csv.tab.title.field_options"));

        fileButton.setPrefWidth(100);
        charsetBox.setPrefWidth(100);
        formats.setPrefWidth(100);
        charsetBox.setMaxWidth(1000);
        formats.setMaxWidth(1000);

        count.setMinHeight(22);
        count.numberProperty().addListener(new ChangeListener<BigDecimal>() {

            @Override
            public void changed(ObservableValue<? extends BigDecimal> ov, BigDecimal t, BigDecimal t1) {
                updateTree();
            }
        });

        automatic.setDisable(true);
        automatic.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if (_csvFile != null) {
                    try {
                        CSVAnalyser analys = new CSVAnalyser(_csvFile);
                        setEncloser(analys.getEnclosed());
                        setSeperator(analys.getSeperator());
                        formats.getSelectionModel().select(Format.Custom.name());
                        updateTree();

                    } catch (Exception ex) {
                        System.out.println("Error while anylysing csv: " + ex);
                    }
                }

            }
        });

        fileButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                FileChooser fileChooser = new FileChooser();
                if (JEConfig.getLastPath() != null) {
//                    System.out.println("Last Path: " + JEConfig.getLastPath().getParentFile());
                    File file = JEConfig.getLastPath();
                    if (file.exists() && file.canRead()) {
                        fileChooser.setInitialDirectory(file);
                    }
                }

                FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All files ", "*");
                fileChooser.getExtensionFilters().addAll(csvFilter, allFilter);

                final File file = fileChooser.showOpenDialog(JEConfig.getStage());
                if (file != null && file.canRead()) {

                    Platform.runLater(() -> {
                        try {
                            JEConfig.setLastPath(file);
                            System.out.println("file: " + file);

                            fileNameL.setText(file.getName());// + System.getProperty("file.separator") + file.getName());

                            openFile(file);
                            automatic.setDisable(false);
                            CSVAnalyser analyse = new CSVAnalyser(_csvFile);

                            setEncloser(analyse.getEnclosed());
                            setSeperator(analyse.getSeperator());
                            formats.getSelectionModel().select(Format.Custom.name());
                            updateTree();
                        } catch (Exception ex) {
                            Logger.getLogger(CSVImportDialog.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
//                        JEConfig.setLastPath(file);
//                        System.out.println("file: " + file);
//
//                        fileNameL.setText(file.getCanonicalPath());// + System.getProperty("file.separator") + file.getName());
//
//                        openFile(file);
//                        automatic.setDisable(false);
//                        CSVAnalyser analys = new CSVAnalyser(_csvFile);
//
//                        setEncloser(analys.getEnclosed());
//                        setSeperator(analys.getSeperator());
//                        formats.getSelectionModel().select(Format.Custom.name());
//                        updateTree();

                }
            }
        });

        int x = 0;

//        gp.add(title, 0, x, 3, 1);
        gp.add(fileL, 0, ++x);
        gp.add(fileButton, 1, x);
        gp.add(fileNameL, 2, x);

        //@TODO: implement format function
//        gp.add(formatL, 0, ++x);
//        gp.add(formats, 1, x);
//        gp.add(automatic, 2, x);

        gp.add(charSetL, 0, ++x);
        gp.add(charsetBox, 1, x);

        gp.add(fromRow, 0, ++x);
        gp.add(count, 1, x);

        GridPane.setHgrow(title, Priority.ALWAYS);

        updateTree();

        return gp;

    }

    public enum Format {

        Default, ARA01, Custom
    }

    private Node buildSeparatorPane() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(5, 10, 5, LEFT_PADDING));

        gp.setHgap(10);
        gp.setVgap(5);

//        gp.setStyle("-fx-background-color: #86D64D;");
        Label sepL = new Label(I18n.getInstance().getString("csv.separator.column"));
        Label sepTextL = new Label(I18n.getInstance().getString("csv.separator.text"));

        tab.setToggleGroup(sepGroup);
        semicolon.setToggleGroup(sepGroup);
        comma.setToggleGroup(sepGroup);
        space.setToggleGroup(sepGroup);
        otherLineSep.setToggleGroup(sepGroup);
//        sepGroup.selectToggle(semicolon);

        otherColumnF.setMinHeight(22);

        none.setToggleGroup(textDiGroup);
        ditto.setToggleGroup(textDiGroup);
        apostrop.setToggleGroup(textDiGroup);
        enc2.setToggleGroup(textDiGroup);
        otherTextSep.setToggleGroup(textDiGroup);
//        textDiGroup.selectToggle(none);

        setEncloser("");
        setSeperator(";");

        textDiGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                updateEnclosed();
            }
        });

        sepGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                updateSeperator();
            }
        });

        otherTextF.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
//                sepGroup.selectToggle(otherLineSep);
                setEncloser(otherColumnF.getText());
//                _seperator = ;
//                reloadTree();
            }
        });

        otherColumnF.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
//                sepGroup.selectToggle(otherLineSep);
//                _seperator = otherColumnF.getText();
                if (!otherColumnF.getText().isEmpty()) {
                    setSeperator(otherColumnF.getText());
                }
//                reloadTree();
            }
        });


        HBox otherBox = new HBox(5);
        otherBox.setAlignment(Pos.CENTER_LEFT);
        otherBox.getChildren().setAll(otherLineSep, otherColumnF);

        HBox otherTextBox = new HBox(5);
        otherTextBox.setAlignment(Pos.CENTER_LEFT);
        otherTextBox.getChildren().setAll(otherTextSep, otherTextF);

        HBox root = new HBox();

        VBox columnB = new VBox(5);
        VBox textB = new VBox(5);

        root.setPadding(new Insets(5, 10, 5, LEFT_PADDING));
        VBox cSep = new VBox(5);
        VBox tSep = new VBox(5);
        cSep.setPadding(new Insets(0, 0, 0, 20));
        tSep.setPadding(new Insets(0, 0, 0, 20));

        cSep.getChildren().setAll(semicolon, tab, comma, space, otherBox);
        tSep.getChildren().setAll(none, apostrop, ditto, enc2, otherTextBox);

        columnB.getChildren().setAll(sepL, cSep);
        textB.getChildren().setAll(sepTextL, tSep);

        Region spacer = new Region();
//        spacer.setStyle("-fx-background-color: red;");

        root.getChildren().setAll(columnB, textB, spacer);
        root.setAlignment(Pos.TOP_LEFT);
//        root.setPadding(new Insets(0, 0, 0, 10));
        HBox.setHgrow(columnB, Priority.NEVER);
        HBox.setHgrow(textB, Priority.NEVER);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        return root;

    }

    private Node buildTitle(String name) {
        HBox titelBox = new HBox(2);
        titelBox.setPadding(new Insets(8));
        Separator titelSep = new Separator(Orientation.HORIZONTAL);
        titelSep.setMaxWidth(Double.MAX_VALUE);
        Label title = new Label(name);
//        titelBox.getChildren().setAll(titelSep);
        titelBox.getChildren().setAll(title, titelSep);
        HBox.setHgrow(titelSep, Priority.NEVER);
        HBox.setHgrow(titelSep, Priority.ALWAYS);
        titelBox.setAlignment(Pos.CENTER_LEFT);
        titelBox.setPrefWidth(1024);

        return titelBox;
    }

    private void parseFile() {
    }

    private int getStartLine() {
        return count.getNumber().intValue();
    }

    private String getEncloser() {
        return _encloser;

    }

    private void updateEnclosed() {
        RadioButton selecedt = (RadioButton) textDiGroup.getSelectedToggle();
        if (selecedt.equals(none)) {
            _encloser = "";
        } else if (selecedt.equals(ditto)) {
            _encloser = "\"";
        } else if (selecedt.equals(apostrop)) {
            _encloser = "'";
        } else if (selecedt.equals(enc2)) {
            _encloser = "`";
        } else if (selecedt.equals(ditto)) {
            _encloser = "\"";
        } else if (selecedt.equals(otherTextSep)) {
            _encloser = otherTextF.getText();
        }
        updateTree();

    }

    private void setEncloser(String endclosed) {
        _encloser = endclosed;
        RadioButton toSelect = none;

        switch (endclosed) {
            case "\"":
                toSelect = ditto;
                break;
            case "'":
                toSelect = apostrop;
                break;
            case "`":
                toSelect = enc2;
                break;
            case "":
                toSelect = none;
                break;
            default:
                toSelect = otherTextSep;
                otherTextSep.setText(endclosed);
                break;
        }

        if (textDiGroup.getSelectedToggle() != toSelect) {
            textDiGroup.selectToggle(toSelect);
        }
    }

    private String getSeperator() {
        return _seperator;
    }

    private void updateSeperator() {
        if (sepGroup.getSelectedToggle() != null) {
            RadioButton selecedt = (RadioButton) sepGroup.getSelectedToggle();
            if (selecedt.equals(semicolon)) {
                _seperator = ";";
            } else if (selecedt.equals(comma)) {
                _seperator = ",";
            } else if (selecedt.equals(space)) {
                _seperator = " ";
            } else if (selecedt.equals(tab)) {
                _seperator = "\t";
            } else if (selecedt.equals(otherLineSep)) {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        otherColumnF.requestFocus();
                    }
                });
                if (!otherColumnF.getText().isEmpty()) {
                    _seperator = otherColumnF.getText();
                } else {
                    return;
                }

            }
            updateTree();
        }

    }

    private void setSeperator(String sep) {
        _seperator = sep;
        RadioButton toSelect = semicolon;

        switch (sep) {
            case ";":
                toSelect = semicolon;
                break;
            case ",":
                toSelect = comma;
                break;
            case " ":
                toSelect = space;
                break;
            case "\t":
                toSelect = tab;
                break;
            default:
                toSelect = otherLineSep;
                otherColumnF.setText(sep);
                break;
        }

        if (sepGroup.getSelectedToggle() != toSelect) {
            sepGroup.selectToggle(toSelect);
        }

    }

    public enum Response {

        OK, CANCEL
    }

    private void openFile(File file) {
        _csvFile = file;
    }

    public class CSVTabel extends TreeView<CSVCell> {

    }

}
