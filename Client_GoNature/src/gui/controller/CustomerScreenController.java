package gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.ClientApplication;
import client.ClientCommunication;
import gui.view.ApplicationViewType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import logic.ClientRequestDataContainer;
import logic.EntitiesContainer;
import logic.ExternalUser;
import logic.Guide;
import logic.ICustomer;
import logic.SceneLoaderHelper;
import logic.ServerResponseBackToClient;
import logic.Visitor;
import utils.AlertPopUp;
import utils.CurrentWindow;
import utils.enums.ClientRequest;
import utils.enums.UserTypeEnum;

public class CustomerScreenController implements Initializable, IScreenController {

	@FXML
	public BorderPane screen;
	@FXML
	public Label userIdLabel;
	@FXML
	public Label accountTypeLabel;
	@FXML
	public Button homeButton;
	@FXML
	public Button makeOrderButton;
	@FXML
	public Button manageOrdersButton;
	@FXML
	public Button backButton;
	@FXML
	public Button logoutButton;
	
//	private Customer customer;
	private ExternalUser customer;
	private UserTypeEnum currentCustomer;
	private SceneLoaderHelper GuiHelper= new SceneLoaderHelper();
	
	public CustomerScreenController(ExternalUser customer) {
		this.customer=customer;
		currentCustomer=customer.getUserType();
	}

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO: hide buttons according to user type
		switch(customer.getUserType()) {
		case ExternalUser:
			updateNewVisitorMenu(customer);
			break;
		case Guide:
			updateGuideMenu((Guide)customer);
			break;
		case Visitor:
			updateVisitorMenu((Visitor)customer);
			break;
		}
		onHomeClicked();
		
	}
	
	private void updateVisitorMenu(Visitor visitor) {
		userIdLabel.setText(visitor.getVisitorId());
		accountTypeLabel.setText(visitor.getUserType().toString());
		logoutButton.setVisible(false);
	}
	
	private void updateGuideMenu(Guide guide) {
		userIdLabel.setText(guide.getUserId());
		accountTypeLabel.setText(guide.getUserType().toString());
		backButton.setVisible(false);
	}
	
	private void updateNewVisitorMenu(ExternalUser newVisitor) {
		userIdLabel.setText("???");
		accountTypeLabel.setText("New Guest");
		manageOrdersButton.setVisible(false);
		logoutButton.setVisible(false);
	}
	
	public void onBackClicked() {
		if(customer.getUserType()==UserTypeEnum.ExternalUser) {
			GuiHelper.setScreenAfterLogoutOrBack();
		}
		else
			onLogoutClicked();
	}
	
	public void onHomeClicked() {
		AnchorPane dashboard = GuiHelper.loadRightScreenToBorderPaneWithController(screen,"/gui/view/CustomerHomepageScreen.fxml",
				ApplicationViewType.CustomerHomgepageScreen,new EntitiesContainer(customer));
		screen.setCenter(dashboard);
	}
	
	public void onMakeOrderClicked() {
		ICustomer customerDetails = null;
		if(!(currentCustomer==UserTypeEnum.ExternalUser))
			customerDetails = (ICustomer) customer;
		AnchorPane dashboard = GuiHelper.loadRightScreenToBorderPaneWithController(screen,"/gui/view/MakeOrderScreen.fxml",
				ApplicationViewType.MakeOrderScreen,new EntitiesContainer(customer,customerDetails));
		screen.setCenter(dashboard);
	}
	
	public void onManageOrdersClicked() {
		AnchorPane dashboard = GuiHelper.loadRightScreenToBorderPaneWithController(screen,"/gui/view/IdenticationScreen.fxml",
				ApplicationViewType.IdenticationScreen,new EntitiesContainer(customer));
		screen.setCenter(dashboard);
	}
	
	
	public void onLogoutClicked() {
		ClientRequestDataContainer request = new ClientRequestDataContainer(ClientRequest.Logout, customer);
		ClientApplication.client.accept(request);
		ServerResponseBackToClient response = ClientCommunication.responseFromServer;
		GuiHelper.setScreenAfterLogoutOrBack();
	}
	
	public void onServerCrashed() {
		AlertPopUp alert = new AlertPopUp(AlertType.ERROR,"FATAL ERROR","Server is Down","Server Crashed - The application will be closed.");
		alert.showAndWait();
		try {
			ClientApplication.client.getClient().closeConnection();
			Platform.runLater(()->CurrentWindow.getCurrentWindow().close());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
