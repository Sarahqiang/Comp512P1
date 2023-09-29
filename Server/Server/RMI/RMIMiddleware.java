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
        if (args.length > 0) {
            s_serverName = args[0];
        }

        // Create the RMI server entry
        try {
            // Create a new Middleware object
            RMIMiddleware server = new RMIMiddleware(s_serverName);

            // Dynamically generate the stub (client proxy)
            IResourceManager resourceManager = (IResourceManager) UnicastRemoteObject.exportObject(server, 0);

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
        FlightResourceManager flightManager = new FlightResourceManager(s_serverName + "_Flight");
        CarResourceManager carManager = new CarResourceManager(s_serverName + "_Car");
        RoomResourceManager roomManager = new RoomResourceManager(s_serverName + "_Room");
        this.setCarManager(carManager);
        this.setFlightManager(flightManager);
        this.setRoomManager(roomManager);
    }
}
