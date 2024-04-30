package lk.ijse.dep12.shared.image.to;

import java.io.Serializable;

public class ImageFile implements Serializable {

    private byte[] imageBytes = new byte[1024 * 1024 * 10];

    public ImageFile(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
}
