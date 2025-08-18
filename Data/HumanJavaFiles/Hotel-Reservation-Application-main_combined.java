package com.sameetasadullah.i180479_i180531;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.sameetasadullah.i180479_i180531", appContext.getPackageName());
    }
}

package com.sameetasadullah.i180479_i180531;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}

package com.sameetasadullah.i180479_i180531.logicLayer;

import java.time.LocalDate;

public class Room {
    private int number;
    private String type;
    private LocalDate availableDate;
    private boolean available;

    //constructor
    public Room() {}
    public Room(int no, String Type) {
        //assigning values to data members
        number = no;
        type = Type;
        available = true;
        availableDate = null;
    }

    //getters
    public int getNumber() {
        return number;
    }
    public String getType() {
        return type;
    }
    public boolean isAvailable() {
        return available;
    }
    public LocalDate getAvailableDate() { return availableDate; }

    //setters
    public void setAvailable(boolean available) {
        this.available = available;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }
}



package com.sameetasadullah.i180479_i180531.logicLayer;

public class Customer {
    private String name, address, email, password, phoneNo, CNIC, accountNo, dp;
    private int ID;

    //constructors
    public Customer() {
    }
    public Customer(int id, String mail, String pass, String Name,
                    String add, String phone, String cnic, String accNo, String dp) {
        ID = id;
        name = Name;
        address = add;
        phoneNo = phone;
        CNIC = cnic;
        accountNo = accNo;
        email = mail;
        password = pass;
        this.dp = dp;
    }

    //getters
    public int getID() {
        return ID;
    }
    public String getEmail() {
        return email;
    }
    public String getAccountNo() {
        return accountNo;
    }
    public String getCNIC() {
        return CNIC;
    }
    public String getPhoneNo() {
        return phoneNo;
    }
    public String getAddress() {
        return address;
    }
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public String getDp() { return dp; }

    //setters
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setCNIC(String CNIC) {
        this.CNIC = CNIC;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setDp(String dp) { this.dp = dp; }
}


package com.sameetasadullah.i180479_i180531.logicLayer;

import java.time.LocalDate;
import java.util.*;

public class Reservation {
    private String customerEmail, checkInDate, checkOutDate, hotelName, hotelLocation, roomNumbers, totalRooms, totalPrice;

    public Reservation(String hotelName, String hotelLocation, String totalRooms, String roomNumbers,
                       String totalPrice, String checkInDate, String checkOutDate, String customerEmail) {
        this.customerEmail = customerEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.hotelName = hotelName;
        this.hotelLocation = hotelLocation;
        this.roomNumbers = roomNumbers;
        this.totalRooms = totalRooms;
        this.totalPrice = totalPrice;
    }

    public String getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(String totalRooms) {
        this.totalRooms = totalRooms;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelLocation() {
        return hotelLocation;
    }

    public void setHotelLocation(String hotelLocation) {
        this.hotelLocation = hotelLocation;
    }

    public String getRoomNumbers() {
        return roomNumbers;
    }

    public void setRoomNumbers(String roomNumbers) {
        this.roomNumbers = roomNumbers;
    }
}


package com.sameetasadullah.i180479_i180531.logicLayer;

import com.sameetasadullah.i180479_i180531.dataLayer.writerAndReader;

import java.time.LocalDate;
import java.util.*;

public class Hotel {
    private int ID, totalRooms;
    private String singleRooms, doubleRooms, singleRoomPrice, doubleRoomPrice, name, address, location, registered_by;
    private Vector<Room> rooms;
    private Vector<Reservation> reservations;

    //constructors
    public Hotel() {
    }
    public Hotel(int id, String Name, String add, String loc, String sRooms, String dRooms,
                 String sRoomPrice, String dRoomPrice, String registered_by) {
        //assigning values to data members
        ID = id;
        name = Name;
        address = add;
        totalRooms = Integer.parseInt(sRooms) + Integer.parseInt(dRooms);
        singleRooms = sRooms;
        doubleRooms = dRooms;
        location = loc;
        singleRoomPrice = sRoomPrice;
        doubleRoomPrice = dRoomPrice;
        this.registered_by = registered_by;
        reservations = new Vector<>();

        //making rooms in hotel
        rooms = new Vector<>();
        for (int i = 0; i < totalRooms; ++i) {
            Room r1;
            if (i < Integer.parseInt(singleRooms)) {
                r1 = new Room(i + 1, "Single");
            } else {
                r1 = new Room(i + 1, "Double");
            }
            rooms.add(r1);
        }
    }

    //getters
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public int getID() {
        return ID;
    }
    public int getTotalRooms() {
        return totalRooms;
    }
    public Vector<Room> getRooms() {
        return rooms;
    }
    public String getLocation() {
        return location;
    }
    public String getDoubleRooms() {
        return doubleRooms;
    }
    public String getSingleRooms() {
        return singleRooms;
    }
    public String getSingleRoomPrice() {
        return singleRoomPrice;
    }
    public Vector<Reservation> getReservations() {
        return reservations;
    }
    public String getDoubleRoomPrice() {
        return doubleRoomPrice;
    }
    public String getRegistered_by() { return registered_by; }

    //setters
    public void setID(int ID) {
        this.ID = ID;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setRooms(Vector<Room> rooms) {
        this.rooms = rooms;
    }
    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setDoubleRooms(String doubleRooms) {
        this.doubleRooms = doubleRooms;
    }
    public void setReservations(Vector<Reservation> reservations) { this.reservations = reservations; }
    public void setSingleRooms(String singleRooms) {
        this.singleRooms = singleRooms;
    }
    public void setSingleRoomPrice(String singleRoomPrice) { this.singleRoomPrice = singleRoomPrice; }
    public void setDoubleRoomPrice(String doubleRoomPrice) { this.doubleRoomPrice = doubleRoomPrice; }
    public void setRegistered_by(String registered_by) { this.registered_by = registered_by; }

    //return rooms of hotel which can accommodate user requirements
    public Vector<Room> getRooms(String noOfPersons, LocalDate checkInDate, String roomType, Boolean both) {
        int personsCount = 0;
        Vector<Room> searchedRooms = new Vector<>();

        for (int i = 0; i < totalRooms; ++i) {
            if (rooms.get(i).isAvailable() || checkInDate.isEqual(rooms.get(i).getAvailableDate()) ||
                    checkInDate.isAfter(rooms.get(i).getAvailableDate())) {
                if (both == true) {
                    if (rooms.get(i).getType().equals("Single")) {
                        personsCount += 1;
                    } else {
                        personsCount += 2;
                    }
                    searchedRooms.add(rooms.get(i));
                }
                else {
                    if (rooms.get(i).getType().equals("Single") && roomType.equals("Single")) {
                        personsCount += 1;
                        searchedRooms.add(rooms.get(i));
                    } else if (rooms.get(i).getType().equals("Double") && roomType.equals("Double")){
                        personsCount += 2;
                        searchedRooms.add(rooms.get(i));
                    }
                }
            }

            if (personsCount >= Integer.parseInt(noOfPersons)) {
                return searchedRooms;
            }
        }
        return null;
    }

    //function for reserving room in a hotel
    public Reservation reserveRoom(LocalDate checkInDate, LocalDate checkOutDate, Customer c, Vector<Hotel> hotels) {
        int temp = 0;
        for (int i = 0; i < rooms.size(); ++i) {
            rooms.get(i).setAvailable(false);
            rooms.get(i).setAvailableDate(checkOutDate.plusDays(1));
        }
        for (int i = 0; i < hotels.size(); ++i) {
            if (hotels.get(i).getID() == ID) {
                for (int j = 0; j < hotels.get(i).getRooms().size(); ++j) {
                    if (hotels.get(i).getRooms().get(j).getNumber() == rooms.get(temp).getNumber()) {
                        hotels.get(i).getRooms().get(j).setAvailable(rooms.get(temp).isAvailable());
                        hotels.get(i).getRooms().get(j).setAvailableDate(rooms.get(temp).getAvailableDate());
                        if ((temp + 1) != rooms.size()) {
                            temp++;
                        }
                    }
                }

                String roomNumbers = "";
                for (int j = 0; j < rooms.size(); ++j) {
                    roomNumbers += rooms.get(j).getNumber();
                    if (j != rooms.size() - 1) {
                        roomNumbers += ", ";
                    }
                }

                int totalPriceCal=0;
                for (int j=0;j<rooms.size();j++){
                    if (rooms.get(j).getType().equals("Single")){
                        totalPriceCal= totalPriceCal + Integer.parseInt(singleRoomPrice);
                    }
                    else{
                        totalPriceCal= totalPriceCal + Integer.parseInt(doubleRoomPrice);
                    }
                }

                Hotel hotel = hotels.get(i);
                Reservation r1 = new Reservation(hotel.getName(), hotel.getLocation(),
                        Integer.toString(rooms.size()), roomNumbers, Integer.toString(totalPriceCal),
                        checkInDate.toString(), checkOutDate.toString(), c.getEmail());
                hotels.get(i).getReservations().add(r1);
                return r1;
            }
        }
        return null;
    }
}


package com.sameetasadullah.i180479_i180531.logicLayer;

public class Vendor {
    private String name, address, email, password, phoneNo, CNIC, accountNo, dp;
    private int ID;

    //constructors
    public Vendor() {
    }
    public Vendor(int id, String mail, String pass, String Name,
                  String add, String phone, String cnic, String accNo, String dp) {
        ID = id;
        name = Name;
        address = add;
        phoneNo = phone;
        CNIC = cnic;
        accountNo = accNo;
        email = mail;
        password = pass;
        this.dp = dp;
    }

    //getters
    public int getID() {
        return ID;
    }
    public String getAddress() {
        return address;
    }
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public String getAccountNo() {
        return accountNo;
    }
    public String getCNIC() {
        return CNIC;
    }
    public String getPhoneNo() {
        return phoneNo;
    }
    public String getDp() { return dp; }

    //setters
    public void setAddress(String address) {
        this.address = address;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setCNIC(String CNIC) {
        this.CNIC = CNIC;
    }
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
    public void setDp(String dp) { this.dp = dp; }
}



package com.sameetasadullah.i180479_i180531.logicLayer;

import android.content.Context;
import android.net.Uri;

import com.sameetasadullah.i180479_i180531.dataLayer.VolleyCallBack;
import com.sameetasadullah.i180479_i180531.dataLayer.writerAndReader;

import java.time.LocalDate;
import java.util.List;
import java.util.Vector;
import java.io.*;

public class HRS {
    private Vector<Customer> customers;
    private Vector<Hotel> hotels;
    private Vector<Vendor> vendors;
    private writerAndReader readAndWrite;
    private static HRS hrs;

    // constructor
    private HRS(Context context) {
        // initializing data members
        customers = new Vector<>();
        hotels = new Vector<>();
        vendors = new Vector<>();
        readAndWrite = new writerAndReader(context);

        // fetching old data from server
        readAndWrite.getCustomersFromServer(customers);
        readAndWrite.getVendorsFromServer(vendors);
        readAndWrite.getHotelsFromServer(hotels, new VolleyCallBack() {
            @Override
            public void onSuccess() {
                for (int i = 0; i < hotels.size(); ++i) {
                    readAndWrite.getRoomsFromServer(hotels.get(i));
                }
                readAndWrite.getReservationsFromServer(hotels);
            }
        });
    }

    // getters
    public static HRS getInstance(Context context) {
        if (hrs == null) {
            hrs = new HRS(context);
        }
        return hrs;
    }
    public Vector<Customer> getCustomers() {
        return customers;
    }
    public Vector<Hotel> getHotels() {
        return hotels;
    }
    public Vector<Vendor> getVendors() {
        return vendors;
    }
    public writerAndReader getReadAndWrite() {
        return readAndWrite;
    }

    // setters
    public void setCustomers(Vector<Customer> customers) {
        this.customers = customers;
    }
    public void setHotels(Vector<Hotel> hotels) {
        this.hotels = hotels;
    }
    public void setReadAndWrite(writerAndReader readAndWrite) {
        this.readAndWrite = readAndWrite;
    }
    public void setVendors(Vector<Vendor> vendors) { this.vendors = vendors; }

    // function to check if customer with same email already exists or not
    public boolean validateCustomerEmail(String email) {
        for (int i = 0; i < customers.size(); ++i) {
            if (email.equals(customers.get(i).getEmail())) {
                return false;
            }
        }
        return true;
    }

    // function to check if customer has logged in correctly or not
    public boolean validateCustomerAccount(String email, String pass) {
        for (int i = 0; i < customers.size(); ++i) {
            if (email.equals(customers.get(i).getEmail()) && customers.get(i).getPassword().equals(pass)) {
                return true;
            }
        }
        return false;
    }

    // function to check if vendor with same email already exists or not
    public boolean validateVendorEmail(String email) {
        for (int i = 0; i < vendors.size(); ++i) {
            if (email.equals(vendors.get(i).getEmail())) {
                return false;
            }
        }
        return true;
    }

    // function to check if vendor has logged in correctly or not
    public boolean validateVendorAccount(String email, String pass) {
        for (int i = 0; i < vendors.size(); ++i) {
            if (email.equals(vendors.get(i).getEmail()) && vendors.get(i).getPassword().equals(pass)) {
                return true;
            }
        }
        return false;
    }

    // function to check if hotel with same name and location already exists or not
    public boolean validateHotel(String name, String loc) {
        for (int i = 0; i < hotels.size(); ++i) {
            if (name.equals(hotels.get(i).getName()) && loc.equals(hotels.get(i).getLocation())) {
                return false;
            }
        }
        return true;
    }

    // function for customer registration
    public void registerCustomer(String name, String email, String pass,
                                 String add, String phone, String cnic, String accNo, Uri dp,
                                 VolleyCallBack volleyCallBack) {
        int ID = 0;

        //getting maximum ID
        for (int i = 0; i < customers.size(); ++i) {
            if (customers.get(i).getID() > ID) {
                ID = customers.get(i).getID();
            }
        }
        ID++;

        //registering customer
        Customer c = new Customer(ID, email, pass, name, add, phone, cnic, accNo, "");
        readAndWrite.insertCustomerDataIntoServer(c, dp, volleyCallBack);
        customers.add(c);
    }

    //function for vendor registration
    public void registerVendor(String name, String email, String pass,
                               String add, String phone, String cnic, String accNo, Uri dp,
                               VolleyCallBack volleyCallBack) {
        int ID = 0;

        //getting maximum ID
        for (int i = 0; i < vendors.size(); ++i) {
            if (vendors.get(i).getID() > ID) {
                ID = vendors.get(i).getID();
            }
        }
        ID++;

        //registering vendor
        Vendor v = new Vendor(ID, email, pass, name, add, phone, cnic, accNo, "");
        readAndWrite.insertVendorDataIntoServer(v, dp, volleyCallBack);
        vendors.add(v);
    }

    //function for hotel registration
    public void registerHotel(String name, String add, String loc, String singleRooms, String doubleRooms,
                              String singleRoomPrice, String doubleRoomPrice, String registered_by) {
        int ID = 0;

        //getting maximum ID
        for (int i = 0; i < hotels.size(); ++i) {
            if (hotels.get(i).getID() > ID) {
                ID = hotels.get(i).getID();
            }
        }
        ID++;

        //registering hotel
        Hotel h = new Hotel(ID, name, add, loc, singleRooms, doubleRooms, singleRoomPrice, doubleRoomPrice, registered_by);
        hotels.add(h);
        readAndWrite.insertHotelIntoServer(h);
        readAndWrite.insertRoomsIntoServer(h);
    }

    //function for hotel booking
    public Vector<Hotel> getHotels(String location, String noOfPersons, LocalDate checkInDate, String roomType, boolean both) {
        Vector<Hotel> searchedHotels = new Vector<>();
        for (int i = 0; i < hotels.size(); ++i) {
            if (hotels.get(i).getLocation().equals(location)) {
                Hotel h1 = new Hotel();
                h1.setAddress(hotels.get(i).getAddress());
                h1.setName(hotels.get(i).getName());
                h1.setID(hotels.get(i).getID());
                h1.setRooms(hotels.get(i).getRooms());
                h1.setTotalRooms(hotels.get(i).getTotalRooms());
                h1.setLocation(hotels.get(i).getLocation());
                h1.setDoubleRoomPrice(hotels.get(i).getDoubleRoomPrice());
                h1.setDoubleRooms(hotels.get(i).getDoubleRooms());
                h1.setSingleRooms(hotels.get(i).getSingleRooms());
                h1.setSingleRoomPrice(hotels.get(i).getSingleRoomPrice());
                h1.setReservations(hotels.get(i).getReservations());
                Vector<Room> r;
                r = hotels.get(i).getRooms(noOfPersons, checkInDate, roomType, both);
                if (r != null) {
                    h1.setRooms(r);
                    searchedHotels.add(h1);
                }
            }
        }
        return searchedHotels;
    }

    //function for reserving room
    public void makeReservation(String email, Hotel h, LocalDate checkInDate, LocalDate checkOutDate) {
        //finding customer and calling for reservation
        for (int i = 0; i < customers.size(); ++i) {
            if (customers.get(i).getEmail().equals(email)) {
                Reservation reservation = h.reserveRoom(checkInDate, checkOutDate, customers.get(i), hotels);
                readAndWrite.truncateATable("rooms", new VolleyCallBack() {
                    @Override
                    public void onSuccess() {
                        for (int j = 0; j < hotels.size(); ++j) {
                            readAndWrite.insertRoomsIntoServer(hotels.get(j));
                        }
                    }
                });
                if (reservation != null) {
                    readAndWrite.insertReservationIntoServer(reservation);
                }
                break;
            }
        }
    }

    // Search Customer On Email Basis
    public Customer searchCustomerByMail(String Email){
        for(int i=0;i<customers.size();++i){
            if(Email.equals(customers.get(i).getEmail())){
                return customers.get(i);
            }
        }
        return null;
    }
    // Search Vendor On Email Basis
    public Vendor searchVendorByMail(String Email){
        for(int i=0;i<vendors.size();++i){
            if(Email.equals(vendors.get(i).getEmail())){
                return vendors.get(i);
            }
        }
        return null;
    }
    // Search Hotel On Name and Location Basis
    public Hotel searchHotelByNameLoc(String Name,String Loc){
        for(int i=0;i<hotels.size();++i){
            if(Name.equals(hotels.get(i).getName()) && Loc.equals(hotels.get(i).getLocation())){
                return hotels.get(i);
            }
        }
        return null;
    }

    // Login Customer On Email and Password Basis
    public Boolean LoginCustomer(String Email,String Pass){
        for(int i=0;i<customers.size();++i){
            if(Email.equals(customers.get(i).getEmail()) &&  Pass.equals(customers.get(i).getPassword())){
                return true;
            }
        }
        return false;
    }
    // Login Vendor On Email and Password Basis
    public Boolean LoginVendor(String Email,String Pass){
        for(int i=0;i<vendors.size();++i){
            if(Email.equals(vendors.get(i).getEmail()) &&  Pass.equals(vendors.get(i).getPassword())){
                return true;
            }
        }
        return false;
    }
}


package com.sameetasadullah.i180479_i180531.dataLayer;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Custom request to make multipart header and upload file.
 *
 * Virtual Qube Technologies
 * Created by Ankit Prajapati on 27/0/2018 12.05.
 */
public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;
    private Map<String, String> mHeaders;

    /**
     * Default constructor with predefined header and post method.
     *
     * @param url           request destination
     * @param headers       predefined custom header
     * @param listener      on success achieved 200 code from request
     * @param errorListener on error http or library timeout
     */
    public VolleyMultipartRequest(String url, Map<String, String> headers,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
    }

    /**
     * Constructor with option method and default header configuration.
     *
     * @param method        method for now accept POST and GET only
     * @param url           request destination
     * @param listener      on success event handler
     * @param errorListener on error event handler
     */
    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // populate text payload
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                textParse(dos, params, getParamsEncoding());
            }

            // populate data byte payload
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                dataParse(dos, data);
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Custom method handle data payload.
     *
     * @return Map data part label with data byte
     * @throws AuthFailureError
     */
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    /**
     * Parse string map into data output stream by key and value.
     *
     * @param dataOutputStream data output stream handle string parsing
     * @param params           string inputs collection
     * @param encoding         encode the inputs, default UTF-8
     * @throws IOException
     */
    private void textParse(DataOutputStream dataOutputStream, Map<String, String> params, String encoding) throws IOException {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buildTextPart(dataOutputStream, entry.getKey(), entry.getValue());
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encoding, uee);
        }
    }

    /**
     * Parse data into data output stream.
     *
     * @param dataOutputStream data output stream handle file attachment
     * @param data             loop through data
     * @throws IOException
     */
    private void dataParse(DataOutputStream dataOutputStream, Map<String, DataPart> data) throws IOException {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
            buildDataPart(dataOutputStream, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Write string data into header and data output stream.
     *
     * @param dataOutputStream data output stream handle string parsing
     * @param parameterName    name of input
     * @param parameterValue   value of input
     * @throws IOException
     */
    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
        //dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    /**
     * Write data file into header and data output stream.
     *
     * @param dataOutputStream data output stream handle data parsing
     * @param dataFile         data byte as DataPart from collection
     * @param inputName        name of data input
     * @throws IOException
     */
    private void buildDataPart(DataOutputStream dataOutputStream, DataPart dataFile, String inputName) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd);
        if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: " + dataFile.getType() + lineEnd);
        }
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    /**
     * Simple data container use for passing byte file
     */
    public class DataPart {
        private String fileName;
        private byte[] content;
        private String type;

        /**
         * Default data part
         * @param name
         * @param uploads
         * @param mimeType
         */
        public DataPart(String name, File[] uploads, String mimeType) {
        }

        /**
         * Constructor with data.
         *
         * @param name label of data
         * @param data byte data
         */
        public DataPart(String name, byte[] data) {
            fileName = name;
            content = data;
        }

        /**
         * Constructor with mime data type.
         *
         * @param name     label of data
         * @param data     byte data
         * @param mimeType mime data like "image/jpeg"
         */
        public DataPart(String name, byte[] data, String mimeType) {
            fileName = name;
            content = data;
            type = mimeType;
        }

        /**
         * Getter file name.
         *
         * @return file name
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Setter file name.
         *
         * @param fileName string file name
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Getter content.
         *
         * @return byte file data
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * Setter content.
         *
         * @param content byte file data
         */
        public void setContent(byte[] content) {
            this.content = content;
        }

        /**
         * Getter mime type.
         *
         * @return mime type
         */
        public String getType() {
            return type;
        }

        /**
         * Setter mime type.
         *
         * @param type mime type
         */
        public void setType(String type) {
            this.type = type;
        }
    }
}

package com.sameetasadullah.i180479_i180531.dataLayer;

public interface VolleyCallBack {
    void onSuccess();
}

package com.sameetasadullah.i180479_i180531.dataLayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sameetasadullah.i180479_i180531.logicLayer.Customer;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Hotel;
import com.sameetasadullah.i180479_i180531.logicLayer.Reservation;
import com.sameetasadullah.i180479_i180531.logicLayer.Room;
import com.sameetasadullah.i180479_i180531.logicLayer.Vendor;
import com.sameetasadullah.i180479_i180531.presentationLayer.MyDBHelper;
import com.sameetasadullah.i180479_i180531.presentationLayer.Reservations_Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class writerAndReader {
    Context context;
    String directoryUrl = "http://192.168.18.81/PHP_Files/";

    public writerAndReader(Context context) {
        this.context = context;
    }

    @NonNull
    private byte[] getByteArray(Uri image) {
        Bitmap pic = null;
        try {
            pic = MediaStore.Images.Media.getBitmap(context.getContentResolver(), image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void insertCustomerDataIntoServer(Customer customer, Uri dp, VolleyCallBack volleyCallBack) {
        String url = directoryUrl + "insert_image.php";
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("code").equals("1")) {
                                String imageUrl = directoryUrl + obj.getString("url");
                                customer.setDp(imageUrl);
                                insertCustomerIntoServer(customer,imageUrl, volleyCallBack);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }
        ){
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();
                long imageName = System.currentTimeMillis();
                params.put("image", new DataPart(imageName + ".png", getByteArray(dp)));
                return params;
            }
        };
        Volley.newRequestQueue(context).add(volleyMultipartRequest);
    }

    private void insertCustomerIntoServer(Customer customer, String imageUrl, VolleyCallBack volleyCallBack) {
        String url = directoryUrl + "insert_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyCallBack.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "customers");
                data.put("name", customer.getName());
                data.put("email", customer.getEmail());
                data.put("password", customer.getPassword());
                data.put("phoneno", customer.getPhoneNo());
                data.put("cnic", customer.getCNIC());
                data.put("accountno", customer.getAccountNo());
                data.put("address", customer.getAddress());
                data.put("dp", imageUrl);
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void getCustomersFromServer(Vector<Customer> customers) {
        String url = directoryUrl + "get_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            if(object.getInt("reqcode")==1){
                                JSONArray data=object.getJSONArray("data");
                                for (int i=0;i<data.length();i++)
                                {
                                    customers.add(
                                            new Customer(
                                                    data.getJSONObject(i).getInt("id"),
                                                    data.getJSONObject(i).getString("email"),
                                                    data.getJSONObject(i).getString("password"),
                                                    data.getJSONObject(i).getString("name"),
                                                    data.getJSONObject(i).getString("address"),
                                                    data.getJSONObject(i).getString("phoneno"),
                                                    data.getJSONObject(i).getString("cnic"),
                                                    data.getJSONObject(i).getString("accountno"),
                                                    data.getJSONObject(i).getString("dp")
                                            )
                                    );
                                }
                            }
                            else {
                                Toast.makeText(context,
                                        "Failed to load data",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "customers");
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void insertVendorDataIntoServer(Vendor vendor, Uri dp, VolleyCallBack volleyCallBack) {
        String url = directoryUrl + "insert_image.php";
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("code").equals("1")) {
                                String imageUrl = directoryUrl + obj.getString("url");
                                vendor.setDp(imageUrl);
                                insertVendorIntoServer(vendor,
                                        imageUrl,
                                        volleyCallBack);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }
        ){
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();
                long imageName = System.currentTimeMillis();
                params.put("image", new DataPart(imageName + ".png", getByteArray(dp)));
                return params;
            }
        };
        Volley.newRequestQueue(context).add(volleyMultipartRequest);
    }

    private void insertVendorIntoServer(Vendor vendor, String imageUrl, VolleyCallBack volleyCallBack) {
        String url = directoryUrl + "insert_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyCallBack.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "vendors");
                data.put("name", vendor.getName());
                data.put("email", vendor.getEmail());
                data.put("password", vendor.getPassword());
                data.put("phoneno", vendor.getPhoneNo());
                data.put("cnic", vendor.getCNIC());
                data.put("accountno", vendor.getAccountNo());
                data.put("address", vendor.getAddress());
                data.put("dp", imageUrl);
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void getVendorsFromServer(Vector<Vendor> vendors) {
        String url = directoryUrl + "get_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            if(object.getInt("reqcode")==1){
                                JSONArray data=object.getJSONArray("data");
                                for (int i=0;i<data.length();i++)
                                {
                                    vendors.add(
                                            new Vendor(
                                                    data.getJSONObject(i).getInt("id"),
                                                    data.getJSONObject(i).getString("email"),
                                                    data.getJSONObject(i).getString("password"),
                                                    data.getJSONObject(i).getString("name"),
                                                    data.getJSONObject(i).getString("address"),
                                                    data.getJSONObject(i).getString("phoneno"),
                                                    data.getJSONObject(i).getString("cnic"),
                                                    data.getJSONObject(i).getString("accountno"),
                                                    data.getJSONObject(i).getString("dp")
                                            )
                                    );
                                }
                            }
                            else {
                                Toast.makeText(context,
                                        "Failed to load data",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "vendors");
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void insertHotelIntoServer(Hotel hotel) {
        String url = directoryUrl + "insert_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // do nothing
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "hotels");
                data.put("name", hotel.getName());
                data.put("address", hotel.getAddress());
                data.put("location", hotel.getLocation());
                data.put("single_rooms", hotel.getSingleRooms());
                data.put("double_rooms", hotel.getDoubleRooms());
                data.put("single_room_price", hotel.getSingleRoomPrice());
                data.put("double_room_price", hotel.getDoubleRoomPrice());
                data.put("registered_by", hotel.getRegistered_by());
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }



    public void getHotelsFromServer(Vector<Hotel> hotels, VolleyCallBack volleyCallBack) {
        String url = directoryUrl + "get_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @SuppressLint("Range")
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            if(object.getInt("reqcode")==1){
                                JSONArray data=object.getJSONArray("data");
                                for (int i=0;i<data.length();i++)
                                {
                                    hotels.add(
                                            new Hotel(
                                                    data.getJSONObject(i).getInt("id"),
                                                    data.getJSONObject(i).getString("name"),
                                                    data.getJSONObject(i).getString("address"),
                                                    data.getJSONObject(i).getString("location"),
                                                    data.getJSONObject(i).getString("single_rooms"),
                                                    data.getJSONObject(i).getString("double_rooms"),
                                                    data.getJSONObject(i).getString("single_room_price"),
                                                    data.getJSONObject(i).getString("double_room_price"),
                                                    data.getJSONObject(i).getString("registered_by")
                                                    )
                                    );
                                }
                                volleyCallBack.onSuccess();
                            }
                            else {
                                Toast.makeText(context,
                                        "Failed to load data fromm Server",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();

                        ///////////
                        MyDBHelper helper = new MyDBHelper(context);
                        SQLiteDatabase db = helper.getReadableDatabase();
                        String[] projection = new String[] {

                                Reservations_Store.OneRegisteration._NAME,
                                Reservations_Store.OneRegisteration._ID,
                                Reservations_Store.OneRegisteration._LOCATION,
                                Reservations_Store.OneRegisteration._ADDRESS,
                                Reservations_Store.OneRegisteration._SINGLEPRICE,
                                Reservations_Store.OneRegisteration._SINGLEROOMS,
                                Reservations_Store.OneRegisteration._DOUBLEROOMS,
                                Reservations_Store.OneRegisteration._DOUBLEPRICE,
                                Reservations_Store.OneRegisteration._REGISTEREDBY


                        };
                        String sort = Reservations_Store.OneRegisteration._ID + " ASC";
                        Cursor c =db.query(Reservations_Store.OneRegisteration.TABLENAME,projection,
                                null,
                                null,
                                null,
                                null,
                                sort
                        );

                        while(c.moveToNext())
                        {
                            int index_ID = c.getColumnIndex(Reservations_Store.OneRegisteration._ID);
                            int index_name = c.getColumnIndex(Reservations_Store.OneRegisteration._NAME);
                            int index_address = c.getColumnIndex(Reservations_Store.OneRegisteration._ADDRESS);
                            int index_location = c.getColumnIndex(Reservations_Store.OneRegisteration._LOCATION);
                            int index_singleRooms = c.getColumnIndex(Reservations_Store.OneRegisteration._SINGLEROOMS);
                            int index_doubleRooms = c.getColumnIndex(Reservations_Store.OneRegisteration._DOUBLEROOMS);
                            int index_singlePrice = c.getColumnIndex(Reservations_Store.OneRegisteration._SINGLEPRICE);
                            int index_doublePrice = c.getColumnIndex(Reservations_Store.OneRegisteration._DOUBLEPRICE);
                            int index_registeredBy = c.getColumnIndex(Reservations_Store.OneRegisteration._REGISTEREDBY);
                            hotels.add(
                                    new Hotel(
                                            c.getInt(index_ID),
                                            c.getString(index_name),
                                            c.getString(index_address),
                                            c.getString(index_location),
                                            c.getString(index_singleRooms),
                                            c.getString(index_doubleRooms),
                                            c.getString(index_singlePrice),
                                            c.getString(index_doublePrice),
                                            c.getString(index_registeredBy)
                                    )
                            );
                        }

                        volleyCallBack.onSuccess();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "hotels");
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void insertReservationIntoServer(Reservation reservation) {
        String url = directoryUrl + "insert_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // do nothing
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "reservations");
                data.put("hotel_name", reservation.getHotelName());
                data.put("hotel_location", reservation.getHotelLocation());
                data.put("total_rooms", reservation.getTotalRooms());
                data.put("room_numbers", reservation.getRoomNumbers());
                data.put("total_price", reservation.getTotalPrice());
                data.put("check_in_date", reservation.getCheckInDate());
                data.put("check_out_date", reservation.getCheckOutDate());
                data.put("reserved_by", reservation.getCustomerEmail());
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void getReservationsFromServer(Vector<Hotel> hotels) {
        String url = directoryUrl + "get_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @SuppressLint("Range")
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            if(object.getInt("reqcode")==1){
                                JSONArray data=object.getJSONArray("data");
                                for (int i=0;i<data.length();i++)
                                {
                                    String hotel_name = data.getJSONObject(i).getString("hotel_name");
                                    String hotel_location = data.getJSONObject(i).getString("hotel_location");

                                    for (int j = 0; j < hotels.size(); ++j) {
                                        if (hotels.get(j).getName().equals(hotel_name) && hotels.get(j).getLocation().equals(hotel_location)) {
                                            hotels.get(j).getReservations().add(new Reservation
                                                    (
                                                            hotel_name,
                                                            hotel_location,
                                                            data.getJSONObject(i).getString("total_rooms"),
                                                            data.getJSONObject(i).getString("room_numbers"),
                                                            data.getJSONObject(i).getString("total_price"),
                                                            data.getJSONObject(i).getString("check_in_date"),
                                                            data.getJSONObject(i).getString("check_out_date"),
                                                            data.getJSONObject(i).getString("reserved_by")
                                                    )
                                            );
                                        }
                                    }
                                }
                            }
                            else {
                                Toast.makeText(context,
                                        "Failed to load data from server",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();

                        ///////////
                        MyDBHelper helper = new MyDBHelper(context);
                        SQLiteDatabase db = helper.getReadableDatabase();
                        String[] projection = new String[] {
                                Reservations_Store.OneReservation._NAME,
                                Reservations_Store.OneReservation._LOCATION,
                                Reservations_Store.OneReservation._CHECKIN,
                                Reservations_Store.OneReservation._CHECKOUT,
                                Reservations_Store.OneReservation._ROOMS,
                                Reservations_Store.OneReservation._TOTALPRICE,
                                Reservations_Store.OneReservation._TOTALROOMS,
                                Reservations_Store.OneReservation._RESERVEDBY

                        };
                        String sort = Reservations_Store.OneReservation._NAME + " ASC";
                        Cursor c =db.query(Reservations_Store.OneReservation.TABLENAME,projection,
                                null,
                                null,
                                null,
                                null,
                                sort
                        );
                        int i=0;
                        while(c.moveToNext())
                        {
                            @SuppressLint("Range") String hotel_name = c.getString(c.getColumnIndex(Reservations_Store.OneReservation._NAME));
                            @SuppressLint("Range") String hotel_location = c.getString(c.getColumnIndex(Reservations_Store.OneReservation._NAME));

                            for (int j = 0; j < hotels.size(); ++j) {
                                if (hotels.get(j).getName().equals(hotel_name) && hotels.get(j).getLocation().equals(hotel_location)) {
                                    int index_totalRooms = c.getColumnIndex(Reservations_Store.OneReservation._TOTALROOMS);
                                    int index_rooms = c.getColumnIndex(Reservations_Store.OneReservation._ROOMS);
                                    int index_totalPrice = c.getColumnIndex(Reservations_Store.OneReservation._TOTALPRICE);
                                    int index_checkIn = c.getColumnIndex(Reservations_Store.OneReservation._CHECKIN);
                                    int index_checkOut = c.getColumnIndex(Reservations_Store.OneReservation._CHECKOUT);
                                    int index_reservedBy = c.getColumnIndex(Reservations_Store.OneReservation._RESERVEDBY);
                                    hotels.get(j).getReservations().add(new Reservation
                                            (
                                                    hotel_name,
                                                    hotel_location,
                                                    c.getString(index_totalRooms),
                                                    c.getString(index_rooms),
                                                    c.getString(index_totalPrice),
                                                    c.getString(index_checkIn),
                                                    c.getString(index_checkOut),
                                                    c.getString(index_reservedBy)
                                            )
                                    );
                                }
                            }
                            i+=1;
                        }
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "reservations");
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void insertRoomsIntoServer(Hotel hotel) {
        String url = directoryUrl + "insert_data.php";
        for (int i = 0; i < hotel.getRooms().size(); ++i) {
            Room room = hotel.getRooms().get(i);
            StringRequest request=new StringRequest(
                    Request.Method.POST,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // do nothing
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context,
                                    error.toString(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            ){
                protected Map<String,String> getParams()
                {
                    Map<String,String> data=new HashMap<String,String>();
                    data.put("tableName", "rooms");
                    data.put("hotel_id", String.valueOf(hotel.getID()));
                    data.put("roomno", String.valueOf(room.getNumber()));
                    data.put("type", room.getType());
                    if (room.getAvailableDate() == null) {
                        data.put("available_date", "null");
                    } else {
                        data.put("available_date", room.getAvailableDate().toString());
                    }
                    data.put("is_available", String.valueOf(room.isAvailable()));
                    return data;
                }
            };
            Volley.newRequestQueue(context).add(request);
        }
    }

    public void getRoomsFromServer(Hotel hotel) {
        String url = directoryUrl + "get_data.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            if(object.getInt("reqcode")==1){
                                JSONArray data=object.getJSONArray("data");
                                Vector<Room> rooms = new Vector<>();
                                for (int i=0;i<data.length();i++)
                                {
                                    int hotel_id = data.getJSONObject(i).getInt("hotel_id");
                                    if (hotel_id == hotel.getID()) {
                                        Room r = new Room(
                                                data.getJSONObject(i).getInt("roomno"),
                                                data.getJSONObject(i).getString("type")
                                        );
                                        String date = data.getJSONObject(i).getString("available_date");
                                        if (date.equals("null")) {
                                            r.setAvailableDate(null);
                                        } else {
                                            r.setAvailableDate(LocalDate.parse(date));
                                        }
                                        r.setAvailable(Boolean.parseBoolean(data.getJSONObject(i).getString("is_available")));
                                        rooms.add(r);
                                    }
                                }
                                hotel.setRooms(rooms);
                            }
                            else {
                                Toast.makeText(context,
                                        "Failed to load data",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", "rooms");
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    public void truncateATable(String tableName, VolleyCallBack volleyCallBack) {
        String url = directoryUrl + "truncate_table.php";
        StringRequest request=new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyCallBack.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        ){
            protected Map<String,String> getParams()
            {
                Map<String,String> data=new HashMap<String,String>();
                data.put("tableName", tableName);
                return data;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }
}


package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Hotel;

import java.util.ArrayList;
import java.util.List;

public class Registered_Hotels_Screen extends AppCompatActivity {
    RecyclerView rv;
    List<Hotel_Registraion_row> ls;
    ImageView backButton;
    HRS hrs;
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_hotels_screen);

        rv = findViewById(R.id.rv);
        backButton = findViewById(R.id.back_button);
        searchEditText = findViewById(R.id.search_edit_text);
        ls = new ArrayList<>();
        hrs = HRS.getInstance(Registered_Hotels_Screen.this);

        String email = getIntent().getStringExtra("email");

        for (Hotel hotel : hrs.getHotels()) {
            if (hotel.getRegistered_by().equals(email)) {
                ls.add(new Hotel_Registraion_row(hotel.getName(), hotel.getLocation(),
                        hotel.getSingleRoomPrice(), hotel.getDoubleRoomPrice()));
            }
        }

        //Adapter
        Hotel_Registration_adapter adapter = new Hotel_Registration_adapter(ls,this);
        RecyclerView.LayoutManager lm =new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        rv.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sameetasadullah.i180479_i180531.R;

import java.util.ArrayList;
import java.util.List;

public class Hotel_Registration_adapter
        extends RecyclerView.Adapter<Hotel_Registration_adapter.Hotel_Registration_Holder>
        implements Filterable {

    List<Hotel_Registraion_row> ls;
    List<Hotel_Registraion_row> filteredList;
    Context c;

    public Hotel_Registration_adapter(List<Hotel_Registraion_row> ls, Context c) {
        this.c=c;
        this.ls=ls;
        this.filteredList = ls;
    }

    @NonNull
    @Override
    public Hotel_Registration_adapter.Hotel_Registration_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(c).inflate(R.layout.hotel_registration_row,parent,false);
        return new Hotel_Registration_Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Hotel_Registration_adapter.Hotel_Registration_Holder holder, int position) {
        holder.name.setText(filteredList.get(position).getName());
        holder.singlePrice.setText(filteredList.get(position).getSinglePrice());
        holder.doublePrice.setText(filteredList.get(position).getDoublePrice());
        holder.location.setText(filteredList.get(position).getLocation());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String Key = constraint.toString();
                if(Key.isEmpty()){
                    filteredList = ls;
                }
                else{
                    List<Hotel_Registraion_row> listFiltered = new ArrayList<>();
                    for (Hotel_Registraion_row row: ls){
                        if(row.getName().toLowerCase().contains(Key.toLowerCase())){
                            listFiltered.add(row);
                        }
                    }
                    filteredList = listFiltered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList =  (List<Hotel_Registraion_row>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class Hotel_Registration_Holder extends RecyclerView.ViewHolder {
        TextView name,singlePrice,doublePrice,location;
        public Hotel_Registration_Holder(@NonNull View itemView){
            super(itemView);
            name =itemView.findViewById(R.id.name);
            singlePrice= itemView.findViewById(R.id.single_price);
            doublePrice=itemView.findViewById(R.id.double_price);
            location=itemView.findViewById(R.id.Location_hotel);
        }
    }
}



package com.sameetasadullah.i180479_i180531.presentationLayer;

public class Hotel_Registraion_row {

    private String name,singlePrice,doublePrice,location;
    //private int hotelImage;
    public Hotel_Registraion_row(String name,String location,String singlePrice,String doublePrice) {
        this.name = name;
        this.location= location;
        this.singlePrice=singlePrice;
        this.doublePrice=doublePrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getSinglePrice() {return singlePrice;}
    public String getDoublePrice() {return doublePrice;}

    public void setSinglePrice(String sp){this.singlePrice=sp;}
    public void setDoublePrice(String dp){this.doublePrice=dp;}

    public String getLocation(){return location;}
    public void setLocation(String location){
        this.location=location;
    }

}


package com.sameetasadullah.i180479_i180531.presentationLayer;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Hotel;

public class Reserve_Screen extends AppCompatActivity {

    ImageView backbutton;
    EditText location,persons,checkinDate,checkoutDate;
    CheckBox single_room,double_room;
    RelativeLayout submitButton;
    HRS hrs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_screen);

        backbutton=findViewById(R.id.back_button);
        location = findViewById(R.id.Location_text);
        persons = findViewById(R.id.Persons_text);
        checkinDate = findViewById(R.id.Check_in_date);
        checkoutDate = findViewById(R.id.Check_out_date);
        submitButton = findViewById(R.id.submit_button);
        single_room = findViewById(R.id.Single_box);
        double_room = findViewById(R.id.Double_box);
        hrs= HRS.getInstance(Reserve_Screen.this);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String Location,Persons,CheckinDate,CheckoutDate;
                Location = location.getText().toString();
                Persons = persons.getText().toString();
                CheckinDate = checkinDate.getText().toString();
                CheckoutDate = checkoutDate.getText().toString();
                if(Location.equals("") || Persons.equals("") || CheckinDate.equals("") || CheckoutDate.equals("") ||
                        (single_room.isChecked()==Boolean.FALSE && double_room.isChecked()==Boolean.FALSE)){
                    Toast.makeText(Reserve_Screen.this,"Fill All Boxes Correctly.",Toast.LENGTH_LONG).show();
                }
                else {
                    Boolean single, doub, both;
                    both = Boolean.FALSE;
                    single = Boolean.FALSE;
                    doub = Boolean.FALSE;
                    String TypeRoom="";
                    if (single_room.isChecked()) {
                        single = Boolean.TRUE;
                        TypeRoom="Single";
                    }
                    if (double_room.isChecked()) {
                        doub = Boolean.TRUE;
                        TypeRoom="Double";
                    }
                    if (single && doub) {
                        both = Boolean.TRUE;
                        TypeRoom="";
                    }
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate localDate = LocalDate.parse(CheckinDate, formatter);
                        LocalDate localDate1 = LocalDate.parse(CheckoutDate, formatter);

                        Vector<Hotel> hotels=hrs.getHotels(Location,Persons,localDate,TypeRoom,both);
                        if (hotels.isEmpty()==Boolean.TRUE){
                            Toast.makeText(Reserve_Screen.this,"No Hotels Found",Toast.LENGTH_LONG).show();
                        }
                        else {
                            Intent intent = new Intent(Reserve_Screen.this, Hotel_Selection.class);
                            intent.putExtra("Location",Location);
                            intent.putExtra("Persons",Persons);
                            intent.putExtra("localDate",CheckinDate);
                            intent.putExtra("checkoutDate",CheckoutDate);
                            intent.putExtra("TypeRoom",TypeRoom);
                            intent.putExtra("both",both);
                            intent.putExtra("Email",getIntent().getStringExtra("email"));
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Toast.makeText(Reserve_Screen.this,"Kindly enter dates in correct format (dd/MM/yyyy)",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.Customer;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import de.hdodenhof.circleimageview.CircleImageView;

public class Customer_Choose_Option_Screen extends AppCompatActivity {
    RelativeLayout reserve_hotel, view_old_reservations;
    CircleImageView dp;
    HRS hrs;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_choose_option_screen);

        reserve_hotel = findViewById(R.id.rl_reserve_hotel_button);
        view_old_reservations = findViewById(R.id.rl_view_old_reservations);
        dp = findViewById(R.id.display_pic);
        hrs = HRS.getInstance(Customer_Choose_Option_Screen.this);

        sharedPreferences = getSharedPreferences("com.sameetasadullah.i180479_180531", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String email = getIntent().getStringExtra("email");
        for(Customer customer : hrs.getCustomers()) {
            if (customer.getEmail().equals(email)) {
                Picasso.get().load(customer.getDp()).into(dp);
            }
        }

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Customer_Choose_Option_Screen.this)
                        .setTitle("Log out")
                        .setMessage("Are you sure you want to log out?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editor.putBoolean("loggedIn", false);
                                editor.commit();
                                editor.apply();
                                Intent intent = new Intent(Customer_Choose_Option_Screen.this, Splash_Screen.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        reserve_hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Customer_Choose_Option_Screen.this, Reserve_Screen.class);
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });
        view_old_reservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Customer_Choose_Option_Screen.this, Reservations_Screen.class);
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.dataLayer.VolleyCallBack;
import com.sameetasadullah.i180479_i180531.dataLayer.writerAndReader;
import com.sameetasadullah.i180479_i180531.logicLayer.Customer;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Vendor;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register_Screen extends AppCompatActivity {

    String Page;
    ImageView backButton, addDisplayPic;
    CircleImageView dp;
    EditText name,email,contact,card,cnic,address,password;
    RelativeLayout signup_Button;
    HRS hrs;
    Uri imageURI = null;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        Page = getIntent().getStringExtra("Page");
        backButton = findViewById(R.id.back_button);
        name = findViewById(R.id.Name_text);
        email = findViewById(R.id.Email_text);
        contact = findViewById(R.id.Contact_text);
        cnic = findViewById(R.id.CNIC_text);
        address = findViewById(R.id.Address_text);
        password = findViewById(R.id.Password_text);
        card = findViewById(R.id.Card_text);
        signup_Button = findViewById(R.id.sign_up_button);
        hrs = HRS.getInstance(Register_Screen.this);
        addDisplayPic = findViewById(R.id.add_display_pic);
        dp = findViewById(R.id.display_pic);

        sharedPreferences = getSharedPreferences("com.sameetasadullah.i180479_180531", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        signup_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Name=name.getText().toString();
                String Cnic=cnic.getText().toString();
                String Email=email.getText().toString();
                String Contact=contact.getText().toString();
                String Address=address.getText().toString();
                String Password=password.getText().toString();
                String Card=card.getText().toString();

                if(Name.equals("") ||Cnic.equals("") ||Password.equals("") ||Card.equals("") ||Address.equals("") ||Contact.equals("") ||Email.equals("")  ){
                    Toast.makeText(Register_Screen.this,"Please Fill All Blocks",Toast.LENGTH_LONG).show();
                }
                else if (imageURI == null) {
                    Toast.makeText(Register_Screen.this,
                            "Please select image", Toast.LENGTH_LONG).show();
                }
                else {
                    if (Page.equals("Customer")){
                        if (!hrs.validateCustomerEmail(Email)){
                            Toast.makeText(Register_Screen.this,"Account with this Email / Phone no Already Exists",Toast.LENGTH_LONG).show();
                        }
                        else {
                            ProgressDialog pd=new ProgressDialog(Register_Screen.this);
                            pd.setMessage("Loading");
                            pd.setCancelable(false);
                            pd.show();

                            hrs.registerCustomer(Name, Email, Password, Address, Contact, Cnic, Card, imageURI, new VolleyCallBack() {
                                @Override
                                public void onSuccess() {
                                    pd.dismiss();
                                    editor.putString("user", "Customer");
                                    editor.putString("email", Email);
                                    editor.putBoolean("loggedIn", true);
                                    editor.commit();
                                    editor.apply();
                                    Intent intent=new Intent(Register_Screen.this,Customer_Choose_Option_Screen.class);
                                    intent.putExtra("email", Email);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }
                    else {
                        if (!hrs.validateVendorEmail(Email)){
                            Toast.makeText(Register_Screen.this,"Account with this Email / Phone no Already Exists",Toast.LENGTH_LONG).show();
                        }
                        else {
                            ProgressDialog pd=new ProgressDialog(Register_Screen.this);
                            pd.setMessage("Loading");
                            pd.setCancelable(false);
                            pd.show();
                            hrs.registerVendor(Name,Email,Password,Address,Contact,Cnic,Card,imageURI, new VolleyCallBack(){
                                @Override
                                public void onSuccess() {
                                    pd.dismiss();
                                    editor.putString("user", "Vendor");
                                    editor.putString("email", Email);
                                    editor.putBoolean("loggedIn", true);
                                    editor.commit();
                                    editor.apply();
                                    Intent intent=new Intent(Register_Screen.this,Vendor_Choose_Option_Screen.class);
                                    intent.putExtra("email", Email);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }
                }
            }
        });
        addDisplayPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            addDisplayPic.setAlpha((float)0);
            imageURI = data.getData();
            dp.setImageURI(imageURI);
        }
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import android.widget.ImageView;

public class Hotel_Selection_row {

    private String name,singlePrice,doublePrice,location;
    //private int hotelImage;
    public Hotel_Selection_row(String name,String location,String singlePrice,String doublePrice) {
        this.name = name;
        this.location= location;
        this.singlePrice=singlePrice;
        this.doublePrice=doublePrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getSinglePrice() {return singlePrice;}
    public String getDoublePrice() {return doublePrice;}

    public void setSinglePrice(String sp){this.singlePrice=sp;}
    public void setDoublePrice(String dp){this.doublePrice=dp;}

    public String getLocation(){return location;}
    public void setLocation(String location){
        this.location=location;
    }
}


package com.sameetasadullah.i180479_i180531.presentationLayer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.service.chooser.ChooserTarget;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.Hotel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

public class Hotel_Selection_adapter extends RecyclerView.Adapter<Hotel_Selection_adapter.Hotel_Selection_Holder>{
    List<Hotel_Selection_row> ls;
    Context c;
    Vector<Hotel> hotels;

    public Hotel_Selection_adapter(List<Hotel_Selection_row> ls, Context c, Vector<Hotel> hotels) {
        this.c=c;
        this.ls=ls;
        this.hotels = hotels;
    }

    @NonNull
    @Override
    public Hotel_Selection_adapter.Hotel_Selection_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(c).inflate(R.layout.hotel_selection_row,parent,false);
        return new Hotel_Selection_Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Hotel_Selection_adapter.Hotel_Selection_Holder holder, int position) {
        holder.name.setText(ls.get(position).getName());
        holder.singlePrice.setText(ls.get(position).getSinglePrice());
        holder.doublePrice.setText(ls.get(position).getDoublePrice());
        holder.location.setText(ls.get(position).getLocation());
        holder.l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(c)
                        .setTitle("Confirm Booking")
                        .setMessage("Are you sure you want to Reserve this Hotel?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                Intent intent = new Intent(c,Hotel_Reservation_Screen.class);
                                intent.putExtra("Email",((Hotel_Selection)c).Email);
                                intent.putExtra("Hotel_name",ls.get(holder.getAdapterPosition()).getName() );
                                intent.putExtra("Hotel_Loc",ls.get(holder.getAdapterPosition()).getLocation() );
                                intent.putExtra("checkinDate",((Hotel_Selection)c).checkInDate);
                                intent.putExtra("checkOutDate",((Hotel_Selection)c).checkOutDate);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                LocalDate localDate = LocalDate.parse(((Hotel_Selection)c).checkInDate, formatter);
                                LocalDate localDate1 = LocalDate.parse(((Hotel_Selection)c).checkOutDate, formatter);


                                ((Hotel_Selection)c).hrs.makeReservation(((Hotel_Selection)c).Email,hotels.get(holder.getAdapterPosition()),localDate,localDate1);
                                c.startActivity(intent);
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
       // holder.hotelImage.setImageResource(ls.get(position).getHotelImage());
    }

    @Override
    public int getItemCount() {

        return ls.size();
    }



    public class Hotel_Selection_Holder extends RecyclerView.ViewHolder {
        TextView name,singlePrice,doublePrice,location;
        LinearLayout l1;
      //  ImageView hotelImage;
        public Hotel_Selection_Holder(@NonNull View itemView){
            super(itemView);
            name =itemView.findViewById(R.id.name);
            singlePrice= itemView.findViewById(R.id.single_price);
            doublePrice=itemView.findViewById(R.id.double_price);
            location=itemView.findViewById(R.id.Location_hotel);
            l1=itemView.findViewById(R.id.hotel_sel_row_id);

            //  hotelImage =itemView.findViewById(R.id.image_hotel);
        }
    }
}


package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Hotel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Hotel_Selection extends AppCompatActivity {

    RecyclerView rv;
    List<Hotel_Selection_row> ls;
    HRS hrs;
    String Location,Persons,checkInDate,TypeRoom,Email,checkOutDate;
    boolean both;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_selection);

        backButton = findViewById(R.id.back_button);
        rv = findViewById(R.id.rv);
        ls = new ArrayList<>();
        hrs= HRS.getInstance(Hotel_Selection.this);
        Location = getIntent().getStringExtra("Location");
        Persons =getIntent().getStringExtra("Persons");
        checkInDate=getIntent().getStringExtra("localDate");
        checkOutDate=getIntent().getStringExtra("checkoutDate");
        TypeRoom=getIntent().getStringExtra("TypeRoom");
        Email = getIntent().getStringExtra("Email");
        Bundle bundle =getIntent().getExtras();
         both = bundle.getBoolean("both");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(checkInDate, formatter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Vector<Hotel> hotels=hrs.getHotels(Location,Persons,localDate,TypeRoom,both);
        for (int i=0; i < hotels.size();i++){
            ls.add(new Hotel_Selection_row(hotels.get(i).getName(),hotels.get(i).getLocation(),hotels.get(i).getSingleRoomPrice(),hotels.get(i).getDoubleRoomPrice()));
        }

        //Adapter
        Hotel_Selection_adapter adapter = new Hotel_Selection_adapter(ls,this, hotels);
        RecyclerView.LayoutManager lm =new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        rv.setAdapter(adapter);

    }




}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Reservation;

import java.util.ArrayList;
import java.util.List;

public class Reservations_Screen extends AppCompatActivity {


    RecyclerView rv;
    List<Hotel_row> ls;
    HRS hrs;
    ImageView back_button;
    EditText searchEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations_screen);

        rv = findViewById(R.id.rv);
        searchEditText = findViewById(R.id.search_edit_text);
        back_button = findViewById(R.id.back_button);
        ls = new ArrayList<>();
        hrs = HRS.getInstance(Reservations_Screen.this);

        String email = getIntent().getStringExtra("email");

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        for (int i = 0; i < hrs.getHotels().size(); ++i) {
            for (int j = 0; j < hrs.getHotels().get(i).getReservations().size(); ++j) {
                Reservation reservation = hrs.getHotels().get(i).getReservations().get(j);
                if (reservation.getCustomerEmail().equals(email)) {
                    ls.add(new Hotel_row(reservation.getHotelName(),
                            reservation.getHotelLocation(), reservation.getRoomNumbers(),
                            reservation.getCheckInDate(), reservation.getCheckOutDate()));
                }
            }
        }

        //Adapter
        Hotel_row_adapter adapter = new Hotel_row_adapter(ls,this);
        RecyclerView.LayoutManager lm =new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        rv.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Login_Screen extends AppCompatActivity {
    ImageView backButton;
    EditText email, password;
    RelativeLayout loginButton;
    HRS hrs;
    TextView page_user, sign_up;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        backButton = findViewById(R.id.back_button);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.log_in_button);
        page_user = findViewById(R.id.tv_page);
        sign_up = findViewById(R.id.tv_sign_up);
        hrs = HRS.getInstance(Login_Screen.this);

        sharedPreferences = getSharedPreferences("com.sameetasadullah.i180479_180531", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String page = getIntent().getStringExtra("Page");
        page_user.setText(page);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_string = email.getText().toString();
                String password_string = password.getText().toString();

                if (email_string.equals("") || password_string.equals("")) {
                    Toast.makeText(Login_Screen.this, "Kindly fill all the input fields", Toast.LENGTH_LONG).show();
                }
                else {
                    if (page.equals("Vendor")) {
                        if (hrs.validateVendorAccount(email_string, password_string)) {
                            editor.putString("user", "Vendor");
                            editor.putString("email", email_string);
                            editor.putBoolean("loggedIn", true);
                            editor.commit();
                            editor.apply();
                            Intent intent = new Intent(Login_Screen.this, Vendor_Choose_Option_Screen.class);
                            intent.putExtra("email", email_string);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(Login_Screen.this, "Incorrect email and password", Toast.LENGTH_LONG).show();
                        }
                    }
                    else if (page.equals("Customer")) {
                        if (hrs.validateCustomerAccount(email_string, password_string)) {
                            editor.putString("user", "Customer");
                            editor.putString("email", email_string);
                            editor.putBoolean("loggedIn", true);
                            editor.commit();
                            editor.apply();
                            Intent intent = new Intent(Login_Screen.this, Customer_Choose_Option_Screen.class);
                            intent.putExtra("email", email_string);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(Login_Screen.this, "Incorrect email and password", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
        });
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login_Screen.this, Register_Screen.class);
                intent.putExtra("Page", page);
                startActivity(intent);
            }
        });
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sameetasadullah.i180479_i180531.R;

import java.util.ArrayList;
import java.util.List;

public class Hotel_row_adapter
        extends RecyclerView.Adapter<Hotel_row_adapter.Hotel_row_Holder>
        implements Filterable {

    List<Hotel_row> ls;
    List<Hotel_row> filteredList;
    Context c;

    public Hotel_row_adapter(List<Hotel_row> ls, Context c) {
        this.c=c;
        this.ls=ls;
        this.filteredList = ls;
    }

    @NonNull
    @Override
    public Hotel_row_adapter.Hotel_row_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(c).inflate(R.layout.hotel_row,parent,false);
        return new Hotel_row_Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Hotel_row_adapter.Hotel_row_Holder holder, int position) {
        holder.name.setText(filteredList.get(position).getName());
        holder.singlePrice.setText(filteredList.get(position).getSinglePrice());
        holder.doublePrice.setText(filteredList.get(position).getDoublePrice());
        holder.location.setText(filteredList.get(position).getLocation());
        holder.check_out_date.setText(filteredList.get(position).getCheck_out_date());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String Key = constraint.toString();
                if(Key.isEmpty()){
                    filteredList = ls;
                }
                else{
                    List<Hotel_row> listFiltered = new ArrayList<>();
                    for (Hotel_row row: ls){
                        if(row.getName().toLowerCase().contains(Key.toLowerCase())){
                            listFiltered.add(row);
                        }
                    }
                    filteredList = listFiltered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList =  (List<Hotel_row>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class Hotel_row_Holder extends RecyclerView.ViewHolder {
        TextView name,singlePrice,doublePrice,location,check_out_date;
        public Hotel_row_Holder(@NonNull View itemView){
            super(itemView);
            name =itemView.findViewById(R.id.name);
            singlePrice= itemView.findViewById(R.id.single_price);
            doublePrice=itemView.findViewById(R.id.double_price);
            location=itemView.findViewById(R.id.Location_hotel);
            check_out_date=itemView.findViewById(R.id.check_out_date);
        }
    }
}



package com.sameetasadullah.i180479_i180531.presentationLayer;

import android.provider.BaseColumns;

public class Reservations_Store {
    public static String DB_NAME="myReservations.db";
    public static int DB_VERSION=1;

    public static class OneReservation implements BaseColumns {
        public static String TABLENAME="ReservationsTable";
        public static String _NAME="name";
        public static String _LOCATION="location";
        public static String _CHECKIN ="checkin";
        public static String _CHECKOUT ="checkout";
        public static String _TOTALROOMS ="total_rooms";
        public static String _TOTALPRICE ="total_price";
        public static String _ROOMS ="rooms";
        public static String _RESERVEDBY ="reserved_by";
    }

    public static class OneRegisteration implements BaseColumns {
        public static String TABLENAME="RegisteredHotelTable";
        public static String _NAME="name";
        public static String _ADDRESS="address";
        public static String _LOCATION ="location";
        public static String _SINGLEROOMS ="single_rooms";
        public static String _DOUBLEROOMS ="double_rooms";
        public static String _SINGLEPRICE ="single_room_price";
        public static String _DOUBLEPRICE ="double_room_price";
        public static String _REGISTEREDBY ="registered_by";
    }



}


package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.dataLayer.writerAndReader;
import com.sameetasadullah.i180479_i180531.logicLayer.Customer;
import com.sameetasadullah.i180479_i180531.logicLayer.Hotel;
import com.sameetasadullah.i180479_i180531.logicLayer.Room;
import com.sameetasadullah.i180479_i180531.logicLayer.Vendor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Main_Screen extends AppCompatActivity {


    RelativeLayout customer;
    RelativeLayout vendor;
    String Page;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        customer = findViewById(R.id.rl_customer_button);
        vendor = findViewById(R.id.rl_vendor_button);

        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page= "Customer";
                Intent intent=new Intent(Main_Screen.this,Login_Screen.class);
                intent.putExtra("Page",Page);
                startActivity(intent);
            }
        });
        vendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page= "Vendor";
                Intent intent=new Intent(Main_Screen.this,Login_Screen.class);
                intent.putExtra("Page",Page);
                startActivity(intent);
            }
        });
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;


public class Splash_Screen extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    Boolean loggedIn;
    String user, email;
    HRS hrs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sharedPreferences = getSharedPreferences("com.sameetasadullah.i180479_180531", MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
        user = sharedPreferences.getString("user", "");
        email  = sharedPreferences.getString("email", "");

        hrs = HRS.getInstance(Splash_Screen.this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = null;
                if (!loggedIn) {
                    intent = new Intent(Splash_Screen.this, Main_Screen.class);
                }
                else {
                    if (user.equals("Customer")) {
                        intent = new Intent(Splash_Screen.this, Customer_Choose_Option_Screen.class);
                    }
                    else if (user.equals("Vendor")) {
                        intent = new Intent(Splash_Screen.this, Vendor_Choose_Option_Screen.class);
                    }
                    assert intent != null;
                    intent.putExtra("email", email);
                }
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

public class Hotel_row {

    private String name,singlePrice,doublePrice,location, check_out_date;
    //private int hotelImage;
    public Hotel_row(String name,String location,String singlePrice,String doublePrice, String check_out_date) {
        this.name = name;
        this.location= location;
        this.singlePrice=singlePrice;
        this.doublePrice=doublePrice;
        this.check_out_date = check_out_date;
    }

    public String getCheck_out_date() {
        return check_out_date;
    }

    public void setCheck_out_date(String check_out_date) {
        this.check_out_date = check_out_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getSinglePrice() {return singlePrice;}
    public String getDoublePrice() {return doublePrice;}

    public void setSinglePrice(String sp){this.singlePrice=sp;}
    public void setDoublePrice(String dp){this.doublePrice=dp;}

    public String getLocation(){return location;}
    public void setLocation(String location){
        this.location=location;
    }

}


package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;

public class Hotel_Registration_Screen extends AppCompatActivity {
    EditText hotelName, hotelAdd, hotelLoc, totalSingleRooms,
            totalDoubleRooms, singleRoomPrice, doubleRoomPrice;
    RelativeLayout registerButton;
    HRS hrs;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_registration_screen);

        hotelName = findViewById(R.id.tv_hotel_name);
        hotelAdd = findViewById(R.id.tv_hotel_address);
        hotelLoc = findViewById(R.id.tv_hotel_loc);
        backButton = findViewById(R.id.back_button);
        totalSingleRooms = findViewById(R.id.tv_single_rooms);
        singleRoomPrice = findViewById(R.id.tv_single_price);
        totalDoubleRooms = findViewById(R.id.tv_double_rooms);
        doubleRoomPrice = findViewById(R.id.tv_double_price);
        registerButton = findViewById(R.id.register_button);
        hrs = HRS.getInstance(Hotel_Registration_Screen.this);

        String email = getIntent().getStringExtra("email");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hotelName_string = hotelName.getText().toString();
                String hotelAdd_string = hotelAdd.getText().toString();
                String hotelLoc_string = hotelLoc.getText().toString();
                String totalSingleRooms_string = totalSingleRooms.getText().toString();
                String singleRoomPrice_string = singleRoomPrice.getText().toString();
                String totalDoubleRooms_string = totalDoubleRooms.getText().toString();
                String doubleRoomPrice_string = doubleRoomPrice.getText().toString();

                if (hotelName_string.equals("") || hotelAdd_string.equals("") ||
                        hotelLoc_string.equals("") || totalSingleRooms_string.equals("") ||
                        singleRoomPrice_string.equals("") || totalDoubleRooms_string.equals("") ||
                        doubleRoomPrice_string.equals("")) {
                    Toast.makeText(Hotel_Registration_Screen.this,"Please Fill All Blocks",Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        Integer.parseInt(totalSingleRooms_string);
                        Integer.parseInt(totalDoubleRooms_string);
                        Integer.parseInt(singleRoomPrice_string);
                        Integer.parseInt(doubleRoomPrice_string);

                        if (hrs.validateHotel(hotelName_string, hotelLoc_string)) {
                            hrs.registerHotel(hotelName_string, hotelAdd_string, hotelLoc_string,
                                    totalSingleRooms_string, totalDoubleRooms_string,
                                    singleRoomPrice_string, doubleRoomPrice_string, email);
                            Toast.makeText(Hotel_Registration_Screen.this,"Registered Successfully",Toast.LENGTH_LONG).show();
                            //SQL LITE
                            MyDBHelper helper = new MyDBHelper(Hotel_Registration_Screen.this);
                            SQLiteDatabase database = helper.getWritableDatabase();
                            ContentValues cv = new ContentValues();
                            cv.put(Reservations_Store.OneRegisteration._NAME,hotelName_string);
                            cv.put(Reservations_Store.OneRegisteration._LOCATION,hotelLoc_string);
                            cv.put(Reservations_Store.OneRegisteration._ADDRESS,hotelAdd_string);
                            cv.put(Reservations_Store.OneRegisteration._SINGLEROOMS,totalSingleRooms_string);
                            cv.put(Reservations_Store.OneRegisteration._DOUBLEROOMS,totalDoubleRooms_string);
                            cv.put(Reservations_Store.OneRegisteration._SINGLEPRICE,singleRoomPrice_string);
                            cv.put(Reservations_Store.OneRegisteration._DOUBLEPRICE,doubleRoomPrice_string);
                            cv.put(Reservations_Store.OneRegisteration._REGISTEREDBY,email);
                            double tep = database.insert(Reservations_Store.OneRegisteration.TABLENAME,null,cv);
                            database.close();
                            helper.close();
                            onBackPressed();
                        }
                        else {
                            Toast.makeText(Hotel_Registration_Screen.this,"Hotel with this name and location already exists",Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(Hotel_Registration_Screen.this,"Kindly enter integer values where required",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {
    String CREATE_RESERVATION_TABLE="CREATE TABLE " +
            Reservations_Store.OneReservation.TABLENAME + "("+
            Reservations_Store.OneReservation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "+
            Reservations_Store.OneReservation._NAME + " TEXT NOT NULL, "+
            Reservations_Store.OneReservation._LOCATION + " TEXT , "+
            Reservations_Store.OneReservation._TOTALROOMS + " TEXT , "+
            Reservations_Store.OneReservation._TOTALPRICE + " TEXT , "+
            Reservations_Store.OneReservation._RESERVEDBY + " TEXT , "+
            Reservations_Store.OneReservation._CHECKIN + " TEXT ," +
            Reservations_Store.OneReservation._ROOMS + " TEXT ,"+
            Reservations_Store.OneReservation._CHECKOUT + " TEXT );";

    String DELETE_RESERVATION_TABLE="DROP TABLE IF EXISTS "+Reservations_Store.OneReservation.TABLENAME;


    String CREATE_REGISTRATION_TABLE="CREATE TABLE " +
            Reservations_Store.OneRegisteration.TABLENAME + "("+
            Reservations_Store.OneRegisteration._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "+
            Reservations_Store.OneRegisteration._NAME + " TEXT NOT NULL, "+
            Reservations_Store.OneRegisteration._LOCATION + " TEXT , "+
            Reservations_Store.OneRegisteration._ADDRESS + " TEXT , "+
            Reservations_Store.OneRegisteration._SINGLEROOMS + " TEXT , "+
            Reservations_Store.OneRegisteration._SINGLEPRICE + " TEXT , "+
            Reservations_Store.OneRegisteration._DOUBLEPRICE + " TEXT ," +
            Reservations_Store.OneRegisteration._DOUBLEROOMS + " TEXT ,"+
            Reservations_Store.OneRegisteration._REGISTEREDBY + " TEXT );";

    String DELETE_REGISTRATION_TABLE="DROP TABLE IF EXISTS "+Reservations_Store.OneRegisteration.TABLENAME;



    public MyDBHelper(@Nullable Context context) {
        super(context, Reservations_Store.DB_NAME,null,Reservations_Store.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_RESERVATION_TABLE);
        sqLiteDatabase.execSQL(CREATE_REGISTRATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_RESERVATION_TABLE);
        sqLiteDatabase.execSQL(DELETE_REGISTRATION_TABLE);
        onCreate(sqLiteDatabase);
    }
}


package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Hotel;
import com.sameetasadullah.i180479_i180531.logicLayer.Reservation;
import com.sameetasadullah.i180479_i180531.logicLayer.Room;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class Hotel_Reservation_Screen extends AppCompatActivity {

    RelativeLayout endButton;
    TextView hotelName,rooms,totalPrice,totalRooms;
    HRS hrs;
    String Email,checkInDate,checkOutDate,HotelName,HotelLocation;
    Hotel h1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_reservation_screen);

        hotelName = findViewById(R.id.tv_hotel_name);
        rooms = findViewById(R.id.tv_rooms);
        totalPrice = findViewById(R.id.tv_total_price);
        totalRooms = findViewById(R.id.tv_total_rooms);
        endButton = findViewById(R.id.END_button);
        hrs = HRS.getInstance(Hotel_Reservation_Screen.this);

        Email = getIntent().getStringExtra("Email");
        HotelName = getIntent().getStringExtra("Hotel_name");
        HotelLocation = getIntent().getStringExtra("Hotel_Loc");
        checkInDate = getIntent().getStringExtra("checkinDate");
        checkOutDate = getIntent().getStringExtra("checkOutDate");


        h1 = hrs.searchHotelByNameLoc(HotelName,HotelLocation);

        Vector<Reservation> res= h1.getReservations();

        Reservation reservation=res.get(res.size() - 1);
        hotelName.setText(h1.getName());
        totalRooms.setText(reservation.getTotalRooms());
        totalPrice.setText(reservation.getTotalPrice());
        rooms.setText(reservation.getRoomNumbers());

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(Hotel_Reservation_Screen.this,Customer_Choose_Option_Screen.class);
                intent.putExtra("email",Email);

                //SQL LITE
                MyDBHelper helper = new MyDBHelper(Hotel_Reservation_Screen.this);
                SQLiteDatabase database = helper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(Reservations_Store.OneReservation._NAME,HotelName);
                cv.put(Reservations_Store.OneReservation._LOCATION,HotelLocation);
                cv.put(Reservations_Store.OneReservation._CHECKIN,checkInDate);
                cv.put(Reservations_Store.OneReservation._CHECKOUT,checkOutDate);
                cv.put(Reservations_Store.OneReservation._TOTALPRICE,String.valueOf(totalPrice));
                cv.put(Reservations_Store.OneReservation._TOTALROOMS,String.valueOf(totalPrice));
                cv.put(Reservations_Store.OneReservation._ROOMS,reservation.getRoomNumbers());
                cv.put(Reservations_Store.OneReservation._RESERVEDBY,Email);
                double tep = database.insert(Reservations_Store.OneReservation.TABLENAME,null,cv);
                database.close();
                helper.close();

                startActivity(intent);
                finish();
            }
        });
    }
}

package com.sameetasadullah.i180479_i180531.presentationLayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sameetasadullah.i180479_i180531.R;
import com.sameetasadullah.i180479_i180531.logicLayer.Customer;
import com.sameetasadullah.i180479_i180531.logicLayer.HRS;
import com.sameetasadullah.i180479_i180531.logicLayer.Vendor;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Vendor_Choose_Option_Screen extends AppCompatActivity {
    RelativeLayout register_hotel, view_registered_hotels;
    CircleImageView dp;
    HRS hrs;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_choose_option_screen);

        register_hotel = findViewById(R.id.rl_register_hotel_button);
        view_registered_hotels = findViewById(R.id.rl_view_registered_hotels);
        dp = findViewById(R.id.display_pic);
        hrs = HRS.getInstance(Vendor_Choose_Option_Screen.this);

        sharedPreferences = getSharedPreferences("com.sameetasadullah.i180479_180531", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String email = getIntent().getStringExtra("email");
        for(int i = 0; i < hrs.getVendors().size(); ++i) {
            if (hrs.getVendors().get(i).getEmail().equals(email)) {
                Picasso.get().load(hrs.getVendors().get(i).getDp()).into(dp);
            }
        }

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Vendor_Choose_Option_Screen.this)
                        .setTitle("Log out")
                        .setMessage("Are you sure you want to log out?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editor.putBoolean("loggedIn", false);
                                editor.commit();
                                editor.apply();
                                Intent intent = new Intent(Vendor_Choose_Option_Screen.this, Splash_Screen.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        register_hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Vendor_Choose_Option_Screen.this, Hotel_Registration_Screen.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
        view_registered_hotels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Vendor_Choose_Option_Screen.this, Registered_Hotels_Screen.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }
}

