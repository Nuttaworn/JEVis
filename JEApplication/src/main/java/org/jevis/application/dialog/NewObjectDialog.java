/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.dialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.I18nWS;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.resource.ImageConverter;
import org.jevis.application.resource.ResourceLoader;
import org.jevis.application.tools.NumberSpinner;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fs
 */
public class NewObjectDialog {

    public static String ICON = "1403104602_brick_add.png";

    private int createCount = 1;
    private JEVisClass createClass;
    private String createName = "No Name";
    private boolean userSetName = false;
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());

    public static enum Type {

        NEW, RENAME
    }

    public static enum Response {

        NO, YES, CANCEL
    }

    private Response response = Response.CANCEL;

    public int getCreateCount() {
        if (createCount > 0 && createCount < 100) {
            return createCount;
        } else {
            return 1;
        }
    }

    public String getCreateName() {
        return createName;
    }

    public JEVisClass getCreateClass() {
        return createClass;
    }

    /**
     * @param owner
     * @param jclass
     * @param parent
     * @param fixClass
     * @param type
     * @param objName
     * @return
     */
    public Response show(Stage owner, final JEVisClass jclass, final JEVisObject parent, boolean fixClass, Type type, String objName) {
        final Stage stage = new Stage();

        final BooleanProperty isOK = new SimpleBooleanProperty(false);

        stage.setTitle(rb.getString("jevistree.dialog.new.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(380);
        stage.setHeight(260);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(rb.getString("jevistree.dialog.new.header"));
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        ImageView imageView = ResourceLoader.getImage(ICON, 50, 50);

        stage.getIcons().add(imageView.getImage());

        VBox vboxLeft = new VBox();
        VBox vboxRight = new VBox();
        vboxLeft.getChildren().add(topTitle);
        vboxLeft.setAlignment(Pos.CENTER_LEFT);
        vboxRight.setAlignment(Pos.CENTER_LEFT);
        vboxRight.getChildren().add(imageView);

        header.setLeft(vboxLeft);

        header.setRight(vboxRight);

        HBox buttonPanel = new HBox();

        final Button ok = new Button(rb.getString("jevistree.dialog.new.ok"));
        ok.setDefaultButton(true);
        ok.setDisable(true);

        Button cancel = new Button(rb.getString("jevistree.dialog.new.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(5);
        int x = 0;

        Label lName = new Label(rb.getString("jevistree.dialog.new.name"));
        final TextField fName = new TextField();
        fName.setPromptText(rb.getString("jevistree.dialog.new.name.prompt"));

        if (objName != null) {
            fName.setText(objName);
            userSetName = true;
        }

        fName.setOnKeyTyped(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                userSetName = true;
            }
        });

        Label lClass = new Label(rb.getString("jevistree.dialog.new.class"));

        ObservableList<JEVisClass> options = FXCollections.observableArrayList();

        if (type == Type.NEW) {
            try {

                options = FXCollections.observableArrayList(
                        parent.getAllowedChildrenClasses()
                );

            } catch (JEVisException ex) {
                Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (type == Type.RENAME) {
            options.add(jclass);
        }

        Callback<ListView<JEVisClass>, ListCell<JEVisClass>> cellFactory = new Callback<ListView<JEVisClass>, ListCell<JEVisClass>>() {
            @Override
            public ListCell<JEVisClass> call(ListView<JEVisClass> param) {
                final ListCell<JEVisClass> cell = new ListCell<JEVisClass>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(JEVisClass item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            HBox box = new HBox(5);
                            box.setAlignment(Pos.CENTER_LEFT);
                            try {
                                ImageView icon = ImageConverter.convertToImageView(item.getIcon(), 15, 15);
                                //Label cName = new Label(item.getName());
                                Label cName = new Label(I18nWS.getInstance().getClassName(item.getName()));
                                cName.setTextFill(Color.BLACK);
                                box.getChildren().setAll(icon, cName);

                            } catch (JEVisException ex) {
                                Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            setGraphic(box);

                        }
                    }
                };
                return cell;
            }
        };

        final ComboBox<JEVisClass> comboBox = new ComboBox<>(options);
        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));

        comboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<JEVisClass>() {

            @Override
            public void changed(ObservableValue<? extends JEVisClass> observable, JEVisClass oldValue, JEVisClass newValue) {
                try {
                    if (!userSetName) {
                        fName.setText(newValue.getName());
                    }
                } catch (JEVisException ex) {
                    Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        if (jclass != null) {
            comboBox.getSelectionModel().select(jclass);
        }

        comboBox.setMinWidth(250);
        comboBox.setMaxWidth(Integer.MAX_VALUE);//workaround

        Label lCount = new Label(rb.getString("jevistree.dialog.new.amount"));
        //TODo: disable spinner if class is uniq also disable OK button if there is allready one of its kind
        final NumberSpinner count = new NumberSpinner(BigDecimal.valueOf(1), BigDecimal.valueOf(1));

        if (fixClass) {
            comboBox.setDisable(true);
            count.setDisable(true);
        }

        gp.add(lName, 0, x);
        gp.add(fName, 1, x);

        gp.add(lClass, 0, ++x, 1, 1);
        gp.add(comboBox, 1, x, 1, 1);
        gp.add(lCount, 0, ++x);
        gp.add(count, 1, x);

        GridPane.setHgrow(count, Priority.ALWAYS);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), gp, buttonPanel);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                System.out.println("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
                stage.close();

                createName = fName.getText();
                createClass = comboBox.getSelectionModel().getSelectedItem();
                createCount = Integer.parseInt(count.getNumber().toString());//dirty :)
//                isOK.setValue(true);
                response = Response.YES;

            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;

            }
        });

        fName.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (!fName.getText().equals("")) {
                    ok.setDisable(false);
                }
            }
        });

        fName.setDisable(true);
        comboBox.setDisable(true);
        ok.setDisable(true);
        count.setDisable(true);

        try {
            if (parent.getDataSource().getCurrentUser().canWrite(parent.getID())) {
                fName.setDisable(false);
                comboBox.setDisable(false);
                ok.setDisable(false);
                count.setDisable(false);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(NewObjectDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (type == Type.NEW) {
            stage.setTitle(rb.getString("jevistree.dialog.new.title"));
            topTitle.setText(rb.getString("jevistree.dialog.new.title"));
            comboBox.getSelectionModel().selectFirst();
        } else if (type == Type.RENAME) {
            stage.setTitle(rb.getString("jevistree.dialog.rename.title"));
            topTitle.setText(rb.getString("jevistree.dialog.rename.header"));
            count.setDisable(true);
            comboBox.getSelectionModel().select(jclass);
        }

        stage.showAndWait();
        return response;
    }

}
