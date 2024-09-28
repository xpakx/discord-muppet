package main

import (
	"github.com/charmbracelet/bubbles/cursor"
	"github.com/charmbracelet/bubbles/textarea"
	"github.com/charmbracelet/bubbles/viewport"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

type model struct {
    profile            Profile
    contacts           []Friend   
    currentContact     int
    contactsActive     bool
    messages           []MessageItem   
    websocket          *websocket_service
    textInput          textarea.Model
    loadedContact      string
    viewport           viewport.Model
}

func initialModel(profile Profile, contacts []Friend, messages OpenConversation, websocket *websocket_service) model {

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


	chatWidth := width - columnWidth - 5;
	newViewport := viewport.New(chatWidth, 15)

	return model{
		profile:  profile,
		contacts: contacts,
		messages: messages.Messages,
		loadedContact: messages.Username,
		websocket: websocket,
		textInput: textInput,
		currentContact: 0,
		contactsActive: false,
		viewport: newViewport,
	}
}

func (m *model) UpdateViewport() {
	chatWidth := width - columnWidth - 5;
	var renderedMessages []string
	for _, msg := range m.messages {
		if msg.Type == "Message" {
			renderedMessages = append(
				renderedMessages, 
				message(msg.Message.Username, msg.Message.Timestamp, msg.Message.Content, msg.Message.Username == m.profile.VisibleName, chatWidth))
		}
	}

	messageContainer := lipgloss.JoinVertical(lipgloss.Top, renderedMessages...)
	m.viewport.SetContent(messageContainer)
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
			msg := m.textInput.Value()
			if msg == "" {
				return m, nil
			}
			m.textInput.Reset()
			sendMessage(msg) // TODO
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
						return OpenMsg{messages.Messages, messages.Username}
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
	    m.UpdateViewport()
	    m.viewport.GotoBottom() // TODO: detect if scrolled down
    case OpenMsg:
	    if (m.loadedContact != msg.user) {
		    m.messages = msg.messages
		    m.UpdateViewport()
		    m.viewport.GotoBottom()
	    }
    case cursor.BlinkMsg:
		var cmd tea.Cmd
		m.textInput, cmd = m.textInput.Update(msg)
		return m, cmd
    case tea.WindowSizeMsg:
	    // TODO
	    m.UpdateViewport()
	    m.viewport.GotoBottom() // TODO: only first time
    }

    return m, nil
}

func (m model) View() string {
    return draw(m)
}
