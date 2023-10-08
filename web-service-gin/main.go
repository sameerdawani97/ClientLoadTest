package main

import (
	"fmt"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type respondPostBody struct {
	AlbumID   string `json:"albumID"`
	ImageSize string `json:"imageSize"`
	// Artist    string `json:"artist"`
	// Title     string `json:"Title"`
	// Year      string `json:"year"`
}

type requestPostBody struct {
	Image  string `json:"image"`
	Artist string `json:"artist"`
	Title  string `json:"Title"`
	Year   string `json:"year"`
}

// album represents data about a record album.
type album struct {
	Artist string `json:"artist"`
	Title  string `json:"Title"`
	Year   string `json:"year"`
}

// albums slice to seed record album data.
var albums = []album{
	{Artist: "Sex Pistols", Title: "Never Mind The Bollocks!", Year: "1977"},
}

func main() {
	router := gin.Default()
	//router.GET("/albums", getAlbums)
	router.GET("/albums/:id", getAlbumByID)
	router.POST("/albums", postAlbums)

	router.Run(":8080")
}

// postAlbums adds an album from JSON received in the request body.
func postAlbums(c *gin.Context) {
	// Parse form data including the image file and other properties.
	form, _ := c.MultipartForm()
	imageFileHeaders := form.File["Image"]
	otherProperties := form.Value

	// Handle the uploaded image file.
	if len(imageFileHeaders) > 0 && len(otherProperties) > 0 {
		imageFileHeader := imageFileHeaders[0]
		imageFile, err := imageFileHeader.Open()
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid request"})
			return
		}
		defer imageFile.Close()

		// Here, you can process or save the image file as needed.
		// For simplicity, let's calculate and return the size of the image.
		imageSize := imageFileHeader.Size
		imgSize := fmt.Sprintf("%d", imageSize)
		respondBody := respondPostBody{
			AlbumID:   "1",
			ImageSize: imgSize,
		}

		c.JSON(http.StatusOK, respondBody)
	} else {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid request"})
	}
}

// getAlbumByID locates the album whose ID value matches the id
// parameter sent by the client, then returns that album as a response.
func getAlbumByID(c *gin.Context) {
	idParam := c.Param("id")
	// Simple validation: Check if the ID is an integer
	_, err := strconv.Atoi(idParam)
	if err != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{"message": "Invalid Request"})
		return
	}
	// returns the 200 code and constant object which we have of album
	c.IndentedJSON(http.StatusOK, albums[0])
	return

}
