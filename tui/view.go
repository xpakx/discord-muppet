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

	checkMark = lipgloss.NewStyle().SetString("⍟").
			Foreground(special).
			PaddingRight(1).
			String()

	friend = func(s string, notif bool, active bool) string {
		result := ""
		padding := 0;
		if notif {
			result += checkMark
		} else {
			padding = 2
		}
		if (active) {
			return result + lipgloss.NewStyle().PaddingLeft(padding).Render(s)
		}

		return result + lipgloss.NewStyle().
			PaddingLeft(padding).
			Foreground(lipgloss.AdaptiveColor{Light: "#969B86", Dark: "#696969"}).
			Render(s)
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

func draw(profile Profile) {
	physicalWidth, _, _ := term.GetSize(int(os.Stdout.Fd()))
	doc := strings.Builder{}


	chatWidth := width - columnWidth - 5;

	messages := lipgloss.JoinVertical(lipgloss.Top,
			message("User 1", "18:49", "Msg 1", false, chatWidth),
			message("Me", "18:50", "Msg 2", true, chatWidth),
		)

	lists := lipgloss.JoinHorizontal(lipgloss.Top,
		list.Width(columnWidth).Height(19).Render(
			lipgloss.JoinVertical(lipgloss.Left,
				friendHeader("Contacts"),
				friend("User 1", false, true),
				friend("User 2", false, true),
				friend("User 3", true, false),
				friend("User 4", true, true),
				friend("User 5", false, true),
			),
		),

		// input
		lipgloss.JoinVertical(lipgloss.Top,
			lipgloss.NewStyle().MaxHeight(19 - 4).Height(19 - 4).Render(messages),
			input.
			Width(width - columnWidth - 5).
			Render("Message"),
		),
	)

	doc.WriteString(lipgloss.JoinHorizontal(lipgloss.Top, lists))

	doc.WriteString("\n\n")

	// Status bar 
	{
		w := lipgloss.Width

		statusKey := modeStyle.Render("INSERT")
		encoding := statusStyle.Render(profile.Status)
		fishCake := logoStyle.Render("🧶 " + profile.VisibleName)
		statusVal := statusText.
			Width(width - w(statusKey) - w(encoding) - w(fishCake)).
			Render(profile.Description)

		bar := lipgloss.JoinHorizontal(lipgloss.Top,
			statusKey,
			encoding,
			statusVal,
			fishCake,
		)

		doc.WriteString(descriptionBarStyle.Width(width).Render(bar))
	}

	if physicalWidth > 0 {
		docStyle = docStyle.MaxWidth(physicalWidth)
	}

	fmt.Println(docStyle.Render(doc.String()))
}
