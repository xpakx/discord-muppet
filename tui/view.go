package main

import (
	"fmt"
	"os"
	"strings"

	"github.com/charmbracelet/lipgloss"
	"golang.org/x/term"
)

const (
	width = 96 // TODO
	columnWidth = 20
)

var (
	subtle    = lipgloss.AdaptiveColor{Light: "#D9DCCF", Dark: "#383838"}
	highlight = lipgloss.AdaptiveColor{Light: "#874BFD", Dark: "#7D56F4"}
	special   = lipgloss.AdaptiveColor{Light: "#43BF6D", Dark: "#73F59F"}
	yellow   = lipgloss.Color("#F4D03F")
	red   = lipgloss.Color("#E74C3C")

	border = lipgloss.Border{
		Top:         "─",
		Bottom:      "─",
		Left:        "│",
		Right:       "│",
		TopLeft:     "╭",
		TopRight:    "╮",
		BottomLeft:  "╰ ",
		BottomRight: "╯",
	}

	// List.

	list = lipgloss.NewStyle().
		Border(lipgloss.NormalBorder(), false, true, false, false).
		BorderForeground(subtle).
		MarginRight(2).
		Height(8).
		Width(columnWidth + 1)

	friendHeader = lipgloss.NewStyle().
			BorderStyle(lipgloss.NormalBorder()).
			BorderBottom(true).
			BorderForeground(subtle).
			MarginRight(2).
			Render

	
	onlineBadge = lipgloss.NewStyle().SetString("⍟").
			Foreground(special).
			PaddingRight(1).
			String()
	idleBadge = lipgloss.NewStyle().SetString("◷").
			Foreground(yellow).
			PaddingRight(1).
			String()
	doNotDisturbBadge = lipgloss.NewStyle().SetString("⊗").
			Foreground(red).
			PaddingRight(1).
			String()
	invisibleBadge = lipgloss.NewStyle().SetString("👻").
			PaddingRight(1).
			String()


	friend = func(friend Friend) string {
		result := ""
		padding := 0;
		switch (friend.Status) {
		case "Online":
			result += onlineBadge
			break
		case "Idle":
			result += idleBadge
			break
		case "DoNotDisturb":
			result += doNotDisturbBadge
			break
		default:
			padding += 2
		}
		if (friend.Online()) {
			result += lipgloss.NewStyle().
				PaddingLeft(padding).
				Render(friend.VisibleName)
		} else {
			result += lipgloss.NewStyle().
				PaddingLeft(padding).
				Foreground(lipgloss.AdaptiveColor{Light: "#969B86", Dark: "#696969"}).
				Render(friend.VisibleName)
		}
		if friend.NewMessages {
			result += lipgloss.NewStyle().
			        Foreground(red).
				PaddingLeft(1).
				Render(fmt.Sprintf("(%d)", friend.Notifications))
		}
		return result
	}

	// Status Bar.
	logoStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#FFFDF5")).
			Background(lipgloss.Color("#6124DF")).
			Padding(0, 1)

	descriptionBarStyle = lipgloss.NewStyle().
			Foreground(lipgloss.AdaptiveColor{Light: "#343433", Dark: "#C1C6B2"}).
			Background(lipgloss.AdaptiveColor{Light: "#D9DCCF", Dark: "#353533"})

	modeStyle = lipgloss.NewStyle().
			Inherit(descriptionBarStyle).
			Foreground(lipgloss.Color("#FFFDF5")).
			Background(lipgloss.Color("#0F5F87")).
			Padding(0, 1)

	statusStyle = modeStyle.
			Background(lipgloss.Color("#A550DF")).
			Align(lipgloss.Left).
			MarginRight(1)

	onlineStyle = statusStyle.
			SetString("⍟").
			Foreground(special).
			String()
	idleStyle = statusStyle.
			SetString("◷").
			Foreground(yellow).
			String()
	doNotDisturbStyle = statusStyle.
			SetString("⊗").
			Foreground(red).
			String()
	invisibleStyle = statusStyle.
			SetString("👻").
			PaddingRight(1).
			String()

	statusBadge = func(status string) string {
		switch(status) {
		case "Online":
			return onlineStyle
		case "Idle":
			return idleStyle
		case "DoNotDisturb":
			return doNotDisturbStyle
		case "Invisible":
			return invisibleStyle
		default:
			return " "
		}
	}


	statusText = lipgloss.NewStyle().Inherit(descriptionBarStyle)

	// Chat
	input = lipgloss.NewStyle().
		Border(border, true).
		BorderForeground(subtle).
		MarginTop(1).
		Padding(0, 1)
	
	nameStyle = lipgloss.NewStyle().
		Foreground(highlight).
		MarginRight(5)
	myNameStyle = nameStyle.
		Foreground(special)
	dateStyle = lipgloss.NewStyle().
		Foreground(subtle)

	message = func(name string, date string, content string, me bool, width int) string {
		var nameR = ""
		if (me) {
			nameR = myNameStyle.Render(name) 
		} else {
			nameR = nameStyle.Render(name) 
		}
		var dateR = dateStyle.Render(date)

		gap := strings.Repeat(" ", max(0, width-lipgloss.Width(nameR)-lipgloss.Width(dateR)))
		header := nameR + gap + dateR
		return  lipgloss.JoinVertical(lipgloss.Top,
				header,
				lipgloss.NewStyle().Width(width).Render(content),
			)
	}
	
	docStyle = lipgloss.NewStyle().Padding(1, 2, 1, 2)
)

func draw(m model) (string) {
	profile := m.profile
	contacts := m.contacts
	messages := m.messages
	textInput := m.textInput

	physicalWidth, _, _ := term.GetSize(int(os.Stdout.Fd()))
	doc := strings.Builder{}

	chatWidth := width - columnWidth - 5;
	var renderedMessages []string
	for _, msg := range messages {
		if msg.Type == "Message" {
			renderedMessages = append(
				renderedMessages, 
				message(msg.Message.Username, msg.Message.Timestamp, msg.Message.Content, msg.Message.Username == profile.VisibleName, chatWidth))
		}
	}

	messageContainer := lipgloss.JoinVertical(lipgloss.Top, renderedMessages...)


	friends := []string{ friendHeader("Contacts") }


	for _, f := range contacts {
		friends = append(friends, friend(f))
	}


	lists := lipgloss.JoinHorizontal(lipgloss.Top,
		list.Width(columnWidth).Height(19).Render(
			lipgloss.JoinVertical(lipgloss.Left, friends...),
		),

		// input
		lipgloss.JoinVertical(lipgloss.Top,
			lipgloss.NewStyle().MaxHeight(19 - 4).Height(19 - 4).Render(messageContainer),
			input.
			Width(width - columnWidth - 5).
			Render(textInput.View()),
		),
	)

	doc.WriteString(lipgloss.JoinHorizontal(lipgloss.Top, lists))

	doc.WriteString("\n\n")

	// Status bar 
	{
		w := lipgloss.Width

		modeIndicator := modeStyle.Render("INSERT")
		statusIcon := statusBadge(profile.Status)
		nameContainer := logoStyle.Render("🧶 " + profile.VisibleName)
		description := statusText.
			Width(width - w(modeIndicator) - w(statusIcon) - w(nameContainer)).
			Render(profile.Description)

		bar := lipgloss.JoinHorizontal(lipgloss.Top,
			modeIndicator,
			statusIcon,
			description,
			nameContainer,
		)

		doc.WriteString(descriptionBarStyle.Width(width).Render(bar))
	}

	if physicalWidth > 0 {
		docStyle = docStyle.MaxWidth(physicalWidth)
	}

	return docStyle.Render(doc.String())
}
