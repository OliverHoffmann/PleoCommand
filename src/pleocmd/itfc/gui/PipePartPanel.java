package pleocmd.itfc.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import pleocmd.Log;
import pleocmd.exc.PipeException;
import pleocmd.itfc.gui.icons.IconLoader;
import pleocmd.pipe.PipePart;

public final class PipePartPanel<E extends PipePart> extends JPanel {

	private static final long serialVersionUID = 1806583246927239923L;

	private final JTable table;

	private final PipePartTableModel<E> tableModel;

	private final Class<E>[] availableParts;

	private final JButton btnAdd;

	public PipePartPanel(final Class<E>[] availableParts) {
		this.availableParts = availableParts;

		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 8;
		tableModel = new PipePartTableModel<E>();
		table = new JTable(tableModel);
		table.getTableHeader().setVisible(false);
		table.setShowGrid(false);
		table.setColumnSelectionAllowed(false);
		add(new JScrollPane(table), gbc);
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridy = 1;

		gbc.gridx = 0;
		btnAdd = new JButton("Add", IconLoader.getIcon("list-add.png"));
		btnAdd.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e1) {
				showAddPPMenu();
			}
		});
		gbc.gridx = 1;
		add(btnAdd, gbc);

		final JButton remove = new JButton("Remove", IconLoader
				.getIcon("list-remove.png"));
		gbc.gridx = 2;
		add(remove, gbc);
		remove.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				removePipeParts(table.getSelectedRows());
			}
		});

		final JButton modify = new JButton("Modify", IconLoader
				.getIcon("document-edit.png"));
		gbc.gridx = 3;
		add(modify, gbc);
		modify.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final int idx = table.getSelectedRow();
				if (idx != -1) modifyPipePart(idx);
			}
		});

		final JButton up = new JButton("Up", IconLoader.getIcon("arrow-up.png"));
		gbc.gridx = 4;
		add(up, gbc);
		up.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final int idx = table.getSelectedRow();
				if (idx > 0) movePipePartUp(idx, idx - 1);
			}
		});

		final JButton down = new JButton("Down", IconLoader
				.getIcon("arrow-down.png"));
		gbc.gridx = 5;
		add(down, gbc);
		down.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final int idx = table.getSelectedRow();
				if (idx != -1 && idx < tableModel.getRowCount() - 1)
					movePipePartUp(idx, idx + 1);
			}
		});

		gbc.gridx = 6;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;
		final JButton clear = new JButton("Clear", IconLoader
				.getIcon("archive-remove.png"));
		gbc.gridx = 7;
		add(clear, gbc);
		clear.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				tableModel.clear();
			}
		});
	}

	private void showAddPPMenu() {
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
		E pp;
		try {
			pp = part.newInstance();
		} catch (final InstantiationException e) {
			Log.error(e);
			return;
		} catch (final IllegalAccessException e) {
			Log.error(e);
			return;
		}
		try {
			if (pp.getConfig().readFromGUI("Add"))
				tableModel.addPipePart(pp);
			else
				pp.tryClose();
		} catch (final PipeException e) {
			Log.error(e);
			pp.tryClose();
			return;
		}
	}

	public void removePipeParts(final int[] indices) {
		for (final int idx : indices)
			tableModel.removePipePart(idx).tryClose();
	}

	public void modifyPipePart(final int index) {
		try {
			tableModel.getPipePart(index).getConfig().readFromGUI("Configure");
		} catch (final PipeException exc) {
			Log.error(exc);
		}
	}

	public void movePipePartUp(final int indexOld, final int indexNew) {
		tableModel
				.insertPipePart(tableModel.removePipePart(indexOld), indexNew);
		table.getSelectionModel().clearSelection();
		table.getSelectionModel().setSelectionInterval(indexNew, indexNew);
	}

	public PipePartTableModel<E> getTableModel() {
		return tableModel;
	}

}
