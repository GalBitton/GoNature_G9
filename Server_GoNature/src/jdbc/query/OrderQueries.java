package jdbc.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.swing.DefaultBoundedRangeModel;

import jdbc.DatabaseResponse;
import jdbc.MySqlConnection;
import jdbc.QueryType;
import logic.Order;
import logic.Park;
import utils.enums.EmployeeTypeEnum;
import utils.enums.OrderStatusEnum;
import utils.enums.OrderTypeEnum;
import utils.enums.ParkNameEnum;
import utils.enums.UserStatus;
import utils.enums.UserTypeEnum;

public class OrderQueries {

	public OrderQueries() {
	}

	/**
	 * Gets an order and checks if it's in DB
	 * 
	 * @param order - the order to search for. *already initialized*
	 * @return on Success: returns Order_Found_Successfully
	 *         on Failure: returns Such_Order_Does_Not_Exists
	 *         exception: returns Exception_Was_Thrown
	 */
	public DatabaseResponse fetchOrderByOrderID(Order order) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con
					.prepareStatement("SELECT * FROM preorders WHERE orderId = ? AND OrderStatus!='Cancelled'");
			stmt.setInt(1, order.getOrderId());
			ResultSet rs = stmt.executeQuery();

			// if the query ran successfully, but returned as empty table.
			if (!rs.next()) {
				return DatabaseResponse.Such_Order_Does_Not_Exists;
			}

			order.setOrderId(rs.getInt(1));
			order.setParkName(ParkNameEnum.fromParkId(rs.getInt(2)));
			order.setUserId(String.format("%d", rs.getInt(3)));
			order.setOwnerType(rs.getString(4));
			order.setEnterDate(rs.getTimestamp(5).toLocalDateTime());
			order.setExitDate(rs.getTimestamp(6).toLocalDateTime());
			order.setPaid(rs.getBoolean(7));
			order.setStatus(OrderStatusEnum.fromString(rs.getString(8)));
			order.setEmail(rs.getString(9));
			order.setTelephoneNumber(rs.getString(10));
			order.setFirstName(rs.getString(11));
			order.setLastName(rs.getString(12));
			order.setOrderType(OrderTypeEnum.fromString(rs.getString(13)));
			order.setNumberOfVisitors(rs.getInt(14));
			order.setPrice(rs.getDouble(15));

			return DatabaseResponse.Order_Found_Successfully;

		} catch (SQLException ex) {
//			serverController.printToLogConsole("Query search for user failed");
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}

	/**
	 * gets an order and checks if the current date & time is available for a
	 * specific park
	 * 
	 * @param order - contains the order EnterDate and parkId
	 * @return The amount of visitors that are currently in the park, returns -1 in
	 *         case of failure
	 */
	public int numberOfVisitorsAtSpecificTime(Order order) {
		int visitorsAmount = 0;

		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement(
					"SELECT SUM(Amount) AS Count FROM preorders WHERE EnterDate <= ? AND ExitDate > ? AND (OrderStatus = 'Wait Notify' OR OrderStatus = 'Notified' OR OrderStatus = 'Confirm Not Paid' OR OrderStatus = 'Paid' OR OrderStatus = 'In Park') AND parkId = ?;");
			stmt.setString(1, order.getEnterDate().toString());
			stmt.setString(2, order.getEnterDate().toString());
			stmt.setInt(3, order.getParkName().getParkId());

			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				return visitorsAmount;
			}

			visitorsAmount = rs.getInt(1);
			return visitorsAmount;

		} catch (SQLException ex) {
			ex.printStackTrace();
			return -1;
		}
		
	}


	/**
	 * Searches an order in DB using ownerId, ownerId is the user which owns the
	 * order
	 * 
	 * @param order - contains the ownerId
	 * @return on success returns Order_Found_Successfully 
	 *         on failure returns Such_Order_Does_Not_Exists 
	 *         exception: returns Excpetion_Was_Thrown
	 */
	public DatabaseResponse fetchOrderByOwnerID(Order order) {

		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con
					.prepareStatement("SELECT * FROM preorders WHERE ownerId = ? AND OrderStatus!='Cancelled'");
			stmt.setString(1, order.getUserId());
			ResultSet rs = stmt.executeQuery();

			// if the query ran successfully, but returned as empty table.
			if (!rs.next()) {
				return DatabaseResponse.Such_Order_Does_Not_Exists;
			}

			order.setOrderId(rs.getInt(1));
			order.setParkName(ParkNameEnum.fromParkId(rs.getInt(2)));
			order.setUserId(String.format("%d", rs.getInt(3)));
			order.setOwnerType(rs.getString(4));
			order.setEnterDate(rs.getTimestamp(5).toLocalDateTime());
			order.setExitDate(rs.getTimestamp(6).toLocalDateTime());
			order.setPaid(rs.getBoolean(7));
			order.setStatus(OrderStatusEnum.fromString(rs.getString(8)));
			order.setEmail(rs.getString(9));
			order.setTelephoneNumber(rs.getString(10));
			order.setFirstName(rs.getString(11));
			order.setLastName(rs.getString(12));
			order.setOrderType(OrderTypeEnum.fromString(rs.getString(13)));
			order.setNumberOfVisitors(rs.getInt(14));
			order.setPrice(rs.getDouble(15));

			return DatabaseResponse.Order_Found_Successfully;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}

	/**
	 * gets an order and changes its status according statusToUpdate in DB, also
	 * changes the status in the given entity order.
	 * 
	 * @param order - the order where the change of its status will be made
	 * @return on success returns Order_Status_Updated 
	 *         on failure returns Such_Order_Does_Not_Exists
	 *         on exception returns Excpetion_Was_Thrown
	 */
	public DatabaseResponse updateOrderStatus(Order order, OrderStatusEnum statusToUpdate) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE preorders SET OrderStatus = ? WHERE (OrderId = ?);");
			stmt.setString(1, statusToUpdate.toString());
			stmt.setInt(2, order.getOrderId());
			int rs = stmt.executeUpdate();

			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			order.setStatus(statusToUpdate);
			order.setLastStatusUpdatedTime(LocalDateTime.now().toString());

			return DatabaseResponse.Order_Status_Updated;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}

	/**
	 * Gets an order and adds it to the pre-order table in DB
	 * 
	 * @param order - the requested order to add in DB
	 * @return on Success: returns Order_Added_Into_Table 
	 *         on Failure: returns Failed
	 *         exception: returns Exception_Was_Thrown
	 */
	public DatabaseResponse insertOrderIntoDB(Order order) {

		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement(
					"INSERT INTO preorders (OrderId, ParkId, OwnerId, OwnerType, EnterDate, ExitDate, PayStatus, OrderStatus, Email, Phone, FirstName, LastName, OrderType, Amount, Price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			stmt.setInt(1, order.getOrderId());
			stmt.setInt(2, order.getParkName().getParkId());
			stmt.setString(3, order.getUserId());
			stmt.setString(4, order.getOwnerType());
			stmt.setString(5, order.getEnterDate().toString());
			stmt.setString(6, order.getExitDate().toString());
			int isPaid = order.isPaid() ? 1 : 0;
			stmt.setInt(7, isPaid);
			stmt.setString(8, order.getStatus().toString());
			stmt.setString(9, order.getEmail());
			stmt.setString(10, order.getTelephoneNumber());
			stmt.setString(11, order.getFirstName());
			stmt.setString(12, order.getLastName());
			stmt.setString(13, order.getOrderType().toString());
			stmt.setInt(14, order.getNumberOfVisitors());
			stmt.setDouble(15, order.getPrice());
			int rs = stmt.executeUpdate();

			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			return DatabaseResponse.Order_Added_Into_Table;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}
	
	/**
	 * Gets an order and updates its phone number in the DB
	 * @param order - must be already initialized with the updated phone number
	 * @return on success returns Order_PhoneNumber_Updated
	 *  	   on Failure: returns Failed
	 *         exception: returns Exception_Was_Thrown
	 */
	public DatabaseResponse updateOrderPhoneNumber(Order order) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE preorders SET Phone = ? WHERE (OrderId = ?);");
			stmt.setString(1, order.getTelephoneNumber());
			stmt.setInt(2, order.getOrderId());
			int rs = stmt.executeUpdate();
			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			return DatabaseResponse.Order_PhoneNumber_Updated;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}
	
	/**
	 * Gets an order and updates its Email address in the DB
	 * @param order - must be already initialized with the updated Email
	 * @return on success returns Order_Email_Updated
	 *  	   on Failure: returns Failed
	 *         exception: returns Exception_Was_Thrown
	 */
	public DatabaseResponse updateOrderEmail(Order order) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE preorders SET Email = ? WHERE (OrderId = ?);");
			stmt.setString(1, order.getEmail());
			stmt.setInt(2, order.getOrderId());
			int rs = stmt.executeUpdate();
			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			return DatabaseResponse.Order_Email_Updated;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}
	
	/**
	 * Gets an order and updates its number of visitors (amount) in the DB
	 * @param order - must be already initialized with the updated amount
	 * @return on success returns Order_Number_Of_Visitors_Updated
	 *  	   on Failure: returns Failed
	 *         exception: returns Exception_Was_Thrown
	 */
	public DatabaseResponse updateOrderNumberOfVisitors(Order order) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE preorders SET Amount = ? WHERE (OrderId = ?);");
			stmt.setInt(1, order.getNumberOfVisitors());
			stmt.setInt(2, order.getOrderId());
			int rs = stmt.executeUpdate();
			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			return DatabaseResponse.Order_Number_Of_Visitors_Updated;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}
	
	/**
	 * Gets an order and updates its type in the DB according to the given requested type 
	 * @param order - the order to update
	 * @return on success returns Order_Type_Updated
	 *  	   on Failure: returns Failed
	 *         exception: returns Exception_Was_Thrown
	 */
	/**
	 * @param order
	 * @param requestedType
	 * @return
	 */
	public DatabaseResponse updateOrderType(Order order, OrderTypeEnum requestedType) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE preorders SET OrderType = ? WHERE (OrderId = ?);");
			stmt.setString(1, order.getOrderType().toString());
			stmt.setInt(2, order.getOrderId());
			int rs = stmt.executeUpdate();
			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			return DatabaseResponse.Order_Type_Updated;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}
	
	
	/**
	 * Gets an order and updates its entry date time to the park in the DB according to the requested enter date time
	 * @param order - the order to update
	 * @return on success returns Order_EnterDate_Updated
	 *  	   on Failure: returns Failed
	 *         exception: returns Exception_Was_Thrown
	 */
	public DatabaseResponse updateOrderEnterDate(Order order, LocalDateTime enterDate) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE preorders SET EnterDate = ? WHERE (OrderId = ?);");
			stmt.setString(1, enterDate.toString());
			stmt.setInt(2, order.getOrderId());
			int rs = stmt.executeUpdate();
			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			return DatabaseResponse.Order_EnterDate_Updated;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}
	
	
	/**
	 * Gets an order and updates its exit date time from the park in the DB according to the requested exit date time
	 * @param order - the order to update
	 * @return on success returns Order_ExitDate_Updated
	 *  	   on Failure: returns Failed
	 *         exception: returns Exception_Was_Thrown
	 */
	public DatabaseResponse updateOrderExitDate(Order order, LocalDateTime exitDate) {
		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE preorders SET ExitDate = ? WHERE (OrderId = ?);");
			stmt.setString(1, exitDate.toString());
			stmt.setInt(2, order.getOrderId());
			int rs = stmt.executeUpdate();
			// if the query ran successfully, but returned as empty table.
			if (rs == 0) {
				return DatabaseResponse.Failed;
			}
			return DatabaseResponse.Order_ExitDate_Updated;

		} catch (SQLException ex) {
			return DatabaseResponse.Exception_Was_Thrown;
		}
	}
	
	
	/**
	 *
	 * @return returns the amount of preorders in the system
	 */
	public int ReturnTotalPreOrders() {
		int ordersCount = 0;

		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) AS orderCount FROM preorders");
			ResultSet rs = stmt.executeQuery();

			// if the query ran successfully, but returned as empty table.
			if (!rs.next()) {
				return ordersCount;
			}

			ordersCount = rs.getInt("orderCount");
			return ordersCount;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ordersCount;
	}
	
	/**
	 * @param status - requested order status
	 * @return the amount of preorder made in the system which currently have the requested status
	 */
	public int returnTotalPreOrdersWithStatus(OrderStatusEnum status) {
		int ordersCount = 0;

		try {
			Connection con = MySqlConnection.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) AS orderCount FROM preorders WHERE OrderStatus = ?");
			stmt.setString(1, status.toString());
			ResultSet rs = stmt.executeQuery();

			// if the query ran successfully, but returned as empty table.
			if (!rs.next()) {
				return ordersCount;
			}

			ordersCount = rs.getInt("orderCount");
			return ordersCount;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ordersCount;
	}
}