package org.virion.jam.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import org.virion.jam.demo.menus.DemoMenuHandler;
import org.virion.jam.framework.DocumentFrame;
import org.virion.jam.panels.SearchPanel;
import org.virion.jam.panels.SearchPanelListener;
import org.virion.jam.panels.StatusBar;
import org.virion.jam.toolbar.Toolbar;
import org.virion.jam.toolbar.ToolbarAction;
import org.virion.jam.toolbar.ToolbarButton;
import org.virion.jam.util.IconUtils;

public class DemoFrame extends DocumentFrame implements DemoMenuHandler {

	private StatusBar statusBar;

	private SearchPanel filterPanel;
	private JPopupMenu filterPopup;

	public DemoFrame(String title) {
		super();

		setTitle(title);

		getSaveAction().setEnabled(false);
		getSaveAsAction().setEnabled(false);

		getCutAction().setEnabled(false);
		getCopyAction().setEnabled(false);
		getPasteAction().setEnabled(false);
		getDeleteAction().setEnabled(false);
		getSelectAllAction().setEnabled(false);
		getFindAction().setEnabled(true);

		getZoomWindowAction().setEnabled(false);
	}

	public void initializeComponents() {

		setSize(new Dimension(1024, 768));

		Toolbar toolBar = new Toolbar();

		Icon infoToolIcon = IconUtils.getIcon(this.getClass(), "images/infoTool.png");

		JButton infoToolButton = new ToolbarButton(
				new ToolbarAction("Get Info", "Get Info...", infoToolIcon) {
					public void actionPerformed(ActionEvent e){
						// do something
					}
				});
		toolBar.addComponent(infoToolButton);
		infoToolButton.setEnabled(false);


		toolBar.addSeparator();


		toolBar.addFlexibleSpace();

		filterPopup = new JPopupMenu();
		filterPanel = new SearchPanel("Filter", filterPopup, true);

		filterPanel.addSearchPanelListener(new SearchPanelListener() {

			/**
			 * Called when the user requests a search by pressing return having
			 * typed a search string into the text field. If the continuousUpdate
			 * flag is true then this method is called when the user types into
			 * the text field.
			 *
			 * @param searchString the user's search string
			 */
			public void searchStarted(String searchString) {
				int index = filterPopup.getSelectionModel().getSelectedIndex();
				if (index == -1) index = 0;

				// do something
			}

			/**
			 * Called when the user presses the cancel search button or presses
			 * escape while the search is in focus.
			 */
			public void searchStopped() {
			}
		});

		JPanel panel3 = new JPanel(new FlowLayout());

		panel3.add(filterPanel);

		toolBar.addComponent(panel3);

		statusBar = new StatusBar("");
		statusBar.setStatusProvider(null);

		JPanel topPanel = new JPanel(new BorderLayout(0,0));
		topPanel.add(toolBar, BorderLayout.NORTH);
		topPanel.add(statusBar, BorderLayout.CENTER);

		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(topPanel, BorderLayout.NORTH);

		JList list1 = new JList();
		list1.setBorder(BorderFactory.createEmptyBorder());

		JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, list1, new JPanel());
		splitPane1.setBorder(BorderFactory.createEmptyBorder());
		splitPane1.setDividerLocation(240);
		splitPane1.setContinuousLayout(true);
		splitPane1.putClientProperty("Quaqua.SplitPane.style","bar");

		JList list = new JList();
		list.setBorder(BorderFactory.createEmptyBorder());
		list.setBackground(new Color(231, 237, 246));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, list, splitPane1);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setDividerLocation(120);
		splitPane.setContinuousLayout(true);
		splitPane.putClientProperty("Quaqua.SplitPane.style","bar");
		splitPane.setDividerSize(1);

		getContentPane().add(splitPane, BorderLayout.CENTER);
	}

	protected boolean readFromFile(File file) throws FileNotFoundException, IOException {

		return false;
	}

	protected boolean writeToFile(File file) {
		return false;
	}

	public void doCopy() {
	}

	public final void doFind() {
	}

	public final void doFindAgain() {
	}

	public JComponent getExportableComponent() {
		return (JComponent)getContentPane();
	}

	public Action getFirstAction() {
		return firstAction;
	}

	public Action getSecondAction() {
		return secondAction;
	}

	private AbstractAction firstAction = new AbstractAction("First") {
		public void actionPerformed(ActionEvent ae) {
		}
	};

	private AbstractAction secondAction = new AbstractAction("Second") {
		public void actionPerformed(ActionEvent ae) {
		}
	};
}