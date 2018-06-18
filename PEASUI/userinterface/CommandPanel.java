package org.jax.peas.userinterface;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class CommandPanel extends JPanel {

	public CommandPanel(Font font, final String command){
		JTextArea ta = new JTextArea(8, 12);
		ta.setText(command);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		JScrollPane sp = new JScrollPane(ta);
		
		JLabel l = new JLabel("Copy and paste the following command into a terminal:");
		l.setHorizontalAlignment(JLabel.LEFT);
		l.setFont(font);
		
		JPanel copypanel = new JPanel();
		copypanel.setLayout(new BorderLayout());
		JButton copy = new JButton("Copy");
		copypanel.add(copy, BorderLayout.EAST);
		copy.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection sl = new StringSelection(command);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sl, sl);
			}
			
		});
		
		setLayout(new BorderLayout());
		add(l, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
		add(copypanel, BorderLayout.SOUTH);
		
	}
	
}
