package org.jax.peas.userinterface;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jax.peas.userinterface.inputvalidation.AnnotateValidator;
import org.jax.peas.userinterface.inputvalidation.ExtractFeaturesValidator;
import org.jax.peas.userinterface.inputvalidation.FeaturePrediction;
import org.jax.peas.userinterface.inputvalidation.ModelPredictorValidator;
import org.jax.peas.userinterface.inputvalidation.ModelTrainerValidator;
import org.jax.peas.userinterface.inputvalidation.TSSPromoterValidator;

public class PEASUserInterface {
	private final JFileChooser _fc = new JFileChooser();
	private final Font _labelfont = new Font("Arial", Font.BOLD, 12);
	private final FileFilter _featurefilter = new FileNameExtensionFilter("Feature Text File (.txt)", "txt");
	private final FileFilter _bamfilter = new FileNameExtensionFilter("BAM File (.bam)", "bam");
	private final FileFilter _modelfilter = new FileNameExtensionFilter("Trained Model File (.pkl)", "pkl");
	private final FileFilter _bedfilter = new FileNameExtensionFilter("BED File (.bed)", "bed");
	private final FileFilter _motifsfilter = new FileNameExtensionFilter("Motif File (.motif, .motifs)", "motif", "motifs");
	private final FileFilter _fastafilter = new FileNameExtensionFilter("FASTA File (.fa)", "fa", "fasta");
	private String _path = "./";
	private String _pythoncmd = "python";
	private JFrame _frame;
	
	private JTextField _pathfield;
	private JTextField _pcfield;
	private TrainModelOptions _tm;
	private TrainModelOptions _etm;
	private TrainModelOptions _ptm;
	private ExtractFeaturesOptions _efo;
	private JTextField _emf;
	private JTextField _pmf;
	private JTextField _eclassfield;
	private JTextField _tclassfield;
	private JTextField _promoterfeaturefile;
	private JTextField _enhancerfeaturefile;

	
	private UIUtil _util = new UIUtil();
	
	public static void main(String[] args) {
	    try {
	    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) { }
	    catch (ClassNotFoundException e) { }
	    catch (InstantiationException e) { }
	    catch (IllegalAccessException e) { }
		new PEASUserInterface();
		
	}
	
	public PEASUserInterface(){
		//try {
			_path = System.getProperty("user.dir");//PEASUserInterface.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		//} catch (URISyntaxException e) {
			//e.printStackTrace();
		//}
		_fc.setCurrentDirectory(new File(_path));
		
		//Path related fields that need to know about each other and update
		_eclassfield = new JTextField(_path+"models/classes.txt");
		_tclassfield = new JTextField(_path+"models/classes.txt");
		_pathfield = new JTextField(_path);
		_pathfield.setColumns(15);
		_pcfield = new JTextField(_pythoncmd);
		_pcfield.setColumns(15);
		_etm = new TrainModelOptions(_util, _fc, _path, _featurefilter);
		_tm = new TrainModelOptions(_util, _fc, _path, _featurefilter);
		_ptm = new TrainModelOptions(_util, _fc, _path, _featurefilter);
		_efo = new ExtractFeaturesOptions(_util, _fc, _path, _bedfilter, _motifsfilter);
		_emf = new JTextField(_path+"models/enhancermodel.pkl");
		_pmf = new JTextField(_path+"models/promotermodel.pkl");
		
		//Set up the Frame
		_frame = new JFrame();
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel featureextpanel = getFeatureExtractionPanel();
		JPanel featureannpanel = getFeatureAnnotationPanel();
		JPanel promoterpanel = getPromoterPredictionPanel();
		JPanel enhpanel = getEnhancerPredictionPanel();
		JPanel trainpanel = getTrainModelPanel();
		JPanel configpanel = getConfigPanel();

		JPanel mainpanel = getMainPanel(featureextpanel,featureannpanel,promoterpanel, enhpanel, trainpanel, configpanel);

		_frame.add(mainpanel);
		_frame.setSize(540,480);
		_frame.setResizable(false);
		_frame.setVisible(true);
		_frame.setTitle("PEAS");
		
	}
	
	private JPanel getMainPanel(final JPanel fepanel, final JPanel fapanel,
			final JPanel ppanel, final JPanel epanel, final JPanel trainpanel, final JPanel configpanel){
		JPanel rvpanel = new JPanel();
		rvpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JPanel leftpanel = new JPanel();
		final JPanel rightpanel = new JPanel();
		rvpanel.setLayout(new BorderLayout());
		rvpanel.add(leftpanel, BorderLayout.WEST);
		rvpanel.add(rightpanel, BorderLayout.CENTER);
		
		rightpanel.add(fepanel);

		leftpanel.setLayout(new BoxLayout(leftpanel, BoxLayout.Y_AXIS));
		try {
			
			URL logourl = PEASUserInterface.class.getResource("/resources/logo.png");
			//BufferedImage logo = ImageIO.read(logourl);
			ImageIcon logoicon = new ImageIcon(logourl);
			JLabel logolabel = new JLabel();
			logolabel.setSize(100,100);
			logolabel.setIcon(logoicon);
			leftpanel.add(logolabel);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		JLabel predl = new JLabel("Enhancer Prediction:");
		predl.setFont(_labelfont);
		JLabel efl = new JLabel("Step 1:");
		efl.setFont(_labelfont);
		JLabel ppl = new JLabel("Step 2:");
		ppl.setFont(_labelfont);
		JLabel pel = new JLabel("Step 3:");
		pel.setFont(_labelfont);

		JLabel trainl = new JLabel("Model Training Tools:");
		trainl.setFont(_labelfont);
		
		int width = 150;
		int height = 50;
		final JButton ef = new JButton("Extract Features");
		ef.setPreferredSize(new Dimension(width,height));
		ef.setMaximumSize(new Dimension(width,height));
		ef.setEnabled(false);
		ef.setFocusable(false);
		
		final JButton pp = new JButton("Predict Promoters");
		pp.setPreferredSize(new Dimension(width,height));
		pp.setMaximumSize(new Dimension(width,height));
		pp.setFocusable(false);
		
		final JButton pe = new JButton("Predict Enhancers");
		pe.setPreferredSize(new Dimension(width,height));
		pe.setMaximumSize(new Dimension(width,height));
		pe.setFocusable(false);
		
		
		final JButton af = new JButton("Annotate Features");
		af.setPreferredSize(new Dimension(width,height));
		af.setMaximumSize(new Dimension(width,height));
		af.setFocusable(false);
		
		final JButton tm = new JButton("Train Model");
		tm.setPreferredSize(new Dimension(width,height));
		tm.setMaximumSize(new Dimension(width,height));
		tm.setFocusable(false);
		
		
		final JButton config = new JButton("Configure");
		config.setPreferredSize(new Dimension(width,height));
		config.setMaximumSize(new Dimension(width,height));
		config.setFocusable(false);

		ef.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ef.setEnabled(false);
				af.setEnabled(true);
				pp.setEnabled(true);
				pe.setEnabled(true);
				tm.setEnabled(true);
				config.setEnabled(true);
				
				rightpanel.remove(fepanel);
				rightpanel.remove(fapanel);
				rightpanel.remove(ppanel);
				rightpanel.remove(epanel);
				rightpanel.remove(trainpanel);
				rightpanel.remove(configpanel);
				rightpanel.add(fepanel);
				
				_frame.revalidate();
				_frame.repaint();

			}
		});
		
		af.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ef.setEnabled(true);
				af.setEnabled(false);
				pp.setEnabled(true);
				pe.setEnabled(true);
				tm.setEnabled(true);
				config.setEnabled(true);
				
				rightpanel.remove(fepanel);
				rightpanel.remove(fapanel);
				rightpanel.remove(ppanel);
				rightpanel.remove(epanel);
				rightpanel.remove(trainpanel);
				rightpanel.remove(configpanel);
				rightpanel.add(fapanel);
				
				_frame.revalidate();
				_frame.repaint();

			}
		});
		
		pp.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ef.setEnabled(true);
				af.setEnabled(true);
				pp.setEnabled(false);
				pe.setEnabled(true);
				tm.setEnabled(true);
				config.setEnabled(true);
				
				rightpanel.remove(fepanel);
				rightpanel.remove(fapanel);
				rightpanel.remove(ppanel);
				rightpanel.remove(epanel);
				rightpanel.remove(trainpanel);
				rightpanel.remove(configpanel);
				rightpanel.add(ppanel);
				
				_frame.revalidate();
				_frame.repaint();


			}
		});
		
		pe.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ef.setEnabled(true);
				af.setEnabled(true);
				pp.setEnabled(true);
				pe.setEnabled(false);
				tm.setEnabled(true);
				config.setEnabled(true);
				
				rightpanel.remove(fepanel);
				rightpanel.remove(fapanel);
				rightpanel.remove(ppanel);
				rightpanel.remove(epanel);
				rightpanel.remove(trainpanel);
				rightpanel.remove(configpanel);
				rightpanel.add(epanel);

				_frame.revalidate();
				_frame.repaint();

			}
		});
		
		
		tm.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ef.setEnabled(true);
				af.setEnabled(true);
				pp.setEnabled(true);
				pe.setEnabled(true);
				tm.setEnabled(false);
				config.setEnabled(true);
				
				rightpanel.remove(fepanel);
				rightpanel.remove(fapanel);
				rightpanel.remove(ppanel);
				rightpanel.remove(epanel);
				rightpanel.remove(trainpanel);
				rightpanel.remove(configpanel);
				rightpanel.add(trainpanel);

				_frame.revalidate();
				_frame.repaint();
			}
		});
		
		config.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ef.setEnabled(true);
				af.setEnabled(true);
				pp.setEnabled(true);
				pe.setEnabled(true);
				tm.setEnabled(true);
				config.setEnabled(false);
				
				rightpanel.remove(fepanel);
				rightpanel.remove(fapanel);
				rightpanel.remove(ppanel);
				rightpanel.remove(epanel);
				rightpanel.remove(trainpanel);
				rightpanel.remove(configpanel);
				rightpanel.add(configpanel);

				_pcfield.setText(_pythoncmd);
				_pathfield.setText(_path);
				
				_frame.revalidate();
				_frame.repaint();
			}
		});


		leftpanel.add(predl);
		leftpanel.add(efl);
		leftpanel.add(ef);
		leftpanel.add(ppl);
		leftpanel.add(pp);
		leftpanel.add(pel);
		leftpanel.add(pe);
		
		JSeparator js = new JSeparator(JSeparator.HORIZONTAL);
		js.setMaximumSize(new Dimension(1000, 15));
		leftpanel.add(js);
		leftpanel.add(trainl);
		leftpanel.add(af);
		leftpanel.add(tm);
		JSeparator js2 = new JSeparator(JSeparator.HORIZONTAL);
		js.setMaximumSize(new Dimension(1000, 15));
		leftpanel.add(js2);
		leftpanel.add(config);
		
		return rvpanel;
	}
	
	private JPanel getPromoterPredictionPanel(){
		final JPanel comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.PAGE_AXIS));
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		_promoterfeaturefile = new JTextField();
		_util.setFieldPanel(comp, _fc, new JLabel("Feature File:"), _promoterfeaturefile, new JButton("..."), _featurefilter, false);
		
		
		final JRadioButton modelrb = new JRadioButton();
		final JRadioButton tssrb = new JRadioButton();
		
		JPanel modelrbp = new JPanel(new BorderLayout());
		modelrbp.add(modelrb, BorderLayout.NORTH);
		JPanel tssrbp = new JPanel(new BorderLayout());
		tssrbp.add(tssrb, BorderLayout.NORTH);

		ButtonGroup bg = new ButtonGroup();
		bg.add(modelrb);
		bg.add(tssrb);
		
		JPanel modelpanel = new JPanel();
		modelpanel.setLayout(new BorderLayout());
		modelpanel.add(modelrbp, BorderLayout.WEST);
		modelpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JPanel mpoptions = new JPanel();
		mpoptions.setLayout(new BorderLayout());
		JLabel ml = new JLabel("Predict promoters using ML model");
		ml.setFont(_labelfont);
		ml.setHorizontalAlignment(JLabel.LEFT);
		mpoptions.add(ml, BorderLayout.NORTH);
		final JTextField pmfield = _pmf;
		pmfield.setColumns(12);
		final JButton pmbutton =  new JButton("...");
		_util.setFieldPanel(mpoptions, _fc, new JLabel("Promoter Model:"), pmfield, pmbutton, _modelfilter, false);
		final TrainModelOptions tm = _ptm;
		mpoptions.add(tm, BorderLayout.SOUTH);
		modelpanel.add(mpoptions, BorderLayout.CENTER);

		
		
		JPanel tsspanel = new JPanel();
		tsspanel.setLayout(new BorderLayout());
		tsspanel.add(tssrbp, BorderLayout.WEST);
		tsspanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JPanel tssoptions = new JPanel();
		tssoptions.setLayout(new BorderLayout());
		JLabel tssl = new JLabel("Predict promoters using distance to TSS");
		tssl.setFont(_labelfont);
		tssl.setHorizontalAlignment(JLabel.LEFT);
		tssoptions.add(tssl, BorderLayout.NORTH);
		
		final JTextField tsscolumn = new JTextField("24");
		tsscolumn.setColumns(3);
		final JTextField upfield = new JTextField("2000");
		final JTextField downfield = new JTextField("2000");
		
		tssoptions.add(_util.getHorizontalField(new JLabel("Distance to TSS Column:"), tsscolumn), BorderLayout.CENTER);
		tssoptions.add(_util.getHorizontalField( new JLabel("Upstream:"), upfield, new JLabel("Downstream:"), downfield), BorderLayout.SOUTH);
		tsspanel.add(tssoptions, BorderLayout.CENTER);
		
		
		ChangeListener cl = new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				boolean ms = modelrb.isSelected();
				boolean ts = tssrb.isSelected();
				pmfield.setEnabled(ms);
				pmbutton.setEnabled(ms);
				tm.setEnabled(ms);
				upfield.setEnabled(ts);
				downfield.setEnabled(ts);
				tsscolumn.setEnabled(ts);
			}
		};
		modelrb.setSelected(true);
		cl.stateChanged(null);
		modelrb.addChangeListener(cl);
		tssrb.addChangeListener(cl);
		
		JPanel combinedpanel = new JPanel();
		combinedpanel.setLayout(new BoxLayout(combinedpanel, BoxLayout.Y_AXIS));
		final JTextField outdirfield = new JTextField();
		_util.setFieldPanel(combinedpanel, _fc, new JLabel("Output Directory:"), outdirfield, new JButton("..."), null, true);
		combinedpanel.add(modelpanel);
		combinedpanel.add(tsspanel);
		MoreOptionsPanel motm = new MoreOptionsPanel(combinedpanel); 
		comp.add(motm);
		
		
		JButton cmdb = new JButton("Get Command");
		cmdb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = "";
				boolean error = false;
				String errormessages = "";
				if(modelrb.isSelected()){
					ModelPredictorValidator pmv = new ModelPredictorValidator(_pythoncmd, _path, _promoterfeaturefile.getText(), outdirfield.getText(), pmfield.getText(), _ptm.getFeatureIndexFileField(), _ptm.getLabelEncoderField(), "", false, "promoter");
					error = pmv.hasError();
					errormessages = pmv.getErrorMessage();
					command = pmv.getCommand();
				}
				else if(tssrb.isSelected()){
					TSSPromoterValidator tsspv = new TSSPromoterValidator(_pythoncmd, _path, _promoterfeaturefile.getText(), outdirfield.getText(), tsscolumn.getText(), upfield.getText(), downfield.getText());
					error = tsspv.hasError();
					errormessages = tsspv.getErrorMessage();
					command = tsspv.getCommand();
				}
				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(_frame, new CommandPanel(_labelfont, command),"Predict Promoter Command",JOptionPane.PLAIN_MESSAGE);
				}
			}
			
		});
		
		
		JButton ppb = new JButton("Predict Promoters");
		
		final CommandRunner ppcr = new CommandRunner(_frame, _labelfont, "Promoter Prediction");
		
		ppb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean error = false;
				String errormessages = "";
				String[] cmd = new String[0];
				Function postfunction = null;
				String predictionfile = "";
				
				if(modelrb.isSelected()){
					ModelPredictorValidator pmv = new ModelPredictorValidator(_pythoncmd, _path, _promoterfeaturefile.getText(), outdirfield.getText(), pmfield.getText(), _ptm.getFeatureIndexFileField(), _ptm.getLabelEncoderField(), "", false, "promoter");
					error = pmv.hasError();
					errormessages = pmv.getErrorMessage();
					
					if(!error){
						cmd = pmv.getCommandArray();
						FeaturePrediction[] fp = pmv.getFeaturePredictions();
						PredictionAnnotator[] pa = new PredictionAnnotator[fp.length];
						for(int i = 0; i < fp.length; i++){
							pa[i] = new PredictionAnnotator(_pythoncmd, _path,fp[i].getFeatureFile(), fp[i].getPredictionFile());
						}
						
						postfunction = new MultiPredictionAnnotator(pa);
						predictionfile = ((MultiPredictionAnnotator)postfunction).getPredictionFile(pmv.getOutputDirectory(), "promoter");
					}
				}
				else if(tssrb.isSelected()){
					TSSPromoterValidator tsspv = new TSSPromoterValidator(_pythoncmd, _path, _promoterfeaturefile.getText(), outdirfield.getText(), tsscolumn.getText(), upfield.getText(), downfield.getText());
					error = tsspv.hasError();
					errormessages = tsspv.getErrorMessage();
					cmd = tsspv.getCommandArray();
					predictionfile = tsspv.getPredictionFile();
				}
				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					ppcr.setPostCommandFunction(postfunction);
					ppcr.run(cmd);
					_enhancerfeaturefile.setText(predictionfile);
				}

			}
			
		});
		comp.add(_util.getHorizontalField(cmdb, ppb));
		return comp;
	}
	
	
	
	private JPanel getEnhancerPredictionPanel(){
		final JPanel comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		_enhancerfeaturefile = new JTextField();
		_util.setFieldPanel(comp, _fc, new JLabel("Feature File:"), _enhancerfeaturefile, new JButton("..."), _featurefilter, false);
		
		JPanel notepanel = new JPanel();
		JLabel note = new JLabel("Inlcude all peaks with promoters annotated.");
		note.setFont(new Font("Arial", Font.ITALIC, 10));
		notepanel.setLayout(new BorderLayout());
		notepanel.add(note, BorderLayout.CENTER);
		comp.add(notepanel);
		

		JPanel combinedpanel = new JPanel();
		combinedpanel.setLayout(new BoxLayout(combinedpanel, BoxLayout.Y_AXIS));
		final JTextField outdirfield = new JTextField();
		_util.setFieldPanel(combinedpanel, _fc, new JLabel("Output Directory:"), outdirfield, new JButton("..."), null, true);
		final JTextField emfield = _emf;
		_util.setFieldPanel(combinedpanel, _fc, new JLabel("Enhancer Model:"), emfield, new JButton("..."), _modelfilter, false);
		final TrainModelOptions tm = _etm;
		combinedpanel.add(tm);
		
		_util.setFieldPanel(combinedpanel, _fc, new JLabel("Class File:"), _eclassfield, new JButton("..."), null, false);

		final JCheckBox evalbox = new JCheckBox();
		JPanel evalpanel = new JPanel();
		JLabel evalnote = new JLabel("Features must have enhancer annotations to evaluate predictions.");
		evalnote.setFont(new Font("Arial", Font.ITALIC, 10));
		evalpanel.setLayout(new BorderLayout());
		evalpanel.add(evalnote, BorderLayout.SOUTH);
		evalpanel.add(_util.getHorizontalField(new JLabel("Evaluate Predictions:"), evalbox), BorderLayout.CENTER);
		combinedpanel.add(evalpanel);

		MoreOptionsPanel motm = new MoreOptionsPanel(combinedpanel); 
		
		comp.add(motm);
		
		JButton cmdb = new JButton("Get Command");
		cmdb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = "";
				boolean error = false;
				String errormessages = "";
				ModelPredictorValidator pmv = new ModelPredictorValidator(_pythoncmd, _path, _enhancerfeaturefile.getText(), outdirfield.getText(), emfield.getText(), _ptm.getFeatureIndexFileField(), _ptm.getLabelEncoderField(), _eclassfield.getText(), evalbox.isSelected(), "enhancer");
				error = pmv.hasError();
				errormessages = pmv.getErrorMessage();
				command = pmv.getCommand();

				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(_frame, new CommandPanel(_labelfont, command),"Predict Enhancer Command",JOptionPane.PLAIN_MESSAGE);
				}
			}
			
		});
		
		
		JButton peb = new JButton("Predict Enhancers");
		
		final CommandRunner pecr = new CommandRunner(_frame, _labelfont, "Enhancer Prediction");
		
		peb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean error = false;
				String errormessages = "";
				String[] cmd = new String[0];
				Function postfunction = null;
				
				ModelPredictorValidator emv = new ModelPredictorValidator(_pythoncmd, _path, _enhancerfeaturefile.getText(), outdirfield.getText(), emfield.getText(), _ptm.getFeatureIndexFileField(), _ptm.getLabelEncoderField(), _eclassfield.getText(), evalbox.isSelected(), "enhancer");
				error = emv.hasError();
				errormessages = emv.getErrorMessage();
				
				if(!error){
					cmd = emv.getCommandArray();
					FeaturePrediction[] fp = emv.getFeaturePredictions();
					PredictionAnnotator[] pa = new PredictionAnnotator[fp.length];
					for(int i = 0; i < fp.length; i++){
						pa[i] = new PredictionAnnotator(_pythoncmd, _path,fp[i].getFeatureFile(), fp[i].getPredictionFile());
					}
					postfunction = new PredictionInformer(emv.getFeaturePredictions());
				}
				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					pecr.setPostCommandFunction(postfunction);
					pecr.run(cmd);
				}

			}
			
		});
		
		comp.add(_util.getHorizontalField(cmdb,peb));
		return comp;
	}
	
	
	private JPanel getFeatureAnnotationPanel(){
		final JPanel comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		final JTextField featurefile = new JTextField();
		final JTextField annotationfile = new JTextField();

		
		_util.setFieldPanel(comp, _fc, new JLabel("Feature File:"), featurefile, new JButton("..."), _featurefilter,false);
		_util.setFieldPanel(comp, _fc, new JLabel("Annotation File:"), annotationfile, new JButton("..."), null, false);

		JButton cmdb = new JButton("Get Command");
		cmdb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean error = false;
				String errormessages = "";
				
				AnnotateValidator av = new AnnotateValidator(_path, featurefile.getText(), annotationfile.getText());
				error = av.hasError();
				errormessages = av.getErrorMessage();
				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(_frame, new CommandPanel(_labelfont, av.getCommand()),"Annotate Features Command",JOptionPane.PLAIN_MESSAGE);
				}
				
			}
			
		});
		
		final CommandRunner afcr = new CommandRunner(_frame, _labelfont, "Annotate Features");
		
		JButton afb = new JButton("Annotate Features");
		afb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				boolean error = false;
				String errormessages = "";
				String[] cmd = new String[0];
				Function postfunction = null;
				
				AnnotateValidator av = new AnnotateValidator(_path, featurefile.getText(), annotationfile.getText());
				error = av.hasError();
				errormessages = av.getErrorMessage();
				cmd = av.getCommandArray();
				postfunction = new AnnotationInformer(av.getOutputFile());
				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					afcr.setPostCommandFunction(postfunction);
					afcr.run(cmd);
				}
			}
			
		});
		
		
		comp.add(_util.getHorizontalField(cmdb, afb));
		return comp;
	}
	
	private JPanel getConfigPanel(){
		final JPanel comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		comp.add(_util.getHorizontalField(new JLabel("PEAS Path:"), _pathfield));
		comp.add(_util.getHorizontalField(new JLabel("Python Command:"), _pcfield));
		
		JButton setb = new JButton("Set Configuration");
		setb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				_pythoncmd = _pcfield.getText();
				
				updatePath(_pathfield.getText());
			}
		});
		comp.add(_util.getHorizontalField(setb));
		
		return comp;
	}
	
	private JPanel getFeatureExtractionPanel(){
		final JPanel comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JTextField bamfield = new JTextField();
		final JTextField outfield = new JTextField();
		final JTextField fastafield = new JTextField();

		
		_util.setFieldPanel(comp, _fc, new JLabel("BAM File:"), bamfield, new JButton("..."), _bamfilter, false);
		_util.setFieldPanel(comp, _fc, new JLabel("Output Directory:"), outfield, new JButton("..."), null, true);
		_util.setFieldPanel(comp, _fc, new JLabel("FASTA File:"), fastafield, new JButton("..."), _fastafilter, false);

		final ExtractFeaturesOptions efo = _efo;
		MoreOptionsPanel mop = new MoreOptionsPanel(efo);
		comp.add(mop);
		
		
		
		JButton cmdb = new JButton("Get Command");
		cmdb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean error = false;
				String errormessages = "";
				
				ExtractFeaturesValidator efv = new ExtractFeaturesValidator(_path, bamfield.getText(), outfield.getText(), fastafield.getText(), efo.getPeakField(), efo.getKnownMotifField(), efo.getConservationField(), efo.getCTCFField());
				error = efv.hasError();
				errormessages = efv.getErrorMessage();
				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(_frame, new CommandPanel(_labelfont, efv.getCommand()),"Feature Extraction Command",JOptionPane.PLAIN_MESSAGE);
				}
			}
			
		});
		
		
		JButton ef = new JButton("Extract Features");
		
		final CommandRunner efcr = new CommandRunner(_frame, _labelfont, "Feature Extraction");
		
		ef.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean error = false;
				String errormessages = "";
				
				ExtractFeaturesValidator efv = new ExtractFeaturesValidator(_path, bamfield.getText(), outfield.getText(), fastafield.getText(), efo.getPeakField(), efo.getKnownMotifField(), efo.getConservationField(), efo.getCTCFField());
				error = efv.hasError();
				errormessages = efv.getErrorMessage();
				
				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					efcr.run(efv.getCommandArray());
					_promoterfeaturefile.setText(efv.getFeatureFile());
				}

			}
			
		});
		
		comp.add(_util.getHorizontalField(cmdb, ef));
		return comp;
	}

	
	private JPanel getTrainModelPanel(){
		final JPanel comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final JTextField modelfield = new JTextField();
		
		comp.add(_util.getHorizontalField(new JLabel("Model Name:"), modelfield));
		
		DefaultListModel<ListFile> lm = new DefaultListModel<ListFile>();

		final JList<ListFile> list = new JList<ListFile>(lm);
		list.setVisibleRowCount(5);

		list.revalidate();
		list.repaint();

		
		comp.add(getAnnotatedFeaturesPanel(list, lm));

		JPanel combinedpanel = new JPanel();
		combinedpanel.setLayout(new BoxLayout(combinedpanel, BoxLayout.Y_AXIS));
		
		final JTextField outdirfield = new JTextField();
		_util.setFieldPanel(comp, _fc, new JLabel("Output Directory:"), outdirfield, new JButton("..."), null, true);
		final JTextField pfield = new JTextField();
		combinedpanel.add(_util.getHorizontalField(new JLabel("Model Parameters:"), pfield));
		JPanel notepanel = new JPanel();
		JLabel note = new JLabel("parameter1=value1, parameter2=value2, ...");
		note.setFont(new Font("Arial", Font.ITALIC, 10));
		notepanel.setLayout(new BorderLayout());
		notepanel.add(note, BorderLayout.CENTER);
		combinedpanel.add(notepanel);

		final TrainModelOptions tm = _tm;
		combinedpanel.add(tm);
		
		_util.setFieldPanel(combinedpanel, _fc, new JLabel("Class File:"), _tclassfield, new JButton("..."), null, false);
		
		final JTextField rsfield = new JTextField("929");
		combinedpanel.add(_util.getHorizontalField(new JLabel("Random State:"), rsfield));
		
		MoreOptionsPanel motm = new MoreOptionsPanel(combinedpanel); 
		
		comp.add(motm);
		
		JButton cmdb = new JButton("Get Command");
		cmdb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = "";
				boolean error = false;
				String errormessages = "";
				
				ListModel<ListFile> model = list.getModel();
				ListFile[] l = new ListFile[model.getSize()];
				for(int i = 0; i < l.length; i++){
					l[i] = model.getElementAt(i);
				}
				
				ModelTrainerValidator mtv = new ModelTrainerValidator(_pythoncmd, _path, modelfield.getText(), l, outdirfield.getText(), pfield.getText(), _tm.getFeatureIndexFileField(), _tclassfield.getText(), _tm.getLabelEncoderField(), rsfield.getText());
				error = mtv.hasError();
				errormessages = mtv.getErrorMessage();
				command = mtv.getCommand();

				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(_frame, new CommandPanel(_labelfont, command),"Model Trainer Command",JOptionPane.PLAIN_MESSAGE);
				}
			}
			
		});
		
		
		JButton tmb = new JButton("Train Model");
		
		final CommandRunner tmcr = new CommandRunner(_frame, _labelfont, "Train Model");
		
		tmb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean error = false;
				String errormessages = "";
				String[] cmd = new String[0];
				
				ListModel<ListFile> model = list.getModel();
				ListFile[] l = new ListFile[model.getSize()];
				for(int i = 0; i < l.length; i++){
					l[i] = model.getElementAt(i);
				}
				
				ModelTrainerValidator mtv = new ModelTrainerValidator(_pythoncmd, _path, modelfield.getText(), l, outdirfield.getText(), pfield.getText(), _tm.getFeatureIndexFileField(), _tclassfield.getText(), _tm.getLabelEncoderField(), rsfield.getText());
				error = mtv.hasError();
				errormessages = mtv.getErrorMessage();
				cmd = mtv.getCommandArray();
				Function postfunction = new TrainModelInformer(mtv.getModelFile());

				if(error){
					JOptionPane.showMessageDialog(_frame, errormessages.toString(),"Input Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
					tmcr.setPostCommandFunction(postfunction);
					tmcr.run(cmd);
				}

			}
			
		});
		comp.add(_util.getHorizontalField(cmdb,tmb));
		return comp;
	}
	
	
	
	private JPanel getAnnotatedFeaturesPanel(final JList<ListFile> list, final DefaultListModel<ListFile> lm){
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setBorder(new EmptyBorder(0, 0, 0, 0));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JScrollPane lsp = new JScrollPane(list);
		lsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		lsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		lsp.setFocusable(false);
		lsp.setPreferredSize(new Dimension(180, 80));
		JPanel buttonpanel = new JPanel();
		buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.Y_AXIS));
		JButton add = new JButton("Add");
		final int MAXPATHLENGTH = 20;
		add.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				_fc.resetChoosableFileFilters();
				_fc.setFileFilter(_featurefilter);
				_fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				_fc.setMultiSelectionEnabled(true);
				int result = _fc.showOpenDialog(_frame);
				if(result == JFileChooser.APPROVE_OPTION){
					//filetext.setText(fc.getSelectedFile().getAbsolutePath());
					File[] f = _fc.getSelectedFiles();
					for(int i = 0; i < f.length; i++){
						String curfile = f[i].getAbsolutePath();
						String name = curfile;
						int l = name.length();
						if(l > MAXPATHLENGTH){
							name = "..."+name.substring(l-MAXPATHLENGTH, l);
						}
						ListFile newlf = new ListFile(name, curfile);
						if(!lm.contains(newlf)){
							lm.addElement(newlf);
						}
					}
				}
			}
			
		});
		
		JButton remove = new JButton("Remove");
		remove.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				List<ListFile> sv = list.getSelectedValuesList();
				for(Iterator<ListFile> it = sv.iterator(); it.hasNext();){
					lm.removeElement(it.next());
				}
			}
			
		});
		
		buttonpanel.add(add);
		buttonpanel.add(remove);
		
		return _util.getHorizontalField(new JLabel("Feature Files:"), lsp, buttonpanel);
	}
	
	private void updatePath(String newpath){
		newpath = newpath.trim();
		if(!newpath.endsWith("/")){
			newpath = newpath+"/";
		}
		_path = newpath;
		_tm.setPath(_path);
		_etm.setPath(_path);
		_ptm.setPath(_path);
		_efo.setPath(_path);
		_emf.setText(_path+"models/enhancermodel.pkl");
		_pmf.setText(_path+"models/promotermodel.pkl");
		_tclassfield.setText(_path+"models/classes.txt");
		_eclassfield.setText(_path+"models/classes.txt");

	}
	

}
