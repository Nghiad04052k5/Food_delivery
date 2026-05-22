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

    // Dữ liệu mẫu để sinh tên Tiếng Việt (Khách hàng & Tài xế)
    private static final String[] VIET_HOS = {"Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Vo", "Dang", "Bui", "Do", "Ho", "Ngo"};
    private static final String[] VIET_DEMS = {"Van", "Thi", "Minh", "Anh", "Quang", "Duy", "Hoang", "Ngoc", "Tuan", "Duc", "Xuan", "Phuong", "Khanh", "Thu"};
    private static final String[] VIET_TENS = {"Anh", "Binh", "Cuong", "Dung", "Em", "Gia", "Hai", "Huy", "Hung", "Khoa", "Linh", "Long", "Minh", "Nam", "Phong", "Quan", "Son", "Thanh", "Trang", "Tuan", "Viet", "Vy"};

    // Dữ liệu mẫu tiếng Anh cho Nhà hàng
    private static final String[] REST_PREFIX = {"The Golden", "Red", "Green", "Happy", "Urban", "Tasty", "Royal", "Spicy", "Sweet", "Crazy", "Gourmet", "Little"};
    private static final String[] REST_NOUN = {"Kitchen", "Bistro", "Garden", "Palace", "House", "Station", "Corner", "Diner", "Grill", "Hub", "Lab", "Eatery"};
    private static final String[] REST_SUFFIX = {"& Co", "BBQ", "Noodles", "Bakery", "Cafe", "Sushi", "Pizza", "Express", "Tavern", "Deli"};

    // Dữ liệu mẫu tiếng Anh cho Món ăn
    private static final String[] FOOD_ADJ = {"Crispy", "Spicy", "Grilled", "Baked", "Sweet", "Sour", "Fresh", "Smoked", "Cheesy", "Garlic", "Honey"};
    private static final String[] FOOD_NOUN = {"Chicken Wings", "Beef Burger", "Fried Rice", "Pasta Carbonara", "Pizza Pepperoni", "Salad Caesar", "Sushi Roll", "Tacos", "Noodle Soup", "Steak", "Waffles", "Smoothie"};

    // Dữ liệu mẫu tên đường phố TP.HCM để làm địa chỉ nhà hàng
    private static final String[] STREETS = {"Nguyen Hue", "Le Loi", "Pasteur", "Nam Ky Khoi Nghia", "Bui Vien", "Tran Hung Dao", "Nguyen Thi Minh Khai", "Le Duan", "Hai Ba Trung", "Nguyen Trai", "Marie Curie", "Nguyen Van Troi"};

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
        // Chuyển "Nguyen Van Anh" -> "nguyenvananh123@gmail.com"
        String cleanName = fullName.toLowerCase().replaceAll("\\s+", "");
        return cleanName + id + "@gmail.com";
    }

    // 1. Customers (2.000 dòng)
    private static void generateCustomers() {
        try (BufferedWriter writer = getWriter("customers.csv")) {
            writer.write("id,name,email,latitude,longitude\n");
            for (int i = 1; i <= NUM_CUSTOMERS; i++) {
                String name = getRandomVietName();
                String email = getRandomEmail(name, i);
                double lat = 10.730000 + (random.nextDouble() * 0.15); // Tọa độ thực tế quanh nội thành HCM
                double lng = 106.600000 + (random.nextDouble() * 0.15);
                writer.write(String.format(Locale.US, "%d,%s,%s,%.6f,%.6f\n", i, name, email, lat, lng));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // 2. Restaurants (200 dòng)
    private static void generateRestaurants() {
        try (BufferedWriter writer = getWriter("restaurants.csv")) {
            writer.write("id,name,address,rating\n");
            for (int i = 1; i <= NUM_RESTAURANTS; i++) {
                String restName = REST_PREFIX[random.nextInt(REST_PREFIX.length)] + " " + 
                                  REST_NOUN[random.nextInt(REST_NOUN.length)] + " " + 
                                  REST_SUFFIX[random.nextInt(REST_SUFFIX.length)];
                String address = (random.nextInt(299) + i) + " " + STREETS[random.nextInt(STREETS.length)] + " Street, District " + (random.nextInt(12) + 1);
                double rating = 3.8 + (random.nextDouble() * 1.2);
                writer.write(String.format(Locale.US, "%d,%s,%s,%.1f\n", i, restName, address, rating));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // 3. MenuItems (2.000 dòng)
    private static void generateMenuItems() {
        try (BufferedWriter writer = getWriter("menu_items.csv")) {
            writer.write("id,restaurant_id,item_name,price,stock_qty,version\n");
            for (int i = 1; i <= NUM_MENU_ITEMS; i++) {
                int restaurantId = random.nextInt(NUM_RESTAURANTS) + 1;
                String foodName = FOOD_ADJ[random.nextInt(FOOD_ADJ.length)] + " " + FOOD_NOUN[random.nextInt(FOOD_NOUN.length)];
                double price = (random.nextInt(45) + 5) * 10000; // 50,000đ - 500,000đ
                int stockQty = random.nextInt(81) + 20; // 20 - 100
                int version = 0;
                writer.write(String.format(Locale.US, "%d,%d,%s,%.1f,%d,%d\n", i, restaurantId, foodName, price, stockQty, version));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // 4. Drivers (500 dòng)
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

    // 5. Orders (3.000 dòng)
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
                double totalPrice = (random.nextInt(35) + 5) * 10000; // 50,000đ - 400,000đ
                int version = 0;
                writer.write(String.format(Locale.US, "%d,%d,%s,%.1f,%s,%d\n", i, customerId, driverIdStr, totalPrice, status, version));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // 6. OrderItems (9.000 dòng)
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

    // 7. DeliveryRoutes (2.000 dòng)
    private static void generateDeliveryRoutes() {
        try (BufferedWriter writer = getWriter("delivery_routes.csv")) {
            writer.write("id,order_id,distance_km,estimated_time_min\n");
            for (int i = 1; i <= NUM_ROUTES; i++) {
                int orderId = i; 
                double distance = 1.2 + (random.nextDouble() * 12.0); // 1.2km - 13.2km
                int estTime = (int) (distance * 4) + random.nextInt(6); 
                writer.write(String.format(Locale.US, "%d,%d,%.2f,%d\n", i, orderId, distance, estTime));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static BufferedWriter getWriter(String fileName) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
    }
}