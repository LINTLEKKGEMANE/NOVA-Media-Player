package com;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    /*
     * =========================================================
     * PLAYLIST
     * =========================================================
     */

    private final ObservableList<File> playlist =
            FXCollections.observableArrayList();

    private final ListView<File> playlistView =
            new ListView<>(playlist);

    /*
     * =========================================================
     * MEDIA PLAYER
     * =========================================================
     */

    private MediaPlayer mediaPlayer;

    private final MediaView mediaView =
            new MediaView();

    /*
     * =========================================================
     * AUDIO VISUALIZER
     * =========================================================
     *
     * This replaces the old:
     *
     * "Audio playing..."
     *
     * message.
     *
     * It is made completely with JavaFX shapes.
     *
     * No image.
     * No URL.
     * No external resource.
     */

    private final StackPane audioVisual =
            new StackPane();

    private ParallelTransition audioAnimation;

    /*
     * =========================================================
     * UI LABELS
     * =========================================================
     */

    private final Label titleLabel =
            new Label("No media selected");

    private final Label artistLabel =
            new Label("Nova Media Player");

    private final Label timeLabel =
            new Label("00:00 / 00:00");

    private final Label volumeLabel =
            new Label("70%");

    private final Label emptyVideoLabel =
            new Label("Choose a media file with  Add Media");

    /*
     * =========================================================
     * SEARCH
     * =========================================================
     */

    private final TextField searchField =
            new TextField();

    /*
     * =========================================================
     * PLAYER SLIDERS
     * =========================================================
     */

    private final Slider progress =
            new Slider(0, 1, 0);

    private final Slider volume =
            new Slider(0, 1, 0.70);

    /*
     * =========================================================
     * PLAYER BUTTONS
     * =========================================================
     */

    private final Button playPause =
            new Button("▶");

    private final Button previous =
            new Button("⏮");

    private final Button next =
            new Button("⏭");

    private final Button stop =
            new Button("■");

    private final Button mute =
            new Button("🔊");

    /*
     * =========================================================
     * PLAYER STATE
     * =========================================================
     */

    private int currentIndex = -1;

    private boolean muted = false;

    private double savedVolume = 0.70;

    private boolean updatingProgress = false;

    /*
     * Prevent duplicate media-error dialogs.
     */

    private boolean mediaErrorShown = false;

    /*
     * =========================================================
     * START APPLICATION
     * =========================================================
     */

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        root.getStyleClass().add("root");

        /*
         * HEADER
         */

        root.setTop(
                buildHeader(stage)
        );

        /*
         * LEFT SIDEBAR
         */

        root.setLeft(
                buildSidebar()
        );

        /*
         * MAIN CONTENT
         */

        root.setCenter(
                buildMainContent()
        );

        /*
         * RIGHT PLAYLIST
         */

        root.setRight(
                buildPlaylistPanel()
        );

        /*
         * BOTTOM PLAYLIST
         */

        root.setBottom(
                buildBottomPlaylist()
        );

        /*
         * SCENE
         */

        Scene scene = new Scene(
                root,
                1280,
                800
        );

        /*
         * LOAD LOCAL CSS
         */

        loadStylesheet(scene);

        /*
         * KEYBOARD CONTROLS
         */

        installKeyboardControls(scene);

        /*
         * WINDOW
         */

        stage.setTitle(
                "NOVA Media Player"
        );

        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        stage.setScene(scene);

        stage.setWidth(1280);
        stage.setHeight(800);

        stage.centerOnScreen();

        stage.show();
    }

    /*
     * =========================================================
     * LOAD CSS
     * =========================================================
     */

    private void loadStylesheet(Scene scene) {

        /*
         * Preferred:
         *
         * src/main/resources/com/styles.css
         */

        if (getClass().getResource("styles.css") != null) {

            scene.getStylesheets().add(
                    getClass()
                            .getResource("styles.css")
                            .toExternalForm()
            );

            return;
        }

        /*
         * Alternative:
         *
         * src/main/resources/styles.css
         */

        if (getClass().getResource("/styles.css") != null) {

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/styles.css")
                            .toExternalForm()
            );

            return;
        }

        System.out.println(
                "NOVA: styles.css was not found. "
                        + "Default JavaFX styling will be used."
        );
    }

    /*
     * =========================================================
     * HEADER
     * =========================================================
     */

    private HBox buildHeader(Stage stage) {

        HBox header =
                new HBox(22);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.getStyleClass().add(
                "header"
        );

        header.setPadding(
                new Insets(
                        16,
                        24,
                        16,
                        30
                )
        );

        /*
         * LOGO
         */

        Label logoIcon =
                new Label("▶");

        logoIcon.getStyleClass().add(
                "logo-icon"
        );

        Label nova =
                new Label("NOVA");

        nova.getStyleClass().add(
                "logo-text"
        );

        Label subtitle =
                new Label(
                        "Media Player"
                );

        subtitle.getStyleClass().add(
                "header-subtitle"
        );

        /*
         * SPACER
         */

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        /*
         * SEARCH
         */

        searchField.setPromptText(
                "Search media, artists, or files..."
        );

        searchField.getStyleClass().add(
                "search-field"
        );

        searchField.setPrefWidth(380);
        searchField.setMinWidth(180);
        searchField.setMaxWidth(500);

        searchField.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        filterPlaylist(newValue)
        );

        Label shortcut =
                new Label(
                        "⌨  Ctrl + K"
                );

        shortcut.getStyleClass().add(
                "shortcut-pill"
        );

        /*
         * SETTINGS
         */

        Button settings =
                new Button("⚙");

        settings.getStyleClass().add(
                "header-button"
        );

        settings.setOnAction(
                e -> showSettings()
        );

        /*
         * MAXIMIZE
         */

        Button maximize =
                new Button("↗");

        maximize.getStyleClass().add(
                "header-button"
        );

        maximize.setOnAction(
                e -> stage.setMaximized(
                        !stage.isMaximized()
                )
        );

        /*
         * MINIMIZE
         */

        Button minimize =
                new Button("—");

        minimize.getStyleClass().add(
                "header-button"
        );

        minimize.setOnAction(
                e -> stage.setIconified(true)
        );

        /*
         * CLOSE
         */

        Button close =
                new Button("×");

        close.getStyleClass().add(
                "header-button"
        );

        close.setOnAction(
                e -> stage.close()
        );

        header.getChildren().addAll(
                logoIcon,
                nova,
                subtitle,
                spacer,
                searchField,
                shortcut,
                settings,
                maximize,
                minimize,
                close
        );

        return header;
    }

    /*
     * =========================================================
     * SETTINGS
     * =========================================================
     */

    private void showSettings() {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "NOVA Media Player\n\n"
                                + "Local audio and video playback "
                                + "is supported.\n\n"
                                + "Recommended video format:\n"
                                + "MP4 + H.264 video + AAC audio.",
                        ButtonType.OK
                );

        alert.setTitle(
                "NOVA Settings"
        );

        alert.setHeaderText(
                "NOVA Media Player"
        );

        alert.showAndWait();
    }

    /*
     * =========================================================
     * SIDEBAR
     * =========================================================
     */

    private VBox buildSidebar() {

        VBox side =
                new VBox(12);

        side.getStyleClass().add(
                "sidebar"
        );

        side.setPadding(
                new Insets(
                        26,
                        18,
                        18,
                        18
                )
        );

        side.setPrefWidth(210);
        side.setMinWidth(180);
        side.setMaxWidth(230);

        side.getChildren().addAll(

                navButton(
                        "⌂",
                        "Home",
                        true
                ),

                navButton(
                        "▱",
                        "Library",
                        false
                ),

                navButton(
                        "♡",
                        "Favorites",
                        false
                ),

                navButton(
                        "☷",
                        "Playlists",
                        false
                )
        );

        Label my =
                new Label(
                        "MY PLAYLISTS"
                );

        my.getStyleClass().add(
                "section-label"
        );

        VBox.setMargin(
                my,
                new Insets(
                        22,
                        0,
                        4,
                        16
                )
        );

        Button addPlaylist =
                new Button("+");

        addPlaylist.getStyleClass().add(
                "plus-button"
        );

        addPlaylist.setOnAction(
                e -> showPlaylistMessage()
        );

        Region playlistSpacer =
                new Region();

        HBox.setHgrow(
                playlistSpacer,
                Priority.ALWAYS
        );

        HBox playlistTitle =
                new HBox(
                        my,
                        playlistSpacer,
                        addPlaylist
                );

        playlistTitle.setAlignment(
                Pos.CENTER_LEFT
        );

        side.getChildren().add(
                playlistTitle
        );

        side.getChildren().addAll(

                playlistCategory(
                        "♫",
                        "Chill Vibes",
                        "12 items"
                ),

                playlistCategory(
                        "♫",
                        "Focus",
                        "8 items"
                ),

                playlistCategory(
                        "♫",
                        "Workout",
                        "10 items"
                ),

                playlistCategory(
                        "♫",
                        "Movies",
                        "6 items"
                )
        );

        return side;
    }

    /*
     * =========================================================
     * NAVIGATION BUTTON
     * =========================================================
     */

    private Button navButton(
            String icon,
            String text,
            boolean selected) {

        Button button =
                new Button(
                        icon + "    " + text
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.getStyleClass().add(
                "nav-button"
        );

        if (selected) {

            button.getStyleClass().add(
                    "selected"
            );
        }

        return button;
    }

    /*
     * =========================================================
     * PLAYLIST CATEGORY
     * =========================================================
     */

    private HBox playlistCategory(
            String icon,
            String name,
            String count) {

        Label iconLabel =
                new Label(icon);

        iconLabel.getStyleClass().add(
                "category-icon"
        );

        Label nameLabel =
                new Label(name);

        nameLabel.getStyleClass().add(
                "category-name"
        );

        Label countLabel =
                new Label(count);

        countLabel.getStyleClass().add(
                "category-count"
        );

        VBox text =
                new VBox(
                        3,
                        nameLabel,
                        countLabel
                );

        HBox box =
                new HBox(
                        14,
                        iconLabel,
                        text
                );

        box.setAlignment(
                Pos.CENTER_LEFT
        );

        box.setPadding(
                new Insets(
                        8,
                        10,
                        8,
                        10
                )
        );

        return box;
    }

    /*
     * =========================================================
     * MAIN CONTENT
     * =========================================================
     */

    private VBox buildMainContent() {

        VBox main =
                new VBox(16);

        main.getStyleClass().add(
                "main"
        );

        main.setPadding(
                new Insets(
                        20,
                        22,
                        18,
                        22
                )
        );

        /*
         * PAGE TITLE
         */

        Label now =
                new Label(
                        "Now Playing"
                );

        now.getStyleClass().add(
                "page-title"
        );

        /*
         * =====================================================
         * MEDIA CARD
         * =====================================================
         */

        StackPane videoCard =
                new StackPane();

        videoCard.getStyleClass().add(
                "video-card"
        );

        videoCard.setMinHeight(280);
        videoCard.setPrefHeight(380);

        /*
         * =====================================================
         * REAL VIDEO VIEW
         * =====================================================
         */

        mediaView.setPreserveRatio(true);

        mediaView.fitWidthProperty().bind(
                videoCard.widthProperty()
                        .subtract(20)
        );

        mediaView.fitHeightProperty().bind(
                videoCard.heightProperty()
                        .subtract(20)
        );

        mediaView.setVisible(false);

        videoCard.getChildren().add(
                mediaView
        );

        /*
         * =====================================================
         * BEAUTIFUL AUDIO VISUALIZER
         * =====================================================
         *
         * This is what appears when an audio file is playing.
         *
         * It is NOT an image.
         *
         * It is NOT text.
         *
         * It is made entirely from JavaFX shapes.
         */

        buildAudioVisualizer();

        audioVisual.setVisible(false);

        audioVisual.setMouseTransparent(true);

        videoCard.getChildren().add(
                audioVisual
        );

        /*
         * =====================================================
         * EMPTY / LOADING MESSAGE
         * =====================================================
         */

        emptyVideoLabel.getStyleClass().add(
                "empty-video"
        );

        emptyVideoLabel.setVisible(true);

        videoCard.getChildren().add(
                emptyVideoLabel
        );

        /*
         * MEDIA INFORMATION
         */

        titleLabel.getStyleClass().add(
                "media-title"
        );

        artistLabel.getStyleClass().add(
                "media-subtitle"
        );

        Label type =
                new Label("Media");

        type.getStyleClass().add(
                "tag"
        );

        HBox detailsRow =
                new HBox(
                        12,
                        titleLabel,
                        type
                );

        detailsRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox information =
                new VBox(
                        5,
                        detailsRow,
                        artistLabel
                );

        /*
         * ACTION BUTTONS
         */

        HBox actionRow =
                new HBox(10);

        actionRow.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button favorite =
                new Button("♡");

        favorite.getStyleClass().add(
                "round-action"
        );

        Button more =
                new Button("•••");

        more.getStyleClass().add(
                "round-action"
        );

        actionRow.getChildren().addAll(
                favorite,
                more
        );

        /*
         * TRANSPORT CONTROLS
         */

        HBox controls =
                new HBox(28);

        controls.setAlignment(
                Pos.CENTER
        );

        controls.getStyleClass().add(
                "transport"
        );

        styleTransport(previous);
        styleTransport(stop);
        styleTransport(playPause);
        styleTransport(next);
        styleTransport(mute);

        previous.setOnAction(
                e -> playPrevious()
        );

        stop.setOnAction(
                e -> stopPlayback()
        );

        playPause.setOnAction(
                e -> togglePlayPause()
        );

        next.setOnAction(
                e -> playNext()
        );

        mute.setOnAction(
                e -> toggleMute()
        );

        controls.getChildren().addAll(
                previous,
                stop,
                playPause,
                next,
                mute
        );

        /*
         * PROGRESS
         */

        HBox progressRow =
                new HBox(12);

        progressRow.setAlignment(
                Pos.CENTER_LEFT
        );

        progress.setMinWidth(100);

        progress.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                progress,
                Priority.ALWAYS
        );

        progressRow.getChildren().addAll(
                progress,
                timeLabel
        );

        /*
         * VOLUME
         */

        HBox volumeRow =
                new HBox(12);

        volumeRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label speaker =
                new Label("🔊");

        speaker.getStyleClass().add(
                "small-icon"
        );

        volume.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                volume,
                Priority.ALWAYS
        );

        volume.valueProperty().addListener(
                (obs, oldValue, newValue) -> {

                    double value =
                            newValue.doubleValue();

                    if (!muted) {

                        savedVolume = value;

                        if (mediaPlayer != null) {

                            mediaPlayer.setVolume(
                                    value
                            );
                        }
                    }

                    volumeLabel.setText(
                            Math.round(
                                    value * 100
                            ) + "%"
                    );
                }
        );

        Label quiet =
                new Label("🔇");

        volumeRow.getChildren().addAll(
                speaker,
                volume,
                volumeLabel,
                quiet
        );

        /*
         * ADD TO MAIN
         */

        main.getChildren().addAll(
                now,
                videoCard,
                information,
                actionRow,
                controls,
                progressRow,
                volumeRow
        );

        VBox.setVgrow(
                videoCard,
                Priority.ALWAYS
        );

        return main;
    }

    /*
     * =========================================================
     * BUILD AUDIO VISUALIZER
     * =========================================================
     *
     * A NOVA-style purple / blue music visual.
     *
     * The center looks like a glowing audio disc.
     *
     * The bars animate while audio is playing.
     */

    private void buildAudioVisualizer() {

        audioVisual.getChildren().clear();

        /*
         * OUTER GLOW
         */

        Circle glow =
                new Circle(105);

        glow.setFill(
                Color.rgb(
                        124,
                        92,
                        255,
                        0.10
                )
        );

        glow.setEffect(
                new DropShadow(
                        45,
                        Color.rgb(
                                124,
                                92,
                                255,
                                0.65
                        )
                )
        );

        /*
         * MAIN DISC
         */

        Circle disc =
                new Circle(82);

        disc.setFill(
                Color.rgb(
                        18,
                        28,
                        48
                )
        );

        disc.setStroke(
                Color.rgb(
                        124,
                        92,
                        255,
                        0.85
                )
        );

        disc.setStrokeWidth(2.5);

        disc.setEffect(
                new DropShadow(
                        25,
                        Color.rgb(
                                34,
                                211,
                                238,
                                0.40
                        )
                )
        );

        /*
         * INNER DISC
         */

        Circle innerDisc =
                new Circle(57);

        innerDisc.setFill(
                Color.rgb(
                        124,
                        92,
                        255,
                        0.22
                )
        );

        innerDisc.setStroke(
                Color.rgb(
                        34,
                        211,
                        238,
                        0.45
                )
        );

        innerDisc.setStrokeWidth(2);

        /*
         * CENTER
         */

        Circle center =
                new Circle(10);

        center.setFill(
                Color.rgb(
                        34,
                        211,
                        238
                )
        );

        center.setEffect(
                new DropShadow(
                        15,
                        Color.rgb(
                                34,
                                211,
                                238,
                                0.80
                        )
                )
        );

        /*
         * =====================================================
         * EQUALIZER BARS
         * =====================================================
         */

        HBox bars =
                new HBox(7);

        bars.setAlignment(
                Pos.CENTER
        );

        double[] heights = {
                28,
                48,
                72,
                42,
                88,
                55,
                76,
                38,
                64,
                45,
                82
        };

        Color[] barColors = {
                Color.rgb(124, 92, 255),
                Color.rgb(124, 92, 255),
                Color.rgb(104, 104, 255),
                Color.rgb(34, 211, 238),
                Color.rgb(34, 211, 238),
                Color.rgb(124, 92, 255),
                Color.rgb(124, 92, 255),
                Color.rgb(34, 211, 238),
                Color.rgb(34, 211, 238),
                Color.rgb(104, 104, 255),
                Color.rgb(124, 92, 255)
        };

        List<ScaleTransition> animations =
                new ArrayList<>();

        for (int i = 0;
             i < heights.length;
             i++) {

            Rectangle bar =
                    new Rectangle(
                            9,
                            heights[i]
                    );

            bar.setArcWidth(9);
            bar.setArcHeight(9);

            bar.setFill(
                    barColors[i]
            );

            bar.setEffect(
                    new DropShadow(
                            12,
                            Color.rgb(
                                    124,
                                    92,
                                    255,
                                    0.45
                            )
                    )
            );

            /*
             * Start slightly smaller.
             */

            bar.setScaleY(0.45);

            /*
             * Animate each bar differently.
             */

            ScaleTransition animation =
                    new ScaleTransition(
                            Duration.millis(
                                    450 + (i * 65)
                            ),
                            bar
                    );

            animation.setFromY(0.35);
            animation.setToY(1.0);

            animation.setAutoReverse(true);

            animation.setCycleCount(
                    Animation.INDEFINITE
            );

            /*
             * Stagger the bars.
             */

            animation.setDelay(
                    Duration.millis(
                            i * 70
                    )
            );

            animations.add(
                    animation
            );

            bars.getChildren().add(
                    bar
            );
        }

        /*
         * =====================================================
         * ADD VISUAL ELEMENTS
         * =====================================================
         */

        audioVisual.getChildren().addAll(
                glow,
                disc,
                innerDisc,
                bars,
                center
        );

        /*
         * =====================================================
         * ANIMATION CONTROLLER
         * =====================================================
         */

        audioAnimation =
                new ParallelTransition();

        audioAnimation.getChildren().addAll(
                animations
        );

        audioAnimation.setCycleCount(
                Animation.INDEFINITE
        );
    }

    /*
     * =========================================================
     * SHOW AUDIO VISUAL
     * =========================================================
     */

    private void showAudioVisual() {

        mediaView.setVisible(false);

        emptyVideoLabel.setVisible(false);

        audioVisual.setVisible(true);

        if (audioAnimation != null) {

            audioAnimation.play();
        }
    }

    /*
     * =========================================================
     * HIDE AUDIO VISUAL
     * =========================================================
     */

    private void hideAudioVisual() {

        if (audioAnimation != null) {

            audioAnimation.stop();
        }

        audioVisual.setVisible(false);
    }

    /*
     * =========================================================
     * TRANSPORT STYLE
     * =========================================================
     */

    private void styleTransport(Button button) {

        button.getStyleClass().add(
                "transport-button"
        );
    }

    /*
     * =========================================================
     * RIGHT PLAYLIST
     * =========================================================
     */

    private VBox buildPlaylistPanel() {

        VBox panel =
                new VBox(12);

        panel.getStyleClass().add(
                "playlist-panel"
        );

        panel.setPadding(
                new Insets(
                        24,
                        18,
                        16,
                        18
                )
        );

        panel.setPrefWidth(270);
        panel.setMinWidth(240);
        panel.setMaxWidth(310);

        /*
         * HEADING
         */

        HBox heading =
                new HBox();

        Label label =
                new Label("Playlist");

        label.getStyleClass().add(
                "panel-title"
        );

        Label count =
                new Label("0 items");

        count.getStyleClass().add(
                "item-count"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        heading.getChildren().addAll(
                label,
                spacer,
                count
        );

        count.textProperty().bind(
                Bindings
                        .size(playlist)
                        .asString("%d items")
        );

        /*
         * LIST VIEW
         */

        playlistView.setPlaceholder(
                new Label(
                        "No media yet"
                )
        );

        playlistView.getStyleClass().add(
                "playlist-view"
        );

        /*
         * CELLS
         */

        playlistView.setCellFactory(
                view -> new ListCell<File>() {

                    @Override
                    protected void updateItem(
                            File file,
                            boolean empty) {

                        super.updateItem(
                                file,
                                empty
                        );

                        if (empty || file == null) {

                            setText(null);
                            setGraphic(null);

                            return;
                        }

                        Label playIcon =
                                new Label(
                                        isVideo(file)
                                                ? "▶"
                                                : "♫"
                                );

                        Label name =
                                new Label(
                                        file.getName()
                                );

                        name.getStyleClass().add(
                                "playlist-name"
                        );

                        Label mediaType =
                                new Label(
                                        isVideo(file)
                                                ? "Video"
                                                : "Audio"
                                );

                        VBox meta =
                                new VBox(
                                        4,
                                        name,
                                        mediaType
                                );

                        meta.getStyleClass().add(
                                "playlist-meta"
                        );

                        HBox row =
                                new HBox(
                                        12,
                                        playIcon,
                                        meta
                                );

                        row.setAlignment(
                                Pos.CENTER_LEFT
                        );

                        setGraphic(row);
                    }
                }
        );

        /*
         * SELECTING A MEDIA FILE PLAYS IT.
         */

        playlistView
                .getSelectionModel()
                .selectedIndexProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            int index =
                                    newValue.intValue();

                            if (index >= 0
                                    && index != currentIndex) {

                                playIndex(index);
                            }
                        }
                );

        /*
         * ADD
         */

        Button add =
                new Button(
                        "+  Add Media"
                );

        add.getStyleClass().add(
                "add-button"
        );

        add.setOnAction(
                e -> chooseFiles()
        );

        /*
         * REMOVE
         */

        Button remove =
                new Button(
                        "▢  Remove"
                );

        remove.getStyleClass().add(
                "remove-button"
        );

        remove.setOnAction(
                e -> removeSelected()
        );

        add.setMaxWidth(
                Double.MAX_VALUE
        );

        remove.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                add,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                remove,
                Priority.ALWAYS
        );

        HBox actions =
                new HBox(
                        10,
                        add,
                        remove
                );

        panel.getChildren().addAll(
                heading,
                playlistView,
                actions
        );

        VBox.setVgrow(
                playlistView,
                Priority.ALWAYS
        );

        return panel;
    }

    /*
     * =========================================================
     * BOTTOM PLAYLIST
     * =========================================================
     */

    private HBox buildBottomPlaylist() {

        HBox bottom =
                new HBox(18);

        bottom.getStyleClass().add(
                "bottom-strip"
        );

        bottom.setPadding(
                new Insets(
                        12,
                        22,
                        12,
                        24
                )
        );

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                new Label(
                        "Current Playlist\n0 items"
                );

        title.getStyleClass().add(
                "current-playlist"
        );

        playlist.addListener(
                (javafx.collections.ListChangeListener<File>)
                        change -> {

                            title.setText(
                                    "Current Playlist\n"
                                            + playlist.size()
                                            + " items"
                            );
                        }
        );

        HBox cards =
                new HBox(14);

        cards.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                cards,
                Priority.ALWAYS
        );

        playlist.addListener(
                (javafx.collections.ListChangeListener<File>)
                        change -> {

                            cards.getChildren().clear();

                            int maxCards =
                                    Math.min(
                                            5,
                                            playlist.size()
                                    );

                            for (
                                    int i = 0;
                                    i < maxCards;
                                    i++
                            ) {

                                File file =
                                        playlist.get(i);

                                Label icon =
                                        new Label(
                                                isVideo(file)
                                                        ? "▶"
                                                        : "♫"
                                        );

                                icon.getStyleClass().add(
                                        "mini-icon"
                                );

                                Label name =
                                        new Label(
                                                stripExtension(
                                                        file.getName()
                                                )
                                        );

                                name.setWrapText(
                                        true
                                );

                                name.getStyleClass().add(
                                        "mini-name"
                                );

                                Label type =
                                        new Label(
                                                isVideo(file)
                                                        ? "Video"
                                                        : "Audio"
                                        );

                                VBox text =
                                        new VBox(
                                                3,
                                                name,
                                                type
                                        );

                                HBox card =
                                        new HBox(
                                                8,
                                                icon,
                                                text
                                        );

                                card.getStyleClass().add(
                                        "mini-card"
                                );

                                card.setAlignment(
                                        Pos.CENTER_LEFT
                                );

                                HBox.setHgrow(
                                        card,
                                        Priority.ALWAYS
                                );

                                cards.getChildren().add(
                                        card
                                );
                            }
                        }
        );

        bottom.getChildren().addAll(
                title,
                cards
        );

        return bottom;
    }

    /*
     * =========================================================
     * CHOOSE MEDIA FILES
     * =========================================================
     */

    private void chooseFiles() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Select Media Files"
        );

        /*
         * AUDIO + VIDEO
         */

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Media Files",
                        "*.mp3",
                        "*.wav",
                        "*.m4a",
                        "*.aac",
                        "*.mp4",
                        "*.m4v"
                )
        );

        List<File> files =
                chooser.showOpenMultipleDialog(
                        playlistView
                                .getScene()
                                .getWindow()
                );

        if (files == null
                || files.isEmpty()) {

            return;
        }

        /*
         * ADD FILES
         */

        for (File file : files) {

            if (file != null
                    && file.exists()
                    && file.isFile()
                    && !playlist.contains(file)) {

                playlist.add(file);
            }
        }

        /*
         * AUTO PLAY FIRST FILE
         */

        if (currentIndex < 0
                && !playlist.isEmpty()) {

            playlistView
                    .getSelectionModel()
                    .select(0);
        }
    }

    /*
     * =========================================================
     * REMOVE SELECTED
     * =========================================================
     */

    private void removeSelected() {

        int index =
                playlistView
                        .getSelectionModel()
                        .getSelectedIndex();

        if (index < 0
                || index >= playlist.size()) {

            return;
        }

        boolean removingCurrent =
                index == currentIndex;

        playlist.remove(index);

        if (playlist.isEmpty()) {

            disposeCurrentPlayer();

            currentIndex = -1;

            titleLabel.setText(
                    "No media selected"
            );

            artistLabel.setText(
                    "Nova Media Player"
            );

            return;
        }

        if (removingCurrent) {

            disposeCurrentPlayer();

            currentIndex = -1;

            int nextIndex =
                    Math.min(
                            index,
                            playlist.size() - 1
                    );

            playlistView
                    .getSelectionModel()
                    .select(nextIndex);

            return;
        }

        if (index < currentIndex) {

            currentIndex--;
        }
    }

    /*
     * =========================================================
     * PLAY MEDIA
     * =========================================================
     */

    private void playIndex(int index) {

        if (index < 0
                || index >= playlist.size()) {

            return;
        }

        File file =
                playlist.get(index);

        /*
         * FILE CHECK
         */

        if (!file.exists()
                || !file.isFile()) {

            showError(
                    "The selected file could not be found:\n\n"
                            + file.getAbsolutePath()
            );

            return;
        }

        /*
         * DISPOSE OLD PLAYER
         */

        disposeCurrentPlayer();

        currentIndex = index;

        mediaErrorShown = false;

        setProgressSilently(0);

        timeLabel.setText(
                "00:00 / 00:00"
        );

        /*
         * CREATE MEDIA
         *
         * Local media uses file.toURI().
         *
         * This is NOT a hard-coded web URL.
         */

        Media media;

        try {

            media =
                    new Media(
                            file.toURI().toString()
                    );

        } catch (Exception ex) {

            showMediaLoadError(
                    file,
                    ex
            );

            return;
        }

        /*
         * MEDIA ERROR
         */

        media.setOnError(
                () -> showMediaErrorOnce(
                        file,
                        media.getError()
                )
        );

        /*
         * CREATE MEDIA PLAYER
         */

        try {

            mediaPlayer =
                    new MediaPlayer(media);

        } catch (Exception ex) {

            showMediaLoadError(
                    file,
                    ex
            );

            return;
        }

        /*
         * CONNECT MEDIA PLAYER TO MEDIA VIEW
         */

        mediaView.setMediaPlayer(
                mediaPlayer
        );

        /*
         * =====================================================
         * DISPLAY TYPE
         * =====================================================
         *
         * VIDEO:
         *     MediaView is shown.
         *
         * AUDIO:
         *     Beautiful NOVA audio visualizer is shown.
         */

        if (isVideo(file)) {

            hideAudioVisual();

            mediaView.setVisible(false);

            emptyVideoLabel.setText(
                    "Loading video..."
            );

            emptyVideoLabel.setVisible(true);

        } else {

            mediaView.setVisible(false);

            emptyVideoLabel.setVisible(false);

            showAudioVisual();
        }

        /*
         * =====================================================
         * VOLUME
         * =====================================================
         */

        mediaPlayer.setVolume(
                muted
                        ? 0
                        : volume.getValue()
        );

        /*
         * =====================================================
         * MEDIA INFORMATION
         * =====================================================
         */

        titleLabel.setText(
                stripExtension(
                        file.getName()
                )
        );

        artistLabel.setText(
                isVideo(file)
                        ? "Local Video"
                        : "Local Audio"
        );

        /*
         * =====================================================
         * MEDIA READY
         * =====================================================
         */

        mediaPlayer.setOnReady(
                () -> {

                    Duration total =
                            mediaPlayer
                                    .getTotalDuration();

                    updateTime(
                            Duration.ZERO,
                            total
                    );

                    /*
                     * VIDEO IS READY
                     */

                    if (isVideo(file)) {

                        hideAudioVisual();

                        mediaView.setVisible(
                                true
                        );

                        emptyVideoLabel.setVisible(
                                false
                        );

                    } else {

                        /*
                         * AUDIO VISUALIZER
                         */

                        mediaView.setVisible(
                                false
                        );

                        emptyVideoLabel.setVisible(
                                false
                        );

                        showAudioVisual();
                    }

                    /*
                     * START PLAYBACK
                     */

                    mediaPlayer.play();

                    playPause.setText(
                            "Ⅱ"
                    );
                }
        );

        /*
         * =====================================================
         * CURRENT TIME
         * =====================================================
         */

        mediaPlayer
                .currentTimeProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            if (mediaPlayer == null) {
                                return;
                            }

                            Duration total =
                                    mediaPlayer
                                            .getTotalDuration();

                            if (total == null
                                    || total.isUnknown()
                                    || !total.greaterThan(
                                            Duration.ZERO
                                    )) {

                                return;
                            }

                            double ratio =
                                    newValue.toMillis()
                                            / total.toMillis();

                            if (Double.isNaN(ratio)
                                    || Double.isInfinite(ratio)) {

                                return;
                            }

                            ratio =
                                    Math.max(
                                            0,
                                            Math.min(
                                                    1,
                                                    ratio
                                            )
                                    );

                            setProgressSilently(
                                    ratio
                            );

                            updateTime(
                                    newValue,
                                    total
                            );
                        }
        );

        /*
         * =====================================================
         * END OF MEDIA
         * =====================================================
         */

        mediaPlayer.setOnEndOfMedia(
                this::playNext
        );

        /*
         * =====================================================
         * PLAYER ERROR
         * =====================================================
         */

        mediaPlayer.setOnError(
                () -> showMediaErrorOnce(
                        file,
                        mediaPlayer.getError()
                )
        );

        /*
         * =====================================================
         * PLAYING
         * =====================================================
         */

        mediaPlayer.setOnPlaying(
                () -> {

                    playPause.setText(
                            "Ⅱ"
                    );

                    if (isVideo(file)) {

                        hideAudioVisual();

                        mediaView.setVisible(
                                true
                        );

                    } else {

                        showAudioVisual();
                    }
                }
        );

        /*
         * =====================================================
         * PAUSED
         * =====================================================
         */

        mediaPlayer.setOnPaused(
                () -> {

                    playPause.setText(
                            "▶"
                    );

                    /*
                     * Keep the beautiful visual visible
                     * while audio is paused, but stop
                     * the animation.
                     */

                    if (!isVideo(file)) {

                        if (audioAnimation != null) {

                            audioAnimation.pause();
                        }
                    }
                }
        );

        /*
         * =====================================================
         * STOPPED
         * =====================================================
         */

        mediaPlayer.setOnStopped(
                () -> {

                    playPause.setText(
                            "▶"
                    );

                    if (!isVideo(file)) {

                        if (audioAnimation != null) {

                            audioAnimation.pause();
                        }
                    }
                }
        );
    }

    /*
     * =========================================================
     * DISPOSE CURRENT PLAYER
     * =========================================================
     */

    private void disposeCurrentPlayer() {

        if (audioAnimation != null) {

            audioAnimation.stop();
        }

        audioVisual.setVisible(false);

        if (mediaPlayer != null) {

            try {

                mediaPlayer.stop();

            } catch (Exception ignored) {
            }

            try {

                mediaPlayer.dispose();

            } catch (Exception ignored) {
            }

            mediaPlayer = null;
        }

        mediaView.setMediaPlayer(
                null
        );

        mediaView.setVisible(
                false
        );

        emptyVideoLabel.setText(
                "Choose a media file with  Add Media"
        );

        emptyVideoLabel.setVisible(
                true
        );

        playPause.setText(
                "▶"
        );
    }

    /*
     * =========================================================
     * PLAY NEXT
     * =========================================================
     */

    private void playNext() {

        if (playlist.isEmpty()) {
            return;
        }

        int nextIndex;

        if (currentIndex < 0) {

            nextIndex = 0;

        } else {

            nextIndex =
                    (currentIndex + 1)
                            % playlist.size();
        }

        playlistView
                .getSelectionModel()
                .select(nextIndex);
    }

    /*
     * =========================================================
     * PLAY PREVIOUS
     * =========================================================
     */

    private void playPrevious() {

        if (playlist.isEmpty()) {
            return;
        }

        int previousIndex;

        if (currentIndex <= 0) {

            previousIndex =
                    playlist.size() - 1;

        } else {

            previousIndex =
                    currentIndex - 1;
        }

        playlistView
                .getSelectionModel()
                .select(previousIndex);
    }

    /*
     * =========================================================
     * PLAY / PAUSE
     * =========================================================
     */

    private void togglePlayPause() {

        if (mediaPlayer == null) {
            return;
        }

        MediaPlayer.Status status =
                mediaPlayer.getStatus();

        if (status ==
                MediaPlayer.Status.PLAYING) {

            mediaPlayer.pause();

            playPause.setText(
                    "▶"
            );

        } else {

            mediaPlayer.play();

            playPause.setText(
                    "Ⅱ"
            );
        }
    }

    /*
     * =========================================================
     * STOP
     * =========================================================
     */

    private void stopPlayback() {

        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.stop();

        setProgressSilently(0);

        Duration total =
                mediaPlayer.getTotalDuration();

        updateTime(
                Duration.ZERO,
                total
        );

        playPause.setText(
                "▶"
        );
    }

    /*
     * =========================================================
     * SEEK FROM SLIDER
     * =========================================================
     */

    private void seekFromSlider() {

        if (mediaPlayer == null) {
            return;
        }

        Duration total =
                mediaPlayer.getTotalDuration();

        if (total == null
                || total.isUnknown()
                || !total.greaterThan(
                        Duration.ZERO
                )) {

            return;
        }

        double value =
                progress.getValue();

        value =
                Math.max(
                        0,
                        Math.min(
                                1,
                                value
                        )
                );

        mediaPlayer.seek(
                total.multiply(value)
        );
    }

    /*
     * =========================================================
     * PROGRESS VALUE
     * =========================================================
     */

    private void setProgressSilently(
            double value) {

        updatingProgress = true;

        try {

            progress.setValue(
                    Math.max(
                            0,
                            Math.min(
                                    1,
                                    value
                            )
                    )
            );

        } finally {

            updatingProgress = false;
        }
    }

    /*
     * =========================================================
     * INCREASE VOLUME
     * =========================================================
     */

    private void increaseVolume() {

        double current =
                volume.getValue();

        double newVolume =
                Math.min(
                        1.0,
                        current + 0.05
                );

        if (muted) {

            muted = false;

            mute.setText(
                    "🔊"
            );
        }

        volume.setValue(
                newVolume
        );

        savedVolume =
                newVolume;

        if (mediaPlayer != null) {

            mediaPlayer.setVolume(
                    newVolume
            );
        }

        volumeLabel.setText(
                Math.round(
                        newVolume * 100
                ) + "%"
        );
    }

    /*
     * =========================================================
     * DECREASE VOLUME
     * =========================================================
     */

    private void decreaseVolume() {

        double current =
                volume.getValue();

        double newVolume =
                Math.max(
                        0.0,
                        current - 0.05
                );

        if (muted) {

            muted = false;

            mute.setText(
                    "🔊"
            );
        }

        volume.setValue(
                newVolume
        );

        savedVolume =
                newVolume;

        if (mediaPlayer != null) {

            mediaPlayer.setVolume(
                    newVolume
            );
        }

        volumeLabel.setText(
                Math.round(
                        newVolume * 100
                ) + "%"
        );
    }

    /*
     * =========================================================
     * MUTE
     * =========================================================
     */

    private void toggleMute() {

        if (!muted) {

            savedVolume =
                    volume.getValue();

            muted = true;

            if (mediaPlayer != null) {

                mediaPlayer.setVolume(
                        0
                );
            }

            mute.setText(
                    "🔇"
            );

        } else {

            muted = false;

            volume.setValue(
                    savedVolume
            );

            if (mediaPlayer != null) {

                mediaPlayer.setVolume(
                        savedVolume
                );
            }

            mute.setText(
                    "🔊"
            );
        }
    }

    /*
     * =========================================================
     * KEYBOARD CONTROLS
     * =========================================================
     *
     * SPACE = PLAY / PAUSE
     * S     = STOP
     * N     = NEXT
     * P     = PREVIOUS
     * UP    = VOLUME UP
     * DOWN  = VOLUME DOWN
     * M     = MUTE / UNMUTE
     */

    private void installKeyboardControls(
            Scene scene) {

        scene.addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {

                    KeyCode code =
                            event.getCode();

                    /*
                     * CTRL + K
                     */

                    if (event.isControlDown()
                            && code == KeyCode.K) {

                        searchField.requestFocus();

                        event.consume();

                        return;
                    }

                    /*
                     * Do not steal normal typing
                     * from text fields.
                     */

                    if (event.getTarget()
                            instanceof TextInputControl) {

                        return;
                    }

                    /*
                     * SPACE
                     */

                    if (code == KeyCode.SPACE) {

                        togglePlayPause();

                        event.consume();

                        return;
                    }

                    /*
                     * S
                     */

                    if (code == KeyCode.S) {

                        stopPlayback();

                        event.consume();

                        return;
                    }

                    /*
                     * N
                     */

                    if (code == KeyCode.N) {

                        playNext();

                        event.consume();

                        return;
                    }

                    /*
                     * P
                     */

                    if (code == KeyCode.P) {

                        playPrevious();

                        event.consume();

                        return;
                    }

                    /*
                     * UP
                     */

                    if (code == KeyCode.UP) {

                        increaseVolume();

                        event.consume();

                        return;
                    }

                    /*
                     * DOWN
                     */

                    if (code == KeyCode.DOWN) {

                        decreaseVolume();

                        event.consume();

                        return;
                    }

                    /*
                     * M
                     */

                    if (code == KeyCode.M) {

                        toggleMute();

                        event.consume();

                        return;
                    }
                }
        );

        /*
         * PROGRESS SLIDER
         */

        progress.valueChangingProperty()
                .addListener(
                        (obs, oldValue, changing) -> {

                            if (!changing
                                    && !updatingProgress) {

                                seekFromSlider();
                            }
                        }
                );

        /*
         * Clicking progress bar seeks immediately.
         */

        progress.setOnMouseReleased(
                event -> {

                    if (!updatingProgress) {

                        seekFromSlider();
                    }
                }
        );
    }

    /*
     * =========================================================
     * SEARCH / FILTER
     * =========================================================
     */

    private void filterPlaylist(
            String query) {

        if (query == null
                || query.trim().isEmpty()) {

            return;
        }

        String text =
                query.trim()
                        .toLowerCase();

        for (int i = 0;
             i < playlist.size();
             i++) {

            File file =
                    playlist.get(i);

            if (file.getName()
                    .toLowerCase()
                    .contains(text)) {

                playlistView
                        .getSelectionModel()
                        .select(i);

                playlistView
                        .scrollTo(i);

                return;
            }
        }
    }

    /*
     * =========================================================
     * TIME DISPLAY
     * =========================================================
     */

    private void updateTime(
            Duration current,
            Duration total) {

        timeLabel.setText(
                formatDuration(current)
                        + " / "
                        + formatDuration(total)
        );
    }

    /*
     * =========================================================
     * FORMAT DURATION
     * =========================================================
     */

    private String formatDuration(
            Duration duration) {

        if (duration == null
                || duration.isUnknown()
                || duration.isIndefinite()
                || duration.lessThan(
                        Duration.ZERO
                )) {

            return "00:00";
        }

        long totalSeconds =
                (long) Math.floor(
                        duration.toSeconds()
                );

        long hours =
                totalSeconds / 3600;

        long minutes =
                (totalSeconds % 3600) / 60;

        long seconds =
                totalSeconds % 60;

        if (hours > 0) {

            return String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    /*
     * =========================================================
     * MEDIA TYPE
     * =========================================================
     */

    private boolean isVideo(File file) {

        if (file == null) {
            return false;
        }

        String name =
                file.getName()
                        .toLowerCase();

        return name.endsWith(".mp4")
                || name.endsWith(".m4v");
    }

    /*
     * =========================================================
     * MEDIA ERROR
     * =========================================================
     */

    private void showMediaErrorOnce(
            File file,
            MediaException error) {

        if (mediaErrorShown) {
            return;
        }

        mediaErrorShown = true;

        String reason =
                error != null
                        ? error.getMessage()
                        : "JavaFX could not decode the media.";

        showError(
                buildMediaErrorMessage(
                        file,
                        reason
                )
        );
    }

    /*
     * =========================================================
     * MEDIA LOAD ERROR
     * =========================================================
     */

    private void showMediaLoadError(
            File file,
            Exception exception) {

        String reason =
                exception != null
                        ? exception.getMessage()
                        : "Unknown error";

        showError(
                buildMediaErrorMessage(
                        file,
                        reason
                )
        );
    }

    /*
     * =========================================================
     * BUILD MEDIA ERROR
     * =========================================================
     */

    private String buildMediaErrorMessage(
            File file,
            String reason) {

        StringBuilder message =
                new StringBuilder();

        message.append(
                "Could not play this media file.\n\n"
        );

        message.append(
                "File: "
        );

        message.append(
                file != null
                        ? file.getName()
                        : "Unknown"
        );

        message.append(
                "\n\nReason:\n"
        );

        message.append(
                reason != null
                        ? reason
                        : "Unknown media error."
        );

        if (file != null
                && isVideo(file)) {

            message.append(
                    "\n\n"
                            + "For JavaFX video playback, use:\n"
                            + "• MP4 container\n"
                            + "• H.264 / AVC video\n"
                            + "• AAC audio\n"
                            + "• 8-bit YUV 4:2:0 video\n\n"
                            + "If this MP4 still fails, its encoding "
                            + "may contain a feature that the JavaFX "
                            + "media engine cannot decode."
            );
        }

        return message.toString();
    }

    /*
     * =========================================================
     * EXTENSION
     * =========================================================
     */

    private String stripExtension(
            String name) {

        if (name == null) {
            return "";
        }

        int dot =
                name.lastIndexOf('.');

        if (dot > 0) {

            return name.substring(
                    0,
                    dot
            );
        }

        return name;
    }

    /*
     * =========================================================
     * PLAYLIST MESSAGE
     * =========================================================
     */

    private void showPlaylistMessage() {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "Playlist categories are available "
                                + "in the NOVA interface.\n\n"
                                + "Use Add Media to add your local "
                                + "audio and video files.",
                        ButtonType.OK
                );

        alert.setTitle(
                "NOVA Playlists"
        );

        alert.setHeaderText(
                "My Playlists"
        );

        alert.showAndWait();
    }

    /*
     * =========================================================
     * GENERAL ERROR
     * =========================================================
     */

    private void showError(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR,
                        message,
                        ButtonType.OK
                );

        alert.setTitle(
                "NOVA Media Player"
        );

        alert.setHeaderText(
                "Media Error"
        );

        alert.showAndWait();
    }

    /*
     * =========================================================
     * APPLICATION STOP
     * =========================================================
     */

    @Override
    public void stop() {

        if (audioAnimation != null) {

            audioAnimation.stop();
        }

        disposeCurrentPlayer();
    }

    /*
     * =========================================================
     * MAIN
     * =========================================================
     */

    public static void main(
            String[] args) {

        launch(args);
    }
}