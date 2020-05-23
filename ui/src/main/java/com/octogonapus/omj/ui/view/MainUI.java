/*
 * This file is part of OMJ.
 *
 * OMJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OMJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OMJ.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.octogonapus.omj.ui.view;

import com.octogonapus.omj.util.Util;
import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainUI extends Application {

  @Override
  public void start(final Stage primaryStage) {
    final var root = new BorderPane();
    primaryStage.setTitle("OMJ");
    primaryStage.setScene(new Scene(root, 1000.0, 800.0));
    primaryStage.show();

    final var menuBar = new MenuBar();
    root.setTop(menuBar);

    final var fileMenu = new Menu("File");
    menuBar.getMenus().add(fileMenu);

    final var openTraceMenuItem = new MenuItem("Open Trace");
    openTraceMenuItem.setAccelerator(KeyCombination.keyCombination("CTRL+O"));
    openTraceMenuItem.setOnAction(
        event -> {
          final var fileChooser = new FileChooser();
          fileChooser.setTitle("Open Trace File");
          fileChooser.setInitialDirectory(Util.cacheDir.toFile());
          fileChooser
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("Trace Files", "*.trace"));
          final File selectedFile = fileChooser.showOpenDialog(primaryStage);
          if (selectedFile != null) {
            root.setCenter(new TraceDisplay(selectedFile));
          }
        });
    fileMenu.getItems().add(openTraceMenuItem);

    final var closeMenuItem = new MenuItem("Close");
    closeMenuItem.setAccelerator(KeyCombination.keyCombination("CTRL+Q"));
    closeMenuItem.setOnAction(event -> Platform.exit());
    fileMenu.getItems().add(closeMenuItem);
  }
}
