package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	tea "github.com/charmbracelet/bubbletea"
)

func main() {
	profile := getProfile()
	contacts := getContacts()
	msgs := currentChannel()
	w := websocket_service{};

	p := tea.NewProgram(initialModel(profile, contacts, msgs, &w))
	w.SetProgram(p)
	w.ConnectWS()
	go w.Run()
	w.Connect()
	w.Subscribe()

	defer func() {
		if w.Connection != nil {
			if err := w.Connection.Close(); err != nil {
				fmt.Println("error closing WebSocket connection:", err)
			} else {
				fmt.Println("closed WebSocket connection")
			}
		} else {
			fmt.Println("No WebSocket connection")
		}
	}()
	
	if _, err := p.Run(); err != nil {
		fmt.Printf("error: %v", err)
		os.Exit(1)
	}
}

type Message struct {
	Content       string    `json:"content"`
	Timestamp     string    `json:"timestamp"`
	ChainStart    bool      `json:"chainStart"`
	Id            string    `json:"id"`
	Username      string    `json:"username"`
}

type MessageItem struct {
	Type          string    `json:"type"`
	Message       Message   `json:"message"`
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

type OpenConversation struct {
	Username      string    `json:"username"`
	Messages      []MessageItem   `json:"messages"`
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

func openChannel(f Friend) (OpenConversation) {
	res, err := http.Get("http://localhost:8080/api/v1/contacts/" + f.Username)
	if err != nil {
		fmt.Printf("error: %s\n", err)
		os.Exit(1)
	}

	if res.StatusCode != 200 {
		fmt.Printf("error: %d\n", res.StatusCode)
		os.Exit(1)
	}

	var msgs OpenConversation

	err = json.NewDecoder(res.Body).Decode(&msgs)
	if err != nil {
		fmt.Printf("error: %d\n", err)
		os.Exit(1)
	}

	return msgs
}

func currentChannel() (OpenConversation) {
	res, err := http.Get("http://localhost:8080/api/v1/current")
	if err != nil {
		fmt.Printf("error: %s\n", err)
		os.Exit(1)
	}

	if res.StatusCode != 200 {
		fmt.Printf("error: %d\n", res.StatusCode)
		os.Exit(1)
	}

	var msgs OpenConversation

	err = json.NewDecoder(res.Body).Decode(&msgs)
	if err != nil {
		fmt.Printf("error: %d\n", err)
		os.Exit(1)
	}

	return msgs
}

func sendMessage(msg string) {
	// TODO: error messages
	data := map[string]string{
		"message": msg,
	}
	jsonData, err := json.Marshal(data)
	if err != nil {
		fmt.Printf("error marshalling data: %s\n", err)
		return
	}

	res, err := http.Post(
		"http://localhost:8080/api/v1/messages",
		"application/json",
		bytes.NewBuffer(jsonData))
	if err != nil {
		fmt.Printf("error: %s\n", err)
		return
	}
	defer res.Body.Close()

	if res.StatusCode != http.StatusOK {
		fmt.Printf("error: %d\n", res.StatusCode)
	}
}
