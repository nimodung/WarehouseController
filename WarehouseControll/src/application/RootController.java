package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class RootController implements Initializable {

	private Stage primaryStage;
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	@FXML
	private Button btn_room1_in, btn_room1_out, btn_room2_in, 
					btn_room2_out, btn_room3_in, btn_room3_out,
					btn_room1_modify;

	@FXML
	private Label lb_room1_desire, lb_room2_desire, lb_room3_desire, 
					lb_room1_current, lb_room2_current, lb_room3_current,
					lb_room1_product_name, lb_room2_product_name, lb_room3_product_name,
					lb_room1_product_amount, lb_room2_product_amount, lb_room3_product_amount;
	@FXML
	private Slider sld_room1, sld_room2, sld_room3;

	@FXML private TextField tf_room1_in, tf_room2_in, tf_room3_in,
							tf_room1_out, tf_room2_out, tf_room3_out;
	
	static InputStream in;
	static OutputStream out;
	static SerialPort serialPort;

	
	static Label lbRoom1DesireTemp, lbRoom2DesireTemp, lbRoom3DesireTemp,
				lbRoom1CurrentTemp, lbRoom2CurrentTemp, lbRoom3CurrentTemp; //inner class에서 객체 사용하기 위함
	static Label lbRoom1pName, lbRoom2pName, lbRoom3pName,
					lbRoom1pAmount, lbRoom2pAmount, lbRoom3pAmount;
	
	
	static Connection connection = null;
	static Statement st = null;
	ResultSet result = null;
	String url = "jdbc:mysql://localhost?serverTimezone=UTC";
	String loginID = "root";
	String loginPW = "1234";
	String sql = "";
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lbRoom1DesireTemp = lb_room1_desire;
		lbRoom2DesireTemp = lb_room2_desire;
		lbRoom3DesireTemp = lb_room3_desire;
		lbRoom1CurrentTemp = lb_room1_current;
		lbRoom2CurrentTemp = lb_room2_current;
		lbRoom3CurrentTemp = lb_room3_current;
		
		lbRoom1pName = lb_room1_product_name;
		lbRoom2pName = lb_room2_product_name;
		lbRoom3pName = lb_room3_product_name;
		lbRoom1pAmount = lb_room1_product_amount;
		lbRoom2pAmount = lb_room2_product_amount;
		lbRoom3pAmount = lb_room3_product_amount;
		
		try {
			new RootController().DBconnect();
			new RootController().connect("COM7");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		
		sld_room1.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				int value = newValue.intValue();
				String strValue = value + "\n";
				try {
					out.write(("room1_desire : "+strValue).getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	
		lbRoom1DesireTemp.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String strRoom1 = newValue;
				sld_room1.setValue(Integer.parseInt(strRoom1));
			}
		});
		sld_room2.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				int value = newValue.intValue();
				String strValue = value + "\n";
				try {
					out.write(("room2_desire : "+strValue).getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		lbRoom2DesireTemp.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String strRoom2 = newValue;
				sld_room2.setValue(Integer.parseInt(strRoom2));
			}
		});
		sld_room3.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				int value = newValue.intValue();
				String strValue = value + "\n";
				try {
					out.write(("room3_desire : "+strValue).getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		lbRoom3DesireTemp.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String strRoom3 = newValue;
				sld_room3.setValue(Integer.parseInt(strRoom3));
			}
		});
		
		
		
	}
	
	public void StockAmountManagerOnAction(ActionEvent event) {
		try {
			String btnInOutID = ((Button)event.getSource()).getId();
			int pAmount = 0;
			if(btnInOutID.equals("btn_room1_in")) {
				sql = "select pAmount from warehousedb.stock where sNumber = '1';";
				//ResultSet : arr // usertbl의 row들이 들어 있음 // 각 row들이 통채로 들어있음
				
				result = st.executeQuery(sql);
				
				while(result.next()) {
					pAmount = Integer.parseInt(result.getString("pAmount"));
					pAmount += Integer.parseInt(tf_room1_in.getText());
					
				}
				sql = "update warehousedb.stock set pamount = " + pAmount + " where snumber = '1';";
				
				st.executeUpdate(sql);
				
				lbRoom1pAmount.setText(pAmount + "");
				tf_room1_in.clear();
			}
			
			else if(btnInOutID.equals("btn_room1_out")) {
				sql = "select pAmount from warehousedb.stock where sNumber = '1';";
				//ResultSet : arr // usertbl의 row들이 들어 있음 // 각 row들이 통채로 들어있음
				
				result = st.executeQuery(sql);
				
				while(result.next()) {
					pAmount = Integer.parseInt(result.getString("pAmount"));
					pAmount -= Integer.parseInt(tf_room1_out.getText());
				}
				sql = "update warehousedb.stock set pamount = " + pAmount + " where snumber = '1';";
				
				st.executeUpdate(sql);
				
				lbRoom1pAmount.setText(pAmount + "");
				tf_room1_out.clear();
			}
			else if(btnInOutID.equals("btn_room2_in")) {
				sql = "select pAmount from warehousedb.stock where sNumber = '2';";
				//ResultSet : arr // usertbl의 row들이 들어 있음 // 각 row들이 통채로 들어있음
				
				result = st.executeQuery(sql);
				
				while(result.next()) {
					pAmount = Integer.parseInt(result.getString("pAmount"));
					pAmount += Integer.parseInt(tf_room2_in.getText());
				}
				sql = "update warehousedb.stock set pamount = " + pAmount + " where snumber = '2';";
				
				st.executeUpdate(sql);
				
				lbRoom2pAmount.setText(pAmount + "");
				tf_room2_in.clear();
			}
			else if(btnInOutID.equals("btn_room2_out")) {
				sql = "select pAmount from warehousedb.stock where sNumber = '2';";
				//ResultSet : arr // usertbl의 row들이 들어 있음 // 각 row들이 통채로 들어있음
				
				result = st.executeQuery(sql);
				
				while(result.next()) {
					pAmount = Integer.parseInt(result.getString("pAmount"));
					pAmount -= Integer.parseInt(tf_room2_out.getText());
				}
				sql = "update warehousedb.stock set pamount = " + pAmount + " where snumber = '2';";
				
				st.executeUpdate(sql);
				
				lbRoom2pAmount.setText(pAmount + "");
				tf_room2_out.clear();
			}
			else if(btnInOutID.equals("btn_room3_in")) {
				sql = "select pAmount from warehousedb.stock where sNumber = '3';";
				//ResultSet : arr // usertbl의 row들이 들어 있음 // 각 row들이 통채로 들어있음
				
				result = st.executeQuery(sql);
				
				while(result.next()) {
					pAmount = Integer.parseInt(result.getString("pAmount"));
					pAmount += Integer.parseInt(tf_room3_in.getText());
				}

				sql = "update warehousedb.stock set pamount = " + pAmount + " where snumber = '3';";
				
				st.executeUpdate(sql);
				
				lbRoom3pAmount.setText(pAmount + "");
				tf_room3_in.clear();
			}
			else if(btnInOutID.equals("btn_room3_out")) {
				sql = "select pAmount from warehousedb.stock where sNumber = '3';";
				//ResultSet : arr // usertbl의 row들이 들어 있음 // 각 row들이 통채로 들어있음
				
				result = st.executeQuery(sql);
				
				while(result.next()) {
					pAmount = Integer.parseInt(result.getString("pAmount"));
					pAmount -= Integer.parseInt(tf_room3_out.getText());
				}
				sql = "update warehousedb.stock set pamount = " + pAmount + " where snumber = '3';";
				
				st.executeUpdate(sql);
				
				lbRoom3pAmount.setText(pAmount + "");
				tf_room3_out.clear();
			}
		
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void ModifyProductOnAction(ActionEvent event) {
		try {
			String btnModifyID = ((Button)event.getSource()).getId();
			
			Stage ModifyDialog = new Stage();
			ModifyDialog.initModality(Modality.WINDOW_MODAL);
			ModifyDialog.initOwner(primaryStage);
			ModifyDialog.setTitle("재고 수정");
			
			FXMLLoader loadModify = new FXMLLoader(getClass().getResource("ModifyStock.fxml"));
			Parent modify = loadModify.load();
			
			Scene sceneModify = new Scene(modify);
			
			ModifyDialog.setScene(sceneModify);
			ModifyDialog.show();
			
			
			Label lb_roomNum = (Label)modify.lookup("#lb_roomnumber");
			if(btnModifyID.equals("btn_room1_modify")) {
				 lb_roomNum.setText("1");
			}
			else if(btnModifyID.equals("btn_room2_modify")) {
				 lb_roomNum.setText("2");
			}
			else if(btnModifyID.equals("btn_room3_modify")) {
				 lb_roomNum.setText("3");
			}
			
			TextField tf_pName = (TextField)modify.lookup("#tf_pName");
			TextField tf_pAmount = (TextField)modify.lookup("#tf_pAmount");
			Button btn_ok = (Button)modify.lookup("#btn_ok");
			Button btn_cancle = (Button)modify.lookup("#btn_cancle");
			
			btn_ok.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {
						sql = "update warehousedb.stock "
							+ "set pName = '" + tf_pName.getText() +"', pAmount = " + tf_pAmount.getText()
							+" where sNumber = '" + lb_roomNum.getText() +"';";
					
						st.executeUpdate(sql);
						
						sql = "select * from warehousedb.stock where sNumber = " + lb_roomNum.getText() +";";
					
						result = st.executeQuery(sql);
						
						while(result.next()) {
							String sqlResult = result.getString("sNumber");
							if(sqlResult.equals("1")) {
								sqlResult = result.getString("pName");
								lbRoom1pName.setText(sqlResult);
								sqlResult = result.getString("pAmount");
								lbRoom1pAmount.setText(sqlResult);
							}
							else if(sqlResult.equals("2")) {
								sqlResult = result.getString("pName");
								lbRoom2pName.setText(sqlResult);
								sqlResult = result.getString("pAmount");
								lbRoom2pAmount.setText(sqlResult);
							}
							else if(sqlResult.equals("3")) {
								sqlResult = result.getString("pName");
								lbRoom3pName.setText(sqlResult);
								sqlResult = result.getString("pAmount");
								lbRoom3pAmount.setText(sqlResult);
							}
							
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						Thread.sleep(1000);
						ModifyDialog.close();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			btn_cancle.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					ModifyDialog.close();
					
				}
			});
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 시리얼 연결을 하기위한 함수
	private void connect(String portName) throws Exception {

		System.out.printf("Port : %s\n", portName);

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		if (portIdentifier.isCurrentlyOwned()) { // currentlyowned : 누가 쓰고있냐
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams( // 시리얼 포트 설정
						9600, // 통신 속도
						SerialPort.DATABITS_8, // 몇비트 통신인가,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				in = serialPort.getInputStream();

				out = serialPort.getOutputStream();

				(new Thread(new SerialReader(in, new RootController()))).start();
				(new Thread(new SerialWriter(out))).start();
			}
		}
	}

	public static class SerialReader implements Runnable {
		InputStream in;
		RootController root;
		
		
		public SerialReader(InputStream in, RootController root) {
			this.in = in;
			this.root = root;
		}

		@Override
		public void run() {

			byte[] buffer = new byte[1024];
			int len = -1;
			String str = "";
			
			try {
				while ((len = this.in.read(buffer)) > -1) {
					 
					
					if(len > 0) { 
						str += new String(buffer, 0, len);
						
						if(str.charAt(str.length()-1) == '\n') {
							String[] strArr = str.trim().split(" : |\n");
							System.out.println(strArr[0] + " : " + strArr[1]);
							str = "";
						  
							if(strArr[0].equals("room1_desire")) { 
								Platform.runLater(new Runnable() {
						  
									@Override public void run() { 
										lbRoom1DesireTemp.setText(strArr[1]); 
									} 
							
								}); 
							}	
							else if(strArr[0].equals("room2_desire")) { 
								Platform.runLater(new Runnable() {
									@Override public void run() { 
										lbRoom2DesireTemp.setText(strArr[1]); 
									} 
								}); 
							}
							else if(strArr[0].equals("room3_desire")) { 
								Platform.runLater(new Runnable() {
									@Override public void run() { 
										lbRoom3DesireTemp.setText(strArr[1]); 
									} 
								}); 
							}
							else if(strArr[0].equals("room1_current")) { 
								Platform.runLater(new Runnable() {
									@Override public void run() { 
										lbRoom1CurrentTemp.setText(strArr[1]); 
									} 
								}); 
							}
							else if(strArr[0].equals("room2_current")) { 
								Platform.runLater(new Runnable() {
									@Override public void run() { 
										lbRoom2CurrentTemp.setText(strArr[1]); 
									} 
								}); 
							}
							else if(strArr[0].equals("room3_current")) { 
								Platform.runLater(new Runnable() {
									@Override public void run() { 
										lbRoom3CurrentTemp.setText(strArr[1]); 
									} 
								}); 
							}
							else if(strArr[0].equals("desire_temp")) { 
								Platform.runLater(new Runnable() {
									@Override public void run() {
										String[] desireStr = strArr[1].split(" ");
										lbRoom1DesireTemp.setText(desireStr[0]);
										lbRoom2DesireTemp.setText(desireStr[1]); 
										lbRoom3DesireTemp.setText(desireStr[2]); 
									} 
								}); 
							}
						} 
						
					}
						
				}
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	public static class SerialWriter implements Runnable {
		OutputStream out;

		public SerialWriter(OutputStream out) {
			this.out = out;
		}

		@Override
		public void run() {
			try {
				int c = 0;
				System.out.println("\n Keyboard Input Read!!!");// 안내 문구 출력
				while ((c = System.in.read()) > -1) {
					this.out.write(c);
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
	
	
	public void handleTextInput() {
		TextInputDialog dialog = new TextInputDialog("");
		
		//optional<?> : 내가 받을 형식을 정해준다
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			this.lb_room1_product_name.setText(name);
		});
		
	}
	
	private void DBconnect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			//연결점 만들기
			
			connection = DriverManager.getConnection(url, loginID, loginPW);
			
			st = connection.createStatement();
			
			sql = "select * from warehousedb.stock;";
			//ResultSet : arr // usertbl의 row들이 들어 있음 // 각 row들이 통채로 들어있음
			result = st.executeQuery(sql);
			
			while(result.next()) { //next() : result에 다음 row가 있으면 true, 없으면 false return 
				String sqlResult = result.getString("sNumber"); //현재 row의 userid의 column 값
						
				if(sqlResult.equals("1")) {
					sqlResult = result.getString("pName");
					lbRoom1pName.setText(sqlResult);
					sqlResult = result.getString("pAmount");
					lbRoom1pAmount.setText(sqlResult);
				}
				else if(sqlResult.equals("2")) {
					sqlResult = result.getString("pName");
					lbRoom2pName.setText(sqlResult);
					sqlResult = result.getString("pAmount");
					lbRoom2pAmount.setText(sqlResult);
				}
				else if(sqlResult.equals("3")) {
					sqlResult = result.getString("pName");
					lbRoom3pName.setText(sqlResult);
					sqlResult = result.getString("pAmount");
					lbRoom3pAmount.setText(sqlResult);
				}
			}
			 
			
		} catch (SQLException e) {
			System.out.println("SQLException");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException");
			e.printStackTrace();
		}
		/*
		finally {
			try {
				if(st != null) st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				if(connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		*/
	}
	
	
}
