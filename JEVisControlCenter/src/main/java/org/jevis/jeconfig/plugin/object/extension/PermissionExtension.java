/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object.extension;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.ToggleSwitch;
import org.jevis.api.*;
import org.jevis.application.dialog.ConfirmDialog;
import org.jevis.application.dialog.InfoDialog;
import org.jevis.commons.relationship.RelationsManagment;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.classes.editor.ClassEditor;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.permission.AddSharePermissonsDialog;
import org.jevis.jeconfig.plugin.object.permission.RemoveSharePermissonsDialog;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ImageConverter;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PermissionExtension implements ObjectEditorExtension {

    private static final String TITEL = I18n.getInstance().getString("plugin.object.permissions.title");
    private JEVisObject _obj;

    private BorderPane _view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    public PermissionExtension(JEVisObject obj) {
        this._obj = obj;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        return true;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public void setVisible() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    build(_obj);
                } catch (Exception ex) {
                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

    }

    @Override
    public String getTitle() {
        return TITEL;
    }

    @Override
    public boolean needSave() {
        return false;
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
        //TODO delete changes
    }

    private void build(final JEVisObject obj) {
        List<JEVisRelationship> ownerRel = new ArrayList<>();
        List<JEVisObject> ownerObj = new ArrayList<>();

        try {
            for (JEVisRelationship rel : obj.getRelationships(JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD)) {
//                System.out.println("owner: " + rel.getOtherObject(obj));
                ownerRel.add(rel);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        GridPane gridPane = new GridPane();
//        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(15);
        gridPane.setVgap(4);

        Label userHeader = new Label(I18n.getInstance().getString("plugin.object.permissions.sharewith"));
        userHeader.setMinWidth(120d);
        Label removeHeader = new Label("");

        userHeader.setStyle("-fx-font-weight: bold;");
        removeHeader.setStyle("-fx-font-weight: bold;");

        int yAxis = 0;

        for (final JEVisRelationship rel : ownerRel) {
            try {
                HBox groupBox = new HBox(2);
                Label nameLabel = new Label(getDisplayName(rel.getOtherObject(obj)));

                ownerObj.add(rel.getOtherObject(obj));

                ImageView usericon = new ImageView();
                try {
                    usericon = ImageConverter.convertToImageView(rel.getOtherObject(obj).getJEVisClass().getIcon(), 17, 17);
                } catch (JEVisException ex) {
                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                }

                groupBox.getChildren().addAll(usericon, nameLabel);

                Button remove = new Button();
                remove.setGraphic(JEConfig.getImage("list-remove.png", 17, 17));
                remove.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {

                        try {
                            RemoveSharePermissonsDialog dia = new RemoveSharePermissonsDialog();
                            RemoveSharePermissonsDialog.Response re = dia.show(JEConfig.getStage(),
                                    I18n.getInstance().getString("plugin.object.permissions.delete.title"),
                                    I18n.getInstance().getString("plugin.object.permissions.delete.title_long"),
                                    I18n.getInstance().getString("plugin.object.permissions.delete.message"));
                            if (re == RemoveSharePermissonsDialog.Response.YES) {
                                rel.getStartObject().deleteRelationship(rel);
                            } else if (re == RemoveSharePermissonsDialog.Response.YES_ALL) {
                                removeFromAllChildren(obj, rel.getOtherObject(obj));
                                rel.getStartObject().deleteRelationship(rel);
                            }

                        } catch (JEVisException ex) {
                            Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    build(_obj);
                                } catch (Exception ex) {
                                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        });
                    }
                });

                Button forAllChildren = new Button();
                forAllChildren.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.permissions.include_children")));
                forAllChildren.setGraphic(JEConfig.getImage("1417642712_sitemap.png", 17, 17));
                forAllChildren.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {

                        try {
                            ConfirmDialog dia = new ConfirmDialog();
                            ConfirmDialog.Response re = dia.show(JEConfig.getStage(),
                                    I18n.getInstance().getString("plugin.object.permissions.share.title"),
                                    I18n.getInstance().getString("plugin.object.permissions.share.title_long"),
                                    I18n.getInstance().getString("plugin.object.permissions.share.message"));

                            if (re == ConfirmDialog.Response.YES) {
                                addTooAllChildren(obj, rel.getOtherObject(obj));

                            }

                        } catch (JEVisException ex) {
                            Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });

                remove.setDisable(true);
                forAllChildren.setDisable(true);
                try {
//                    RelationsManagment.canDeleteRelationship(obj.getDataSource().getCurrentUser(), rel);

                    if (RelationsManagment.canDeleteOwnership(rel)) {
                        remove.setDisable(false);
                        forAllChildren.setDisable(false);
                    }

                } catch (Exception ex) {
                    System.out.println("permissions error: " + ex);
                }

                HBox controls = new HBox(5);
                controls.getChildren().addAll(forAllChildren, remove);

                yAxis++;
                GridPane.setValignment(groupBox, VPos.BASELINE);
                GridPane.setValignment(controls, VPos.BASELINE);

                gridPane.add(groupBox, 0, yAxis);
                gridPane.add(controls, 1, yAxis);

            } catch (Exception ex) {
                Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
                InfoDialog infoDia = new InfoDialog();
                infoDia.show(JEConfig.getStage(),
                        I18n.getInstance().getString("dialog.waring.title"),
                        I18n.getInstance().getString("plugin.object.permissions.error.title_long"),
                        ex.getMessage());
            }
        }
        yAxis++;
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 2, 1);

        yAxis++;
        try {
            gridPane.add(buildNewBox(obj, ownerObj), 0, yAxis, 2, 1);
        } catch (JEVisException ex) {
            Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        ToggleSwitch isPublicButton = new ToggleSwitch(I18n.getInstance().getString("plugin.object.permissions.share_public"));
//        isPublicButton.setPadding(new Insets(5, 0, 20, 20));
        try {
            isPublicButton.setDisable(!obj.getDataSource().getCurrentUser().isSysAdmin());
            isPublicButton.setSelected(obj.isPublic());
            isPublicButton.selectedProperty().setValue(obj.isPublic());
        } catch (JEVisException ex) {
            isPublicButton.setDisable(true);
            isPublicButton.setSelected(false);
        }


        isPublicButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                try {
                    obj.setIsPublic(newValue);
                    obj.commit();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        VBox vbox = new VBox(10d, isPublicButton, gridPane);
        vbox.setPadding(new Insets(5, 0, 20, 20));

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(vbox);
//        _view.getChildren().setAll(scroll);
        _view.setCenter(scroll);
    }

    /**
     * Alppys the same ower to all Children
     *
     * @param obj
     * @param group
     */
    private void addTooAllChildren(JEVisObject obj, JEVisObject group) {
        try {
            for (JEVisObject children : obj.getChildren()) {
                try {
                    JEVisRelationship newRel = children.buildRelationship(group, JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);
                    addTooAllChildren(children, group);
                } catch (JEVisException ex) {
                    Logger.getLogger(PermissionExtension.class.getName()).log(Level.WARNING, "Error while creating userright", ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeFromAllChildren(JEVisObject obj, JEVisObject group) {
        try {
            for (JEVisObject children : obj.getChildren()) {
                for (JEVisRelationship rel : children.getRelationships(JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD)) {
                    if (rel.getEndObject().equals(group)) {
                        try {
                            rel.getStartObject().deleteRelationship(rel);
                        } catch (JEVisException ex) {
                            Logger.getLogger(PermissionExtension.class.getName()).log(Level.WARNING, "Error while deleting userright", ex);
                        }
                    }
                }
                removeFromAllChildren(children, group);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the Organization of an Group. TODO; this code is very static and
     * could be more dynamic
     *
     * @param obj
     * @return
     * @throws JEVisException
     */
    private JEVisObject getOrganization(JEVisObject obj) throws JEVisException {
        Objects.requireNonNull(obj, "getDisplayName: Missing Group Object");
        if (obj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP)) {
            for (JEVisObject p1 : obj.getParents()) {
                if (p1.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP_DIRECTORY)) {
                    for (JEVisObject p2 : p1.getParents()) {
                        if (p2.getJEVisClass().getName().equals(Constants.JEVisClass.ADMINISTRATION_DIRECTROY)) {
                            for (JEVisObject p3 : p2.getParents()) {
                                if (p3.getJEVisClass().getName().equals(Constants.JEVisClass.ORGANIZATION)
                                        || p3.getJEVisClass().getName().equals(Constants.JEVisClass.SYSTEM)) {
                                    return p3;
                                }
                            }
                        }
                    }
                }
            }

        }
        return null;

//        System.out.println("-getOrganization: " + obj.getParents().size());
//        for (JEVisObject parent : obj.getParents()) {
//            System.out.println("--> is this Org parent: " + parent.getName());
//            if (parent.getJEVisClass().getName().equals(Constants.JEVisClass.ORGANIZATION)) {
//                System.out.println("!!!! is is");
//                return parent;
//            } else {
//                return getOrganization(parent);
//            }
//
//        }
//        return null;
    }

    private String getDisplayName(JEVisObject obj) {
        Objects.requireNonNull(obj, "getDisplayName: Missing Group Object");
//        System.out.println("get Diaplayname for: " + obj.getName());
        String dName = "";

        try {
            JEVisObject org = getOrganization(obj);
            if (org != null) {
//            System.out.println("Using org: " + org.getName());
                dName += "(" + org.getName() + ") ";
            } else {
//            System.out.println("is null");
            }
        } catch (JEVisException ex) {
            Logger.getLogger(PermissionExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        dName += obj.getName();

        return dName;

    }

    private HBox buildNewBox(final JEVisObject obj, final List<JEVisObject> allreadyOwner) throws JEVisException {
        JEVisClass groupClass = obj.getDataSource().getJEVisClass(Constants.JEVisClass.GROUP);
        List<JEVisObject> allGroups = obj.getDataSource().getObjects(groupClass, true);

        Label newOwnerlabel = new Label(I18n.getInstance().getString("plugin.object.permissions.share_with_new"));
        newOwnerlabel.setPrefHeight(21);
        GridPane.setValignment(newOwnerlabel, VPos.CENTER);
        HBox addNewBox = new HBox(5);

        Button newB = new Button();
        //ToDo
        final ComboBox<JEVisObject> groupsCBox = new ComboBox<>();
        groupsCBox.setMinWidth(300);
        groupsCBox.setButtonCell(new ListCell<JEVisObject>() {

            @Override
            protected void updateItem(JEVisObject t, boolean bln) {
                super.updateItem(t, bln); //To change body of generated methods, choose Tools | Templates.
                if (!bln && t != null) {
                    setMinWidth(300);
                    setText(getDisplayName(t));
//                    setText(t.getName());
                }
            }

        });
        groupsCBox.setCellFactory(new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {

            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> p) {
                final ListCell<JEVisObject> cell = new ListCell<JEVisObject>() {
                    {
                        super.setPrefWidth(300);
                    }

                    @Override
                    public void updateItem(JEVisObject item,
                                           boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                            setText(getDisplayName(item));
                        } else {
                            setText("*There is no unused group*");
                        }
                    }
                };
                return cell;
            }
        });

        try {
//            List<JEVisObject> usersObjs = obj.getDataSource().getObjects(obj.getDataSource().getJEVisClass("User"), true);
            Collections.sort(allGroups, new Comparator<JEVisObject>() {

                @Override
                public int compare(JEVisObject o1, JEVisObject o2) {
                    //                    return o1.getName().compareTo(o2.getName());
                    return getDisplayName(o1).compareTo(getDisplayName(o2));
                }
            });

            for (JEVisObject group : allGroups) {
                if (!allreadyOwner.contains(group)) {
                    groupsCBox.getItems().add(group);
                }

            }

            groupsCBox.getSelectionModel().selectFirst();

        } catch (Exception ex) {
            Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        newB.setGraphic(JEConfig.getImage("list-add.png", 17, 17));
        newB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    JEVisObject groupObj = groupsCBox.getSelectionModel().getSelectedItem();
//                    System.out.println("create relationship for user: " + groupObj.getName());

                    if (groupObj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP)) {
                        //TODo: here
                        AddSharePermissonsDialog dia = new AddSharePermissonsDialog();
                        AddSharePermissonsDialog.Response re = dia.show(JEConfig.getStage(),
                                I18n.getInstance().getString("plugin.object.permissions.new.title"),
                                I18n.getInstance().getString("plugin.object.permissions.new.title_long"),
                                I18n.getInstance().getString("plugin.object.permissions.new.message", groupObj.getName()));
                        if (re == AddSharePermissonsDialog.Response.YES || re == AddSharePermissonsDialog.Response.YES_ALL) {
                            JEVisRelationship newRel = obj.buildRelationship(groupObj, JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);
//                            System.out.println("new Owner: " + newRel);
                            if (re == AddSharePermissonsDialog.Response.YES_ALL) {
//                                System.out.println("add also to all children");
                                addTooAllChildren(obj, newRel.getOtherObject(obj));
                            }
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    build(_obj);
                                } catch (Exception ex) {
                                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        });
                    }

                } catch (Exception ex) {
                    Logger.getLogger(ClassEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        addNewBox.getChildren().setAll(newOwnerlabel, groupsCBox, newB);

        return addNewBox;
    }

}
