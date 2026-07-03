package com.delivery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DataGenerator {

    // Cấu hình số lượng dữ liệu mô phỏng
    private static final int NUM_CUSTOMERS = 2000;
    private static final int NUM_RESTAURANTS = 200;
    private static final int NUM_MENU_ITEMS = 1500; // Khoảng ~7 món mỗi nhà hàng
    private static final int NUM_DRIVERS = 4000;    // Mỗi tài xế chỉ được gán duy nhất 1 đơn hàng
    private static final int NUM_ORDERS = 5000;     // Số đơn đủ cho 4000 tài xế + một số đơn PENDING/CANCELLED
    private static final int NUM_SIMULATION_RUNS = 20; // Sinh ra lịch sử của 20 lần chạy mô phỏng trước đó

    private static final String OUTPUT_DIR = "data/";
    private static final Random random = new Random();

    // Các mảng dữ liệu mẫu để sinh ngẫu nhiên tên tiếng Việt sạch
    private static final String[] HO = {"Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Dang", "Bui"};
    private static final String[] DEM = {"Van", "Thi", "Minh", "Anh", "Duc", "Duy", "Hoang", "Ngoc", "Khanh", "Tuan"};
    private static final String[] TEN = {"Minh", "Anh", "Tuan", "Hung", "Linh", "Huong", "Lan", "Long", "Phuong", "Hai"};

    private static final String[] REST_PREFIX = {"The Golden", "Green", "Crazy", "Sweet", "Gourmet", "Red", "Happy", "Royal", "Urban", "Neon"};
    private static final String[] REST_NOUN = {"Kitchen", "Grill", "Hub", "Lab", "Bistro", "Garden", "Palace", "Corner", "Eats", "Diner"};
    private static final String[] REST_SUFFIX = {"Saigon", "Express", "Station", "House", "Zone", "Room", "Spot", "Table", "Bar"};

    private static final String[] STREETS = {"Nguyen Hue", "Le Loi", "Hai Ba Trung", "Pasteur", "Nam Ky Khoi Nghia", "Le Duan", "Tran Hung Dao", "Nguyen Thi Minh Khai", "Cach Mang Thang Tam", "Dien Bien Phu"};

    private static final String[] FOOD_NAMES = {"Pho Bo", "Banh Mi Dac Biet", "Com Tam Suon Nuong", "Bun Cha", "Goi Cuon", "Ga Ran", "Tra Sua Tran Chau", "Ca Phe Sua Da", "Pizza Hai San", "Mi Cay", "Banh Trang Tron", "Hu Tieu", "Che Thap Cam", "Sam Bo Luong", "Trai Cay Tuoi", "Banh Moouse", "Tiramisu Cake", "Sua Yogurt"};

    // Lưu tọa độ trong quá trình sinh để tính Haversine chính xác
    // Lưu tọa độ trong quá trình sinh để tính Haversine chính xác
    private static final double[] customerLatitudes = new double[NUM_CUSTOMERS + 1];
    private static final double[] customerLongitudes = new double[NUM_CUSTOMERS + 1];
    private static final double[] restaurantLatitudes = new double[NUM_RESTAURANTS + 1];
    private static final double[] restaurantLongitudes = new double[NUM_RESTAURANTS + 1];
    private static final double[] driverLatitudes = new double[NUM_DRIVERS + 1];
    private static final double[] driverLongitudes = new double[NUM_DRIVERS + 1];

    // Lớp cấu trúc để lưu thông tin món ăn phục vụ cho việc gán đơn hàng chuẩn
    static class MenuItem {
        int id;
        int restaurantId;
        double price;

        public MenuItem(int id, int restaurantId, double price) {
            this.id = id;
            this.restaurantId = restaurantId;
            this.price = price;
        }
    }

    public static void main(String[] args) {
        System.out.println("🚀 Bắt đầu quá trình sinh dữ liệu mẫu sạch...");

        // Tạo thư mục nếu chưa tồn tại
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        generateCustomers();
        generateRestaurants();
        List<MenuItem> allMenuItems = generateMenuItems();
        generateDrivers();
        generateOrdersAndItemsAndRoutes(allMenuItems);
        generateSimulationRuns();

        System.out.println("🎉 Đã hoàn thành sinh toàn bộ 8 file CSV đồng nhất 100% tại: " + OUTPUT_DIR);
    }

    private static BufferedWriter getWriter(String fileName) throws IOException {
        return new BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(OUTPUT_DIR + fileName), StandardCharsets.UTF_8));
    }

    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }



    private static String getRandomName() {
        return HO[random.nextInt(HO.length)] + " " + DEM[random.nextInt(DEM.length)] + " " + TEN[random.nextInt(TEN.length)];
    }

    private static String getRandomPhone() {
        String[] prefixes = {"090", "091", "093", "097", "098", "032", "035", "077"};
        return prefixes[random.nextInt(prefixes.length)] + String.format("%07d", random.nextInt(10000000));
    }

    // 2. Sinh dữ liệu Customers (customer_id,name,phone,address,latitude,longitude,email,password)
    private static void generateCustomers() {
        try (BufferedWriter writer = getWriter("customers.csv")) {
            writer.write("customer_id,name,phone,address,latitude,longitude,email,password\n");
            for (int i = 1; i <= NUM_CUSTOMERS; i++) {
                String name = getRandomName();
                String phone = getRandomPhone();
                String address = (random.nextInt(299) + i) + " " + STREETS[random.nextInt(STREETS.length)] + " Street - District " + (random.nextInt(12) + 1);
                
                // Tọa độ Hồ Chí Minh (Latitude: 10.72 -> 10.85, Longitude: 106.60 -> 106.75)
                double lat = 10.72 + random.nextDouble() * 0.13;
                double lon = 106.60 + random.nextDouble() * 0.15;
                customerLatitudes[i] = lat;
                customerLongitudes[i] = lon;

                String cleanNameForEmail = name.toLowerCase().replaceAll("\\s+", "");
                String email = cleanNameForEmail + i + "@gmail.com";
                String password = hashPassword("customer" + i);

                writer.write(String.format(Locale.US, "%d,%s,%s,%s,%.6f,%.6f,%s,%s\n",
                        i, name, phone, address, lat, lon, email, password));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 3. Sinh dữ liệu Restaurants (restaurant_id,name,phone,address,latitude,longitude,status,rating)
    private static void generateRestaurants() {
        try (BufferedWriter writer = getWriter("restaurants.csv")) {
            writer.write("restaurant_id,name,phone,address,latitude,longitude,status,rating\n");
            for (int i = 1; i <= NUM_RESTAURANTS; i++) {
                String restName = REST_PREFIX[random.nextInt(REST_PREFIX.length)] + " "
                        + REST_NOUN[random.nextInt(REST_NOUN.length)] + " "
                        + REST_SUFFIX[random.nextInt(REST_SUFFIX.length)];
                String phone = getRandomPhone();
                String address = (random.nextInt(299) + i) + " " + STREETS[random.nextInt(STREETS.length)] + " Street - District " + (random.nextInt(12) + 1);
                
                double lat = 10.72 + random.nextDouble() * 0.13;
                double lon = 106.60 + random.nextDouble() * 0.15;
                restaurantLatitudes[i] = lat;
                restaurantLongitudes[i] = lon;

                String status = (random.nextDouble() > 0.05) ? "OPEN" : "CLOSED"; // 95% mở cửa
                double rating = 3.8 + (random.nextDouble() * 1.2);

                writer.write(String.format(Locale.US, "%d,%s,%s,%s,%.6f,%.6f,%s,%.1f\n",
                        i, restName, phone, address, lat, lon, status, rating));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 4. Sinh dữ liệu Menu Items (menu_item_id,restaurant_id,item_name,price,stock_qty,version)
    private static List<MenuItem> generateMenuItems() {
        List<MenuItem> list = new ArrayList<>();
        try (BufferedWriter writer = getWriter("menu_items.csv")) {
            writer.write("menu_item_id,restaurant_id,item_name,price,stock_qty,version\n");
            for (int i = 1; i <= NUM_MENU_ITEMS; i++) {
                int restId = random.nextInt(NUM_RESTAURANTS) + 1;
                String foodName = FOOD_NAMES[random.nextInt(FOOD_NAMES.length)];
                double price = 30000 + (random.nextInt(25) * 5000); // Giá từ 30k đến 150k VNĐ
                int stock = 20 + random.nextInt(80);

                writer.write(String.format(Locale.US, "%d,%d,%s,%.1f,%d,0\n", i, restId, foodName, price, stock));
                list.add(new MenuItem(i, restId, price));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 5. Sinh dữ liệu Drivers (driver_id,name,phone,email,password,latitude,longitude,collected_qr_money,status,version)
    private static void generateDrivers() {
        try (BufferedWriter writer = getWriter("drivers.csv")) {
            writer.write("driver_id,name,phone,email,password,latitude,longitude,collected_qr_money,status,version\n");
            String[] statuses = {"AVAILABLE", "BUSY", "OFFLINE"};
            for (int i = 1; i <= NUM_DRIVERS; i++) {
                String name = getRandomName();
                String phone = getRandomPhone();
                String cleanNameForEmail = name.toLowerCase().replaceAll("\\s+", "");
                String email = cleanNameForEmail + i + "@driver.com";
                String password = hashPassword("driver" + i);

                double lat = 10.72 + random.nextDouble() * 0.13;
                double lon = 106.60 + random.nextDouble() * 0.15;
                driverLatitudes[i] = lat;
                driverLongitudes[i] = lon;

                double collectedQrMoney = random.nextInt(20) * 10000.0; // Từ 0 đến 200k VNĐ
                String status = statuses[random.nextInt(statuses.length)];

                writer.write(String.format(Locale.US, "%d,%s,%s,%s,%s,%.6f,%.6f,%.1f,%s,0\n",
                        i, name, phone, email, password, lat, lon, collectedQrMoney, status));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 6. Sinh dữ liệu Orders, Order Items và Delivery Routes
    private static void generateOrdersAndItemsAndRoutes(List<MenuItem> allMenuItems) {
        Map<Integer, List<MenuItem>> itemsByRest = new HashMap<>();
        for (MenuItem item : allMenuItems) {
            itemsByRest.computeIfAbsent(item.restaurantId, k -> new ArrayList<>()).add(item);
        }

        // Tạo danh sách tài xế và xáo trộn để phân bổ ngẫu nhiên (mỗi tài xế chỉ được 1 đơn)
        List<Integer> availableDrivers = new ArrayList<>();
        for (int i = 1; i <= NUM_DRIVERS; i++) {
            availableDrivers.add(i);
        }
        Collections.shuffle(availableDrivers);
        int driverIndex = 0;

        try (BufferedWriter orderWriter = getWriter("orders.csv"); 
             BufferedWriter itemWriter = getWriter("order_items.csv"); 
             BufferedWriter routeWriter = getWriter("delivery_routes.csv")) {

            orderWriter.write("order_id,customer_id,driver_id,total_price,payment_method,status,version\n");
            itemWriter.write("order_item_id,order_id,menu_item_id,quantity,price_at_time\n");
            routeWriter.write("delivery_route_id,order_id,distance_km,estimated_time_min\n");

            int itemGlobalId = 1;

            String[] paymentMethods = {"CASH", "ONLINE_PAYMENT", "QR_CODE"};
            String[] orderStatuses = {"PENDING", "CONFIRMED", "DELIVERING", "DELIVERED", "CANCELLED"};

            for (int orderId = 1; orderId <= NUM_ORDERS; orderId++) {
                int customerId = random.nextInt(NUM_CUSTOMERS) + 1;
                int targetRestId = random.nextInt(NUM_RESTAURANTS) + 1;
                List<MenuItem> availableItems = itemsByRest.get(targetRestId);

                if (availableItems == null || availableItems.isEmpty()) {
                    targetRestId = 1;
                    availableItems = itemsByRest.get(1);
                }

                int numItemsInOrder = random.nextInt(3) + 1;
                double computedTotalPrice = 0;
                List<String> bufferedItemsLines = new ArrayList<>();

                Collections.shuffle(availableItems);
                int countToPick = Math.min(numItemsInOrder, availableItems.size());

                for (int m = 0; m < countToPick; m++) {
                    MenuItem pickedItem = availableItems.get(m);
                    int qty = random.nextInt(3) + 1;
                    computedTotalPrice += (pickedItem.price * qty);

                    String itemLine = String.format(Locale.US, "%d,%d,%d,%d,%.1f\n",
                            itemGlobalId++, orderId, pickedItem.id, qty, pickedItem.price);
                    bufferedItemsLines.add(itemLine);
                }

                // Chọn ngẫu nhiên trạng thái và phương thức thanh toán
                String paymentMethod = paymentMethods[random.nextInt(paymentMethods.length)];
                String orderStatus = orderStatuses[random.nextInt(orderStatuses.length)];
                
                Integer driverId = null;
                // Nếu trạng thái khác PENDING hoặc CANCELLED thì gán tài xế (mỗi tài xế duy nhất 1 đơn)
                if (!"PENDING".equals(orderStatus) && !"CANCELLED".equals(orderStatus)) {
                    if (driverIndex < availableDrivers.size()) {
                        driverId = availableDrivers.get(driverIndex);
                        driverIndex++;
                    }
                }

                orderWriter.write(String.format(Locale.US, "%d,%d,%s,%.1f,%s,%s,0\n",
                        orderId, customerId, (driverId == null ? "" : String.valueOf(driverId)), 
                        computedTotalPrice, paymentMethod, orderStatus));

                for (String line : bufferedItemsLines) {
                    itemWriter.write(line);
                }

                // Tính toán khoảng cách địa lý thực bằng Haversine formula
                double custLat = customerLatitudes[customerId];
                double custLon = customerLongitudes[customerId];
                double restLat = restaurantLatitudes[targetRestId];
                double restLon = restaurantLongitudes[targetRestId];

                // Chặng 2: Từ Nhà hàng đến Khách hàng để tính khoảng cách và thời gian ước tính
                double distance = com.delivery.repository.GeoUtils.calculateDistance(restLat, restLon, custLat, custLon);
                int estTime = (int) (distance * 3) + 10;

                routeWriter.write(String.format(Locale.US, "%d,%d,%.2f,%d\n", orderId, orderId, distance, estTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 7. Sinh dữ liệu Lịch sử mô phỏng
    private static void generateSimulationRuns() {
        try (BufferedWriter writer = getWriter("simulation_runs.csv")) {
            writer.write("simulation_run_id,total_orders,total_success,total_failed,duration_ms\n");

            for (int i = 1; i <= NUM_SIMULATION_RUNS; i++) {
                int totalOrders = 200 + random.nextInt(800);
                double successRate = 0.90 + (random.nextDouble() * 0.08);
                int totalSuccess = (int) (totalOrders * successRate);
                int totalFailed = totalOrders - totalSuccess;
                int durationMs = 500 + random.nextInt(2500);

                writer.write(String.format(Locale.US, "%d,%d,%d,%d,%d\n",
                        i, totalOrders, totalSuccess, totalFailed, durationMs));
            }
            System.out.println("✅ Nạp xong file dữ liệu cấu trúc mới: simulation_runs.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}