package Server.Middleware;

import Server.Common.*;
import Server.Interface.*;
import Server.ResourceManagers.*;

import java.rmi.RemoteException;
import java.util.*;

public class Middleware extends ResourceManager implements IResourceManager {

//    FlightResourceManager flightManager = new FlightResourceManager("FlightResourceManager");
//    CarResourceManager carManager = new CarResourceManager("CarResourceManager");
//    RoomResourceManager roomManager = new RoomResourceManager("RoomResourceManager");

    //    private HashMap<Integer, Customer> customers;
    private FlightResourceManager flightManager;
    private CarResourceManager carManager;
    private RoomResourceManager roomManager;

    public Middleware(String name) {
        super(name);
//        this.customers = new HashMap<>();
    }

    public void setFlightManager(FlightResourceManager RM) {
        this.flightManager = RM;
    }

    public void setCarManager(CarResourceManager RM) {
        this.carManager = RM;
    }

    public void setRoomManager(RoomResourceManager RM) {
        this.roomManager = RM;
    }


    @Override
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightManager.addFlight(id, flightNum, flightSeats, flightPrice);
    }

    @Override
    public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
        return carManager.addCars(id, location, numCars, price);
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
        return roomManager.addRooms(id, location, numRooms, price);
    }

    @Override
    public int newCustomer(int xid) throws RemoteException {
        Trace.info("Middleware::newCustomer(" + xid + ") called");
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt(String.valueOf(xid) +
                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                String.valueOf(Math.round(Math.random() * 100 + 1)));
        Customer customer = new Customer(cid);
        writeData(xid, customer.getKey(), customer);
        Trace.info("Middleware::newCustomer(" + cid + ") returns ID=" + cid);
        return cid;
    }


    @Override
    public boolean newCustomer(int xid, int customerID) throws RemoteException {
        Trace.info("Middleware::newCustomer(" + xid + ", " + customerID + ") called");

        Customer customer = (Customer) readData(xid, Customer.getKey(customerID));
        if (customer == null) {
            customer = new Customer(customerID);
            writeData(xid, customer.getKey(), customer);
            Trace.info("Middleware::newCustomer(" + xid + ", " + customerID + ") created a new customer");
            return true;
        } else {
            Trace.info("INFO: Middleware::newCustomer(" + xid + ", " + customerID + ") failed--customer already exists");
            return false;
        }
    }


    @Override
    public boolean deleteCustomer(int xid, int customerID) throws RemoteException {
        Trace.info("Middleware::deleteCustomer(" + xid + ", " + customerID + ") called");

        Customer customer = (Customer) readData(xid, Customer.getKey(customerID));
        if (customer == null) {
            Trace.warn("Middleware::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
            return false;
        } else {
            RMHashMap reservations = customer.getReservations();
            for (String reservedKey : reservations.keySet()) {
                ReservedItem reserveditem = customer.getReservedItem(reservedKey);
                Trace.info("Middleware::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " + reserveditem.getCount() + " times");

                // Delegate to the appropriate ResourceManager based on the reservedKey
                if (reservedKey.startsWith("flight-")) {
                    flightManager.deleteFlight(xid, Integer.parseInt(reservedKey.substring(7)));
                } else if (reservedKey.startsWith("car-")) {
                    carManager.deleteCars(xid, reservedKey.substring(4));
                } else if (reservedKey.startsWith("room-")) {
                    roomManager.deleteRooms(xid, reservedKey.substring(5));
                }
            }

            // Remove the customer from the storage
            removeData(xid, customer.getKey());
            Trace.info("Middleware::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
            return true;
        }
    }


    @Override
    public String queryCustomerInfo(int xid, int customerID) throws RemoteException {
        Trace.info("Middleware::queryCustomerInfo(" + xid + ", " + customerID + ") called");
        Customer customer = (Customer) readData(xid, Customer.getKey(customerID));
        if (customer == null) {
            Trace.warn("Middleware::queryCustomerInfo(" + xid + ", " + customerID + ") failed--customer doesn't exist");
            // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
            return "";
        } else {
            Trace.info("Middleware::queryCustomerInfo(" + xid + ", " + customerID + ")");
            System.out.println(customer.getBill());
            return customer.getBill();
        }
    }


    @Override
    public boolean deleteFlight(int id, int flightNum) throws RemoteException {
        return flightManager.deleteFlight(id, flightNum);
    }

    @Override
    public boolean deleteCars(int id, String location) throws RemoteException {
        return carManager.deleteCars(id, location);
    }

    @Override
    public boolean deleteRooms(int id, String location) throws RemoteException {
        return roomManager.deleteRooms(id, location);
    }

    @Override
    public int queryFlight(int id, int flightNumber) throws RemoteException {
        return flightManager.queryFlight(id, flightNumber);
    }

    @Override
    public int queryCars(int id, String location) throws RemoteException {
        return carManager.queryCars(id, location);
    }

    @Override
    public int queryRooms(int id, String location) throws RemoteException {
        return roomManager.queryRooms(id, location);
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
        return flightManager.queryFlightPrice(id, flightNumber);
    }

    @Override
    public int queryCarsPrice(int id, String location) throws RemoteException {
        return carManager.queryCarsPrice(id, location);
    }

    @Override
    public int queryRoomsPrice(int id, String location) throws RemoteException {
        return roomManager.queryRoomsPrice(id, location);
    }

    @Override
    public boolean reserveFlight(int id, int customerID, int flightNumber) throws RemoteException {
        return flightManager.reserveFlight(id, customerID, flightNumber);
    }

    @Override
    public boolean reserveCar(int id, int customerID, String location) throws RemoteException {
        return carManager.reserveCar(id, customerID, location);
    }

    @Override
    public boolean reserveRoom(int id, int customerID, String location) throws RemoteException {
        return roomManager.reserveRoom(id, customerID, location);
    }

    @Override
    public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        Trace.info("Middleware::bundle(" + id + ", " + customerID + ", " + flightNumbers + ", " + location + ", " + car + ", " + room + ") called");

        Customer customer = (Customer) readData(id, Customer.getKey(customerID));

        if (customer == null) {
            Trace.warn("Middleware::bundle(" + id + ", " + customerID + ")  failed--customer doesn't exist");
            return false;
        }

        boolean success = true;

        // Reserve all flights
        for (String flightNumber : flightNumbers) {
            success &= flightManager.reserveFlight(id, customerID, Integer.parseInt(flightNumber));
        }

        // Reserve car if requested
        if (car) {
            success &= carManager.reserveCar(id, customerID, location);
        }

        // Reserve room if requested
        if (room) {
            success &= roomManager.reserveRoom(id, customerID, location);
        }

        if (!success) {
            Trace.warn("Middleware::bundle(" + id + ", " + customerID + ", " + flightNumbers + ", " + location + ", " + car + ", " + room + ") failed");
        } else {
            Trace.info("Middleware::bundle(" + id + ", " + customerID + ", " + flightNumbers + ", " + location + ", " + car + ", " + room + ") succeeded");
        }


        return success;
    }

    @Override
    public String getName() throws RemoteException {
        return "Middleware";
    }
}
