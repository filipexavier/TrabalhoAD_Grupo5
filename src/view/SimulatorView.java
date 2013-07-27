package view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import controller.Simulator;

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
		frame.setBounds(100, 100, 1224, 560);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series = new XYSeries("txwnd/MSS");
		
		dataset.addSeries(series);
		
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
		
		JPanel panel_2 = new JPanel();
		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
							.addGroup(groupLayout.createSequentialGroup()
								.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
								.addContainerGap())
							.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
									.addComponent(panel_2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
									.addComponent(panel, GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE))
								.addContainerGap()))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(lblTaxasEmMbps)
							.addGap(492))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 714, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chartPanel, GroupLayout.PREFERRED_SIZE, 307, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblTaxasEmMbps)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(62, Short.MAX_VALUE))
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
		
		JLabel lblNewLabel_5 = new JLabel("IC recepitor:");
		panel_2.add(lblNewLabel_5);
		
		receiverCI = new JTextField();
		receiverCI.setEditable(false);
		panel_2.add(receiverCI);
		receiverCI.setColumns(20);
		
		JLabel simulationTimeLabel = new JLabel("Tempo de simulação(ms)");
		panel_1.add(simulationTimeLabel);
		
		simulationTimeTextField = new JTextField();
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
		
		panel_1.add(btnNewButton);
		
		JLabel lblNmeroDeVezes = new JLabel("Número de rodadas:");
		panel_1.add(lblNmeroDeVezes);
		
		numOfRuns = new JTextField();
		numOfRuns.setEditable(false);
		panel_1.add(numOfRuns);
		numOfRuns.setColumns(10);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
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
		XYSeries series = new XYSeries("txwnd/MSS");
		dataset.addSeries(series);
		plot.setDataset(dataset);
	}

	public void updateChart(Integer value, Float time) {
		JFreeChart chart = chartPanel.getChart();
		XYPlot plot = (XYPlot) chart.getPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		dataset.getSeries(0).add(time, value);
	}
	
	public JTextField getSimulationTimeTextField() {
		return simulationTimeTextField;
	}

	public void setSimulationTimeTextField(JTextField simulationTimeTextField) {
		this.simulationTimeTextField = simulationTimeTextField;
	}
}
