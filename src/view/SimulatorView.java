package view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import models.Server;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import controller.Simulator;
import javax.swing.JSeparator;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;

public class SimulatorView {

	private JFrame frame;
	private static SimulatorView instance;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	private JTextField serverBroadcast;
	public JTextField getServerBroadcast() {
		return serverBroadcast;
	}

	public void setServerBroadcast(JTextField serverBroadcast) {
		this.serverBroadcast = serverBroadcast;
	}

	public JTextField getRouteBroadcast() {
		return routeBroadcast;
	}

	public void setRouteBroadcast(JTextField routeBroadcast) {
		this.routeBroadcast = routeBroadcast;
	}

	public JTextField getReceiverBroadcast() {
		return receiverBroadcast;
	}

	public void setReceiverBroadcast(JTextField receiverBroadcast) {
		this.receiverBroadcast = receiverBroadcast;
	}

	private JTextField routeBroadcast;
	private JTextField receiverBroadcast;
	private JTextField simulationTimeTextField;
	private JTextField serverCI;
	private JTextField routerCI;
	private JTextField receiverCI;
	private JTextField numOfRuns;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField transientTime;
	
	public static SimulatorView getInstance() {
		if (instance == null) {
			instance = new SimulatorView();
		}
		return instance;
	}

	/**
	 * Launch the application.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		SimulatorView.getInstance();
	}

	/**
	 * Create the application.
	 */
	private SimulatorView() {
		initialize();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1223, 727);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		XYSeriesCollection dataset = new XYSeriesCollection();				
		chart = ChartFactory.createXYLineChart(
				 "Gráfico do simulador", // Title
				 "Tempo", // x-axis Label
				 "Valor", // y-axis Label
				 dataset, // Dataset
				 PlotOrientation.VERTICAL, // Plot Orientation
				 true, // Show Legend
				 true, // Use tooltips
				 false // Configure chart to generate URLs?
				 );
		chartPanel = new ChartPanel(chart);
		
		JPanel panel = new JPanel();
		
		JLabel lblTaxasEmMbps = new JLabel("Taxas em Pacotes por segundo");
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_1.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		JLabel lblNewLabel_6 = new JLabel("Grupo 2");
		
		JSeparator separator = new JSeparator();
		
		JPanel panel_3 = new JPanel();
		panel_3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel label = new JLabel("Taxa saída servidor\n");
		panel_3.add(label);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setColumns(10);
		panel_3.add(textField);
		
		JLabel label_1 = new JLabel("Taxa saída do roteador");
		panel_3.add(label_1);
		
		textField_1 = new JTextField();
		textField_1.setEditable(false);
		textField_1.setColumns(10);
		panel_3.add(textField_1);
		
		JLabel label_2 = new JLabel("Taxa chegada do servidor");
		panel_3.add(label_2);
		
		textField_2 = new JTextField();
		textField_2.setEditable(false);
		textField_2.setColumns(10);
		panel_3.add(textField_2);
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		
		JLabel label_3 = new JLabel("IC servidor:");
		panel_4.add(label_3);
		
		textField_3 = new JTextField();
		textField_3.setEditable(false);
		textField_3.setColumns(20);
		panel_4.add(textField_3);
		
		JLabel label_4 = new JLabel("IC roteador:");
		panel_4.add(label_4);
		
		textField_4 = new JTextField();
		textField_4.setEditable(false);
		textField_4.setColumns(20);
		panel_4.add(textField_4);
		
		JLabel lblIcReceptor = new JLabel("IC receptor:");
		panel_4.add(lblIcReceptor);
		
		textField_5 = new JTextField();
		textField_5.setEditable(false);
		textField_5.setColumns(20);
		panel_4.add(textField_5);
		
		JLabel lblGrupo = new JLabel("Grupo 1");
		
		JSeparator separator_2 = new JSeparator();
		
		JSeparator separator_1 = new JSeparator();
		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(25)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
							.addGroup(groupLayout.createSequentialGroup()
								.addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE)
								.addGap(202)
								.addComponent(lblTaxasEmMbps)
								.addGap(492))
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblGrupo)
									.addGap(18)
									.addComponent(separator_2, GroupLayout.PREFERRED_SIZE, 1084, GroupLayout.PREFERRED_SIZE))
								.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 1174, Short.MAX_VALUE)
								.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblNewLabel_6)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(panel_2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE)
										.addComponent(panel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 1100, GroupLayout.PREFERRED_SIZE))
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(panel_4, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(panel_3, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 1100, GroupLayout.PREFERRED_SIZE)))
								.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 1084, GroupLayout.PREFERRED_SIZE))))
					.addGap(98))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chartPanel, GroupLayout.PREFERRED_SIZE, 307, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblTaxasEmMbps)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(13)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblGrupo)
						.addComponent(separator_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(136)
							.addComponent(lblNewLabel_6))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(10)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
					.addGap(32))
		);
		
		JLabel lblNewLabel_3 = new JLabel("IC servidor:");
		panel_2.add(lblNewLabel_3);
		
		serverCI = new JTextField();
		serverCI.setEditable(false);
		panel_2.add(serverCI);
		serverCI.setColumns(20);
		
		JLabel lblNewLabel_4 = new JLabel("IC roteador:");
		panel_2.add(lblNewLabel_4);
		
		routerCI = new JTextField();
		routerCI.setEditable(false);
		panel_2.add(routerCI);
		routerCI.setColumns(20);
		
		JLabel lblNewLabel_5 = new JLabel("IC receptor:");
		panel_2.add(lblNewLabel_5);
		
		receiverCI = new JTextField();
		receiverCI.setEditable(false);
		panel_2.add(receiverCI);
		receiverCI.setColumns(20);
		
		JLabel simulationTimeLabel = new JLabel("Tempo de simulação(ms)");
		panel_1.add(simulationTimeLabel);
		
		simulationTimeTextField = new JTextField();
		simulationTimeTextField.setText("10000");
		panel_1.add(simulationTimeTextField);
		simulationTimeTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Iniciar");
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearChart();
				try {
					Simulator.startSimulator();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JLabel lblTempoAtEstacionar = new JLabel("Tempo até estacionar");
		panel_1.add(lblTempoAtEstacionar);
		
		transientTime = new JTextField();
		transientTime.setText("1000");
		transientTime.setColumns(10);
		panel_1.add(transientTime);
		
		panel_1.add(btnNewButton);
		
		JLabel lblNmeroDeVezes = new JLabel("Número de rodadas:");
		panel_1.add(lblNmeroDeVezes);
		
		numOfRuns = new JTextField();
		numOfRuns.setEditable(false);
		panel_1.add(numOfRuns);
		numOfRuns.setColumns(10);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel lblNewLabel = new JLabel("Taxa saída servidor\n");
		panel.add(lblNewLabel);
		
		serverBroadcast = new JTextField();
		serverBroadcast.setEditable(false);
		panel.add(serverBroadcast);
		serverBroadcast.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Taxa saída do roteador");
		panel.add(lblNewLabel_1);
		
		routeBroadcast = new JTextField();
		routeBroadcast.setEditable(false);
		panel.add(routeBroadcast);
		routeBroadcast.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Taxa chegada do servidor");
		panel.add(lblNewLabel_2);
		
		receiverBroadcast = new JTextField();
		receiverBroadcast.setEditable(false);
		panel.add(receiverBroadcast);
		receiverBroadcast.setColumns(10);
		frame.getContentPane().setLayout(groupLayout);
	}
	
	public JTextField getTransientTime() {
		return transientTime;
	}

	public void setTransientTime(JTextField transientTime) {
		this.transientTime = transientTime;
	}

	public JTextField getServerCI() {
		return serverCI;
	}

	public void setServerCI(JTextField serverCI) {
		this.serverCI = serverCI;
	}

	public JTextField getRouterCI() {
		return routerCI;
	}

	public void setRouterCI(JTextField routerCI) {
		this.routerCI = routerCI;
	}

	public JTextField getReceiverCI() {
		return receiverCI;
	}

	public void setReceiverCI(JTextField receiverCI) {
		this.receiverCI = receiverCI;
	}

	public JTextField getNumOfRuns() {
		return numOfRuns;
	}

	public void setNumOfRuns(JTextField numOfRuns) {
		this.numOfRuns = numOfRuns;
	}

	protected void clearChart() {
		JFreeChart chart = chartPanel.getChart();
		XYPlot plot = (XYPlot) chart.getPlot();
		XYSeriesCollection dataset = new XYSeriesCollection();
		plot.setDataset(dataset);
	}

	public void updateChart(HashMap<Server, HashMap<Float, Integer>> series) {
		JFreeChart chart = chartPanel.getChart();
		XYPlot plot = (XYPlot) chart.getPlot();
		
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		for (Entry<Server, HashMap<Float, Integer>> entry : series.entrySet()) {
			XYSeries serie = new XYSeries("txwnd/MSS " + entry.getKey());
			for (Entry<Float, Integer> values : entry.getValue().entrySet()) {
				serie.add(values.getKey(), values.getValue());
			}
			dataset.addSeries(serie);
		}		
	}
	
	public JTextField getSimulationTimeTextField() {
		return simulationTimeTextField;
	}

	public void setSimulationTimeTextField(JTextField simulationTimeTextField) {
		this.simulationTimeTextField = simulationTimeTextField;
	}
}
