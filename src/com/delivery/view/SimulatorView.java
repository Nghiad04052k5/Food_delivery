package com.delivery.view;

import com.delivery.controller.CustomerController;
import com.delivery.controller.DriverController;
import com.delivery.controller.OrderController;
import com.delivery.controller.RestaurantController;
import com.delivery.model.Customer;
import com.delivery.model.Driver;
import com.delivery.model.MenuItem;
import com.delivery.model.Restaurant;
import com.delivery.model.RestaurantStatus;

import java.util.List;
import java.util.Scanner;

/**
 * SimulatorView - Giao diện Console để nhập liệu và điều hướng thao tác mô phỏng.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Hiển thị menu chính dạng ASCII đẹp mắt
 * - Nhận input từ người dùng và gọi Sub-Controllers tương ứng
 * - Hỗ trợ các luồng: Đăng ký KH, Xem nhà hàng, Đổi trạng thái tài xế
 */
public class SimulatorView {

    // ANSI color codes để làm đẹp giao diện Console
    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";

    private final Scanner scanner;
    private final CustomerController customerController;
    private final RestaurantController restaurantController;
    private final DriverController driverController;
    private final OrderController orderController;

    public SimulatorView(CustomerController customerController,
                         RestaurantController restaurantController,
                         DriverController driverController,
                         OrderController orderController) {
        this.scanner = new Scanner(System.in);
        this.customerController = customerController;
        this.restaurantController = restaurantController;
        this.driverController = driverController;
        this.orderController = orderController;
    }

    /**
     * Điểm vào chính - khởi động vòng lặp menu Console.
     */
    public void start() {
        printWelcomeBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Nhap lua chon cua ban: ");
            switch (choice) {
                case 1 -> handleRegisterCustomer();
                case 2 -> handleViewOpenRestaurants();
                case 3 -> handleViewMenu();
                case 4 -> handleDriverGoOnline();
                case 5 -> handleDriverGoOffline();
                case 6 -> handleFindNearestDriver();
                case 7 -> handleViewAvailableDrivers();
                case 8 -> {
                    printLine(YELLOW + "Thoat chuong trinh. Tam biet!" + RESET);
                    running = false;
                }
                default -> printLine(RED + "[!] Lua chon khong hop le. Vui long thu lai." + RESET);
            }
        }
        scanner.close();
    }

    // =========================================================
    // MENU & BANNER
    // =========================================================

    private void printWelcomeBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║       FOOD DELIVERY SIMULATION SYSTEM v1.0           ║");
        System.out.println("║       LAB211 - FPT University                        ║");
        System.out.println("║       Thanh vien 3: Tuan (Sub-Controllers & Views)   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    private void printMainMenu() {
        System.out.println(CYAN + "\n┌─────────────────────────────────────────┐");
        System.out.println("│           MENU CHINH (SIMULATOR)        │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│  1. Dang ky Khach hang moi              │");
        System.out.println("│  2. Xem danh sach Nha hang dang mo      │");
        System.out.println("│  3. Xem Menu (Mon an) cua Nha hang      │");
        System.out.println("│  4. Tai xe: Bat dau nhan don (AVAILABLE)│");
        System.out.println("│  5. Tai xe: Ngung nhan don (OFFLINE)    │");
        System.out.println("│  6. Tim tai xe gan Nha hang nhat        │");
        System.out.println("│  7. Xem tat ca tai xe dang AVAILABLE    │");
        System.out.println("│  8. Thoat                               │");
        System.out.println("└─────────────────────────────────────────┘" + RESET);
    }

    // =========================================================
    // CÁC HANDLER XỬ LÝ TỪNG CHỨC NĂNG
    // =========================================================

    private void handleRegisterCustomer() {
        printLine(GREEN + "\n--- DANG KY KHACH HANG MOI ---" + RESET);
        String name    = readString("Ten khach hang: ");
        String phone   = readString("So dien thoai: ");
        String address = readString("Dia chi: ");
        double lat     = readDouble("Vi do (latitude, vi du 10.78): ");
        double lon     = readDouble("Kinh do (longitude, vi du 106.65): ");
        String email   = readString("Email: ");
        String pass    = readString("Mat khau: ");

        Customer c = customerController.registerCustomer(name, phone, address, lat, lon, email, pass);
        printLine(GREEN + "[OK] Dang ky thanh cong! ID=" + c.getId() + RESET);
    }

    private void handleViewOpenRestaurants() {
        printLine(GREEN + "\n--- DANH SACH NHA HANG DANG MO CUA ---" + RESET);
        List<Restaurant> list = restaurantController.getOpenRestaurants();
        if (list.isEmpty()) {
            printLine(RED + "Khong co nha hang nao dang mo cua." + RESET);
            return;
        }
        printTableHeader("%-6s | %-30s | %-15s | %-7s", "ID", "TEN NHA HANG", "DIA CHI (truncated)", "RATING");
        for (Restaurant r : list) {
            String shortAddr = r.getAddress().length() > 15
                    ? r.getAddress().substring(0, 12) + "..." : r.getAddress();
            System.out.printf("%-6d | %-30s | %-15s | %.1f%n",
                    r.getId(), r.getName(), shortAddr, r.getRating());
        }
    }

    private void handleViewMenu() {
        printLine(GREEN + "\n--- XEM MENU NHA HANG ---" + RESET);
        int restaurantId = readInt("Nhap ID nha hang: ");
        List<MenuItem> menu = restaurantController.getMenuByRestaurant(restaurantId);
        if (menu.isEmpty()) {
            printLine(RED + "Nha hang nay chua co mon an nao." + RESET);
            return;
        }
        printTableHeader("%-6s | %-25s | %-12s | %-8s", "ID", "TEN MON", "GIA (VND)", "TON KHO");
        for (MenuItem item : menu) {
            System.out.printf("%-6d | %-25s | %,-12.0f | %d%n",
                    item.getId(), item.getItemName(), item.getPrice(), item.getStockQty());
        }
    }

    private void handleDriverGoOnline() {
        printLine(GREEN + "\n--- TAI XE: BAT DAU NHAN DON ---" + RESET);
        int driverId = readInt("Nhap ID tai xe: ");
        driverController.goOnline(driverId);
    }

    private void handleDriverGoOffline() {
        printLine(GREEN + "\n--- TAI XE: NGUNG NHAN DON ---" + RESET);
        int driverId = readInt("Nhap ID tai xe: ");
        driverController.goOffline(driverId);
    }

    private void handleFindNearestDriver() {
        printLine(GREEN + "\n--- TIM TAI XE GAN NHA HANG NHAT ---" + RESET);
        double lat = readDouble("Vi do nha hang (latitude): ");
        double lon = readDouble("Kinh do nha hang (longitude): ");
        Driver nearest = driverController.findNearest(lat, lon);
        if (nearest != null) {
            printLine(GREEN + "[KET QUA] Tai xe duoc chon: ID=" + nearest.getId()
                    + ", Ten=" + nearest.getName() + RESET);
        }
    }

    private void handleViewAvailableDrivers() {
        printLine(GREEN + "\n--- DANH SACH TAI XE DANG SAN SANG ---" + RESET);
        List<Driver> list = driverController.getAvailableDrivers();
        if (list.isEmpty()) {
            printLine(RED + "Khong co tai xe AVAILABLE nao." + RESET);
            return;
        }
        printTableHeader("%-6s | %-20s | %-12s | %-10s", "ID", "TEN", "SDT", "TRANG THAI");
        for (Driver d : list) {
            System.out.printf("%-6d | %-20s | %-12s | %s%n",
                    d.getId(), d.getName(), d.getPhone(), d.getStatus());
        }
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private void printLine(String msg) {
        System.out.println(msg);
    }

    private void printTableHeader(String format, String... headers) {
        String separator = "─".repeat(70);
        System.out.println(YELLOW + separator + RESET);
        System.out.printf(BOLD + format + RESET + "%n", (Object[]) headers);
        System.out.println(YELLOW + separator + RESET);
    }

    private String readString(String prompt) {
        System.out.print(CYAN + prompt + RESET);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(CYAN + prompt + RESET);
                int val = Integer.parseInt(scanner.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                printLine(RED + "[!] Vui long nhap so nguyen hop le." + RESET);
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(CYAN + prompt + RESET);
                double val = Double.parseDouble(scanner.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                printLine(RED + "[!] Vui long nhap so thap phan hop le (vi du: 10.78)." + RESET);
            }
        }
    }


    /**
     * NHIỆM VỤ BẮT BUỘC: Hiển thị bảng thông số cấu hình chuẩn 8 cột ký tự ASCII
     */
    public void displaySimulationConfigTable(int threads, int orders, String lockMechanism) {
        System.out.println("\n+" + "═".repeat(112) + "+");
        System.out.println("|" + String.format(" %-110s ", "BẢNG CẤU HÌNH THÔNG SỐ MÔ PHỎNG HỆ THỐNG GIAO HÀNG ĐA LUỒNG (CHUYÊN NGHIỆP)") + "|");
        System.out.println("+" + "─".repeat(112) + "+");
        
        // Định dạng tiêu đề đúng chuẩn 8 cột
        System.out.println(String.format("| %-5s | %-25s | %-10s | %-10s | %-15s | %-12s | %-10s | %-6s |", 
            "COL 1", "COL 2 (THAM SỐ)", "COL 3 (VAL)", "COL 4(UNIT)", "COL 5 (LOCK)", "COL 6 (STAT)", "COL 7 (PRI)", "COL 8"));
        System.out.println(String.format("| %-5s | %-25s | %-10s | %-10s | %-15s | %-12s | %-10s | %-6s |", 
            "STT", "Tên Thông Số Giả Lập", "Giá Trị", "Đơn Vị", "Cơ Chế Đồng Bộ", "Trạng Thái", "Độ Ưu Tiên", "Kết Luận"));
        System.out.println("+" + "─".repeat(112) + "+");
        
        // Dòng dữ liệu 1
        System.out.println(String.format("| %-5s | %-25s | %-10d | %-10s | %-15s | %-12s | %-10s | %-6s |", 
            "01", "Concurrent Threads", threads, "Luồng", lockMechanism, "SẴN SÀNG", "CAO", "OK"));
            
        // Dòng dữ liệu 2
        System.out.println(String.format("| %-5s | %-25s | %-10d | %-10s | %-15s | %-12s | %-10s | %-6s |", 
            "02", "Total Test Orders", orders, "Đơn hàng", lockMechanism, "SẴN SÀNG", "TRUNG BÌNH", "OK"));
            
        System.out.println("+" + "═".repeat(112) + "+\n");
    }
}
