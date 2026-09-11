package com.hms.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Handles profile-picture storage (copying an uploaded file into the app's
 * local data folder under a generated name) and rendering it as a circular
 * avatar. Kept separate from any one controller since both the top-bar
 * avatar and the Profile dialog need the same rendering logic.
 */
public final class ImageUtil {

    private static final String AVATAR_FOLDER = System.getProperty("user.home") + "/.hms/avatars";
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of("png", "jpg", "jpeg", "gif");

    private ImageUtil() {
    }

    /**
     * Copies the given source image file into the app's local avatar
     * storage folder under a fresh UUID name and returns the absolute path
     * to store on the User record. Validates type and size first.
     */
    public static String storeAvatar(Path sourceFile) throws ValidationException {
        String fileName = sourceFile.getFileName().toString().toLowerCase();
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Please choose a PNG, JPG, or GIF image.");
        }
        try {
            if (Files.size(sourceFile) > MAX_FILE_SIZE_BYTES) {
                throw new ValidationException("Image is too large - please choose a file under 5 MB.");
            }
            Files.createDirectories(Paths.get(AVATAR_FOLDER));
            String storedName = UUID.randomUUID() + "." + extension;
            Path destination = Paths.get(AVATAR_FOLDER, storedName);
            Files.copy(sourceFile, destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new ValidationException("Could not save the image: " + e.getMessage());
        }
    }

    /** Loads an avatar image from disk, or null if the path is missing/unreadable. */
    public static Image loadAvatar(String avatarPath) {
        if (avatarPath == null || avatarPath.isBlank()) {
            return null;
        }
        Path path = Paths.get(avatarPath);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return new Image(path.toUri().toString(), 96, 96, true, true);
        } catch (Exception e) {
            return null;
        }
    }

    /** Applies a circular clip to an ImageView so any square/rectangular photo renders as a round avatar. */
    public static void makeCircular(ImageView imageView, double radius) {
        Circle clip = new Circle(radius, radius, radius);
        imageView.setClip(clip);
        imageView.setFitWidth(radius * 2);
        imageView.setFitHeight(radius * 2);
        imageView.setPreserveRatio(false);
    }
}
