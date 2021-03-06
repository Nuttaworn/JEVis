/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXComboBox;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

/**
 *
 * @author br
 */
public class TimeZoneEditor implements AttributeEditor {

    private final JEVisAttribute _attribute;
    private final HBox _editor = new HBox(5);
    private JEVisSample _newSample;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(true);

    public TimeZoneEditor(JEVisAttribute att) {
        _editor.getStylesheets().add("/styles/TimeZoneEditor.css");
        _attribute = att;
        buildGUI();
    }

    private void buildGUI() {
        JEVisSample sample = _attribute.getLatestSample();
        ObservableList<String> timeZones = FXCollections.observableArrayList();
        timeZones.add("");
        timeZones.add("UTC");
        timeZones.add("Europe/Berlin");

        timeZones.addAll(FXCollections.observableArrayList(org.joda.time.DateTimeZone.getAvailableIDs()));

//        ComboBox<String> picker = new ComboBox<String>(timeZones);
//        final ComboBox picker = new ComboBox(timeZones);
        JFXComboBox picker = new JFXComboBox(timeZones);
        picker.setPrefWidth(GenericAttributeExtension.editorWhith.getValue());
//        new AutoCompleteComboBoxListener<>(picker);

        Callback<ListView<String>, ListCell<String>> cellFactory = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }

                };
            }

        };
        picker.setCellFactory(cellFactory);
        picker.setButtonCell(cellFactory.call(null));

        if (sample != null) {
            try {
                String value = sample.getValueAsString();
                if (timeZones.contains(value)) {
                    picker.setValue(sample.getValueAsString());
                } else {
                    //Fix missconfigured zones
                    picker.setValue("UTC");

                    for (String zone : timeZones) {
                        if (zone.contains(value)) {
                            picker.setValue(zone);
                        }
                    }
                    _newSample = _attribute.buildSample(new DateTime(), picker.getValue());
                    _changed.setValue(Boolean.TRUE);

                }

            } catch (JEVisException ex) {
                Logger.getLogger(TimeZoneEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                picker.setValue("UTC");
                _newSample = _attribute.buildSample(new DateTime(), "UTC");
                _changed.setValue(Boolean.TRUE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        _readOnly.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                picker.setDisable(newValue);
            }
        });

        picker.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), newValue);
                _changed.setValue(Boolean.TRUE);
            } catch (JEVisException ex) {
                Logger.getLogger(TimeZoneEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        _editor.getChildren().addAll(picker);

    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        if (_newSample != null) {
            _newSample.commit();
            _changed.setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        return _editor;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _readOnly.setValue(canRead);
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }
}
