/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;


import client.scenes.HomePageCtrl;
import client.scenes.JoinServerCtrl;
import client.scenes.MainCtrl;
import client.scenes.OverviewCtrl;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

import static com.google.inject.Guice.createInjector;

@ComponentScan
public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    public static final MyFXML FXML = new MyFXML(INJECTOR);
    public static final float[] SCREEN_SIZE = {1366, 768};

    @Getter @Setter
    private static boolean isAdmin = false;

    /**
     * The main method of the client
     * @param args the input parameters to the client
     */
    public static void main(final String[] args) {
        launch();
    }

    /**
     * Starts the GUI application
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws IOException if FXML file not found
     */
    @Override
    public void start(final Stage primaryStage) throws IOException {
        try {
            System.setProperty("prism.lcdtext", "false");
            final var joinServer = FXML.load(JoinServerCtrl.class, "JoinServer.fxml");
            final var overview = FXML.load(OverviewCtrl.class, "Overview.fxml");
            final var homePage = FXML.load(HomePageCtrl.class, "HomePage.fxml");

            final var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
            mainCtrl.initialize(primaryStage, overview, joinServer, homePage);
        }
        catch (IOException e) {
            throw new IOException(e);
        }

    }
}