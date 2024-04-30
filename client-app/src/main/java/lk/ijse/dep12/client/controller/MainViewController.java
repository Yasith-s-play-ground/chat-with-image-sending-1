package lk.ijse.dep12.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lk.ijse.dep12.shared.image.to.ImageFile;

import java.io.*;
import java.net.Socket;

public class MainViewController {
    //public TextArea txtDisplay;
    public TextField txtMessage;
    public Button btnSend;
    public ScrollPane scrlPane;
    public VBox vBoxMain;
    public Button btnAttach;
    private Socket remoteSocket;
    private String nickName;

    private ObjectOutputStream oos;

    public void btnSendOnAction(ActionEvent actionEvent) throws IOException {
        Label messageLabel = new Label("You : " + txtMessage.getText().strip() + "\n");
        vBoxMain.getChildren().add(messageLabel);
        //txtDisplay.appendText("You : " + txtMessage.getText().strip() + "\n");
        String message = nickName + ": " + txtMessage.getText().strip() + "\n";

        //sending as a String object
//        try (ObjectOutputStream oos = new ObjectOutputStream(remoteSocket.getOutputStream())) {
//            oos.writeObject(message);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        //ObjectOutputStream oos = new ObjectOutputStream(remoteSocket.getOutputStream());
        oos.writeObject(message);
        //remoteSocket.getOutputStream().write(message.getBytes());

        txtMessage.requestFocus();
        txtMessage.clear();
    }

    public void initData(Socket remoteSocket, String nickName) throws IOException {
        this.remoteSocket = remoteSocket;
        oos = new ObjectOutputStream(remoteSocket.getOutputStream());

        this.nickName = nickName;
        appendText("You entered into the chat room \n");

        new Thread(() -> {

            while (true) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(remoteSocket.getInputStream());

                    Object object = ois.readObject();
                    if (object instanceof ImageFile) {
                        System.out.println("this is a image file");

                        File output = new File(new File(System.getenv("HOME"), "Desktop"), "temp" + System.currentTimeMillis());
                        ImageFile image = (ImageFile) object;
                        byte[] imageBytes = image.getImageBytes();
                        System.out.println(output.getName());
                        FileOutputStream fos = new FileOutputStream(output);
                        fos.write(imageBytes);

                        setImageToChat(output);
                    } else if (object instanceof String) {
                        System.out.println("this is a string");
                        String message = (String) object;
                        appendText(message);
                    }


                    //oldest method to send text messages
//                byte[] byteBuffer = new byte[1024];
//                while (true) {
//                    int read = is.read(byteBuffer);
//                    if (read == -1) {
//                        appendText("Connection lost");
//                        Platform.runLater(() -> {
//                            btnSend.setDisable(true);
//                            txtMessage.setDisable(true);
//                        });
//                        break;
//                    }
//                    String message = new String(byteBuffer, 0, read);
//                    Arrays.fill(byteBuffer, (byte) 0); // fill array with 0 again
//                    appendText(message);
//                }
                } catch (IOException e) {
                    if (remoteSocket.isConnected())
                        throw new RuntimeException(e); // when socket is closed when window closed
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void appendText(String message) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(message);
            vBoxMain.getChildren().add(messageLabel);
        });
    }

    public void btnAttachOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser(); // predefined class to access files
        fileChooser.setTitle("Open image files");

        //applying an extension filter to get jpeg images
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG Image", "*.jpeg", "*.jpg"));

        // for gif
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GIF Image", "*.gif"));

        //for all files
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        //to select multiple files
        //List<File> files = fileChooser.showOpenMultipleDialog(btnOpen.getScene().getWindow());

        //to select a single file
        File file = fileChooser.showOpenDialog(btnAttach.getScene().getWindow());// as a modal window
// if we don't want as a modal window, can put null

        if (file == null) {
            txtMessage.setText("No file selected");
        } else {
            //setting image in own chat window
            setImageToChat(file);



            ImageFile image = null;
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] imageBytes = new byte[(int) file.length()];
                int read = fis.read(imageBytes); // read = no of bytes read , not the size of array
                image = new ImageFile(imageBytes);
                oos.writeObject(image);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void setImageToChat(File image) {
        Platform.runLater(() -> {
            ImageView imageView = new ImageView(image.toURI().toString());// give path as a URL
            imageView.setFitWidth(200); // set width of image view
            imageView.setPreserveRatio(true);
            vBoxMain.getChildren().add(imageView);
        });

    }
}
