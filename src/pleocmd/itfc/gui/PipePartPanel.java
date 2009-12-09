package pleocmd.itfc.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import pleocmd.Log;
import pleocmd.exc.PipeException;
import pleocmd.itfc.gui.icons.IconLoader;
import pleocmd.pipe.PipePart;

public final class PipePartPanel<E extends PipePart> extends JPanel {

	private static final long serialVersionUID = 1806583246927239923L;

	private final DefaultListModel listmodel;

	public PipePartPanel(final Class<E>[] availableParts) {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 8;
		// TODO replace with JTable
		listmodel = new DefaultListModel(); // TODO display more than just the
		// component's name (part of config, ...)
		final JList list = new JList(listmodel);
		add(list, gbc);
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridy = 1;

		gbc.gridx = 0;
		final JButton add = new JButton("Add", IconLoader
				.getIcon("list-add.png"));
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e1) {
				final JPopupMenu menu = new JPopupMenu();
				for (final Class<E> part : availableParts) {
					final JMenuItem item = new JMenuItem(part.getSimpleName());
					menu.add(item);
					item.addActionListener(new ActionListener() {
						@Override
						@SuppressWarnings("synthetic-access")
						public void actionPerformed(final ActionEvent e2) {
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
									listmodel.addElement(pp);
								else
									pp.tryClose();
							} catch (final PipeException e) {
								Log.error(e);
								pp.tryClose();
								return;
							}
						}
					});
				}
				menu.show(add, add.getWidth() / 2, add.getHeight());
			}
		});
		gbc.gridx = 1;
		add(add, gbc);

		final JButton remove = new JButton("Remove", IconLoader
				.getIcon("list-remove.png"));
		gbc.gridx = 2;
		add(remove, gbc);
		remove.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings( { "unchecked", "synthetic-access" })
			public void actionPerformed(final ActionEvent e) {
				for (final int idx : list.getSelectedIndices())
					((E) listmodel.remove(idx)).tryClose();
			}
		});

		final JButton modify = new JButton("Modify", IconLoader
				.getIcon("document-edit.png"));
		gbc.gridx = 3;
		add(modify, gbc);
		modify.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings( { "unchecked", "synthetic-access" })
			public void actionPerformed(final ActionEvent e) {
				final int idx = list.getSelectedIndex();
				if (idx != -1)
					try {
						((E) listmodel.get(idx)).getConfig().readFromGUI(
								"Configure");
					} catch (final PipeException exc) {
						Log.error(exc);
					}
			}
		});

		final JButton up = new JButton("Up", IconLoader.getIcon("arrow-up.png"));
		gbc.gridx = 4;
		add(up, gbc);
		up.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final int idx = list.getSelectedIndex();
				if (idx > 0) {
					listmodel.insertElementAt(listmodel.remove(idx), idx - 1);
					list.setSelectedIndex(idx - 1);
				}
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
				final int idx = list.getSelectedIndex();
				if (idx != -1 && idx < listmodel.size() - 1) {
					listmodel.insertElementAt(listmodel.remove(idx), idx + 1);
					list.setSelectedIndex(idx + 1);
				}
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
				listmodel.clear();
			}
		});
	}

	public int getPipePartCount() {
		return listmodel.getSize();
	}

	@SuppressWarnings("unchecked")
	public E getPipePart(final int index) {
		return (E) listmodel.getElementAt(index);
	}

	public void addPipePart(final E pp) {
		listmodel.addElement(pp);
	}

	public void addPipeParts(final List<E> list) {
		for (final E pp : list)
			addPipePart(pp);
	}

}
