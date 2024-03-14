package gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import logic.SceneLoaderHelper;
import logic.User;
import utils.AlertPopUp;

public class DepartmentManagerScreenController implements Initializable,IScreenController {

	@FXML
	public BorderPane screen;
	@FXML
	public Button dashboard_btn;
	@FXML
	public Button viewReports_btn;
	@FXML
	public Button profile_btn;
	@FXML
	public Button logout_btn;
	
	private Stage stage;
	private Stage mainScreenStage;
	private User user;
	private SceneLoaderHelper GuiHelper= new SceneLoaderHelper();
	
	public DepartmentManagerScreenController(User user) 
	{
		this.user=user;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		onDashboardClicked();
	}
	
	public void onDashboardClicked() {
		AnchorPane dashboard = GuiHelper.loadRightScreenToBorderPaneWithController("/gui/view/DepartmentManagerDashboard.fxml", ControllerType.Department_Manager_Dashboard_Controller,user);
		screen.setCenter(dashboard);
	}
	
	public void onViewReportsClicked() {
		AnchorPane dashboard = GuiHelper.loadRightScreenToBorderPaneWithController("/gui/view/DepartmentManagerViewReports.fxml", ControllerType.Department_Manager_ViewReports_Controller,user);
		screen.setCenter(dashboard);
	}
	
	public void onProfileClicked() {
		AnchorPane dashboard = GuiHelper.loadRightScreenToBorderPaneWithController("/gui/view/ClientProfile.fxml", ControllerType.Common_Profile_Controller,user);
		screen.setCenter(dashboard);
	}
	
	public void onLogoutClicked() {
		AlertPopUp alert = new AlertPopUp(AlertType.INFORMATION,"Logout","Logout Clicked","Test");
		alert.showAndWait();
	}
	
	private Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void setMainScreenStage(Stage stage) {
		this.mainScreenStage = stage;
	}
	
}
