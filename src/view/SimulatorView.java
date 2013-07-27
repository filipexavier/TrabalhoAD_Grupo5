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


	private JTextField textField_1;
	
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
		SimulatorView window = SimulatorView.getInstance();
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
		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(lblTaxasEmMbps)
							.addGap(422))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(panel, GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 859, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chartPanel, GroupLayout.PREFERRED_SIZE, 307, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblTaxasEmMbps)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(135, Short.MAX_VALUE))
		);
		
		JLabel simulationTimeLabel = new JLabel("Tempo de simulação(ms)(0 para infinito)");
		panel_1.add(simulationTimeLabel);
		
		simulationTimeTextField = new JTextField();
		panel_1.add(simulationTimeTextField);
		simulationTimeTextField.setColumns(10);
		
		JLabel lblIntervaloDeConfiana = new JLabel("Intervalo de confiança");
		panel_1.add(lblIntervaloDeConfiana);
		
		textField_1 = new JTextField();
		panel_1.add(textField_1);
		textField_1.setColumns(10);
		
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
