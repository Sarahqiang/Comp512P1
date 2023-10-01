package Server.RMI;

import Server.Interface.*;
import Server.Middleware.*;
import Server.Common.*;
import Server.ResourceManagers.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIMiddleware extends Middleware {
    private static String s_serverName = "Middleware";
    private static String s_rmiPrefix = "group_17";

    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Not enough arguments. Need <FlightManagerHost> <CarManagerHost> <RoomManagerHost>");
            System.exit(1);
        }

        String flightManagerHost = args[0];
        String[] flight_info = flightManagerHost.split(",");
        String carManagerHost = args[1];
        String[] car_info = carManagerHost.split(",");
        String roomManagerHost = args[2];
        String[] room_info = roomManagerHost.split(",");

        // Create the RMI server entry
        try {
            // Create a new Middleware object
            RMIMiddleware middleware = new RMIMiddleware(s_serverName);

            middleware.initializeRMs(flight_info, car_info, room_info);
//            initializeManagers("f", "localhost", Integer.parseInt(flight_info[1]), s_rmiPrefix + flight_info[0]);
//            initializeManagers("c", "localhost", Integer.parseInt(car_info[1]), s_rmiPrefix + car_info[0]);
//            initializeManagers("r", "localhost", Integer.parseInt(room_info[1]), s_rmiPrefix + room_info[0]);

            // Dynamically generate the stub (client proxy)
            IResourceManager resourceManager = (IResourceManager) UnicastRemoteObject.exportObject(middleware, 0);

            // Bind the remote object's stub in the registry
            Registry l_registry;
            try {
                l_registry = LocateRegistry.createRegistry(3017);
            } catch (RemoteException e) {
                l_registry = LocateRegistry.getRegistry(3017);
            }
            final Registry registry = l_registry;
            registry.rebind(s_rmiPrefix + s_serverName, resourceManager);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        registry.unbind(s_rmiPrefix + s_serverName);
                        System.out.println("'" + s_serverName + "' middleware unbound");
                    } catch (Exception e) {
                        System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("'" + s_serverName + "' middleware server ready and bound to '" + s_rmiPrefix + s_serverName + "'");
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }

        // Create and install a security manager
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
    }

    public RMIMiddleware(String name) {
        super(name);
    }

    public void initializeRMs(String[] flight_info, String[] car_info, String[] room_info) {
        initializeManagers("f", "localhost", Integer.parseInt(flight_info[1]), s_rmiPrefix + flight_info[0]);
        initializeManagers("c", "localhost", Integer.parseInt(car_info[1]), s_rmiPrefix + car_info[0]);
        initializeManagers("r", "localhost", Integer.parseInt(room_info[1]), s_rmiPrefix + room_info[0]);
    }
}
