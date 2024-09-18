package main

import (
	"net/http"
	"fmt"
	"os"
	"encoding/json"

	tea "github.com/charmbracelet/bubbletea"
)

func main() {
	profile := getProfile()
	contacts := getContacts()
	draw(profile, contacts)

	p := tea.NewProgram(initialModel(profile, contacts))
	
	if _, err := p.Run(); err != nil {
		fmt.Printf("error: %v", err)
		os.Exit(1)
	}
}

type Profile struct {
	VisibleName   string    `json:"visibleName"`
	Username      string    `json:"username"`
	Description   string    `json:"description"`
	Status        string    `json:"status"`
}

type Friend struct {
	VisibleName   string    `json:"visibleName"`
	Username      string    `json:"username"`
	Description   string    `json:"description"`
	Status        string    `json:"status"`
	NewMessages   bool      `json:"newMessages"`
	Notifications int       `json:"notifications"`
}

func (f Friend) Online() bool {
	return f.Status == "Online"
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

	return p
}

func getContacts() ([]Friend) {
	res, err := http.Get("http://localhost:8080/api/v1/contacts")
	if err != nil {
		fmt.Printf("error: %s\n", err)
		os.Exit(1)
	}

	if res.StatusCode != 200 {
		fmt.Printf("error: %d\n", res.StatusCode)
		os.Exit(1)
	}

	var friends []Friend

	err = json.NewDecoder(res.Body).Decode(&friends)
	if err != nil {
		fmt.Printf("error: %d\n", err)
		os.Exit(1)
	}

	return friends
}
