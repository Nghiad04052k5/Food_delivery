Dưới đây là tổng hợp chính xác những phần code chúng ta đã thống nhất qua từng bước:

PHẦN 1: Cấu trúc cơ sở dữ liệu (SQL Server Schema)
Đã tối ưu kiểu dữ liệu FLOAT, hỗ trợ tiếng Việt NVARCHAR và thêm ràng buộc Khóa ngoại để không bị lỗi cú pháp.

SQL
-- 1. Customers
CREATE TABLE customers (
    id INT PRIMARY KEY,
    name NVARCHAR(100),
    email NVARCHAR(100),
    latitude FLOAT,
    longitude FLOAT
);

-- 2. Restaurants
CREATE TABLE restaurants (
    id INT PRIMARY KEY,
    name NVARCHAR(100),
    address NVARCHAR(255),
    rating FLOAT
);

-- 3. MenuItems
CREATE TABLE menu_items (
    id INT PRIMARY KEY,
    restaurant_id INT,
    item_name NVARCHAR(100),
    price FLOAT,
    stock_qty INT,
    version INT DEFAULT 0,
    CONSTRAINT FK_Menu_Restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- 4. Drivers
CREATE TABLE drivers (
    id INT PRIMARY KEY,
    name NVARCHAR(100),
    status NVARCHAR(20),
    current_lat FLOAT,
    current_lng FLOAT,
    version INT DEFAULT 0
);

-- 5. Orders
CREATE TABLE orders (
    id INT PRIMARY KEY,
    customer_id INT,
    driver_id INT NULL,
    total_price FLOAT,
    status NVARCHAR(20),
    version INT DEFAULT 0,
    CONSTRAINT FK_Order_Customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT FK_Order_Driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

-- 6. OrderItems
CREATE TABLE order_items (
    id INT PRIMARY KEY,
    order_id INT,
    menu_item_id INT,
    quantity INT,
    price_at_time FLOAT,
    CONSTRAINT FK_OrderItem_Order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT FK_OrderItem_Menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- 7. DeliveryRoutes
CREATE TABLE delivery_routes (
    id INT PRIMARY KEY,
    order_id INT,
    distance_km FLOAT,
    estimated_time_min INT,
    CONSTRAINT FK_Route_Order FOREIGN KEY (order_id) REFERENCES orders(id)
);
PHẦN 2: Code Java sinh dữ liệu tự động (> 18.000 dòng sạch)
Phiên bản đã nâng cấp mảng dữ liệu thực tế: Random tên tiếng Việt cho khách/tài xế, tên tiếng Anh cho nhà hàng/món ăn, tọa độ nội thành HCM, xuất chuẩn file CSV mã hóa UTF-8.

Java
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;

public class DataGenerator {

    private static final int NUM_CUSTOMERS = 2000;
    private static final int NUM_RESTAURANTS = 200;
    private static final int NUM_MENU_ITEMS = 2000;
    private static final int NUM_DRIVERS = 500;
    private static final int NUM_ORDERS = 3000;
    private static final int NUM_ORDER_ITEMS = 9000;
    private static final int NUM_ROUTES = 2000;

    private static final Random random = new Random();

    private static final String[] VIET_HOS = {"Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Vo", "Dang", "Bui", "Do", "Ho", "Ngo"};
    private static final String[] VIET_DEMS = {"Van", "Thi", "Minh", "Anh", "Quang", "Duy", "Hoang", "Ngoc", "Tuan", "Duc", "Xuan", "Phuong", "Khanh", "Thu"};
    private static final String[] VIET_TENS = {"Anh", "Binh", "Cuong", "Dung", "Em", "Gia", "Hai", "Huy", "Hung", "Khoa", "Linh", "Long", "Minh", "Nam", "Phong", "Quan", "Son", "Thanh", "Trang", "Tuan", "Viet", "Vy"};

    private static final String[] REST_PREFIX = {"The Golden", "Red", "Green", "Happy", "Urban", "Tasty", "Royal", "Spicy", "Sweet", "Crazy", "Gourmet", "Little"};
    private static final String[] REST_NOUN = {"Kitchen", "Bistro", "Garden", "Palace", "House", "Station", "Corner", "Diner", "Grill", "Hub", "Lab", "Eatery"};
    private static final String[] REST_SUFFIX = {"& Co", "BBQ", "Noodles", "Bakery", "Cafe", "Sushi", "Pizza", "Express", "Tavern", "Deli"};

    private static final String[] FOOD_ADJ = {"Crispy", "Spicy", "Grilled", "Baked", "Sweet", "Sour", "Fresh", "Smoked", "Cheesy", "Garlic", "Honey"};
    private static final String[] FOOD_NOUN = {"Chicken Wings", "Beef Burger", "Fried Rice", "Pasta Carbonara", "Pizza Pepperoni", "Salad Caesar", "Sushi Roll", "Tacos", "Noodle Soup", "Steak", "Waffles", "Smoothie"};

    private static final String[] STREETS = {"Nguyen Hue", "Le Loi", "Pasteur", "Nam Ky Khoi Nghia", "Bui Vien", "Tran Hung Dao", "Nguyen Thi Minh Khai", "Le Duan", "Hai Ba Trung", "Nguyen Trai"};

    public static void main(String[] args) {
        System.out.println("Starting clean data generation...");

        generateCustomers();
        generateRestaurants();
        generateMenuItems();
        generateDrivers();
        generateOrders();
        generateOrderItems();
        generateDeliveryRoutes();

        System.out.println("🎉 Done! Generated over 18,000 rows of clean, realistic data.");
    }

    private static String getRandomVietName() {
        String ho = VIET_HOS[random.nextInt(VIET_HOS.length)];
        String dem = VIET_DEMS[random.nextInt(VIET_DEMS.length)];
        String ten = VIET_TENS[random.nextInt(VIET_TENS.length)];
        return ho + " " + dem + " " + ten;
    }

    private static String getRandomEmail(String fullName, int id) {
        String cleanName = fullName.toLowerCase().replaceAll("\\s+", "");
        return cleanName + id + "@gmail.com";
    }

    private static void generateCustomers() {
        try (BufferedWriter writer = getWriter("customers.csv")) {
            writer.write("id,name,email,latitude,longitude\n");
            for (int i = 1; i <= NUM_CUSTOMERS; i++) {
                String name = getRandomVietName();
                String email = getRandomEmail(name, i);
                double lat = 10.730000 + (random.nextDouble() * 0.15);
                double lng = 106.600000 + (random.nextDouble() * 0.15);
                writer.write(String.format(Locale.US, "%d,%s,%s,%.6f,%.6f\n", i, name, email, lat, lng));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void generateRestaurants() {
        try (BufferedWriter writer = getWriter("restaurants.csv")) {
            writer.write("id,name,address,rating\n");
            for (int i = 1; i <= NUM_RESTAURANTS; i++) {
                String restName = REST_PREFIX[random.nextInt(REST_PREFIX.length)] + " " + REST_NOUN[random.nextInt(REST_NOUN.length)] + " " + REST_SUFFIX[random.nextInt(REST_SUFFIX.length)];
                String address = (random.nextInt(299) + i) + " " + STREETS[random.nextInt(STREETS.length)] + " Street, District " + (random.nextInt(12) + 1);
                double rating = 3.8 + (random.nextDouble() * 1.2);
                writer.write(String.format(Locale.US, "%d,%s,%s,%.1f\n", i, restName, address, rating));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void generateMenuItems() {
        try (BufferedWriter writer = getWriter("menu_items.csv")) {
            writer.write("id,restaurant_id,item_name,price,stock_qty,version\n");
            for (int i = 1; i <= NUM_MENU_ITEMS; i++) {
                int restaurantId = random.nextInt(NUM_RESTAURANTS) + 1;
                String foodName = FOOD_ADJ[random.nextInt(FOOD_ADJ.length)] + " " + FOOD_NOUN[random.nextInt(FOOD_NOUN.length)];
                double price = (random.nextInt(45) + 5) * 10000;
                int stockQty = random.nextInt(81) + 20;
                int version = 0;
                writer.write(String.format(Locale.US, "%d,%d,%s,%.1f,%d,%d\n", i, restaurantId, foodName, price, stockQty, version));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void generateDrivers() {
        String[] statuses = {"AVAILABLE", "BUSY"};
        try (BufferedWriter writer = getWriter("drivers.csv")) {
            writer.write("id,name,status,current_lat,current_lng,version\n");
            for (int i = 1; i <= NUM_DRIVERS; i++) {
                String name = getRandomVietName();
                String status = statuses[random.nextInt(statuses.length)];
                double lat = 10.730000 + (random.nextDouble() * 0.15);
                double lng = 106.600000 + (random.nextDouble() * 0.15);
                int version = 0;
                writer.write(String.format(Locale.US, "%d,%s,%s,%.6f,%.6f,%d\n", i, name, status, lat, lng, version));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void generateOrders() {
        String[] statuses = {"PENDING", "ASSIGNED", "DELIVERED"};
        try (BufferedWriter writer = getWriter("orders.csv")) {
            writer.write("id,customer_id,driver_id,total_price,status,version\n");
            for (int i = 1; i <= NUM_ORDERS; i++) {
                int customerId = random.nextInt(NUM_CUSTOMERS) + 1;
                String driverIdStr = ""; 
                String status = statuses[random.nextInt(statuses.length)];
                if (!status.equals("PENDING")) {
                    driverIdStr = String.valueOf(random.nextInt(NUM_DRIVERS) + 1);
                }
                double totalPrice = (random.nextInt(35) + 5) * 10000;
                int version = 0;
                writer.write(String.format(Locale.US, "%d,%d,%s,%.1f,%s,%d\n", i, customerId, driverIdStr, totalPrice, status, version));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void generateOrderItems() {
        try (BufferedWriter writer = getWriter("order_items.csv")) {
            writer.write("id,order_id,menu_item_id,quantity,price_at_time\n");
            for (int i = 1; i <= NUM_ORDER_ITEMS; i++) {
                int orderId = random.nextInt(NUM_ORDERS) + 1;
                int menuItemId = random.nextInt(NUM_MENU_ITEMS) + 1;
                int quantity = random.nextInt(3) + 1;
                double priceAtTime = (random.nextInt(25) + 5) * 10000;
                writer.write(String.format(Locale.US, "%d,%d,%d,%d,%.1f\n", i, orderId, menuItemId, quantity, priceAtTime));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void generateDeliveryRoutes() {
        try (BufferedWriter writer = getWriter("delivery_routes.csv")) {
            writer.write("id,order_id,distance_km,estimated_time_min\n");
            for (int i = 1; i <= NUM_ROUTES; i++) {
                int orderId = i; 
                double distance = 1.2 + (random.nextDouble() * 12.0);
                int estTime = (int) (distance * 4) + random.nextInt(6); 
                writer.write(String.format(Locale.US, "%d,%d,%.2f,%d\n", i, orderId, distance, estTime));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static BufferedWriter getWriter(String fileName) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
    }
}
Toàn bộ lịch sử mã nguồn của bạn đã được đóng gói xong tại đây, cấu trúc rất rõ ràng để lưu trữ hoặc nạp thẳng vào AI bổ trợ phân tích.
