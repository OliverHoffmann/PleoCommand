package pleocmd.itfc.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pleocmd.Log;
import pleocmd.cfg.ConfigValue;
import pleocmd.exc.PipeException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.PipePart;

public final class PipePartPanel<E extends PipePart> extends JPanel {

	private static final long serialVersionUID = 1806583246927239923L;

	private final List<Class<E>> availableParts;

	private final PipePartTableModel<E> tableModel;

	private final JTable table;

	private final JButton btnAdd;

	private final JButton btnRemove;

	private final JButton btnModify;

	private final JButton btnUp;

	private final JButton btnDown;

	private final JButton btnClear;

	private boolean okPressed;

	public PipePartPanel(final List<Class<E>> availableParts) {
		this.availableParts = availableParts;

		final Layouter lay = new Layouter(this);
		tableModel = new PipePartTableModel<E>();
		table = new JTable(tableModel);
		getTable().getTableHeader().setVisible(false);
		getTable().setShowGrid(false);
		getTable().setColumnSelectionAllowed(false);
		getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					final int idx = getTable().getSelectedRow();
					if (idx != -1) modifyPipePart(idx);
				}
			}
		});
		getTable().getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(final ListSelectionEvent e) {
						updateState();
					}
				});
		lay.addWholeLine(new JScrollPane(getTable()), true);

		btnAdd = lay.addButton(Button.Add, new Runnable() {
			@Override
			public void run() {
				showAddPPMenu();
			}
		});
		btnRemove = lay.addButton(Button.Remove, new Runnable() {
			@Override
			public void run() {
				removePipeParts(getTable().getSelectedRows());
			}
		});
		btnModify = lay.addButton(Button.Modify, new Runnable() {
			@Override
			public void run() {
				final int idx = getTable().getSelectedRow();
				if (idx != -1) modifyPipePart(idx);
			}
		});
		btnUp = lay.addButton(Button.Up, new Runnable() {
			@Override
			public void run() {
				final int idx = getTable().getSelectedRow();
				if (idx > 0) movePipePartUp(idx, idx - 1);
			}
		});
		btnDown = lay.addButton(Button.Down, new Runnable() {
			@Override
			public void run() {
				final int idx = getTable().getSelectedRow();
				if (idx != -1 && idx < getTableModel().getRowCount() - 1)
					movePipePartUp(idx, idx + 1);
			}
		});
		lay.addSpacer();
		btnClear = lay.addButton(Button.Clear, new Runnable() {
			@Override
			public void run() {
				getTableModel().clear();
			}
		});

		updateState();
	}

	protected void showAddPPMenu() {
		final JPopupMenu menu = new JPopupMenu();
		for (final Class<E> part : availableParts) {
			final JMenuItem item = new JMenuItem(part.getSimpleName());
			menu.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					addPipePart(part);
				}

			});
		}
		menu.show(btnAdd, btnAdd.getWidth() / 2, btnAdd.getHeight());
	}

	public void addPipePart(final Class<E> part) {
		try {
			final E pp = part.newInstance();
			if (createConfigureDialog("Add", pp)) tableModel.addPipePart(pp);
		} catch (final InstantiationException e) {
			Log.error(e);
		} catch (final IllegalAccessException e) {
			Log.error(e);
		}
		updateState();
	}

	public void removePipeParts(final int[] indices) {
		for (final int idx : indices)
			tableModel.removePipePart(idx);
		updateState();
	}

	public void modifyPipePart(final int index) {
		createConfigureDialog("Configure", tableModel.getPipePart(index));
	}

	public void movePipePartUp(final int indexOld, final int indexNew) {
		tableModel
				.insertPipePart(tableModel.removePipePart(indexOld), indexNew);
		getTable().getSelectionModel().clearSelection();
		getTable().getSelectionModel().setSelectionInterval(indexNew, indexNew);
	}

	public PipePartTableModel<E> getTableModel() {
		return tableModel;
	}

	public boolean createConfigureDialog(final String prefix, final PipePart pp) {
		// no need to configure if no values assigned
		if (pp.getGroup().isEmpty()) return true;
		okPressed = false;

		final JDialog dlg = new JDialog();
		dlg.setTitle(String.format("%s %s", prefix, pp));
		final Layouter lay = new Layouter(dlg);
		for (final ConfigValue v : pp.getGroup().getValueMap().values()) {
			// each config-value gets its own JPanel so they don't
			// interfere with each other. JPanels covering
			// more than one line are not yet tested.
			// LBL1 SUB1
			// LBL2 SUB2
			// LBL3 SUB3
			// BUTTONS
			final JPanel sub = new JPanel();
			final Layouter laySub = new Layouter(sub);
			final String compLabel = v.getLabel() + ":";
			final JLabel lbl = new JLabel(compLabel, SwingConstants.RIGHT);
			lbl.setVerticalAlignment(SwingConstants.TOP);
			lay.add(lbl, false);
			lay.addWholeLine(sub, false);
			v.insertGUIComponents(laySub);
		}

		final JPanel buttons = new JPanel();
		final Layouter lb = new Layouter(buttons);

		lay.addVerticalSpacer();
		lay.addWholeLine(buttons, false);

		lb
				.addButton(Button.Help, Layouter.help(dlg,
						"PipePartConfigureDialog"));
		lb.addSpacer();
		dlg.getRootPane().setDefaultButton(
				lb.addButton(Button.Ok, new Runnable() {
					@Override
					public void run() {
						saveConfigChanges(pp);
						dlg.dispose();
					}
				}));
		lb.addButton(Button.Apply, new Runnable() {
			@Override
			public void run() {
				saveConfigChanges(pp);
			}
		});
		lb.addButton(Button.Cancel, new Runnable() {
			@Override
			public void run() {
				dlg.dispose();
			}
		});

		dlg.pack();
		dlg.setLocationRelativeTo(null);
		dlg.setModal(true);
		HelpDialog.closeHelpIfOpen();
		dlg.setVisible(true);
		HelpDialog.closeHelpIfOpen();

		return okPressed;
	}

	protected void saveConfigChanges(final PipePart pp) {
		try {
			for (final ConfigValue v : pp.getGroup().getValueMap().values())
				v.setFromGUIComponents();
			pp.configure();
			okPressed = true;
		} catch (final PipeException e) {
			Log.error(e);
		}
	}

	protected JTable getTable() {
		return table;
	}

	public void assignPipeParts(final List<E> ppList) {
		tableModel.clear();
		tableModel.addPipeParts(ppList);
		updateState();
	}

	public void updateState() {
		btnAdd.setEnabled(true);
		final int foc = table.getSelectedRow();
		final int cnt = tableModel.getRowCount();
		btnRemove.setEnabled(table.getSelectedRowCount() > 0);
		btnModify.setEnabled(foc != -1);
		btnUp.setEnabled(foc > 0);
		btnDown.setEnabled(foc != -1 && foc < cnt - 1);
		btnClear.setEnabled(cnt > 0);
	}

}
