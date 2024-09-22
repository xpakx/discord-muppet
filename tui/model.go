package main

import (
	tea "github.com/charmbracelet/bubbletea"
)

type model struct {
    profile    Profile
    contacts   []Friend   
    messages   []MessageItem   
    websocket  *websocket_service
}

func initialModel(profile Profile, contacts []Friend, messages []MessageItem, websocket *websocket_service) model {

	return model{
		profile:  profile,
		contacts: contacts,
		messages: messages,
		websocket: websocket,
	}
}

func (m model) Init() tea.Cmd {
    return nil
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
    switch msg := msg.(type) {
    case tea.KeyMsg:
        switch msg.String() {
        case "ctrl+c", "q":
            return m, tea.Quit
        }
    case NotifMsg:
	    m.contacts = msg.friends
    case ChannelMsg:
	    m.messages = append(m.messages, msg.messages...)
    case OpenMsg:
	    m.messages = msg.messages
    }

    return m, nil
}

func (m model) View() string {
    return draw(m.profile, m.contacts, m.messages)
}
