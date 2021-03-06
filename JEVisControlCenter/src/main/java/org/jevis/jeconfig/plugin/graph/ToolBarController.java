/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.dialog.GraphSelectionDialog;
import org.jevis.application.jevistree.plugin.BarChartDataModel;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.plugin.graph.view.ToolBarView;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author broder
 */
public class ToolBarController implements EventHandler {

    private GraphDataModel model;
    private ToolBarView view;
    private final JEVisDataSource ds;
    private GraphSelectionDialog selectionDialog;

    public ToolBarController(ToolBarView view, GraphDataModel model, JEVisDataSource ds) {
        this.model = model;
        this.view = view;
        this.ds = ds;
    }

    @Override
    public void handle(Event event) {
        if (selectionDialog == null) {
            selectionDialog = new GraphSelectionDialog(ds);
        }

        if (selectionDialog.show(new Stage()) == GraphSelectionDialog.Response.OK) {

            Set<BarChartDataModel> selectedData = new HashSet<>();
            for (Map.Entry<String, BarChartDataModel> entrySet : selectionDialog.getSelectedData().entrySet()) {
                BarChartDataModel value = entrySet.getValue();
                if (value.getSelected()) {
                    selectedData.add(value);
                }
            }
            model.setSelectedData(selectedData);
        }
    }

}
