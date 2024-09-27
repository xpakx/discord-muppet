package main

import (
	"github.com/charmbracelet/bubbles/textarea"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/charmbracelet/bubbles/cursor"
)

type model struct {
    profile            Profile
    contacts           []Friend   
    currentContact     int
    contactsActive     bool
    messages           []MessageItem   
    websocket          *websocket_service
    textInput          textarea.Model
}

func initialModel(profile Profile, contacts []Friend, messages []MessageItem, websocket *websocket_service) model {

	textInput := textarea.New()
	textInput.Placeholder = "Type a message..."

	textInput.CharLimit = 280
	textInput.SetWidth(30)
	textInput.SetHeight(1)
	textInput.MaxHeight = 5
	textInput.FocusedStyle.CursorLine = lipgloss.NewStyle()
	textInput.ShowLineNumbers = false
	textInput.FocusedStyle.CursorLine = lipgloss.NewStyle()
	textInput.BlurredStyle.CursorLine = lipgloss.NewStyle()
	textInput.Prompt = ""

	return model{
		profile:  profile,
		contacts: contacts,
		messages: messages,
		websocket: websocket,
		textInput: textInput,
		currentContact: 0,
		contactsActive: false,
	}
}

func (m model) Init() tea.Cmd {
    return textarea.Blink
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
    switch msg := msg.(type) {
    case tea.KeyMsg:
	if (m.textInput.Focused()) {
		switch msg.String() {
		case "ctrl+c":
			return m, tea.Quit
		case "ctrl+l":
			m.textInput.Reset()
			return m, nil
		case "esc":
			m.textInput.Blur()
			return m, nil
		case "enter":
			// TODO
			m.textInput.Reset()
			return m, nil
		default:
			var cmd tea.Cmd
			m.textInput, cmd = m.textInput.Update(msg)
			return m, cmd
		}
	} else {
		switch msg.String() {
		case "ctrl+c", "q":
		    return m, tea.Quit
		case "i":
		    m.contactsActive = false
		    return m, m.textInput.Focus()
		case "c":
		    m.contactsActive = !m.contactsActive
		    return m, nil
		case "j", "down":
			if m.contactsActive {
				m.currentContact = min(m.currentContact+1, len(m.contacts)-1)
			        return m, nil
			}
		case "k", "up":
			if m.contactsActive {
				m.currentContact = max(m.currentContact-1, 0)
			        return m, nil
			}
		case "enter":
			if m.contactsActive {
				if m.currentContact >= 0 && m.currentContact < len(m.contacts) {
					currentContact := m.contacts[m.currentContact]
					return m, func() tea.Msg {
						messages := openChannel(currentContact)
						return OpenMsg{messages}
					}
				}
			        return m, nil
			}
		}
        }
    case NotifMsg:
	    m.contacts = msg.friends
    case ChannelMsg:
	    m.messages = append(m.messages, msg.messages...)
    case OpenMsg:
	    m.messages = msg.messages
    case cursor.BlinkMsg:
		var cmd tea.Cmd
		m.textInput, cmd = m.textInput.Update(msg)
		return m, cmd
    }

    return m, nil
}

func (m model) View() string {
    return draw(m)
}
