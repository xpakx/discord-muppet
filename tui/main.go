package main

import (
	"net/http"
	"fmt"
	"os"
	"encoding/json"
)
func main() {
	profile := getProfile()
	draw(profile);
}


type Profile struct {
	VisibleName   string    `json:"visibleName"`
	Username      string    `json:"username"`
	Description   string    `json:"description"`
	Status        string    `json:"status"`
}

func getProfile() (Profile) {
	res, err := http.Get("http://localhost:8080/api/v1/profile")
	if err != nil {
		fmt.Printf("error: %s\n", err)
		os.Exit(1)
	}

	if res.StatusCode != 200 {
		fmt.Printf("error: %d\n", res.StatusCode)
		os.Exit(1)
	}

	var p Profile

	err = json.NewDecoder(res.Body).Decode(&p)
	if err != nil {
		fmt.Printf("error: %d\n", err)
		os.Exit(1)
	}

	fmt.Printf("Profile: %+v", p)
	return p
}
