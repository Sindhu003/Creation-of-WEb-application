import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ShopServlet")
public class ShopServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Database credentials
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/ecommerce";
    private static final String DB_USER = "rahul";
    private static final String DB_PASS = "rahul123";
    
    // SQL queries
    private static final String SELECT_ITEMS = "SELECT * FROM items WHERE category = ?";
    private static final String SELECT_ITEM_BY_ID = "SELECT * FROM items WHERE id = ?";
    private static final String SELECT_ITEM_QUANTITY = "SELECT quantity FROM items WHERE id = ?";
    private static final String UPDATE_ITEM_QUANTITY = "UPDATE items SET quantity = ? WHERE id = ?";
    private static final String INSERT_ORDER = "INSERT INTO orders (customer_name, customer_email, customer_address, total_price) VALUES (?, ?, ?, ?)";
    private static final String INSERT_ORDER_ITEM = "INSERT INTO order_items (order_id, item_id, quantity, price) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ORDERS = "SELECT * FROM orders";
    private static final String SELECT_ORDER_ITEMS = "SELECT * FROM order_items WHERE order_id = ?";
    private static final String SELECT_ITEMS_ADMIN = "SELECT * FROM items";
    private static final String SELECT_ITEM_BY_NAME = "SELECT * FROM items WHERE name = ?";
    private static final String INSERT_ITEM = "INSERT INTO items (name, description, price, quantity, category, image) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ITEM = "UPDATE items SET name = ?, description = ?, price = ?, quantity = ?, category = ?, image = ? WHERE id = ?";
    private static final String DELETE_ITEM = "DELETE FROM items WHERE id = ?";
    
    private Connection conn;
    
    public void init() throws ServletException {
        try {
            // Register JDBC driver
            Class.forName("org.postgresql.Driver");
            
            // Open a connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void destroy() {
        try {
            // Close the connection
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "home";
        }
        
        switch (action) {
            case "home":
                showHome(request, response);
                break;
            case "items":
                showItems(request, response);
                break;
            case "item":
                showItem(request, response);
                break;
            case "cart":
                showCart(request, response);
                break;
            case "checkout":
                showCheckout(request, response);
                break;
            case "confirmation":
                showConfirmation(request, response);
                break;
            case "login":
                showLogin(request, response);
                break;
            case "register":
                showRegistration(request, response);
                break
        case "admin":
            showAdmin(request, response);
            break;
        case "admin-orders":
            showAdminOrders(request, response);
            break;
        case "admin-items":
            showAdminItems(request, response);
            break;
        case "admin-item-add":
            showAdminItemAdd(request, response);
            break;
        case "admin-item-edit":
            showAdminItemEdit(request, response);
            break;
        case "logout":
            logout(request, response);
            break;
        default:
            showHome(request, response);
    }
}

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");
    if (action == null) {
        action = "home";
    }
    
    switch (action) {
        case "cart-add":
            addToCart(request, response);
            break;
        case "cart-remove":
            removeFromCart(request, response);
            break;
        case "cart-update":
            updateCart(request, response);
            break;
        case "checkout":
            placeOrder(request, response);
            break;
        case "login":
            login(request, response);
            break;
        case "register":
            register(request, response);
            break;
        case "admin-item-add":
            addNewItem(request, response);
            break;
        case "admin-item-edit":
            editItem(request, response);
            break;
        case "admin-item-delete":
            deleteItem(request, response);
            break;
        default:
            showHome(request, response);
    }
}

private void showHome(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.getRequestDispatcher("index.html").forward(request, response);
}

private void showItems(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String category = request.getParameter("category");
    if (category == null) {
        category = "all";
    }
    
    List<Item> items = getItemsByCategory(category);
    request.setAttribute("items", items);
    request.setAttribute("category", category);
    
    request.getRequestDispatcher("items.html").forward(request, response);
}

private void showItem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    int itemId = Integer.parseInt(request.getParameter("id"));
    Item item = getItemById(itemId);
    request.setAttribute("item", item);
    
    request.getRequestDispatcher("item.html").forward(request, response);
}

private void showCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
    if (cart == null) {
        cart = new HashMap<>();
    }
    
    List<Item> items = getItemsByIds(cart.keySet());
    Map<Item, Integer> cartItems = new HashMap<>();
    double totalPrice = 0;
    for (Item item : items) {
        int quantity = cart.get(item.getId());
        cartItems.put(item, quantity);
        totalPrice += item.getPrice() * quantity;
    }
    
    request.setAttribute("cartItems", cartItems);
    request.setAttribute("totalPrice", totalPrice);
    
    request.getRequestDispatcher("cart.html").forward(request, response);
}

private void showCheckout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
    if (cart == null) {
        cart = new HashMap<>();
    }
    
    List<Item> items = getItemsByIds(cart.keySet());
    Map<Item, Integer> cartItems = new HashMap<>();
    double totalPrice = 0;
    for (Item item : items) {
        int quantity = cart.get(item.getId());
cartItems.put(item, quantity);
totalPrice += item.getPrice() * quantity;
}

request.setAttribute("cartItems", cartItems);
request.setAttribute("totalPrice", totalPrice);

request.getRequestDispatcher("checkout.html").forward(request, response);

}

private void placeOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
HttpSession session = request.getSession();
Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
if (cart == null || cart.isEmpty()) {
response.sendRedirect("cart.html");
return;
}

String name = request.getParameter("name");
String email = request.getParameter("email");
String address = request.getParameter("address");
String city = request.getParameter("city");
String state = request.getParameter("state");
String zip = request.getParameter("zip");
String cardNumber = request.getParameter("card-number");
String cardExpiry = request.getParameter("card-expiry");
String cardCvv = request.getParameter("card-cvv");

if (name == null || name.isEmpty() || email == null || email.isEmpty() || address == null || address.isEmpty()
        || city == null || city.isEmpty() || state == null || state.isEmpty() || zip == null || zip.isEmpty()
        || cardNumber == null || cardNumber.isEmpty() || cardExpiry == null || cardExpiry.isEmpty() || cardCvv == null || cardCvv.isEmpty()) {
    request.setAttribute("error", "All fields are required");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Pattern cardNumberPattern = Pattern.compile("^\\d{16}$");
Matcher cardNumberMatcher = cardNumberPattern.matcher(cardNumber);
if (!cardNumberMatcher.matches()) {
    request.setAttribute("error", "Invalid credit card number");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Pattern cardExpiryPattern = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");
Matcher cardExpiryMatcher = cardExpiryPattern.matcher(cardExpiry);
if (!cardExpiryMatcher.matches()) {
    request.setAttribute("error", "Invalid credit card expiry date (MM/YY)");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Pattern cardCvvPattern = Pattern.compile("^\\d{3}$");
Matcher cardCvvMatcher = cardCvvPattern.matcher(cardCvv);
if (!cardCvvMatcher.matches()) {
    request.setAttribute("error", "Invalid credit card CVV");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Order order = new Order(name, email, address, city, state, zip, cardNumber, cardExpiry, cardCvv, new Date());
for (Integer itemId : cart.keySet()) {
    int quantity = cart.get(itemId);
    order.addItem(new OrderItem(itemId, quantity));
}
addOrder(order);

session.removeAttribute("cart");

request.setAttribute("orderId", order.getId());
request.getRequestDispatcher("order-confirmation.html").forward(request, response);

());
cartItems.put(item, quantity);
totalPrice += item.getPrice() * quantity;
}

scss
Copy code
request.setAttribute("cartItems", cartItems);
request.setAttribute("totalPrice", totalPrice);

request.getRequestDispatcher("checkout.html").forward(request, response);
}

private void placeOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
HttpSession session = request.getSession();
Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
if (cart == null || cart.isEmpty()) {
response.sendRedirect("cart.html");
return;
}

java
Copy code
String name = request.getParameter("name");
String email = request.getParameter("email");
String address = request.getParameter("address");
String city = request.getParameter("city");
String state = request.getParameter("state");
String zip = request.getParameter("zip");
String cardNumber = request.getParameter("card-number");
String cardExpiry = request.getParameter("card-expiry");
String cardCvv = request.getParameter("card-cvv");

if (name == null || name.isEmpty() || email == null || email.isEmpty() || address == null || address.isEmpty()
        || city == null || city.isEmpty() || state == null || state.isEmpty() || zip == null || zip.isEmpty()
        || cardNumber == null || cardNumber.isEmpty() || cardExpiry == null || cardExpiry.isEmpty() || cardCvv == null || cardCvv.isEmpty()) {
    request.setAttribute("error", "All fields are required");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Pattern cardNumberPattern = Pattern.compile("^\\d{16}$");
Matcher cardNumberMatcher = cardNumberPattern.matcher(cardNumber);
if (!cardNumberMatcher.matches()) {
    request.setAttribute("error", "Invalid credit card number");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Pattern cardExpiryPattern = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");
Matcher cardExpiryMatcher = cardExpiryPattern.matcher(cardExpiry);
if (!cardExpiryMatcher.matches()) {
    request.setAttribute("error", "Invalid credit card expiry date (MM/YY)");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Pattern cardCvvPattern = Pattern.compile("^\\d{3}$");
Matcher cardCvvMatcher = cardCvvPattern.matcher(cardCvv);
if (!cardCvvMatcher.matches()) {
    request.setAttribute("error", "Invalid credit card CVV");
    request.getRequestDispatcher("checkout.html").forward(request, response);
    return;
}

Order order = new Order(name, email, address, city, state, zip, cardNumber, cardExpiry, cardCvv, new Date());
for (Integer itemId : cart.keySet()) {
    int quantity = cart.get(itemId);
    order.addItem(new OrderItem(itemId, quantity));
}
addOrder(order);

session.removeAttribute("cart");

request.setAttribute("orderId", order.getId());
request.getRequestDispatcher("order-confirmation.html").forward(request, response);
}

private void showAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
List<Order> orders = getAllOrders();
request.setAttribute("orders", orders);

request.getRequestDispatcher("admin.html").forward(request, response);


}

private void showAdminOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
int orderId = Integer.parseInt(request.getParameter("id"));
Order order = getOrderById(orderId);
if (order == null) {
response.sendRedirect("admin.html");
return;
}

request.setAttribute("order", order);
request.getRequestDispatcher("admin-orders.html").forward(request, response);
}

private void showAdminItems(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
List<Item> items = getAllItems();
request.setAttribute("items", items);

request.getRequestDispatcher("admin-items.html").forward(request, response);


}

private void showAdminEditItem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
int itemId = Integer.parseInt(request.getParameter("id"));
Item item = getItemById(itemId);
if (item == null) {
response.sendRedirect("admin-items.html");
return;
}

request.setAttribute("item", item);

request.getRequestDispatcher("admin-item-edit.html").forward(request, response);
}

private void editItem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
int itemId = Integer.parseInt(request.getParameter("id"));
Item item = getItemById(itemId);
if (item == null) {
response.sendRedirect("admin-items.html");
return;
}

String name = request.getParameter("name");
String category = request.getParameter("category");
String description = request.getParameter("description");
String imageUrl = request.getParameter("image-url");
double price = Double.parseDouble(request.getParameter("price"));
int stockQuantity = Integer.parseInt(request.getParameter("stock-quantity"));

item.setName(name);
item.setCategory(category);
item.setDescription(description);
item.setImageUrl(imageUrl);
item.setPrice(price);
item.setStockQuantity(stockQuantity);

updateItem(item);

response.sendRedirect("admin-items.html");


}

private void showAdminAddItem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
request.getRequestDispatcher("admin-item-add.html").forward(request, response);
}

private void addItem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
String name = request.getParameter("name");
String category = request.getParameter("category");
String description = request.getParameter("description");
String imageUrl = request.getParameter("image-url");
double price = Double.parseDouble(request.getParameter("price"));
int stockQuantity = Integer.parseInt(request.getParameter("stock-quantity"));


Item item = new Item(name, category, description, imageUrl, price, stockQuantity);
addItem(item);

response.sendRedirect("admin-items.html");


}

protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
String action = request.getParameter("action");
if (action == null) {
action = "showItems";
}


switch (action) {
    case "showItems":
        showItems(request, response);
        break;
    case "showItem":
        showItem(request, response);
        break;
    case "addToCart":
        addToCart(request, response);
        break;
    case "showCart":
        showCart(request, response);
        break;
    case "updateCart":
        updateCart(request, response);
        break;
    case "removeFromCart":
        removeFromCart(request, response);
        break;
    case "showCheckout":
        showCheckout(request, response);
        break;
    case "placeOrder":
        placeOrder(request, response);
        break;
    case "showAccount":
        showAccount(request, response);
        break;
    case "showLogin":
        showLogin(request, response);
        break;
    case "login":
        login(request, response);
        break;
    case "showRegistration":
        showRegistration(request, response);
        break;
    case "register":
        register(request, response);
        break;
    case "logout":
        logout(request, response);
        break;
    case "showAdmin":
        showAdmin(request, response);
        break;
    case "showAdminOrders":
        showAdminOrders(request, response);
        break;
    case "showAdminItems":
       


    	showAdminItems(request, response);
    	break;
    case "showAdminEditItem":
    	showAdminEditItem(request, response);
    	break;
    case "editItem":
    	editItem(request, response);
    	break;
    case "showAdminAddItem":
    	showAdminAddItem(request, response);
    	break;
    case "addItem":
    	addItem(request, response);
    	break;
    default:
    	response.sendRedirect("index.html");
    	break;
	}
}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
}