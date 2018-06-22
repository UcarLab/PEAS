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

	private JTextField _referencefield;
	private JTextField _peakfield;
	private JTextField _knownfield;
	private JTextField _consfield;
	private JTextField _ctcffield;

	public ExtractFeaturesOptions(UIUtil util, JFileChooser fc, String path, FileFilter bedfilter, FileFilter motifsfilter){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		_referencefield = new JTextField("hg19");
		_referencefield.setColumns(12);
		_peakfield = new JTextField(path+"extraction_files/hg19.filter.bed");
		_knownfield = new JTextField(path+"extraction_files/humantop_Nov2016_HOMER.motifs");
		_consfield = new JTextField(path+"extraction_files/phastCons46wayPlacental.bed");
		_ctcffield = new JTextField(path+"extraction_files/CTCF.motifs");


		this.add(util.getHorizontalField(new JLabel("Homer Reference:"), _referencefield));
		util.setFieldPanel(this, fc, new JLabel("Peak Filter:"), _peakfield, new JButton("..."), bedfilter, false);
		util.setFieldPanel(this, fc, new JLabel("Known Motifs:"), _knownfield, new JButton("..."), motifsfilter, false);
		util.setFieldPanel(this, fc, new JLabel("Conservation:"), _consfield, new JButton("..."), bedfilter, false);
		util.setFieldPanel(this, fc, new JLabel("CTCF Motifs:"), _ctcffield, new JButton("..."), motifsfilter, false);

	}
	
	public void setPath(String path){
		_peakfield.setText(path+"extraction_files/hg19.filter.bed");
		_knownfield.setText(path+"extraction_files/humantop_Nov2016_HOMER.motifs");
		_consfield.setText(path+"extraction_files/phastCons46wayPlacental.bed");
		_ctcffield.setText(path+"extraction_files/CTCF.motifs");
	}
	
	public String getReference(){
		return _referencefield.getText();
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
