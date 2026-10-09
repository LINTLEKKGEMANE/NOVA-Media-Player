package com;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import javafx.scene.input.KeyEvent;
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
     * MEDIA
     * =========================================================
     */

    private MediaPlayer mediaPlayer;

    private final MediaView mediaView =
            new MediaView();

    /*
     * =========================================================
     * AUDIO VISUALIZER
     * =========================================================
     */

    private final StackPane audioVisual =
            new StackPane();

    private ParallelTransition audioAnimation;

    /*
     * =========================================================
     * LABELS
     * =========================================================
     */

    private final Label titleLabel =
            new Label("No media selected");

    private final Label artistLabel =
            new Label("Spotfire Media Player");

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
     * SLIDERS
     * =========================================================
     */

    private final Slider progress =
            new Slider(0, 1, 0);

    private final Slider volume =
            new Slider(0, 1, 0.70);

    /*
     * =========================================================
     * BUTTONS
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

    private boolean mediaErrorShown = false;

    /*
     * =========================================================
     * START
     * =========================================================
     */

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        root.getStyleClass().add("root");

        root.setTop(buildHeader(stage));
        root.setLeft(buildSidebar());
        root.setCenter(buildMainContent());
        root.setRight(buildPlaylistPanel());
        root.setBottom(buildBottomPlaylist());

        // Reduced window size to fit under 1000x650
        Scene scene = new Scene(
                root,
                950,
                620
        );

        loadStylesheet(scene);

        installKeyboardControls(scene);

        stage.setTitle("SPOTFIRE Media Player");

        stage.setMinWidth(820);
        stage.setMinHeight(560);

        stage.setScene(scene);

        stage.setWidth(950);
        stage.setHeight(620);

        stage.centerOnScreen();

        stage.show();
    }

    /*
     * =========================================================
     * CSS
     * =========================================================
     */

    private void loadStylesheet(Scene scene) {

        if (getClass().getResource("styles.css") != null) {

            scene.getStylesheets().add(
                    getClass()
                            .getResource("styles.css")
                            .toExternalForm()
            );

            return;
        }

        if (getClass().getResource("/styles.css") != null) {

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/styles.css")
                            .toExternalForm()
            );

            return;
        }

        System.out.println(
                "SPOTFIRE: styles.css was not found."
        );
    }

    /*
     * =========================================================
     * HEADER
     * =========================================================
     */

    private HBox buildHeader(Stage stage) {

        HBox header = new HBox(18);

        header.setAlignment(Pos.CENTER_LEFT);

        header.getStyleClass().add("header");

        header.setPadding(
                new Insets(
                        12,
                        20,
                        12,
                        22
                )
        );

        Label logoIcon = new Label("▶");

        logoIcon.getStyleClass().add("logo-icon");

        Label nova = new Label("SPOTFIRE");

        nova.getStyleClass().add("logo-text");

        Label subtitle = new Label("Media Player");

        subtitle.getStyleClass().add("header-subtitle");

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        searchField.setPromptText(
                "Search media, artists, or files..."
        );

        searchField.getStyleClass().add(
                "search-field"
        );

        searchField.setPrefWidth(300);
        searchField.setMinWidth(150);
        searchField.setMaxWidth(420);

        searchField.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        filterPlaylist(newValue)
        );

        header.getChildren().addAll(
                logoIcon,
                nova,
                subtitle,
                spacer,
                searchField
        );

        return header;
    }

    /*
     * =========================================================
     * SETTINGS
     * =========================================================
     */

    private void showSettings() {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "SPOTFIRE Media Player\n\n"
                        + "Local audio and video playback "
                        + "is supported.\n\n"
                        + "You can add local media files "
                        + "using the Add Media button.\n\n"
                        + "Keyboard Controls:\n"
                        + "Space - Play / Pause\n"
                        + "S - Stop\n"
                        + "N - Next\n"
                        + "P - Previous\n"
                        + "Up Arrow - Volume Up\n"
                        + "Down Arrow - Volume Down\n"
                        + "M - Mute / Unmute",
                ButtonType.OK
        );

        alert.setTitle("SPOTFIRE Settings");

        alert.setHeaderText(
                "SPOTFIRE Media Player"
        );

        alert.showAndWait();
    }

    /*
     * =========================================================
     * SIDEBAR
     * =========================================================
     */

    private VBox buildSidebar() {

        VBox side = new VBox(10);

        side.getStyleClass().add("sidebar");

        side.setPadding(
                new Insets(
                        20,
                        14,
                        14,
                        14
                )
        );

        side.setPrefWidth(190);
        side.setMinWidth(160);
        side.setMaxWidth(210);

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

        Label my = new Label("MY PLAYLISTS");

        my.getStyleClass().add(
                "section-label"
        );

        VBox.setMargin(
                my,
                new Insets(
                        18,
                        0,
                        4,
                        12
                )
        );

        Button addPlaylist = new Button("+");

        addPlaylist.getStyleClass().add(
                "plus-button"
        );

        addPlaylist.setOnAction(
                e -> showPlaylistMessage()
        );

        Region playlistSpacer = new Region();

        HBox.setHgrow(
                playlistSpacer,
                Priority.ALWAYS
        );

        HBox playlistTitle = new HBox(
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

        Button button = new Button(
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

        Label iconLabel = new Label(icon);

        iconLabel.getStyleClass().add(
                "category-icon"
        );

        Label nameLabel = new Label(name);

        nameLabel.getStyleClass().add(
                "category-name"
        );

        Label countLabel = new Label(count);

        countLabel.getStyleClass().add(
                "category-count"
        );

        VBox text = new VBox(
                3,
                nameLabel,
                countLabel
        );

        HBox box = new HBox(
                12,
                iconLabel,
                text
        );

        box.setAlignment(
                Pos.CENTER_LEFT
        );

        box.setPadding(
                new Insets(
                        6,
                        8,
                        6,
                        8
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

        VBox main = new VBox(12);

        main.getStyleClass().add("main");

        main.setPadding(
                new Insets(
                        14,
                        16,
                        14,
                        16
                )
        );

        Label now = new Label("Now Playing");

        now.getStyleClass().add(
                "page-title"
        );

        StackPane videoCard = new StackPane();

        videoCard.getStyleClass().add(
                "video-card"
        );

        videoCard.setMinHeight(180);
        videoCard.setPrefHeight(240);

        /*
         * VIDEO VIEW
         */

        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);

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
         * AUDIO VISUALIZER
         */

        buildAudioVisualizer();

        audioVisual.setVisible(false);

        audioVisual.setMouseTransparent(true);

        videoCard.getChildren().add(
                audioVisual
        );

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

        Label type = new Label("Media");

        type.getStyleClass().add(
                "tag"
        );

        HBox detailsRow = new HBox(
                12,
                titleLabel,
                type
        );

        detailsRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox information = new VBox(
                5,
                detailsRow,
                artistLabel
        );

        /*
         * ACTION BUTTONS
         */

        HBox actionRow = new HBox(10);

        actionRow.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button favorite = new Button("♡");

        favorite.getStyleClass().add(
                "round-action"
        );

        Button more = new Button("•••");

        more.getStyleClass().add(
                "round-action"
        );

        actionRow.getChildren().addAll(
                favorite,
                more
        );

        /*
         * TRANSPORT
         */

        HBox controls = new HBox(20);

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

        mute.setPadding(new Insets(0, 0, 0, 30));

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

        HBox progressRow = new HBox(10);

        progressRow.setAlignment(
                Pos.CENTER_LEFT
        );

        progress.setMinWidth(80);

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
         *
         * Redesigned to look distinct from the transport row.
         */

        HBox volumeRow = new HBox(12);

        volumeRow.setAlignment(
                Pos.CENTER_LEFT
        );

        volumeRow.setPadding(new Insets(8, 14, 8, 14));
        volumeRow.setStyle(
                "-fx-background-color: rgba(124, 92, 255, 0.08);" +
                "-fx-background-radius: 8;"
        );

        Label volumeTitle = new Label("Volume");
        volumeTitle.setStyle(
                "-fx-text-fill: #a0a0a0;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );

        volume.setMaxWidth(Double.MAX_VALUE);
        volume.setPrefWidth(300);
        HBox.setHgrow(volume, Priority.ALWAYS);

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

        volumeLabel.setStyle(
                "-fx-text-fill: #7c5cff;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;"
        );

        Label quiet = new Label("🔇");
        quiet.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #666666;"
        );

        volumeRow.getChildren().addAll(
                volumeTitle,
                volume,
                volumeLabel,
                quiet
        );

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
     * AUDIO VISUALIZER
     * =========================================================
     */

    private void buildAudioVisualizer() {

        audioVisual.getChildren().clear();

        Circle glow = new Circle(105);

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

        Circle disc = new Circle(82);

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

        Circle innerDisc = new Circle(57);

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

        Circle center = new Circle(10);

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

        HBox bars = new HBox(7);

        bars.setAlignment(
                Pos.CENTER
        );

        double[] heights = {
                28, 48, 72, 42, 88,
                55, 76, 38, 64, 45, 82
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

            animation.setDelay(
                    Duration.millis(i * 70)
            );

            animations.add(animation);

            bars.getChildren().add(bar);
        }

        audioVisual.getChildren().addAll(
                glow,
                disc,
                innerDisc,
                bars,
                center
        );

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
     * AUDIO VISUAL
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
     * PLAYLIST PANEL
     * =========================================================
     */

    private VBox buildPlaylistPanel() {

        VBox panel = new VBox(10);

        panel.getStyleClass().add(
                "playlist-panel"
        );

        panel.setPadding(
                new Insets(
                        18,
                        14,
                        14,
                        14
                )
        );

        panel.setPrefWidth(230);
        panel.setMinWidth(200);
        panel.setMaxWidth(260);

        HBox heading = new HBox();

        Label label = new Label("Playlist");

        label.getStyleClass().add(
                "panel-title"
        );

        Label count = new Label("0 items");

        count.getStyleClass().add(
                "item-count"
        );

        Region spacer = new Region();

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

        playlistView.setPlaceholder(
                new Label("No media yet")
        );

        playlistView.getStyleClass().add(
                "playlist-view"
        );

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
                                        10,
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
                new Button("+  Add Media");

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
                new Button("▢  Remove");

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
                        8,
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
                new HBox(14);

        bottom.getStyleClass().add(
                "bottom-strip"
        );

        bottom.setPadding(
                new Insets(
                        8,
                        18,
                        8,
                        18
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
                new HBox(10);

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
                                            3,
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

                                name.setWrapText(true);

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
                                                2,
                                                name,
                                                type
                                        );

                                HBox card =
                                        new HBox(
                                                6,
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
     * CHOOSE MEDIA
     * =========================================================
     */

    private void chooseFiles() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Select Audio or Video"
        );

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "All Media Files",
                        "*.mp3",
                        "*.wav",
                        "*.m4a",
                        "*.aac",
                        "*.aiff",
                        "*.aif",
                        "*.mp4",
                        "*.m4v",
                        "*.mov",
                        "*.avi",
                        "*.mkv",
                        "*.wmv",
                        "*.flv",
                        "*.webm",
                        "*.mpeg",
                        "*.mpg",
                        "*.3gp",
                        "*.3g2",
                        "*.ts",
                        "*.m2ts",
                        "*.mts",
                        "*.ogv",
                        "*.vob",
                        "*.asf",
                        "*.f4v",
                        "*.m2v",
                        "*.divx"
                )
        );

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "All Files",
                        "*.*"
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

        boolean playlistWasEmpty =
                playlist.isEmpty();

        for (File file : files) {

            if (file == null
                    || !file.exists()
                    || !file.isFile()
                    || playlist.contains(file)) {

                continue;
            }

            playlist.add(file);
        }

        if (playlistWasEmpty
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
                    "Spotfire Media Player"
            );

            timeLabel.setText(
                    "00:00 / 00:00"
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

        if (file == null
                || !file.exists()
                || !file.isFile()) {

            showError(
                    "The selected media file does not exist:\n\n"
                            + (
                            file != null
                                    ? file.getAbsolutePath()
                                    : "Unknown file"
                    )
            );

            return;
        }

        disposeCurrentPlayer();

        currentIndex = index;

        mediaErrorShown = false;

        setProgressSilently(0);

        timeLabel.setText(
                "00:00 / 00:00"
        );

        boolean video =
                isVideo(file);

        mediaView.setVisible(false);

        emptyVideoLabel.setVisible(false);

        hideAudioVisual();

        final String source;

        try {

            source =
                    file.toPath()
                            .toUri()
                            .toString();

        } catch (Exception ex) {

            showMediaLoadError(
                    file,
                    ex
            );

            return;
        }

        final Media media;

        try {

            media =
                    new Media(source);

        } catch (Exception ex) {

            showMediaLoadError(
                    file,
                    ex
            );

            return;
        }

        if (media.getError() != null) {

            showMediaErrorOnce(
                    file,
                    media.getError()
            );

            return;
        }

        media.setOnError(
                () -> showMediaErrorOnce(
                        file,
                        media.getError()
                )
        );

        final MediaPlayer player;

        try {

            player =
                    new MediaPlayer(media);

        } catch (Exception ex) {

            showMediaLoadError(
                    file,
                    ex
            );

            return;
        }

        mediaPlayer = player;

        mediaView.setMediaPlayer(
                player
        );

        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);

        if (video) {

            emptyVideoLabel.setText(
                    "Loading video..."
            );

            emptyVideoLabel.setVisible(true);

            mediaView.setVisible(false);

        } else {

            showAudioVisual();
        }

        player.setVolume(
                muted
                        ? 0
                        : volume.getValue()
        );

        titleLabel.setText(
                stripExtension(
                        file.getName()
                )
        );

        artistLabel.setText(
                video
                        ? "Local Video"
                        : "Local Audio"
        );

        player.setOnReady(
                () -> {

                    if (mediaPlayer != player) {
                        return;
                    }

                    Duration total =
                            player.getTotalDuration();

                    updateTime(
                            Duration.ZERO,
                            total
                    );

                    if (video) {

                        mediaView.setMediaPlayer(
                                player
                        );

                        mediaView.setPreserveRatio(
                                true
                        );

                        mediaView.setSmooth(
                                true
                        );

                        hideAudioVisual();

                        emptyVideoLabel.setVisible(
                                false
                        );

                        mediaView.setVisible(
                                true
                        );

                    } else {

                        mediaView.setVisible(
                                false
                        );

                        emptyVideoLabel.setVisible(
                                false
                        );

                        showAudioVisual();
                    }

                    player.play();
                }
        );

        player.currentTimeProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            if (mediaPlayer != player) {
                                return;
                            }

                            Duration total =
                                    player.getTotalDuration();

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

        player.setOnEndOfMedia(
                this::playNext
        );

        player.setOnError(
                () -> showMediaErrorOnce(
                        file,
                        player.getError()
                )
        );

        player.setOnPlaying(
                () -> {

                    if (mediaPlayer != player) {
                        return;
                    }

                    playPause.setText(
                            "Ⅱ"
                    );

                    if (video) {

                        hideAudioVisual();

                        mediaView.setMediaPlayer(
                                player
                        );

                        mediaView.setVisible(
                                true
                        );

                        emptyVideoLabel.setVisible(
                                false
                        );

                    } else {

                        showAudioVisual();
                    }
                }
        );

        player.setOnPaused(
                () -> {

                    if (mediaPlayer != player) {
                        return;
                    }

                    playPause.setText(
                            "▶"
                    );

                    if (!video
                            && audioAnimation != null) {

                        audioAnimation.pause();
                    }
                }
        );

        player.setOnStopped(
                () -> {

                    if (mediaPlayer != player) {
                        return;
                    }

                    playPause.setText(
                            "▶"
                    );

                    if (!video
                            && audioAnimation != null) {

                        audioAnimation.pause();
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

        try {

            mediaView.setMediaPlayer(null);

        } catch (Exception ignored) {
        }

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

        mediaView.setVisible(false);

        emptyVideoLabel.setText(
                "Choose a media file with  Add Media"
        );

        emptyVideoLabel.setVisible(true);

        playPause.setText(
                "▶"
        );
    }

    /*
     * =========================================================
     * NEXT
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
     * PREVIOUS
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

        } else if (
                status != MediaPlayer.Status.DISPOSED
                && status != MediaPlayer.Status.UNKNOWN
        ) {

            mediaPlayer.play();
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
     * SEEK
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
                Math.max(
                        0,
                        Math.min(
                                1,
                                progress.getValue()
                        )
                );

        mediaPlayer.seek(
                total.multiply(value)
        );
    }

    /*
     * =========================================================
     * PROGRESS
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
     * VOLUME UP
     * =========================================================
     */

    private void increaseVolume() {

        double newVolume =
                Math.min(
                        1.0,
                        volume.getValue() + 0.05
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
    }

    /*
     * =========================================================
     * VOLUME DOWN
     * =========================================================
     */

    private void decreaseVolume() {

        double newVolume =
                Math.max(
                        0.0,
                        volume.getValue() - 0.05
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

                mediaPlayer.setVolume(0);
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
     */

    private void installKeyboardControls(
            Scene scene) {

        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {

                    KeyCode code =
                            event.getCode();

                    if (event.isControlDown()
                            && code == KeyCode.K) {

                        searchField.requestFocus();

                        event.consume();

                        return;
                    }

                    if (event.getTarget()
                            instanceof TextInputControl) {

                        return;
                    }

                    if (code == KeyCode.SPACE) {

                        togglePlayPause();

                        event.consume();

                    } else if (code == KeyCode.S) {

                        stopPlayback();

                        event.consume();

                    } else if (code == KeyCode.N) {

                        playNext();

                        event.consume();

                    } else if (code == KeyCode.P) {

                        playPrevious();

                        event.consume();

                    } else if (code == KeyCode.UP) {

                        increaseVolume();

                        event.consume();

                    } else if (code == KeyCode.DOWN) {

                        decreaseVolume();

                        event.consume();

                    } else if (code == KeyCode.M) {

                        toggleMute();

                        event.consume();
                    }
                }
        );

        progress.valueChangingProperty()
                .addListener(
                        (obs, oldValue, changing) -> {

                            if (!changing
                                    && !updatingProgress) {

                                seekFromSlider();
                            }
                        }
                );

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
     * SEARCH
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
                        .toLowerCase(
                                Locale.ROOT
                        );

        for (int i = 0;
             i < playlist.size();
             i++) {

            File file =
                    playlist.get(i);

            if (file.getName()
                    .toLowerCase(
                            Locale.ROOT
                    )
                    .contains(text)) {

                playlistView
                        .getSelectionModel()
                        .select(i);

                playlistView.scrollTo(i);

                return;
            }
        }
    }

    /*
     * =========================================================
     * TIME
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
     * VIDEO DETECTION
     * =========================================================
     */

    private boolean isVideo(File file) {

        if (file == null) {
            return false;
        }

        String name =
                file.getName()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return name.endsWith(".mp4")
                || name.endsWith(".m4v")
                || name.endsWith(".mov")
                || name.endsWith(".avi")
                || name.endsWith(".mkv")
                || name.endsWith(".wmv")
                || name.endsWith(".flv")
                || name.endsWith(".webm")
                || name.endsWith(".mpeg")
                || name.endsWith(".mpg")
                || name.endsWith(".3gp")
                || name.endsWith(".3g2")
                || name.endsWith(".ts")
                || name.endsWith(".m2ts")
                || name.endsWith(".mts")
                || name.endsWith(".ogv")
                || name.endsWith(".vob")
                || name.endsWith(".asf")
                || name.endsWith(".f4v")
                || name.endsWith(".m2v")
                || name.endsWith(".divx");
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

        String reason;

        if (error != null
                && error.getMessage() != null
                && !error.getMessage().isBlank()) {

            reason =
                    error.getMessage();

        } else {

            reason =
                    "JavaFX reported ERROR_MEDIA_INVALID.";
        }

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

        String reason;

        if (exception != null
                && exception.getMessage() != null
                && !exception.getMessage().isBlank()) {

            reason =
                    exception.getMessage();

        } else {

            reason =
                    "JavaFX could not open the media file.";
        }

        showError(
                buildMediaErrorMessage(
                        file,
                        reason
                )
        );
    }

    /*
     * =========================================================
     * MEDIA ERROR MESSAGE
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
                        && !reason.isBlank()
                        ? reason
                        : "JavaFX could not decode the media."
        );

        message.append(
                "\n\n"
        );

        message.append(
                "The file was accepted by SPOTFIRE. "
                        + "JavaFX is attempting to play it "
                        + "using its built-in media engine."
        );

        return message.toString();
    }

    /*
     * =========================================================
     * FILE NAME
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
                                + "in the SPOTFIRE interface.\n\n"
                                + "Use Add Media to add your "
                                + "local audio and video files.",
                        ButtonType.OK
                );

        alert.setTitle(
                "SPOTFIRE Playlists"
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
                "SPOTFIRE Media Player"
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