package com.projectlibre1.pm.graphic.menu;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.projectlibre1.menu.MenuActionConstants;
import com.projectlibre1.menu.MenuManager;
import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.pm.graphic.frames.DocumentFrame;
import com.projectlibre1.pm.graphic.frames.GraphicManager;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.BrowserControl;

public class ModernMenuShell extends JPanel implements MenuActionConstants {
	private static final long serialVersionUID = 1L;

	private static final String SECTION_PLAN = "plan";
	private static final String SECTION_EDIT = "edit";
	private static final String SECTION_SHARE = "share";
	private static final String SECTION_VIEW = "view";

	private static final String PROFILE_EMPTY = "empty";
	private static final String PROFILE_TASK = "task";
	private static final String PROFILE_RESOURCE = "resource";
	private static final String PROFILE_STRUCTURE = "structure";

	private static final String[] VIEW_ORDER = new String[] {
		ACTION_GANTT,
		ACTION_TRACKING_GANTT,
		ACTION_NETWORK,
		ACTION_RESOURCES,
		ACTION_WBS,
		ACTION_RBS,
		ACTION_REPORT
	};

	private final GraphicManager graphicManager;
	private final MenuManager menuManager;
	private final JPanel projectSwitcherHost = new JPanel(new BorderLayout());
	private final JPanel workspaceHost = new JPanel(new BorderLayout());
	private final JPanel emptyStatePanel = new JPanel();
	private final JPanel contextCards = new JPanel(new CardLayout());
	private final Map<String, JToggleButton> railButtons = new LinkedHashMap<String, JToggleButton>();
	private final Map<String, JToggleButton> sectionButtons = new LinkedHashMap<String, JToggleButton>();
	private final ButtonGroup railGroup = new ButtonGroup();
	private final ButtonGroup sectionGroup = new ButtonGroup();
	private final JPopupMenu applicationPopup = new JPopupMenu();
	private final JLabel projectTitleLabel = new JLabel(Messages.getContextString("Text.ApplicationTitle"));
	private final JLabel projectMetaLabel = new JLabel("Start with a project or jump back into your active plan.");
	private final JLabel activeViewLabel = new JLabel("Gantt");
	private final JLabel contextCaptionLabel = new JLabel("Plan");
	private final JLabel emptyStateTitle = new JLabel(Messages.getContextString("Text.ApplicationTitle"));

	private String activeSection = SECTION_PLAN;
	private String activeProfile = PROFILE_EMPTY;
	private String activeViewId = ACTION_GANTT;

	public ModernMenuShell(GraphicManager graphicManager, MenuManager menuManager) {
		super(new BorderLayout(ModernMenuTheme.sectionGap(), ModernMenuTheme.sectionGap()));
		this.graphicManager = graphicManager;
		this.menuManager = menuManager;
		setOpaque(true);
		setBackground(ModernMenuTheme.workspaceSurface());
		setBorder(ModernMenuTheme.shellBorder());

		buildApplicationPopup();
		add(createNavigationRail(), BorderLayout.WEST);
		add(createWorkspaceColumn(), BorderLayout.CENTER);
		refresh(null);
	}

	public Container getWorkspaceHost() {
		return workspaceHost;
	}

	public JComponent getEmptyStatePanel() {
		return emptyStatePanel;
	}

	public void bindProjectSwitcher(JComponent switcher) {
		projectSwitcherHost.removeAll();
		projectSwitcherHost.setOpaque(false);
		if (switcher != null) {
			projectSwitcherHost.add(switcher, BorderLayout.CENTER);
		}
		projectSwitcherHost.revalidate();
		projectSwitcherHost.repaint();
	}

	public void setActiveSection(String section) {
		if (section == null || !sectionButtons.containsKey(section)) {
			return;
		}
		activeSection = section;
		JToggleButton button = sectionButtons.get(section);
		if (button != null) {
			button.setSelected(true);
		}
		contextCaptionLabel.setText(capitalize(section));
		showContextCard();
	}

	public void refresh(DocumentFrame frame) {
		String nextViewId = frame == null ? ACTION_GANTT : graphicManager.getTopViewId();
		if (nextViewId == null) {
			nextViewId = ACTION_GANTT;
		}
		activeViewId = nextViewId;
		activeProfile = frame == null ? PROFILE_EMPTY : profileForView(activeViewId);

		projectTitleLabel.setText(frame != null && frame.getProject() != null
			? frame.getProject().getTitle()
			: Messages.getContextString("Text.ApplicationTitle"));
		projectMetaLabel.setText(frame != null && frame.getProject() != null
			? profileSummary(activeProfile)
			: "Create a project, open an existing file, or resume planning from the menu.");
		activeViewLabel.setText(safeText(activeViewId));
		contextCaptionLabel.setText(capitalize(activeSection));
		JToggleButton sectionButton = sectionButtons.get(activeSection);
		if (sectionButton != null) {
			sectionButton.setSelected(true);
		}

		selectRailButton(activeViewId);
		updateRailLabels();
		updateSectionAvailability(frame != null);
		showContextCard();
		updateEmptyState(frame == null);
	}

	private JComponent createNavigationRail() {
		JPanel rail = new JPanel(new BorderLayout());
		rail.setOpaque(true);
		rail.setBackground(ModernMenuTheme.elevatedSurface());
		rail.setBorder(ModernMenuTheme.railBorder());
		rail.setPreferredSize(new Dimension(ModernMenuTheme.railWidth(), 0));

		JPanel brand = new JPanel();
		brand.setOpaque(false);
		brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

		JButton logo = new JButton(IconManager.getIcon("logo.ProjectLibre"));
		logo.setAlignmentX(Component.LEFT_ALIGNMENT);
		logo.setBorder(BorderFactory.createEmptyBorder());
		logo.setContentAreaFilled(false);
		logo.setFocusPainted(false);
		logo.setToolTipText("ProjectLibre");
		logo.addActionListener(e -> BrowserControl.displayURL("http://www.projectlibre.com/"));
		brand.add(logo);
		brand.add(Box.createVerticalStrut(10));

		JLabel caption = new JLabel("Workspace");
		caption.setAlignmentX(Component.LEFT_ALIGNMENT);
		caption.setFont(caption.getFont().deriveFont(Font.BOLD, 13f));
		caption.setForeground(ModernMenuTheme.strongText());
		brand.add(caption);

		JLabel subcaption = new JLabel("Jump between views");
		subcaption.setAlignmentX(Component.LEFT_ALIGNMENT);
		subcaption.setForeground(ModernMenuTheme.mutedText());
		brand.add(subcaption);

		JPanel railButtonsPanel = new JPanel();
		railButtonsPanel.setOpaque(false);
		railButtonsPanel.setLayout(new BoxLayout(railButtonsPanel, BoxLayout.Y_AXIS));
		railButtonsPanel.setBorder(new EmptyBorder(16, 0, 16, 0));
		for (int i = 0; i < VIEW_ORDER.length; i++) {
			String id = VIEW_ORDER[i];
			JToggleButton button = createRailButton(id);
			railButtons.put(id, button);
			railGroup.add(button);
			railButtonsPanel.add(button);
			railButtonsPanel.add(Box.createVerticalStrut(6));
		}

		JPanel utility = new JPanel();
		utility.setOpaque(false);
		utility.setLayout(new BoxLayout(utility, BoxLayout.Y_AXIS));
		utility.add(Box.createVerticalGlue());
		utility.add(createUtilityButton("Menu", null, applicationPopup, IconManager.getIcon("application.icon")));
		utility.add(Box.createVerticalStrut(6));
		utility.add(createUtilityActionButton(ACTION_LOCALE, "Locale"));
		utility.add(Box.createVerticalStrut(6));
		utility.add(createUtilityButton("Help", new Runnable() {
			public void run() {
				graphicManager.showHelpDialog();
			}
		}, null, IconManager.getRibbonIcon("ribbon.help", 18, 18)));

		rail.add(brand, BorderLayout.NORTH);
		rail.add(railButtonsPanel, BorderLayout.CENTER);
		rail.add(utility, BorderLayout.SOUTH);
		return rail;
	}

	private JComponent createWorkspaceColumn() {
		JPanel column = new JPanel(new BorderLayout(0, ModernMenuTheme.sectionGap()));
		column.setOpaque(false);
		column.add(createTopChrome(), BorderLayout.NORTH);
		column.add(createWorkspaceFrame(), BorderLayout.CENTER);
		return column;
	}

	private JComponent createTopChrome() {
		JPanel topChrome = new JPanel();
		topChrome.setOpaque(false);
		topChrome.setLayout(new BoxLayout(topChrome, BoxLayout.Y_AXIS));
		topChrome.add(createHeaderCard());
		topChrome.add(Box.createVerticalStrut(ModernMenuTheme.sectionGap()));
		topChrome.add(createContextCard());
		return topChrome;
	}

	private JComponent createHeaderCard() {
		JPanel header = new JPanel(new BorderLayout(12, 0));
		header.setOpaque(true);
		header.setBackground(ModernMenuTheme.cardSurface());
		header.setBorder(ModernMenuTheme.headerBorder());

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		projectTitleLabel.setFont(projectTitleLabel.getFont().deriveFont(Font.BOLD, 20f));
		projectTitleLabel.setForeground(ModernMenuTheme.strongText());
		titleBlock.add(projectTitleLabel);
		titleBlock.add(Box.createVerticalStrut(2));

		projectMetaLabel.setForeground(ModernMenuTheme.mutedText());
		titleBlock.add(projectMetaLabel);

		JPanel rightSide = new JPanel(new BorderLayout(12, 0));
		rightSide.setOpaque(false);

		JPanel switcherCard = new JPanel(new BorderLayout());
		switcherCard.setOpaque(true);
		switcherCard.setBackground(ModernMenuTheme.elevatedSurface());
		switcherCard.setBorder(ModernMenuTheme.cardBorder());

		JLabel switcherLabel = new JLabel("Active project");
		switcherLabel.setForeground(ModernMenuTheme.mutedText());
		switcherLabel.setBorder(new EmptyBorder(0, 0, 6, 0));

		JPanel switcherStack = new JPanel();
		switcherStack.setOpaque(false);
		switcherStack.setLayout(new BoxLayout(switcherStack, BoxLayout.Y_AXIS));
		switcherStack.add(switcherLabel);
		switcherStack.add(projectSwitcherHost);
		switcherCard.add(switcherStack, BorderLayout.CENTER);

		JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		quickActions.setOpaque(false);
		quickActions.add(createHeaderActionButton(ACTION_SAVE_PROJECT));
		quickActions.add(createHeaderActionButton(ACTION_UNDO));
		quickActions.add(createHeaderActionButton(ACTION_REDO));
		quickActions.add(createHeaderActionButton(ACTION_FIND));

		rightSide.add(switcherCard, BorderLayout.CENTER);
		rightSide.add(quickActions, BorderLayout.EAST);

		header.add(titleBlock, BorderLayout.WEST);
		header.add(rightSide, BorderLayout.CENTER);
		return header;
	}

	private JComponent createContextCard() {
		JPanel card = new JPanel(new BorderLayout(0, 12));
		card.setOpaque(true);
		card.setBackground(ModernMenuTheme.cardSurface());
		card.setBorder(ModernMenuTheme.cardBorder());

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);

		JPanel labels = new JPanel();
		labels.setOpaque(false);
		labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

		JLabel smallTitle = new JLabel("Context");
		smallTitle.setForeground(ModernMenuTheme.mutedText());
		labels.add(smallTitle);
		labels.add(Box.createVerticalStrut(2));

		activeViewLabel.setFont(activeViewLabel.getFont().deriveFont(Font.BOLD, 16f));
		activeViewLabel.setForeground(ModernMenuTheme.strongText());
		labels.add(activeViewLabel);

		JPanel sectionToggleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		sectionToggleBar.setOpaque(false);
		sectionToggleBar.add(createSectionButton(SECTION_PLAN, "Plan"));
		sectionToggleBar.add(createSectionButton(SECTION_EDIT, "Edit"));
		sectionToggleBar.add(createSectionButton(SECTION_SHARE, "Share"));
		sectionToggleBar.add(createSectionButton(SECTION_VIEW, "View"));

		header.add(labels, BorderLayout.WEST);
		header.add(sectionToggleBar, BorderLayout.EAST);

		card.add(header, BorderLayout.NORTH);
		card.add(createContextCards(), BorderLayout.CENTER);
		return card;
	}

	private JComponent createContextCards() {
		contextCards.setOpaque(false);
		contextCards.add(createActionCard("Project cadence", new String[] {
			ACTION_NEW_PROJECT, ACTION_OPEN_PROJECT, ACTION_SAVE_PROJECT, ACTION_SAVE_PROJECT_AS,
			ACTION_PROJECT_INFORMATION, ACTION_CHANGE_WORKING_TIME
		}), key(PROFILE_TASK, SECTION_PLAN));
		contextCards.add(createActionCard("Task shaping", new String[] {
			ACTION_INSERT_TASK, ACTION_DELETE, ACTION_INDENT, ACTION_OUTDENT,
			ACTION_LINK, ACTION_UNLINK, ACTION_INFORMATION, ACTION_NOTES, ACTION_ASSIGN_RESOURCES
		}), key(PROFILE_TASK, SECTION_EDIT));
		contextCards.add(createActionCard("Share the plan", new String[] {
			ACTION_PRINT, ACTION_PDF, ACTION_PROJECTS_DIALOG, ACTION_CLOSE_PROJECT
		}), key(PROFILE_TASK, SECTION_SHARE));
		contextCards.add(createActionCard("Focus the view", new String[] {
			ACTION_ZOOM_IN, ACTION_ZOOM_OUT, ACTION_FIND, ACTION_SCROLL_TO_TASK, ACTION_CHOOSE_FILTER
		}), key(PROFILE_TASK, SECTION_VIEW));

		contextCards.add(createActionCard("Resource planning", new String[] {
			ACTION_NEW_PROJECT, ACTION_OPEN_PROJECT, ACTION_SAVE_PROJECT, ACTION_PROJECT_INFORMATION,
			ACTION_CHANGE_WORKING_TIME, ACTION_PROJECTS_DIALOG
		}), key(PROFILE_RESOURCE, SECTION_PLAN));
		contextCards.add(createActionCard("Resource editing", new String[] {
			ACTION_INSERT_RESOURCE, ACTION_DELETE, ACTION_INDENT, ACTION_OUTDENT,
			ACTION_INFORMATION, ACTION_NOTES, ACTION_FIND
		}), key(PROFILE_RESOURCE, SECTION_EDIT));
		contextCards.add(createActionCard("Share the resourcing view", new String[] {
			ACTION_PRINT, ACTION_PDF, ACTION_SAVE_PROJECT_AS, ACTION_CLOSE_PROJECT
		}), key(PROFILE_RESOURCE, SECTION_SHARE));
		contextCards.add(createActionCard("Navigate the resource lens", new String[] {
			ACTION_ZOOM_IN, ACTION_ZOOM_OUT, ACTION_FIND, ACTION_CHOOSE_FILTER
		}), key(PROFILE_RESOURCE, SECTION_VIEW));

		contextCards.add(createActionCard("Project lens", new String[] {
			ACTION_NEW_PROJECT, ACTION_OPEN_PROJECT, ACTION_SAVE_PROJECT, ACTION_PROJECT_INFORMATION,
			ACTION_PROJECTS_DIALOG, ACTION_CHANGE_WORKING_TIME
		}), key(PROFILE_STRUCTURE, SECTION_PLAN));
		contextCards.add(createActionCard("Structure review", new String[] {
			ACTION_INFORMATION, ACTION_NOTES, ACTION_FIND
		}), key(PROFILE_STRUCTURE, SECTION_EDIT));
		contextCards.add(createActionCard("Publish and share", new String[] {
			ACTION_PRINT, ACTION_PDF, ACTION_SAVE_PROJECT_AS, ACTION_CLOSE_PROJECT
		}), key(PROFILE_STRUCTURE, SECTION_SHARE));
		contextCards.add(createActionCard("Explore the model", new String[] {
			ACTION_ZOOM_IN, ACTION_ZOOM_OUT, ACTION_FIND, ACTION_CHOOSE_FILTER
		}), key(PROFILE_STRUCTURE, SECTION_VIEW));

		contextCards.add(createActionCard("Start here", new String[] {
			ACTION_NEW_PROJECT, ACTION_OPEN_PROJECT, ACTION_PROJECTS_DIALOG
		}), key(PROFILE_EMPTY, SECTION_PLAN));
		contextCards.add(createActionCard("Nothing to edit yet", new String[] {
			ACTION_NEW_PROJECT, ACTION_OPEN_PROJECT
		}), key(PROFILE_EMPTY, SECTION_EDIT));
		contextCards.add(createActionCard("Share when a project is open", new String[] {
			ACTION_OPEN_PROJECT, ACTION_NEW_PROJECT
		}), key(PROFILE_EMPTY, SECTION_SHARE));
		contextCards.add(createActionCard("Open a project to navigate views", new String[] {
			ACTION_OPEN_PROJECT, ACTION_NEW_PROJECT
		}), key(PROFILE_EMPTY, SECTION_VIEW));
		return contextCards;
	}

	private JComponent createActionCard(String title, String[] actionIds) {
		JPanel card = new JPanel(new BorderLayout(0, 10));
		card.setOpaque(true);
		card.setBackground(ModernMenuTheme.elevatedSurface());
		card.setBorder(ModernMenuTheme.softCardBorder());

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
		titleLabel.setForeground(ModernMenuTheme.strongText());
		card.add(titleLabel, BorderLayout.NORTH);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		buttons.setOpaque(false);
		for (int i = 0; i < actionIds.length; i++) {
			buttons.add(createActionButton(actionIds[i], true, false));
		}
		card.add(buttons, BorderLayout.CENTER);
		return card;
	}

	private JComponent createWorkspaceFrame() {
		JPanel frame = new JPanel(new BorderLayout());
		frame.setOpaque(true);
		frame.setBackground(ModernMenuTheme.cardSurface());
		frame.setBorder(ModernMenuTheme.cardBorder());

		workspaceHost.setOpaque(true);
		workspaceHost.setBackground(ModernMenuTheme.cardSurface());
		frame.add(workspaceHost, BorderLayout.CENTER);

		emptyStatePanel.setOpaque(true);
		emptyStatePanel.setBackground(ModernMenuTheme.cardSurface());
		emptyStatePanel.setLayout(new BoxLayout(emptyStatePanel, BoxLayout.Y_AXIS));
		emptyStatePanel.setBorder(BorderFactory.createEmptyBorder(54, 54, 54, 54));

		emptyStateTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		emptyStateTitle.setFont(emptyStateTitle.getFont().deriveFont(Font.BOLD, 26f));
		emptyStateTitle.setForeground(ModernMenuTheme.strongText());
		emptyStatePanel.add(emptyStateTitle);
		emptyStatePanel.add(Box.createVerticalStrut(8));

		JLabel body = new JLabel("<html>Build schedules, rebalance resources, and review structure in a cleaner editorial workspace.</html>");
		body.setAlignmentX(Component.LEFT_ALIGNMENT);
		body.setForeground(ModernMenuTheme.mutedText());
		emptyStatePanel.add(body);
		emptyStatePanel.add(Box.createVerticalStrut(24));

		JPanel callToAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		callToAction.setOpaque(false);
		callToAction.setAlignmentX(Component.LEFT_ALIGNMENT);
		callToAction.add(createActionButton(ACTION_NEW_PROJECT, true, true));
		callToAction.add(createActionButton(ACTION_OPEN_PROJECT, true, false));
		emptyStatePanel.add(callToAction);

		return frame;
	}

	private JToggleButton createRailButton(String id) {
		final JToggleButton button = new JToggleButton(safeText(id));
		button.putClientProperty("viewId", id);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setIcon(iconFor(id));
		button.setFocusPainted(false);
		button.setBorder(ModernMenuTheme.cardBorder());
		button.setBackground(ModernMenuTheme.elevatedSurface());
		button.setForeground(ModernMenuTheme.strongText());
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
		button.setPreferredSize(new Dimension(ModernMenuTheme.railWidth() - 24, 46));
		button.setToolTipText(safeText(id));
		Action action = menuManager.getActionFromId(id);
		if (action != null) {
			button.addActionListener(action);
		}
		register(id, button);
		return button;
	}

	private JToggleButton createSectionButton(final String section, String text) {
		JToggleButton button = new JToggleButton(text);
		button.setFocusPainted(false);
		button.setOpaque(true);
		button.setBorder(ModernMenuTheme.cardBorder());
		button.setBackground(ModernMenuTheme.elevatedSurface());
		button.setForeground(ModernMenuTheme.strongText());
		button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setActiveSection(section);
			}
		});
		sectionButtons.put(section, button);
		sectionGroup.add(button);
		return button;
	}

	private JButton createHeaderActionButton(String id) {
		return (JButton) createActionButton(id, false, false);
	}

	private JButton createUtilityActionButton(String id, String fallbackText) {
		AbstractButton button = createActionButton(id, false, false);
		if (button.getIcon() == null) {
			button.setText(fallbackText);
		}
		styleUtilityButton(button);
		return (JButton) button;
	}

	private JButton createUtilityButton(String text, final Runnable handler, final JPopupMenu popup, Icon icon) {
		JButton button = new JButton(text);
		button.setIcon(icon);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setToolTipText(text);
		styleUtilityButton(button);
		button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (popup != null) {
					popup.show(button, button.getWidth(), 0);
				} else if (handler != null) {
					handler.run();
				}
			}
		});
		return button;
	}

	private void styleUtilityButton(AbstractButton button) {
		button.setFocusPainted(false);
		button.setBackground(ModernMenuTheme.elevatedSurface());
		button.setForeground(ModernMenuTheme.strongText());
		button.setBorder(ModernMenuTheme.cardBorder());
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
	}

	private AbstractButton createActionButton(String id, boolean showText, boolean prominent) {
		Action action = menuManager.getActionFromId(id);
		JButton button = new JButton();
		if (action != null) {
			button.addActionListener(action);
		}
		button.setActionCommand(id);
		button.setIcon(iconFor(id));
		button.setText(showText ? safeText(id) : "");
		button.setToolTipText(safeText(id));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setFocusPainted(false);
		button.setBorder(ModernMenuTheme.cardBorder());
		button.setBackground(prominent ? ModernMenuTheme.accentSoft() : ModernMenuTheme.cardSurface());
		button.setForeground(ModernMenuTheme.strongText());
		button.setMargin(ModernMenuTheme.roomyInsets());
		register(id, button);
		return button;
	}

	private void buildApplicationPopup() {
		addPopupItem("New Project", ACTION_NEW_PROJECT);
		addPopupItem("Open Project", ACTION_OPEN_PROJECT);
		addPopupItem("Save Project", ACTION_SAVE_PROJECT);
		addPopupItem("Save As", ACTION_SAVE_PROJECT_AS);
		applicationPopup.add(new JSeparator());
		addPopupItem("Projects", ACTION_PROJECTS_DIALOG);
		addPopupItem("Project Information", ACTION_PROJECT_INFORMATION);
		addPopupItem("Print", ACTION_PRINT);
		addPopupItem("PDF", ACTION_PDF);
		applicationPopup.add(new JSeparator());
		addPopupItem("Close Project", ACTION_CLOSE_PROJECT);
	}

	private void addPopupItem(String text, String actionId) {
		JMenuItem item = new JMenuItem(text);
		item.setIcon(iconFor(actionId));
		Action action = menuManager.getActionFromId(actionId);
		if (action != null) {
			item.addActionListener(action);
		}
		menuManager.registerManagedActionComponent(actionId, item);
		applicationPopup.add(item);
	}

	private void register(String id, AbstractButton button) {
		menuManager.registerManagedActionComponent(id, button);
	}

	private void updateRailLabels() {
		for (Map.Entry<String, JToggleButton> entry : railButtons.entrySet()) {
			String id = entry.getKey();
			JToggleButton button = entry.getValue();
			boolean selected = id.equals(activeViewId);
			button.setText(selected ? safeText(id) : "");
			button.setBackground(selected ? ModernMenuTheme.selectionBackground() : ModernMenuTheme.elevatedSurface());
			button.setForeground(selected ? ModernMenuTheme.selectionForeground() : ModernMenuTheme.strongText());
		}
	}

	private void updateSectionAvailability(boolean hasDocument) {
		boolean enableAdvanced = hasDocument;
		setSectionEnabled(SECTION_EDIT, enableAdvanced);
		setSectionEnabled(SECTION_SHARE, true);
		setSectionEnabled(SECTION_VIEW, true);
		setSectionEnabled(SECTION_PLAN, true);
		if (!enableAdvanced && SECTION_EDIT.equals(activeSection)) {
			setActiveSection(SECTION_PLAN);
		}
	}

	private void setSectionEnabled(String section, boolean enabled) {
		JToggleButton button = sectionButtons.get(section);
		if (button != null) {
			button.setEnabled(enabled);
		}
	}

	private void updateEmptyState(boolean empty) {
		emptyStateTitle.setText(empty ? "Start your next schedule" : projectTitleLabel.getText());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				workspaceHost.revalidate();
				workspaceHost.repaint();
			}
		});
	}

	private void showContextCard() {
		CardLayout layout = (CardLayout) contextCards.getLayout();
		layout.show(contextCards, key(activeProfile, activeSection));
	}

	private void selectRailButton(String viewId) {
		JToggleButton button = railButtons.get(viewId);
		if (button != null) {
			button.setSelected(true);
		}
	}

	private String profileForView(String viewId) {
		if (ACTION_RESOURCES.equals(viewId)) {
			return PROFILE_RESOURCE;
		}
		if (ACTION_NETWORK.equals(viewId) || ACTION_WBS.equals(viewId) || ACTION_RBS.equals(viewId) || ACTION_REPORT.equals(viewId)) {
			return PROFILE_STRUCTURE;
		}
		return PROFILE_TASK;
	}

	private String profileSummary(String profile) {
		if (PROFILE_RESOURCE.equals(profile)) {
			return "Resource planning workspace with focused editing and filtering controls.";
		}
		if (PROFILE_STRUCTURE.equals(profile)) {
			return "Review structure, relationships, and presentation views from a calmer layout.";
		}
		return "Task planning workspace with quick access to scheduling, linking, and review controls.";
	}

	private String key(String profile, String section) {
		return profile + ":" + section;
	}

	private String safeText(String id) {
		String text = menuManager.getTextForId(id);
		return text != null ? text : id;
	}

	private String capitalize(String value) {
		if (value == null || value.length() == 0) {
			return "";
		}
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}

	private Icon iconFor(String id) {
		if (ACTION_GANTT.equals(id))
			return IconManager.getIcon("view.gantt");
		if (ACTION_TRACKING_GANTT.equals(id))
			return IconManager.getIcon("view.trackingGantt");
		if (ACTION_NETWORK.equals(id))
			return IconManager.getIcon("view.network");
		if (ACTION_RESOURCES.equals(id))
			return IconManager.getIcon("view.resources");
		if (ACTION_WBS.equals(id))
			return IconManager.getIcon("view.WBS");
		if (ACTION_RBS.equals(id))
			return IconManager.getIcon("view.RBS");
		if (ACTION_REPORT.equals(id))
			return IconManager.getIcon("view.report");
		if (ACTION_NEW_PROJECT.equals(id))
			return IconManager.getIcon("ribbon.new");
		if (ACTION_OPEN_PROJECT.equals(id))
			return IconManager.getIcon("ribbon.open");
		if (ACTION_SAVE_PROJECT.equals(id))
			return IconManager.getIcon("ribbon.save");
		if (ACTION_SAVE_PROJECT_AS.equals(id))
			return IconManager.getIcon("ribbon.saveAs");
		if (ACTION_CLOSE_PROJECT.equals(id))
			return IconManager.getIcon("ribbon.close");
		if (ACTION_PRINT.equals(id))
			return IconManager.getIcon("ribbon.print");
		if (ACTION_PDF.equals(id))
			return IconManager.getIcon("ribbon.pdf");
		if (ACTION_UNDO.equals(id))
			return IconManager.getIcon("menu24.undo");
		if (ACTION_REDO.equals(id))
			return IconManager.getIcon("menu24.redo");
		if (ACTION_INSERT_TASK.equals(id))
			return IconManager.getIcon("ribbon.insert");
		if (ACTION_INSERT_RESOURCE.equals(id))
			return IconManager.getIcon("ribbon.insert");
		if (ACTION_DELETE.equals(id))
			return IconManager.getIcon("ribbon.delete");
		if (ACTION_INDENT.equals(id))
			return IconManager.getIcon("ribbon.indent");
		if (ACTION_OUTDENT.equals(id))
			return IconManager.getIcon("ribbon.outdent");
		if (ACTION_LINK.equals(id))
			return IconManager.getIcon("ribbon.link");
		if (ACTION_UNLINK.equals(id))
			return IconManager.getIcon("ribbon.unlink");
		if (ACTION_INFORMATION.equals(id))
			return IconManager.getIcon("ribbon.information");
		if (ACTION_PROJECT_INFORMATION.equals(id))
			return IconManager.getIcon("ribbon.information");
		if (ACTION_CHANGE_WORKING_TIME.equals(id))
			return IconManager.getIcon("ribbon.calendar");
		if (ACTION_NOTES.equals(id))
			return IconManager.getIcon("ribbon.notes");
		if (ACTION_ASSIGN_RESOURCES.equals(id))
			return IconManager.getIcon("menu24.assignResources");
		if (ACTION_ZOOM_IN.equals(id))
			return IconManager.getIcon("ribbon.zoomIn");
		if (ACTION_ZOOM_OUT.equals(id))
			return IconManager.getIcon("ribbon.zoomOut");
		if (ACTION_FIND.equals(id))
			return IconManager.getIcon("ribbon.find");
		if (ACTION_SCROLL_TO_TASK.equals(id))
			return IconManager.getIcon("menu24.scrollToTask");
		if (ACTION_CHOOSE_FILTER.equals(id))
			return IconManager.getIcon("menu24.filter");
		if (ACTION_LOCALE.equals(id))
			return IconManager.getIcon("menu16.locale");
		if (ACTION_PROJECTS_DIALOG.equals(id))
			return IconManager.getIcon("dialog.projects");
		return null;
	}
}
