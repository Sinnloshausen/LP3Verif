package solver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Class that handles the creation of and writing into a file.
 */
public class FileHandler {

  // class fields
  private String filePath;
  private String fileName;

  /**
   * Constructor that is called to create a file.
   * 
   * @param filePath
   *          the path of the file
   * @param fileName
   *          the name of the file to create
   */
  public FileHandler(String filePath, String fileName) {
    this.fileName = fileName;
    this.filePath = filePath;
  }

  /**
   * Method that write the content of lines into the file.
   * 
   * @param lines
   *          a byte array with content to write into a file
   * @return true, if successful
   */
  public boolean writeFile(byte[] lines) {
    String fullPath = filePath + File.separator + fileName;
    File file = new File(fullPath);
    try {
      if (!file.createNewFile()) {
        // TODO maybe ask the user?
        // System.out.println("File already exists. Overwriting...");
      }
      // write into the file
      Files.write(file.toPath(), lines);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    // success
    return true;
  }

}
