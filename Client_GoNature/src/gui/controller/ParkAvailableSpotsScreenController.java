package gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import client.ClientApplication;
import client.ClientCommunication;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import logic.ClientRequestDataContainer;
import logic.Employee;
import logic.Park;
import logic.ServerResponseBackToClient;
import utils.CurrentDateAndTime;
import utils.enums.ClientRequest;
import utils.enums.ParkNameEnum;

public class ParkAvailableSpotsScreenController implements Initializable {
	@FXML
	public Label dateLabel;
	@FXML
	public ComboBox<ParkNameEnum> parkSelect;
	@FXML
	public Label currentInParkLabel;
	@FXML
	public Label maxCapacityLabel;
	
	private ObservableList<ParkNameEnum> parks = FXCollections.observableArrayList();
	
	private Employee employee;
	private Park selectedPark;
	private ParkNameEnum selectedParkName=ParkNameEnum.None;
	
	public ParkAvailableSpotsScreenController(Object employee,Object park) {
		this.employee=(Employee)employee;
		selectedPark=(Park)park;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		dateLabel.setText(CurrentDateAndTime.getCurrentDate("'Today' yyyy-MM-dd"));
		switch(employee.getEmployeeType()) {
		case Department_Manager:
			parks.add(ParkNameEnum.Banias);
			parks.add(ParkNameEnum.Herodium);
			parks.add(ParkNameEnum.Masada);
			break;
		case Park_Manager:
		case Park_Employee:
			parks.add(employee.getRelatedPark());
			break;
		}
		
		currentInParkLabel.setText("?");
		maxCapacityLabel.setText("?");
		
		parkSelect.getItems().addAll(parks);
		parkSelect.setOnAction(this::onChangeParkSelection);
		
	}
	
	private void onChangeParkSelection(ActionEvent event) {
		if(!(parkSelect.getValue() instanceof ParkNameEnum))
			return;

		selectedParkName=parkSelect.getValue();
		Park selectedPark = new Park(selectedParkName.getParkId());
		ClientRequestDataContainer request = new ClientRequestDataContainer(ClientRequest.Search_For_Specific_Park,selectedPark);
		ClientApplication.client.accept(request);
		ServerResponseBackToClient response = ClientCommunication.responseFromServer;
		selectedPark = (Park)response.getMessage();
		
		currentInParkLabel.setText(String.format("%d", selectedPark.getCurrentInPark()));
		maxCapacityLabel.setText(String.format("%d", selectedPark.getCurrentMaxCapacity()));

	}

}
