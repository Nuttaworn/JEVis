/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.jevistree.plugin;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.application.jevistree.TreePlugin;
import org.jevis.application.jevistree.UserSelection;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fs
 */
public class SimpleTargetPlugin implements TreePlugin {

    private JEVisTree _tree;
    private List<JEVisObject> _selected = new ArrayList<>();
    private List<UserSelection> _preselect = new ArrayList<>();
    private List<SimpleTargetPluginData> _data = new ArrayList<>();
    private boolean allowMultySelection = false;
    private BooleanProperty validProperty = new SimpleBooleanProperty(false);
    public static Logger LOGGER = LogManager.getLogger(SimpleTargetPlugin.class);

    public enum MODE {
        OBJECT, ATTRIBUTE
    }

    private MODE mode = MODE.OBJECT;

    @Override
    public void setTree(JEVisTree tree) {
        _tree = tree;
    }

    public void setModus(MODE mode) {
        this.mode = mode;
    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> pluginHeader = new TreeTableColumn<>("Target");

        TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, "");
        selectColumn.setEditable(true);
        pluginHeader.getColumns().add(selectColumn);

        list.add(pluginHeader);

        return list;
    }

    public BooleanProperty getValidProperty() {
        return validProperty;
    }

    public void setAllowMultySelection(boolean selection) {
        allowMultySelection = selection;
    }

    @Override
    public void selectionFinished() {
    }

    @Override
    public String getTitle() {
        return "Selection";
    }

    private SimpleTargetPluginData getData(JEVisTreeRow row) {
        for (SimpleTargetPluginData data : _data) {
            if (row.getID().equals(data.getRow().getID())) {
                return data;
            }
        }
        SimpleTargetPluginData data = new SimpleTargetPluginData(row);
        _data.add(data);
        return data;
    }

    private void unselectAllBut(JEVisTreeRow row) {
        for (SimpleTargetPluginData data : _data) {
            if (data.getRow().getID().equals(row.getID())) {
                continue;
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    data.setSelected(false);
                    data.getBox().setSelected(false);
                }
            });

            System.out.println(data.getBox().isSelected());
        }
    }

    private TreeTableColumn<JEVisTreeRow, Boolean> buildSelectionColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(columnName);
        column.setPrefWidth(90);
        column.setEditable(true);

        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, Boolean>, ObservableValue<Boolean>>() {

            @Override
            public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, Boolean> param) {
                return new ReadOnlyObjectWrapper<>(getData(param.getValue().getValue()).isSelected());
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                TreeTableCell<JEVisTreeRow, Boolean> cell = new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            setText(null);
                            setGraphic(null);

                            try {
                                if (getTreeTableRow().getItem() != null
                                        && tree != null
                                        && ((mode == MODE.ATTRIBUTE && getTreeTableRow().getItem().getType() == JEVisTreeRow.TYPE.ATTRIBUTE)
                                        || (mode == MODE.OBJECT && getTreeTableRow().getItem().getType() == JEVisTreeRow.TYPE.OBJECT))) {

                                    StackPane.setAlignment(hbox, Pos.CENTER_LEFT);

                                    CheckBox box = getData(getTreeTableRow().getItem()).getBox();
                                    hbox.getChildren().setAll(box);//fehler?!
                                    setGraphic(hbox);

                                    box.setOnAction(new EventHandler<ActionEvent>() {

                                        @Override
                                        public void handle(ActionEvent event) {
                                            if (!allowMultySelection && box.isSelected()) {
                                                unselectAllBut(getTreeTableRow().getItem());
                                                commitEdit(box.isSelected());
                                                for (SimpleTargetPluginData data : _data) {
                                                    if (data.isSelected()) {
                                                        validProperty.setValue(true);
                                                    }
                                                }

                                            }
                                            getData(getTreeTableRow().getItem()).setSelected(box.isSelected());

                                        }
                                    });

                                    if (isPreselected(getTreeTableRow().getItem())) {
                                        box.setSelected(true);
                                    }
//                                    if (isPreselected(getTreeTableRow().getItem().getJEVisObject())) {
//                                        box.setSelected(true);
//                                    }

                                }
                            } catch (Exception ex) {

                            }

                        } else {
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private boolean isPreselected(JEVisTreeRow row) {
        for (UserSelection us : _preselect) {
            if (mode == MODE.OBJECT) {
                if (us.getSelectedObject().equals(row.getJEVisObject())) {
                    return true;
                }
            } else if (mode == MODE.ATTRIBUTE) {
                if (us.getSelectedAttribute().equals(row.getJEVisAttribute())) {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean isPreselected(JEVisObject obj) {
        for (UserSelection us : _preselect) {
            if (us.getSelectedObject().equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public void setUserSelection(List<UserSelection> list) {
        _preselect = list;
    }

    public List<UserSelection> getUserSelection() {
        for (SimpleTargetPluginData data : _data) {
            if (data.isSelected()) {
//                _preselect.add(new UserSelection(UserSelection.SelectionType.Object, data.getObj()));
                if (mode == MODE.OBJECT) {
                    _preselect.add(new UserSelection(UserSelection.SelectionType.Object, data.getObj()));
                } else {
                    _preselect.add(new UserSelection(UserSelection.SelectionType.Attribute, data.getAtt(), null, null));
                }
            }
        }

        return _preselect;
    }

}
