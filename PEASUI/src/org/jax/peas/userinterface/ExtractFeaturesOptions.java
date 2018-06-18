package org.jax.peas.userinterface;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class ExtractFeaturesOptions extends JPanel{

	private JTextField _peakfield;
	private JTextField _knownfield;
	private JTextField _consfield;
	private JTextField _ctcffield;

	public ExtractFeaturesOptions(UIUtil util, JFileChooser fc, String path, FileFilter bedfilter, FileFilter motifsfilter){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		_peakfield = new JTextField(path+"hg19.filter.bed");
		_knownfield = new JTextField(path+"humantop_Nov2016_HOMER.motifs");
		_consfield = new JTextField(path+"phastCons46wayPlacental.bed");
		_ctcffield = new JTextField(path+"CTCF.motifs");

		util.setFieldPanel(this, fc, new JLabel("Peak Filter:"), _peakfield, new JButton("..."), bedfilter, false);
		util.setFieldPanel(this, fc, new JLabel("Known Motifs:"), _knownfield, new JButton("..."), motifsfilter, false);
		util.setFieldPanel(this, fc, new JLabel("Conservation:"), _consfield, new JButton("..."), bedfilter, false);
		util.setFieldPanel(this, fc, new JLabel("CTCF Motifs:"), _ctcffield, new JButton("..."), motifsfilter, false);

	}
	
	public void setPath(String path){
		_peakfield.setText(path+"hg19.filter.bed");
		_knownfield.setText(path+"humantop_Nov2016_HOMER.motifs");
		_consfield.setText(path+"phastCons46wayPlacental.bed");
		_ctcffield.setText(path+"CTCF.motifs");
	}
	
	
	public String getPeakField(){
		return _peakfield.getText();
	}
	
	public String getKnownMotifField(){
		return _knownfield.getText();
	}
	
	public String getConservationField(){
		return _consfield.getText();
	}
	
	public String getCTCFField(){
		return _ctcffield.getText();
	}
	
	
}
