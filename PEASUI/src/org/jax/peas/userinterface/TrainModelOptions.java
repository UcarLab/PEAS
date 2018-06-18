package org.jax.peas.userinterface;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class TrainModelOptions extends JPanel{

	private JTextField _fifield;
	private JTextField _labelfield;
	private JButton _fibutton;
	private JButton _labelbutton;


	public TrainModelOptions(UIUtil util, JFileChooser fc, String path, FileFilter featurefilter){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		_fifield = new JTextField(path+"features.txt");
		_labelfield = new JTextField(path+"labelencoder.txt");
		
		_fibutton = new JButton("...");
		_labelbutton = new JButton("...");

		util.setFieldPanel(this, fc, new JLabel("Feature Indices:"), _fifield, _fibutton, null, false);
		util.setFieldPanel(this, fc, new JLabel("Label Encoder:"), _labelfield, _labelbutton, null, false);
	}
	
	public String getFeatureIndexFileField(){
		return _fifield.getText();
	}
	
	
	public String getLabelEncoderField(){
		return _labelfield.getText();
	}
	
	public void setPath(String path){
		_fifield.setText(path+"features.txt");
		_labelfield.setText(path+"labelencoder.txt");
	}
	
	public void setEnabled(boolean enabled){
		_fifield.setEnabled(enabled);
		_labelfield.setEnabled(enabled);
		_fibutton.setEnabled(enabled);
		_labelbutton.setEnabled(enabled);
	}
	
}
