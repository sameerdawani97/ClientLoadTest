import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.google.gson.Gson;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet("/albums/")
public class Albums extends javax.servlet.http.HttpServlet {
  // Assuming 'albums' is a list of albums you want to access
  private List<Album> albums;
  Album album = new Album("Sex Pistols","Never Mind The Bollocks!","1977");

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");

    String partToCheck = urlParts[1]; // Assuming you want to check the fourth part
    int id = 0;
    try {
      id = Integer.parseInt(partToCheck);
    } catch (NumberFormatException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Not integer");
      return;
    }

    //String idParam = request.getParameter("id");

    // Simple validation: Check if the ID is an integer
    try {

      if (album != null) {
        //String employeeJsonString = new Gson().toJson(album);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{\"artist\": \"" + album.getArtist() + "\", \"Title\": \"" + album.getTitle() + "\", \"year\": \"" + album.getYear() + "\"}");
        out.flush();
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (NumberFormatException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().print("{\"message\": \"Invalid Request\"}");
    }
  }

  private Album findAlbumById(int id) {
    // Implement your logic here to find the album by ID from the 'albums' list
    // Return the album object or null if not found.
    for (Album album : albums) {
      if (album.getId() == id) {
        return album;
      }
    }
    return null;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {


    if (ServletFileUpload.isMultipartContent(request)) {
      try{
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);

        String albumID = null;
        long imageSize = 0;

        for (FileItem item : items) {
          if (item.isFormField()) {
            // Handle regular form fields
            if ("AlbumID".equals(item.getFieldName())) {
              albumID = item.getString();
            }
          } else {
            // Handle uploaded files (e.g., image)
            if ("Image".equals(item.getFieldName())) {
              // Process or save the uploaded image file as needed
              imageSize = item.getSize();
              // For simplicity, let's calculate and return the size of the image.
            }
          }
        }
        if (imageSize!=0){
          // Here, you can process the album ID and image size
          // For simplicity, let's return them as a JSON response
          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType("application/json");
          response.getWriter().write(
              "{\"AlbumID\": \"" + String.valueOf(1) + "\", \"ImageSize\": \""
                  + String.valueOf(imageSize) + "\"}");
        }
        else {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write("{\"error\": \"Invalid request\"}");
        }


      } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("{\"error\": \"Internal Server Error\"}");
        e.printStackTrace();
      }
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("{\"error\": \"Invalid request\"}");
    }
  }
}

