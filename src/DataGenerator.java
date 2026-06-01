package com.delivery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DataGenerator {

    // Cấu hình số lượng dữ liệu mô phỏng
    private static final int NUM_CUSTOMERS = 2000;
    private static final int NUM_RESTAURANTS = 200;
    private static final int NUM_MENU_ITEMS = 1500; // Khoảng ~7 món mỗi nhà hàng
    private static final int NUM_DRIVERS = 300;
    private static final int NUM_ORDERS = 5000;
    private static final int NUM_SIMULATION_RUNS = 20; // Sinh ra lịch sử của 20 lần chạy mô phỏng trước đó

    private static final String OUTPUT_DIR = "C:/Users/ADMIN/Downloads/SUM26/labl/LAB211/";
    private static final Random random = new Random();

    // Các mảng dữ liệu mẫu để sinh ngẫu nhiên tên tiếng Việt sạch
    private static final String[] HO = {"Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Dang", "Bui"};
    private static final String[] DEM = {"Van", "Thi", "Minh", "Anh", "Duc", "Duy", "Hoang", "Ngoc", "Khanh", "Tuan"};
    private static final String[] TEN = {"Minh", "Anh", "Tuan", "Hung", "Linh", "Huong", "Lan", "Long", "Phuong", "Hai"};

    private static final String[] REST_PREFIX = {"The Golden", "Green", "Crazy", "Sweet", "Gourmet", "Red", "Happy", "Royal", "Urban", "Neon"};
    private static final String[] REST_NOUN = {"Kitchen", "Grill", "Hub", "Lab", "Bistro", "Garden", "Palace", "Corner", "Eats", "Diner"};
    private static final String[] REST_SUFFIX = {"Saigon", "Express", "Station", "House", "Zone", "Room", "Spot", "Table", "Bar"};

    private static final String[] STREETS = {"Nguyen Hue", "Le Loi", "Hai Ba Trung", "Pasteur", "Nam Ky Khoi Nghia", "Le Duan", "Tran Hung Dao", "Nguyen Thi Minh Khai", "Cach Mang Thang Tam", "Dien Bien Phu"};

    private static final String[] FOOD_NAMES = {"Pho Bo", "Banh Mi Dac Biet", "Com Tam Suon Nuong", "Bun Cha", "Goi Cuon", "Ga Ran", "Tra Sua Tran Chau", "Ca Phe Sua Da", "Pizza Hai San", "Mi Cay", "Banh Trang Tron", "Hu Tieu", "Che Thap Cam", "Sam Bo Luong", "Trai Cay Tuoi", "Banh Moouse", "Tiramisu Cake", "Sua Chua"};

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
        return new BufferedWriter(new FileWriter(OUTPUT_DIR + fileName, StandardCharsets.UTF_8));
    }

    // 1. Hàm băm SHA-256 bảo mật mật khẩu
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getRandomName() {
        return HO[random.nextInt(HO.length)] + " " + DEM[random.nextInt(DEM.length)] + " " + TEN[random.nextInt(TEN.length)];
    }

    // 2. Sinh dữ liệu Customers (Mật khẩu riêng biệt băm SHA-256)
    private static void generateCustomers() {
        try (BufferedWriter writer = getWriter("customers.csv")) {
            writer.write("customer_id,name,phone,email,password\n");
            for (int i = 1; i <= NUM_CUSTOMERS; i++) {
                String name = getRandomName();
                String cleanNameForEmail = name.toLowerCase().replaceAll("\\s+", "");
                String email = cleanNameForEmail + i + "@gmail.com";

                String[] prefixes = {"090", "091", "093", "097", "098", "032", "035", "077"};
                String phone = prefixes[random.nextInt(prefixes.length)] + String.format("%07d", random.nextInt(10000000));

                String rawPassword = "CustomerPass" + i + "!";
                String hashedPassword = hashPassword(rawPassword);

                writer.write(String.format(Locale.US, "%d,%s,%s,%s,%s\n",
                        i, name, phone, email, hashedPassword));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 3. Sinh dữ liệu Restaurants (Dùng dấu phân tách cột là Chấm Phẩy ';' để loại bỏ lệch cột địa chỉ)
    private static void generateRestaurants() {
        try (BufferedWriter writer = getWriter("restaurants.csv")) {
            writer.write("restaurant_id,name,address,rating\n");
            for (int i = 1; i <= NUM_RESTAURANTS; i++) {
                String restName = REST_PREFIX[random.nextInt(REST_PREFIX.length)] + " "
                        + REST_NOUN[random.nextInt(REST_NOUN.length)] + " "
                        + REST_SUFFIX[random.nextInt(REST_SUFFIX.length)];

                String address = (random.nextInt(299) + i) + " " + STREETS[random.nextInt(STREETS.length)] + " Street, District " + (random.nextInt(12) + 1);
                double rating = 3.8 + (random.nextDouble() * 1.2);

                // SỬA TẠI ĐÂY: Thêm dấu \" bọc quanh %s của address
                writer.write(String.format(Locale.US, "%d,%s,\"%s\",%.1f\n", i, restName, address, rating));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 4. Sinh dữ liệu Menu Items
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

    // 5. Sinh dữ liệu Drivers
    private static void generateDrivers() {
        try (BufferedWriter writer = getWriter("drivers.csv")) {
            writer.write("id,name,status,current_lat,current_lng,version\n");
            String[] statuses = {"AVAILABLE", "BUSY"};
            for (int i = 1; i <= NUM_DRIVERS; i++) {
                String name = getRandomName();
                String status = statuses[random.nextInt(statuses.length)];
                double lat = 10.730000 + (random.nextDouble() * 0.15);
                double lng = 106.600000 + (random.nextDouble() * 0.15);

                writer.write(String.format(Locale.US, "%d,%s,%s,%.6f,%.6f,0\n", i, name, status, lat, lng));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 6. ĐỒNG BỘ TUYỆT ĐỐI: Sinh dữ liệu Orders, Order Items (Sửa triệt để lỗi 2, 3) và Delivery Routes
    private static void generateOrdersAndItemsAndRoutes(List<MenuItem> allMenuItems) {
        // Phân nhóm món ăn theo Nhà hàng để phục vụ việc chọn món không chéo hàng
        Map<Integer, List<MenuItem>> itemsByRest = new HashMap<>();
        for (MenuItem item : allMenuItems) {
            itemsByRest.computeIfAbsent(item.restaurantId, k -> new ArrayList<>()).add(item);
        }

        try (BufferedWriter orderWriter = getWriter("orders.csv"); BufferedWriter itemWriter = getWriter("order_items.csv"); BufferedWriter routeWriter = getWriter("delivery_routes.csv")) {

            orderWriter.write("order_id,customer_id,driver_id,total_price,status,version\n");
            itemWriter.write("order_item_id,order_id,menu_item_id,quantity,price_at_time\n");
            routeWriter.write("delivery_route_id,order_id,distance_km,estimated_time_min\n");

            int itemGlobalId = 1;

            for (int orderId = 1; orderId <= NUM_ORDERS; orderId++) {
                int customerId = random.nextInt(NUM_CUSTOMERS) + 1;

                // Chọn một nhà hàng bất kỳ để đặt đơn
                int targetRestId = random.nextInt(NUM_RESTAURANTS) + 1;
                List<MenuItem> availableItems = itemsByRest.get(targetRestId);

                // Phòng trường hợp nhà hàng rỗng món, chọn tạm sang nhà hàng số 1
                if (availableItems == null || availableItems.isEmpty()) {
                    targetRestId = 1;
                    availableItems = itemsByRest.get(1);
                }

                int numItemsInOrder = random.nextInt(3) + 1; // 1 đến 3 món khác nhau
                double computedTotalPrice = 0;
                List<String> bufferedItemsLines = new ArrayList<>();

                // Trộn danh sách món của nhà hàng đó để bốc ngẫu nhiên
                Collections.shuffle(availableItems);
                int countToPick = Math.min(numItemsInOrder, availableItems.size());

                for (int m = 0; m < countToPick; m++) {
                    MenuItem pickedItem = availableItems.get(m);
                    int qty = random.nextInt(3) + 1; // Số lượng từ 1 đến 3 phần
                    computedTotalPrice += (pickedItem.price * qty);

                    String itemLine = String.format(Locale.US, "%d,%d,%d,%d,%.1f\n",
                            itemGlobalId++, orderId, pickedItem.id, qty, pickedItem.price);
                    bufferedItemsLines.add(itemLine);
                }

                // Gán tài xế hợp lệ
                Integer driverId = (random.nextDouble() > 0.15) ? (random.nextInt(NUM_DRIVERS) + 1) : null;
                String orderStatus = (driverId == null) ? "PENDING" : (random.nextBoolean() ? "DELIVERING" : "DELIVERED");

                // Ghi vào file orders.csv với tổng tiền THỰC TẾ đã cộng dồn thành công
                orderWriter.write(String.format(Locale.US, "%d,%d,%s,%.1f,%s,0\n",
                        orderId, customerId, (driverId == null ? "" : driverId), computedTotalPrice, orderStatus));

                // Xả dữ liệu chi tiết tương ứng vào file order_items.csv
                for (String line : bufferedItemsLines) {
                    itemWriter.write(line);
                }

                // Tiện tay tạo luôn lộ trình giao hàng cho đơn này tương ứng
                double distance = 1.0 + (random.nextDouble() * 14.0); // 1km - 15km
                int estTime = (int) (distance * 3) + random.nextInt(5); // ~3 phút/km

                routeWriter.write(String.format(Locale.US, "%d,%d,%.2f,%d\n", orderId, orderId, distance, estTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void generateSimulationRuns() {
        try (BufferedWriter writer = getWriter("simulation_runs.csv")) {
            // Dòng tiêu đề chuẩn theo yêu cầu của bạn
            writer.write("simulation_run_id,total_orders,total_success,total_failed,duration_ms\n");

            for (int i = 1; i <= NUM_SIMULATION_RUNS; i++) {
                // Tổng số đơn mô phỏng từ 200 đến 1000 đơn
                int totalOrders = 200 + random.nextInt(800);

                // Tính toán tỷ lệ thành công ngẫu nhiên cao (từ 90% đến 98%) để dữ liệu thực tế
                double successRate = 0.90 + (random.nextDouble() * 0.08);
                int totalSuccess = (int) (totalOrders * successRate);

                // Số đơn thất bại là phần còn lại
                int totalFailed = totalOrders - totalSuccess;

                // Thời gian chạy tính bằng mili-giây (ví dụ từ 500ms đến 3000ms cho các thuật toán tối ưu)
                int durationMs = 500 + random.nextInt(2500);

                // Ghi thẳng hàng vào file CSV
                writer.write(String.format(Locale.US, "%d,%d,%d,%d,%d\n",
                        i, totalOrders, totalSuccess, totalFailed, durationMs));
            }
            System.out.println("✅ Nạp xong file dữ liệu cấu trúc mới: simulation_runs.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
