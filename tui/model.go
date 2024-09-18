package main

import (
	tea "github.com/charmbracelet/bubbletea"
)

type model struct {
    profile    Profile
    contacts   []Friend   
}

func initialModel(profile Profile, contacts []Friend) model {

	return model{
		profile:  profile,
		contacts: contacts,
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
    }

    return m, nil
}

func (m model) View() string {
    return draw(m.profile, m.contacts)
}
