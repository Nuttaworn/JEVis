/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.graph.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.dialog.GraphSelectionDialog;
import org.jevis.application.jevistree.plugin.BarChartDataModel;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.graph.GraphController;
import org.jevis.jeconfig.plugin.graph.LoadAnalysisDialog;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;

import java.util.*;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GraphPluginView implements Plugin, Observer {

    private ToolBarView toolBarView;
    private GraphDataModel dataModel;
    private GraphController controller;
    private AreaChartView chartView;

    private StringProperty name = new SimpleStringProperty("Graph");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane border;
    private boolean firstStart = true;
    private ToolBar toolBar;
    private LoadAnalysisDialog dialog;

    public GraphPluginView(JEVisDataSource ds, String newname) {
        dataModel = new GraphDataModel();
        dataModel.addObserver(this);

        controller = new GraphController(this, dataModel);
        toolBarView = new ToolBarView(dataModel, ds, chartView);
        chartView = new AreaChartView(dataModel);

        this.ds = ds;
        name.set(newname);
    }

    @Override
    public String getClassName() {
        return "Graph Plugin";
    }

    @Override
    public void setHasFocus() {

        if (firstStart) {
            firstStart = false;

            dialog = new LoadAnalysisDialog(ds, dataModel, toolBarView);

            dialog.showAndWait()
                    .ifPresent(response -> {
                        if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                            GraphSelectionDialog selectionDialog = new GraphSelectionDialog(ds);

                            if (selectionDialog.show(JEConfig.getStage()) == GraphSelectionDialog.Response.OK) {

                                Set<BarChartDataModel> selectedData = new HashSet<>();
                                for (Map.Entry<String, BarChartDataModel> entrySet : selectionDialog.getSelectedData().entrySet()) {
                                    BarChartDataModel value = entrySet.getValue();
                                    if (value.getSelected()) {
                                        selectedData.add(value);
                                    }
                                }
                                dataModel.setSelectedData(selectedData);
                            }
                        } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                            dialog.updateToolBarView();
                            toolBarView.select(dialog.getLv().getSelectionModel().getSelectedItem());
                        }
                    });

        }
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String value) {
        name.set(value);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String getUUID() {
        return id.get();
    }

    @Override
    public void setUUID(String newid) {
        id.set(newid);
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    @Override
    public Node getContentNode() {
        if (border == null) {
            border = new BorderPane();
            chartView.drawDefaultAreaChart();
            border.setCenter(chartView.getAreaChartRegion());
//            border.setCenter(new Button("click me"));

//            border.setCenter(lineChart);
            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
        }

        return border;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {
        if (toolBar == null) {
            toolBar = toolBarView.getToolbar(getDataSource());
        }
        return toolBar;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        //TODO: implement
        return false;
    }

    @Override
    public void handleRequest(int cmdType) {
        try {
            System.out.println("Command to ClassPlugin: " + cmdType);
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
                    System.out.println("save");
                    break;
                case Constants.Plugin.Command.DELTE:
                    break;
                case Constants.Plugin.Command.EXPAND:
                    System.out.println("Expand");
                    break;
                case Constants.Plugin.Command.NEW:
                    break;
                case Constants.Plugin.Command.RELOAD:
                    System.out.println("reload");
                    break;
                default:
                    System.out.println("Unknows command ignore...");
            }
        } catch (Exception ex) {
        }

    }

    @Override
    public void fireCloseEvent() {
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("1415314386_Graph.png", 20, 20);
    }

    @Override
    public void update(Observable o, Object arg) {
        border.setTop(chartView.getLegend());
        border.setCenter(chartView.getAreaChartRegion());
        border.setBottom(chartView.getVbox());

    }


}
