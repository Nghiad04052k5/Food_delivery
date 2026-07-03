package com.delivery.view;

import com.delivery.controller.CustomerController;
import com.delivery.controller.DriverController;
import com.delivery.controller.OrderController;
import com.delivery.controller.RestaurantController;
import com.delivery.model.*;
import com.delivery.repository.CustomerRepository;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.MenuItemRepository;
import com.delivery.repository.OrderItemRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.repository.RestaurantRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * SimulatorView - Giao diện Console đầy đủ cho hệ thống Food Delivery.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Quản lý giao diện 4 vai trò: Người dùng, Nhà hàng, Tài xế, Quản trị viên
 * - CHỈ in ra menu và đọc input từ người dùng
 * - Mọi logic nghiệp vụ được ủy quyền cho các Controller tương ứng
 */
public class SimulatorView {

    private final Scanner scanner;

    // Các Controller được inject từ MainApplication
    private final CustomerController   customerController;
    private final RestaurantController restaurantController;
    private final DriverController     driverController;
    private final OrderController      orderController;

    // Repository chỉ dùng trực tiếp cho Admin (thống kê tổng hợp, không cần controller riêng)
    private final CustomerRepository  customerRepo;
    private final RestaurantRepository restaurantRepo;
    private final DriverRepository    driverRepo;
    private final OrderRepository     orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final MenuItemRepository  menuItemRepo;

    // Lưu đánh giá tạm thời trong bộ nhớ (In-memory, chưa có FeedbackRepository)
    static class Feedback {
        int    restaurantId;
        int    orderId;
        String customerName;
        int    stars;
        String comment;
    }
    private final List<Feedback> feedbacks = new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR - nhận tất cả Controller + Repository từ MainApplication
    // =========================================================
    public SimulatorView(CustomerController customerController,
                         RestaurantController restaurantController,
                         DriverController driverController,
                         OrderController orderController,
                         CustomerRepository customerRepo,
                         RestaurantRepository restaurantRepo,
                         DriverRepository driverRepo,
                         OrderRepository orderRepo,
                         OrderItemRepository orderItemRepo,
                         MenuItemRepository menuItemRepo) {
        this.scanner              = new Scanner(System.in);
        this.customerController   = customerController;
        this.restaurantController = restaurantController;
        this.driverController     = driverController;
        this.orderController      = orderController;
        this.customerRepo         = customerRepo;
        this.restaurantRepo       = restaurantRepo;
        this.driverRepo           = driverRepo;
        this.orderRepo            = orderRepo;
        this.orderItemRepo        = orderItemRepo;
        this.menuItemRepo         = menuItemRepo;
    }

    // =========================================================
    // ĐIỂM VÀO CHÍNH - gọi từ MainApplication
    // =========================================================

    /**
     * Khởi chạy vòng lặp menu chính.
     */
    public void start() {
        System.out.println("Chao mung den voi He Thong Food Delivery!");

        while (true) {
            System.out.println("\n--- CHON VAI TRO ---");
            System.out.println("1. Nguoi dung (User)");
            System.out.println("2. Nha hang (Restaurant)");
            System.out.println("3. Tai xe (Driver)");
            System.out.println("4. Quan tri vien (Admin)");
            System.out.println("0. Thoat");
            System.out.print("Vui long chon (0-4): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": handleUserMenu();       break;
                case "2": handleRestaurantMenu(); break;
                case "3": handleDriverMenu();     break;
                case "4": handleAdminMenu();      break;
                case "0":
                    System.out.println("Cam on ban da su dung dich vu!");
                    return;
                default:
                    System.out.println("Lua chon khong hop le. Vui long chon lai.");
            }
        }
    }

    // =========================================================
    // HELPER - Băm mật khẩu SHA-256 (chỉ dùng ở tầng View để truyền vào Controller)
    // =========================================================
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================
    // MENU NGUOI DUNG (USER)
    // =========================================================

    private void handleUserMenu() {
        Customer loggedInCustomer = null;

        // Vòng lặp đăng nhập / đăng ký
        while (loggedInCustomer == null) {
            System.out.println("\n--- MENU NGUOI DUNG ---");
            System.out.println("1. Dang nhap");
            System.out.println("2. Dang ky");
            System.out.println("0. Quay lai");
            System.out.print("Chon: ");
            String c = scanner.nextLine();

            if (c.equals("1")) {
                System.out.print("Nhap email: ");
                String email = scanner.nextLine();
                System.out.print("Nhap password: ");
                String pass = scanner.nextLine();
                // Gọi CustomerController.login()
                loggedInCustomer = customerController.login(email, hashPassword(pass));
                if (loggedInCustomer == null) {
                    System.out.println("Tai khoan khong ton tai hoac sai mat khau!");
                }

            } else if (c.equals("2")) {
                System.out.print("Nhap ten: ");    String name    = scanner.nextLine();
                System.out.print("Nhap SDT: ");    String phone   = scanner.nextLine();
                System.out.print("Nhap dia chi: "); String address = scanner.nextLine();
                System.out.print("Nhap email: ");  String email   = scanner.nextLine();
                System.out.print("Nhap password: ");
                String hashedPass = hashPassword(scanner.nextLine());
                // Gọi CustomerController.registerCustomer()
                customerController.registerCustomer(name, phone, address, 10.75, 106.65, email, hashedPass);
                System.out.println("Dang ky thanh cong! Ban co the dang nhap.");

            } else if (c.equals("0")) {
                return;
            }
        }

        // Menu sau khi đăng nhập thành công
        while (true) {
            System.out.println("\n--- MENU NGUOI DUNG: " + loggedInCustomer.getName() + " ---");
            System.out.println("1. Xem Profile");
            System.out.println("2. Cap nhat Profile (Ten, SDT, Dia chi)");
            System.out.println("3. Xem danh sach nha hang & Tim kiem");
            System.out.println("4. Dat hang");
            System.out.println("5. Theo doi don hang");
            System.out.println("6. Huy don");
            System.out.println("7. Lich su don hang");
            System.out.println("8. Danh gia");
            System.out.println("9. Xem ban do & chi tiet don hang");
            System.out.println("0. Dang xuat");
            System.out.print("Chon: ");
            String uc = scanner.nextLine();

            if (uc.equals("1")) {
                // In thông tin, không cần gọi Controller
                System.out.println("Thong tin hien tai:");
                System.out.println("- Ten: "    + loggedInCustomer.getName());
                System.out.println("- SDT: "    + loggedInCustomer.getPhone());
                System.out.println("- Dia chi: " + loggedInCustomer.getAddress());

            } else if (uc.equals("2")) {
                System.out.print("Ten moi (Enter de giu nguyen): ");   String n = scanner.nextLine();
                System.out.print("SDT moi (Enter de giu nguyen): ");   String p = scanner.nextLine();
                System.out.print("Dia chi moi (Enter de giu nguyen): "); String a = scanner.nextLine();
                // Gọi CustomerController.updateProfile()
                customerController.updateProfile(loggedInCustomer, n, p, a);
                System.out.println("Cap nhat thanh cong!");

            } else if (uc.equals("3")) {
                System.out.print("Nhap ten nha hang de tim kiem (Bo trong de xem tat ca): ");
                String kw = scanner.nextLine();
                // Gọi RestaurantController.searchOpenRestaurants()
                List<Restaurant> shops = restaurantController.searchOpenRestaurants(kw);
                if (shops.isEmpty()) {
                    System.out.println("Khong tim thay nha hang nao!");
                } else {
                    for (Restaurant r : shops) {
                        System.out.println(r.getId() + ". " + r.getName()
                                + " | SDT: " + r.getPhone()
                                + " | Danh gia: " + r.getRating() + " sao");
                    }
                }

            } else if (uc.equals("4")) {
                System.out.print("Nhap ID nha hang ban muon dat: ");
                int restId;
                try { restId = Integer.parseInt(scanner.nextLine()); }
                catch (NumberFormatException e) { System.out.println("ID khong hop le!"); continue; }

                // Gọi RestaurantController.getMenuByRestaurant()
                List<MenuItem> menu = restaurantController.getMenuByRestaurant(restId);
                if (menu.isEmpty()) {
                    System.out.println("Nha hang nay khong co mon nao!");
                    continue;
                }

                System.out.println("\n--- MENU NHA HANG ---");
                for (MenuItem m : menu) {
                    System.out.println(m.getId() + ". " + m.getItemName()
                            + " - Gia: " + m.getPrice()
                            + " - Ton kho: " + m.getStockQty());
                }

                // Giỏ hàng
                List<OrderItem> cart  = new ArrayList<>();
                double          total = 0;
                while (true) {
                    System.out.print("Nhap ID mon de them vao gio (hoac 0 de thanh toan, -1 de huy): ");
                    int itemId;
                    try { itemId = Integer.parseInt(scanner.nextLine()); }
                    catch (NumberFormatException e) { System.out.println("Nhap so nguyen hop le!"); continue; }
                    if (itemId == 0) break;
                    if (itemId == -1) { cart.clear(); break; }

                    final int finalItemId = itemId;
                    MenuItem selected = menu.stream()
                            .filter(m -> m.getId() == finalItemId).findFirst().orElse(null);
                    if (selected == null) {
                        System.out.println("Mon khong ton tai!");
                        continue;
                    }
                    System.out.print("Nhap so luong: ");
                    int qty;
                    try { qty = Integer.parseInt(scanner.nextLine()); }
                    catch (NumberFormatException e) { continue; }

                    if (qty > selected.getStockQty()) {
                        System.out.println("Kho khong du!");
                    } else {
                        OrderItem oi = new OrderItem();
                        oi.setMenuItemId(itemId);
                        oi.setQuantity(qty);
                        oi.setPriceAtTime(selected.getPrice());
                        cart.add(oi);
                        total += selected.getPrice() * qty;
                        System.out.println("Da them " + qty + " x " + selected.getItemName() + " vao gio.");
                    }
                }

                if (!cart.isEmpty()) {
                    System.out.println("\n=== HOA DON CUA BAN ===");
                    System.out.println("Tong tien: " + total + " VND");
                    System.out.print("Xac nhan dat hang va thanh toan? (y/n): ");
                    if (scanner.nextLine().equalsIgnoreCase("y")) {
                        // Tạo Order
                        int orderId = orderRepo.readAll().stream()
                                .mapToInt(o -> o.getId()).max().orElse(0) + 1;
                        Order order = new Order();
                        order.setId(orderId);
                        order.setCustomerId(loggedInCustomer.getId());
                        order.setTotalPrice(total);
                        order.setPaymentMethod(PaymentMethod.CASH);
                        order.setStatus(OrderStatus.PENDING);
                        order.setVersion(0);
                        // Gọi OrderController.placeOrder()
                        orderController.placeOrder(order);

                        // Lưu từng OrderItem và trừ kho
                        for (OrderItem oi : cart) {
                            oi.setId(orderItemRepo.readAll().stream()
                                    .mapToInt(o -> o.getId()).max().orElse(0) + 1);
                            oi.setOrderId(orderId);
                            orderItemRepo.save(oi);
                            // Gọi RestaurantController.deductMenuItemStock()
                            restaurantController.deductMenuItemStock(oi.getMenuItemId(), oi.getQuantity());
                        }
                        System.out.println("Dat hang thanh cong! Ma don: " + orderId);
                    }
                }

            } else if (uc.equals("5")) {
                final int cid = loggedInCustomer.getId();
                List<Order> activeOrders = orderRepo.readAll().stream()
                        .filter(o -> o.getCustomerId() == cid
                                && o.getStatus() != OrderStatus.DELIVERED
                                && o.getStatus() != OrderStatus.CANCELLED)
                        .collect(java.util.stream.Collectors.toList());
                if (activeOrders.isEmpty()) {
                    System.out.println("Ban khong co don hang nao dang xu ly.");
                } else {
                    for (Order o : activeOrders) {
                        System.out.println("Don #" + o.getId()
                                + " - Trang thai: " + o.getStatus()
                                + " - Tong: " + o.getTotalPrice());
                    }
                }

            } else if (uc.equals("6")) {
                final int cid = loggedInCustomer.getId();
                List<Order> activeOrders = orderRepo.readAll().stream()
                        .filter(o -> o.getCustomerId() == cid
                                && o.getStatus() != OrderStatus.DELIVERED
                                && o.getStatus() != OrderStatus.CANCELLED)
                        .collect(java.util.stream.Collectors.toList());
                if (activeOrders.isEmpty()) {
                    System.out.println("Ban khong co don hang nao dang xu ly.");
                } else {
                    for (Order o : activeOrders) {
                        System.out.println("Don #" + o.getId()
                                + " - Trang thai: " + o.getStatus()
                                + " - Tong: " + o.getTotalPrice());
                    }
                    System.out.print("Nhap ID don hang muon huy (hoac 0 de thoat): ");
                    int cancelId;
                    try { cancelId = Integer.parseInt(scanner.nextLine()); }
                    catch (NumberFormatException e) { cancelId = 0; }

                    if (cancelId > 0) {
                        final int finalCancelId = cancelId;
                        Order toCancel = activeOrders.stream()
                                .filter(o -> o.getId() == finalCancelId).findFirst().orElse(null);
                        if (toCancel != null && toCancel.getStatus() == OrderStatus.PENDING) {
                            toCancel.setStatus(OrderStatus.CANCELLED);
                            // Gọi OrderController.saveOrder() để lưu trạng thái mới
                            orderController.saveOrder(toCancel);
                            System.out.println("Da huy don hang thanh cong!");
                        } else if (toCancel != null) {
                            System.out.println("Khong the huy don hang nay do da duoc nha hang xu ly!");
                        }
                    }
                }

            } else if (uc.equals("7")) {
                final int cid = loggedInCustomer.getId();
                List<Order> pastOrders = orderRepo.readAll().stream()
                        .filter(o -> o.getCustomerId() == cid
                                && (o.getStatus() == OrderStatus.DELIVERED
                                    || o.getStatus() == OrderStatus.CANCELLED))
                        .collect(java.util.stream.Collectors.toList());
                if (pastOrders.isEmpty()) {
                    System.out.println("Chua co lich su don hang nao.");
                } else {
                    for (Order o : pastOrders) {
                        System.out.println("Don #" + o.getId()
                                + " - Trang thai: " + o.getStatus()
                                + " - Tong: " + o.getTotalPrice());
                    }
                }

            } else if (uc.equals("8")) {
                System.out.print("Ban muon danh gia don hang nao? (Nhap ID don, hoac 0 bo qua): ");
                int rateId;
                try { rateId = Integer.parseInt(scanner.nextLine()); }
                catch (NumberFormatException e) { rateId = 0; }

                if (rateId > 0) {
                    System.out.print("Nhap so sao (1-5): ");
                    int stars;
                    try { stars = Integer.parseInt(scanner.nextLine()); }
                    catch (NumberFormatException e) { stars = 5; }
                    System.out.print("Nhap nhan xet cua ban: ");
                    String comment = scanner.nextLine();

                    // Dò nhà hàng của đơn thông qua OrderItemRepo + MenuItemRepo (truy vấn dữ liệu)
                    final int finalRateId = rateId;
                    OrderItem oi = orderItemRepo.readAll().stream()
                            .filter(item -> item.getOrderId() == finalRateId)
                            .findFirst().orElse(null);
                    if (oi != null) {
                        // Gọi RestaurantController.getMenuItemById()
                        MenuItem mi = restaurantController.getMenuItemById(oi.getMenuItemId());
                        if (mi != null) {
                            Feedback fb = new Feedback();
                            fb.restaurantId  = mi.getRestaurantId();
                            fb.orderId       = rateId;
                            fb.customerName  = loggedInCustomer.getName();
                            fb.stars         = stars;
                            fb.comment       = comment;
                            feedbacks.add(fb);
                            System.out.println("Cam on ban da danh gia! Nhan xet da duoc gui den nha hang.");
                        } else {
                            System.out.println("Khong tim thay nha hang cho don nay!");
                        }
                    } else {
                        System.out.println("Khong tim thay don hang!");
                    }
                }

            } else if (uc.equals("9")) {
                final int cid = loggedInCustomer.getId();
                List<Order> allUserOrders = orderRepo.readAll().stream()
                        .filter(o -> o.getCustomerId() == cid)
                        .collect(java.util.stream.Collectors.toList());
                
                if (allUserOrders.isEmpty()) {
                    System.out.println("Ban khong co don hang nao.");
                    continue;
                }
                
                System.out.println("\n--- DANH SACH DON HANG ---");
                for (Order o : allUserOrders) {
                    System.out.println("Don #" + o.getId() + " - Trang thai: " + o.getStatus() + " - Tong: " + o.getTotalPrice());
                }
                System.out.print("Nhap ID don de xem ban do & chi tiet (hoac 0 de thoat): ");
                int viewOrderIdInput;
                try { viewOrderIdInput = Integer.parseInt(scanner.nextLine()); }
                catch (NumberFormatException e) { viewOrderIdInput = 0; }
                final int viewOrderId = viewOrderIdInput;
                
                if (viewOrderId > 0) {
                    Order selectedOrder = allUserOrders.stream()
                            .filter(o -> o.getId() == viewOrderId).findFirst().orElse(null);
                    if (selectedOrder != null) {
                        // Hien thi chi tiet don hang
                        Customer cust = customerRepo.findById(selectedOrder.getCustomerId());
                        
                        // Lay ID nha hang tu OrderItem -> MenuItem
                        Integer restId = null;
                        List<OrderItem> orderItems = orderItemRepo.readAll().stream()
                                .filter(oi -> oi.getOrderId() == selectedOrder.getId())
                                .collect(java.util.stream.Collectors.toList());
                        if (!orderItems.isEmpty()) {
                            MenuItem mi = menuItemRepo.findById(orderItems.get(0).getMenuItemId());
                            if (mi != null) restId = mi.getRestaurantId();
                        }
                        Restaurant rest = restId != null ? restaurantRepo.findById(restId) : null;
                        Driver drv = selectedOrder.getDriverId() != null ? driverRepo.findById(selectedOrder.getDriverId()) : null;
                        
                        OrderView orderView = new OrderView("data/order_items.csv");
                        orderView.printOrderDetail(selectedOrder, cust, rest, drv);
                        
                        // Hien thi ban do neu co tai xe
                        if (drv != null && rest != null) {
                            System.out.println();
                            MapView mapView = new MapView();
                            if (cust != null) mapView.setCustomer(cust.getLatitude(), cust.getLongitude(), cust.getId());
                            mapView.setRestaurant(rest.getLatitude(), rest.getLongitude(), rest.getId());
                            mapView.setDriver(drv.getLatitude(), drv.getLongitude(), drv.getId());
                            mapView.drawDeliveryRoute(rest.getLatitude(), rest.getLongitude(), cust.getLatitude(), cust.getLongitude());
                            mapView.printDetailedMap(String.valueOf(selectedOrder.getId()), 
                                    cust != null ? cust.getName() : "Unknown",
                                    rest.getName(),
                                    drv.getName(),
                                    0.0,  // distance placeholder
                                    0);   // time placeholder
                        }
                        
                        // Hien thi danh sach mon an
                        List<MenuItem> items = menuItemRepo.readAll();
                        orderView.printOrderItems(selectedOrder.getId(), items, orderItems);
                        orderView.printPriceSummary(selectedOrder);
                    }
                }
            
            } else if (uc.equals("0")) {
                break;
            }
        }
    }

    // =========================================================
    // MENU NHA HANG (RESTAURANT)
    // =========================================================

    private void handleRestaurantMenu() {
        System.out.print("Nhap ID nha hang cua ban (Dang nhap): ");
        int restId;
        try { restId = Integer.parseInt(scanner.nextLine()); }
        catch (NumberFormatException e) { System.out.println("ID khong hop le!"); return; }

        // Gọi RestaurantController.getRestaurantById()
        Restaurant loggedInRest = restaurantController.getRestaurantById(restId);
        if (loggedInRest == null) {
            System.out.println("Khong tim thay nha hang!");
            return;
        }

        while (true) {
            System.out.println("\n--- QUAN LY NHA HANG: " + loggedInRest.getName() + " ---");
            System.out.println("1. Dong / Mo nha hang (Hien tai: " + loggedInRest.getStatus() + ")");
            System.out.println("2. Them mon moi");
            System.out.println("3. Cap nhat gia/ton kho mon an");
            System.out.println("4. Xu ly don hang (Xem Pending, Chap nhan, Tu choi)");
            System.out.println("5. Theo doi cac don dang xu ly");
            System.out.println("6. Xem lich su don hang (Da giao & Da huy)");
            System.out.println("7. Quan ly phan hoi khach hang");
            System.out.println("8. Thanh toan & Hoan tien (Mo phong)");
            System.out.println("0. Dang xuat");
            System.out.print("Chon: ");
            String rc = scanner.nextLine();

            if (rc.equals("1")) {
                // Gọi RestaurantController.toggleRestaurantStatus()
                restaurantController.toggleRestaurantStatus(loggedInRest);
                System.out.println("Da doi trang thai thanh: " + loggedInRest.getStatus());

            } else if (rc.equals("2")) {
                System.out.print("Ten mon moi: ");
                String itemName = scanner.nextLine();
                System.out.print("Gia ban: ");
                double price = 0;
                try { price = Double.parseDouble(scanner.nextLine()); } catch (NumberFormatException ignored) {}
                System.out.print("So luong ton kho: ");
                int stockQty = 0;
                try { stockQty = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException ignored) {}
                // Gọi RestaurantController.addMenuItem()
                restaurantController.addMenuItem(loggedInRest.getId(), itemName, price, stockQty);
                System.out.println("Them mon moi thanh cong!");

            } else if (rc.equals("3")) {
                // Gọi RestaurantController.getMenuByRestaurant()
                List<MenuItem> myMenu = restaurantController.getMenuByRestaurant(loggedInRest.getId());
                for (MenuItem m : myMenu) {
                    System.out.println(m.getId() + ". " + m.getItemName()
                            + " - " + m.getPrice() + " VND (Kho: " + m.getStockQty() + ")");
                }
                System.out.print("Nhap ID mon muon cap nhat (hoac 0 de thoat): ");
                int mid;
                try { mid = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { mid = 0; }
                if (mid > 0) {
                    System.out.print("Nhap gia moi (Enter bo qua): ");
                    String ps = scanner.nextLine();
                    System.out.print("Nhap so luong kho moi (Enter bo qua): ");
                    String ss = scanner.nextLine();
                    Double newPrice    = ps.isEmpty() ? null : Double.parseDouble(ps);
                    Integer newStock   = ss.isEmpty() ? null : Integer.parseInt(ss);
                    // Gọi RestaurantController.updateMenuItem()
                    restaurantController.updateMenuItem(mid, newPrice, newStock);
                    System.out.println("Cap nhat thanh cong!");
                }

            } else if (rc.equals("4")) {
                // Gọi RestaurantController.getOrdersByRestaurantAndStatus()
                List<Order> pendings = restaurantController
                        .getOrdersByRestaurantAndStatus(loggedInRest.getId(), OrderStatus.PENDING);
                if (pendings.isEmpty()) {
                    System.out.println("Khong co don hang moi nao!");
                } else {
                    for (Order o : pendings) {
                        System.out.println("Don #" + o.getId()
                                + " - Khach hang ID: " + o.getCustomerId()
                                + " - " + o.getTotalPrice() + " VND");
                    }
                    System.out.print("Nhap ID don muon xu ly (hoac 0 de thoat): ");
                    int oid;
                    try { oid = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { oid = 0; }
                    if (oid > 0) {
                        System.out.print("1-Chap nhan | 2-Tu choi (Het mon): ");
                        String ac = scanner.nextLine();
                        // Gọi RestaurantController.processOrder()
                        restaurantController.processOrder(oid, ac.equals("1"));
                        if (ac.equals("1")) {
                            System.out.println("Da xac nhan chuan bi don hang #" + oid);
                            orderController.dispatchOrder(oid, loggedInRest.getLatitude(), loggedInRest.getLongitude());
                        } else if (ac.equals("2")) {
                            System.out.println("Da huy don hang do tu choi phuc vu.");
                        }
                    }
                }

            } else if (rc.equals("5")) {
                // Gọi RestaurantController.getOrdersByRestaurantAndStatuses()
                List<Order> activeOrders = restaurantController.getOrdersByRestaurantAndStatuses(
                        loggedInRest.getId(), OrderStatus.CONFIRMED, OrderStatus.DELIVERING);
                if (activeOrders.isEmpty()) {
                    System.out.println("Khong co don nao dang xu ly.");
                } else {
                    for (Order o : activeOrders) {
                        System.out.println("Don #" + o.getId() + " - Trang thai: " + o.getStatus());
                    }
                }

            } else if (rc.equals("6")) {
                List<Order> deliveredOrders = restaurantController.getOrdersByRestaurantAndStatuses(
                        loggedInRest.getId(), OrderStatus.DELIVERED, OrderStatus.CANCELLED);
                System.out.println("=== LICH SU DON HANG (DA GIAO / BI HUY) ===");
                if (deliveredOrders.isEmpty()) {
                    System.out.println("Chua co don hang nao.");
                } else {
                    for (Order o : deliveredOrders) {
                        System.out.println("Don #" + o.getId()
                                + " - Trang thai: " + o.getStatus()
                                + " - Tong tien: " + o.getTotalPrice() + " VND");
                    }
                }

            } else if (rc.equals("7")) {
                System.out.println("=== DANH SACH DANH GIA TU KHACH HANG ===");
                boolean hasFb = false;
                for (Feedback fb : feedbacks) {
                    if (fb.restaurantId == loggedInRest.getId()) {
                        System.out.println(">> Don #" + fb.orderId
                                + " | Khach: " + fb.customerName
                                + " | " + fb.stars + " sao | Nhan xet: " + fb.comment);
                        hasFb = true;
                    }
                }
                if (!hasFb) System.out.println("Chua co danh gia nao.");

            } else if (rc.equals("8")) {
                System.out.print("Ban co muon thuc hien hoan tien cho don hang bi huy? (Nhap ID don hoac 0): ");
                int refId;
                try { refId = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { refId = 0; }
                if (refId > 0) {
                    System.out.println("[MO PHONG] Da hoan tien cho don #" + refId + " thanh cong!");
                }

            } else if (rc.equals("0")) {
                break;
            }
        }
    }

    // =========================================================
    // MENU TAI XE (DRIVER)
    // =========================================================

    private void handleDriverMenu() {
        Driver loggedInDriver = null;

        // Vòng lặp đăng nhập / đăng ký
        while (loggedInDriver == null) {
            System.out.println("\n--- MENU TAI XE ---");
            System.out.println("1. Dang nhap (Email & Pass)");
            System.out.println("2. Dang ky");
            System.out.println("0. Quay lai");
            System.out.print("Chon: ");
            String c = scanner.nextLine();

            if (c.equals("1")) {
                System.out.print("Nhap email: ");
                String email = scanner.nextLine();
                System.out.print("Nhap password: ");
                String pass = scanner.nextLine();
                // Gọi DriverController.login()
                loggedInDriver = driverController.login(email, hashPassword(pass));
                if (loggedInDriver == null) {
                    System.out.println("Tai khoan khong ton tai hoac sai mat khau!");
                }

            } else if (c.equals("2")) {
                System.out.print("Nhap ten: ");   String name  = scanner.nextLine();
                System.out.print("Nhap SDT: ");   String phone = scanner.nextLine();
                System.out.print("Nhap email: "); String email = scanner.nextLine();
                System.out.print("Nhap password: ");
                String hashedPass = hashPassword(scanner.nextLine());
                // Gọi DriverController.registerDriver()
                driverController.registerDriver(name, phone, email, hashedPass, 10.75, 106.65);
                System.out.println("Dang ky thanh cong!");

            } else if (c.equals("0")) {
                return;
            }
        }

        // Menu sau khi đăng nhập thành công
        while (true) {
            System.out.println("\n--- MENU TAI XE: " + loggedInDriver.getName() + " ---");
            System.out.println("Trang thai hien tai: " + loggedInDriver.getStatus());
            System.out.println("1. Cap nhat Profile (Ten, SDT)");
            System.out.println("2. Chuyen trang thai (Online / Offline)");
            System.out.println("3. Xu ly don hang duoc gan (Accept/Deny)");
            System.out.println("4. Xem don dang giao");
            System.out.println("5. Hoan thanh don & Thu tien");
            System.out.println("6. Huy don (Khach bom hang)");
            System.out.println("7. Xem lich su giao hang");
            System.out.println("8. Xem ban do tuyen duong giao hang");
            System.out.println("0. Dang xuat");
            System.out.print("Chon: ");
            String dc = scanner.nextLine();

            if (dc.equals("1")) {
                System.out.print("Ten moi (Enter bo qua): ");  String n = scanner.nextLine();
                System.out.print("SDT moi (Enter bo qua): ");  String p = scanner.nextLine();
                // Gọi DriverController.updateProfile()
                driverController.updateProfile(loggedInDriver, n, p);
                System.out.println("Cap nhat thanh cong!");

            } else if (dc.equals("2")) {
                // Gọi DriverController.toggleOnlineStatus()
                driverController.toggleOnlineStatus(loggedInDriver);
                System.out.println("Trang thai moi: " + loggedInDriver.getStatus());

            } else if (dc.equals("3")) {
                final int currentDriverId = loggedInDriver.getId();
                List<Order> proposedOrders = orderRepo.readAll().stream()
                        .filter(o -> o.getStatus() == OrderStatus.CONFIRMED && o.getDriverId() != null && o.getDriverId() == currentDriverId)
                        .collect(java.util.stream.Collectors.toList());

                if (proposedOrders.isEmpty()) {
                    System.out.println("Hien tai khong co don hang nao duoc gan cho ban.");
                    if (loggedInDriver.getStatus() != DriverStatus.AVAILABLE) {
                        System.out.println("(Luu y: Ban can doi trang thai sang AVAILABLE de duoc gán don)");
                    }
                    continue;
                }

                Order o = proposedOrders.get(0);
                
                Integer restId = null;
                List<OrderItem> orderItems = orderItemRepo.readAll().stream()
                        .filter(oi -> oi.getOrderId() == o.getId())
                        .collect(java.util.stream.Collectors.toList());
                if (!orderItems.isEmpty()) {
                    MenuItem mi = menuItemRepo.findById(orderItems.get(0).getMenuItemId());
                    if (mi != null) restId = mi.getRestaurantId();
                }
                Restaurant rest = restId != null ? restaurantRepo.findById(restId) : null;
                
                System.out.println("Ban co 1 don hang duoc gan!");
                System.out.println("Don #" + o.getId() + " - Tong: " + o.getTotalPrice() + " VND - Giao cho khach ID: " + o.getCustomerId());
                if (rest != null) {
                    System.out.println("Nha hang: " + rest.getName() + " (Cach ban: " + String.format("%.2f", com.delivery.repository.GeoUtils.calculateDistance(loggedInDriver.getLatitude(), loggedInDriver.getLongitude(), rest.getLatitude(), rest.getLongitude())) + " km)");
                }
                
                System.out.print("1-Chap nhan | 2-Tu choi: ");
                String action = scanner.nextLine();
                
                if (action.equals("1")) {
                    boolean success = orderController.acceptOrder(o.getId(), loggedInDriver.getId());
                    if (success) {
                        System.out.println("Nhan don thanh cong! Trang thai cua ban van la BUSY.");
                        loggedInDriver = driverRepo.findById(loggedInDriver.getId());
                    } else {
                        System.out.println("Co loi xay ra hoac don hang da bi huy/chuyen cho nguoi khac!");
                    }
                } else if (action.equals("2")) {
                    double rLat = rest != null ? rest.getLatitude() : 0.0;
                    double rLon = rest != null ? rest.getLongitude() : 0.0;
                    boolean success = orderController.rejectOrder(o.getId(), loggedInDriver.getId(), rLat, rLon);
                    if (success) {
                        System.out.println("Da tu choi don hang. Trang thai cua ban chuyen ve AVAILABLE.");
                        loggedInDriver = driverRepo.findById(loggedInDriver.getId());
                    } else {
                        System.out.println("Co loi xay ra khi tu choi don hang!");
                    }
                }

            } else if (dc.equals("4")) {
                final int did = loggedInDriver.getId();
                List<Order> myDeliveries = orderRepo.readAll().stream()
                        .filter(o -> o.getDriverId() != null
                                && o.getDriverId() == did
                                && o.getStatus() == OrderStatus.DELIVERING)
                        .collect(java.util.stream.Collectors.toList());
                if (myDeliveries.isEmpty()) {
                    System.out.println("Ban khong co don nao dang giao.");
                } else {
                    for (Order o : myDeliveries) {
                        // Gọi CustomerController.getCustomerById()
                        Customer cust = customerController.getCustomerById(o.getCustomerId());
                        String addr   = (cust != null) ? cust.getAddress() : "N/A";
                        System.out.println("Don #" + o.getId()
                                + " - Giao toi: " + addr
                                + " - Can thu: " + o.getTotalPrice() + " VND");
                    }
                }

            } else if (dc.equals("5")) {
                final int did = loggedInDriver.getId();
                List<Order> myDeliveries = orderRepo.readAll().stream()
                        .filter(o -> o.getDriverId() != null
                                && o.getDriverId() == did
                                && o.getStatus() == OrderStatus.DELIVERING)
                        .collect(java.util.stream.Collectors.toList());
                if (myDeliveries.isEmpty()) {
                    System.out.println("Khong co don dang giao de hoan thanh.");
                } else {
                    for (Order o : myDeliveries) {
                        System.out.println("Don #" + o.getId() + " - Can thu: " + o.getTotalPrice() + " VND");
                    }
                    System.out.print("Nhap ID don da giao xong (hoac 0 de thoat): ");
                    int oid;
                    try { oid = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { oid = 0; }
                    if (oid > 0) {
                        final int finalOid = oid;
                        Order o = myDeliveries.stream()
                                .filter(order -> order.getId() == finalOid).findFirst().orElse(null);
                        if (o != null) {
                            System.out.println("[MO PHONG] Ma QR thu tien da duoc hien thi cho khach.");
                            System.out.println("Khach hang thanh toan thanh cong!");
                            // Gọi OrderController.deliverOrder() để cập nhật trạng thái DELIVERED
                            orderController.deliverOrder(o.getId());
                            loggedInDriver.setStatus(DriverStatus.AVAILABLE);
                            // Gọi DriverController.saveDriver()
                            driverController.saveDriver(loggedInDriver);
                            System.out.println("Hoan thanh giao hang! Trang thai cua ban la AVAILABLE.");
                        }
                    }
                }

            } else if (dc.equals("6")) {
                final int did = loggedInDriver.getId();
                List<Order> myDeliveries = orderRepo.readAll().stream()
                        .filter(o -> o.getDriverId() != null
                                && o.getDriverId() == did
                                && o.getStatus() == OrderStatus.DELIVERING)
                        .collect(java.util.stream.Collectors.toList());
                if (myDeliveries.isEmpty()) {
                    System.out.println("Khong co don dang giao de huy.");
                } else {
                    System.out.print("Nhap ID don muon huy do khach bom hang (hoac 0 de thoat): ");
                    int oid;
                    try { oid = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { oid = 0; }
                    if (oid > 0) {
                        final int finalOid = oid;
                        Order o = myDeliveries.stream()
                                .filter(order -> order.getId() == finalOid).findFirst().orElse(null);
                        if (o != null) {
                            o.setDriverId(null);
                            o.setStatus(OrderStatus.CONFIRMED); // Đẩy lại lên hệ thống
                            // Gọi OrderController.saveOrder()
                            orderController.saveOrder(o);
                            loggedInDriver.setStatus(DriverStatus.AVAILABLE);
                            // Gọi DriverController.saveDriver()
                            driverController.saveDriver(loggedInDriver);
                            System.out.println("Da huy nhan don! Ban da duoc chuyen lai trang thai AVAILABLE.");
                        }
                    }
                }

            } else if (dc.equals("7")) {
                final int did = loggedInDriver.getId();
                List<Order> pastDeliveries = orderRepo.readAll().stream()
                        .filter(o -> o.getDriverId() != null
                                && o.getDriverId() == did
                                && o.getStatus() == OrderStatus.DELIVERED)
                        .collect(java.util.stream.Collectors.toList());
                if (pastDeliveries.isEmpty()) {
                    System.out.println("Chua co lich su giao hang nao.");
                } else {
                    for (Order o : pastDeliveries) {
                        System.out.println("Don #" + o.getId()
                                + " - Tri gia: " + o.getTotalPrice()
                                + " - Da giao thanh cong.");
                    }
                }

            } else if (dc.equals("8")) {
                final int did = loggedInDriver.getId();
                List<Order> myDeliveries = orderRepo.readAll().stream()
                        .filter(o -> o.getDriverId() != null
                                && o.getDriverId() == did
                                && o.getStatus() == OrderStatus.DELIVERING)
                        .collect(java.util.stream.Collectors.toList());
                
                if (myDeliveries.isEmpty()) {
                    System.out.println("Ban khong co don dang giao.");
                    continue;
                }
                
                System.out.println("\n--- CAC DON DANG GIAO ---");
                for (Order o : myDeliveries) {
                    System.out.println("Don #" + o.getId() + " - Giao cho khach: " + o.getCustomerId() + " - Tong: " + o.getTotalPrice());
                }
                System.out.print("Chon don de xem ban do (hoac 0 de thoat): ");
                int mapOrderIdInput;
                try { mapOrderIdInput = Integer.parseInt(scanner.nextLine()); }
                catch (NumberFormatException e) { mapOrderIdInput = 0; }
                final int mapOrderId = mapOrderIdInput;
                
                if (mapOrderId > 0) {
                    Order selectedOrder = myDeliveries.stream()
                            .filter(o -> o.getId() == mapOrderId).findFirst().orElse(null);
                    if (selectedOrder != null) {
                        Customer cust = customerRepo.findById(selectedOrder.getCustomerId());
                        
                        // Lay ID nha hang tu OrderItem -> MenuItem
                        Integer restId = null;
                        List<OrderItem> orderItems = orderItemRepo.readAll().stream()
                                .filter(oi -> oi.getOrderId() == selectedOrder.getId())
                                .collect(java.util.stream.Collectors.toList());
                        if (!orderItems.isEmpty()) {
                            MenuItem mi = menuItemRepo.findById(orderItems.get(0).getMenuItemId());
                            if (mi != null) restId = mi.getRestaurantId();
                        }
                        Restaurant rest = restId != null ? restaurantRepo.findById(restId) : null;
                        
                        // Hien thi chi tiet don hang
                        OrderView orderView = new OrderView("data/order_items.csv");
                        orderView.printOrderDetail(selectedOrder, cust, rest, loggedInDriver);
                        
                        // Hien thi ban do
                        System.out.println();
                        MapView mapView = new MapView();
                        if (cust != null) mapView.setCustomer(cust.getLatitude(), cust.getLongitude(), cust.getId());
                        if (rest != null) mapView.setRestaurant(rest.getLatitude(), rest.getLongitude(), rest.getId());
                        mapView.setDriver(loggedInDriver.getLatitude(), loggedInDriver.getLongitude(), loggedInDriver.getId());
                        
                        // Ve tuyen duong
                        if (rest != null && cust != null) {
                            mapView.drawDeliveryRoute(rest.getLatitude(), rest.getLongitude(), cust.getLatitude(), cust.getLongitude());
                        }
                        
                        mapView.printMap();
                        
                        // Hien thi danh sach mon an
                        List<MenuItem> items = menuItemRepo.readAll();
                        orderView.printOrderItems(selectedOrder.getId(), items, orderItems);
                        orderView.printPriceSummary(selectedOrder);
                    }
                }

            } else if (dc.equals("0")) {
                break;
            }
        }
    }

    // =========================================================
    // MENU ADMIN
    // =========================================================

    private void handleAdminMenu() {
        System.out.print("Nhap mat khau Admin: ");
        if (!scanner.nextLine().equals("admin")) {
            System.out.println("Sai mat khau!");
            return;
        }

        while (true) {
            System.out.println("\n--- MENU ADMIN ---");
            System.out.println("1. Xem thong ke nguoi dung");
            System.out.println("2. Xem thong ke nha hang");
            System.out.println("3. Xem thong ke tai xe");
            System.out.println("4. Xem thong ke don hang");
            System.out.println("5. Xem Dashboard tong quan");
            System.out.println("6. Chinh sua thong tin Khach hang");
            System.out.println("7. Chinh sua thong tin Nha hang");
            System.out.println("8. Chinh sua thong tin Tai xe");
            System.out.println("0. Dang xuat");
            System.out.print("Chon: ");
            String ac = scanner.nextLine();

            if (ac.equals("1")) {
                displayCustomersAdmin();
            } else if (ac.equals("2")) {
                displayRestaurantsAdmin();
            } else if (ac.equals("3")) {
                displayDriversAdmin();
            } else if (ac.equals("4")) {
                displayOrdersAdmin();
            } else if (ac.equals("5")) {
                displayDashboardAdmin();
            } else if (ac.equals("6")) {
                editCustomerAdmin();
            } else if (ac.equals("7")) {
                editRestaurantAdmin();
            } else if (ac.equals("8")) {
                editDriverAdmin();
            } else if (ac.equals("0")) {
                break;
            } else {
                System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private void displayCustomersAdmin() {
        List<Customer> customers = customerController.getAllCustomers();
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          DANH SACH KHACH HANG                                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");
        
        if (customers.isEmpty()) {
            System.out.println("║ Khong co khach hang nao!                                                       ║");
            System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
            return;
        }
        
        System.out.println(String.format("║ %-5s │ %-15s │ %-12s │ %-25s │ %-15s │", 
                "ID", "Ten", "Dien thoai", "Email", "Tao dia chi"));
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");
        
        for (Customer c : customers) {
            System.out.println(String.format("║ %-5d │ %-15s │ %-12s │ %-25s │ %-15s │", 
                    c.getId(), 
                    truncate(c.getName(), 15),
                    c.getPhone(),
                    truncate(c.getEmail(), 25),
                    truncate(c.getAddress(), 15)));
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("Tong cong: " + customers.size() + " khach hang");
    }

    private void displayRestaurantsAdmin() {
        List<Restaurant> restaurants = restaurantRepo.readAll();
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          DANH SACH NHA HANG                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        
        if (restaurants.isEmpty()) {
            System.out.println("║ Khong co nha hang nao!                                                   ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
            return;
        }
        
        System.out.println(String.format("║ %-4s │ %-18s │ %-12s │ %-15s │ %-12s │ %-6s │", 
                "ID", "Ten", "Dien thoai", "Trang thai", "Dia chi", "Rating"));
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        
        for (Restaurant r : restaurants) {
            System.out.println(String.format("║ %-4d │ %-18s │ %-12s │ %-15s │ %-12s │ %-6.1f │", 
                    r.getId(),
                    truncate(r.getName(), 18),
                    r.getPhone(),
                    r.getStatus().toString(),
                    truncate(r.getAddress(), 12),
                    r.getRating()));
        }
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("Tong cong: " + restaurants.size() + " nha hang");
    }

    private void displayDriversAdmin() {
        List<Driver> drivers = driverRepo.readAll();
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          DANH SACH TAI XE                                 ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        
        if (drivers.isEmpty()) {
            System.out.println("║ Khong co tai xe nao!                                                     ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
            return;
        }
        
        System.out.println(String.format("║ %-4s │ %-15s │ %-12s │ %-15s │ %-12s │ %-10s │", 
                "ID", "Ten", "Dien thoai", "Trang thai", "Tien QR", "Kinh do"));
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        
        for (Driver d : drivers) {
            System.out.println(String.format("║ %-4d │ %-15s │ %-12s │ %-15s │ %-12.0f │ %-10.2f │", 
                    d.getId(),
                    truncate(d.getName(), 15),
                    d.getPhone(),
                    d.getStatus().toString(),
                    d.getCollectedQrMoney(),
                    d.getLongitude()));
        }
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("Tong cong: " + drivers.size() + " tai xe");
    }

    private void displayOrdersAdmin() {
        List<Order> orders = orderRepo.readAll();
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          DANH SACH DON HANG                                ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        if (orders.isEmpty()) {
            System.out.println("║ Khong co don hang nao!                                                    ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
            return;
        }
        
        System.out.println(String.format("║ %-4s │ %-8s │ %-8s │ %-15s │ %-12s │ %-12s │", 
                "ID", "Khach", "TaiXe", "Tien", "Thanh toan", "Trang thai"));
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        for (Order o : orders) {
            System.out.println(String.format("║ %-4d │ %-8d │ %-8d │ %-15.0f │ %-12s │ %-12s │", 
                    o.getId(),
                    o.getCustomerId(),
                    o.getDriverId() != null ? o.getDriverId() : 0,
                    o.getTotalPrice(),
                    o.getPaymentMethod(),
                    o.getStatus().toString()));
        }
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("Tong cong: " + orders.size() + " don hang");
    }

    private void displayDashboardAdmin() {
        List<Order> orders = orderRepo.readAll();
        double totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalPrice)
                .sum();
        
        long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long confirmed = orders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long delivering = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERING).count();
        long delivered = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          DASHBOARD TONG QUAN                         ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ Tong doanh thu (Da giao): %-42.0f VND ║", totalRevenue));
        System.out.println(String.format("║ Tong so don hang:         %-42d ║", orders.size()));
        System.out.println(String.format("║   - Cho xu ly (PENDING):  %-42d ║", pending));
        System.out.println(String.format("║   - Da xac nhan (CONFIRM):%-42d ║", confirmed));
        System.out.println(String.format("║   - Dang giao (DELIVERING):%-41d ║", delivering));
        System.out.println(String.format("║   - Da giao (DELIVERED):  %-42d ║", delivered));
        System.out.println(String.format("║   - Da huy (CANCELLED):   %-42d ║", cancelled));
        System.out.println(String.format("║ Tong so khach hang:       %-42d ║", customerRepo.readAll().size()));
        System.out.println(String.format("║ Tong so nha hang:         %-42d ║", restaurantRepo.readAll().size()));
        System.out.println(String.format("║ Tong so tai xe:           %-42d ║", driverRepo.readAll().size()));
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
    }

    private void editCustomerAdmin() {
        System.out.print("Nhap ID khach hang can sua: ");
        int cid;
        try { cid = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { System.out.println("ID khong hop le!"); return; }
        Customer c = customerRepo.findById(cid);
        if (c == null) {
            System.out.println("Khong tim thay khach hang!");
            return;
        }
        System.out.println("Dang sua Khach hang: " + c.getName());
        System.out.print("Ten moi (Enter de giu nguyen): "); String name = scanner.nextLine();
        System.out.print("SDT moi (Enter de giu nguyen): "); String phone = scanner.nextLine();
        System.out.print("Dia chi moi (Enter de giu nguyen): "); String address = scanner.nextLine();
        
        if (!name.isEmpty()) c.setName(name);
        if (!phone.isEmpty()) c.setPhone(phone);
        if (!address.isEmpty()) c.setAddress(address);
        
        customerRepo.update(c);
        System.out.println("Cap nhat khach hang thanh cong!");
    }

    private void editRestaurantAdmin() {
        System.out.print("Nhap ID nha hang can sua: ");
        int rid;
        try { rid = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { System.out.println("ID khong hop le!"); return; }
        Restaurant r = restaurantRepo.findById(rid);
        if (r == null) {
            System.out.println("Khong tim thay nha hang!");
            return;
        }
        System.out.println("Dang sua Nha hang: " + r.getName());
        System.out.print("Ten moi (Enter de giu nguyen): "); String name = scanner.nextLine();
        System.out.print("SDT moi (Enter de giu nguyen): "); String phone = scanner.nextLine();
        System.out.print("Trang thai moi (OPEN/CLOSED, Enter de giu nguyen): "); String status = scanner.nextLine();
        
        if (!name.isEmpty()) r.setName(name);
        if (!phone.isEmpty()) r.setPhone(phone);
        if (!status.isEmpty()) {
            try {
                r.setStatus(RestaurantStatus.valueOf(status.toUpperCase()));
            } catch (Exception e) {
                System.out.println("Trang thai khong hop le!");
            }
        }
        
        restaurantRepo.update(r);
        System.out.println("Cap nhat nha hang thanh cong!");
    }

    private void editDriverAdmin() {
        System.out.print("Nhap ID tai xe can sua: ");
        int did;
        try { did = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { System.out.println("ID khong hop le!"); return; }
        Driver d = driverRepo.findById(did);
        if (d == null) {
            System.out.println("Khong tim thay tai xe!");
            return;
        }
        System.out.println("Dang sua Tai xe: " + d.getName());
        System.out.print("Ten moi (Enter de giu nguyen): "); String name = scanner.nextLine();
        System.out.print("SDT moi (Enter de giu nguyen): "); String phone = scanner.nextLine();
        System.out.print("Trang thai moi (AVAILABLE/BUSY/OFFLINE, Enter de giu nguyen): "); String status = scanner.nextLine();
        System.out.print("Tien thu ho (QR) moi (Enter de giu nguyen): "); String money = scanner.nextLine();

        if (!name.isEmpty()) d.setName(name);
        if (!phone.isEmpty()) d.setPhone(phone);
        if (!status.isEmpty()) {
            try {
                d.setStatus(DriverStatus.valueOf(status.toUpperCase()));
            } catch (Exception e) {
                System.out.println("Trang thai khong hop le!");
            }
        }
        if (!money.isEmpty()) {
            try { d.setCollectedQrMoney(Double.parseDouble(money)); } catch (NumberFormatException e) { System.out.println("Tien khong hop le!"); }
        }
        
        driverRepo.update(d);
        System.out.println("Cap nhat tai xe thanh cong!");
    }


    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 2) + "..";
    }
}
