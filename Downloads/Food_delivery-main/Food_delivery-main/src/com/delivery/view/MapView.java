package com.delivery.view;

/**
 * MapView - Giao diện hiển thị bản đồ ASCII với tọa độ được transform thành lưới ma trận.
 * 
 * Chức năng:
 * - Nội suy (interpolation) dải tọa độ (Kinh độ/Vĩ độ) thành tọa độ trong lưới ASCII
 * - Vẽ vị trí Khách hàng (C), Nhà hàng (R), Tài xế (D) trên lưới
 * - Hiển thị quãng đường giao hàng bằng ASCII art
 */
public class MapView {

    // ANSI color codes
    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";
    private static final String BLUE   = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";

    // Kích thước lưới
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;

    // Dải tọa độ Hồ Chí Minh
    private static final double MIN_LAT = 10.72;
    private static final double MAX_LAT = 10.85;
    private static final double MIN_LON = 106.60;
    private static final double MAX_LON = 106.75;

    private char[][] grid;

    public MapView() {
        initializeGrid();
    }

    private void initializeGrid() {
        grid = new char[GRID_HEIGHT][GRID_WIDTH];
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                grid[i][j] = '.';
            }
        }
    }

    /**
     * Transform tọa độ (latitude, longitude) thành tọa độ trong lưới ASCII
     */
    private int[] transformToGrid(double latitude, double longitude) {
        // Tính tỷ lệ phần trăm của vị trí trong dải
        double latRatio = (latitude - MIN_LAT) / (MAX_LAT - MIN_LAT);
        double lonRatio = (longitude - MIN_LON) / (MAX_LON - MIN_LON);

        // Clamp vào [0, 1]
        latRatio = Math.max(0, Math.min(1, latRatio));
        lonRatio = Math.max(0, Math.min(1, lonRatio));

        // Transform thành tọa độ lưới
        int x = (int) (lonRatio * (GRID_WIDTH - 1));
        int y = (int) (latRatio * (GRID_HEIGHT - 1));

        return new int[]{x, y};
    }

    /**
     * Đặt vị trí khách hàng
     */
    public void setCustomer(double latitude, double longitude, int customerId) {
        int[] pos = transformToGrid(latitude, longitude);
        if (isValidPosition(pos[0], pos[1])) {
            grid[pos[1]][pos[0]] = 'C';
        }
    }

    /**
     * Đặt vị trí nhà hàng
     */
    public void setRestaurant(double latitude, double longitude, int restaurantId) {
        int[] pos = transformToGrid(latitude, longitude);
        if (isValidPosition(pos[0], pos[1])) {
            grid[pos[1]][pos[0]] = 'R';
        }
    }

    /**
     * Đặt vị trí tài xế
     */
    public void setDriver(double latitude, double longitude, int driverId) {
        int[] pos = transformToGrid(latitude, longitude);
        if (isValidPosition(pos[0], pos[1])) {
            grid[pos[1]][pos[0]] = 'D';
        }
    }

    /**
     * Vẽ đường giao hàng từ một điểm đến điểm khác
     */
    public void drawDeliveryRoute(double lat1, double lon1, double lat2, double lon2) {
        int[] start = transformToGrid(lat1, lon1);
        int[] end = transformToGrid(lat2, lon2);

        drawLine(start[0], start[1], end[0], end[1]);
    }

    /**
     * Thuật toán Bresenham để vẽ đường thẳng
     */
    private void drawLine(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0, y = y0;
        while (true) {
            if (isValidPosition(x, y) && grid[y][x] == '.') {
                grid[y][x] = '-';
            }

            if (x == x1 && y == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT;
    }

    /**
     * In bản đồ lên console
     */
    public void printMap() {
        System.out.println();
        System.out.println(CYAN + BOLD + "╔" + repeatChar('═', GRID_WIDTH * 2 + 2) + "╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + " BAO CAO DUONG GIAO HANG (ASCII MAP)" + repeatChar(' ', GRID_WIDTH * 2 - 33) + CYAN + "║" + RESET);
        System.out.println(CYAN + BOLD + "╠" + repeatChar('═', GRID_WIDTH * 2 + 2) + "╣" + RESET);

        // In lưới
        for (int y = 0; y < GRID_HEIGHT; y++) {
            System.out.print(CYAN + "║" + RESET);
            for (int x = 0; x < GRID_WIDTH; x++) {
                char cell = grid[y][x];
                String display;
                switch (cell) {
                    case 'C':
                        display = GREEN + "C" + RESET;
                        break;
                    case 'R':
                        display = RED + "R" + RESET;
                        break;
                    case 'D':
                        display = YELLOW + "D" + RESET;
                        break;
                    case '-':
                        display = BLUE + "-" + RESET;
                        break;
                    default:
                        display = ".";
                }
                System.out.print(display + " ");
            }
            System.out.println(CYAN + "║" + RESET);
        }

        System.out.println(CYAN + BOLD + "╚" + repeatChar('═', GRID_WIDTH * 2 + 2) + "╝" + RESET);
        System.out.println();

        // In huyền
        System.out.println(CYAN + "Legend:" + RESET);
        System.out.println("  " + GREEN + "C" + RESET + " - Khach hang (Customer)");
        System.out.println("  " + RED + "R" + RESET + " - Nha hang (Restaurant)");
        System.out.println("  " + YELLOW + "D" + RESET + " - Tai xe (Driver)");
        System.out.println("  " + BLUE + "-" + RESET + " - Duong giao hang (Delivery Route)");
        System.out.println();
    }

    /**
     * In bản đồ kèm thông tin chi tiết
     */
    public void printDetailedMap(String orderId, String customerName, String restaurantName,
                                  String driverName, double distance, int estimatedTime) {
        System.out.println();
        System.out.println(BOLD + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + "║ DON HANG #" + orderId + " - THONG TIN DUONG GIAO HANG" + repeatChar(' ', 30) + "║" + RESET);
        System.out.println(BOLD + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(String.format("║ Khach hang: %-50s ║", truncate(customerName, 50)));
        System.out.println(String.format("║ Nha hang:   %-50s ║", truncate(restaurantName, 50)));
        System.out.println(String.format("║ Tai xe:     %-50s ║", truncate(driverName, 50)));
        System.out.println(String.format("║ Quang duong: %-48.1f km ║", distance));
        System.out.println(String.format("║ Thoi gian du kien: %-40d phut ║", estimatedTime));
        System.out.println(BOLD + "╠════════════════════════════════════════════════════════════════╣" + RESET);

        printMap();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 2) + "..";
    }

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
