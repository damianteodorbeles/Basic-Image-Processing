import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import ro.damianteodorbeles.imageprocessing.ColorFilter;
import ro.damianteodorbeles.imageprocessing.FilterEngine;
import ro.damianteodorbeles.imageprocessing.KernelFactory;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JTextField txfImagePath;

	private BufferedImage image;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("Basic Image Processing by Damian-Teodor BELES");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 624, 392);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnApplyFilter = new JButton("Apply filter");
		JButton btnBrowse = new JButton("Browse...");
		JComboBox<String> cmbFilter = new JComboBox<String>();
		JPanel panelFilter = new JPanel();
		JPanel panelProcessedImage = new JPanel();

		btnApplyFilter.setBounds(10, 51, 207, 23);
		btnApplyFilter.setEnabled(false);

		btnBrowse.setBounds(509, 6, 89, 23);

		cmbFilter.setEnabled(false);
		cmbFilter
				.setModel(new DefaultComboBoxModel<String>(new String[] { "BOX BLUR", "GAUSSIAN BLUR", "EDGE DETECTION",
						"SHARPEN", "EMBOSS", "RED COLORING", "GREEN COLORING", "BLUE COLORING", "GRAYSCALE" }));
		cmbFilter.setBounds(10, 20, 207, 20);

		panelFilter.setBorder(BorderFactory.createTitledBorder("Filter"));
		panelFilter.setBounds(10, 36, 227, 83);
		panelFilter.setLayout(null);

		panelProcessedImage.setBounds(247, 36, 351, 306);

		txfImagePath = new JTextField();
		txfImagePath.setHorizontalAlignment(SwingConstants.CENTER);
		txfImagePath.setEditable(false);
		txfImagePath.setBounds(10, 8, 489, 20);
		txfImagePath.setColumns(10);

		btnApplyFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BufferedImage processedImage = null;
				FilterEngine filterEngine = new FilterEngine();
				switch (cmbFilter.getSelectedItem().toString()) {
				case "BOX BLUR":
					processedImage = filterEngine.applyKernel(image, new KernelFactory().BOX_BLUR());
					break;
				case "GAUSSIAN BLUR":
					processedImage = filterEngine.applyKernel(image, new KernelFactory().GAUSSIAN_BLUR());
					break;
				case "EDGE DETECTION":
					processedImage = filterEngine.applyKernel(image, new KernelFactory().EDGE_DETECTION());
					break;
				case "SHARPEN":
					processedImage = filterEngine.applyKernel(image, new KernelFactory().SHARPEN());
					break;
				case "EMBOSS":
					processedImage = filterEngine.applyKernel(image, new KernelFactory().EMBOSS());
					break;
				case "RED COLORING":
					processedImage = filterEngine.setColorFilter(image, ColorFilter.Red);
					break;
				case "GREEN COLORING":
					processedImage = filterEngine.setColorFilter(image, ColorFilter.Green);
					break;
				case "BLUE COLORING":
					processedImage = filterEngine.setColorFilter(image, ColorFilter.Blue);
					break;
				case "GRAYSCALE":
					processedImage = filterEngine.grayscale(image);
					break;
				}
				panelProcessedImage.getGraphics()
						.drawImage(processedImage.getScaledInstance(panelProcessedImage.getWidth(),
								panelProcessedImage.getHeight(), BufferedImage.SCALE_SMOOTH), 0, 0, null);
			}
		});

		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(
						new FileNameExtensionFilter("Image Files (*.jpg) | (*.gif) | (*.png)", "jpg", "gif", "png"));

				final int dialogResult = fileChooser.showOpenDialog(null);
				if (dialogResult == JFileChooser.APPROVE_OPTION) {
					try {
						File selectedFile = fileChooser.getSelectedFile().getAbsoluteFile();
						image = ImageIO.read(selectedFile);
						txfImagePath.setText(selectedFile.getAbsolutePath());
						btnApplyFilter.setEnabled(true);
						cmbFilter.setEnabled(true);
					} catch (IOException exception) {
						JOptionPane.showMessageDialog(contentPane, "Couldn't read the file containing the image!");
					}
				} else {
					JOptionPane.showMessageDialog(contentPane, "Choose an image in order to proceed.");
				}
			}
		});

		panelFilter.add(btnApplyFilter);
		panelFilter.add(cmbFilter);

		contentPane.add(btnBrowse);
		contentPane.add(txfImagePath);
		contentPane.add(panelFilter);
		contentPane.add(panelProcessedImage);
	}
}
